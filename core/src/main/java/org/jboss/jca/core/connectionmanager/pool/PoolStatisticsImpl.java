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
import org.jboss.jca.core.spi.transaction.XAResourceStatistics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Pool statistics
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolStatisticsImpl implements PoolStatistics, XAResourceStatistics
{
   /** Serial version uid */
   private static final long serialVersionUID = 9L;

   private static final String ACTIVE_COUNT = "ActiveCount";
   private static final String AVAILABLE_COUNT = "AvailableCount";
   private static final String AVERAGE_BLOCKING_TIME = "AverageBlockingTime";
   private static final String AVERAGE_CREATION_TIME = "AverageCreationTime";
   private static final String AVERAGE_GET_TIME = "AverageGetTime";
   private static final String AVERAGE_POOL_TIME = "AveragePoolTime";
   private static final String AVERAGE_USAGE_TIME = "AverageUsageTime";
   private static final String BLOCKING_FAILURE_COUNT = "BlockingFailureCount";
   private static final String CREATED_COUNT = "CreatedCount";
   private static final String DESTROYED_COUNT = "DestroyedCount";
   private static final String IDLE_COUNT = "IdleCount";
   private static final String IN_USE_COUNT = "InUseCount";
   private static final String MAX_CREATION_TIME = "MaxCreationTime";
   private static final String MAX_GET_TIME = "MaxGetTime";
   private static final String MAX_POOL_TIME = "MaxPoolTime";
   private static final String MAX_USAGE_TIME = "MaxUsageTime";
   private static final String MAX_USED_COUNT = "MaxUsedCount";
   private static final String MAX_WAIT_COUNT = "MaxWaitCount";
   private static final String MAX_WAIT_TIME = "MaxWaitTime";
   private static final String TIMED_OUT = "TimedOut";
   private static final String TOTAL_BLOCKING_TIME = "TotalBlockingTime";
   private static final String TOTAL_CREATION_TIME = "TotalCreationTime";
   private static final String TOTAL_GET_TIME = "TotalGetTime";
   private static final String TOTAL_POOL_TIME = "TotalPoolTime";
   private static final String TOTAL_USAGE_TIME = "TotalUsageTime";
   private static final String WAIT_COUNT = "WaitCount";

   private static final String XA_COMMIT_COUNT = "XACommitCount";
   private static final String XA_COMMIT_AVERAGE_TIME = "XACommitAverageTime";
   private static final String XA_COMMIT_TOTAL_TIME = "XACommitTotalTime";
   private static final String XA_COMMIT_MAX_TIME = "XACommitMaxTime";
   private static final String XA_END_COUNT = "XAEndCount";
   private static final String XA_END_AVERAGE_TIME = "XAEndAverageTime";
   private static final String XA_END_TOTAL_TIME = "XAEndTotalTime";
   private static final String XA_END_MAX_TIME = "XAEndMaxTime";
   private static final String XA_FORGET_COUNT = "XAForgetCount";
   private static final String XA_FORGET_AVERAGE_TIME = "XAForgetAverageTime";
   private static final String XA_FORGET_TOTAL_TIME = "XAForgetTotalTime";
   private static final String XA_FORGET_MAX_TIME = "XAForgetMaxTime";
   private static final String XA_PREPARE_COUNT = "XAPrepareCount";
   private static final String XA_PREPARE_AVERAGE_TIME = "XAPrepareAverageTime";
   private static final String XA_PREPARE_TOTAL_TIME = "XAPrepareTotalTime";
   private static final String XA_PREPARE_MAX_TIME = "XAPrepareMaxTime";
   private static final String XA_RECOVER_COUNT = "XARecoverCount";
   private static final String XA_RECOVER_AVERAGE_TIME = "XARecoverAverageTime";
   private static final String XA_RECOVER_TOTAL_TIME = "XARecoverTotalTime";
   private static final String XA_RECOVER_MAX_TIME = "XARecoverMaxTime";
   private static final String XA_ROLLBACK_COUNT = "XARollbackCount";
   private static final String XA_ROLLBACK_AVERAGE_TIME = "XARollbackAverageTime";
   private static final String XA_ROLLBACK_TOTAL_TIME = "XARollbackTotalTime";
   private static final String XA_ROLLBACK_MAX_TIME = "XARollbackMaxTime";
   private static final String XA_START_COUNT = "XAStartCount";
   private static final String XA_START_AVERAGE_TIME = "XAStartAverageTime";
   private static final String XA_START_TOTAL_TIME = "XAStartTotalTime";
   private static final String XA_START_MAX_TIME = "XAStartMaxTime";

   private int maxPoolSize;
   private transient SortedSet<String> names;
   private transient Map<String, Class> types;
   private transient Map<Locale, ResourceBundle> rbs;

   private transient AtomicBoolean enabled;
   private transient AtomicInteger createdCount;
   private transient AtomicInteger destroyedCount;
   private transient AtomicInteger maxUsedCount;
   private transient AtomicLong maxCreationTime;
   private transient AtomicLong maxGetTime;
   private transient AtomicLong maxPoolTime;
   private transient AtomicLong maxUsageTime;
   private transient AtomicInteger maxWaitCount;
   private transient AtomicLong maxWaitTime;
   private transient AtomicInteger timedOut;
   private transient AtomicLong totalBlockingTime;
   private transient AtomicLong totalBlockingTimeInvocations;
   private transient AtomicLong totalCreationTime;
   private transient AtomicLong totalGetTime;
   private transient AtomicLong totalGetTimeInvocations;
   private transient AtomicLong totalPoolTime;
   private transient AtomicLong totalPoolTimeInvocations;
   private transient AtomicLong totalUsageTime;
   private transient AtomicLong totalUsageTimeInvocations;
   private transient AtomicInteger inUseCount;
   private transient AtomicInteger blockingFailureCount;
   private transient AtomicInteger waitCount;


   private transient AtomicLong commitCount;
   private transient AtomicLong commitTotalTime;
   private transient AtomicLong commitMaxTime;
   private transient AtomicLong endCount;
   private transient AtomicLong endTotalTime;
   private transient AtomicLong endMaxTime;
   private transient AtomicLong forgetCount;
   private transient AtomicLong forgetTotalTime;
   private transient AtomicLong forgetMaxTime;
   private transient AtomicLong prepareCount;
   private transient AtomicLong prepareTotalTime;
   private transient AtomicLong prepareMaxTime;
   private transient AtomicLong recoverCount;
   private transient AtomicLong recoverTotalTime;
   private transient AtomicLong recoverMaxTime;
   private transient AtomicLong rollbackCount;
   private transient AtomicLong rollbackTotalTime;
   private transient AtomicLong rollbackMaxTime;
   private transient AtomicLong startCount;
   private transient AtomicLong startTotalTime;
   private transient AtomicLong startMaxTime;

   /**
    * Constructor
    * @param maxPoolSize The maximum pool size
    */
   public PoolStatisticsImpl(int maxPoolSize)
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

      this.createdCount = new AtomicInteger(0);
      this.destroyedCount = new AtomicInteger(0);
      this.maxCreationTime = new AtomicLong(Long.MIN_VALUE);
      this.maxGetTime = new AtomicLong(Long.MIN_VALUE);
      this.maxPoolTime = new AtomicLong(Long.MIN_VALUE);
      this.maxUsageTime = new AtomicLong(Long.MIN_VALUE);
      this.maxUsedCount = new AtomicInteger(Integer.MIN_VALUE);
      this.maxWaitCount = new AtomicInteger(0);
      this.maxWaitTime = new AtomicLong(Long.MIN_VALUE);
      this.timedOut = new AtomicInteger(0);
      this.totalBlockingTime = new AtomicLong(0);
      this.totalBlockingTimeInvocations = new AtomicLong(0);
      this.totalCreationTime = new AtomicLong(0);
      this.totalGetTime = new AtomicLong(0);
      this.totalGetTimeInvocations = new AtomicLong(0);
      this.totalPoolTime = new AtomicLong(0);
      this.totalPoolTimeInvocations = new AtomicLong(0);
      this.totalUsageTime = new AtomicLong(0);
      this.totalUsageTimeInvocations = new AtomicLong(0);
      this.inUseCount = new AtomicInteger(0);
      this.blockingFailureCount = new AtomicInteger(0);
      this.waitCount = new AtomicInteger(0);

      this.commitCount = new AtomicLong(0L);
      this.commitTotalTime = new AtomicLong(0L);
      this.commitMaxTime = new AtomicLong(0L);
      this.endCount = new AtomicLong(0L);
      this.endTotalTime = new AtomicLong(0L);
      this.endMaxTime = new AtomicLong(0L);
      this.forgetCount = new AtomicLong(0L);
      this.forgetTotalTime = new AtomicLong(0L);
      this.forgetMaxTime = new AtomicLong(0L);
      this.prepareCount = new AtomicLong(0L);
      this.prepareTotalTime = new AtomicLong(0L);
      this.prepareMaxTime = new AtomicLong(0L);
      this.recoverCount = new AtomicLong(0L);
      this.recoverTotalTime = new AtomicLong(0L);
      this.recoverMaxTime = new AtomicLong(0L);
      this.rollbackCount = new AtomicLong(0L);
      this.rollbackTotalTime = new AtomicLong(0L);
      this.rollbackMaxTime = new AtomicLong(0L);
      this.startCount = new AtomicLong(0L);
      this.startTotalTime = new AtomicLong(0L);
      this.startMaxTime = new AtomicLong(0L);

      SortedSet<String> n = new TreeSet<String>();
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

      n.add(AVERAGE_USAGE_TIME);
      t.put(AVERAGE_USAGE_TIME, long.class);

      n.add(AVERAGE_POOL_TIME);
      t.put(AVERAGE_POOL_TIME, long.class);

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

      n.add(MAX_POOL_TIME);
      t.put(MAX_POOL_TIME, long.class);

      n.add(MAX_USAGE_TIME);
      t.put(MAX_USAGE_TIME, long.class);

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

      n.add(TOTAL_POOL_TIME);
      t.put(TOTAL_POOL_TIME, long.class);

      n.add(TOTAL_USAGE_TIME);
      t.put(TOTAL_USAGE_TIME, long.class);

      n.add(WAIT_COUNT);
      t.put(WAIT_COUNT, int.class);

      n.add(XA_COMMIT_COUNT);
      t.put(XA_COMMIT_COUNT, long.class);
      n.add(XA_COMMIT_AVERAGE_TIME);
      t.put(XA_COMMIT_AVERAGE_TIME, long.class);
      n.add(XA_COMMIT_TOTAL_TIME);
      t.put(XA_COMMIT_TOTAL_TIME, long.class);
      n.add(XA_COMMIT_MAX_TIME);
      t.put(XA_COMMIT_MAX_TIME, long.class);

      n.add(XA_END_COUNT);
      t.put(XA_END_COUNT, long.class);
      n.add(XA_END_AVERAGE_TIME);
      t.put(XA_END_AVERAGE_TIME, long.class);
      n.add(XA_END_TOTAL_TIME);
      t.put(XA_END_TOTAL_TIME, long.class);
      n.add(XA_END_MAX_TIME);
      t.put(XA_END_MAX_TIME, long.class);

      n.add(XA_FORGET_COUNT);
      t.put(XA_FORGET_COUNT, long.class);
      n.add(XA_FORGET_AVERAGE_TIME);
      t.put(XA_FORGET_AVERAGE_TIME, long.class);
      n.add(XA_FORGET_TOTAL_TIME);
      t.put(XA_FORGET_TOTAL_TIME, long.class);
      n.add(XA_FORGET_MAX_TIME);
      t.put(XA_FORGET_MAX_TIME, long.class);

      n.add(XA_PREPARE_COUNT);
      t.put(XA_PREPARE_COUNT, long.class);
      n.add(XA_PREPARE_AVERAGE_TIME);
      t.put(XA_PREPARE_AVERAGE_TIME, long.class);
      n.add(XA_PREPARE_TOTAL_TIME);
      t.put(XA_PREPARE_TOTAL_TIME, long.class);
      n.add(XA_PREPARE_MAX_TIME);
      t.put(XA_PREPARE_MAX_TIME, long.class);

      n.add(XA_RECOVER_COUNT);
      t.put(XA_RECOVER_COUNT, long.class);
      n.add(XA_RECOVER_AVERAGE_TIME);
      t.put(XA_RECOVER_AVERAGE_TIME, long.class);
      n.add(XA_RECOVER_TOTAL_TIME);
      t.put(XA_RECOVER_TOTAL_TIME, long.class);
      n.add(XA_RECOVER_MAX_TIME);
      t.put(XA_RECOVER_MAX_TIME, long.class);

      n.add(XA_ROLLBACK_COUNT);
      t.put(XA_ROLLBACK_COUNT, long.class);
      n.add(XA_ROLLBACK_AVERAGE_TIME);
      t.put(XA_ROLLBACK_AVERAGE_TIME, long.class);
      n.add(XA_ROLLBACK_TOTAL_TIME);
      t.put(XA_ROLLBACK_TOTAL_TIME, long.class);
      n.add(XA_ROLLBACK_MAX_TIME);
      t.put(XA_ROLLBACK_MAX_TIME, long.class);

      n.add(XA_START_COUNT);
      t.put(XA_START_COUNT, long.class);
      n.add(XA_START_AVERAGE_TIME);
      t.put(XA_START_AVERAGE_TIME, long.class);
      n.add(XA_START_TOTAL_TIME);
      t.put(XA_START_TOTAL_TIME, long.class);
      n.add(XA_START_MAX_TIME);
      t.put(XA_START_MAX_TIME, long.class);

      this.names = Collections.unmodifiableSortedSet(n);
      this.types = Collections.unmodifiableMap(t);
      this.enabled = new AtomicBoolean(true);
      
      ResourceBundle defaultResourceBundle = 
         ResourceBundle.getBundle("poolstatistics", Locale.US,
                                  SecurityActions.getClassLoader(PoolStatisticsImpl.class));
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
            ResourceBundle.getBundle("poolstatistics", locale,
                                     SecurityActions.getClassLoader(PoolStatisticsImpl.class));

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
      else if (AVERAGE_USAGE_TIME.equals(name))
      {
         return getAverageUsageTime();
      }
      else if (AVERAGE_POOL_TIME.equals(name))
      {
         return getAveragePoolTime();
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
      else if (MAX_POOL_TIME.equals(name))
      {
         return getMaxPoolTime();
      }
      else if (MAX_USAGE_TIME.equals(name))
      {
         return getMaxUsageTime();
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
      else if (TOTAL_POOL_TIME.equals(name))
      {
         return getTotalPoolTime();
      }
      else if (TOTAL_USAGE_TIME.equals(name))
      {
         return getTotalUsageTime();
      }
      else if (WAIT_COUNT.equals(name))
      {
         return getWaitCount();
      }
      else if (XA_COMMIT_COUNT.equals(name))
      {
         return getCommitCount();
      }
      else if (XA_COMMIT_AVERAGE_TIME.equals(name))
      {
         return getCommitAverageTime();
      }
      else if (XA_COMMIT_TOTAL_TIME.equals(name))
      {
         return getCommitTotalTime();
      }
      else if (XA_COMMIT_MAX_TIME.equals(name))
      {
         return getCommitMaxTime();
      }
      else if (XA_END_COUNT.equals(name))
      {
         return getEndCount();
      }
      else if (XA_END_AVERAGE_TIME.equals(name))
      {
         return getEndAverageTime();
      }
      else if (XA_END_TOTAL_TIME.equals(name))
      {
         return getEndTotalTime();
      }
      else if (XA_END_MAX_TIME.equals(name))
      {
         return getEndMaxTime();
      }
      else if (XA_FORGET_COUNT.equals(name))
      {
         return getForgetCount();
      }
      else if (XA_FORGET_AVERAGE_TIME.equals(name))
      {
         return getForgetAverageTime();
      }
      else if (XA_FORGET_TOTAL_TIME.equals(name))
      {
         return getForgetTotalTime();
      }
      else if (XA_FORGET_MAX_TIME.equals(name))
      {
         return getForgetMaxTime();
      }
      else if (XA_PREPARE_COUNT.equals(name))
      {
         return getPrepareCount();
      }
      else if (XA_PREPARE_AVERAGE_TIME.equals(name))
      {
         return getPrepareAverageTime();
      }
      else if (XA_PREPARE_TOTAL_TIME.equals(name))
      {
         return getPrepareTotalTime();
      }
      else if (XA_PREPARE_MAX_TIME.equals(name))
      {
         return getPrepareMaxTime();
      }
      else if (XA_RECOVER_COUNT.equals(name))
      {
         return getRecoverCount();
      }
      else if (XA_RECOVER_AVERAGE_TIME.equals(name))
      {
         return getRecoverAverageTime();
      }
      else if (XA_RECOVER_TOTAL_TIME.equals(name))
      {
         return getRecoverTotalTime();
      }
      else if (XA_RECOVER_MAX_TIME.equals(name))
      {
         return getRecoverMaxTime();
      }
      else if (XA_ROLLBACK_COUNT.equals(name))
      {
         return getRollbackCount();
      }
      else if (XA_ROLLBACK_AVERAGE_TIME.equals(name))
      {
         return getRollbackAverageTime();
      }
      else if (XA_ROLLBACK_TOTAL_TIME.equals(name))
      {
         return getRollbackTotalTime();
      }
      else if (XA_ROLLBACK_MAX_TIME.equals(name))
      {
         return getRollbackMaxTime();
      }
      else if (XA_START_COUNT.equals(name))
      {
         return getStartCount();
      }
      else if (XA_START_AVERAGE_TIME.equals(name))
      {
         return getStartAverageTime();
      }
      else if (XA_START_TOTAL_TIME.equals(name))
      {
         return getStartTotalTime();
      }
      else if (XA_START_MAX_TIME.equals(name))
      {
         return getStartMaxTime();
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
      if (enabled.get() != v)
      {
         enabled.set(v);
         clear();
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getActiveCount()
   {
      if (!enabled.get())
         return 0;

      if (createdCount.get() < destroyedCount.get())
         clear();

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
   public long getAverageUsageTime()
   {
      if (!enabled.get())
         return 0L;

      return totalUsageTimeInvocations.get() != 0 ? totalUsageTime.get() / totalUsageTimeInvocations.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getAveragePoolTime()
   {
      if (!enabled.get())
         return 0L;

      return totalPoolTimeInvocations.get() != 0 ? totalPoolTime.get() / totalPoolTimeInvocations.get() : 0;
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
      inUseCount.set(v);
      setMaxUsedCount(v);
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
   private void setMaxUsedCount(int v)
   {
      if (v > maxUsedCount.get())
         maxUsedCount.set(v);
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
   public long getMaxPoolTime()
   {
      if (!enabled.get())
         return 0L;

      return maxPoolTime.get() != Long.MIN_VALUE ? maxPoolTime.get() : 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getMaxUsageTime()
   {
      if (!enabled.get())
         return 0L;

      return maxUsageTime.get() != Long.MIN_VALUE ? maxUsageTime.get() : 0;
   }

   /**
    * Get max wait count
    * @return The value
    */
   public int getMaxWaitCount()
   {
      if (!isEnabled())
         return 0;

      return maxWaitCount.get() != Integer.MIN_VALUE ? maxWaitCount.get() : 0;
   }

   /**
    * Set max wait count
    * @param v The value
    */
   public void setMaxWaitCount(int v)
   {
      if (v > maxWaitCount.get())
         maxWaitCount.set(v);
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
   public long getTotalPoolTime()
   {
      if (!enabled.get())
         return 0L;

      return totalPoolTime.get();
   }

   /**
    * Add delta to total pool time
    * @param delta The value
    */
   public void deltaTotalPoolTime(long delta)
   {
      if (enabled.get() && delta > 0)
      {
         totalPoolTime.addAndGet(delta);
         totalPoolTimeInvocations.incrementAndGet();

         if (delta > maxPoolTime.get())
            maxPoolTime.set(delta);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getTotalUsageTime()
   {
      if (!enabled.get())
         return 0L;

      return totalUsageTime.get();
   }

   /**
    * Add delta to total usage time
    * @param delta The value
    */
   public void deltaTotalUsageTime(long delta)
   {
      if (enabled.get() && delta > 0)
      {
         totalUsageTime.addAndGet(delta);
         totalUsageTimeInvocations.incrementAndGet();

         if (delta > maxUsageTime.get())
            maxUsageTime.set(delta);
      }
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
   public long getCommitCount()
   {
      if (!isEnabled())
         return 0L;

      return commitCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getCommitTotalTime()
   {
      if (!isEnabled())
         return 0L;

      return commitTotalTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getCommitAverageTime()
   {
      if (!isEnabled())
         return 0L;

      if (commitCount.get() > 0)
         return commitTotalTime.get() / commitCount.get();

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public long getCommitMaxTime()
   {
      if (!isEnabled())
         return 0L;

      return commitMaxTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaCommit(long time)
   {
      commitCount.incrementAndGet();

      if (time > 0)
      {
         commitTotalTime.addAndGet(time);

         if (time > commitMaxTime.get())
            commitMaxTime.set(time);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getEndCount()
   {
      if (!isEnabled())
         return 0L;

      return endCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getEndTotalTime()
   {
      if (!isEnabled())
         return 0L;

      return endTotalTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getEndAverageTime()
   {
      if (!isEnabled())
         return 0L;

      if (endCount.get() > 0)
         return endTotalTime.get() / endCount.get();

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public long getEndMaxTime()
   {
      if (!isEnabled())
         return 0L;

      return endMaxTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaEnd(long time)
   {
      endCount.incrementAndGet();

      if (time > 0)
      {
         endTotalTime.addAndGet(time);

         if (time > endMaxTime.get())
            endMaxTime.set(time);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getForgetCount()
   {
      if (!isEnabled())
         return 0L;

      return forgetCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getForgetTotalTime()
   {
      if (!isEnabled())
         return 0L;

      return forgetTotalTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getForgetAverageTime()
   {
      if (!isEnabled())
         return 0L;

      if (forgetCount.get() > 0)
         return forgetTotalTime.get() / forgetCount.get();

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public long getForgetMaxTime()
   {
      if (!isEnabled())
         return 0L;

      return forgetMaxTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaForget(long time)
   {
      forgetCount.incrementAndGet();

      if (time > 0)
      {
         forgetTotalTime.addAndGet(time);

         if (time > forgetMaxTime.get())
            forgetMaxTime.set(time);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getPrepareCount()
   {
      if (!isEnabled())
         return 0L;

      return prepareCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getPrepareTotalTime()
   {
      if (!isEnabled())
         return 0L;

      return prepareTotalTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getPrepareAverageTime()
   {
      if (!isEnabled())
         return 0L;

      if (prepareCount.get() > 0)
         return prepareTotalTime.get() / prepareCount.get();

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public long getPrepareMaxTime()
   {
      if (!isEnabled())
         return 0L;

      return prepareMaxTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaPrepare(long time)
   {
      prepareCount.incrementAndGet();

      if (time > 0)
      {
         prepareTotalTime.addAndGet(time);

         if (time > prepareMaxTime.get())
            prepareMaxTime.set(time);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getRecoverCount()
   {
      if (!isEnabled())
         return 0L;

      return recoverCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getRecoverTotalTime()
   {
      if (!isEnabled())
         return 0L;

      return recoverTotalTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getRecoverAverageTime()
   {
      if (!isEnabled())
         return 0L;

      if (recoverCount.get() > 0)
         return recoverTotalTime.get() / recoverCount.get();

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public long getRecoverMaxTime()
   {
      if (!isEnabled())
         return 0L;

      return recoverMaxTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaRecover(long time)
   {
      recoverCount.incrementAndGet();

      if (time > 0)
      {
         recoverTotalTime.addAndGet(time);

         if (time > recoverMaxTime.get())
            recoverMaxTime.set(time);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getRollbackCount()
   {
      if (!isEnabled())
         return 0L;

      return rollbackCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getRollbackTotalTime()
   {
      if (!isEnabled())
         return 0L;

      return rollbackTotalTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getRollbackAverageTime()
   {
      if (!isEnabled())
         return 0L;

      if (rollbackCount.get() > 0)
         return rollbackTotalTime.get() / rollbackCount.get();

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public long getRollbackMaxTime()
   {
      if (!isEnabled())
         return 0L;

      return rollbackMaxTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaRollback(long time)
   {
      rollbackCount.incrementAndGet();

      if (time > 0)
      {
         rollbackTotalTime.addAndGet(time);

         if (time > rollbackMaxTime.get())
            rollbackMaxTime.set(time);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getStartCount()
   {
      if (!isEnabled())
         return 0L;

      return startCount.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getStartTotalTime()
   {
      if (!isEnabled())
         return 0L;

      return startTotalTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public long getStartAverageTime()
   {
      if (!isEnabled())
         return 0L;

      if (startCount.get() > 0)
         return startTotalTime.get() / startCount.get();

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public long getStartMaxTime()
   {
      if (!isEnabled())
         return 0L;

      return startMaxTime.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaStart(long time)
   {
      startCount.incrementAndGet();

      if (time > 0)
      {
         startTotalTime.addAndGet(time);

         if (time > startMaxTime.get())
            startMaxTime.set(time);
      }
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
      this.maxPoolTime.set(Long.MIN_VALUE);
      this.maxUsageTime.set(Long.MIN_VALUE);
      this.maxUsedCount.set(Integer.MIN_VALUE);
      this.maxWaitTime.set(Long.MIN_VALUE);
      this.timedOut.set(0);
      this.totalBlockingTime.set(0L);
      this.totalBlockingTimeInvocations.set(0L);
      this.totalCreationTime.set(0L);
      this.totalGetTime.set(0L);
      this.totalGetTimeInvocations.set(0L);
      this.totalPoolTime.set(0L);
      this.totalPoolTimeInvocations.set(0L);
      this.totalUsageTime.set(0L);
      this.totalUsageTimeInvocations.set(0L);
      this.inUseCount.set(0);
      this.blockingFailureCount.set(0);
      this.waitCount.set(0);

      this.commitCount = new AtomicLong(0L);
      this.commitTotalTime = new AtomicLong(0L);
      this.commitMaxTime = new AtomicLong(0L);
      this.endCount = new AtomicLong(0L);
      this.endTotalTime = new AtomicLong(0L);
      this.endMaxTime = new AtomicLong(0L);
      this.forgetCount = new AtomicLong(0L);
      this.forgetTotalTime = new AtomicLong(0L);
      this.forgetMaxTime = new AtomicLong(0L);
      this.prepareCount = new AtomicLong(0L);
      this.prepareTotalTime = new AtomicLong(0L);
      this.prepareMaxTime = new AtomicLong(0L);
      this.recoverCount = new AtomicLong(0L);
      this.recoverTotalTime = new AtomicLong(0L);
      this.recoverMaxTime = new AtomicLong(0L);
      this.rollbackCount = new AtomicLong(0L);
      this.rollbackTotalTime = new AtomicLong(0L);
      this.rollbackMaxTime = new AtomicLong(0L);
      this.startCount = new AtomicLong(0L);
      this.startTotalTime = new AtomicLong(0L);
      this.startMaxTime = new AtomicLong(0L);
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

      sb.append("PoolStatistics@").append(Integer.toHexString(System.identityHashCode(this)));

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
      sb.append(AVERAGE_GET_TIME).append("=").append(getAverageGetTime());
      sb.append(",");
      sb.append(AVERAGE_POOL_TIME).append("=").append(getAveragePoolTime());
      sb.append(",");
      sb.append(AVERAGE_USAGE_TIME).append("=").append(getAverageUsageTime());
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
      sb.append(MAX_POOL_TIME).append("=").append(getMaxPoolTime());
      sb.append(",");
      sb.append(MAX_USAGE_TIME).append("=").append(getMaxUsageTime());
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
      sb.append(TOTAL_POOL_TIME).append("=").append(getTotalPoolTime());
      sb.append(",");
      sb.append(TOTAL_USAGE_TIME).append("=").append(getTotalUsageTime());
      sb.append(",");
      sb.append(WAIT_COUNT).append("=").append(getWaitCount());

      sb.append(",");
      sb.append(XA_COMMIT_COUNT).append("=").append(getCommitCount());
      sb.append(",");
      sb.append(XA_COMMIT_AVERAGE_TIME).append("=").append(getCommitAverageTime());
      sb.append(",");
      sb.append(XA_COMMIT_TOTAL_TIME).append("=").append(getCommitTotalTime());
      sb.append(",");
      sb.append(XA_COMMIT_MAX_TIME).append("=").append(getCommitMaxTime());
      sb.append(",");
      sb.append(XA_END_COUNT).append("=").append(getEndCount());
      sb.append(",");
      sb.append(XA_END_AVERAGE_TIME).append("=").append(getEndAverageTime());
      sb.append(",");
      sb.append(XA_END_TOTAL_TIME).append("=").append(getEndTotalTime());
      sb.append(",");
      sb.append(XA_END_MAX_TIME).append("=").append(getEndMaxTime());
      sb.append(",");
      sb.append(XA_FORGET_COUNT).append("=").append(getForgetCount());
      sb.append(",");
      sb.append(XA_FORGET_AVERAGE_TIME).append("=").append(getForgetAverageTime());
      sb.append(",");
      sb.append(XA_FORGET_TOTAL_TIME).append("=").append(getForgetTotalTime());
      sb.append(",");
      sb.append(XA_FORGET_MAX_TIME).append("=").append(getForgetMaxTime());
      sb.append(",");
      sb.append(XA_PREPARE_COUNT).append("=").append(getPrepareCount());
      sb.append(",");
      sb.append(XA_PREPARE_AVERAGE_TIME).append("=").append(getPrepareAverageTime());
      sb.append(",");
      sb.append(XA_PREPARE_TOTAL_TIME).append("=").append(getPrepareTotalTime());
      sb.append(",");
      sb.append(XA_PREPARE_MAX_TIME).append("=").append(getPrepareMaxTime());
      sb.append(",");
      sb.append(XA_RECOVER_COUNT).append("=").append(getRecoverCount());
      sb.append(",");
      sb.append(XA_RECOVER_AVERAGE_TIME).append("=").append(getRecoverAverageTime());
      sb.append(",");
      sb.append(XA_RECOVER_TOTAL_TIME).append("=").append(getRecoverTotalTime());
      sb.append(",");
      sb.append(XA_RECOVER_MAX_TIME).append("=").append(getRecoverMaxTime());
      sb.append(",");
      sb.append(XA_ROLLBACK_COUNT).append("=").append(getRollbackCount());
      sb.append(",");
      sb.append(XA_ROLLBACK_AVERAGE_TIME).append("=").append(getRollbackAverageTime());
      sb.append(",");
      sb.append(XA_ROLLBACK_TOTAL_TIME).append("=").append(getRollbackTotalTime());
      sb.append(",");
      sb.append(XA_ROLLBACK_MAX_TIME).append("=").append(getRollbackMaxTime());
      sb.append(",");
      sb.append(XA_START_COUNT).append("=").append(getStartCount());
      sb.append(",");
      sb.append(XA_START_AVERAGE_TIME).append("=").append(getStartAverageTime());
      sb.append(",");
      sb.append(XA_START_TOTAL_TIME).append("=").append(getStartTotalTime());
      sb.append(",");
      sb.append(XA_START_MAX_TIME).append("=").append(getStartMaxTime());

      sb.append("]");
      
      return sb.toString();
   }
}
