/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.extensions.novendor;

import org.jboss.jca.adapters.jdbc.util.LRUCache;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * GoodForSeconds connection validation
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class GoodForSecondsValidConnectionChecker extends JDBC4ValidConnectionChecker
{
   private int goodForSeconds;
   private int maxKeepSize;
   private LRUCache<Integer, Long> cache;

   /**
    * Constructor
    */
   public GoodForSecondsValidConnectionChecker()
   {
      goodForSeconds = 5;
      maxKeepSize = 20;
      cache = null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SQLException isValidConnection(Connection c)
   {
      if (cache == null)
         cache = new LRUCache<Integer, Long>(maxKeepSize);

      long now = System.currentTimeMillis();
      Integer identifier = Integer.valueOf(System.identityHashCode(c));
      Long lastChecked = cache.get(identifier);

      if (lastChecked != null)
      {
         if ((lastChecked.longValue() + (goodForSeconds * 60000L)) < now)
         {
            return null;
         }
      }

      SQLException result = super.isValidConnection(c);

      if (result == null)
      {
         cache.insert(identifier, Long.valueOf(now));
      }
      else
      {
         cache.remove(identifier);
      }

      return result;
   }

   /**
    * Set the good for seconds
    * @param v The value
    */
   public void setGoodForSeconds(int v)
   {
      this.goodForSeconds = v;
   }

   /**
    * Set the max keep size
    * @param v The value
    */
   public void setMaxKeepSize(int v)
   {
      this.maxKeepSize = v;
   }
}
