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
package org.jboss.jca.adapters.jdbc.statistics;

import org.jboss.jca.adapters.jdbc.PreparedStatementCache;
import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JDBC statistics.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JdbcStatisticsPlugin implements StatisticsPlugin
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   private static final String PREPARED_STATEMENT_CACHE_ACCESS_COUNT = "PreparedStatementCacheAccessCount";
   private static final String PREPARED_STATEMENT_CACHE_ADD_COUNT = "PreparedStatementCacheAddCount";
   private static final String PREPARED_STATEMENT_CACHE_CURRENT_SIZE = "PreparedStatementCacheCurrentSize";
   private static final String PREPARED_STATEMENT_CACHE_DELETE_COUNT = "PreparedStatementCacheDeleteCount";
   private static final String PREPARED_STATEMENT_CACHE_HIT_COUNT = "PreparedStatementCacheHitCount";
   private static final String PREPARED_STATEMENT_CACHE_MISS_COUNT = "PreparedStatementCacheMissCount";

   private AtomicLong preparedStatementCacheAccessCount;
   private AtomicLong preparedStatementCacheAddCount;
   private AtomicLong preparedStatementCacheDeleteCount;
   private AtomicLong preparedStatementCacheHitCount;
   private AtomicLong preparedStatementCacheMissCount;

   private Set<String> names;
   private Map<String, Class> types;
   private AtomicBoolean enabled;
   private transient Map<Locale, ResourceBundle> rbs;

   private transient Set<PreparedStatementCache> psCaches;

   /**
    * Constructor
    */
   public JdbcStatisticsPlugin()
   {
      Set<String> n = new HashSet<String>();
      Map<String, Class> t = new HashMap<String, Class>();

      n.add(PREPARED_STATEMENT_CACHE_ACCESS_COUNT);
      t.put(PREPARED_STATEMENT_CACHE_ACCESS_COUNT, long.class);

      n.add(PREPARED_STATEMENT_CACHE_ADD_COUNT);
      t.put(PREPARED_STATEMENT_CACHE_ADD_COUNT, long.class);

      n.add(PREPARED_STATEMENT_CACHE_CURRENT_SIZE);
      t.put(PREPARED_STATEMENT_CACHE_CURRENT_SIZE, int.class);

      n.add(PREPARED_STATEMENT_CACHE_DELETE_COUNT);
      t.put(PREPARED_STATEMENT_CACHE_DELETE_COUNT, long.class);

      n.add(PREPARED_STATEMENT_CACHE_HIT_COUNT);
      t.put(PREPARED_STATEMENT_CACHE_HIT_COUNT, long.class);

      n.add(PREPARED_STATEMENT_CACHE_MISS_COUNT);
      t.put(PREPARED_STATEMENT_CACHE_MISS_COUNT, long.class);

      this.names = Collections.unmodifiableSet(n);
      this.types = Collections.unmodifiableMap(t);
      this.enabled = new AtomicBoolean(true);
      
      ResourceBundle defaultResourceBundle = 
         ResourceBundle.getBundle("jdbc", Locale.US, SecurityActions.getClassLoader(JdbcStatisticsPlugin.class));
      this.rbs = new HashMap<Locale, ResourceBundle>(1);
      this.rbs.put(Locale.US, defaultResourceBundle);

      this.preparedStatementCacheAccessCount = new AtomicLong(0);
      this.preparedStatementCacheAddCount = new AtomicLong(0);
      this.preparedStatementCacheDeleteCount = new AtomicLong(0);
      this.preparedStatementCacheHitCount = new AtomicLong(0);
      this.preparedStatementCacheMissCount = new AtomicLong(0);

      this.psCaches = Collections.synchronizedSet(new HashSet<PreparedStatementCache>());

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
            ResourceBundle.getBundle("jdbc", locale,
                                     SecurityActions.getClassLoader(JdbcStatisticsPlugin.class));

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
      if (PREPARED_STATEMENT_CACHE_ACCESS_COUNT.equals(name))
      {
         return getPreparedStatementCacheAccessCount();
      }
      else if (PREPARED_STATEMENT_CACHE_ADD_COUNT.equals(name))
      {
         return getPreparedStatementCacheAddCount();
      }
      else if (PREPARED_STATEMENT_CACHE_CURRENT_SIZE.equals(name))
      {
         return getPreparedStatementCacheCurrentSize();
      }
      else if (PREPARED_STATEMENT_CACHE_DELETE_COUNT.equals(name))
      {
         return getPreparedStatementCacheDeleteCount();
      }
      else if (PREPARED_STATEMENT_CACHE_HIT_COUNT.equals(name))
      {
         return getPreparedStatementCacheHitCount();
      }
      else if (PREPARED_STATEMENT_CACHE_MISS_COUNT.equals(name))
      {
         return getPreparedStatementCacheMissCount();
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
    * Register prepared statement cache
    * @param v The cache
    */
   public void registerPreparedStatementCache(PreparedStatementCache v)
   {
      psCaches.add(v);
   }

   /**
    * Deregister prepared statement cache
    * @param v The cache
    */
   public void deregisterPreparedStatementCache(PreparedStatementCache v)
   {
      psCaches.remove(v);
   }

   /**
    * Get the access count for the prepated statement cache
    * @return The value
    */
   public long getPreparedStatementCacheAccessCount()
   {
      if (isEnabled())
         return preparedStatementCacheAccessCount.get();

      return 0;
   }

   /**
    * Delta the access count for the prepated statement cache
    */
   public void deltaPreparedStatementCacheAccessCount()
   {
      if (isEnabled())
         preparedStatementCacheAccessCount.incrementAndGet();
   }

   /**
    * Get the add count for the prepated statement cache
    * @return The value
    */
   public long getPreparedStatementCacheAddCount()
   {
      if (isEnabled())
         return preparedStatementCacheAddCount.get();

      return 0;
   }

   /**
    * Delta the add count for the prepated statement cache
    */
   public void deltaPreparedStatementCacheAddCount()
   {
      if (isEnabled())
         preparedStatementCacheAddCount.incrementAndGet();
   }

   /**
    * Get the current size for the prepated statement cache
    * @return The value
    */
   public int getPreparedStatementCacheCurrentSize()
   {
      if (isEnabled())
      {
         Set<PreparedStatementCache> copy = new HashSet<PreparedStatementCache>(psCaches);
         int size = 0;

         for (PreparedStatementCache psc : copy)
         {
            size += psc.size();
         }

         return size;
      }

      return 0;
   }

   /**
    * Get the delete count for the prepated statement cache
    * @return The value
    */
   public long getPreparedStatementCacheDeleteCount()
   {
      if (isEnabled())
         return preparedStatementCacheDeleteCount.get();

      return 0;
   }

   /**
    * Delta the delete count for the prepated statement cache
    */
   public void deltaPreparedStatementCacheDeleteCount()
   {
      if (isEnabled())
         preparedStatementCacheDeleteCount.incrementAndGet();
   }

   /**
    * Get the hit count for the prepated statement cache
    * @return The value
    */
   public long getPreparedStatementCacheHitCount()
   {
      if (isEnabled())
         return preparedStatementCacheHitCount.get();

      return 0;
   }

   /**
    * Delta the hit count for the prepated statement cache
    */
   public void deltaPreparedStatementCacheHitCount()
   {
      if (isEnabled())
         preparedStatementCacheHitCount.incrementAndGet();
   }

   /**
    * Get the miss count for the prepated statement cache
    * @return The value
    */
   public long getPreparedStatementCacheMissCount()
   {
      if (isEnabled())
         return preparedStatementCacheMissCount.get();

      return 0;
   }

   /**
    * Delta the miss count for the prepated statement cache
    */
   public void deltaPreparedStatementCacheMissCount()
   {
      if (isEnabled())
         preparedStatementCacheMissCount.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void clear()
   {
      if (isEnabled())
      {
         preparedStatementCacheAccessCount.set(0);
         preparedStatementCacheAddCount.set(0);
         preparedStatementCacheDeleteCount.set(0);
         preparedStatementCacheHitCount.set(0);
         preparedStatementCacheMissCount.set(0);
      }
   }
}
