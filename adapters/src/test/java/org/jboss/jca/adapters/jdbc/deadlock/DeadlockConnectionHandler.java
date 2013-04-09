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

package org.jboss.jca.adapters.jdbc.deadlock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

/**
 * Deadlock connection handler
 */
class DeadlockConnectionHandler implements InvocationHandler
{
   private static Logger log = Logger.getLogger(DeadlockConnectionHandler.class);
   private Connection connection;

   /**
    * Constructor
    * @param connection The connection
    */
   public DeadlockConnectionHandler(Connection connection)
   {
      this.connection = connection;
   }

   /**
    * {@inheritDoc}
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String methodName = method.getName();
      if ("createStatement".equals(methodName))
      {
         log.info("Forcing deadlock");

         forceDeadlock();

         throw new SQLException("Deadlock should now occur");
      }
      else
      {
         return method.invoke(connection, args);
      }
   }

   private void forceDeadlock() throws SystemException
   {
      final TransactionManager tm = getTransactionManager();
      final Transaction transaction = tm.getTransaction();

      Runnable rollback = new Runnable()
      {
         public void run()
         {
            log.info("Before rollback");
            try
            {
               transaction.rollback();
            }
            catch (Throwable t)
            {
               log.info(t.getMessage(), t);
            }
            finally
            {
               log.info("After rollback");
            }
         }
      };

      Thread thread = new Thread(rollback);
      thread.start();

      try
      {
         Thread.sleep(5000);
      }
      catch (InterruptedException ie)
      {
         // Ignore
      }
   }

   private TransactionManager getTransactionManager()
   {
      Context context = null;
      try
      {
         context = new InitialContext();
         return (TransactionManager)context.lookup("java:/TransactionManager");
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }

      return null;
   }
}
