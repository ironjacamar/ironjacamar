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

package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pool statistics
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolStatisticsImpl implements PoolStatistics
{
   /** Serial version uid */
   private static final long serialVersionUID = 5L;

   private static final String ACTIVE_COUNT = "ActiveCount";
   private static final String AVAILABLE_COUNT = "AvailableCount";
   private static final String AVERAGE_BLOCKING_TIME = "AverageBlockingTime";
   private static final String AVERAGE_CREATION_TIME = "AverageCreationTime";
   private static final String AVERAGE_GET_TIME = "AverageGetTime";
   private static final String BLOCKING_FAILURE_COUNT = "BlockingFailureCount";
   private static final String CREATED_COUNT = "CreatedCount";
   private static final String DESTROYED_COUNT = "DestroyedCount";
   private static final String IDLE_COUNT = "IdleCount";
   private static final String IN_USE_COUNT = "InUseCount";
   private static final String MAX_CREATION_TIME = "MaxCreationTime";
   private static final String MAX_GET_TIME = "MaxGetTime";
   private static final String MAX_USED_COUNT = "MaxUsedCount";
   private static final String MAX_WAIT_COUNT = "MaxWaitCount";
   private static final String MAX_WAIT_TIME = "MaxWaitTime";
   private static final String TIMED_OUT = "TimedOut";
   private static final String TOTAL_BLOCKING_TIME = "TotalBlockingTime";
   private static final String TOTAL_CREATION_TIME = "TotalCreationTime";
   private static final String TOTAL_GET_TIME = "TotalGetTime";
   private static final String WAIT_COUNT = "WaitCount";

   private int maxPoolSize;
   private transient ConcurrentMap<Object, ManagedConnectionPool> mcpPools;
   private transient Set<String> names;
   private transient Map<String, Class> types;
   private transient AtomicBoolean enabled;
   private transient Map<Locale, ResourceBundle> rbs;

   /**
    * Constructor
    * @param maxPoolSize The maximum pool size
    * @param mcpPools The pool map
    */
   public PoolStatisticsImpl(int maxPoolSize, ConcurrentMap<Object, ManagedConnectionPool> mcpPools)
   {
      init(maxPoolSize, mcpPools);
   }

   /**
    * Init
    * @param maxPoolSize The maximum pool size
    * @param mcpPools The pool map
    */
   private void init(int maxPoolSize, ConcurrentMap<Object, ManagedConnectionPool> mcpPools)
   {
      this.maxPoolSize = maxPoolSize;
      this.mcpPools = mcpPools;

      Set<String> n = new HashSet<String>();
      Map<String, Class> t = new HashMap<String, Class>();

      n.add(ACTIVE_COUNT);
      t.put(ACTIVE_COUNT, int.class);

      n.add(AVAILABLE_COUNT);
      t.put(AVAILABLE_COUNT, int.class);

      n.add(AVERAGE_BLOCKING_TIME);
      t.put(AVERAGE_BLOCKING_TIME, long.class);

      n.add(AVERAGE_CREATION_TIME);
      t.put(AVERAGE_CREATION_TIME, long.class);

      n.add(AVERAGE_GET_TIME);
      t.put(AVERAGE_GET_TIME, long.class);

      n.add(BLOCKING_FAILURE_COUNT);
      t.put(BLOCKING_FAILURE_COUNT, int.class);

      n.add(CREATED_COUNT);
      t.put(CREATED_COUNT, int.class);

      n.add(DESTROYED_COUNT);
      t.put(DESTROYED_COUNT, int.class);

      n.add(IDLE_COUNT);
      t.put(IDLE_COUNT, int.class);

      n.add(IN_USE_COUNT);
      t.put(IN_USE_COUNT, int.class);

      n.add(MAX_CREATION_TIME);
      t.put(MAX_CREATION_TIME, long.class);

      n.add(MAX_GET_TIME);
      t.put(MAX_GET_TIME, long.class);

      n.add(MAX_USED_COUNT);
      t.put(MAX_USED_COUNT, int.class);

      n.add(MAX_WAIT_COUNT);
      t.put(MAX_WAIT_COUNT, int.class);

      n.add(MAX_WAIT_TIME);
      t.put(MAX_WAIT_TIME, long.class);

      n.add(TIMED_OUT);
      t.put(TIMED_OUT, int.class);

      n.add(TOTAL_BLOCKING_TIME);
      t.put(TOTAL_BLOCKING_TIME, long.class);

      n.add(TOTAL_CREATION_TIME);
      t.put(TOTAL_CREATION_TIME, long.class);

      n.add(TOTAL_GET_TIME);
      t.put(TOTAL_GET_TIME, long.class);

      n.add(WAIT_COUNT);
      t.put(WAIT_COUNT, int.class);

      this.names = Collections.unmodifiableSet(n);
      this.types = Collections.unmodifiableMap(t);
      this.enabled = new AtomicBoolean(true);
      
      ResourceBundle defaultResourceBundle = 
         ResourceBundle.getBundle("poolstatistics", Locale.US, PoolStatisticsImpl.class.getClassLoader());
      this.rbs = new HashMap<Locale, ResourceBundle>(1);
      this.rbs.put(Locale.US, defaultResourceBundle);

      clear();
   }


   /**
    * {@inheritDoc}
    */
   public Set<String> getNames()
   {
      return names;
   }

   /**
    * {@inheritDoc}
    */
   public Class getType(String name)
   {
      return types.get(name);
   }

   /**
    * {@inheritDoc}
    */
   public String getDescription(String name)
   {
      return getDescription(name, Locale.US);
   }

   /**
    * {@inheritDoc}
    */
   public String getDescription(String name, Locale locale)
   {
      ResourceBundle rb = rbs.get(locale);

      if (rb == null)
      {
         ResourceBundle newResourceBundle =
            ResourceBundle.getBundle("poolstatistics", locale, PoolStatisticsImpl.class.getClassLoader());

         if (newResourceBundle != null)
            rbs.put(locale, newResourceBundle);
      }

      if (rb == null)
         rb = rbs.get(Locale.US);

      if (rb != null)
         return rb.getString(name);

      return "";
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getValue(String name)
   {
      if (ACTIVE_COUNT.equals(name))
      {
         return getActiveCount();
      }
      else if (AVAILABLE_COUNT.equals(name))
      {
         return getAvailableCount();
      }
      else if (AVERAGE_BLOCKING_TIME.equals(name))
      {
         return getAverageBlockingTime();
      }
      else if (AVERAGE_CREATION_TIME.equals(name))
      {
         return getAverageCreationTime();
      }
      else if (AVERAGE_GET_TIME.equals(name))
      {
         return getAverageGetTime();
      }
      else if (BLOCKING_FAILURE_COUNT.equals(name))
      {
         return getBlockingFailureCount();
      }
      else if (CREATED_COUNT.equals(name))
      {
         return getCreatedCount();
      }
      else if (DESTROYED_COUNT.equals(name))
      {
         return getDestroyedCount();
      }
      else if (IDLE_COUNT.equals(name))
      {
         return getIdleCount();
      }
      else if (IN_USE_COUNT.equals(name))
      {
         return getInUseCount();
      }
      else if (MAX_CREATION_TIME.equals(name))
      {
         return getMaxCreationTime();
      }
      else if (MAX_GET_TIME.equals(name))
      {
         return getMaxGetTime();
      }
      else if (MAX_USED_COUNT.equals(name))
      {
         return getMaxUsedCount();
      }
      else if (MAX_WAIT_COUNT.equals(name))
      {
         return getMaxWaitCount();
      }
      else if (MAX_WAIT_TIME.equals(name))
      {
         return getMaxWaitTime();
      }
      else if (TIMED_OUT.equals(name))
      {
         return getTimedOut();
      }
      else if (TOTAL_BLOCKING_TIME.equals(name))
      {
         return getTotalBlockingTime();
      }
      else if (TOTAL_CREATION_TIME.equals(name))
      {
         return getTotalCreationTime();
      }
      else if (TOTAL_GET_TIME.equals(name))
      {
         return getTotalGetTime();
      }
      else if (WAIT_COUNT.equals(name))
      {
         return getWaitCount();
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isEnabled()
   {
      return enabled.get();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setEnabled(boolean v)
   {
      enabled.set(v);

      for (ManagedConnectionPool mcp : mcpPools.values())
      {
         mcp.getStatistics().setEnabled(v);
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getActiveCount()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getActiveCount();
         }
         
         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getAvailableCount()
   {
      return getAvailableCount(isEnabled());
   }

   /**
    * The available count
    * @param override True if the value should be returned
    * @return The value
    */
   int getAvailableCount(boolean override)
   {
      if (override)
      {
         int result = -1;

         if (mcpPools.size() > 0)
         {
            result = 0;
            for (ManagedConnectionPool mcp : mcpPools.values())
            {
               result += mcp.getStatistics().getAvailableCount();
            }
         }
         
         if (result != -1)
            return result;

         return maxPoolSize;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageBlockingTime()
   {
      if (isEnabled())
      {
         long invocations = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            invocations += mcp.getStatistics().getTotalBlockingInvocations();
         }

         return invocations != 0 ? getTotalBlockingTime() / invocations : 0;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageCreationTime()
   {
      if (isEnabled())
      {
         int createdCount = getCreatedCount();
         return createdCount != 0 ? getTotalCreationTime() / createdCount : 0;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageGetTime()
   {
      if (isEnabled())
      {
         long invocations = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            invocations += mcp.getStatistics().getTotalGetInvocations();
         }

         return invocations != 0 ? getTotalGetTime() / invocations : 0;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getBlockingFailureCount()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getBlockingFailureCount();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getCreatedCount()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getCreatedCount();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getDestroyedCount()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getDestroyedCount();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getIdleCount()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getIdleCount();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getInUseCount()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getInUseCount();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxCreationTime()
   {
      if (isEnabled())
      {
         long result = Long.MIN_VALUE;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            long v = mcp.getStatistics().getMaxCreationTime();
            if (v > result)
               result = v;
         }

         return result != Long.MIN_VALUE ? result : 0;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxGetTime()
   {
      if (isEnabled())
      {
         long result = Long.MIN_VALUE;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            long v = mcp.getStatistics().getMaxGetTime();
            if (v > result)
               result = v;
         }

         return result != Long.MIN_VALUE ? result : 0;
      }

      return 0;
   }

   /**
    * Get max used count
    * @return The value
    */
   public int getMaxUsedCount()
   {
      if (isEnabled())
      {
         int result = Integer.MIN_VALUE;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            int v = mcp.getStatistics().getMaxUsedCount();
            if (v > result)
               result = v;
         }

         return result != Integer.MIN_VALUE ? result : 0;
      }

      return 0;
   }

   /**
    * Get max wait count
    * @return The value
    */
   public int getMaxWaitCount()
   {
      if (isEnabled())
      {
         int result = Integer.MIN_VALUE;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            int v = mcp.getStatistics().getMaxWaitCount();
            if (v > result)
               result = v;
         }

         return result != Integer.MIN_VALUE ? result : 0;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxWaitTime()
   {
      if (isEnabled())
      {
         long result = Long.MIN_VALUE;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            long v = mcp.getStatistics().getMaxWaitTime();
            if (v > result)
               result = v;
         }

         return result != Long.MIN_VALUE ? result : 0;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getTimedOut()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getTimedOut();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalBlockingTime()
   {
      if (isEnabled())
      {
         long result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getTotalBlockingTime();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalCreationTime()
   {
      if (isEnabled())
      {
         long result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getTotalCreationTime();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalGetTime()
   {
      if (isEnabled())
      {
         long result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getTotalGetTime();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getWaitCount()
   {
      if (isEnabled())
      {
         int result = 0;

         for (ManagedConnectionPool mcp : mcpPools.values())
         {
            result += mcp.getStatistics().getWaitCount();
         }

         return result;
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public void clear()
   {
      for (ManagedConnectionPool mcp : mcpPools.values())
      {
         mcp.getStatistics().clear();
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.writeInt(maxPoolSize);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      init(in.readInt(), new ConcurrentHashMap<Object, ManagedConnectionPool>());
   }

   /**
    * toString
    * @return The value
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("PoolStatistics@").append(Integer.toHexString(System.identityHashCode(this)));

      sb.append("[");

      sb.append(ACTIVE_COUNT).append("=").append(getActiveCount());
      sb.append(",");
      sb.append(AVAILABLE_COUNT).append("=").append(getAvailableCount());
      sb.append(",");
      sb.append(AVERAGE_BLOCKING_TIME).append("=").append(getAverageBlockingTime());
      sb.append(",");
      sb.append(AVERAGE_CREATION_TIME).append("=").append(getAverageCreationTime());
      sb.append(",");
      sb.append(AVERAGE_GET_TIME).append("=").append(getAverageGetTime());
      sb.append(",");
      sb.append(BLOCKING_FAILURE_COUNT).append("=").append(getBlockingFailureCount());
      sb.append(",");
      sb.append(CREATED_COUNT).append("=").append(getCreatedCount());
      sb.append(",");
      sb.append(DESTROYED_COUNT).append("=").append(getDestroyedCount());
      sb.append(",");
      sb.append(IDLE_COUNT).append("=").append(getIdleCount());
      sb.append(",");
      sb.append(IN_USE_COUNT).append("=").append(getInUseCount());
      sb.append(",");
      sb.append(MAX_CREATION_TIME).append("=").append(getMaxCreationTime());
      sb.append(",");
      sb.append(MAX_GET_TIME).append("=").append(getMaxGetTime());
      sb.append(",");
      sb.append(MAX_USED_COUNT).append("=").append(getMaxUsedCount());
      sb.append(",");
      sb.append(MAX_WAIT_COUNT).append("=").append(getMaxWaitCount());
      sb.append(",");
      sb.append(MAX_WAIT_TIME).append("=").append(getMaxWaitTime());
      sb.append(",");
      sb.append(TIMED_OUT).append("=").append(getTimedOut());
      sb.append(",");
      sb.append(TOTAL_BLOCKING_TIME).append("=").append(getTotalBlockingTime());
      sb.append(",");
      sb.append(TOTAL_CREATION_TIME).append("=").append(getTotalCreationTime());
      sb.append(",");
      sb.append(TOTAL_GET_TIME).append("=").append(getTotalGetTime());
      sb.append(",");
      sb.append(WAIT_COUNT).append("=").append(getWaitCount());

      sb.append("]");
      
      return sb.toString();
   }
}
