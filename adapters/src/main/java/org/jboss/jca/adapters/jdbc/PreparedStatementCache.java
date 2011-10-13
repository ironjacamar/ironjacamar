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

package org.jboss.jca.adapters.jdbc;

import org.jboss.jca.adapters.jdbc.statistics.JdbcStatisticsPlugin;
import org.jboss.jca.adapters.jdbc.util.BoundedConcurrentHashMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.jboss.logging.Logger;


/**
 * Cache for PreparedStatements. When ps ages out, close it.
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@SuppressWarnings("unchecked")
public class PreparedStatementCache
{
   private static BoundedConcurrentHashMap.Eviction evictionPolicy;

   private final Logger log = Logger.getLogger(getClass());

   private BoundedConcurrentHashMap<Key, CachedPreparedStatement> cache;
   private JdbcStatisticsPlugin statistics;

   static
   {
      String cache = SecurityActions.getSystemProperty("ironjacamar.jdbc.cache");
      if (cache != null)
      {
         cache = cache.trim();

         if ("LRU".equals(cache))
         {
            evictionPolicy = BoundedConcurrentHashMap.Eviction.LRU;
         }
         else if ("LRU_OLD".equals(cache))
         {
            evictionPolicy = BoundedConcurrentHashMap.Eviction.LRU_OLD;
         }
         else if ("LIRS".equals(cache))
         {
            evictionPolicy = BoundedConcurrentHashMap.Eviction.LIRS;
         }         
      }
       
      if (evictionPolicy == null)
         evictionPolicy = BoundedConcurrentHashMap.Eviction.LRU_OLD;
   }

   /**
    * Key class
    */
   public static class Key
   {
      /** Prepared statement cache */
      public static final int PREPARED_STATEMENT = 1;

      /** Callable statement cache */
      public static final int CALLABLE_STATEMENT = 2;

      private final String sql;
      private final int type;
      private final int resultSetType;
      private final int resultSetConcurrency;

      /**
       * Constructor
       * @param sql The SQL string
       * @param type The type
       * @param resultSetType The result set type
       * @param resultSetConcurrency The result set concurrency
       */
      public Key(String sql, int type, int resultSetType, int resultSetConcurrency)
      {
         this.sql = sql;
         this.type = type;
         this.resultSetType = resultSetType;
         this.resultSetConcurrency = resultSetConcurrency;
      }

      /**
       * Get the SQL string
       * @return The value
       */
      public String getSql()
      {
         return sql;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean equals(Object o)
      {
         if (this == o) return true;
         if (o == null || !(o instanceof Key)) return false;

         final Key key = (Key) o;

         if (resultSetConcurrency != key.resultSetConcurrency) return false;
         if (resultSetType != key.resultSetType) return false;
         if (type != key.type) return false;
         return !(sql != null ? !sql.equals(key.sql) : key.sql != null);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public int hashCode()
      {
         int result;
         result = (sql != null ? sql.hashCode() : 0);
         result = 29 * result + type;
         result = 29 * result + resultSetType;
         result = 29 * result + resultSetConcurrency;
         return result;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String toString()
      {
         StringBuffer tmp = new StringBuffer(super.toString());
         tmp.append('[');
         tmp.append("sql=");
         tmp.append(sql);
         tmp.append(" type=");
         tmp.append(type == PREPARED_STATEMENT ? "PS" : "CS");
         tmp.append(" resultSetType=");
         switch (resultSetType)
         {
            case ResultSet.TYPE_FORWARD_ONLY:
            {
               tmp.append("TYPE_FORWARD_ONLY");
               break;
            }
            case ResultSet.TYPE_SCROLL_INSENSITIVE:
            {
               tmp.append("TYPE_SCROLL_INSENSITIVE");
               break;
            }
            case ResultSet.TYPE_SCROLL_SENSITIVE:
            {
               tmp.append("TYPE_SCROLL_SENSITIVE");
               break;
            }
            default:
               tmp.append(resultSetType);
         }
         tmp.append(" resultSetConcurrency=");
         switch (resultSetConcurrency)
         {
            case ResultSet.CONCUR_READ_ONLY:
            {
               tmp.append("CONCUR_READ_ONLY");
               break;
            }
            case ResultSet.CONCUR_UPDATABLE:
            {
               tmp.append("CONCUR_UPDATABLE");
               break;
            }
            default:
               tmp.append(resultSetConcurrency);
         }
         tmp.append(']');
         return tmp.toString();
      }
   }

   /**
    * Constructor
    * @param max The max value
    * @param stats The statistics plugin
    */
   public PreparedStatementCache(int max, JdbcStatisticsPlugin stats)
   {
      BoundedConcurrentHashMap.EvictionListener evictionListener =
         new PreparedStatementEvictionListener(stats, log);

      this.cache =
         new BoundedConcurrentHashMap<Key, CachedPreparedStatement>(max, 16, 
                                                                    evictionPolicy,
                                                                    evictionListener);
      this.statistics = stats;
   }

   /**
    * Get
    * @param key The key
    * @return The value
    */
   public CachedPreparedStatement get(Key key)
   {
      return cache.get(key);
   }

   /**
    * Put
    * @param key The key
    * @param value The value
    */
   public void put(Key key, CachedPreparedStatement value)
   {
      cache.put(key, value);
   }

   /**
    * Size
    * @return The size
    */
   public int size()
   {
      return cache.size();
   }

   /**
    * Prepared statement eviction listener
    */
   static class PreparedStatementEvictionListener implements BoundedConcurrentHashMap.EvictionListener
   {
      private JdbcStatisticsPlugin statistics;
      private Logger log;

      /**
       * Constructor
       * @param stats The statistics
       * @param log The logger
       */
      public PreparedStatementEvictionListener(JdbcStatisticsPlugin stats, Logger log)
      {
         this.statistics = stats;
         this.log = log;
      }

      /**
       * Entry eviction
       * @param evicted The entry
       */
      public void onEntryEviction(Map evicted)
      {
         if (evicted != null)
         {
            Iterator it = evicted.values().iterator();
            while (it.hasNext())
            {
               try
               {
                  CachedPreparedStatement ws = (CachedPreparedStatement)it.next();
                  ws.agedOut();
               }
               catch (SQLException e)
               {
                  log.debug("Failed closing cached statement", e);
               }
               finally
               {
                  statistics.deltaPreparedStatementCacheDeleteCount();
               }
            }
         }
      }

      /**
       * {@inheritDoc}
       */
      public void onEntryChosenForEviction(Object internalCacheEntry)
      {
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("PreparedStatementCache size: ").append(size()).append(" ");
      Iterator<Key> it = cache.keySet().iterator();
      while (it.hasNext())
      {
         Key key = it.next();
         sb.append("[").append(key.getSql()).append("] ");
      }
      return sb.toString();
   }
}
