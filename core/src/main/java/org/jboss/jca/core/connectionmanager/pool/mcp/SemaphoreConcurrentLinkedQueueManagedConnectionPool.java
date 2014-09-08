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
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityDecrementer;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PrefillPool;
import org.jboss.jca.core.connectionmanager.pool.capacity.DefaultCapacity;
import org.jboss.jca.core.connectionmanager.pool.idle.IdleRemover;
import org.jboss.jca.core.connectionmanager.pool.validator.ConnectionValidator;
import org.jboss.jca.core.tracer.Tracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableException;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Messages;

/**
 * The internal implementation
 * 
 * @author <a href="mailto:johara@redhat.com">John O'Hara</a>
 * @version $Revision: 1 $
 */
public class SemaphoreConcurrentLinkedQueueManagedConnectionPool implements ManagedConnectionPool 
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

   /** The connection manager */
   private ConnectionManager cm;

   /** The default subject */
   private Subject defaultSubject;

   /** The default connection request information */
   private ConnectionRequestInfo defaultCri;

   /** The pool configuration */
   private PoolConfiguration poolConfiguration;

   /** The pool */
   private Pool pool;

   /**
    * Copy of the maximum size from the pooling parameters. Dynamic changes to
    * this value are not compatible with the semaphore which cannot change be
    * dynamically changed.
    */
   private int maxSize;

   /** The available connection event listeners */
   private ConcurrentLinkedQueue<ConnectionListenerWrapper> clq;

   /** all connection event listeners */
   private Map<ConnectionListener, ConnectionListenerWrapper> cls;

   /** Current pool size **/
   private AtomicInteger poolSize = new AtomicInteger();

   /** Current checked out connections **/
   private AtomicInteger checkedOutSize = new AtomicInteger();

   /** The permits used to control who can checkout a connection */
   private Semaphore permits;

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
   public SemaphoreConcurrentLinkedQueueManagedConnectionPool() 
   {
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
   public void initialize(ManagedConnectionFactory mcf, ConnectionManager cm, Subject subject,
           ConnectionRequestInfo cri, PoolConfiguration pc, Pool p)
   {
      if (mcf == null)
         throw new IllegalArgumentException(
               "ManagedConnectionFactory is null");

      if (cm == null)
         throw new IllegalArgumentException("ConnectionManager is null");

      if (pc == null)
         throw new IllegalArgumentException("PoolConfiguration is null");

      if (p == null)
         throw new IllegalArgumentException("Pool is null");

      this.mcf = mcf;
      this.cm = cm;
      this.defaultSubject = subject;
      this.defaultCri = cri;
      this.poolConfiguration = pc;
      this.maxSize = pc.getMaxSize();
      this.pool = p;
      this.log = pool.getLogger();
      this.debug = log.isDebugEnabled();
      this.trace = log.isTraceEnabled();
      this.clq = new ConcurrentLinkedQueue<ConnectionListenerWrapper>();
      this.cls = new ConcurrentHashMap<ConnectionListener, ConnectionListenerWrapper>();
      this.statistics = new ManagedConnectionPoolStatisticsImpl(maxSize);
      this.statistics.setEnabled(p.getStatistics().isEnabled());
      this.permits = new Semaphore(maxSize, true, statistics);
      this.poolSize.set(0);
      this.checkedOutSize.set(0);
      this.supportsLazyAssociation = null;
      this.lastIdleCheck = Long.MIN_VALUE;
      this.lastUsed = Long.MAX_VALUE;

      // Schedule managed connection pool for prefill
      if ((pc.isPrefill() || pc.isStrictMin()) && p instanceof PrefillPool && pc.getInitialSize() > 0) 
      {
         PoolFiller.fillPool(new FillRequest(this, pc.getInitialSize()));
      }

      if (poolConfiguration.getIdleTimeoutMinutes() > 0) 
      {
         // Register removal support
         IdleRemover.getInstance().registerPool(this,
               poolConfiguration.getIdleTimeoutMinutes() * 1000L * 60);
      }

      if (poolConfiguration.isBackgroundValidation() && poolConfiguration.getBackgroundValidationMillis() > 0) 
      {
         if (debug)
            log.debug("Registering for background validation at interval "
                  + poolConfiguration.getBackgroundValidationMillis());

         // Register validation
         ConnectionValidator.getInstance().registerPool(this,
               poolConfiguration.getBackgroundValidationMillis());
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRunning() 
   {
      return !pool.isShutdown();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isEmpty() 
   {
      return poolSize.get() == 0;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isFull() 
   {
      return poolSize.get() == maxSize;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isIdle() 
   {
      return checkedOutSize.get() == 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getActive() 
   {
      return poolSize.get();
   }

   /**
    * Check if the pool has reached a certain size
    * 
    * @param size
    *            The size
    * @return True if reached; otherwise false
    */
   private boolean isSize(int size) 
   {
      return poolSize.get() >= size;
   }

   /**
    * {@inheritDoc}
    */
   public void prefill() 
   {
      if (isRunning()
            && (poolConfiguration.isPrefill() || poolConfiguration.isStrictMin()) && 
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
            List<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();
            List<ConnectionListener> available = new ArrayList<ConnectionListener>();
            for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
            {
               if (entry.getValue().isCheckedOut())
                  checkedOut.add(entry.getKey());
               else
                  available.add(entry.getKey());
            }
            Collections.sort(checkedOut);
            Collections.sort(available);
            log.trace(ManagedConnectionPoolUtility.fullDetails(
                  System.identityHashCode(this), method, mcf, cm, pool,
                  poolConfiguration, available, checkedOut, statistics));
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
      {
         statistics.deltaWaitCount();

         if (pool.isSharable() && (supportsLazyAssociation == null || supportsLazyAssociation.booleanValue())) 
         {
            if (supportsLazyAssociation == null)
               checkLazyAssociation();

            if (supportsLazyAssociation != null && supportsLazyAssociation.booleanValue()) 
            {
               if (trace)
                  log.tracef("Trying to detach - Pool: %s MCP: %s", pool.getName(), 
                     Integer.toHexString(System.identityHashCode(this)));

               if (!detachConnectionListener()) 
               {
                  if (trace)
                     log.tracef(
                           "Detaching didn't succeed - Pool: %s MCP: %s", pool.getName(), 
                           Integer.toHexString(System.identityHashCode(this)));
               }
            }
         }
      }

      long startWait = statistics.isEnabled() ? System.currentTimeMillis() : 0L;
      try 
      {
         if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS)) 
         {
            if (statistics.isEnabled())
               statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);

            // We have a permit to get a connection. Is there one in the
            // pool already?
            ConnectionListenerWrapper clw = null;
            do 
            {
               if (!isRunning()) 
               {
                  permits.release();

                  throw new ResourceException(
                     bundle.thePoolHasBeenShutdown(pool.getName(),
                                                        Integer.toHexString(System.identityHashCode(this))));
               }

               clw = clq.poll();

               if (clw != null) 
               {
                  clw.setCheckedOut(true);
                  checkedOutSize.incrementAndGet();

                  // Yes, we retrieved a ManagedConnection from the pool.
                  // Does it match?
                  try 
                  {
                     Object matchedMC = mcf.matchManagedConnections(Collections.singleton(
                        clw.getConnectionListener().getManagedConnection()), subject, cri);

                     boolean valid = true;

                     if (matchedMC != null)
                     {
                        if (poolConfiguration.isValidateOnMatch())
                        {
                           if (mcf instanceof ValidatingManagedConnectionFactory)
                           {
                              try
                              {
                                 ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory) mcf;
                                 Set candidateSet =
                                    Collections.singleton(clw.getConnectionListener().getManagedConnection());
                                 candidateSet = vcf.getInvalidConnections(candidateSet);

                                 if (candidateSet != null && candidateSet.size() > 0)
                                 {
                                    valid = false;
                                 }
                              }
                              catch (Throwable t)
                              {
                                 valid = false;
                                 if (trace)
                                    log.tracef("Exception while ValidateOnMatch: " + t.getMessage(), t);
                              }
                           }
                           else
                           {
                              log.validateOnMatchNonCompliantManagedConnectionFactory(mcf.getClass().getName());
                           }
                        }

                        if (valid)
                        {
                           if (trace)
                              log.trace("supplying ManagedConnection from pool: " + clw.getConnectionListener());

                           lastUsed = System.currentTimeMillis();

                           if (statistics.isEnabled())
                           {
                              statistics.deltaTotalGetTime(lastUsed - startWait);
                              statistics.deltaTotalPoolTime(lastUsed - clw.getConnectionListener().getLastUsedTime());
                           }

                           if (Tracer.isEnabled())
                              Tracer.getConnectionListener(pool.getName(), clw.getConnectionListener(),
                                                           true, pool.isInterleaving());

                           clw.setHasPermit(true);

                           return clw.getConnectionListener();
                        }
                     }

                     // Match did not succeed but no exception was
                     // thrown.
                     // Either we have the matching strategy wrong or the
                     // connection died while being checked. We need to
                     // distinguish these cases, but for now we always
                     // destroy the connection.
                     if (valid)
                     {
                        log.destroyingConnectionNotSuccessfullyMatched(clw.getConnectionListener());
                     }
                     else
                     {
                        log.destroyingConnectionNotValidated(clw.getConnectionListener());
                     }
                     doDestroy(clw);
                     clw = null;
                  } 
                  catch (Throwable t) 
                  {
                     log.throwableWhileTryingMatchManagedConnectionThenDestroyingConnection(
                        clw.getConnectionListener(), t);

                     doDestroy(clw);
                     clw = null;
                  }

                  // We made it here, something went wrong and we should
                  // validate
                  // if we should continue attempting to acquire a
                  // connection
                  if (poolConfiguration.isUseFastFail()) 
                  {
                     if (trace)
                        log.trace("Fast failing for connection attempt. No more attempts will be made to "
                              + "acquire connection from pool and a new connection will be created immeadiately");
                     break;
                  }

               } 
            } 
            while (clq.size() > 0);

            // OK, we couldnt find a working connection from the pool. Make
            // a new one.
            try 
            {
               // No, the pool was empty, so we have to make a new one.
               clw = new ConnectionListenerWrapper(createConnectionEventListener(subject, cri), true, true);

               clw.setCheckedOut(true);
               checkedOutSize.incrementAndGet();

               cls.put(clw.getConnectionListener(), clw);
               poolSize.incrementAndGet();

               if (statistics.isEnabled())
                  statistics.setInUsedCount(checkedOutSize.get());

               if (trace)
                  log.trace("supplying new ManagedConnection: " + clw.getConnectionListener());

               lastUsed = System.currentTimeMillis();

               if (statistics.isEnabled())
                  statistics.deltaTotalGetTime(lastUsed - startWait);

               prefill();

               // Trigger capacity increase
               if (pool.getCapacity().getIncrementer() != null)
                  CapacityFiller.schedule(new CapacityRequest(this, subject, cri));

               if (Tracer.isEnabled())
                  Tracer.getConnectionListener(pool.getName(), clw.getConnectionListener(), false, 
                     pool.isInterleaving());

               return clw.getConnectionListener();
            } 
            catch (Throwable t) 
            {
               if ((clw != null && clw.getConnectionListener() != null) || !(t instanceof RetryableException))
                  log.throwableWhileAttemptingGetNewGonnection(clw != null ? clw.getConnectionListener() : null, t);

               // Return permit and rethrow
               if (clw != null) 
               {
                  doDestroy(clw);
               }

               if (statistics.isEnabled())
                  statistics.setInUsedCount(checkedOutSize.get());

               permits.release();

               throw new ResourceException(
                  bundle.unexpectedThrowableWhileTryingCreateConnection(
                     clw != null ? clw.getConnectionListener() : null), t);
            }
         } 
         else 
         {
            // We timed out
            throw new ResourceException(
                  bundle.noMManagedConnectionsAvailableWithinConfiguredBlockingTimeout(
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
      catch (Exception e) 
      {
         permits.release();

         throw new ResourceException(e.getMessage());
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
      for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
      {
         if (entry.getValue().isCheckedOut() && entry.getKey().controls(mc, connection))
            return entry.getKey();
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
            List<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();
            List<ConnectionListener> available = new ArrayList<ConnectionListener>();
            for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
            {
               if (entry.getValue().isCheckedOut())
                  checkedOut.add(entry.getKey());
               else
                  available.add(entry.getKey());
            }
            Collections.sort(checkedOut);
            Collections.sort(available);
            log.trace(ManagedConnectionPoolUtility.fullDetails(
                  System.identityHashCode(this), method, mcf, cm, pool,
                  poolConfiguration, available, checkedOut, statistics));
         }
      } 
      else if (debug) 
      {
         String method = "returnConnection(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")";
         log.debug(ManagedConnectionPoolUtility.details(method,
               pool.getName(), statistics.getInUseCount(), maxSize));
      }

      ConnectionListenerWrapper clw = cls.get(cl);
      if (cl.getState() == ConnectionState.DESTROYED) 
      {
         if (trace)
            log.trace("ManagedConnection is being returned after it was destroyed: " + cl);

         if (clw.hasPermit()) 
         {
            clw.setHasPermit(false);
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

      // If we are destroying, check the connection is not in the pool
      if (kill) 
      {
         // Adrian Brock: A resource adapter can asynchronously notify us
         // that
         // a connection error occurred.
         // This could happen while the connection is not checked out.
         // e.g. JMS can do this via an ExceptionListener on the connection.
         // I have twice had to reinstate this line of code, PLEASE DO NOT
         // REMOVE IT!
         clq.remove(clw);
         ConnectionListenerWrapper wrapper = cls.remove(cl);
         if (wrapper != null)
         {
            poolSize.decrementAndGet();
            if (wrapper.isCheckedOut())
            {
               wrapper.setCheckedOut(false);
               checkedOutSize.decrementAndGet();
            }
         }
      }
      // return to the pool
      else 
      {
         cl.used();
         if (!clq.contains(cl)) 
         {
            clq.add(cls.get(cl));
         } 
         else 
         {
            log.attemptReturnConnectionTwice(cl, new Throwable("STACKTRACE"));
         }
      }

      if (clw != null) 
      {
         if (clw.hasPermit())
         {
            clw.setHasPermit(false);
            permits.release();
         }
         if (clw.isCheckedOut())
         {
            clw.setCheckedOut(false);
            checkedOutSize.decrementAndGet();
         }
      }

      if (statistics.isEnabled())
         statistics.setInUsedCount(checkedOutSize.get());

      if (kill) 
      {
         if (trace)
            log.trace("Destroying returned connection " + cl);

         doDestroy(clw);
         clw = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush(FlushMode mode) 
   {
      ArrayList<ConnectionListenerWrapper> destroy = null;

      synchronized (cls)
      {
         if (FlushMode.ALL == mode) 
         {
            if (trace) 
            {
               List<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();
               for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
               {
                  if (entry.getValue().isCheckedOut())
                     checkedOut.add(entry.getKey());
               }
               Collections.sort(checkedOut);
               log.trace("Flushing pool checkedOut=" + checkedOut + " inPool=" + cls);

            }

            // Mark checked out connections as requiring destruction
            for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
            {
               if (entry.getValue().isCheckedOut()) 
               {
                  entry.getValue().setCheckedOut(false);
                  checkedOutSize.decrementAndGet();
               }
               if (entry.getValue().hasPermit())
               {
                  entry.getValue().setHasPermit(false);
                  permits.release();
               }

               if (trace)
                  log.trace("Flush marking checked out connection for destruction " + entry.getKey());

               entry.getKey().setState(ConnectionState.DESTROY);

               if (destroy == null)
                  destroy = new ArrayList<ConnectionListenerWrapper>(1);

               destroy.add(entry.getValue());

               clq.remove(entry.getValue());
               cls.remove(entry.getKey());
               poolSize.decrementAndGet();
            }

            if (statistics.isEnabled())
               statistics.setInUsedCount(checkedOutSize.get());
         } 
         else if (FlushMode.GRACEFULLY == mode) 
         {
            if (trace) 
            {
               List<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();
               for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
               {
                  if (entry.getValue().isCheckedOut())
                     checkedOut.add(entry.getKey());
               }
               Collections.sort(checkedOut);
               log.trace("Gracefully flushing pool checkedOut=" + checkedOut + " inPool=" + cls);
            }

            for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
            {
               if (entry.getValue().isCheckedOut()) 
               {
                  if (trace)
                     log.trace("Graceful flush marking checked out connection for destruction " + entry.getKey());
                  
                  entry.getKey().setState(ConnectionState.DESTROY);
               }
            }

         }

         // Destroy connections in the pool
         Iterator<ConnectionListenerWrapper> clqIter = clq.iterator();
         while (clqIter.hasNext()) 
         {
            ConnectionListenerWrapper clw = clqIter.next();
            boolean kill = true;

            if (FlushMode.INVALID == mode) 
            {
               if (mcf instanceof ValidatingManagedConnectionFactory) 
               {
                  try 
                  {
                     ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory) mcf;
                     Set candidateSet = Collections.singleton(clw.getConnectionListener().getManagedConnection());
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
               clq.remove(clw);
               cls.remove(clw.getConnectionListener());
               poolSize.decrementAndGet();

               if (destroy == null)
                  destroy = new ArrayList<ConnectionListenerWrapper>(1);

               clw.getConnectionListener().setState(ConnectionState.DESTROY);
               destroy.add(clw);
            }

         }
      }

      // We need to destroy some connections
      if (destroy != null) 
      {
         for (ConnectionListenerWrapper clw : destroy) 
         {
            if (trace)
               log.trace("Destroying flushed connection " + clw.getConnectionListener());

            doDestroy(clw);
            clw = null;
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

      ArrayList<ConnectionListenerWrapper> destroyConnections = null;
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
            List<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();
            List<ConnectionListener> available = new ArrayList<ConnectionListener>();
            for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
            {
               if (entry.getValue().isCheckedOut())
                  checkedOut.add(entry.getKey());
               else
                  available.add(entry.getKey());
            }
            Collections.sort(checkedOut);
            Collections.sort(available);
            log.trace(ManagedConnectionPoolUtility.fullDetails(
                  System.identityHashCode(this), method, mcf, cm, pool,
                  poolConfiguration, available, checkedOut, statistics));
         }
      } 
      else if (debug) 
      {
         String method = "removeIdleConnections(" + timeout + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics.getInUseCount(), maxSize));
      }

      Iterator<ConnectionListenerWrapper> clwIter = clq.iterator();
      while (clwIter.hasNext()) 
      {
         // Nothing left to destroy
         if (clq.size() == 0)
            break;

         ConnectionListenerWrapper clw = clwIter.next();

         destroy = decrementer.shouldDestroy(clw.getConnectionListener(),
               timeout, poolSize.get(),
               poolConfiguration.getMinSize(), destroyed);

         if (destroy) 
         {
            if (shouldRemove()) 
            {
               if (statistics.isEnabled())
                  statistics.deltaTimedOut();

               if (trace)
                  log.trace("Idle connection cl=" + clw.getConnectionListener());

               // We need to destroy this one
               if (cls.remove(clw.getConnectionListener()) != null) 
               {
                  poolSize.decrementAndGet();
               }
               else 
               {
                  log.trace("Connection Pool did not contain: " + clw.getConnectionListener());
               }

               if (!clq.remove(clw)) 
               {
                  log.trace("Available connection queue did not contain: " + clw.getConnectionListener());
               }

               if (destroyConnections == null)
                  destroyConnections = new ArrayList<ConnectionListenerWrapper>(1);

               destroyConnections.add(clw);
               destroyed++;
            } 
            else 
            {
               destroy = false;
            }
         }
      }

      // We found some connections to destroy
      if (destroyConnections != null) 
      {
         for (ConnectionListenerWrapper clw : destroyConnections) 
         {
            if (trace)
               log.trace("Destroying connection " + clw.getConnectionListener());

            doDestroy(clw);
            clw = null;
         }

         if (isRunning()) 
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
   public void shutdown() 
   {
      if (trace)
         log.tracef("Shutdown - Pool: %s MCP: %s", pool.getName(),
               Integer.toHexString(System.identityHashCode(this)));

      IdleRemover.getInstance().unregisterPool(this);
      ConnectionValidator.getInstance().unregisterPool(this);

      if (checkedOutSize.get() > 0) 
      {
         for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
         {
            if (entry.getValue().isCheckedOut())
               log.destroyingActiveConnection(pool.getName(), entry.getKey().getManagedConnection());

            if (Tracer.isEnabled())
               Tracer.clearConnectionListener(pool.getName(), entry.getKey());

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
            List<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();
            List<ConnectionListener> available = new ArrayList<ConnectionListener>();
            for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
            {
               if (entry.getValue().isCheckedOut())
                  checkedOut.add(entry.getKey());
               else
                  available.add(entry.getKey());
            }
            Collections.sort(checkedOut);
            Collections.sort(available);
            log.trace(ManagedConnectionPoolUtility.fullDetails(
                  System.identityHashCode(this), method, mcf, cm, pool,
                  poolConfiguration, available, checkedOut, statistics));
         }
      } 
      else if (debug) 
      {
         String method = "fillTo(" + size + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(), statistics.getInUseCount(), maxSize));
      }

      while (true) 
      {
         // Get a permit - avoids a race when the pool is nearly full
         // Also avoids unnessary fill checking when all connections are
         // checked out
         try 
         {
            long startWait = statistics.isEnabled() ? System.currentTimeMillis() : 0L;
            if (permits.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS)) 
            {
               if (statistics.isEnabled())
                  statistics.deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
               try 
               {
                  if (!isRunning()) 
                  {
                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOutSize.get());
                     return;
                  }

                  // We already have enough connections
                  if (isSize(size)) 
                  {
                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOutSize.get());
                     return;
                  }

                  // Create a connection to fill the pool
                  try 
                  {
                     ConnectionListener cl = createConnectionEventListener(defaultSubject, defaultCri);

                     if (trace)
                        log.trace("Filling pool cl=" + cl);

                     cls.put(cl, new ConnectionListenerWrapper(cl, false, false));
                     poolSize.incrementAndGet();
                     clq.add(cls.get(cl));

                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOutSize.get() + 1);
                  } 
                  catch (ResourceException re) 
                  {
                     if (statistics.isEnabled())
                        statistics.setInUsedCount(checkedOutSize.get());
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
    * 
    * @return The module
    */
   public ManagedConnectionPoolStatistics getStatistics() 
   {
      return statistics;
   }

   @Override
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
                  if (!isRunning()) 
                  {
                     statistics.setInUsedCount(checkedOutSize.get());
                     return;
                  }

                  int currentSize = cls.size();

                  create = pool.getCapacity().getIncrementer().shouldCreate(currentSize,
                                                                            poolConfiguration.getMaxSize(), created);

                  if (create) 
                  {
                     try 
                     {
                        ConnectionListener cl = createConnectionEventListener(subject, cri);

                        if (trace)
                           log.trace("Capacity fill: cl=" + cl);

                        cls.put(cl, new ConnectionListenerWrapper(cl, false, false));
                        poolSize.incrementAndGet();
                        clq.add(cls.get(cl));

                        created++;
                        statistics.setInUsedCount(checkedOutSize.get() + 1);
                     } 
                     catch (ResourceException re) 
                     {
                        statistics.setInUsedCount(checkedOutSize.get());
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

      statistics.setInUsedCount(checkedOutSize.get());

   }

   /**
    * {@inheritDoc}
    */
   public void addConnectionListener(ConnectionListener cl) 
   {
      cls.put(cl, new ConnectionListenerWrapper(cl, false, false));
      poolSize.incrementAndGet();
      clq.add(cls.get(cl));

      if (statistics.isEnabled())
         statistics.deltaCreatedCount();
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener removeConnectionListener() 
   {
      if (cls.size() > 0) 
      {
         if (statistics.isEnabled())
            statistics.deltaDestroyedCount();
         ConnectionListenerWrapper clw = clq.remove();
         if (cls.remove(clw.getConnectionListener()) != null)
            poolSize.decrementAndGet();
         return clw.getConnectionListener();
      }

      return null;
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
    * 
    * @param t
    *            The thread
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
    * Create a connection event listener
    * 
    * @param subject
    *            the subject
    * @param cri
    *            the connection request information
    * @return the new listener
    * @throws ResourceException
    *             for any error
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
         return cm.createConnectionListener(mc, this);
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
    * @param clw
    *            the connection to destroy
    */
   private void doDestroy(ConnectionListenerWrapper clw) 
   {
      if (clw != null) 
      {
         removeConnectionListenerFromPool(clw);
         
         if (clw.getConnectionListener() != null) 
         {
            if (clw.getConnectionListener().getState() == ConnectionState.DESTROYED) 
            {
               if (trace)
                  log.trace("ManagedConnection is already destroyed " + clw.getConnectionListener());

               return;
            }

            if (statistics.isEnabled())
               statistics.deltaDestroyedCount();
            clw.getConnectionListener().setState(ConnectionState.DESTROYED);

            ManagedConnection mc = clw.getConnectionListener().getManagedConnection();
            try 
            {
               mc.destroy();
            }
            catch (Throwable t) 
            {
               log.debug("Exception destroying ManagedConnection " + clw.getConnectionListener(), t);
            }

            mc.removeConnectionEventListener(clw.getConnectionListener());

            clw.setConnectionListener(null);

         }
      }
   }

   /**
    * Should any connections be removed from the pool
    * 
    * @return True if connections should be removed; otherwise false
    */
   private boolean shouldRemove() 
   {
      boolean remove = true;

      if (poolConfiguration.isStrictMin() && pool instanceof PrefillPool) 
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
                  if (clq.size() == 0) 
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
                           ConnectionListenerWrapper clw = cls.remove(cl);

                           doDestroy(clw);
                           clw = null;
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
                     ConnectionListenerWrapper clw = cls.remove(cl);

                     doDestroy(clw);
                     clw = null;
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
    * Returns the connection listener that should be removed due to background
    * validation
    * 
    * @return The listener; otherwise null if none should be removed
    */
   private ConnectionListener removeForFrequencyCheck() 
   {
      log.debug("Checking for connection within frequency");

      ConnectionListenerWrapper clw = null;

      for (Iterator<ConnectionListenerWrapper> iter = clq.iterator(); iter.hasNext();) 
      {
         clw = iter.next();
         long lastCheck = clw.getConnectionListener().getLastValidatedTime();

         if ((System.currentTimeMillis() - lastCheck) >= poolConfiguration.getBackgroundValidationMillis()) 
         {
            clq.remove(clw);
            break;
         } 
         else 
         {
            clw = null;
         }
      }

      if (clw != null)
         return clw.getConnectionListener();
      else
         return null;
   }

   /**
    * Return a connection listener to the pool and update its validation
    * timestamp
    * 
    * @param cl
    *            The listener
    */
   private void returnForFrequencyCheck(ConnectionListener cl) 
   {
      log.debug("Returning for connection within frequency");

      cl.setLastValidatedTime(System.currentTimeMillis());
      clq.add(cls.get(cl));
   }

   /**
    * Check if the resource adapter supports lazy association
    */
   private void checkLazyAssociation() 
   {
      ConnectionListener cl = null;

      if (cls.size() > 0)
         cl = cls.keySet().iterator().next();

      if (cl != null) 
      {
         if (cl.supportsLazyAssociation()) 
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
    * 
    * @return The outcome
    */
   private boolean detachConnectionListener() 
   {
      synchronized (cls) 
      {
         ConnectionListener cl = null;
         try 
         {
            for (Entry<ConnectionListener, ConnectionListenerWrapper> entry : cls.entrySet()) 
            {
               cl = entry.getKey();
               
               if (entry.getValue().isCheckedOut()) 
               {
                  if (!cl.isEnlisted()) 
                  {
                     if (trace)
                        log.tracef("Detach: %s", cl);

                     DissociatableManagedConnection dmc = (DissociatableManagedConnection) cl.getManagedConnection();
                     dmc.dissociateConnections();

                     cl.unregisterConnections();

                     if (Tracer.isEnabled())
                        Tracer.returnConnectionListener(pool.getName(), cl, false, pool.isInterleaving());

                     returnConnection(cl, false, false);

                     return true;
                  }
               }
            }
         } 
         catch (Throwable t) 
         {
            // Ok - didn't work; nuke it and disable
            if (debug)
               log.debug("Exception during detach for: " + pool.getName(),
                     t);

            supportsLazyAssociation = Boolean.FALSE;

            if (cl != null) 
            {
               if (Tracer.isEnabled())
                  Tracer.returnConnectionListener(pool.getName(), cl, true, pool.isInterleaving());

               returnConnection(cl, true, true);
            }
         }
      }

      return false;
   }

   /**
    * Remove Connection Listener from pool and update counters and statistics
    */
   private void removeConnectionListenerFromPool(ConnectionListenerWrapper clw)
   {

      if (clw.getConnectionListener() != null)
         clq.remove(clw.getConnectionListener());

      // remove connection listener from pool
      // ConcurrentHashMap does *not* not allow null to be used as a key or value, 
      // so null indicates cls did not contain the ConnectionListenerWrapper 
      if (cls.remove(clw) != null)
         poolSize.decrementAndGet();

      //update counter and statistics
      if (clw.isCheckedOut()) 
      {
         clw.setCheckedOut(false);
         checkedOutSize.decrementAndGet();
      }

      if (statistics.isEnabled())
         statistics.setInUsedCount(checkedOutSize.get());

   }
   
   /**
    * String representation
    * 
    * @return The string
    */
   @Override
   public String toString() 
   {
      StringBuilder sb = new StringBuilder();

      sb.append("SemaphoreConcurrentLinkedQueueManagedConnectionPool@");
      sb.append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[pool=").append(pool.getName());
      sb.append("]");

      return sb.toString();
   }

   /**
    * Connection Listener wrapper to retain connection listener pool state
    * 
    * @author <a href="mailto:johara@redhat.com">John O'Hara</a>
    * @version $Revision: 1 $
    */
   class ConnectionListenerWrapper 
   {
      private ConnectionListener cl;
      private boolean checkedOut;
      private boolean hasPermit;

      /**
       * Constructor
       * 
       * @param connectionListener wrapped Connection Listener
       */
      public ConnectionListenerWrapper(ConnectionListener connectionListener) 
      {
         this(connectionListener, false, false);
      }

      /**
       * Constructor
       * 
       * @param connectionListener wrapped Connection Listener
       * @param checkedOut is connection listener checked out
       */
      public ConnectionListenerWrapper(ConnectionListener connectionListener, boolean checkedOut) 
      {
         this(connectionListener, checkedOut, false);
      }

      /**
       * Constructor
       * 
       * @param connectionListener wrapped Connection Listener
       * @param checkedOut is connection listener checked out
       * @param hasPermit does connection listener have a permit
       */
      public ConnectionListenerWrapper(ConnectionListener connectionListener, boolean checkedOut, boolean hasPermit) 
      {
         this.cl = connectionListener;
         this.checkedOut = checkedOut;
         this.hasPermit = hasPermit;
      }

      /**
       * Get wrapped Connection Listener
       * 
       * @return Wrapped Connection Listener
       */
      public ConnectionListener getConnectionListener() 
      {
         return cl;
      }

      /**
       * Set wrapped Connection Listener
       * 
       * @param connectionListener wrapped Connection Listener
       */
      public void setConnectionListener(ConnectionListener connectionListener) 
      {
         this.cl = connectionListener;
      }

      /**
       * Is Connection Listener checked out
       * 
       * @return Connection Listener is checked out
       */
      public boolean isCheckedOut() 
      {
         return checkedOut;
      }

      /**
       * Set whether Connection Listener is checkout out
       * 
       * @param checkedOut is connection listener checked out
       */
      public void setCheckedOut(boolean checkedOut)
      {
         this.checkedOut = checkedOut;
      }

      /**
       * Does Connection Listener have a permit
       * 
       * @return Connection Listener has a permit
       */
      public boolean hasPermit() 
      {
         return hasPermit;
      }

      /**
       * Set whether Connection Listener has permit
       * 
       * @param hasPermit does connection listener have a permit
       */
      public void setHasPermit(boolean hasPermit) 
      {
         this.hasPermit = hasPermit;
      }
   }
}
