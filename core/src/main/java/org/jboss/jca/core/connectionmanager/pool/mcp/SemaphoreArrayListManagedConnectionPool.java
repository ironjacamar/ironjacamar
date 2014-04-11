/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.api.connectionmanager.pool.FlushMode;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityDecrementer;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PrefillPool;
import org.jboss.jca.core.connectionmanager.pool.capacity.DefaultCapacity;
import org.jboss.jca.core.connectionmanager.pool.idle.IdleRemover;
import org.jboss.jca.core.connectionmanager.pool.validator.ConnectionValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableException;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Messages;

/**
 * The internal pool implementation
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:wprice@redhat.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: 107890 $
 */
public class SemaphoreArrayListManagedConnectionPool implements ManagedConnectionPool
{
   /** New line */
   private static String newLine = SecurityActions.getSystemProperty("line.separator");

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
   private final ConcurrentMap<ConnectionListener, ConnectionListener> clPermits =
      new ConcurrentHashMap<ConnectionListener, ConnectionListener>();

   /** The checked out connections */
   private final ArrayList<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();

   /** Whether the pool has been shutdown */
   private final AtomicBoolean shutdown = new AtomicBoolean(false);

   /** Statistics */
   private ManagedConnectionPoolStatisticsImpl statistics;

   /** Supports lazy association */
   private Boolean supportsLazyAssociation;

   /** Last idle check */
   private long lastIdleCheck;

   /** Last used */
   private long lastUsed;

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
      this.statistics.setEnabled(p.getStatistics().isEnabled());
      this.permits = new Semaphore(maxSize, true, statistics);
      this.supportsLazyAssociation = null;
      this.lastIdleCheck = Long.MIN_VALUE;
      this.lastUsed = Long.MAX_VALUE;

      // Check if connection manager supports lazy association
      if (!(clf instanceof LazyAssociatableConnectionManager))
         supportsLazyAssociation = Boolean.FALSE;

      // Schedule managed connection pool for prefill
      if ((pc.isPrefill() || pc.isStrictMin()) && p instanceof PrefillPool && pc.getInitialSize() > 0)
      {
         PoolFiller.fillPool(new FillRequest(this, pc.getInitialSize()));
      }

      if (poolConfiguration.getIdleTimeoutMinutes() > 0)
      {
         //Register removal support
         IdleRemover.getInstance().registerPool(this, poolConfiguration.getIdleTimeoutMinutes() * 1000L * 60);
      }

      if (poolConfiguration.isBackgroundValidation() && poolConfiguration.getBackgroundValidationMillis() > 0)
      {
         if (debug)
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
   public long getLastUsed()
   {
      return lastUsed;
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
    * {@inheritDoc}
    */
   public boolean isFull()
   {
      synchronized (cls)
      {
         return checkedOut.size() == maxSize;
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isIdle()
   {
      synchronized (cls)
      {
         return checkedOut.size() == 0;
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getActive()
   {
      synchronized (cls)
      {
         return cls.size() + checkedOut.size();
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
   public void prefill()
   {
      if (!shutdown.get() &&
          (poolConfiguration.isPrefill() || poolConfiguration.isStrictMin()) &&
          pool instanceof PrefillPool &&
          poolConfiguration.getMinSize() > 0)
         PoolFiller.fillPool(new FillRequest(this, poolConfiguration.getMinSize()));
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
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics.getInUseCount(), maxSize));
      }

      subject = (subject == null) ? defaultSubject : subject;
      cri = (cri == null) ? defaultCri : cri;

      if (isFull())
         statistics.deltaWaitCount();

      if (pool.isSharable() && (supportsLazyAssociation == null || supportsLazyAssociation.booleanValue()) && isFull())
      {
         if (supportsLazyAssociation == null)
            checkLazyAssociation();

         if (supportsLazyAssociation != null && supportsLazyAssociation.booleanValue())
         {
            detachConnectionListener();
         }
      }

      long startWait = statistics.isEnabled() ? System.currentTimeMillis() : 0L;
      try
      {
         if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
         {
            if (statistics.isEnabled())
               statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);

            //We have a permit to get a connection. Is there one in the pool already?
            ConnectionListener cl = null;
            do
            {
               if (shutdown.get())
               {
                  permits.release();
                  throw new ResourceException(
                     bundle.thePoolHasBeenShutdown(pool.getName(),
                                                   Integer.toHexString(System.identityHashCode(this))));
               }

               synchronized (cls)
               {
                  if (cls.size() > 0)
                  {
                     cl = cls.remove(0);
                     checkedOut.add(cl);
                  }
               }

               if (statistics.isEnabled())
                  statistics.setInUsedCount(checkedOut.size());

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

                        lastUsed = System.currentTimeMillis();

                        if (statistics.isEnabled())
                        {
                           statistics.deltaTotalGetTime(lastUsed - startWait);
                           statistics.deltaTotalPoolTime(lastUsed - cl.getLastUsedTime());
                        }

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
                     }

                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOut.size());

                     doDestroy(cl);
                     cl = null;
                  }
                  catch (Throwable t)
                  {
                     log.throwableWhileTryingMatchManagedConnectionThenDestroyingConnection(cl, t);

                     synchronized (cls)
                     {
                        checkedOut.remove(cl);
                     }

                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOut.size());

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
               }

               if (statistics.isEnabled())
                  statistics.setInUsedCount(checkedOut.size());

               if (trace)
                  log.trace("supplying new ManagedConnection: " + cl);

               clPermits.put(cl, cl);

               lastUsed = System.currentTimeMillis();

               if (statistics.isEnabled())
                  statistics.deltaTotalGetTime(lastUsed - startWait);

               // Trigger prefill
               prefill();

               // Trigger capacity increase
               if (pool.getCapacity().getIncrementer() != null)
                  CapacityFiller.schedule(new CapacityRequest(this, subject, cri));

               return cl;
            }
            catch (Throwable t)
            {
               if (cl != null || !(t instanceof RetryableException))
                  log.throwableWhileAttemptingGetNewGonnection(cl, t);

               if (cl != null)
               {
                  // Return permit and rethrow
                  synchronized (cls)
                  {
                     checkedOut.remove(cl);
                  }

                  doDestroy(cl);
               }

               if (statistics.isEnabled())
                  statistics.setInUsedCount(checkedOut.size());

               permits.release();

               if (t instanceof ResourceException)
               {
                  throw (ResourceException)t;
               }
               else
               {
                  throw new ResourceException(bundle.unexpectedThrowableWhileTryingCreateConnection(cl), t);
               }
            }
         }
         else
         {
            statistics.deltaBlockingFailureCount();

            // We timed out
            throw new ResourceException(bundle.noMManagedConnectionsAvailableWithinConfiguredBlockingTimeout(
                  poolConfiguration.getBlockingTimeout()));
         }

      }
      catch (InterruptedException ie)
      {
         Thread.interrupted();

         long end = statistics.isEnabled() ? (System.currentTimeMillis() - startWait) : 0L;
         statistics.deltaTotalBlockingTime(end);
         throw new ResourceException(bundle.interruptedWhileRequestingPermit(end));
      }
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
      synchronized (cls)
      {
         for (ConnectionListener cl : checkedOut)
         {
            if (cl.controls(mc, connection))
               return cl;
         }
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void addConnectionListener(ConnectionListener cl)
   {
      synchronized (cls)
      {
         cls.add(cl);
      }
      
      if (statistics.isEnabled())
         statistics.deltaCreatedCount();
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener removeConnectionListener()
   {
      synchronized (cls)
      {
         if (cls.size() > 0)
         {
            if (statistics.isEnabled())
               statistics.deltaDestroyedCount();
            return cls.remove(0);
         }
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
    * {@inheritDoc}
    */
   public void returnConnection(ConnectionListener cl, boolean kill, boolean cleanup)
   {
      if (statistics.isEnabled() && cl.getState() != ConnectionState.DESTROYED)
         statistics.deltaTotalUsageTime(System.currentTimeMillis() - cl.getLastUsedTime());

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
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics.getInUseCount(), maxSize));
      }

      if (cl.getState() == ConnectionState.DESTROYED)
      {
         if (trace)
            log.trace("ManagedConnection is being returned after it was destroyed: " + cl);

         ConnectionListener present = clPermits.remove(cl);
         if (present != null)
         {
            permits.release();
         }

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

      // This is really an error
      if (!kill && isSize(poolConfiguration.getMaxSize() + 1))
      {
         log.destroyingReturnedConnectionMaximumPoolSizeExceeded(cl);
         kill = true;
      }

      synchronized (cls)
      {
         checkedOut.remove(cl);

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

         ConnectionListener present = clPermits.remove(cl);
         if (present != null)
         {
            permits.release();
         }
      }

      if (statistics.isEnabled())
         statistics.setInUsedCount(checkedOut.size());

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
   public void flush(FlushMode mode)
   {
      ArrayList<ConnectionListener> keep = null;
      ArrayList<ConnectionListener> destroy = null;

      synchronized (cls)
      {
         if (FlushMode.ALL == mode)
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

               ConnectionListener present = clPermits.remove(cl);
               if (present != null)
               {
                  permits.release();
               }
            }

            if (statistics.isEnabled())
               statistics.setInUsedCount(checkedOut.size());
         }
         else if (FlushMode.GRACEFULLY == mode)
         {
            if (trace)
               log.trace("Gracefully flushing pool checkedOut=" + checkedOut + " inPool=" + cls);

            // Mark checked out connections as requiring destruction upon return
            for (ConnectionListener cl : checkedOut)
            {
               if (trace)
                  log.trace("Graceful flush marking checked out connection for destruction " + cl);

               cl.setState(ConnectionState.DESTROY);
            }
         }

         // Destroy connections in the pool
         while (cls.size() > 0)
         {
            ConnectionListener cl = cls.remove(0);
            boolean kill = true;

            if (FlushMode.INVALID == mode)
            {
               if (mcf instanceof ValidatingManagedConnectionFactory)
               {
                  try
                  {
                     ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory) mcf;
                     Set candidateSet = Collections.singleton(cl.getManagedConnection());
                     candidateSet = vcf.getInvalidConnections(candidateSet);

                     if (candidateSet == null || candidateSet.size() == 0)
                     {
                        kill = false;
                     }
                  }
                  catch (Throwable t)
                  {
                     log.trace("Exception during invalid flush", t);
                  }
               }
            }

            if (kill)
            {
               if (destroy == null)
                  destroy = new ArrayList<ConnectionListener>(1);

               cl.setState(ConnectionState.DESTROY);
               destroy.add(cl);
            }
            else
            {
               if (keep == null)
                  keep = new ArrayList<ConnectionListener>(1);

               keep.add(cl);
            }
         }

         if (keep != null)
            cls.addAll(keep);
      }

      // We need to destroy some connections
      if (destroy != null)
      {
         for (ConnectionListener cl : destroy)
         {
            if (trace)
               log.trace("Destroying flushed connection " + cl);

            doDestroy(cl);
            cl = null;
         }
      }

      // Trigger prefill
      prefill();
   }

   /**
    * {@inheritDoc}
    */
   public void removeIdleConnections()
   {
      long now = System.currentTimeMillis();
      long timeoutSetting = poolConfiguration.getIdleTimeoutMinutes() * 1000L * 60;

      if (now < (lastIdleCheck + timeoutSetting))
         return;

      if (trace)
         log.tracef("Idle check - Pool: %s MCP: %s", pool.getName(),
                    Integer.toHexString(System.identityHashCode(this)));

      lastIdleCheck = now;

      ArrayList<ConnectionListener> destroyConnections = null;
      long timeout = now - timeoutSetting;

      CapacityDecrementer decrementer = pool.getCapacity().getDecrementer();
      boolean destroy = true;
      int destroyed = 0;

      if (decrementer == null)
         decrementer = DefaultCapacity.DEFAULT_DECREMENTER;

      if (trace)
      {
         synchronized (cls)
         {
            String method = "removeIdleConnections(" + timeout + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                               mcf, clf, pool, poolConfiguration,
                                                               cls, checkedOut, statistics));
         }
      }
      else if (debug)
      {
         String method = "removeIdleConnections(" + timeout + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        statistics.getInUseCount(), maxSize));
      }

      while (destroy)
      {
         synchronized (cls)
         {
            // Nothing left to destroy
            if (cls.size() == 0)
               break;

            // Check the first in the list
            ConnectionListener cl = cls.get(0);

            destroy = decrementer.shouldDestroy(cl, timeout,
                                                cls.size() + checkedOut.size(),
                                                poolConfiguration.getMinSize(),
                                                destroyed);

            if (destroy && shouldRemove())
            {
               if (statistics.isEnabled())
                  statistics.deltaTimedOut();

               // We need to destroy this one
               cls.remove(0);

               if (destroyConnections == null)
                  destroyConnections = new ArrayList<ConnectionListener>(1);

               destroyConnections.add(cl);
               destroyed++;
            }
         }
      }

      // We found some connections to destroy
      if (destroyConnections != null)
      {
         for (ConnectionListener cl : destroyConnections)
         {
            if (trace)
               log.trace("Destroying connection " + cl);

            doDestroy(cl);
            cl = null;
         }

         if (!shutdown.get())
         {
            // Let prefill and use-strict-min be the same
            boolean emptyManagedConnectionPool = false;

            if ((poolConfiguration.isPrefill() || poolConfiguration.isStrictMin()) && pool instanceof PrefillPool)
            {
               if (poolConfiguration.getMinSize() > 0)
               {
                  prefill();
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
            if (emptyManagedConnectionPool && isEmpty())
               pool.emptyManagedConnectionPool(this);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void shutdown()
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

      flush(FlushMode.ALL);
   }

   /**
    * {@inheritDoc}
    */
   public void fillTo(int size)
   {
      if (size <= 0)
         return;

      if (!(poolConfiguration.isPrefill() || poolConfiguration.isStrictMin()))
         return;

      if (!(pool instanceof PrefillPool))
         return;

      if (trace)
      {
         synchronized (cls)
         {
            String method = "fillTo(" + size + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                               mcf, clf, pool, poolConfiguration,
                                                               cls, checkedOut, statistics));
         }
      }
      else if (debug)
      {
         String method = "fillTo(" + size + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        statistics.getInUseCount(), maxSize));
      }

      while (true)
      {
         // Get a permit - avoids a race when the pool is nearly full
         // Also avoids unnessary fill checking when all connections are checked out
         try
         {
            long startWait = statistics.isEnabled() ? System.currentTimeMillis() : 0L;
            if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
            {
               if (statistics.isEnabled())
                  statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
               try
               {
                  if (shutdown.get())
                  {
                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOut.size());
                     return;
                  }

                  // We already have enough connections
                  if (isSize(size))
                  {
                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOut.size());
                     return;
                  }

                  // Create a connection to fill the pool
                  try
                  {
                     ConnectionListener cl = createConnectionEventListener(defaultSubject, defaultCri);

                     synchronized (cls)
                     {
                        if (trace)
                           log.trace("Filling pool cl=" + cl);

                        cls.add(cl);
                        
                        if (statistics.isEnabled())
                           statistics.setInUsedCount(checkedOut.size() + 1);
                     }
                  }
                  catch (ResourceException re)
                  {
                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOut.size());
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
            Thread.interrupted();

            if (trace)
               log.trace("Interrupted while requesting permit in fillTo");
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
    * {@inheritDoc}
    */
   public void increaseCapacity(Subject subject, ConnectionRequestInfo cri)
   {
      // We have already created one connection when this method is scheduled
      int created = 1;
      boolean create = true;

      while (create && !isFull())
      {
         try
         {
            long startWait = statistics.isEnabled() ? System.currentTimeMillis() : 0L;
            if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
            {
               if (statistics.isEnabled())
                  statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
               try
               {
                  if (shutdown.get())
                  {
                     statistics.setInUsedCount(checkedOut.size());
                     return;
                  }

                  int currentSize = 0;
                  synchronized (cls)
                  {
                     currentSize = cls.size() + checkedOut.size();
                  }

                  create = pool.getCapacity().getIncrementer().shouldCreate(currentSize,
                                                                            poolConfiguration.getMaxSize(), created);

                  if (create)
                  {
                     try
                     {
                        ConnectionListener cl = createConnectionEventListener(subject, cri);

                        synchronized (cls)
                        {
                           if (trace)
                              log.trace("Capacity fill: cl=" + cl);

                           cls.add(cl);
                           created++;
                           statistics.setInUsedCount(checkedOut.size() + 1);
                        }
                     }
                     catch (ResourceException re)
                     {
                        statistics.setInUsedCount(checkedOut.size());
                        log.unableFillPool(re);
                        return;
                     }
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
            Thread.interrupted();

            if (trace)
               log.trace("Interrupted while requesting permit in increaseCapacity");
         }
      }

      statistics.setInUsedCount(checkedOut.size());
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
      long start = statistics.isEnabled() ? System.currentTimeMillis() : 0L;

      ManagedConnection mc = mcf.createManagedConnection(subject, cri);

      if (statistics.isEnabled())
      {
         statistics.deltaTotalCreationTime(System.currentTimeMillis() - start);
         statistics.deltaCreatedCount();
      }
      try
      {
         return clf.createConnectionListener(mc, this);
      }
      catch (ResourceException re)
      {
         if (statistics.isEnabled())
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
   void doDestroy(ConnectionListener cl)
   {
      if (cl.getState() == ConnectionState.DESTROYED)
      {
         if (trace)
            log.trace("ManagedConnection is already destroyed " + cl);

         return;
      }

      if (statistics.isEnabled())
         statistics.deltaDestroyedCount();
      cl.setState(ConnectionState.DESTROYED);

      ManagedConnection mc = cl.getManagedConnection();
      try
      {
         mc.destroy();
      }
      catch (Throwable t)
      {
         if (debug)
            log.debug("Exception destroying ManagedConnection " + cl, t);
      }

      mc.removeConnectionEventListener(cl);
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

      if (trace)
      {
         synchronized (cls)
         {
            String method = "validateConnections()";
            log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                               mcf, clf, pool, poolConfiguration,
                                                               cls, checkedOut, statistics));
         }
      }
      else if (debug)
      {
         String method = "validateConnections()";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        statistics.getInUseCount(), maxSize));
      }

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
               catch (ResourceException re)
               {
                  if (cl != null)
                  {
                     doDestroy(cl);
                     cl = null;
                     destroyed = true;
                     anyDestroyed = true;
                  }

                  log.connectionValidatorIgnoredUnexpectedError(re);
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

            if (anyDestroyed)
               prefill();
         }
      }
   }

   /**
    * Get the pool name
    * @return The value
    */
   String getPoolName()
   {
      if (pool == null)
         return "";

      return pool.getName();
   }

   /**
    * Returns the connection listener that should be removed due to background validation
    * @return The listener; otherwise null if none should be removed
    */
   private ConnectionListener removeForFrequencyCheck()
   {
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

      if (debug)
         log.debugf("Checking for connection within frequency: %s", cl);

      return cl;
   }

   /**
    * Return a connection listener to the pool and update its validation timestamp
    * @param cl The listener
    */
   private void returnForFrequencyCheck(ConnectionListener cl)
   {
      if (debug)
         log.debugf("Returning for connection within frequency: %s", cl);

      cl.setLastValidatedTime(System.currentTimeMillis());
      cls.add(cl);
   }

   /**
    * Check if the resource adapter supports lazy association
    */
   private void checkLazyAssociation()
   {
      synchronized (cls)
      {
         ConnectionListener cl = null;

         if (checkedOut.size() > 0)
            cl = checkedOut.get(0);

         if (cl == null && cls.size() > 0)
            cl = cls.get(0);

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
   }

   /**
    * Detach connection listener
    */
   private void detachConnectionListener()
   {
      ConnectionListener cl = null;

      synchronized (cls)
      {
         if (checkedOut.size() > 0)
            cl = checkedOut.remove(0);

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
   }

   /**
    * {@inheritDoc}
    */
   public String[] dumpQueuedThreads()
   {
      List<String> result = new ArrayList<String>();

      if (permits.hasQueuedThreads())
      {
         Collection<Thread> queuedThreads = new ArrayList<Thread>(permits.getQueuedThreads());
         for (Thread t : queuedThreads)
         {
            result.add(dumpQueuedThread(t));
         }
      }

      return result.toArray(new String[result.size()]);
   }


   /**
    * Dump a thread
    * @param t The thread
    * @return The stack trace
    */
   private String dumpQueuedThread(Thread t)
   {
      StringBuilder sb = new StringBuilder();

      // Header
      sb = sb.append("Queued thread: ");
      sb = sb.append(t.getName());
      sb = sb.append(newLine);

      // Body
      StackTraceElement[] stes = SecurityActions.getStackTrace(t);
      if (stes != null)
      {
         for (StackTraceElement ste : stes)
         {
            sb = sb.append("  ");
            sb = sb.append(ste.getClassName());
            sb = sb.append(":");
            sb = sb.append(ste.getMethodName());
            sb = sb.append(":");
            sb = sb.append(ste.getLineNumber());
            sb = sb.append(newLine);
         }
      }

      return sb.toString();
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("SemaphoreArrayListManagedConnectionPool@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[pool=").append(pool.getName());
      sb.append("]");

      return sb.toString();
   }
}
