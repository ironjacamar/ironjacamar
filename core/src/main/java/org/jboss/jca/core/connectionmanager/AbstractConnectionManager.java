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

import org.jboss.jca.common.api.JBossResourceException;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;
import org.jboss.jca.core.connectionmanager.pool.api.ManagedConnectionPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

import org.jboss.security.SubjectFactory;
import org.jboss.util.NotImplementedException;

/**
 * AbstractConnectionManager.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$
 */
public  abstract class AbstractConnectionManager implements InternalConnectionManager 

{
   /**Log instance*/
   private Logger log = Logger.getLogger(getClass());
   
   /**
    * Note that this copy has a trailing / unlike the original in
    * JaasSecurityManagerService.
    */
   private static final String SECURITY_MGR_PATH = "java:/jaas/";
   
   /**Connection manager pooling strategy*/
   private ManagedConnectionPool poolingStrategy;
   
   /**Security domain jndi name*/
   private String securityDomainJndiName;
   
   /**SubjectFactory*/
   private SubjectFactory subjectFactory;
   
   /**Log trace*/
   private boolean trace;
   
   /**Number of retry to allocate connection*/
   private int allocationRetry;

   /**Interval between retries*/
   private long allocationRetryWaitMillis;

   /**Startup/ShutDown flag*/
   private AtomicBoolean shutdown = new AtomicBoolean(false);
   
   /**Cached connection manager*/
   private CachedConnectionManager cachedConnectionManager;
   
   /**Jndi name*/
   private String jndiName;
   
   /**
    * Creates a new instance of connection manager.   
    */
   protected AbstractConnectionManager()
   {
      this.trace = log.isTraceEnabled();
   }
   
   /**
    * Gets log.
    * @return log instance
    */
   protected Logger getLog()
   {
      return log;
   }
   
   /**
    * Sets pooling strategy.
    * @param poolingStrategy pooling strategy
    */
   public void setPoolingStrategy(ManagedConnectionPool poolingStrategy)
   {
      this.poolingStrategy = poolingStrategy;
   }
   
   /**
    * Gets pooling strategy.
    * @return pooling strategy
    */
   public ManagedConnectionPool getPoolingStrategy()
   {
      return poolingStrategy;
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
    * Sets shut down flag.
    * @param shutDown shut down flag
    */
   public void setShutDown(boolean shutDown)
   {
      this.shutdown.set(shutDown);
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
    * Sets security domain jndi name.
    * @param securityDomainJndiName security jndi name
    */
   public void setSecurityDomainJndiName(String securityDomainJndiName)
   {
      if (securityDomainJndiName != null && securityDomainJndiName.startsWith(SECURITY_MGR_PATH))
      {
         securityDomainJndiName = securityDomainJndiName.substring(SECURITY_MGR_PATH.length());
         log.warn("WARNING: UPDATE YOUR SecurityDomainJndiName! REMOVE " + SECURITY_MGR_PATH);
      }
      
      this.securityDomainJndiName = securityDomainJndiName;
   }

   /**
    * Gets security domain jndi name.
    * @return security domain jndi name
    */
   public String getSecurityDomainJndiName()
   {
      return securityDomainJndiName;
   }
 
   /**
    * Gets subject factory instance.
    * @return subject factory
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
    * Gets managed connection factory.
    * @return managed connection factory
    */
   public javax.resource.spi.ManagedConnectionFactory getManagedConnectionFactory()
   {
      if (poolingStrategy == null)
      {
         if (trace)
         {
            log.trace("No pooling strategy found! for connection manager : " + this);
            return null;
         }
      }
      else
      {
         return poolingStrategy.getManagedConnectionFactory();   
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
      ResourceException failure = null;

      if (shutdown.get())
      {
         throw new ResourceException("The connection manager is shutdown " + jndiName);  
      }
      
      // First attempt
      try
      {
         return poolingStrategy.getConnection(transaction, subject, cri);
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
                  throw new ResourceException("The connection manager is shutdown " + jndiName);  
               }

               if (trace)
               {
                  log.trace("Attempting allocation retry for cri=" + cri);  
               }

               try
               {
                  if (allocationRetryWaitMillis != 0)
                  {
                     Thread.sleep(allocationRetryWaitMillis);  
                  }

                  return poolingStrategy.getConnection(transaction, subject, cri);
               }
               catch (ResourceException re)
               {
                  failure = re;
               }
               catch (InterruptedException ie)
               {
                  JBossResourceException.rethrowAsResourceException("getManagedConnection retry wait was interrupted " +
                        jndiName, ie);
               }
            }
         }
      }

      // If we get here all retries failed, throw the lastest failure
      throw new ResourceException("Unable to get managed connection for " + jndiName, failure);
   }
   
   /**
    * Kill given connection listener wrapped connection instance.
    * @param cl connection listener that wraps connection
    * @param kill kill connection or not
    */
   public void returnManagedConnection(ConnectionListener cl, boolean kill)
   {
      ManagedConnectionPool localStrategy = cl.getManagedConnectionPool();
      if (localStrategy != poolingStrategy)
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
         log.warn("Error during tidy up connection" + cl, t);
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
            log.warn("resourceException returning connection: " + cl.getManagedConnection(), re);  
         }
      }
   }
   
   
   /**
    * {@inheritDoc}
    */
   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri) throws ResourceException
   {
      //Check for pooling!
      if (poolingStrategy == null)
      {
         throw new ResourceException("You are trying to use a connection factory that has been shut down: " +
               "ManagedConnectionFactory is null.");         
      }

      //it is an explicit spec requirement that equals be used for matching rather than ==.
      if (!poolingStrategy.getManagedConnectionFactory().equals(mcf))
      {
         throw new ResourceException("Wrong ManagedConnectionFactory sent to allocateConnection!");  
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
            log.trace("Get exception from managedConnectionDisconnected, maybe delist() have problem" + re);            
            returnManagedConnection(cl, true);
         }
         JBossResourceException.rethrowAsResourceException(
               "Unchecked throwable in ManagedConnection.getConnection() cl=" + cl, t);
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
   public TransactionManager getTransactionManager()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isTransactional()
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public long getTimeLeftBeforeTransactionTimeout(boolean arg0) throws RollbackException
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws SystemException
   {
      throw new NotImplementedException("NYI: getTransactionTimeout()");
   }

   /**
    * {@inheritDoc}
    */
   public void checkTransactionActive() throws RollbackException, SystemException
   {
      //Do Nothing as default
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
         log.trace("disconnect for unshareable connection: nothing to do");
         
         return;
      }

      Set<ConnectionListener> cls = new HashSet<ConnectionListener>();
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
         log.trace("reconnect for unshareable connection: nothing to do");
         return;
      }

      Map<ConnectionRequestInfo, ConnectionListener> criToCLMap = 
            new HashMap<ConnectionRequestInfo, ConnectionListener>();
      
      for (Iterator<ConnectionRecord> i = conns.iterator(); i.hasNext();)
      {
         ConnectionRecord cr = i.next();
         if (cr.getConnectionListener() != null)
         {
            //This might well be an error.
            log.warn("reconnecting a connection handle that still has a managedConnection! "
                  + cr.getConnectionListener().getManagedConnection() + " " + cr.getConnection());
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
      
      criToCLMap.clear();
      
   }

   /**
    * {@inheritDoc}
    */
   public void transactionStarted(Collection<ConnectionRecord> conns) throws SystemException
   {
      //reimplement in subclasses      
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
         JBossResourceException.rethrowAsResourceException("Unchecked throwable in managedConnectionReconnected() cl="
               + cl, t);
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
         log.warn("Unchecked throwable in managedConnectionDisconnected() cl=" + cl, t);
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
    * Gets subject.
    * @return subject
    */
   private Subject getSubject()
   {
      Subject subject = null;
      
      if (subjectFactory != null && securityDomainJndiName != null)
      {
         subject = subjectFactory.createSubject(securityDomainJndiName);
      } 
      
      if (trace)
      {
         log.trace("subject: " + subject);  
      }
      
      return subject;
   }


}
