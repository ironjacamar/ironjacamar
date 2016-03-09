/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.connectionmanager;

import org.ironjacamar.core.api.connectionmanager.ConnectionManagerConfiguration;
import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.connectionmanager.pool.Pool;
import org.ironjacamar.core.spi.security.SubjectFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableException;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.logging.Logger;

/**
 * The base class for all connection manager implementations
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractConnectionManager implements ConnectionManager
{
   /** The logger */
   private static Logger log = Logger.getLogger(AbstractConnectionManager.class);

   /**
    * Startup/ShutDown flag
    */
   protected final AtomicBoolean shutdown = new AtomicBoolean(false);

   /**
    * The managed connection factory
    */
   protected final ManagedConnectionFactory mcf;

   /**
    * The pool
    */
   protected Pool pool;

   /** The cached connection manager */
   protected CachedConnectionManager ccm;
   
   /** The configuration */
   protected ConnectionManagerConfiguration cmConfiguration;

   /**
    * the subject factory
    */
   protected SubjectFactory subjectFactory;

   /** Supports lazy association */
   private Boolean supportsLazyAssociation;
   
   /**
    * Constructor
    *
    * @param mcf The managed connection factory
    * @param ccm The cached connection manager
    * @param cmc The connection manager configuration
    */
   public AbstractConnectionManager(ManagedConnectionFactory mcf,
                                    CachedConnectionManager ccm,
                                    ConnectionManagerConfiguration cmc)
   {
      this.mcf = mcf;
      this.ccm = ccm;
      this.cmConfiguration = cmc;
      this.pool = null;
      this.subjectFactory = null;
      this.supportsLazyAssociation = null;
   }

   /**
    * {@inheritDoc}
    */
   public void setPool(Pool pool)
   {
      this.pool = pool;
   }

   /**
    * {@inheritDoc}
    */
   public Pool getPool()
   {
      return pool;
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionFactory getManagedConnectionFactory()
   {
      return mcf;
   }

   /**
    * {@inheritDoc}
    */
   public CachedConnectionManager getCachedConnectionManager()
   {
      return ccm;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionManagerConfiguration getConnectionManagerConfiguration()
   {
      return cmConfiguration;
   }

   /**
    * {@inheritDoc}
    */
   public void setSubjectFactory(SubjectFactory subjectFactory)
   {
      this.subjectFactory = subjectFactory;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SubjectFactory getSubjectFactory()
   {
      return this.subjectFactory;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void shutdown()
   {
      shutdown.set(true);

      if (pool != null)
         pool.shutdown();
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
   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri) throws ResourceException
   {
      if (shutdown.get())
         throw new ResourceException();
      Credential credential;
      if (subjectFactory == null || cmConfiguration.getSecurityDomain() == null)
      {
         credential = new Credential(null, cri);
      }
      else
      {
         credential = new Credential(SecurityActions.createSubject(subjectFactory,
                                                                   cmConfiguration.getSecurityDomain(),
                                                                   mcf),
                                     cri);
      }
      org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl = getConnectionListener(credential);
      Object connection = cl.getConnection();

      if (ccm != null)
         ccm.registerConnection(this, cl, connection);

      return connection;
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnectionListener(org.ironjacamar.core.api.connectionmanager.listener.ConnectionListener cl,
         boolean kill)
   {
      try
      {
         pool.returnConnectionListener((org.ironjacamar.core.connectionmanager.listener.ConnectionListener) cl, kill);
      }
      catch (Exception e)
      {
         //
      }
   }

   /**
    * Get a connection listener
    *
    * @param credential The credential
    * @return The listener
    * @throws ResourceException Thrown in case of an error
    */
   protected org.ironjacamar.core.connectionmanager.listener.ConnectionListener getConnectionListener(
         Credential credential) throws ResourceException
   {
      org.ironjacamar.core.connectionmanager.listener.ConnectionListener result = null;
      Exception failure = null;

      // First attempt
      boolean isInterrupted = Thread.interrupted();
      boolean innerIsInterrupted = false;
      try
      {
         result = pool.getConnectionListener(credential);

         if (supportsLazyAssociation == null)
         {
            supportsLazyAssociation =
               (result.getManagedConnection() instanceof DissociatableManagedConnection) ? Boolean.TRUE : Boolean.FALSE;
         }

         return result;
      }
      catch (ResourceException e)
      {
         failure = e;

         // Retry?
         if (cmConfiguration.getAllocationRetry() != 0 || e instanceof RetryableException)
         {
            int to = cmConfiguration.getAllocationRetry();
            long sleep = cmConfiguration.getAllocationRetryWaitMillis();

            if (to == 0 && e instanceof RetryableException)
               to = 1;

            for (int i = 0; i < to; i++)
            {
               if (shutdown.get())
               {
                  throw new ResourceException();
               }

               if (Thread.currentThread().isInterrupted())
               {
                  Thread.interrupted();
                  innerIsInterrupted = true;
               }

               try
               {
                  if (sleep > 0)
                     Thread.sleep(sleep);

                  return pool.getConnectionListener(credential);
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
               throw new ResourceException(failure);
         }
      }

      if (cmConfiguration.isSharable() && Boolean.TRUE.equals(supportsLazyAssociation))
         return associateConnectionListener(credential, null);

      // If we get here all retries failed, throw the lastest failure
      throw new ResourceException(failure);
   }

   /**
    * {@inheritDoc}
    */
   public void lazyEnlist(ManagedConnection mc) throws ResourceException
   {
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
   public void inactiveConnectionClosed(Object connection, ManagedConnectionFactory mcf)
   {
      // Foo-bar concept
   }
   
   /**
    * {@inheritDoc}
    */
   public ManagedConnection associateManagedConnection(Object connection, ManagedConnectionFactory mcf,
                                                       ConnectionRequestInfo cri)
      throws ResourceException
   {
      log.tracef("associateManagedConnection(%s, %s, %s)", connection, mcf, cri);
      
      if (!this.mcf.equals(mcf))
      {
         throw new ResourceException();
      }

      if (connection == null)
         throw new ResourceException();

      Credential credential = null;
      if (getSubjectFactory() == null || cmConfiguration.getSecurityDomain() == null)
      {
         credential = new Credential(null, cri);
      }
      else
      {
         credential = new Credential(SecurityActions.createSubject(subjectFactory,
                                                                   cmConfiguration.getSecurityDomain(),
                                                                   mcf),
                                     cri);
      }

      return associateConnectionListener(credential, connection).getManagedConnection();
   }

   /**
    * Associate a ConnectionListener
    * @param credential The credential
    * @param connection The connection handle (optional)
    * @return The connection listener instance
    * @exception ResourceException Thrown in case of an error
    */
   private org.ironjacamar.core.connectionmanager.listener.ConnectionListener
      associateConnectionListener(Credential credential, Object connection)
      throws ResourceException
   {
      log.tracef("associateConnectionListener(%s, %s)", credential, connection);
      
      if (isShutdown())
      {
         throw new ResourceException();
      }

      org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl =
         pool.getActiveConnectionListener(credential);

      if (cl == null)
      {
         try
         {
            cl = pool.getConnectionListener(credential);
         }
         catch (ResourceException re)
         {
            org.ironjacamar.core.connectionmanager.listener.ConnectionListener removeCl =
               pool.removeConnectionListener(null);

            if (removeCl != null)
            {
               try
               {
                  returnConnectionListener(removeCl, true);
                  cl = pool.getConnectionListener(credential);
               }
               catch (ResourceException ire)
               {
                  // Nothing we can do
               }
            }
            else
            {
               if (getTransactionSupport() == TransactionSupportLevel.NoTransaction)
               {
                  org.ironjacamar.core.connectionmanager.listener.ConnectionListener targetCl =
                     pool.removeConnectionListener(credential);

                  if (targetCl != null)
                  {
                     if (targetCl.getManagedConnection() instanceof DissociatableManagedConnection)
                     {
                        DissociatableManagedConnection dmc =
                           (DissociatableManagedConnection)targetCl.getManagedConnection();

                        dmc.dissociateConnections();

                        if (ccm != null)
                        {
                           for (Object c : targetCl.getConnections())
                           {
                              ccm.unregisterConnection(this, targetCl, connection);
                           }
                        }

                        cl = targetCl;
                     }
                  }
               }
            }

            if (cl == null)
               throw new ResourceException();
         }
      }

      if (connection != null)
      {
         // Associate managed connection with the connection
         cl.getManagedConnection().associateConnection(connection);
         cl.addConnection(connection);

         if (ccm != null)
         {
            ccm.registerConnection(this, cl, connection);
         }
      }

      return cl;
   }

   /**
    * {@inheritDoc}
    */
   public boolean dissociateManagedConnection(Object connection, ManagedConnection mc, ManagedConnectionFactory mcf)
      throws ResourceException
   {
      log.tracef("dissociateManagedConnection(%s, %s, %s)", connection, mc, mcf);
      
      if (connection == null || mc == null || mcf == null)
         throw new ResourceException();

      org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl =
         pool.findConnectionListener(mc, connection);

      if (cl != null)
      {
         if (ccm != null)
         {
            ccm.unregisterConnection(this, cl, connection);
         }

         cl.removeConnection(connection);

         if (cl.getConnections().size() == 0)
         {
            pool.delist(cl);
            returnConnectionListener(cl, false);

            return true;
         }
      }
      else
      {
         throw new ResourceException();
      }

      return false;
   }
}
