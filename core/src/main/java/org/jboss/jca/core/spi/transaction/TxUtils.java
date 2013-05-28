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
package org.jboss.jca.core.spi.transaction;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 * Helper methods for transaction status and textual representation
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TxUtils
{
   /** Transaction status strings */
   private static final String[] TX_STATUS_STRINGS =
   {
      "STATUS_ACTIVE",
      "STATUS_MARKED_ROLLBACK",
      "STATUS_PREPARED",
      "STATUS_COMMITTED",
      "STATUS_ROLLEDBACK",
      "STATUS_UNKNOWN",
      "STATUS_NO_TRANSACTION",
      "STATUS_PREPARING",
      "STATUS_COMMITTING",
      "STATUS_ROLLING_BACK"
   };

   /**
    * No instances
    */
   private TxUtils()
   {
   }

   /**
    * Is the transaction active
    * @param tx The transaction
    * @return True if active; otherwise false
    */
   public static boolean isActive(Transaction tx)
   {
      if (tx == null)
         return false;
      
      try
      {
         int status = tx.getStatus();

         return status == Status.STATUS_ACTIVE;
      }
      catch (SystemException error)
      {
         throw new RuntimeException("Error during isActive()", error);
      }
   }

   /**
    * Is the transaction uncommitted
    * @param tx The transaction
    * @return True if uncommitted; otherwise false
    */
   public static boolean isUncommitted(Transaction tx)
   {
      if (tx == null)
         return false;
      
      try
      {
         int status = tx.getStatus();

         return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
      }
      catch (SystemException error)
      {
         throw new RuntimeException("Error during isUncommitted()", error);
      }
   }

   /**
    * Is the transaction completed
    * @param tx The transaction
    * @return True if completed; otherwise false
    */
   public static boolean isCompleted(Transaction tx)
   {
      if (tx == null)
         return true;
      
      try
      {
         int status = tx.getStatus();

         return status == Status.STATUS_COMMITTED ||
            status == Status.STATUS_ROLLEDBACK ||
            status == Status.STATUS_NO_TRANSACTION;
      }
      catch (SystemException error)
      {
         throw new RuntimeException("Error during isCompleted()", error);
      }
   }

   /**
    * Converts a transaction status to a text representation
    * 
    * @param status The status index
    * @return status as String or "STATUS_INVALID(value)"
    * @see javax.transaction.Status
    */
   public static String getStatusAsString(int status)
   {
      if (status >= Status.STATUS_ACTIVE && status <= Status.STATUS_ROLLING_BACK)
      {
         return TX_STATUS_STRINGS[status];
      }
      else
      {
         return "STATUS_INVALID(" + status + ")";
      }
   }
}
