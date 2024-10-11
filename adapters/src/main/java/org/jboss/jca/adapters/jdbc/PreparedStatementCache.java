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

package org.jboss.jca.adapters.jdbc;

import org.jboss.jca.adapters.jdbc.statistics.JdbcStatisticsPlugin;
import org.jboss.jca.adapters.jdbc.util.Cache;
import org.jboss.jca.adapters.jdbc.util.CacheListener;
import org.jboss.jca.adapters.jdbc.util.LRUCache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Predicate;

import org.jboss.logging.Logger;


/**
 * Cache for PreparedStatements. When ps ages out, close it.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@SuppressWarnings("unchecked")
public class PreparedStatementCache implements CacheListener<CachedPreparedStatement>
{
   private final Logger log = Logger.getLogger(getClass());

   private Cache<Key, CachedPreparedStatement> cache;
   private JdbcStatisticsPlugin statistics;

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
      this.cache = new LRUCache<Key, CachedPreparedStatement>(max);
      this.cache.setListener(this);
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
      cache.insert(key, value);
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
    * {@inheritDoc}
    */
   public void onEviction(CachedPreparedStatement evicted)
   {
      if (evicted != null)
      {
         try
         {
            evicted.agedOut();
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

   void flush(Predicate<CachedPreparedStatement> predicate)
   {
      cache.flush(predicate);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("PreparedStatementCache size: ").append(size()).append(" ");
      sb.append(cache);
      return sb.toString();
   }
}
