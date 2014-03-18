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

package org.jboss.jca.core.connectionmanager.pool.mcp;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core statistics.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ManagedConnectionPoolStatisticsImpl implements ManagedConnectionPoolStatistics
{
   /** Serial version uid */
   private static final long serialVersionUID = 6L;

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
   private static final String TOTAL_BLOCKING_TIME_INVOCATIONS = "TotalBlockingTimeInvocations";
   private static final String TOTAL_CREATION_TIME = "TotalCreationTime";
   private static final String TOTAL_GET_TIME = "TotalGetTime";
   private static final String TOTAL_GET_TIME_INVOCATIONS = "TotalGetTimeInvocations";
   private static final String WAIT_COUNT = "WaitCount";

   private int maxPoolSize;

   private transient Set<String> names;
   private transient Map<String, Class> types;
   private transient Map<Locale, ResourceBundle> rbs;

   private transient AtomicBoolean enabled;
   private transient AtomicInteger createdCount;
   private transient AtomicInteger destroyedCount;
   private transient AtomicInteger maxUsedCount;
   private transient AtomicLong maxCreationTime;
   private transient AtomicLong maxGetTime;
   private transient AtomicInteger maxWaitCount;
   private transient AtomicLong maxWaitTime;
   private transient AtomicInteger timedOut;
   private transient AtomicLong totalBlockingTime;
   private transient AtomicLong totalBlockingTimeInvocations;
   private transient AtomicLong totalCreationTime;
   private transient AtomicLong totalGetTime;
   private transient AtomicLong totalGetTimeInvocations;
   private transient AtomicInteger inUseCount;
   private transient AtomicInteger blockingFailureCount;
   private transient AtomicInteger waitCount;

   /**
    * Constructor
    * @param maxPoolSize The maximum pool size
    */
   public ManagedConnectionPoolStatisticsImpl(int maxPoolSize)
   {
      init(maxPoolSize);
   }

   /**
    * Init
    * @param maxPoolSize The maximum pool size
    */
   private void init(int maxPoolSize)
   {
      this.maxPoolSize = maxPoolSize;

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
      
      ResourceBundle defaultResourceBundle = 
         ResourceBundle.getBundle("poolstatistics", Locale.US, 
                                  SecurityActions.getClassLoader(ManagedConnectionPoolStatisticsImpl.class));
      this.rbs = new HashMap<Locale, ResourceBundle>(1);
      this.rbs.put(Locale.US, defaultResourceBundle);

      this.enabled = new AtomicBoolean(true);
      this.createdCount = new AtomicInteger(0);
      this.destroyedCount = new AtomicInteger(0);
      this.maxCreationTime = new AtomicLong(Long.MIN_VALUE);
      this.maxGetTime = new AtomicLong(Long.MIN_VALUE);
      this.maxUsedCount = new AtomicInteger(Integer.MIN_VALUE);
      this.maxWaitCount = new AtomicInteger(Integer.MIN_VALUE);
      this.maxWaitTime = new AtomicLong(Long.MIN_VALUE);
      this.timedOut = new AtomicInteger(0);
      this.totalBlockingTime = new AtomicLong(0);
      this.totalBlockingTimeInvocations = new AtomicLong(0);
      this.totalCreationTime = new AtomicLong(0);
      this.totalGetTime = new AtomicLong(0);
      this.totalGetTimeInvocations = new AtomicLong(0);
      this.inUseCount = new AtomicInteger(0);
      this.blockingFailureCount = new AtomicInteger(0);
      this.waitCount = new AtomicInteger(0);
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
            ResourceBundle.getBundle("poolstatistics", locale, 
                                     SecurityActions.getClassLoader(ManagedConnectionPoolStatisticsImpl.class));

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
   public void setEnabled(boolean v)
   {
      enabled.set(v);
      clear();
   }

   /**
    * {@inheritDoc}
    */
   public int getActiveCount()
   {
      if (!enabled.get())
         return 0;

      return createdCount.get() - destroyedCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public int getAvailableCount()
   {
      if (!enabled.get())
         return 0;

      return maxPoolSize - inUseCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageBlockingTime()
   {
      if (!enabled.get())
         return 0L;

      return totalBlockingTimeInvocations.get() != 0 ? totalBlockingTime.get() / totalBlockingTimeInvocations.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageCreationTime()
   {
      if (!enabled.get())
         return 0L;

      return createdCount.get() != 0 ? totalCreationTime.get() / createdCount.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageGetTime()
   {
      if (!enabled.get())
         return 0L;

      return totalGetTimeInvocations.get() != 0 ? totalGetTime.get() / totalGetTimeInvocations.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getBlockingFailureCount()
   {
      if (!enabled.get())
         return 0;

      return blockingFailureCount.get();
   }

   /**
    * Delta the blocking failure count value
    */
   public void deltaBlockingFailureCount()
   {
      if (enabled.get())
         blockingFailureCount.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getCreatedCount()
   {
      if (!enabled.get())
         return 0;

      return createdCount.get();
   }

   /**
    * Delta the created count value
    */
   public void deltaCreatedCount()
   {
      if (enabled.get())
         createdCount.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getDestroyedCount()
   {
      if (!enabled.get())
         return 0;

      return destroyedCount.get();
   }

   /**
    * Delta the destroyed count value
    */
   public void deltaDestroyedCount()
   {
      if (enabled.get())
         destroyedCount.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getIdleCount()
   {
      if (!enabled.get())
         return 0;

      return getActiveCount() - getInUseCount();
   }

   /**
    * {@inheritDoc}
    */
   public int getInUseCount()
   {
      if (!enabled.get())
         return 0;

      return inUseCount.get();
   }

   /**
    * Set in used count
    * @param v The value
    */
   public void setInUsedCount(int v)
   {
      if (enabled.get())
      {
         inUseCount.set(v);
         setMaxUsedCount(v);
      }
   }

   /**
    * Get max used count
    * @return The value
    */
   public int getMaxUsedCount()
   {
      if (!enabled.get())
         return 0;

      return maxUsedCount.get() != Integer.MIN_VALUE ? maxUsedCount.get() : 0;
   }

   /**
    * Set max used count
    * @param v The value
    */
   public void setMaxUsedCount(int v)
   {
      if (enabled.get() && v > maxUsedCount.get())
         maxUsedCount.set(v);
   }

   /**
    * Get max wait count
    * @return The value
    */
   public int getMaxWaitCount()
   {
      if (!enabled.get())
         return 0;

      return maxWaitCount.get() != Integer.MIN_VALUE ? maxWaitCount.get() : 0;
   }

   /**
    * Set max wait count
    * @param v The value
    */
   public void setMaxWaitCount(int v)
   {
      if (enabled.get() && v > maxWaitCount.get())
         maxWaitCount.set(v);
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxCreationTime()
   {
      if (!enabled.get())
         return 0L;

      return maxCreationTime.get() != Long.MIN_VALUE ? maxCreationTime.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxGetTime()
   {
      if (!enabled.get())
         return 0L;

      return maxGetTime.get() != Long.MIN_VALUE ? maxGetTime.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxWaitTime()
   {
      if (!enabled.get())
         return 0L;

      return maxWaitTime.get() != Long.MIN_VALUE ? maxWaitTime.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getTimedOut()
   {
      if (!enabled.get())
         return 0;

      return timedOut.get();
   }

   /**
    * Delta the timed out value
    */
   public void deltaTimedOut()
   {
      if (enabled.get())
         timedOut.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalBlockingTime()
   {
      if (!enabled.get())
         return 0L;

      return totalBlockingTime.get();
   }

   /**
    * Add delta to total blocking timeout
    * @param delta The value
    */
   public void deltaTotalBlockingTime(long delta)
   {
      if (enabled.get() && delta > 0)
      {
         totalBlockingTime.addAndGet(delta);
         totalBlockingTimeInvocations.incrementAndGet();

         if (delta > maxWaitTime.get())
            maxWaitTime.set(delta);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalBlockingInvocations()
   {
      if (!enabled.get())
         return 0L;

      return totalBlockingTimeInvocations.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalCreationTime()
   {
      if (!enabled.get())
         return 0L;

      return totalCreationTime.get();
   }

   /**
    * Add delta to total creation time
    * @param delta The value
    */
   public void deltaTotalCreationTime(long delta)
   {
      if (enabled.get() && delta > 0)
      {
         totalCreationTime.addAndGet(delta);

         if (delta > maxCreationTime.get())
            maxCreationTime.set(delta);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalGetTime()
   {
      if (!enabled.get())
         return 0L;

      return totalGetTime.get();
   }

   /**
    * Add delta to total get time
    * @param delta The value
    */
   public void deltaTotalGetTime(long delta)
   {
      if (enabled.get() && delta > 0)
      {
         totalGetTime.addAndGet(delta);
         totalGetTimeInvocations.incrementAndGet();

         if (delta > maxGetTime.get())
            maxGetTime.set(delta);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalGetInvocations()
   {
      if (!enabled.get())
         return 0L;

      return totalGetTimeInvocations.get();
   }

   /**
    * {@inheritDoc}
    */
   public int getWaitCount()
   {
      if (!enabled.get())
         return 0;

      return waitCount.get();
   }

   /**
    * Add delta wait count
    */
   public void deltaWaitCount()
   {
      if (enabled.get())
         waitCount.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public void clear()
   {
      this.createdCount.set(0);
      this.destroyedCount.set(0);
      this.maxCreationTime.set(Long.MIN_VALUE);
      this.maxGetTime.set(Long.MIN_VALUE);
      this.maxUsedCount.set(Integer.MIN_VALUE);
      this.maxWaitCount.set(Integer.MIN_VALUE);
      this.maxWaitTime.set(Long.MIN_VALUE);
      this.timedOut.set(0);
      this.totalBlockingTime.set(0L);
      this.totalBlockingTimeInvocations.set(0L);
      this.totalCreationTime.set(0L);
      this.totalGetTime.set(0L);
      this.totalGetTimeInvocations.set(0L);
      this.inUseCount.set(0);
      this.blockingFailureCount.set(0);
      this.waitCount.set(0);
   }

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
      out.writeInt(maxPoolSize);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      init(in.readInt());
   }

   /**
    * toString
    * @return The value
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ManagedConnectionPoolStatistics@").append(Integer.toHexString(System.identityHashCode(this)));

      sb.append("[");

      sb.append("Enabled=").append(isEnabled());
      sb.append(",");
      sb.append(ACTIVE_COUNT).append("=").append(getActiveCount());
      sb.append(",");
      sb.append(AVAILABLE_COUNT).append("=").append(getAvailableCount());
      sb.append(",");
      sb.append(AVERAGE_BLOCKING_TIME).append("=").append(getAverageBlockingTime());
      sb.append(",");
      sb.append(AVERAGE_CREATION_TIME).append("=").append(getAverageCreationTime());
      sb.append(",");
      sb.append(AVERAGE_GET_TIME).append("=").append(getAverageCreationTime());
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
      sb.append(TOTAL_BLOCKING_TIME_INVOCATIONS).append("=").append(totalBlockingTimeInvocations.get());
      sb.append(",");
      sb.append(TOTAL_CREATION_TIME).append("=").append(getTotalCreationTime());
      sb.append(",");
      sb.append(TOTAL_GET_TIME).append("=").append(getTotalGetTime());
      sb.append(",");
      sb.append(TOTAL_GET_TIME_INVOCATIONS).append("=").append(totalGetTimeInvocations.get());
      sb.append(",");
      sb.append(WAIT_COUNT).append("=").append(getWaitCount());

      sb.append("]");
      
      return sb.toString();
   }
}
