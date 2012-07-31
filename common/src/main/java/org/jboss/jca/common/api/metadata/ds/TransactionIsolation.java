/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.api.metadata.ds;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A TransactionIsolation.
 * Define constants used as the possible
 *        transaction isolation levels
 *       in transaction-isolation type.
 *       Include: TRANSACTION_READ_UNCOMMITTED
 *       TRANSACTION_READ_COMMITTED
 *       TRANSACTION_REPEATABLE_READ TRANSACTION_SERIALIZABLE
 *       TRANSACTION_NONE
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public enum TransactionIsolation
{
   /**
    * TRANSACTION_READ_UNCOMMITTED,
    */
   TRANSACTION_READ_UNCOMMITTED("TRANSACTION_READ_UNCOMMITTED", 1),

   /**
    * TRANSACTION_READ_COMMITTED,
    */
   TRANSACTION_READ_COMMITTED("TRANSACTION_READ_COMMITTED", 2),

   /**
    * TRANSACTION_REPEATABLE_READ,
    */
   TRANSACTION_REPEATABLE_READ("TRANSACTION_REPEATABLE_READ", 4),

   /**
    * TRANSACTION_SERIALIZABLE,
    */
   TRANSACTION_SERIALIZABLE("TRANSACTION_SERIALIZABLE", 8),

   /**
    * TRANSACTION_NONE;
    */
   TRANSACTION_NONE("TRANSACTION_NONE", 0);

   private static final Map<String, TransactionIsolation> MAP;
   private String name;
   private int constant;

   /**
    * Constructor
    * @param n The name
    * @param c The constant
    */
   TransactionIsolation(String n, int c)
   {
      this.name = n;
      this.constant = c;
   }

   static
   {
      final Map<String, TransactionIsolation> map = new HashMap<String, TransactionIsolation>();
      for (TransactionIsolation v : values())
      {
         String name = v.name();
         if (name != null)
         {
            map.put(name, v);
            map.put(Integer.toString(v.getConstant()), v);
         }
      }
      MAP = map;
   }

   /**
    * Get the constant
    * @return The value
    */
   int getConstant()
   {
      return constant;
   }

   /**
    * Static method to get enum instance
    *
    * @param v The value
    * @return The enum instance
    */
   public static TransactionIsolation forName(String v)
   {
      return MAP.get(v);
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return name;
   }
}
