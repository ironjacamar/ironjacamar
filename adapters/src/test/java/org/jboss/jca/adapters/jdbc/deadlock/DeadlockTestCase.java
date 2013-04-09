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

package org.jboss.jca.adapters.jdbc.deadlock;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for dead locks
 */
public class DeadlockTestCase
{
   private static Logger log = Logger.getLogger(DeadlockTestCase.class);

   private static Embedded embedded;
   private static TransactionManager tm;

   /**
    * Deadlock with database enabled
    * @exception Throwable If an error occurs
    */
   @Test
   public void testDeadlockDBEnabled() throws Throwable
   {
      Context context = null;

      URL jdbc = DeadlockTestCase.class.getClassLoader().getResource("jdbc-local.rar");
      URL deadlock = DeadlockTestCase.class.getClassLoader().getResource("deadlock-ds.xml");

      embedded.deploy(jdbc);      
      embedded.deploy(deadlock);

      tm.begin();
      try
      {
         Transaction transaction = tm.getTransaction();
            
         context = new InitialContext();
         
         DataSource ds = (DataSource)context.lookup("java:/DeadlockDS");
         Connection connection = ds.getConnection();

         transaction.enlistResource(new ConnectionCloseXAResource(connection));
            
         // Now trigger everything.
         connection.createStatement();
      }
      catch (Throwable t)
      {
         log.info("Throwable during testDeadlockDBEnabled: " + t.getMessage(), t);
      }
      finally
      {
         log.info("Rolling back in testDeadlockDBEnabled");
         try
         {
            tm.rollback();
         }
         finally
         {
            log.info("Completed rollback in testDeadlockDBEnabled");
         }

         embedded.undeploy(deadlock);
         embedded.undeploy(jdbc);
      }
   }

   /**
    * Deadlock with database disabled
    * @exception Throwable If an error occurs
    */
   @Test
   public void testDeadlockDBDisabled() throws Throwable
   {
      Context context = null;

      URL jdbc = DeadlockTestCase.class.getClassLoader().getResource("jdbc-local.rar");
      URL deadlock = DeadlockTestCase.class.getClassLoader().getResource("deadlock-ds.xml");

      embedded.deploy(jdbc);      
      embedded.deploy(deadlock);

      tm.begin();
      try
      {
         Transaction transaction = tm.getTransaction();
            
         context = new InitialContext();
         
         DataSource ds = (DataSource)context.lookup("java:/DeadlockDS");
         Connection connection = ds.getConnection();

         transaction.enlistResource(new ConnectionCloseXAResource(connection));
            
         embedded.undeploy(deadlock);

         // Now trigger everything.
         connection.createStatement();
      }
      catch (Throwable t)
      {
         log.info("Throwable during testDeadlockDBDisabled: " + t.getMessage(), t);
      }
      finally
      {
         log.info("Rolling back in testDeadlockDBDisabled");
         try
         {
            tm.rollback();
         }
         finally
         {
            log.info("Completed rollback in testDeadlockDBDisabled");
         }

         embedded.undeploy(jdbc);
      }
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void before() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create();

      // Startup
      embedded.startup();

      // Transaction Manager
      tm = embedded.lookup("RealTransactionManager", TransactionManager.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void after() throws Throwable
   {
      // Reset TM reference
      tm = null;

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
