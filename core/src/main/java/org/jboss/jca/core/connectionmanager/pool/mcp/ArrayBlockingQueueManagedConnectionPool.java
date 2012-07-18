/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PrefillPool;
import org.jboss.jca.core.connectionmanager.pool.idle.IdleRemover;
import org.jboss.jca.core.connectionmanager.pool.validator.ConnectionValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableUnavailableException;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Messages;

/**
 * A managed connection pool implementation using ArrayBlockingQueue
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ArrayBlockingQueueManagedConnectionPool implements ManagedConnectionPool
{
   /** The log */
   private CoreLogger log;

   /** Whether debug is enabled */
   private boolean debug;
   
   /** Whether trace is enabled */
   private boolean trace;
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** The managed connection factory */
   private ManagedConnectionFactory mcf;

   /** The connection listener factory */
   private ConnectionListenerFactory clf;

   /** The default subject */
   private Subject defaultSubject;

   /** The default connection request information */
   private ConnectionRequestInfo defaultCri;

   /** The pool configuration */
   private PoolConfiguration poolConfiguration;

   /** The pool */
   private Pool pool;

   /** The available connection event listeners */
   private ArrayBlockingQueue<ConnectionListener> cls;

   /** The checked out connections */
   private ConcurrentSkipListSet<ConnectionListener> checkedOut;

   /** Whether the pool has been shutdown */
   private AtomicBoolean shutdown = new AtomicBoolean(false);

   /** Statistics */
   private ManagedConnectionPoolStatisticsImpl statistics;

   /** Supports lazy association */
   private Boolean supportsLazyAssociation;

   /**
    * Constructor
    */
   public ArrayBlockingQueueManagedConnectionPool()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void initialize(ManagedConnectionFactory mcf, ConnectionListenerFactory clf, Subject subject,
                          ConnectionRequestInfo cri, PoolConfiguration pc, Pool p)
   {
      if (mcf == null)
         throw new IllegalArgumentException("ManagedConnectionFactory is null");

      if (clf == null)
         throw new IllegalArgumentException("ConnectionListenerFactory is null");

      if (pc == null)
         throw new IllegalArgumentException("PoolConfiguration is null");

      if (p == null)
         throw new IllegalArgumentException("Pool is null");

      this.mcf = mcf;
      this.clf = clf;
      this.defaultSubject = subject;
      this.defaultCri = cri;
      this.poolConfiguration = pc;
      this.pool = p;
      this.log = pool.getLogger();
      this.debug = log.isDebugEnabled();
      this.trace = log.isTraceEnabled();
      this.cls = new ArrayBlockingQueue<ConnectionListener>(pc.getMaxSize(), true);
      this.checkedOut = new ConcurrentSkipListSet<ConnectionListener>();
      this.statistics = new ManagedConnectionPoolStatisticsImpl(pc.getMaxSize());
      this.supportsLazyAssociation = null;
  
      // Check if connection manager supports lazy association
      if (!(clf instanceof LazyAssociatableConnectionManager))
         supportsLazyAssociation = Boolean.FALSE;

      // Schedule managed connection pool for prefill
      if (pc.isPrefill() && p instanceof PrefillPool && pc.getMinSize() > 0)
      {
         PoolFiller.fillPool(this);
      }

      reenable();

      statistics.setMaxWaitCount(-1);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRunning()
   {
      return !shutdown.get();
   }

   /**
    * {@inheritDoc}
    */
   public synchronized boolean isEmpty()
   {
      return cls.size() == 0 && checkedOut.size() == 0;
   }

   /**
    * Is the pool full ?
    * @return True if full, otherwise false
    */
   public synchronized boolean isFull()
   {
      return checkedOut.size() == poolConfiguration.getMaxSize();
   }

   /**
    * {@inheritDoc}
    */
   public void reenable()
   {
      if (poolConfiguration.getIdleTimeoutMinutes() > 0)
      {
         //Register removal support
         IdleRemover.getInstance().registerPool(this, poolConfiguration.getIdleTimeoutMinutes() * 1000L * 60);
      }
      
      if (poolConfiguration.isBackgroundValidation() && poolConfiguration.getBackgroundValidationMillis() > 0)
      {
         log.debug("Registering for background validation at interval " + 
                   poolConfiguration.getBackgroundValidationMillis());
         
         //Register validation
         ConnectionValidator.getInstance().registerPool(this, poolConfiguration.getBackgroundValidationMillis());
      }

      shutdown.set(false);
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      if (trace)
      {
         String method = "getConnection(" + subject + ", " + cri + ")";
         log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                            mcf, clf, pool, poolConfiguration,
                                                            cls, checkedOut, statistics));
      }
      else if (debug)
      {
         String method = "getConnection(" + subject + ", " + cri + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics));
      }

      subject = (subject == null) ? defaultSubject : subject;
      cri = (cri == null) ? defaultCri : cri;

      ConnectionListener cl = null;
      boolean verifyConnectionListener = true;

      long startWait = System.currentTimeMillis();
      if (cls.size() > 0)
      {
         if (shutdown.get())
            throw new RetryableUnavailableException(
               bundle.thePoolHasBeenShutdown(pool.getName(),
                                             Integer.toHexString(System.identityHashCode(this))));
         
         cl = cls.peek();
         if (cl != null)
         {
            try
            {
               cl = cls.poll(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS);
               statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
            }
            catch (InterruptedException ie)
            {
               Thread.interrupted();

               long end = System.currentTimeMillis() - startWait;
               throw new ResourceException(bundle.interruptedWhileRequestingConnection(end));
            }
         }
         else
         {
            try
            {
               // No, the pool was empty, so we have to make a new one.
               cl = createConnectionEventListener(subject, cri);

               if (trace)
                  log.trace("supplying new ManagedConnection: " + cl);
               
               verifyConnectionListener = false;
            }
            catch (Throwable t)
            {
               log.throwableWhileAttemptingGetNewGonnection(cl, t);

               throw new ResourceException(bundle.unexpectedThrowableWhileTryingCreateConnection(cl), t);
            }
         }
      }
      else
      {
         if (pool.isSharable() && (supportsLazyAssociation == null || supportsLazyAssociation.booleanValue()) &&
             isFull())
         {
            if (supportsLazyAssociation == null)
               checkLazyAssociation();

            if (supportsLazyAssociation != null && supportsLazyAssociation.booleanValue())
            {
               detachConnectionListener();
            }
         }

         try
         {
            cl = cls.poll(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS);
            statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);

            if (shutdown.get())
               throw new RetryableUnavailableException(
                  bundle.thePoolHasBeenShutdown(pool.getName(),
                                                Integer.toHexString(System.identityHashCode(this))));
         }
         catch (InterruptedException ie)
         {
            Thread.interrupted();

            if (!poolConfiguration.isUseFastFail())
            {
               throw new ResourceException(bundle.noMManagedConnectionsAvailableWithinConfiguredBlockingTimeout(
                     poolConfiguration.getBlockingTimeout()));
            }
            else
            {
               if (trace)
                  log.trace("Fast failing for connection attempt. No more attempts will be made to " +
                            "acquire connection from pool and a new connection will be created immeadiately");

               try
               {
                  cl = createConnectionEventListener(subject, cri);
               
                  if (poolConfiguration.isPrefill() &&
                      pool instanceof PrefillPool &&
                      poolConfiguration.getMinSize() > 0)
                     PoolFiller.fillPool(this);
               
                  if (trace)
                     log.trace("supplying new ManagedConnection: " + cl);
               
                  verifyConnectionListener = false;
               }
               catch (Throwable t)
               {
                  log.throwableWhileAttemptingGetNewGonnection(cl, t);

                  throw new ResourceException(bundle.unexpectedThrowableWhileTryingCreateConnection(cl), t);
               }
            }
         }
      }

      // Register the connection listener
      checkedOut.add(cl);

      // Update in used statistics
      statistics.setInUsedCount(checkedOut.size());
      
      if (!verifyConnectionListener)
      {
         // Return connection listener
         return cl;
      }
      else
      {
         try
         {
            Object matchedMC =
               mcf.matchManagedConnections(Collections.singleton(cl.getManagedConnection()), subject, cri);

            if (matchedMC != null)
            {
               if (trace)
                  log.trace("supplying ManagedConnection from pool: " + cl);

               // Return connection listener
               return cl;
            }

            // Match did not succeed but no exception was thrown.
            // Either we have the matching strategy wrong or the
            // connection died while being checked.  We need to
            // distinguish these cases, but for now we always
            // destroy the connection.
            log.destroyingConnectionNotSuccessfullyMatched(cl, mcf);
            
            checkedOut.remove(cl);
            statistics.setInUsedCount(checkedOut.size());
            
            doDestroy(cl);
            cl = null;
         }
         catch (Throwable t)
         {
            log.throwableWhileTryingMatchManagedConnection(cl, t);
         
            checkedOut.remove(cl);
            statistics.setInUsedCount(checkedOut.size());

            doDestroy(cl);
            cl = null;

            throw new ResourceException(bundle.unexpectedThrowableWhileTryingCreateConnection(cl), t);
         }
      }

      throw new ResourceException(bundle.shouldNeverHappen(), new Throwable("STACKTRACE"));
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc)
   {
      return findConnectionListener(mc, null);
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc, Object connection)
   {
      for (ConnectionListener cl : checkedOut)
      {
         if (cl.controls(mc, connection))
            return cl;
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnection(ConnectionListener cl, boolean kill)
   {
      returnConnection(cl, kill, true);
   }

   /**
    * Return a connection to the pool
    * @param cl The connection listener
    * @param kill Should the connection be killed
    * @param cleanup Should cleanup be performed
    */
   public void returnConnection(ConnectionListener cl, boolean kill, boolean cleanup)
   {
      if (trace)
      {
         String method = "returnConnection(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")";
         log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                            mcf, clf, pool, poolConfiguration,
                                                            cls, checkedOut, statistics));
      }
      else if (debug)
      {
         String method = "returnConnection(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics));
      }

      if (cl.getState() == ConnectionState.DESTROYED)
      {
         if (trace)
            log.trace("ManagedConnection is being returned after it was destroyed" + cl);

         return;
      }

      if (cleanup)
      {
         try
         {
            cl.getManagedConnection().cleanup();
         }
         catch (ResourceException re)
         {
            log.resourceExceptionCleaningUpManagedConnection(cl, re);
            kill = true;
         }
      }

      // We need to destroy this one
      if (cl.getState() == ConnectionState.DESTROY || cl.getState() == ConnectionState.DESTROYED)
         kill = true;

      checkedOut.remove(cl);
      statistics.setInUsedCount(checkedOut.size());

      // This is really an error
      if (!kill && cls.size() >= poolConfiguration.getMaxSize())
      {
         log.destroyingReturnedConnectionMaximumPoolSizeExceeded(cl);
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
            try
            {
               cls.put(cl);
            }
            catch (InterruptedException ie)
            {
               Thread.interrupted();

               cl.setState(ConnectionState.DESTROY);
               kill = true;
            }
         }
         else
         {
            log.attemptReturnConnectionTwice(cl, new Throwable("STACKTRACE"));
         }
      }

      if (kill)
      {
         if (trace)
            log.trace("Destroying returned connection " + cl);

         doDestroy(cl);
         cl = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush()
   {
      flush(false);
   }

   /**
    * {@inheritDoc}
    */
   public void flush(boolean kill)
   {
      ArrayList<ConnectionListener> destroy = null;

      if (trace)
         log.trace("Flushing pool checkedOut=" + checkedOut + " inPool=" + cls);

      if (kill)
      {
         // Mark checked out connections as requiring destruction
         while (checkedOut.size() > 0)
         {
            ConnectionListener cl = checkedOut.pollFirst();

            if (trace)
               log.trace("Flush marking checked out connection for destruction " + cl);

            cl.setState(ConnectionState.DESTROY);

            if (destroy == null)
               destroy = new ArrayList<ConnectionListener>(1);

            destroy.add(cl);
         }
      }

      // Destroy connections in the pool
      ConnectionListener cl = cls.poll();
      while (cl != null)
      {
         if (destroy == null)
            destroy = new ArrayList<ConnectionListener>();

         destroy.add(cl);
         cl = cls.poll();
      }

      // We need to destroy some connections
      if (destroy != null)
      {
         for (ConnectionListener destroyCl : destroy)
         {
            if (trace)
               log.trace("Destroying flushed connection " + destroyCl);

            doDestroy(destroyCl);
            destroyCl = null;
         }

         // We destroyed something, check the minimum.
         if (!shutdown.get() && 
             poolConfiguration.getMinSize() > 0 &&
             poolConfiguration.isPrefill() &&
             pool instanceof PrefillPool)
         {
            PoolFiller.fillPool(this);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void removeIdleConnections()
   {
      ArrayList<ConnectionListener> destroy = null;
      long timeout = System.currentTimeMillis() - (poolConfiguration.getIdleTimeoutMinutes() * 1000L * 60);
      
      boolean cont = true;
      while (cont)
      {
         // Check the first in the list
         ConnectionListener cl = cls.peek();
         if (cl != null && cl.isTimedOut(timeout) && shouldRemove())
         {
            statistics.deltaTimedOut();

            // We need to destroy this one
            if (destroy == null)
               destroy = new ArrayList<ConnectionListener>(1);

            cl = cls.poll();

            if (cl != null)
            {
               destroy.add(cl);
            }
            else
            {
               // The connection list were empty
               cont = false;
            }
         }
         else
         {
            // They were inserted chronologically, so if this one 
            // isn't timed out, following ones won't be either.
            cont = false;
         }
      }

      // We found some connections to destroy
      if (destroy != null)
      {
         for (ConnectionListener cl : destroy)
         {
            if (trace)
               log.trace("Destroying timedout connection " + cl);

            doDestroy(cl);
            cl = null;
         }

         if (!shutdown.get())
         {
            if (!poolConfiguration.isStrictMin())
            {
               boolean emptyManagedConnectionPool = false;

               if (poolConfiguration.isPrefill() && pool instanceof PrefillPool)
               {
                  if (poolConfiguration.getMinSize() > 0)
                  {
                     PoolFiller.fillPool(this);
                  }
                  else
                  {
                     emptyManagedConnectionPool = true;
                  }
               }
               else
               {
                  emptyManagedConnectionPool = true;
               }

               // Empty pool
               if (emptyManagedConnectionPool)
                  pool.emptyManagedConnectionPool(this);
            }
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public void shutdown()
   {
      if (trace)
         log.tracef("Shutdown - Pool: %s MCP: %s", pool.getName(), Integer.toHexString(System.identityHashCode(this)));

      shutdown.set(true);
      IdleRemover.getInstance().unregisterPool(this);
      ConnectionValidator.getInstance().unregisterPool(this);

      if (checkedOut.size() > 0)
      {
         for (ConnectionListener cl : checkedOut)
         {
            log.destroyingActiveConnection(pool.getName(), cl.getManagedConnection());
         }
      }

      flush(true);
   }

   /**
    * {@inheritDoc}
    */
   public void fillToMin()
   {
      if (poolConfiguration.getMinSize() <= 0)
         return;

      if (!poolConfiguration.isPrefill())
         return;

      if (!(pool instanceof PrefillPool))
         return;

      while (poolConfiguration.getMinSize() - (cls.size() + checkedOut.size()) > 0)
      {
         if (shutdown.get())
            return;

         // Create a connection to fill the pool
         ConnectionListener cl = null;
         boolean destroy = false;
         try
         {
            cl = createConnectionEventListener(defaultSubject, defaultCri);
            statistics.setInUsedCount(checkedOut.size() + 1);
               
            if ((checkedOut.size() + cls.size()) < poolConfiguration.getMinSize())
            {
               if (trace)
                  log.trace("Filling pool cl=" + cl);

               if (!cls.offer(cl))
               {
                  log.debug("Connection couldn't be inserted during fillToMin");
                  destroy = true;
               }
            }
            else
            {
               log.debug("MinSize reached during fillToMin");
               destroy = true;
            }
         }
         catch (ResourceException re)
         {
            log.unableFillPool(re);
            destroy = true;
         }
         finally
         {
            if (destroy)
            {
               if (cl != null)
               {
                  doDestroy(cl);
                  cl = null;
               }

               break;
            }
         }
      }
   }

   /**
    * Get statistics
    * @return The module
    */
   public ManagedConnectionPoolStatistics getStatistics()
   {
      return statistics;
   }

   /**
    * Create a connection event listener
    *
    * @param subject the subject
    * @param cri the connection request information
    * @return the new listener
    * @throws ResourceException for any error
    */
   private ConnectionListener createConnectionEventListener(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      long start = System.currentTimeMillis();

      ManagedConnection mc = mcf.createManagedConnection(subject, cri);

      statistics.deltaTotalCreationTime(System.currentTimeMillis() - start);
      statistics.deltaCreatedCount();
      try
      {
         return clf.createConnectionListener(mc, this);
      }
      catch (ResourceException re)
      {
         statistics.deltaDestroyedCount();
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

      statistics.deltaDestroyedCount();
      cl.setState(ConnectionState.DESTROYED);

      ManagedConnection mc = cl.getManagedConnection();
      try
      {
         mc.destroy();
      }
      catch (Throwable t)
      {
         log.debug("Exception destroying ManagedConnection " + cl, t);
      }

      mc.removeConnectionEventListener(cl);
   }
   
   private boolean shouldRemove()
   {      
      boolean remove = true;
      
      if (poolConfiguration.isStrictMin())
      {
         remove = cls.size() > poolConfiguration.getMinSize();
         
         if (trace)
            log.trace("StrictMin is active. Current connection will be removed is " + remove);
      }
      
      return remove;
   }
   
   /**
    * {@inheritDoc}
    */
   public void validateConnections() throws Exception
   {
      if (trace)
         log.trace("Attempting to  validate connections for pool " + this);

      boolean anyDestroyed = false;

      try
      {
         while (true)
         {
            ConnectionListener cl = null;
            boolean destroyed = false;

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
               Set candidateSet = Collections.singleton(cl.getManagedConnection());

               if (mcf instanceof ValidatingManagedConnectionFactory)
               {
                  ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory) mcf;
                  candidateSet = vcf.getInvalidConnections(candidateSet);

                  if (candidateSet != null && candidateSet.size() > 0)
                  {
                     if (cl.getState() != ConnectionState.DESTROY)
                     {
                        doDestroy(cl);
                        cl = null;
                        destroyed = true;
                        anyDestroyed = true;
                     }
                  }
               }
               else
               {
                  log.backgroundValidationNonCompliantManagedConnectionFactory();
               }
            }
            finally
            {
               if (!destroyed)
               {
                  if (!returnForFrequencyCheck(cl))
                     anyDestroyed = true;
               }
            }
         }
      }
      finally
      {
         if (anyDestroyed &&
             !shutdown.get() &&
             poolConfiguration.getMinSize() > 0 &&
             poolConfiguration.isPrefill() &&
             pool instanceof PrefillPool)
         {
            PoolFiller.fillPool(this);
         }
      }
   }

   /**
    * Remove a connection to the pool for a frequency check
    * @return A connection; <code>null</code> if no connections needs to be checked
    */
   private ConnectionListener removeForFrequencyCheck()
   {
      log.debug("Checking for connection within frequency");

      ConnectionListener result = null;
      Iterator<ConnectionListener> iter = cls.iterator();

      while (result == null && iter.hasNext())
      {
         ConnectionListener cl = iter.next();
         long lastCheck = cl.getLastValidatedTime();

         if ((System.currentTimeMillis() - lastCheck) >= poolConfiguration.getBackgroundValidationMillis())
         {
            result = cl;
            cls.remove(cl);
         }
      }

      return result;
   }

   /**
    * Return a connection to the pool
    * @param cl The connection
    * @return <code>True</code> if the connection was returned; otherwise <code>false</code>
    */
   private boolean returnForFrequencyCheck(ConnectionListener cl)
   {
      log.debug("Returning for connection within frequency: " + cl);

      if (cl == null)
         return true;

      cl.setLastValidatedTime(System.currentTimeMillis());

      if (!cls.offer(cl))
      {
         log.debug("Connection couldn't be returned");
         doDestroy(cl);
         cl = null;
         return false;
      }

      return true;
   }

   /**
    * Check if the resource adapter supports lazy association
    */
   private void checkLazyAssociation()
   {
      ConnectionListener cl = null;

      if (checkedOut.size() > 0)
         cl = checkedOut.first();

      if (cl == null && cls.size() > 0)
         cl = cls.peek();

      if (cl != null)
      {
         ManagedConnection mc = cl.getManagedConnection();

         if (mc instanceof DissociatableManagedConnection)
         {
            if (debug)
               log.debug("Enable lazy association support for: " + pool.getName());

            supportsLazyAssociation = Boolean.TRUE;
         }
         else
         {
            if (debug)
               log.debug("Disable lazy association support for: " + pool.getName());
            
            supportsLazyAssociation = Boolean.FALSE;
         }
      }
   }

   /**
    * Detach connection listener
    */
   private void detachConnectionListener()
   {
      ConnectionListener cl = null;

      if (checkedOut.size() > 0)
         cl = checkedOut.pollFirst();

      if (cl != null)
      {
         try
         {
            if (trace)
               log.tracef("Detach: %s", cl); 

            DissociatableManagedConnection dmc = (DissociatableManagedConnection)cl.getManagedConnection();
            dmc.dissociateConnections();
               
            cl.unregisterConnections();
               
            returnConnection(cl, false, false);
         }
         catch (Throwable t)
         {
            // Ok - didn't work; nuke it and disable
            if (debug)
               log.debug("Exception during detach for: " + pool.getName(), t); 

            supportsLazyAssociation = Boolean.FALSE;
            returnConnection(cl, true, true);
         }
      }
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ArrayBlockingQueueManagedConnectionPool@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[pool=").append(pool.getName());
      sb.append("]");

      return sb.toString();
   }
}
