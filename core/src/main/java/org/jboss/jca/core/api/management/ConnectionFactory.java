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

package org.jboss.jca.core.api.management;

import org.jboss.jca.core.api.connectionmanager.pool.Pool;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;

import java.lang.ref.WeakReference;

/**
 * Represents a managed connection factory instance
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a> 
 */
public class ConnectionFactory
{
   /** The connection factory */
   private WeakReference<Object> connectionFactory;

   /** The managed connection factory */
   private ManagedConnectionFactory managedConnectionFactory;

   /** jndi name */
   private String jndiName;
   
   /** The pool instance */
   private WeakReference<Pool> pool;

   /** The pool configuration instance */
   private WeakReference<PoolConfiguration> poolConfiguration;
   
   /**
    * Constructor
    * @param cf The connection factory
    * @param mcf The managed connection factory instance
    */
   public ConnectionFactory(Object cf, javax.resource.spi.ManagedConnectionFactory mcf)
   {
      this.connectionFactory = new WeakReference<Object>(cf);
      this.managedConnectionFactory = new ManagedConnectionFactory(mcf);
      this.pool = null;
      this.poolConfiguration = null;
   }

   /**
    * Get the connection factory instance.
    * 
    * Note, that the value may be <code>null</code> if the connection factory was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public Object getConnectionFactory()
   {
      return connectionFactory.get();
   }

   /**
    * Get the managed connection factory instance.
    * 
    * Note, that the value may be <code>null</code> if the managed connection factory was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public ManagedConnectionFactory getManagedConnectionFactory()
   {
      return managedConnectionFactory;
   }

   /**
    * Get the pool instance.
    * 
    * Note, that the value may be <code>null</code> if the pool was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public Pool getPool()
   {
      if (pool == null)
         return null;

      return pool.get();
   }

   /**
    * Set the pool
    * @param p The pool
    */
   public void setPool(Pool p)
   {
      this.pool = new WeakReference<Pool>(p);
   }

   /**
    * Get the pool configuration instance.
    * 
    * Note, that the value may be <code>null</code> if the pool configuration was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public PoolConfiguration getPoolConfiguration()
   {
      if (poolConfiguration == null)
         return null;

      return poolConfiguration.get();
   }

   /**
    * Set the pool configuration
    * @param pc The pool configuration
    */
   public void setPoolConfiguration(PoolConfiguration pc)
   {
      this.poolConfiguration = new WeakReference<PoolConfiguration>(pc);
   }

   /**
    * Get the jndiName.
    * 
    * @return the jndiName.
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Set the jndiName.
    * 
    * @param jndiName The jndiName to set.
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ConnectionFactory@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[jndiName=").append(getJndiName());
      sb.append(" connectionFactory=").append(getConnectionFactory());
      sb.append(" managedConnectionFactory=").append(getManagedConnectionFactory());
      sb.append(" pool=").append(getPool());
      sb.append(" poolconfiguration=").append(getPoolConfiguration());
      sb.append("]");

      return sb.toString();
   }
}
