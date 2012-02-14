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

package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.jboss.logging.Messages;

import org.jboss.security.SubjectFactory;

/**
 * AbstractConnectionManager.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractConnectionManager implements ConnectionManager
{
   /** Log instance */
   private final CoreLogger log;

   /** Log trace */
   protected boolean trace;
   
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

   /** Cached connection manager */
   private CachedConnectionManager cachedConnectionManager;

   /** Jndi name */
   private String jndiName;

   /** Sharable */
   private boolean sharable;

   /**
    * Creates a new instance of connection manager.
    */
   protected AbstractConnectionManager()
   {
      this.log = getLogger();
      this.trace = log.isTraceEnabled();
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
    * Shutdown
    */
   public void shutdown()
   {
      getLogger().debug(jndiName + ": shutdown");
      shutdown.set(true);

      if (pool != null)
         pool.shutdown();
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
         if (trace)
         {
            log.trace("No pooling strategy found! for connection manager : " + this);
            return null;
         }
      }
      else
      {
         return pool.getManagedConnectionFactory();
      }

      return null;
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
         if (allocationRetry != 0)
         {
            for (int i = 0; i < allocationRetry; i++)
            {
               if (shutdown.get())
               {
                  throw new ResourceException(bundle.connectionManagerIsShutdown(jndiName));
               }

               if (trace)
               {
                  log.trace("Attempting allocation retry for cri=" + cri);
               }


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
      if (localStrategy != pool)
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
            log.debug("resourceException killing connection (error retrieving from pool?)", re);
         }
         else
         {
            log.resourceExceptionReturningConnection(cl.getManagedConnection(), re);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri) throws ResourceException
   {
      //Check for pooling!
      if (pool == null)
      {
         throw new ResourceException(bundle.tryingUseConnectionFactoryShutDown());
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
            if (trace)
               log.trace("Get exception from managedConnectionDisconnected, maybe delist() have problem" + re);
            returnManagedConnection(cl, true);
         }
         throw new ResourceException(bundle.uncheckedThrowableInManagedConnectionGetConnection(cl), t);
      }

      // Associate managed connection with the connection
      registerAssociation(cl, connection);

      if (cachedConnectionManager != null)
      {
         cachedConnectionManager.registerConnection(this, cl, connection, cri);
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
      if (pool == null)
      {
         throw new ResourceException(bundle.tryingUseConnectionFactoryShutDown());
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
         cachedConnectionManager.registerConnection(this, cl, connection, cri);
      }

      return cl.getManagedConnection();
   }
 
   /**
    * {@inheritDoc}
    */
   public void inactiveConnectionClosed(Object connection, ManagedConnectionFactory mcf)
   {
      // We don't track inactive connections
   }

   /**
    * {@inheritDoc}
    */
   public void disconnect(Collection<ConnectionRecord> conRecords, Set<String> unsharableResources)
      throws ResourceException
   {
      // if we have an unshareable connection do not remove the association
      // nothing to do
      if (unsharableResources.contains(jndiName))
      {
         if (trace)
            log.trace("disconnect for unshareable connection: nothing to do");
         return;
      }

      Set<ConnectionListener> cls = new HashSet<ConnectionListener>(conRecords.size());
      for (Iterator<ConnectionRecord> i = conRecords.iterator(); i.hasNext();)
      {
         ConnectionRecord cr = i.next();
         ConnectionListener cl = cr.getConnectionListener();
         cr.setConnectionListener(null);
         unregisterAssociation(cl, cr.getConnection());
         if (!cls.contains(cl))
         {
            cls.add(cl);
         }
      }
      for (Iterator<ConnectionListener> i = cls.iterator(); i.hasNext();)
      {
         disconnectManagedConnection(i.next());
      }
   }

   /**
    * {@inheritDoc}
    */
   public void reconnect(Collection<ConnectionRecord> conns, Set<String> unsharableResources) throws ResourceException
   {
      // if we have an unshareable connection the association was not removed
      // nothing to do
      if (unsharableResources.contains(jndiName))
      {
         if (trace)
            log.trace("reconnect for unshareable connection: nothing to do");
         return;
      }

      Map<ConnectionRequestInfo, ConnectionListener> criToCLMap =
         new HashMap<ConnectionRequestInfo, ConnectionListener>(conns.size());

      for (Iterator<ConnectionRecord> i = conns.iterator(); i.hasNext();)
      {
         ConnectionRecord cr = i.next();
         if (cr.getConnectionListener() != null)
         {
            //This might well be an error.
            log.reconnectingConnectionHandleHasManagedConnection(
               cr.getConnectionListener().getManagedConnection(),
               cr.getConnection());
         }
         ConnectionListener cl = criToCLMap.get(cr.getCri());
         if (cl == null)
         {
            cl = getManagedConnection(getSubject(), cr.getCri());
            criToCLMap.put(cr.getCri(), cl);
            //only call once per managed connection, when we get it.
            reconnectManagedConnection(cl);
         }

         cl.getManagedConnection().associateConnection(cr.getConnection());
         registerAssociation(cl, cr.getConnection());
         cr.setConnectionListener(cl);
      }
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
    * Gets subject.
    * @return subject
    */
   private Subject getSubject()
   {
      Subject subject = null;

      if (subjectFactory != null && securityDomain != null)
      {
         subject = subjectFactory.createSubject(securityDomain);

         Set<PasswordCredential> credentials = subject.getPrivateCredentials(PasswordCredential.class);
         if (credentials != null && credentials.size() > 0)
         {
            ManagedConnectionFactory pcMcf = getManagedConnectionFactory();
            for (PasswordCredential pc : credentials)
            {
               pc.setManagedConnectionFactory(pcMcf);
            }
         }
      }

      if (trace)
         log.tracef("Subject: %s", subject);

      return subject;
   }
}
