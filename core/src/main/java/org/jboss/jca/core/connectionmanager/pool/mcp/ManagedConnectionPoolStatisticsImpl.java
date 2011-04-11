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

package org.jboss.jca.core.connectionmanager.pool.mcp;

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
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ManagedConnectionPoolStatisticsImpl implements ManagedConnectionPoolStatistics
{
   private static final String ACTIVE_COUNT = "ActiveCount";
   private static final String AVERAGE_BLOCKING_TIME = "AverageBlockingTime";
   private static final String CREATED_COUNT = "CreatedCount";
   private static final String DESTROYED_COUNT = "DestroyedCount";
   private static final String MAX_USED_COUNT = "MaxUsedCount";
   private static final String MAX_WAIT_TIME = "MaxWaitTime";
   private static final String TIMED_OUT = "TimedOut";
   private static final String TOTAL_BLOCKING_TIME = "TotalBlockingTime";

   private Set<String> names;
   private Map<String, Class> types;
   private AtomicBoolean enabled;
   private Map<Locale, ResourceBundle> rbs;

   private AtomicLong totalBlockingTime;
   private AtomicInteger createdCount;
   private AtomicInteger destroyedCount;
   private AtomicInteger maxUsedCount;
   private AtomicLong maxWaitTime;
   private AtomicInteger timedOut;

   /**
    * Constructor
    */
   public ManagedConnectionPoolStatisticsImpl()
   {
      Set<String> n = new HashSet<String>();
      Map<String, Class> t = new HashMap<String, Class>();

      n.add(ACTIVE_COUNT);
      t.put(ACTIVE_COUNT, int.class);

      n.add(AVERAGE_BLOCKING_TIME);
      t.put(AVERAGE_BLOCKING_TIME, long.class);

      n.add(CREATED_COUNT);
      t.put(CREATED_COUNT, int.class);

      n.add(DESTROYED_COUNT);
      t.put(DESTROYED_COUNT, int.class);

      n.add(MAX_USED_COUNT);
      t.put(MAX_USED_COUNT, int.class);

      n.add(MAX_WAIT_TIME);
      t.put(MAX_WAIT_TIME, long.class);

      n.add(TIMED_OUT);
      t.put(TIMED_OUT, int.class);

      n.add(TOTAL_BLOCKING_TIME);
      t.put(TOTAL_BLOCKING_TIME, long.class);

      this.names = Collections.unmodifiableSet(n);
      this.types = Collections.unmodifiableMap(t);
      this.enabled = new AtomicBoolean(true);
      
      ResourceBundle defaultResourceBundle = 
         ResourceBundle.getBundle("poolstatistics", Locale.US, 
                                  ManagedConnectionPoolStatisticsImpl.class.getClassLoader());
      this.rbs = new HashMap<Locale, ResourceBundle>(1);
      this.rbs.put(Locale.US, defaultResourceBundle);

      this.totalBlockingTime = new AtomicLong(0);
      this.createdCount = new AtomicInteger(0);
      this.destroyedCount = new AtomicInteger(0);
      this.maxUsedCount = new AtomicInteger(Integer.MIN_VALUE);
      this.maxWaitTime = new AtomicLong(Long.MIN_VALUE);
      this.timedOut = new AtomicInteger(0);

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
            ResourceBundle.getBundle("poolstatistics", locale, 
                                     ManagedConnectionPoolStatisticsImpl.class.getClassLoader());

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
      else if (AVERAGE_BLOCKING_TIME.equals(name))
      {
         return getAverageBlockingTime();
      }
      else if (CREATED_COUNT.equals(name))
      {
         return getCreatedCount();
      }
      else if (DESTROYED_COUNT.equals(name))
      {
         return getDestroyedCount();
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
   }

   /**
    * {@inheritDoc}
    */
   public int getActiveCount()
   {
      if (isEnabled())
         return createdCount.get() - destroyedCount.get();

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAverageBlockingTime()
   {
      if (isEnabled())
         return createdCount.get() != 0 ? totalBlockingTime.get() / createdCount.get() : 0;

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getCreatedCount()
   {
      if (isEnabled())
         return createdCount.get();

      return 0;
   }

   /**
    * Delta the created count value
    */
   public void deltaCreatedCount()
   {
      if (isEnabled())
         createdCount.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getDestroyedCount()
   {
      if (isEnabled())
         return destroyedCount.get();

      return 0;
   }

   /**
    * Delta the destroyed count value
    */
   public void deltaDestroyedCount()
   {
      if (isEnabled())
         destroyedCount.incrementAndGet();
   }

   /**
    * Get max used count
    * @return The value
    */
   public int getMaxUsedCount()
   {
      if (isEnabled())
         return maxUsedCount.get() != Integer.MIN_VALUE ? maxUsedCount.get() : 0;

      return 0;
   }

   /**
    * Set max used count
    * @param v The value
    */
   public void setMaxUsedCount(int v)
   {
      if (isEnabled())
      {
         if (v > maxUsedCount.get())
            maxUsedCount.set(v);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxWaitTime()
   {
      if (isEnabled())
         return maxWaitTime.get() != Long.MIN_VALUE ? maxWaitTime.get() : 0;

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getTimedOut()
   {
      if (isEnabled())
         return timedOut.get();

      return 0;
   }

   /**
    * Delta the timed out value
    */
   public void deltaTimedOut()
   {
      if (isEnabled())
         timedOut.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalBlockingTime()
   {
      if (isEnabled())
         return totalBlockingTime.get();

      return 0;
   }

   /**
    * Add delta to total blocking timeout
    * @param delta The value
    */
   public void deltaTotalBlockingTime(long delta)
   {
      if (isEnabled())
      {
         totalBlockingTime.addAndGet(delta);

         if (delta > maxWaitTime.get())
            maxWaitTime.set(delta);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void clear()
   {
      // Do nothing
   }
}
