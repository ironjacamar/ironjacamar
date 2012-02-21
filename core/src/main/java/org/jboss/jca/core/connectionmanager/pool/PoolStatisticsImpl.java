/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
 * Sub pool statistics.
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class PoolStatisticsImpl implements PoolStatistics
{
   /** Serial version uid */
   private static final long serialVersionUID = 2L;

   private static final String ACTIVE_COUNT = "ActiveCount";
   private static final String AVAILABLE_COUNT = "AvailableCount";
   private static final String AVERAGE_BLOCKING_TIME = "AverageBlockingTime";
   private static final String AVERAGE_CREATION_TIME = "AverageCreationTime";
   private static final String CREATED_COUNT = "CreatedCount";
   private static final String DESTROYED_COUNT = "DestroyedCount";
   private static final String MAX_CREATION_TIME = "MaxCreationTime";
   private static final String MAX_USED_COUNT = "MaxUsedCount";
   private static final String MAX_WAIT_COUNT = "MaxWaitCount";
   private static final String MAX_WAIT_TIME = "MaxWaitTime";
   private static final String TIMED_OUT = "TimedOut";
   private static final String TOTAL_BLOCKING_TIME = "TotalBlockingTime";
   private static final String TOTAL_CREATION_TIME = "TotalCreationTime";

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

      n.add(CREATED_COUNT);
      t.put(CREATED_COUNT, int.class);

      n.add(DESTROYED_COUNT);
      t.put(DESTROYED_COUNT, int.class);

      n.add(MAX_CREATION_TIME);
      t.put(MAX_CREATION_TIME, long.class);

      n.add(MAX_USED_COUNT);
      t.put(MAX_USED_COUNT, int.class);

      n.add(MAX_WAIT_TIME);
      t.put(MAX_WAIT_TIME, long.class);

      n.add(TIMED_OUT);
      t.put(TIMED_OUT, int.class);

      n.add(TOTAL_BLOCKING_TIME);
      t.put(TOTAL_BLOCKING_TIME, long.class);

      n.add(TOTAL_CREATION_TIME);
      t.put(TOTAL_CREATION_TIME, long.class);

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
      else if (CREATED_COUNT.equals(name))
      {
         return getCreatedCount();
      }
      else if (DESTROYED_COUNT.equals(name))
      {
         return getDestroyedCount();
      }
      else if (MAX_CREATION_TIME.equals(name))
      {
         return getMaxCreationTime();
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
         return getCreatedCount() != 0 ? getTotalBlockingTime() / getCreatedCount() : 0;

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageCreationTime()
   {
      if (isEnabled())
         return getCreatedCount() != 0 ? getTotalCreationTime() / getCreatedCount() : 0;

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
      sb.append(CREATED_COUNT).append("=").append(getCreatedCount());
      sb.append(",");
      sb.append(DESTROYED_COUNT).append("=").append(getDestroyedCount());
      sb.append(",");
      sb.append(MAX_CREATION_TIME).append("=").append(getMaxCreationTime());
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

      sb.append("]");
      
      return sb.toString();
   }
}
