/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.common.api.JBossResourceException;
import org.jboss.jca.core.connectionmanager.ConnectionCounter;
import org.jboss.jca.core.connectionmanager.ConnectionValidator;
import org.jboss.jca.core.connectionmanager.IdleConnectionRemovalSupport;
import org.jboss.jca.core.connectionmanager.IdleRemover;
import org.jboss.jca.core.connectionmanager.exception.RetryableResourceException;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.util.UnreachableStatementException;


/**
 * Actual internal managed connection pool.
 * 
 * <p>
 * Contains and manages the {@link ConnectionListener} instances.
 * Each pool strategy can contains several {@link SubPoolContext} instance
 * that contains {@link InternalManagedConnectionPool} internally.
 * </p>
 * 
 * <p>
 * Each internal managed connection pool instances could be
 * differentiated by a key.
 * </p>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 *   
 * @version $Rev$ $Date$
 * 
 * @see AbstractPool
 */
public class InternalManagedConnectionPool implements IdleConnectionRemovalSupport
{
   /** The log */
   private static Logger log = Logger.getLogger(InternalManagedConnectionPool.class);   
   
   /** The managed connection factory */
   private final ManagedConnectionFactory mcf;

   /** The connection listener factory */
   private final ConnectionListenerFactory clf;

   /** The default subject */
   private final Subject defaultSubject;

   /** The default connection request information */
   private final ConnectionRequestInfo defaultCri;

   /** The pooling parameters */
   private final PoolParams poolParams;

   /** Copy of the maximum size from the pooling parameters.
    * Dynamic changes to this value are not compatible with
    * the semaphore which cannot change be dynamically changed.
    */
   private int maxSize;

   /** The available connection event listeners */
   private CopyOnWriteArrayList<ConnectionListener> cls = new CopyOnWriteArrayList<ConnectionListener>();

   /** The permits used to control who can checkout a connection */
   private final Semaphore permits;

   /** Whether trace is enabled */
   private final boolean trace = log.isTraceEnabled();

   /** Stats */
   private final ConnectionCounter connectionCounter = new ConnectionCounter();

   /** The checked out connections */
   private final CopyOnWriteArraySet<ConnectionListener> checkedOut = new CopyOnWriteArraySet<ConnectionListener>();

   /** Whether the pool has been started */
   private boolean started = false;

   /** Whether the pool has been shutdown */
   private AtomicBoolean shutdown = new AtomicBoolean(false);

   /** the max connections ever checked out **/
   private volatile int maxUsedConnections = 0;
   
   /**
    * Create a new InternalManagedConnectionPool.
    * 
    * @param mcf the managed connection factory
    * @param clf the connection listener factory
    * @param subject the subject
    * @param cri the connection request info
    * @param poolParams the pool parameters
    */
   public InternalManagedConnectionPool(ManagedConnectionFactory mcf, ConnectionListenerFactory clf, Subject subject,
         ConnectionRequestInfo cri, PoolParams poolParams)
   {
      this.mcf = mcf;
      this.clf = clf;
      this.defaultSubject = subject;
      this.defaultCri = cri;
      this.poolParams = poolParams;
      this.maxSize = poolParams.getMaxSize();
      this.permits = new Semaphore(this.maxSize, true);
  
      if (poolParams.isPrefill())
      {
         PoolFiller.fillPool(this);
      }
      
   }
   
   /**
    * Returns a connection listener that wraps managed connection.
    * @param subject subject
    * @param cri connection request info
    * @return connection listener wrapped managed connection
    * @throws ResourceException exception
    */
   public ConnectionListener getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      ConnectionListener connectionListener = null;
      
      if (subject == null)
      {
         subject = this.defaultSubject;
      }
      
      if (cri == null)
      {
         cri = this.defaultCri;
      }
      
      //Use in blocked time
      long startWait = System.currentTimeMillis();
      
      try
      {
         //Update blocked time
         this.connectionCounter.updateBlockTime(System.currentTimeMillis() - startWait);

         //Check connection is available, and if not waits for the blocking timeout
         if (this.permits.tryAcquire(this.poolParams.getBlockingTimeout(), TimeUnit.MILLISECONDS))
         {
            long poolBlockTime = (System.currentTimeMillis() - startWait);
            this.connectionCounter.updateBlockTime(poolBlockTime);
            
            do
            {
               //Check shutdown
               if (this.shutdown.get())
               {
                  permits.release();
                  throw new RetryableResourceException("The pool has been shut down");
               }
               
               if (cls.size() > 0)
               {
                  connectionListener = this.cls.remove(this.cls.size() - 1);
                  this.checkedOut.add(connectionListener);
                  
                  //Max used connections, maxSize - permits.aval --> gives current used connection!
                  int size = (int) (maxSize - permits.availablePermits());
                  if (size > maxUsedConnections)
                  {
                     maxUsedConnections = size;  
                  }
                  
                  if (connectionListener != null)
                  {
                     try
                     {
                        //Match connection
                        ConnectionListener matchedConnectionListener = 
                           isManagedConnectionMatched(connectionListener, subject, cri);
                        
                        //Connection matched
                        if (matchedConnectionListener != null)
                        {
                           connectionListener = matchedConnectionListener;
                           break;
                        }
                        
                        //Match did not succeed but no exception was thrown.
                        //Either we have the matching strategy wrong or the
                        //connection died while being checked.  We need to
                        //distinguish these cases, but for now we always
                        //destroy the connection.
                        log.warn("Destroying connection that could not be successfully matched: " + connectionListener);
                        removesAndDestorysConnectionListener(connectionListener);
                        
                     }
                     catch (Throwable t)
                     {
                        log.warn("Throwable while trying to match ManagedConnection,destroying connection: "
                              + connectionListener, t);
                        removesAndDestorysConnectionListener(connectionListener);
                     }
                     
                     //We made it here, something went wrong and we should validate if 
                     //we should continue attempting to acquire a connection
                     if (this.poolParams.isUseFastFail())
                     {
                        log.trace("Fast failing for connection attempt. No more attempts will " +
                              "be made to acquire connection from pool and a new connection " +
                              "will be created immeadiately");
                        break;
                     }
                     
                  } //connectionListener != null
               } //cls.size > 0               
            }
            while (this.cls.size() > 0);
            
            //Check connection
            if (connectionListener == null)
            {
               //Ok, no connection in the pool. Creates a new managed connection instance!
               connectionListener = createsNewManagedConnection(subject, cri);               
            }
            
         }
         else
         {
            // we timed out
            throw new ResourceException("No ManagedConnections available within configured blocking timeout ( "
                  + this.poolParams.getBlockingTimeout() + " [ms] )");            
         }
         
      } 
      catch (InterruptedException e)
      {
         long end = System.currentTimeMillis() - startWait;
         connectionCounter.updateBlockTime(end);
         
         throw new ResourceException("Interrupted while requesting permit! Waited " + end + " ms");         
      }
      
      return connectionListener;
   }
   
   /**
    * Removes and destroys given connection.
    * @param connectionListener connection listener
    */
   private void removesAndDestorysConnectionListener(ConnectionListener connectionListener)
   {
      this.checkedOut.remove(connectionListener);
      
      //Destroy it
      doDestroy(connectionListener);
      connectionListener = null;                              
   }
   
   /**
    * Returns given listener if there is a matched connection false ow.
    * @param connectionListener connection listener
    * @param subject subject
    * @param cri connection request info
    * @return true if there is a matched connection false ow.
    */
   private ConnectionListener isManagedConnectionMatched(ConnectionListener connectionListener, 
         Subject subject, ConnectionRequestInfo cri)  throws ResourceException   
   {
      ManagedConnection managedConnection = connectionListener.getManagedConnection();
      managedConnection = this.mcf.matchManagedConnections(Collections.singleton(managedConnection), subject , cri);
      
      //There is a match
      if (managedConnection != null)
      {
         if (trace)
         {
            log.trace("supplying ManagedConnection from pool: " + connectionListener);  
         }
         
         connectionListener.grantPermit(true);
         
         return connectionListener;
      }
      
      return null;
   }
   
   /**
    * Creates a new connection listener.
    * @param subject subject instance
    * @param cri connection request info
    * @return new connection listener
    * @throws ResourceException
    */
   private ConnectionListener createsNewManagedConnection(Subject subject, ConnectionRequestInfo cri) 
      throws ResourceException
   {
      ConnectionListener cl = null;
      try
      {
         //No, the pool was empty, so we have to make a new one.
         cl = createsConnectionEventListener(subject, cri);

         checkedOut.add(cl);
         int size = (int) (maxSize - permits.availablePermits());
         if (size > maxUsedConnections)
         {
            maxUsedConnections = size;  
         }

         //lack of synch on "started" probably ok, if 2 reads occur we will just
         //run fillPool twice, no harm done.
         if (!started)
         {
            started = true;
            if (poolParams.getMinSize() > 0)
            {
               PoolFiller.fillPool(this);  
            }
         }
         if (trace)
         {
            log.trace("supplying new ManagedConnection: " + cl);  
         }
         
         cl.grantPermit(true);
         
         return cl;
      }
      catch (Throwable t)
      {
         log.warn("Throwable while attempting to get a new connection: " + cl, t);
         //return permit and rethrow
         
         checkedOut.remove(cl);
         permits.release();
         JBossResourceException.rethrowAsResourceException("Unexpected throwable while trying to create a connection: " 
               + cl, t);
         throw new UnreachableStatementException();
      }      
   }
   
   
   /**
    * Create a connection event listener
    *
    * @param subject the subject
    * @param cri the connection request information
    * @return the new listener
    * @throws ResourceException for any error
    */
   private ConnectionListener createsConnectionEventListener(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      ManagedConnection mc = mcf.createManagedConnection(subject, cri);
      connectionCounter.inc();
      try
      {
         return clf.createConnectionListener(mc, this);
      }
      catch (ResourceException re)
      {
         connectionCounter.dec();
         mc.destroy();
         throw re;
      }
   }
   
   
   /**
    * Destroy a connection
    *
    * @param cl the connection to destroy
    */
   private void doDestroy(ConnectionListener cl)
   {
      if (cl.getState() == ConnectionState.DESTROYED)
      {
         log.trace("ManagedConnection is already destroyed " + cl);
         return;
      }

      connectionCounter.dec();
      cl.setState(ConnectionState.DESTROYED);
      
      try
      {
         cl.getManagedConnection().destroy();
      }
      catch (Throwable t)
      {
         log.debug("Exception destroying ManagedConnection " + cl, t);
      }

   }   
   
   /**
    * {@inheritDoc}
    */
   public void removeIdleConnections()
   {
      ArrayList<ConnectionListener> destroy = null;
      long timeout = System.currentTimeMillis() - poolParams.getIdleTimeout();
      
      while (true)
      {
         // Nothing left to destroy
         if (cls.size() == 0)
            break;

         // Check the first in the list
         ConnectionListener cl = (ConnectionListener) cls.get(0);
         if (cl.isTimedOut(timeout) && shouldRemove())
         {
            connectionCounter.incTimedOutCount();
            // We need to destroy this one
            cls.remove(0);
            if (destroy == null)
            {
               destroy = new ArrayList<ConnectionListener>();  
            }
            
            destroy.add(cl);
         }
         else
         {
            //They were inserted chronologically, so if this one isn't timed out, following ones won't be either.
            break;
         }
      }

      // We found some connections to destroy
      if (destroy != null)
      {
         for (int i = 0; i < destroy.size(); ++i)
         {
            ConnectionListener cl = (ConnectionListener) destroy.get(i);
            if (trace)
            {
               log.trace("Destroying timedout connection " + cl);  
            }
            
            doDestroy(cl);
         }

         // We destroyed something, check the minimum.
         if (!shutdown.get() && poolParams.getMinSize() > 0)
         {
            PoolFiller.fillPool(this);  
         }

//         // Empty sub-pool
//         if (jmcp != null)
//         {
//            jmcp.getPoolingStrategy().emptySubPool(this);  
//         }
      }
      
   }
   
   /**
    * Returns true if check is ok.
    * @return true if check is ok.
    */
   private boolean shouldRemove()
   {      
      boolean remove = true;
      
      if (this.poolParams.isStrictMin())
      {
         remove = cls.size() > poolParams.getMinSize();
         
         log.trace("StrictMin is active. Current connection will be removed is " + remove);
         
      }
      
      return remove;
      
   }
   
   
   /**
    * Initialize the subpool
    */
   public void initialize()
   {
      if (this.poolParams.getIdleTimeout() != 0L)
      {
         //Register removal support
         IdleRemover.registerPool(this, this.poolParams.getIdleTimeout());
      }
      
      if (this.poolParams.getBackgroundValidationInterval() > 0)
      {
         log.debug("Registering for background validation at interval " + 
               this.poolParams.getBackgroundValidationInterval());         
         
         //Register validation
         ConnectionValidator.registerPool(this, this.poolParams.getBackgroundValidationInterval());
      }

      shutdown.set(false);      
   }
   
   /**
    * Return connection to the pool.
    * @param cl connection listener
    * @param kill kill connection
    */
   public void returnConnection(ConnectionListener cl, boolean kill)
   {
      if (cl.getState().equals(ConnectionState.DESTROYED))
      {
         returnConnectionWithDestroyedState(cl);
         return;
      }
      
      if (trace)
      {
         log.trace("putting ManagedConnection back into pool kill=" + kill + " cl=" + cl); 
      }
      
      returnConnectionWithKillState(cl, kill);
      
   }
   
   /**
    * Connection is returned with destroyed state.
    * @param cl connection listener
    */
   private void returnConnectionWithDestroyedState(ConnectionListener cl)
   {
      if (this.trace)
      {
         log.trace("ManagedConnection is being returned after it was destroyed" + cl);
      }
      
      if (cl.hasPermit())
      {
         cl.grantPermit(false);
         this.permits.release();
      }            
   }
   
   /**
    * Connection is returned with destroyed state.
    * @param cl connection listener
    */
   private void returnConnectionWithKillState(ConnectionListener cl, boolean kill)
   {
      try
      {
         cl.getManagedConnection().cleanup();
      }
      catch (ResourceException re)
      {
         log.warn("ResourceException cleaning up ManagedConnection: " + cl, re);
         kill = true;
      }
      
      // We need to destroy this one
      if (cl.getState().equals(ConnectionState.DESTROY))
      {
         kill = true;
         checkedOut.remove(cl);
      }

      // This is really an error
      if (!kill && cls.size() >= poolParams.getMaxSize())
      {
         log.warn("Destroying returned connection, maximum pool size exceeded " + cl);
         kill = true;
      }

      // If we are destroying, check the connection is not in the pool
      if (kill)
      {
         // Adrian Brock: A resource adapter can asynchronously notify us that
         // a connection error occurred.
         // This could happen while the connection is not checked out.
         // e.g. JMS can do this via an ExceptionListener on the connection.
         // I have twice had to reinstate this line of code, PLEASE DO NOT REMOVE IT!
         cls.remove(cl);
      }
      // return to the pool
      else
      {
         cl.used();
         if (!cls.contains(cl))
         {
            cls.add(cl);  
         }
         else
         {
            log.warn("Attempt to return connection twice (ignored): " + cl, new Throwable("STACKTRACE"));  
         }
      }

      if (cl.hasPermit())
      {
         // release semaphore
         cl.grantPermit(false);
         permits.release();
      }

      if (kill)
      {
         if (trace)
         {
            log.trace("Destroying returned connection " + cl);  
         }
         
         doDestroy(cl);
      }
      
   }
   
   
   /**
    * Gets connection in use count.
    * @return connection in use
    */
   public int getConnectionInUseCount()
   { 
      return this.checkedOut.size();
   }
   
   /**
    * Gets total connection for this pool.
    * @return total connection
    */
   public int getConnectionCount()
   {
      return this.connectionCounter.getCount();
   }
   
   /**
    * Gets total block time.
    * @return total block time
    */
   public long getTotalBlockTime()
   {
      return this.connectionCounter.getTotalBlockTime();
   }
   
   /**
    * Gets timed out.
    * @return timed out
    */
   public int getTimedOutCount()
   {
      return this.connectionCounter.getTimedOutCount();
   }
   
   /**
    * Gets average blocked time.
    * @return average block time.
    */
   public long getAverageBlockTime()
   {
      return this.connectionCounter.getTotalBlockTime() / getConnectionCreatedCount();
   }
   
   /**
    * Gets max wait time.
    * @return max wait time.
    */
   public long getMaxWaitTime()
   {
      return this.connectionCounter.getMaxWaitTime();
   }

   /**
    * Gets connection created count.
    * @return connection created count
    */
   public int getConnectionCreatedCount()
   {
      return this.connectionCounter.getCreatedCount();
   }

   /**
    * Gets connection destroyed count.
    * @return connection destroyed count
    */
   public int getConnectionDestroyedCount()
   {
      return this.connectionCounter.getDestroyedCount();
   }
   
   /**
    * Gets available connections.
    * @return available connections
    */
   public long getAvailableConnections()
   {
      return permits.availablePermits();
   }
   
   /**
    * Gets max connection in-use count.
    * @return max connection in-use count
    */
   public int getMaxConnectionsInUseCount()
   {
      return this.maxUsedConnections;
   }
   
   /**
    * Pool is shut down.
    */
   public void shutdown()
   {
      shutdown.set(true);
      
      //Unregister from idle check
      IdleRemover.unregisterPool(this);
      
      //Unregister from connection validation check
      ConnectionValidator.unregisterPool(this);
      
      //Destroy connections
      flush();
   }
   
   /**
    * Flush pool.
    */
   public void flush()
   {
      ArrayList<ConnectionListener> destroyList = new ArrayList<ConnectionListener>();
      
      if (this.trace)
      {
         log.trace("Flushing pool checkedOut=" + checkedOut + " inPool=" + cls);
      }
      
      Iterator<ConnectionListener> itCheckOut = this.checkedOut.iterator();
      ConnectionListener listener = null;
      while (itCheckOut.hasNext())
      {
         listener = itCheckOut.next();
         listener.setState(ConnectionState.DESTROY);
      }
      
      itCheckOut = this.cls.iterator();
      while (itCheckOut.hasNext())
      {
         listener = itCheckOut.next();
         destroyList.add(listener);
      }
      
      for (ConnectionListener listenerDestroy : destroyList)
      {
         if (this.trace)
         {
            log.trace("Destroying flushed connection " + listenerDestroy);
         }
         
         doDestroy(listenerDestroy);
      }
      
      // We destroyed something, check the minimum.
      if (!shutdown.get() && poolParams.getMinSize() > 0)
      {
         PoolFiller.fillPool(this);  
      }      
   }   
   
   /**
    * Checks that pool is empty or not
    * @return true if is emtpy false otherwise
    */
   boolean isEmpty()
   {
      return this.cls.size() == 0;
   }
   
   /**
    * Gets connection listeners.
    * @return connection listeners
    */
   Set<ConnectionListener> getConnectionListeners()
   {
      Set<ConnectionListener> cls = new HashSet<ConnectionListener>();
      Iterator<ConnectionListener> it = this.cls.iterator();
      while (it.hasNext())
      {
         cls.add(it.next());
      }
      
      it = this.checkedOut.iterator();
      while (it.hasNext())
      {
         cls.add(it.next());
      }
      
      return cls;
   }
   
   /**
    * Returns true if pool is not shut down.
    * @return true if pool is not shut down
    */
   public boolean isRunning()
   {
      return !shutdown.get();
   }
   
   /**
    * Fill to min.
    */
   public void fillToMin()
   {
      while (true)
      {
         // Get a permit - avoids a race when the pool is nearly full
         // Also avoids unnessary fill checking when all connections are checked out
         try
         {
            if (permits.tryAcquire(poolParams.getBlockingTimeout(), TimeUnit.MILLISECONDS))
            {
               try
               {
                  //pool shuts down
                  if (shutdown.get())
                  {
                     return;  
                  }

                  // We already have enough connections
                  if (getMinSize() - connectionCounter.getGuaranteedCount() <= 0)
                  {
                     return;  
                  }

                  // Create a connection to fill the pool
                  try
                  {
                     ConnectionListener cl = createsConnectionEventListener(defaultSubject, defaultCri);
                     if (trace)
                     {
                        log.trace("Filling pool cl=" + cl);  
                     }
                     
                     cls.add(cl);
                  }
                  catch (ResourceException re)
                  {
                     log.warn("Unable to fill pool ", re);
                     return;
                  }
               }
               finally
               {
                  permits.release();
               }
            }
         }
         catch (InterruptedException ignored)
         {
            log.trace("Interrupted while requesting permit in fillToMin");
         }
      }      
   }
   
   /**
    * Guard against configurations or
    * dynamic changes that may increase the minimum
    * beyond the maximum
    */
   private int getMinSize()
   {
      if (this.poolParams.getMinSize() > this.maxSize)
      {
         return maxSize;  
      }
      
      return this.poolParams.getMinSize();
   }
   
   /**
    * Validate connecitons.
    * @throws Exception for exception
    */
   @SuppressWarnings("unchecked")
   public void validateConnections() throws Exception
   {
      if (this.trace)
      {
         log.trace("Attempting to  validate connections for pool " + this);  
      }

      if (this.permits.tryAcquire(this.poolParams.getBlockingTimeout(), TimeUnit.MILLISECONDS))
      {
         boolean destroyed = false;
         try
         {
            while (true)
            {
               ConnectionListener cl = null;
               if (cls.size() == 0)
               {
                  break;
               }

               cl = removeForFrequencyCheck();

               if (cl == null)
               {
                  break;
               }

               try
               {
                  Set<ManagedConnection> candidateSet = Collections.singleton(cl.getManagedConnection());
                  if (mcf instanceof ValidatingManagedConnectionFactory)
                  {
                     ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory) mcf;
                     candidateSet = vcf.getInvalidConnections(candidateSet);

                     if (candidateSet != null && candidateSet.size() > 0)
                     {
                        if (!cl.getState().equals(ConnectionState.DESTROY))
                        {
                           doDestroy(cl);
                           destroyed = true;
                        }
                     }
                  }
                  else
                  {
                     log.warn("warning: background validation was specified with a " +
                           "non compliant ManagedConnectionFactory interface.");
                  }
               }
               finally
               {
                  if (!destroyed)
                  {
                     returnForFrequencyCheck(cl);
                  }
               }
            }
         }
         finally
         {
            permits.release();
            
            //Check min size pool after validation
            if (destroyed && !shutdown.get() && poolParams.getMinSize() > 0)
            {
               PoolFiller.fillPool(this);
            }
         }
      }
   }
   
   /**
    * Remove for frequency check.
    * @return connection listener
    */
   private ConnectionListener removeForFrequencyCheck()
   {
      log.debug("Checking for connection within frequency");
      ConnectionListener cl = null;
      for (Iterator<ConnectionListener> iter = cls.iterator(); iter.hasNext();)
      {
         cl = (ConnectionListener) iter.next();
         long lastCheck = cl.getLastValidatedTime();

         if ((System.currentTimeMillis() - lastCheck) >= poolParams.getBackgroundValidationInterval())
         {
            cls.remove(cl);
            break;
         }
         else
         {
            cl = null;
         }
      }
      
      return cl;
   }
   
   /** 
    * Returns connection to pool again.
    * @param cl connection listener
    */
   private void returnForFrequencyCheck(ConnectionListener cl)
   {

      log.debug("Returning for connection within frequency");

      cl.setLastValidatedTime(System.currentTimeMillis());
      cls.add(cl);
   }

}
