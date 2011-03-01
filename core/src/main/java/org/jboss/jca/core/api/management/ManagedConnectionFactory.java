/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a managed connection factory instance
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ManagedConnectionFactory
{
   /** The object instance */
   private WeakReference<javax.resource.spi.ManagedConnectionFactory> instance;

   /** The config property's */
   private List<ConfigProperty> configProperties;

   /** The pool instance */
   private WeakReference<Pool> pool;

   /** The pool configuration instance */
   private WeakReference<PoolConfiguration> poolConfiguration;

   /**
    * Constructor
    * @param mcf The managed connection factory instance
    */
   public ManagedConnectionFactory(javax.resource.spi.ManagedConnectionFactory mcf)
   {
      this.instance = new WeakReference<javax.resource.spi.ManagedConnectionFactory>(mcf);
      this.configProperties = null;
      this.pool = null;
      this.poolConfiguration = null;
   }

   /**
    * Get the managed connection factory instance.
    * 
    * Note, that the value may be <code>null</code> if the managed connection factory was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public javax.resource.spi.ManagedConnectionFactory getManagedConnectionFactory()
   {
      return instance.get();
   }

   /**
    * Get the list of config property's
    * @return The value
    */
   public List<ConfigProperty> getConfigProperties()
   {
      if (configProperties == null)
         configProperties = new ArrayList<ConfigProperty>(1);

      return configProperties;
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
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ManagedConnectionFactory@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[instance=").append(getManagedConnectionFactory());
      sb.append(" configProperties=").append(configProperties);
      sb.append(" pool=").append(getPool());
      sb.append(" poolconfiguration=").append(getPoolConfiguration());
      sb.append("]");

      return sb.toString();
   }
}
