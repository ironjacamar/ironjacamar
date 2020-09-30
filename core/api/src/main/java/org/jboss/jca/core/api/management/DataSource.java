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

package org.jboss.jca.core.api.management;

import org.jboss.jca.core.api.connectionmanager.pool.Pool;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

import java.lang.ref.WeakReference;

/**
 * Represents a datasource instance
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a> 
 */
public class DataSource implements ManagedEnlistmentTrace
{
   /** xa datasource or not */
   private boolean xa;
   
   /** jndi name */
   private String jndiName;
   
   /** The pool instance */
   private WeakReference<Pool> pool;

   /** The pool configuration instance */
   private WeakReference<PoolConfiguration> poolConfiguration;

   /** The statistics */
   private WeakReference<StatisticsPlugin> statistics;

   /** The enlistment trace */
   private WeakReference<Boolean> enlistmentTrace;


   /**
    * Constructor
    * @param xa datasource is xa or not
    */
   public DataSource(boolean xa)
   {
      this.xa = xa;
   }

   /**
    * xa datasource
    * @return true if it xa datasource
    */
   public boolean isXA()
   {
      return xa;
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
    * @param v The value
    */
   public void setJndiName(String v)
   {
      this.jndiName = v;
   }

   /**
    * Get the statistics instance.
    * 
    * Note, that the value may be <code>null</code> if the statistics module was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public StatisticsPlugin getStatistics()
   {
      if (statistics == null)
         return null;

      return statistics.get();
   }


   /**
    * Set the statistics
    * @param v The statistics module
    */
   public void setStatistics(StatisticsPlugin v)
   {
      this.statistics = new WeakReference<StatisticsPlugin>(v);
   }

   /**
    * Get the enlistmentTrace instance.
    *
    * Note, that the value may be <code>null</code> if the enlistmentTrace module was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
     */
   @Override
   public Boolean getEnlistmentTrace()
   {
      if (enlistmentTrace == null)
         return null;

      return enlistmentTrace.get();
   }

   /**
    * Set the enlistmentTrace
    * @param enlistmentTrace The enlistmentTrace module
    */
   public void setEnlistmentTrace(Boolean enlistmentTrace)
   {
      this.enlistmentTrace = new WeakReference<Boolean>(enlistmentTrace);
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("DataSource@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append(" xa=").append(isXA());
      sb.append(" jndiName=").append(getJndiName());
      sb.append(" pool=").append(getPool());
      sb.append(" poolconfiguration=").append(getPoolConfiguration());
      sb.append(" statistics=").append(getStatistics());
      sb.append(" enlistmentTrace=").append(getEnlistmentTrace());
      sb.append("]");

      return sb.toString();
   }
}
