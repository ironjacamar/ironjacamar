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
import org.jboss.jca.core.connectionmanager.pool.capacity.TimedOutDecrementer;
import org.jboss.jca.core.connectionmanager.pool.capacity.TimedOutFIFODecrementer;
import org.jboss.jca.core.connectionmanager.pool.idle.IdleRemover;
import org.jboss.jca.core.connectionmanager.pool.validator.ConnectionValidator;
import org.jboss.jca.core.tracer.Tracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
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
 */
public class SemaphoreArrayListManagedConnectionPool implements ManagedConnectionPool
{
   /** The log */
   private CoreLogger log;

   /** Whether debug is enabled */
   private boolean debug;

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

   /** FIFO / FILO */
   private boolean fifo;
   
   /**
    * Copy of the maximum size from the pooling parameters.
    * Dynamic changes to this value are not compatible with
    * the semaphore which cannot change be dynamically changed.
    */
   private int maxSize;

   /** The available connection event listeners */
   private ArrayList<ConnectionListener> cls;

   /** The map of connection listeners which has a permit */
   private final ConcurrentMap<ConnectionListener, ConnectionListener> clPermits =
      new ConcurrentHashMap<ConnectionListener, ConnectionListener>();

   /** The checked out connections */
   private final ArrayList<ConnectionListener> checkedOut = new ArrayList<ConnectionListener>();

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
   public void initialize(ManagedConnectionFactory mcf, ConnectionManager cm, Subject subject,
                          ConnectionRequestInfo cri, PoolConfiguration pc, Pool p)
   {
      if (mcf == null)
         throw new IllegalArgumentException("ManagedConnectionFactory is null");

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
      this.fifo = p.isFIFO();
      this.log = pool.getLogger();
      this.debug = log.isDebugEnabled();
      this.cls = new ArrayList<ConnectionListener>(this.maxSize);
      this.supportsLazyAssociation = null;
      this.lastIdleCheck = System.currentTimeMillis();
      this.lastUsed = Long.MAX_VALUE;

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
      return !pool.isShutdown();
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
      if (isRunning() &&
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

      if (log.isTraceEnabled())
      {
         synchronized (cls)
         {
            String method = "getConnection(" + subject + ", " + cri + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(this, method,
                                                               mcf, cm, pool, poolConfiguration,
                                                               cls, checkedOut, pool.getInternalStatistics(),
                                                               subject, cri));
         }
      }
      else if (debug)
      {
         String method = "getConnection(" + subject + ", " + cri + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        pool.getInternalStatistics().getInUseCount(), maxSize));
      }

      subject = (subject == null) ? defaultSubject : subject;
      cri = (cri == null) ? defaultCri : cri;

      if (pool.isFull())
      {
         if (pool.getInternalStatistics().isEnabled())
            pool.getInternalStatistics().deltaWaitCount();

         if (pool.isSharable() && (supportsLazyAssociation == null || supportsLazyAssociation.booleanValue()))
         {
            if (supportsLazyAssociation == null)
               checkLazyAssociation();

            if (supportsLazyAssociation != null && supportsLazyAssociation.booleanValue())
            {
               if (log.isTraceEnabled())
                  log.tracef("Trying to detach - Pool: %s MCP: %s", pool.getName(),
                             Integer.toHexString(System.identityHashCode(this)));

               if (!detachConnectionListener())
               {
                  log.tracef("Detaching didn't succeed - Pool: %s MCP: %s", pool.getName(),
                                Integer.toHexString(System.identityHashCode(this)));
               }
            }
         }
      }

      long startWait = pool.getInternalStatistics().isEnabled() ? System.currentTimeMillis() : 0L;
      try
      {
         if (pool.getLock().tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
         {
            if (pool.getInternalStatistics().isEnabled())
               pool.getInternalStatistics().deltaTotalBlockingTime(System.currentTimeMillis() - startWait);

            //We have a permit to get a connection. Is there one in the pool already?
            ConnectionListener cl = null;
            do
            {
               if (!isRunning())
               {
                  pool.getLock().release();
                  throw new ResourceException(
                     bundle.thePoolHasBeenShutdown(pool.getName(),
                                                   Integer.toHexString(System.identityHashCode(this))));
               }

               synchronized (cls)
               {
                  if (cls.size() > 0)
                  {
                     if (fifo)
                     {
                        cl = cls.remove(0);
                     }
                     else
                     {
                        cl = cls.remove(cls.size() - 1);
                     }
                     checkedOut.add(cl);
                  }
               }

               if (cl != null)
               {
                  //Yes, we retrieved a ManagedConnection from the pool. Does it match?
                  try
                  {
                     Object matchedMC = mcf.matchManagedConnections(Collections.singleton(cl.getManagedConnection()),
                                                                    subject, cri);

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
                                 Set candidateSet = Collections.singleton(cl.getManagedConnection());
                                 candidateSet = vcf.getInvalidConnections(candidateSet);

                                 if (candidateSet != null && candidateSet.size() > 0)
                                 {
                                    valid = false;
                                 }
                              }
                              catch (Throwable t)
                              {
                                 valid = false;
                                 if (log.isTraceEnabled())
                                    log.trace("Exception while ValidateOnMatch: " + t.getMessage(), t);
                              }
                           }
                           else
                           {
                              log.validateOnMatchNonCompliantManagedConnectionFactory(mcf.getClass().getName());
                           }
                        }

                        if (valid)
                        {
                           log.tracef("supplying ManagedConnection from pool: %s", cl);

                           clPermits.put(cl, cl);

                           lastUsed = System.currentTimeMillis();
                           cl.setLastCheckedOutTime(lastUsed);

                           if (pool.getInternalStatistics().isEnabled())
                           {
                              pool.getInternalStatistics().deltaTotalGetTime(lastUsed - startWait);
                              pool.getInternalStatistics().deltaTotalPoolTime(lastUsed - cl.getLastReturnedTime());
                           }

                           if (Tracer.isEnabled())
                              Tracer.getConnectionListener(pool.getName(), this, cl, true, pool.isInterleaving(),
                                                           Tracer.isRecordCallstacks() ?
                                                           new Throwable("CALLSTACK") : null);

                           return cl;
                        }
                     }

                     // Match did not succeed but no exception was thrown.
                     // Either we have the matching strategy wrong or the
                     // connection died while being checked.  We need to
                     // distinguish these cases, but for now we always
                     // destroy the connection.
                     if (valid)
                     {
                        log.destroyingConnectionNotSuccessfullyMatched(cl);
                     }
                     else
                     {
                        log.destroyingConnectionNotValidated(cl);
                     }

                     synchronized (cls)
                     {
                        checkedOut.remove(cl);
                     }

                     if (pool.getInternalStatistics().isEnabled())
                        pool.getInternalStatistics().deltaTotalPoolTime(System.currentTimeMillis() -
                                                                        cl.getLastReturnedTime());

                     if (Tracer.isEnabled())
                        Tracer.destroyConnectionListener(pool.getName(), this, cl, false, false, true, false, false,
                                                         false, false,
                                                         Tracer.isRecordCallstacks() ?
                                                         new Throwable("CALLSTACK") : null);
                     
                     cl.destroy();
                     cl = null;
                  }
                  catch (Throwable t)
                  {
                     log.throwableWhileTryingMatchManagedConnectionThenDestroyingConnection(cl, t);

                     synchronized (cls)
                     {
                        checkedOut.remove(cl);
                     }

                     if (pool.getInternalStatistics().isEnabled())
                        pool.getInternalStatistics().deltaTotalPoolTime(System.currentTimeMillis() -
                                                                        cl.getLastReturnedTime());

                     if (Tracer.isEnabled())
                        Tracer.destroyConnectionListener(pool.getName(), this, cl, false, false, false, false, true,
                                                         false, false,
                                                         Tracer.isRecordCallstacks() ?
                                                         new Throwable("CALLSTACK") : null);
                     
                     cl.destroy();
                     cl = null;
                  }

                  // We made it here, something went wrong and we should validate
                  // if we should continue attempting to acquire a connection
                  if (poolConfiguration.isUseFastFail())
                  {
                     if (log.isTraceEnabled())
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

               if (Tracer.isEnabled())
                  Tracer.createConnectionListener(pool.getName(), this, cl, cl.getManagedConnection(),
                                                  true, false, false,
                                                  Tracer.isRecordCallstacks() ?
                                                  new Throwable("CALLSTACK") : null);

               synchronized (cls)
               {
                  checkedOut.add(cl);
               }

               log.tracef("supplying new ManagedConnection: %s", cl);

               clPermits.put(cl, cl);

               lastUsed = System.currentTimeMillis();

               if (pool.getInternalStatistics().isEnabled())
                  pool.getInternalStatistics().deltaTotalGetTime(lastUsed - startWait);

               // Trigger prefill
               prefill();

               // Trigger capacity increase
               if (pool.getCapacity().getIncrementer() != null)
                  CapacityFiller.schedule(new CapacityRequest(this, subject, cri));
               
               if (Tracer.isEnabled())
                  Tracer.getConnectionListener(pool.getName(), this, cl, false, pool.isInterleaving(),
                                               Tracer.isRecordCallstacks() ?
                                               new Throwable("CALLSTACK") : null);

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

                  if (Tracer.isEnabled())
                     Tracer.destroyConnectionListener(pool.getName(), this, cl, false, false, false, false, true,
                                                      false, false,
                                                      Tracer.isRecordCallstacks() ?
                                                      new Throwable("CALLSTACK") : null);
                     
                  cl.destroy();
               }

               pool.getLock().release();

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
            if (pool.getInternalStatistics().isEnabled())
               pool.getInternalStatistics().deltaBlockingFailureCount();

            // We timed out
            throw new ResourceException(bundle.noMManagedConnectionsAvailableWithinConfiguredBlockingTimeout(
                  poolConfiguration.getBlockingTimeout()));
         }

      }
      catch (InterruptedException ie)
      {
         Thread.interrupted();

         long end = pool.getInternalStatistics().isEnabled() ? (System.currentTimeMillis() - startWait) : 0L;
         pool.getInternalStatistics().deltaTotalBlockingTime(end);
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
      
      if (pool.getInternalStatistics().isEnabled())
         pool.getInternalStatistics().deltaCreatedCount();
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
            if (pool.getInternalStatistics().isEnabled())
               pool.getInternalStatistics().deltaDestroyedCount();
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
      if (pool.getInternalStatistics().isEnabled() && cl.getState() != ConnectionState.DESTROYED)
         pool.getInternalStatistics().deltaTotalUsageTime(System.currentTimeMillis() - cl.getLastCheckedOutTime());

      if (log.isTraceEnabled())
      {
         synchronized (cls)
         {
            String method = "returnConnection(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(this, method,
                                                               mcf, cm, pool, poolConfiguration,
                                                               cls, checkedOut, pool.getInternalStatistics(),
                                                               defaultSubject, defaultCri));
         }
      }
      else if (debug)
      {
         String method = "returnConnection(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        pool.getInternalStatistics().getInUseCount(), maxSize));
      }

      if (cl.getState() == ConnectionState.DESTROYED)
      {
         log.tracef("ManagedConnection is being returned after it was destroyed: %s", cl);

         ConnectionListener present = clPermits.remove(cl);
         if (present != null)
         {
            pool.getLock().release();
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
         synchronized (cls)
         {
            // Adrian Brock: A resource adapter can asynchronously notify us that
            // a connection error occurred.
            // This could happen while the connection is not checked out.
            // e.g. JMS can do this via an ExceptionListener on the connection.
            // I have twice had to reinstate this line of code, PLEASE DO NOT REMOVE IT!
            checkedOut.remove(cl);
            cls.remove(cl);

            if (clPermits.remove(cl) != null)
            {
               pool.getLock().release();
            }
         }
      }
      // return to the pool
      else
      {
         cl.toPool();
         synchronized (cls)
         {
            checkedOut.remove(cl);
            if (!cls.contains(cl))
            {
               cls.add(cl);
            }
            else
            {
               log.attemptReturnConnectionTwice(cl, new Throwable("STACKTRACE"));
            }

            if (clPermits.remove(cl) != null)
            {
               pool.getLock().release();
            }
         }
      }

      if (kill)
      {
         log.tracef("Destroying returned connection %s", cl);

         if (Tracer.isEnabled())
            Tracer.destroyConnectionListener(pool.getName(), this, cl, true, false, false, false, false,
                                             false, false,
                                             Tracer.isRecordCallstacks() ?
                                             new Throwable("CALLSTACK") : null);
                     
         cl.destroy();
         cl = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush(FlushMode mode, Collection<ConnectionListener> toDestroy)
   {
      ArrayList<ConnectionListener> keep = null;

      synchronized (cls)
      {
         if (FlushMode.ALL == mode)
         {
            log.tracef("Flushing pool checkedOut=%s inPool=%s", checkedOut, cls);

            // Mark checked out connections as requiring destruction
            while (checkedOut.size() > 0)
            {
               ConnectionListener cl = checkedOut.remove(0);

               log.tracef("Flush marking checked out connection for destruction %s", cl);

               cl.setState(ConnectionState.DESTROY);

               if (pool.getInternalStatistics().isEnabled())
                  pool.getInternalStatistics().deltaTotalUsageTime(System.currentTimeMillis() -
                                                                   cl.getLastCheckedOutTime());

               toDestroy.add(cl);

               ConnectionListener present = clPermits.remove(cl);
               if (present != null)
               {
                  pool.getLock().release();
               }
            }
         }
         else if (FlushMode.GRACEFULLY == mode)
         {
            log.tracef("Gracefully flushing pool checkedOut=%s inPool=%s", checkedOut , cls);

            // Mark checked out connections as requiring destruction upon return
            for (ConnectionListener cl : checkedOut)
            {
               log.tracef("Graceful flush marking checked out connection for destruction %s", cl);

               cl.setState(ConnectionState.DESTROY);
            }
         }

         // Destroy connections in the pool
         while (cls.size() > 0)
         {
            ConnectionListener cl = cls.remove(0);
            boolean kill = true;

            if (FlushMode.INVALID == mode && cl.getState().equals(ConnectionState.NORMAL))
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
               if (pool.getInternalStatistics().isEnabled())
                  pool.getInternalStatistics().deltaTotalPoolTime(System.currentTimeMillis() -
                                                                  cl.getLastReturnedTime());

               cl.setState(ConnectionState.DESTROY);
               toDestroy.add(cl);
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

      CapacityDecrementer decrementer = pool.getCapacity().getDecrementer();

      if (decrementer == null)
         decrementer = DefaultCapacity.DEFAULT_DECREMENTER;

      if (TimedOutDecrementer.class.getName().equals(decrementer.getClass().getName()) ||
          TimedOutFIFODecrementer.class.getName().equals(decrementer.getClass().getName()))
      {
         // Allow through each minute
         if (now < (lastIdleCheck + 60000L))
            return;
      }
      else
      {
         // Otherwise, strict check
         if (now < (lastIdleCheck + timeoutSetting))
            return;
      }

      lastIdleCheck = now;

      ArrayList<ConnectionListener> destroyConnections = new ArrayList<ConnectionListener>();
      long timeout = now - timeoutSetting;

      boolean destroy = true;
      int destroyed = 0;

      if (log.isTraceEnabled())
      {
         synchronized (cls)
         {
            String method = "removeIdleConnections(" + timeout + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(this, method,
                                                               mcf, cm, pool, poolConfiguration,
                                                               cls, checkedOut, pool.getInternalStatistics(),
                                                               defaultSubject, defaultCri));
         }
      }
      else if (debug)
      {
         String method = "removeIdleConnections(" + timeout + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        pool.getInternalStatistics().getInUseCount(), maxSize));
      }

      while (destroy)
      {
         synchronized (cls)
         {
            // No free connection listeners
            if (cls.size() == 0)
               break;

            // We always check the first connection listener, since it is the oldest
            ConnectionListener cl = cls.get(0);

            destroy = decrementer.shouldDestroy(cl, timeout,
                                                cls.size() + checkedOut.size(),
                                                poolConfiguration.getMinSize(),
                                                destroyed);

            if (destroy)
            {
               if (shouldRemove() || !isRunning())
               {
                  if (pool.getInternalStatistics().isEnabled())
                     pool.getInternalStatistics().deltaTimedOut();

                  log.tracef("Idle connection cl=%s", cl);

                  // We need to destroy this one
                  cls.remove(0);
                  destroyConnections.add(cl);
                  destroyed++;
               }
               else
               {
                  destroy = false;
               }
            }
         }
      }

      // We found some connections to destroy
      if (destroyConnections.size() > 0 || isEmpty())
      {
         for (ConnectionListener cl : destroyConnections)
         {
            log.tracef("Destroying connection %s", cl);

            if (pool.getInternalStatistics().isEnabled())
               pool.getInternalStatistics().deltaTotalPoolTime(System.currentTimeMillis() -
                                                               cl.getLastReturnedTime());

            if (Tracer.isEnabled())
               Tracer.destroyConnectionListener(pool.getName(), this, cl, false, true, false, false, false,
                                                false, false,
                                                Tracer.isRecordCallstacks() ?
                                                new Throwable("CALLSTACK") : null);
                     
            cl.destroy();
            cl = null;
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
      final Collection<ConnectionListener> toDestroy;
      synchronized (this)
      {
         if (log.isTraceEnabled())
            log.tracef("Shutdown - Pool: %s MCP: %s", pool.getName(), Integer.toHexString(System.identityHashCode(
                  this)));

         IdleRemover.getInstance().unregisterPool(this);
         ConnectionValidator.getInstance().unregisterPool(this);

         if (checkedOut.size() > 0)
         {
            for (ConnectionListener cl : checkedOut)
            {
               log.destroyingActiveConnection(pool.getName(), cl.getManagedConnection());

               if (Tracer.isEnabled())
                  Tracer.clearConnectionListener(pool.getName(), this, cl);
            }
         }

         toDestroy = new ArrayList<ConnectionListener>();
         flush(FlushMode.ALL, toDestroy);
      }
      for (ConnectionListener cl : toDestroy)
      {
         cl.destroy();
      }
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

      if (log.isTraceEnabled())
      {
         synchronized (cls)
         {
            String method = "fillTo(" + size + ")";
            log.trace(ManagedConnectionPoolUtility.fullDetails(this, method,
                                                               mcf, cm, pool, poolConfiguration,
                                                               cls, checkedOut, pool.getInternalStatistics(),
                                                               defaultSubject, defaultCri));
         }
      }
      else if (debug)
      {
         String method = "fillTo(" + size + ")";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        pool.getInternalStatistics().getInUseCount(), maxSize));
      }

      while (!pool.isFull())
      {
         // Get a permit - avoids a race when the pool is nearly full
         // Also avoids unnessary fill checking when all connections are checked out
         try
         {
            long startWait = pool.getInternalStatistics().isEnabled() ? System.currentTimeMillis() : 0L;
            if (pool.getLock().tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
            {
               if (pool.getInternalStatistics().isEnabled())
                  pool.getInternalStatistics().deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
               try
               {
                  if (!isRunning())
                  {
                     return;
                  }

                  // We already have enough connections
                  if (isSize(size))
                  {
                     return;
                  }

                  // Create a connection to fill the pool
                  try
                  {
                     ConnectionListener cl = createConnectionEventListener(defaultSubject, defaultCri);

                     if (Tracer.isEnabled())
                        Tracer.createConnectionListener(pool.getName(), this, cl, cl.getManagedConnection(),
                                                        false, true, false,
                                                        Tracer.isRecordCallstacks() ?
                                                        new Throwable("CALLSTACK") : null);

                     boolean added = false;
                     synchronized (cls)
                     {
                        if (!isSize(size))
                        {
                           log.tracef("Filling pool cl=%s", cl);

                           cls.add(cl);
                           added = true;
                        }
                     }

                     if (!added)
                     {
                        if (Tracer.isEnabled())
                           Tracer.destroyConnectionListener(pool.getName(), this, cl, false, false, false, false,
                                                            false, true, false,
                                                            Tracer.isRecordCallstacks() ?
                                                            new Throwable("CALLSTACK") : null);
                     
                        cl.destroy();
                        return;
                     }
                  }
                  catch (ResourceException re)
                  {
                     log.unableFillPool(re, cm.getJndiName());
                     return;
                  }
               }
               finally
               {
                  pool.getLock().release();
               }
            }
         }
         catch (InterruptedException ignored)
         {
            Thread.interrupted();

            log.trace("Interrupted while requesting permit in fillTo");
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void increaseCapacity(Subject subject, ConnectionRequestInfo cri)
   {
      // We have already created one connection when this method is scheduled
      int created = 1;
      boolean create = true;

      while (create && !pool.isFull())
      {
         try
         {
            long startWait = pool.getInternalStatistics().isEnabled() ? System.currentTimeMillis() : 0L;
            if (pool.getLock().tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
            {
               if (pool.getInternalStatistics().isEnabled())
                  pool.getInternalStatistics().deltaTotalBlockingTime(System.currentTimeMillis() - startWait);
               try
               {
                  if (!isRunning())
                  {
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

                        if (Tracer.isEnabled())
                           Tracer.createConnectionListener(pool.getName(), this, cl, cl.getManagedConnection(),
                                                           false, false, true,
                                                           Tracer.isRecordCallstacks() ?
                                                           new Throwable("CALLSTACK") : null);

                        boolean added = false;
                        synchronized (cls)
                        {
                           if (!isSize(poolConfiguration.getMaxSize()))
                           {
                              log.tracef("Capacity fill: cl=%s", cl);

                              cls.add(cl);
                              created++;
                              added = true;
                           }
                        }

                        if (!added)
                        {
                           if (Tracer.isEnabled())
                              Tracer.destroyConnectionListener(pool.getName(), this, cl, false, false, true, false,
                                                               false, false, true,
                                                               Tracer.isRecordCallstacks() ?
                                                               new Throwable("CALLSTACK") : null);
                     
                           cl.destroy();
                           return;
                        }
                     }
                     catch (ResourceException re)
                     {
                        log.unableFillPool(re, cm.getJndiName());
                        return;
                     }
                  }
               }
               finally
               {
                  pool.getLock().release();
               }
            }
         }
         catch (InterruptedException ignored)
         {
            Thread.interrupted();

            log.trace("Interrupted while requesting permit in increaseCapacity");
         }
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
   private ConnectionListener createConnectionEventListener(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      long start = pool.getInternalStatistics().isEnabled() ? System.currentTimeMillis() : 0L;

      ManagedConnection mc = mcf.createManagedConnection(subject, cri);

      if (pool.getInternalStatistics().isEnabled())
      {
         pool.getInternalStatistics().deltaTotalCreationTime(System.currentTimeMillis() - start);
         pool.getInternalStatistics().deltaCreatedCount();
      }
      try
      {
         return cm.createConnectionListener(mc, this);
      }
      catch (ResourceException re)
      {
         if (pool.getInternalStatistics().isEnabled())
            pool.getInternalStatistics().deltaDestroyedCount();
         mc.destroy();
         throw re;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void connectionListenerDestroyed(ConnectionListener cl)
   {
      if (pool.getInternalStatistics().isEnabled())
         pool.getInternalStatistics().deltaDestroyedCount();
   }

   /**
    * Should any connections be removed from the pool
    * @return True if connections should be removed; otherwise false
    */
   private boolean shouldRemove()
   {
      boolean remove = true;

      if (poolConfiguration.isStrictMin() && pool instanceof PrefillPool)
      {
         // Add 1 to min-pool-size since it is strict
         remove = isSize(poolConfiguration.getMinSize() + 1);

         log.tracef("StrictMin is active. Current connection will be removed is %b", remove);
      }

      return remove;
   }

   /**
    * {@inheritDoc}
    */
   public void validateConnections() throws Exception
   {

      if (log.isTraceEnabled())
      {
         log.tracef("Attempting to  validate connections for pool %s", this);
         synchronized (cls)
         {
            String method = "validateConnections()";
            log.trace(ManagedConnectionPoolUtility.fullDetails(this, method,
                                                               mcf, cm, pool, poolConfiguration,
                                                               cls, checkedOut, pool.getInternalStatistics(),
                                                               defaultSubject, defaultCri));
         }
      }
      else if (debug)
      {
         String method = "validateConnections()";
         log.debug(ManagedConnectionPoolUtility.details(method, pool.getName(),
                                                        pool.getInternalStatistics().getInUseCount(), maxSize));
      }

      if (pool.getLock().tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
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

                     if ((candidateSet != null && candidateSet.size() > 0) || !isRunning())
                     {
                        if (cl.getState() != ConnectionState.DESTROY)
                        {
                           if (pool.getInternalStatistics().isEnabled())
                              pool.getInternalStatistics().deltaTotalPoolTime(System.currentTimeMillis() -
                                                                              cl.getLastReturnedTime());

                           if (Tracer.isEnabled())
                              Tracer.destroyConnectionListener(pool.getName(), this, cl, false, false, true,
                                                               false, false, false, false,
                                                               Tracer.isRecordCallstacks() ?
                                                               new Throwable("CALLSTACK") : null);
                     
                           cl.destroy();
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
                     if (pool.getInternalStatistics().isEnabled())
                        pool.getInternalStatistics().deltaTotalPoolTime(System.currentTimeMillis() -
                                                                        cl.getLastReturnedTime());

                     if (Tracer.isEnabled())
                        Tracer.destroyConnectionListener(pool.getName(), this, cl, false, false, false,
                                                         false, true, false, false,
                                                         Tracer.isRecordCallstacks() ?
                                                         new Throwable("CALLSTACK") : null);
                     
                     cl.destroy();
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
            pool.getLock().release();

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
   }

   /**
    * Detach connection listener
    * @return The outcome
    */
   private boolean detachConnectionListener()
   {
      synchronized (cls)
      {
         ConnectionListener cl = null;
         try
         {
            Iterator<ConnectionListener> it = checkedOut.iterator();
            while (it.hasNext())
            {
               cl = it.next();
               if (!cl.isEnlisted() && cl.getManagedConnection() instanceof DissociatableManagedConnection)
               {
                  log.tracef("Detach: %s", cl);

                  DissociatableManagedConnection dmc = (DissociatableManagedConnection)cl.getManagedConnection();
                  dmc.dissociateConnections();

                  cl.unregisterConnections();

                  if (Tracer.isEnabled())
                     Tracer.returnConnectionListener(pool.getName(), this, cl, false, pool.isInterleaving(),
                                                     Tracer.isRecordCallstacks() ?
                                                     new Throwable("CALLSTACK") : null);

                  returnConnection(cl, false, false);

                  return true;
               }
            }
         }
         catch (Throwable t)
         {
            // Ok - didn't work; nuke it and disable
            if (debug)
               log.debug("Exception during detach for: " + pool.getName(), t);

            supportsLazyAssociation = Boolean.FALSE;

            if (cl != null)
            {
               if (Tracer.isEnabled())
                  Tracer.returnConnectionListener(pool.getName(), this, cl, true, pool.isInterleaving(),
                                                  Tracer.isRecordCallstacks() ?
                                                  new Throwable("CALLSTACK") : null);

               returnConnection(cl, true, true);
            }
         }
      }

      return false;
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
