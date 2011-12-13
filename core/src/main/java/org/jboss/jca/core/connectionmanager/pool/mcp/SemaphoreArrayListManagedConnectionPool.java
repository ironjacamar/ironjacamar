/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableUnavailableException;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Messages;

/**
 * The internal pool implementation
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: 107890 $
 */
public class SemaphoreArrayListManagedConnectionPool implements ManagedConnectionPool
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

   /** 
    * Copy of the maximum size from the pooling parameters.
    * Dynamic changes to this value are not compatible with
    * the semaphore which cannot change be dynamically changed.
    */
   private int maxSize;

   /** The available connection event listeners */
   private ArrayList<ConnectionListener> cls;

   /** The permits used to control who can checkout a connection */
   private Semaphore permits;

   /** The map of connection listeners which has a permit */
   private ConcurrentMap<ConnectionListener, ConnectionListener> clPermits =
      new ConcurrentHashMap<ConnectionListener, ConnectionListener>();

   /** The checked out connections */
   private ArrayList<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();

   /** Whether the pool has been shutdown */
   private AtomicBoolean shutdown = new AtomicBoolean(false);

   /** Statistics */
   private ManagedConnectionPoolStatisticsImpl statistics;

   /**
    * Constructor
    */
   public SemaphoreArrayListManagedConnectionPool()
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
      this.maxSize = pc.getMaxSize();
      this.pool = p;
      this.log = pool.getLogger();
      this.debug = log.isDebugEnabled();
      this.trace = log.isTraceEnabled();
      this.cls = new ArrayList<ConnectionListener>(this.maxSize);
      this.statistics = new ManagedConnectionPoolStatisticsImpl(maxSize);
      this.permits = new Semaphore(maxSize, true, statistics);

      // Schedule managed connection pool for prefill
      if (pc.isPrefill() && p instanceof PrefillPool)
      {
         PoolFiller.fillPool(this);
      }

      reenable();
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
   public boolean isEmpty()
   {
      synchronized (cls)
      {
         return cls.size() == 0 && checkedOut.size() == 0;
      }
   }

   /**
    * Check if the pool has reached a certain size
    * @param size The size
    * @return True if reached; otherwise false
    */
   private boolean isSize(int size)
   {
      synchronized (cls)
      {
         return (cls.size() + checkedOut.size()) >= size;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void reenable()
   {
      if (poolConfiguration.getIdleTimeoutMinutes() > 0)
      {
         //Register removal support
         IdleRemover.getInstance().registerPool(this, poolConfiguration.getIdleTimeoutMinutes() * 1000 * 60);
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
         synchronized (cls)
         {
            String method = "getConnection(" + subject + ", " + cri + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                               mcf, clf, pool, poolConfiguration,
                                                               cls, checkedOut, statistics));
         }
      }
      else if (debug)
      {
         String method = "getConnection(" + subject + ", " + cri + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics));
      }

      subject = (subject == null) ? defaultSubject : subject;
      cri = (cri == null) ? defaultCri : cri;
      long startWait = System.currentTimeMillis();
      try
      {
         if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
         {
            statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);

            //We have a permit to get a connection. Is there one in the pool already?
            ConnectionListener cl = null;
            do
            {
               synchronized (cls)
               {
                  if (shutdown.get())
                  {
                     permits.release();
                     throw new RetryableUnavailableException(bundle.thePoolHasBeenShutdown());
                  }

                  int clsSize = cls.size();
                  if (clsSize > 0)
                  {
                     cl = cls.remove(clsSize - 1);
                     checkedOut.add(cl);
                     statistics.setInUsedCount(checkedOut.size());
                  }
               }
               if (cl != null)
               {
                  //Yes, we retrieved a ManagedConnection from the pool. Does it match?
                  try
                  {
                     Object matchedMC = mcf.matchManagedConnections(Collections.singleton(cl.getManagedConnection()),
                                                                    subject, cri);

                     if (matchedMC != null)
                     {
                        if (trace)
                           log.trace("supplying ManagedConnection from pool: " + cl);

                        clPermits.put(cl, cl);

                        return cl;
                     }

                     // Match did not succeed but no exception was thrown.
                     // Either we have the matching strategy wrong or the
                     // connection died while being checked.  We need to
                     // distinguish these cases, but for now we always
                     // destroy the connection.
                     log.destroyingConnectionNotSuccessfullyMatched(cl);

                     synchronized (cls)
                     {
                        checkedOut.remove(cl);
                        statistics.setInUsedCount(checkedOut.size());
                     }

                     doDestroy(cl);
                     cl = null;
                  }
                  catch (Throwable t)
                  {
                     log.throwableWhileTryingMatchManagedConnectionThenDestroyingConnection(cl, t);

                     synchronized (cls)
                     {
                        checkedOut.remove(cl);
                        statistics.setInUsedCount(checkedOut.size());
                     }

                     doDestroy(cl);
                     cl = null;
                  }

                  // We made it here, something went wrong and we should validate 
                  // if we should continue attempting to acquire a connection
                  if (poolConfiguration.isUseFastFail())
                  {
                     if (trace)
                        log.trace("Fast failing for connection attempt. No more attempts will be made to " +
                               "acquire connection from pool and a new connection will be created immeadiately");
                     break;
                  }
               
               }
            }
            while (cls.size() > 0);

            // OK, we couldnt find a working connection from the pool.  Make a new one.
            try
            {
               // No, the pool was empty, so we have to make a new one.
               cl = createConnectionEventListener(subject, cri);

               synchronized (cls)
               {
                  checkedOut.add(cl);
                  statistics.setInUsedCount(checkedOut.size());
               }

               if (trace)
                  log.trace("supplying new ManagedConnection: " + cl);

               clPermits.put(cl, cl);

               return cl;
            }
            catch (Throwable t)
            {
               log.throwableWhileAttemptingGetNewGonnection(cl, t);

               // Return permit and rethrow
               synchronized (cls)
               {
                  checkedOut.remove(cl);
                  statistics.setInUsedCount(checkedOut.size());
               }

               permits.release();

               throw new ResourceException(bundle.unexpectedThrowableWhileTryingCreateConnection(cl), t);
            }
         }
         else
         {
            // We timed out
            throw new ResourceException(bundle.noMManagedConnectionsAvailableWithinConfiguredBlockingTimeout(
                  poolConfiguration.getBlockingTimeout()));
         }

      }
      catch (InterruptedException ie)
      {
         long end = System.currentTimeMillis() - startWait;
         statistics.deltaTotalBlockingTime(end);
         throw new ResourceException(bundle.interruptedWhileRequestingPermit(end));
      }
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnection(ConnectionListener cl, boolean kill)
   {
      if (trace)
      {
         synchronized (cls)
         {
            String method = "returnConnection(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                               mcf, clf, pool, poolConfiguration,
                                                               cls, checkedOut, statistics));
         }
      }
      else if (debug)
      {
         String method = "returnConnection(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics));
      }

      if (cl.getState() == ConnectionState.DESTROYED)
      {
         if (trace)
            log.trace("ManagedConnection is being returned after it was destroyed: " + cl);

         if (clPermits.containsKey(cl))
         {
            clPermits.remove(cl);
            permits.release();
         }

         return;
      }

      try
      {
         cl.getManagedConnection().cleanup();
      }
      catch (ResourceException re)
      {
         log.resourceExceptionCleaningUpManagedConnection(cl, re);
         kill = true;
      }

      synchronized (cls)
      {
         // We need to destroy this one
         if (cl.getState() == ConnectionState.DESTROY || cl.getState() == ConnectionState.DESTROYED)
            kill = true;

         checkedOut.remove(cl);
         statistics.setInUsedCount(checkedOut.size());

         // This is really an error
         if (!kill && isSize(poolConfiguration.getMaxSize() + 1))
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
               cls.add(cl);
            }
            else
            {
               log.attemptReturnConnectionTwice(cl, new Throwable("STACKTRACE"));
            }
         }

         if (clPermits.containsKey(cl))
         {
            clPermits.remove(cl);
            permits.release();
         }
      }

      if (kill)
      {
         if (trace)
            log.trace("Destroying returned connection " + cl);

         doDestroy(cl);
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

      synchronized (cls)
      {
         if (kill)
         {
            if (trace)
               log.trace("Flushing pool checkedOut=" + checkedOut + " inPool=" + cls);

            // Mark checked out connections as requiring destruction
            while (checkedOut.size() > 0)
            {
               ConnectionListener cl = checkedOut.remove(0);

               if (trace)
                  log.trace("Flush marking checked out connection for destruction " + cl);

               cl.setState(ConnectionState.DESTROY);

               if (destroy == null)
                  destroy = new ArrayList<ConnectionListener>(1);

               destroy.add(cl);

               if (clPermits.containsKey(cl))
               {
                  clPermits.remove(cl);
                  permits.release();
               }
            }

            statistics.setInUsedCount(checkedOut.size());
         }

         // Destroy connections in the pool
         while (cls.size() > 0)
         {
            ConnectionListener cl = cls.remove(0);

            if (destroy == null)
               destroy = new ArrayList<ConnectionListener>(1);

            destroy.add(cl);
         }
      }

      // We need to destroy some connections
      if (destroy != null)
      {
         for (ConnectionListener cl : destroy)
         {
            if (trace)
               log.trace("Destroying flushed connection " + cl);

            doDestroy(cl);
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
      long timeout = System.currentTimeMillis() - (poolConfiguration.getIdleTimeoutMinutes() * 1000 * 60);

      while (true)
      {
         synchronized (cls)
         {
            // Nothing left to destroy
            if (cls.size() == 0)
               break;

            // Check the first in the list
            ConnectionListener cl = cls.get(0);
            if (cl.isTimedOut(timeout) && shouldRemove())
            {
               statistics.deltaTimedOut();

               // We need to destroy this one
               cls.remove(0);

               if (destroy == null)
                  destroy = new ArrayList<ConnectionListener>(1);

               destroy.add(cl);
            }
            else
            {
               // They were inserted chronologically, so if this one isn't timed out, following ones won't be either.
               break;
            }
         }
      }

      // We found some connections to destroy
      if (destroy != null)
      {
         for (int i = 0; i < destroy.size(); ++i)
         {
            ConnectionListener cl = destroy.get(i);

            if (trace)
               log.trace("Destroying timedout connection " + cl);

            doDestroy(cl);
         }

         // We destroyed something, check the minimum.
         if (!shutdown.get() &&
             poolConfiguration.getMinSize() > 0 &&
             poolConfiguration.isPrefill() &&
             pool instanceof PrefillPool)
         {
            PoolFiller.fillPool(this);
         }

         // Empty pool
         if (pool != null)
            pool.emptyManagedConnectionPool(this);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void shutdown()
   {
      shutdown.set(true);
      IdleRemover.getInstance().unregisterPool(this);
      ConnectionValidator.getInstance().unregisterPool(this);
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

      while (true)
      {
         // Get a permit - avoids a race when the pool is nearly full
         // Also avoids unnessary fill checking when all connections are checked out
         try
         {
            long startWait = System.currentTimeMillis();
            if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
            {
               statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
               try
               {
                  if (shutdown.get())
                     return;

                  // We already have enough connections
                  if (isSize(poolConfiguration.getMinSize()))
                     return;

                  // Create a connection to fill the pool
                  try
                  {
                     ConnectionListener cl = createConnectionEventListener(defaultSubject, defaultCri);

                     synchronized (cls)
                     {
                        if (trace)
                           log.trace("Filling pool cl=" + cl);

                        cls.add(cl);
                        statistics.setInUsedCount(checkedOut.size() + 1);
                     }
                  }
                  catch (ResourceException re)
                  {
                     log.unableFillPool(re);
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
            if (trace)
               log.trace("Interrupted while requesting permit in fillToMin");
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
         if (trace)
            log.trace("ManagedConnection is already destroyed " + cl);

         return;
      }

      statistics.deltaDestroyedCount();
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
    * Should any connections be removed from the pool
    * @return True if connections should be removed; otherwise false
    */
   private boolean shouldRemove()
   {      
      boolean remove = true;
      
      if (poolConfiguration.isStrictMin())
      {
         // Add 1 to min-pool-size since it is strict
         remove = isSize(poolConfiguration.getMinSize() + 1);

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

      if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
      {
         boolean anyDestroyed = false;

         try
         {
            while (true)
            {
               ConnectionListener cl = null;
               boolean destroyed = false;

               synchronized (cls)
               {
                  if (cls.size() == 0)
                  {
                     break;
                  }

                  cl = removeForFrequencyCheck();
               }

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
                     synchronized (cls)
                     {
                        returnForFrequencyCheck(cl);
                     }
                  }
               }
            }
         }
         finally
         {
            permits.release();

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
   }

   /**
    * Returns the connection listener that should be removed due to background validation
    * @return The listener; otherwise null if none should be removed
    */
   private ConnectionListener removeForFrequencyCheck()
   {
      log.debug("Checking for connection within frequency");

      ConnectionListener cl = null;

      for (Iterator<ConnectionListener> iter = cls.iterator(); iter.hasNext();)
      {
         cl = iter.next();
         long lastCheck = cl.getLastValidatedTime();

         if ((System.currentTimeMillis() - lastCheck) >= poolConfiguration.getBackgroundValidationMillis())
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
    * Return a connection listener to the pool and update its validation timestamp
    * @param cl The listener
    */
   private void returnForFrequencyCheck(ConnectionListener cl)
   {
      log.debug("Returning for connection within frequency");

      cl.setLastValidatedTime(System.currentTimeMillis());
      cls.add(cl);
   }
}
