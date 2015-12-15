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

import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.connectionmanager.pool.Pool;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * The base class for all connection manager implementations
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractConnectionManager implements ConnectionManager
{
   /** Startup/ShutDown flag */
   protected final AtomicBoolean shutdown = new AtomicBoolean(false);

   /** The managed connection factory */
   protected final ManagedConnectionFactory mcf;

   /** The pool */
   protected Pool pool;

   /** The cached connection manager */
   protected CachedConnectionManager ccm;
   
   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param ccm The cached connection manager
    */
   public AbstractConnectionManager(ManagedConnectionFactory mcf,
                                    CachedConnectionManager ccm)
   {
      this.mcf = mcf;
      this.ccm = ccm;
      this.pool = null;
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
      
      Credential credential = new Credential(null, cri);
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
         pool.returnConnectionListener((org.ironjacamar.core.connectionmanager.listener.ConnectionListener)cl, kill);
      }
      catch (Exception e)
      {
         //
      }
   }

   /**
    * Get a connection listener
    * @param credential The credential
    * @return The listener
    * @exception ResourceException Thrown in case of an error
    */
   protected org.ironjacamar.core.connectionmanager.listener.ConnectionListener
      getConnectionListener(Credential credential) throws ResourceException
   {
      return pool.getConnectionListener(credential);
   }
}
