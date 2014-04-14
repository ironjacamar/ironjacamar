/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
import javax.resource.spi.RetryableException;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Messages;

/**
 * A managed connection pool implementation using ArrayBlockingQueue
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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

   /** Last idle check */
   private long lastIdleCheck;

   /** Last used */
   private long lastUsed;

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
      this.statistics.setEnabled(p.getStatistics().isEnabled());
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
         log.debug("Registering for background validation at interval " + 
                   poolConfiguration.getBackgroundValidationMillis());
         
         //Register validation
         ConnectionValidator.getInstance().registerPool(this, poolConfiguration.getBackgroundValidationMillis());
      }

      statistics.setMaxWaitCount(-1);
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
   public synchronized boolean isEmpty()
   {
      return cls.size() == 0 && checkedOut.size() == 0;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized boolean isFull()
   {
      return checkedOut.size() == poolConfiguration.getMaxSize();
   }

   /**
    * {@inheritDoc}
    */
   public synchronized boolean isIdle()
   {
      return checkedOut.size() == 0;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized int getActive()
   {
      return cls.size() + checkedOut.size();
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
         String method = "getConnection(" + subject + ", " + cri + ")";
         log.trace(ManagedConnectionPoolUtility.fullDetails(System.identityHashCode(this), method,
                                                            mcf, clf, pool, poolConfiguration,
                                                            cls, checkedOut, statistics));
      }
      else if (debug)
      {
         String method = "getConnection(" + subject + ", " + cri + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        statistics.getInUseCount(), poolConfiguration.getMaxSize()));
      }

      subject = (subject == null) ? defaultSubject : subject;
      cri = (cri == null) ? defaultCri : cri;

      ConnectionListener cl = null;
      boolean verifyConnectionListener = true;

      long startWait = statistics.isEnabled() ? System.currentTimeMillis() : 0L;

      if (isFull())
         statistics.deltaWaitCount();

      if (cls.size() > 0)
      {
         if (shutdown.get())
            throw new ResourceException(
               bundle.thePoolHasBeenShutdown(pool.getName(),
                                             Integer.toHexString(System.identityHashCode(this))));
         
         cl = cls.peek();
         if (cl != null)
         {
            try
            {
               cl = cls.poll(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS);
               if (statistics.isEnabled())
                  statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
            }
            catch (InterruptedException ie)
            {
               Thread.interrupted();

               long end = statistics.isEnabled() ? (System.currentTimeMillis() - startWait) : 0L;
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
               
               // Trigger capacity increase
               if (pool.getCapacity().getIncrementer() != null)
                  CapacityFiller.schedule(new CapacityRequest(this, subject, cri));

               verifyConnectionListener = false;
            }
            catch (Throwable t)
            {
               if (cl != null || !(t instanceof RetryableException))
                  log.throwableWhileAttemptingGetNewGonnection(cl, t);

               if (cl != null)
                  doDestroy(cl);

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

            if (shutdown.get())
               throw new ResourceException(
                  bundle.thePoolHasBeenShutdown(pool.getName(),
                                                Integer.toHexString(System.identityHashCode(this))));

            if (cl == null)
            {
               statistics.deltaBlockingFailureCount();

               throw new ResourceException(bundle.noMManagedConnectionsAvailableWithinConfiguredBlockingTimeout(
                     poolConfiguration.getBlockingTimeout()));
            }

            if (statistics.isEnabled())
               statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
         }
         catch (InterruptedException ie)
         {
            Thread.interrupted();

            if (!poolConfiguration.isUseFastFail())
            {
               statistics.deltaBlockingFailureCount();

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
               
                  prefill();

                  // Trigger capacity increase
                  if (pool.getCapacity().getIncrementer() != null)
                     CapacityFiller.schedule(new CapacityRequest(this, subject, cri));

                  if (trace)
                     log.trace("supplying new ManagedConnection: " + cl);
               
                  verifyConnectionListener = false;
               }
               catch (Throwable t)
               {
                  log.throwableWhileAttemptingGetNewGonnection(cl, t);

                  if (cl != null)
                     doDestroy(cl);

                  throw new ResourceException(bundle.unexpectedThrowableWhileTryingCreateConnection(cl), t);
               }
            }
         }
      }

      // Register the connection listener
      checkedOut.add(cl);

      // Update in used statistics
      if (statistics.isEnabled())
         statistics.setInUsedCount(checkedOut.size());
      
      if (!verifyConnectionListener)
      {
         lastUsed = System.currentTimeMillis();
         if (statistics.isEnabled())
            statistics.deltaTotalGetTime(lastUsed - startWait);

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

               lastUsed = System.currentTimeMillis();
               if (statistics.isEnabled())
               {
                  statistics.deltaTotalGetTime(lastUsed - startWait);
                  statistics.deltaTotalPoolTime(lastUsed - cl.getLastUsedTime());
               }

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
            if (statistics.isEnabled())
               statistics.setInUsedCount(checkedOut.size());
            
            doDestroy(cl);
            cl = null;
         }
         catch (Throwable t)
         {
            log.throwableWhileTryingMatchManagedConnection(cl, t);
         
            checkedOut.remove(cl);
            if (statistics.isEnabled())
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
   public void addConnectionListener(ConnectionListener cl)
   {
      try
      {
         cls.put(cl);
         if (statistics.isEnabled())
            statistics.deltaCreatedCount();
      }
      catch (Throwable t)
      {
         Thread.interrupted();

         cl.setState(ConnectionState.DESTROY);
         doDestroy(cl);
      }
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener removeConnectionListener()
   {
      ConnectionListener cl = cls.poll();

      if (cl != null && statistics.isEnabled())
         statistics.deltaDestroyedCount();

      return cl;
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
      if (statistics.isEnabled() && cl.getState() != ConnectionState.DESTROYED)
         statistics.deltaTotalUsageTime(System.currentTimeMillis() - cl.getLastUsedTime());

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
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        statistics.getInUseCount(), poolConfiguration.getMaxSize()));
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

      if (statistics.isEnabled())
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
   public void flush(FlushMode mode)
   {
      ArrayList<ConnectionListener> keep = null;
      ArrayList<ConnectionListener> destroy = null;

      if (trace)
         log.trace("Flushing pool checkedOut=" + checkedOut + " inPool=" + cls);

      if (FlushMode.ALL == mode)
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
      ConnectionListener cl = cls.poll();
      while (cl != null)
      {
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

         cl = cls.poll();
      }

      if (keep != null)
      {
         while (keep.size() > 0)
         {
            cl = keep.remove(0);
            if (!cls.offer(cl))
            {
               if (destroy == null)
                  destroy = new ArrayList<ConnectionListener>(1);
            
               destroy.add(cl);
            }
         }
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

      while (destroy)
      {
         // Check the first in the list
         ConnectionListener cl = cls.peek();
         if (cl != null)
         {
            destroy = decrementer.shouldDestroy(cl, timeout,
                                                cls.size() + checkedOut.size(),
                                                poolConfiguration.getMinSize(),
                                                destroyed);

            if (destroy)
            {
               if (shouldRemove())
               {
                  if (statistics.isEnabled())
                     statistics.deltaTimedOut();

                  // We need to destroy this one
                  if (destroyConnections == null)
                     destroyConnections = new ArrayList<ConnectionListener>(1);

                  cl = cls.poll();

                  if (cl != null)
                  {
                     if (trace)
                        log.trace("Idle connection cl=" + cl);

                     destroyConnections.add(cl);
                     destroyed++;
                  }
                  else
                  {
                     // The connection list were empty
                     destroy = false;
                  }
               }
               else
               {
                  destroy = false;
               }
            }
         }
         else
         {
            // We are done
            destroy = false;
         }
      }

      // We found some connections to destroy
      if (destroyConnections != null)
      {
         for (ConnectionListener cl : destroyConnections)
         {
            if (trace)
               log.trace("Destroying timedout connection " + cl);

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
                  PoolFiller.fillPool(new FillRequest(this, poolConfiguration.getMinSize()));
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

      while (size - (cls.size() + checkedOut.size()) > 0)
      {
         if (shutdown.get())
         {
            if (statistics.isEnabled())
               statistics.setInUsedCount(checkedOut.size());
            return;
         }

         // Create a connection to fill the pool
         ConnectionListener cl = null;
         boolean destroy = false;
         try
         {
            cl = createConnectionEventListener(defaultSubject, defaultCri);
            if (statistics.isEnabled())
               statistics.setInUsedCount(checkedOut.size() + 1);
               
            if ((checkedOut.size() + cls.size()) < size)
            {
               if (trace)
                  log.trace("Filling pool cl=" + cl);

               if (!cls.offer(cl))
               {
                  log.debug("Connection couldn't be inserted during fillTo");
                  destroy = true;
               }
            }
            else
            {
               log.debug("Size reached during fillTo");
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
      if (statistics.isEnabled())
         statistics.setInUsedCount(checkedOut.size());
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
         if (shutdown.get())
         {
            if (statistics.isEnabled())
               statistics.setInUsedCount(checkedOut.size());
            return;
         }

         create = pool.getCapacity().getIncrementer().shouldCreate(cls.size() + checkedOut.size(),
                                                                   poolConfiguration.getMaxSize(), created);

         if (create)
         {
            // Create a connection to fill the pool
            ConnectionListener cl = null;
            boolean destroy = false;

            try
            {
               cl = createConnectionEventListener(defaultSubject, defaultCri);
               if (statistics.isEnabled())
                  statistics.setInUsedCount(checkedOut.size() + 1);
               
               if (!cls.offer(cl))
               {
                  log.debug("Connection couldn't be inserted during increaseCapacity");
                  destroy = true;
               }
               else
               {
                  if (trace)
                     log.trace("Capacity fill: cl=" + cl);

                  created++;
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
               }
            }
         }
      }
      if (statistics.isEnabled())
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
   private void doDestroy(ConnectionListener cl)
   {
      if (cl.getState() == ConnectionState.DESTROYED)
      {
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
                                                        statistics.getInUseCount(), poolConfiguration.getMaxSize()));
      }

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
                  if (!returnForFrequencyCheck(cl))
                     anyDestroyed = true;
               }
            }
         }
      }
      finally
      {
         if (anyDestroyed)
            prefill();
      }
   }

   /**
    * Remove a connection to the pool for a frequency check
    * @return A connection; <code>null</code> if no connections needs to be checked
    */
   private ConnectionListener removeForFrequencyCheck()
   {
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

      log.debugf("Checking for connection within frequency: %s", result);

      return result;
   }

   /**
    * Return a connection to the pool
    * @param cl The connection
    * @return <code>True</code> if the connection was returned; otherwise <code>false</code>
    */
   private boolean returnForFrequencyCheck(ConnectionListener cl)
   {
      log.debugf("Returning for connection within frequency: %s", cl);

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
    * {@inheritDoc}
    */
   public String[] dumpQueuedThreads()
   {
      List<String> result = new ArrayList<String>();

      // Not supported

      return result.toArray(new String[result.size()]);
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
