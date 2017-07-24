/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.management.ManagedEnlistmentTrace;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.spi.graceful.GracefulCallback;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.jboss.logging.Messages;

/**
 * AbstractConnectionManager.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractConnectionManager implements ConnectionManager
{
   /** Empty graceful shutdown call back */
   private static final GracefulCallback EMPTY_CALL_BACK = new GracefulCallback()
   {
      @Override public void cancel()
      {
         // do nothing
      }

      @Override public void done()
      {
         // do nothing
      }
   };


   /** Log instance */
   private final CoreLogger log;

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** The pool */
   private Pool pool;

   /** Security domain */
   private String securityDomain;

   /** SubjectFactory */
   private SubjectFactory subjectFactory;

   /** The flush strategy */
   private FlushStrategy flushStrategy;

   /** Number of retry to allocate connection */
   private int allocationRetry;

   /** Interval between retries */
   private long allocationRetryWaitMillis;

   /** Startup/ShutDown flag */
   private final AtomicBoolean shutdown = new AtomicBoolean(false);

   /** Scheduled executor for graceful shutdown */
   private ScheduledExecutorService scheduledExecutorService;

   /** Graceful job */
   private ScheduledFuture scheduledGraceful;

   /** Graceful call back. A non-null value indicates shutdown is prepared but has not started. */
   private volatile GracefulCallback gracefulCallback;

   /** Cached connection manager */
   private CachedConnectionManager cachedConnectionManager;

   /** Jndi name */
   private String jndiName;

   /** Sharable */
   private boolean sharable;

   /** Enlistment */
   protected boolean enlistment;

   /** Connectable */
   protected boolean connectable;

   /** Tracking */
   protected Boolean tracking;

   /** Enlistment trace */
   protected ManagedEnlistmentTrace enlistmentTrace;

   /**
    * Creates a new instance of connection manager.
    */
   protected AbstractConnectionManager()
   {
      this.log = getLogger();
      this.scheduledExecutorService = null;
      this.scheduledGraceful = null;
      this.gracefulCallback = null;
   }

   /**
    * Get the logger.
    * @return The value
    */
   protected abstract CoreLogger getLogger();

   /**
    * Set the pool.
    * @param pool the pool
    */
   public void setPool(Pool pool)
   {
      this.pool = pool;
   }

   /**
    * Get the pool.
    * @return the pool
    */
   public Pool getPool()
   {
      return pool;
   }

   /**
    * Sets cached connection manager.
    * @param cachedConnectionManager cached connection manager
    */
   public void setCachedConnectionManager(CachedConnectionManager cachedConnectionManager)
   {
      this.cachedConnectionManager = cachedConnectionManager;
   }

   /**
    * Gets cached connection manager.
    * @return cached connection manager
    */
   public CachedConnectionManager getCachedConnectionManager()
   {
      return cachedConnectionManager;
   }

   /**
    * {@inheritDoc}
    */
   public boolean cancelShutdown()
   {
      if (scheduledGraceful != null)
      {
         boolean result = scheduledGraceful.cancel(false);

         if (result)
         {
            shutdown.set(false);

            if (gracefulCallback != null)
               gracefulCallback.cancel();

            if (pool != null)
               pool.cancelShutdown();

            scheduledGraceful = null;
            gracefulCallback = null;
         }
         else
         {
            return false;
         }
      }
      else if (shutdown.get())
      {
         shutdown.set(false);

         if (gracefulCallback != null)
            gracefulCallback.cancel();

         if (pool != null)
            pool.cancelShutdown();

         gracefulCallback = null;
      }
      else
      {
         return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown()
   {
      prepareShutdown(0, null);
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown(GracefulCallback cb)
   {
      prepareShutdown(0, cb);
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown(int seconds)
   {
      prepareShutdown(seconds, null);
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown(int seconds, GracefulCallback cb)
   {
      shutdown.set(true);

      // use gracefulCallback as an indicator that shutdown is prepared and not started, for that reason this field
      // will never be null after preparation and before shutdown (this prevents adding an extra field,
      // shutdownPrepared, or having to replace shutdown by a state field)
      if (gracefulCallback == null) 
         gracefulCallback = cb == null ? EMPTY_CALL_BACK : cb;

      if (pool != null)
         pool.prepareShutdown();

      if (seconds > 0)
      {
         if (scheduledGraceful == null)
         {
            if (scheduledExecutorService == null)
               scheduledExecutorService = Executors.newScheduledThreadPool(1);

            scheduledGraceful =
               scheduledExecutorService.schedule(new ConnectionManagerShutdown(this), seconds, TimeUnit.SECONDS);
         }
      }
      else
      {
         if (pool != null && pool.isIdle())
         {
            synchronized (this)
            {
               // skip shutdown if there is another thread already taking care of it
               if (gracefulCallback == null)
                  return;
            }
            shutdown();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void shutdown()
   {
      getLogger().debugf("%s: shutdown", jndiName);
      final GracefulCallback gracefulCallback;
      final ScheduledExecutorService scheduledExecutorService;
      synchronized (this)
      {
         shutdown.set(true);
         gracefulCallback = this.gracefulCallback;
         this.gracefulCallback = null;
         scheduledExecutorService = this.scheduledExecutorService;
         this.scheduledExecutorService = null;
      }

      if (pool != null)
         pool.shutdown();

      if (scheduledExecutorService != null)
      {
         if (scheduledGraceful != null && !scheduledGraceful.isDone())
            scheduledGraceful.cancel(true);

         scheduledGraceful = null;
         scheduledExecutorService.shutdownNow();
      }

      if (gracefulCallback != null)
      {
         gracefulCallback.done();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isShutdown()
   {
      return shutdown.get();
   }

   /**
    * {@inheritDoc}
    */
   public int getDelay()
   {
      if (scheduledGraceful != null)
         return (int)scheduledGraceful.getDelay(TimeUnit.SECONDS);

      if (shutdown.get())
         return Integer.MIN_VALUE;
      
      return Integer.MAX_VALUE;
   }

   /**
    * Gets jndi name.
    * @return jndi name
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Sets jndi name.
    * @param jndiName jndi name
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   /**
    * Is sharable
    * @return The value
    */
   public boolean isSharable()
   {
      return sharable;
   }

   /**
    * Set the sharable flag
    * @param v The value
    */
   public void setSharable(boolean v)
   {
      this.sharable = v;

      log.tracef("sharable=%s", sharable);
   }

   /**
    * Is enlistment
    * @return The value
    */
   public boolean isEnlistment()
   {
      return enlistment;
   }

   /**
    * Set the enlistment flag
    * @param v The value
    */
   public void setEnlistment(boolean v)
   {
      this.enlistment = v;

      log.tracef("enlistment=%s", enlistment);
   }

   /**
    * Is connectable
    * @return The value
    */
   public boolean isConnectable()
   {
      return connectable;
   }

   /**
    * Set the connectable flag
    * @param v The value
    */
   public void setConnectable(boolean v)
   {
      this.connectable = v;

      log.tracef("connectable=%s", connectable);
   }

   /**
    * Get tracking
    * @return The value
    */
   public Boolean getTracking()
   {
      return tracking;
   }

   /**
    * Set the tracking flag
    * @param v The value
    */
   public void setTracking(Boolean v)
   {
      this.tracking = v;

      log.tracef("tracking=%s", tracking);
   }

   /**
    * Get enlistment trace
    * @return The value
    */
   public ManagedEnlistmentTrace getEnlistmentTrace()
   {
      return enlistmentTrace;
   }

   /**
    * Set the enlistment trace flag
    * @param v The value
    */
   public void setEnlistmentTrace(ManagedEnlistmentTrace v)
   {
      this.enlistmentTrace = v;

      log.tracef("enlistment_trace=%s", enlistmentTrace);
   }

   /**
    * {@inheritDoc}
    */
   public String getSecurityDomain()
   {
      return securityDomain;
   }

   /**
    * Sets security domain
    * @param securityDomain security domain
    */
   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   /**
    * {@inheritDoc}
    */
   public SubjectFactory getSubjectFactory()
   {
      return subjectFactory;
   }

   /**
    * Sets subject factory.
    * @param subjectFactory subject factory
    */
   public void setSubjectFactory(SubjectFactory subjectFactory)
   {
      this.subjectFactory = subjectFactory;
   }

   /**
    * Get the flush strategy
    * @return The value
    */
   public FlushStrategy getFlushStrategy()
   {
      return flushStrategy;
   }

   /**
    * Set the flush strategy
    * @param v The value
    */
   public void setFlushStrategy(FlushStrategy v)
   {
      this.flushStrategy = v;
   }

   /**
    * Gets managed connection factory.
    * @return managed connection factory
    */
   public javax.resource.spi.ManagedConnectionFactory getManagedConnectionFactory()
   {
      if (pool == null)
      {
         log.tracef("No pooling strategy found! for connection manager : %s", this);
         return null;
      }
      else
      {
         return pool.getManagedConnectionFactory();
      }

   }

   /**
    * Set the number of allocation retries
    * @param number retry number
    */
   public void setAllocationRetry(int number)
   {
      if (number >= 0)
         allocationRetry = number;
   }

   /**
    * Get the number of allocation retries
    * @return The number of retries
    */
   public int getAllocationRetry()
   {
      return allocationRetry;
   }

   /**
    * Set the wait time between each allocation retry
    * @param millis wait in ms
    */
   public void setAllocationRetryWaitMillis(long millis)
   {
      if (millis > 0)
         allocationRetryWaitMillis = millis;
   }

   /**
    * Get the wait time between each allocation retry
    * @return The millis
    */
   public long getAllocationRetryWaitMillis()
   {
      return allocationRetryWaitMillis;
   }

   /**
    * Public for use in testing pooling functionality by itself.
    * called by both allocateConnection and reconnect.
    *
    * @param subject a <code>Subject</code> value
    * @param cri a <code>ConnectionRequestInfo</code> value
    * @return a <code>ManagedConnection</code> value
    * @exception ResourceException if an error occurs
    */
   public ConnectionListener getManagedConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      return getManagedConnection(null, subject, cri);
   }

   /**
    * Get the managed connection from the pool.
    *
    * @param transaction the transaction for track by transaction
    * @param subject the subject
    * @param cri the ConnectionRequestInfo
    * @return a managed connection
    * @exception ResourceException if an error occurs
    */
   protected ConnectionListener getManagedConnection(Transaction transaction, Subject subject,
         ConnectionRequestInfo cri) throws ResourceException
   {
      Exception failure = null;

      if (shutdown.get())
      {
         throw new ResourceException(bundle.connectionManagerIsShutdown(jndiName));
      }

      // First attempt
      boolean isInterrupted = Thread.interrupted();
      boolean innerIsInterrupted = false;
      try
      {
         return pool.getConnection(transaction, subject, cri);
      }
      catch (ResourceException e)
      {
         failure = e;

         // Retry?
         if (allocationRetry != 0 || e instanceof RetryableException)
         {
            int to = allocationRetry;

            if (allocationRetry == 0 && e instanceof RetryableException)
               to = 1;

            for (int i = 0; i < to; i++)
            {
               if (shutdown.get())
               {
                  throw new ResourceException(bundle.connectionManagerIsShutdown(jndiName));
               }

               log.tracef("%s: Attempting allocation retry (%s, %s, %s)", jndiName, transaction, subject, cri);

               if (Thread.currentThread().isInterrupted())
               {
                  Thread.interrupted();
                  innerIsInterrupted = true;
               }

               try
               {
                  if (allocationRetryWaitMillis != 0)
                  {
                     Thread.sleep(allocationRetryWaitMillis);
                  }

                  return pool.getConnection(transaction, subject, cri);
               }
               catch (ResourceException re)
               {
                  failure = re;
               }
               catch (InterruptedException ie)
               {
                  failure = ie;
                  innerIsInterrupted = true;
               }
            }
         }
      }
      catch (Exception e)
      {
         failure = e;
      }
      finally
      {
         if (isInterrupted || innerIsInterrupted)
         {
            Thread.currentThread().interrupt();
      
            if (innerIsInterrupted)
               throw new ResourceException(bundle.getManagedConnectionRetryWaitInterrupted(jndiName), failure);
         }
      }

      // If we get here all retries failed, throw the lastest failure
      throw new ResourceException(bundle.unableGetManagedConnection(jndiName), failure);
   }

   /**
    * Kill given connection listener wrapped connection instance.
    * @param bcl connection listener that wraps connection
    * @param kill kill connection or not
    */
   public void returnManagedConnection(org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener bcl,
                                       boolean kill)
   {
      // Hack - We know that we can type cast it
      ConnectionListener cl = (ConnectionListener)bcl;

      Pool localStrategy = cl.getPool();
      if (localStrategy != pool || shutdown.get())
      {
         kill = true;
      }

      try
      {
         if (!kill && cl.getState().equals(ConnectionState.NORMAL))
         {
            cl.tidyup();
         }
      }
      catch (Throwable t)
      {
         log.errorDuringTidyUpConnection(cl, t);
         kill = true;
      }

      try
      {
         localStrategy.returnConnection(cl, kill);
      }
      catch (ResourceException re)
      {
         // We can receive notification of an error on the connection
         // before it has been assigned to the pool. Reduce the noise for
         // these errors
         if (kill)
         {
            log.debug("ResourceException killing connection", re);
         }
         else
         {
            log.resourceExceptionReturningConnection(cl.getManagedConnection(), re);
         }
      }
      catch (Exception e)
      {
         try
         {
            // Something is very wrong, so lets set the state up-front
            if (cl.getState().equals(ConnectionState.NORMAL))
               cl.setState(ConnectionState.DESTROY);

            localStrategy.returnConnection(cl, true);
         }
         catch (Throwable t)
         {
            log.throwableReturningConnection(cl.getManagedConnection(), t);
         }
      }

      if (shutdown.get() && pool.isIdle())
      {
         synchronized (this)
         {
            // skip shutdown if there is another thread already taking care of it
            if (gracefulCallback == null)
               return;
         }
         shutdown();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri) throws ResourceException
   {
      //Check for pooling!
      if (pool == null || shutdown.get())
      {
         throw new ResourceException(bundle.tryingUseConnectionFactoryShutDown(jndiName));
      }

      //it is an explicit spec requirement that equals be used for matching rather than ==.
      if (!pool.getManagedConnectionFactory().equals(mcf))
      {
         throw new ResourceException(
            bundle.wrongManagedConnectionFactorySentToAllocateConnection(pool.getManagedConnectionFactory(), mcf));
      }

      // Pick a managed connection from the pool
      Subject subject = getSubject();
      ConnectionListener cl = getManagedConnection(subject, cri);

      // Tell each connection manager the managed connection is active
      reconnectManagedConnection(cl);

      // Ask the managed connection for a connection
      Object connection = null;
      try
      {
         connection = cl.getManagedConnection().getConnection(subject, cri);
      }
      catch (Throwable t)
      {
         try
         {
            managedConnectionDisconnected(cl);
         }
         catch (ResourceException re)
         {
            log.tracef("Get exception from managedConnectionDisconnected, maybe delist() have problem %s", re);
            returnManagedConnection(cl, true);
         }
         throw new ResourceException(bundle.uncheckedThrowableInManagedConnectionGetConnection(cl), t);
      }

      // Associate managed connection with the connection
      registerAssociation(cl, connection);

      if (cachedConnectionManager != null)
      {
         cachedConnectionManager.registerConnection(this, cl, connection);
      }

      return connection;
   }

   /**
    * {@inheritDoc}
    */
   public void associateConnection(Object connection, ManagedConnectionFactory mcf, ConnectionRequestInfo cri)
      throws ResourceException
   {
      associateManagedConnection(connection, mcf, cri);
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection associateManagedConnection(Object connection, ManagedConnectionFactory mcf,
                                                       ConnectionRequestInfo cri)
      throws ResourceException
   {
      // Check for pooling!
      if (pool == null || shutdown.get())
      {
         throw new ResourceException(bundle.tryingUseConnectionFactoryShutDown(jndiName));
      }

      // It is an explicit spec requirement that equals be used for matching rather than ==.
      if (!pool.getManagedConnectionFactory().equals(mcf))
      {
         throw new ResourceException(
            bundle.wrongManagedConnectionFactorySentToAllocateConnection(pool.getManagedConnectionFactory(), mcf));
      }

      if (connection == null)
         throw new ResourceException(bundle.connectionIsNull());

      // Pick a managed connection from the pool
      Subject subject = getSubject();
      ConnectionListener cl = getManagedConnection(subject, cri);

      // Tell each connection manager the managed connection is active
      reconnectManagedConnection(cl);

      // Associate managed connection with the connection
      cl.getManagedConnection().associateConnection(connection);
      registerAssociation(cl, connection);

      if (cachedConnectionManager != null)
      {
         cachedConnectionManager.registerConnection(this, cl, connection);
      }

      return cl.getManagedConnection();
   }
 
   /**
    * {@inheritDoc}
    */
   public boolean dissociateManagedConnection(Object connection, ManagedConnection mc, ManagedConnectionFactory mcf)
      throws ResourceException
   {
      if (connection == null || mc == null || mcf == null)
         throw new ResourceException(bundle.unableToFindConnectionListener());

      ConnectionListener cl = getPool().findConnectionListener(mc, connection);

      if (cl != null)
      {
         log.tracef("DissociateManagedConnection: cl=%s, connection=%s", cl, connection);

         if (getCachedConnectionManager() != null)
         {
            try
            {
               getCachedConnectionManager().unregisterConnection(this, cl, connection);
            }
            catch (Throwable t)
            {
               log.debug("Throwable from unregisterConnection", t);
            }
         }

         unregisterAssociation(cl, connection);

         if (cl.getNumberOfConnections() == 0)
         {
            log.tracef("DissociateManagedConnection: Returning cl=%s", cl);

            cl.dissociate();

            log.tracef("DissociateManagedConnection: isManagedConnectionFree=%s", cl.isManagedConnectionFree());

            if (cl.isManagedConnectionFree())
            {
               // TODO - clean up TSR

               returnManagedConnection(cl, false);

               return true;
            }
         }
      }
      else
      {
         throw new ResourceException(bundle.unableToFindConnectionListener());
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public void inactiveConnectionClosed(Object connection, ManagedConnectionFactory mcf)
   {
      // We don't track inactive connections
   }

   /**
    * Unregister association.
    * @param cl connection listener
    * @param c connection
    */
   //does NOT put the mc back in the pool if no more handles. Doing so would introduce a race condition
   //whereby the mc got back in the pool while still enlisted in the tx.
   //The mc could be checked out again and used before the delist occured.
   public void unregisterAssociation(ConnectionListener cl, Object c)
   {
      cl.unregisterConnection(c);
   }

   /**
    * {@inheritDoc}
    */
   public void lazyEnlist(ManagedConnection mc) throws ResourceException
   {
      // Nothing by default
   }

   /**
    * Invoked to reassociate a managed connection.
    *
    * @param cl the managed connection
    * @throws ResourceException for exception
    */
   protected void reconnectManagedConnection(ConnectionListener cl) throws ResourceException
   {
      try
      {
         managedConnectionReconnected(cl);
      }
      catch (Throwable t)
      {
         disconnectManagedConnection(cl);
         throw new ResourceException(bundle.uncheckedThrowableInManagedConnectionReconnected(cl), t);
      }
   }

   /**
    * Invoked when a managed connection is no longer associated
    *
    * @param cl the managed connection
    */
   protected void disconnectManagedConnection(ConnectionListener cl)
   {
      try
      {
         managedConnectionDisconnected(cl);
      }
      catch (Throwable t)
      {
         log.uncheckedThrowableInManagedConnectionDisconnected(cl, t);
      }
   }

   /**
    * For polymorphism.
    * <p>
    *
    * Do not invoke directly, use reconnectManagedConnection
    * which does the relevent exception handling
    * @param cl connection listener
    * @throws ResourceException for exception
    */
   protected void managedConnectionReconnected(ConnectionListener cl) throws ResourceException
   {
      //Nothing as default
   }

   /**
    * For polymorphism.
    * <p>
    *
    * Do not invoke directly, use disconnectManagedConnection
    * which does the relevent exception handling
    * @param cl connection listener
    * @throws ResourceException for exception
    */
   protected void managedConnectionDisconnected(ConnectionListener cl) throws ResourceException
   {
      //Nothing as default
   }

   /**
    * Register connection with connection listener.
    * @param cl connection listener
    * @param c connection
    * @throws ResourceException exception
    */
   private void registerAssociation(ConnectionListener cl, Object c) throws ResourceException
   {
      cl.registerConnection(c);
   }

   /**
    * {@inheritDoc}
    */
   public abstract void transactionStarted(Collection<ConnectionRecord> conns) throws SystemException;

   /**
    * {@inheritDoc}
    */
   public abstract boolean isTransactional();

   /**
    * {@inheritDoc}
    */
   public abstract TransactionIntegration getTransactionIntegration();

   /**
    * Get a subject
    * @return The subject
    */
   private Subject getSubject()
   {
      Subject subject = null;

      if (subjectFactory != null && securityDomain != null)
      {
         subject = SecurityActions.createSubject(subjectFactory, securityDomain);

         Set<PasswordCredential> credentials = SecurityActions.getPasswordCredentials(subject);
         if (credentials != null && credentials.size() > 0)
         {
            ManagedConnectionFactory pcMcf = getManagedConnectionFactory();
            for (PasswordCredential pc : credentials)
            {
               pc.setManagedConnectionFactory(pcMcf);
            }
         }
      }

      log.tracef("Subject: %s", subject);

      return subject;
   }
}
