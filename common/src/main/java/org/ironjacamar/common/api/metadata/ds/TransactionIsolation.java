/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.api.metadata.ds;

import java.io.Serializable;

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
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TransactionIsolation implements Comparable<TransactionIsolation>, Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * TRANSACTION_READ_UNCOMMITTED
    */
   public static final TransactionIsolation TRANSACTION_READ_UNCOMMITTED =
      new TransactionIsolation("TRANSACTION_READ_UNCOMMITTED", 1);

   /**
    * TRANSACTION_READ_COMMITTED
    */
   public static final TransactionIsolation TRANSACTION_READ_COMMITTED =
      new TransactionIsolation("TRANSACTION_READ_COMMITTED", 2);

   /**
    * TRANSACTION_REPEATABLE_READ,
    */
   public static final TransactionIsolation TRANSACTION_REPEATABLE_READ =
      new TransactionIsolation("TRANSACTION_REPEATABLE_READ", 4);

   /**
    * TRANSACTION_SERIALIZABLE,
    */
   public static final TransactionIsolation TRANSACTION_SERIALIZABLE =
      new TransactionIsolation("TRANSACTION_SERIALIZABLE", 8);

   /**
    * TRANSACTION_NONE;
    */
   public static final TransactionIsolation TRANSACTION_NONE =
      new TransactionIsolation("TRANSACTION_NONE", 0);

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

   /**
    * {@inheritDoc}
    */
   public int compareTo(TransactionIsolation o)
   {
      return ordinal() - o.ordinal();
   }

   /**
    * Name
    * @return The value
    */
   public String name()
   {
      return name;
   }

   /**
    * Ordinal
    * @return The value
    */
   public int ordinal()
   {
      return constant;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;

      result += 7 * name.hashCode();
      result += 7 * constant;

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object o)
   {
      if (this == o)
         return true;

      if (o == null || !(o instanceof TransactionIsolation))
         return false;

      TransactionIsolation ti = (TransactionIsolation)o;

      if (name != null)
      {
         if (!name.equals(ti.name))
            return false;
      }
      else
      {
         if (ti.name != null)
            return false;
      }

      if (constant != ti.constant)
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return name;
   }

   /**
    * Static method to get an instance
    *
    * @param v The value
    * @return The instance
    */
   public static TransactionIsolation forName(String v)
   {
      if (v != null && !v.trim().equals(""))
      {
         if ("TRANSACTION_READ_UNCOMMITTED".equalsIgnoreCase(v) || "1".equalsIgnoreCase(v))
         {
            return TRANSACTION_READ_UNCOMMITTED;
         }
         else if ("TRANSACTION_READ_COMMITTED".equalsIgnoreCase(v) || "2".equalsIgnoreCase(v))
         {
            return TRANSACTION_READ_COMMITTED;
         }
         else if ("TRANSACTION_REPEATABLE_READ".equalsIgnoreCase(v) || "4".equalsIgnoreCase(v))
         {
            return TRANSACTION_REPEATABLE_READ;
         }
         else if ("TRANSACTION_SERIALIZABLE".equalsIgnoreCase(v) || "8".equalsIgnoreCase(v))
         {
            return TRANSACTION_SERIALIZABLE;
         }
         else if ("TRANSACTION_NONE".equalsIgnoreCase(v) || "0".equalsIgnoreCase(v))
         {
            return TRANSACTION_NONE;
         }
      }
      return null;
   }

   /**
    * Static method to get an instance
    * @param v The value
    * @return The instance
    */
   public static TransactionIsolation valueOf(String v)
   {
      return forName(v);
   }

   /**
    * Custom transaction levels
    * @param n The name
    * @return The value
    */
   public static TransactionIsolation customLevel(String n)
   {
      return new TransactionIsolation(n, Integer.MIN_VALUE);
   }
}
