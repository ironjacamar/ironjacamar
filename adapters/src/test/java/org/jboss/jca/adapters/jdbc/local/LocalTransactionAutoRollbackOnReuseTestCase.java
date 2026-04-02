/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2026, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.local;

import org.jboss.jca.adapters.jdbc.local.testimpl.MockDriver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests that a JTA datasource with auto-commit=false properly rolls back
 * uncommitted local transactions when a connection is returned to the pool
 * and reused.
 *
 * Reproduces the regression introduced by JBJCA-1483: when tidyup() resets
 * localTransaction to false after rollback (correct), but cleanup() did not
 * reset inLocalTransaction in BaseWrapperManagedConnection, the
 * checkTransaction() method on connection reuse would not fire
 * LOCAL_TRANSACTION_STARTED because inLocalTransaction was still true from
 * the previous use. This caused tidyup() to skip the rollback on subsequent
 * reuses of the same pooled connection.
 */
public class LocalTransactionAutoRollbackOnReuseTestCase
{
   private LocalManagedConnectionFactory connectionFactory;
   private Subject subject;

   @Before
   public void setup()
   {
      connectionFactory = new LocalManagedConnectionFactory();
      connectionFactory.setDriverClass(MockDriver.class.getName());
      connectionFactory.setJTA(Boolean.TRUE);

      subject = new Subject();
      PasswordCredential pc = new PasswordCredential("test", "test".toCharArray());
      pc.setManagedConnectionFactory(connectionFactory);
      subject.getPrivateCredentials().add(pc);
   }

   /**
    * Verifies that inLocalTransaction is reset during cleanup(), so that
    * LOCAL_TRANSACTION_STARTED fires on every connection reuse cycle.
    *
    * Without the fix (inLocalTransaction.set(false) in resetProperties()),
    * the second checkTransaction() call does not fire the event, which means
    * TxConnectionListener.localTransaction stays false, and tidyup() skips
    * the rollback — leaving uncommitted CRUD operations on the connection.
    */
   @Test
   public void testLocalTransactionStartedFiresOnConnectionReuse() throws Exception
   {
      LocalManagedConnection mc =
         (LocalManagedConnection) connectionFactory.createManagedConnection(subject, null);

      AtomicInteger txStartedCount = new AtomicInteger(0);
      mc.addConnectionEventListener(new ConnectionEventListener()
      {
         @Override
         public void connectionClosed(ConnectionEvent event) {}

         @Override
         public void localTransactionStarted(ConnectionEvent event)
         {
            txStartedCount.incrementAndGet();
         }

         @Override
         public void localTransactionCommitted(ConnectionEvent event) {}

         @Override
         public void localTransactionRolledback(ConnectionEvent event) {}

         @Override
         public void connectionErrorOccurred(ConnectionEvent event) {}
      });

      // --- First use (simulates first HTTP request) ---

      // Application sets auto-commit = false
      setField(mc, "jdbcAutoCommit", false);

      // Application executes SQL, which triggers checkTransaction()
      invokeCheckTransaction(mc);

      Assert.assertEquals("LOCAL_TRANSACTION_STARTED should fire on first use",
                           1, txStartedCount.get());
      Assert.assertTrue("inLocalTransaction should be true after first checkTransaction()",
                         getInLocalTransaction(mc));

      // Connection returned to pool: TxConnectionListener.tidyup() calls local.rollback()
      mc.getLocalTransaction().rollback();

      // Pool calls cleanup() to prepare the connection for reuse
      mc.cleanup();

      // --- Second use (simulates next HTTP request reusing same pooled connection) ---

      Assert.assertFalse("inLocalTransaction must be false after cleanup() so that " +
                          "checkTransaction() fires LOCAL_TRANSACTION_STARTED on reuse",
                          getInLocalTransaction(mc));

      // Application sets auto-commit = false again
      setField(mc, "jdbcAutoCommit", false);

      // Application executes SQL, which triggers checkTransaction()
      invokeCheckTransaction(mc);

      Assert.assertEquals("LOCAL_TRANSACTION_STARTED must fire again on connection reuse",
                           2, txStartedCount.get());

      // --- Third use (verify the fix works for repeated cycles) ---

      mc.getLocalTransaction().rollback();
      mc.cleanup();

      setField(mc, "jdbcAutoCommit", false);
      invokeCheckTransaction(mc);

      Assert.assertEquals("LOCAL_TRANSACTION_STARTED must fire on every reuse cycle",
                           3, txStartedCount.get());
   }

   /**
    * Verifies that inLocalTransaction is false on a freshly created connection,
    * and that the full cycle (checkTransaction → rollback → cleanup) leaves it
    * false for the next use.
    */
   @Test
   public void testInLocalTransactionResetAfterCleanup() throws Exception
   {
      LocalManagedConnection mc =
         (LocalManagedConnection) connectionFactory.createManagedConnection(subject, null);

      mc.addConnectionEventListener(new NoOpConnectionEventListener());

      // Fresh connection: inLocalTransaction should be false
      Assert.assertFalse("inLocalTransaction should be false initially",
                          getInLocalTransaction(mc));

      // Simulate use with auto-commit=false
      setField(mc, "jdbcAutoCommit", false);
      invokeCheckTransaction(mc);
      Assert.assertTrue("inLocalTransaction should be true after checkTransaction()",
                         getInLocalTransaction(mc));

      // tidyup rollback + cleanup
      mc.getLocalTransaction().rollback();
      mc.cleanup();

      // After cleanup, inLocalTransaction must be reset
      Assert.assertFalse("inLocalTransaction must be reset to false by cleanup()",
                          getInLocalTransaction(mc));
   }

   // --- Reflection helpers ---

   private void setField(LocalManagedConnection mc, String fieldName, Object value) throws Exception
   {
      Field f = findField(mc.getClass(), fieldName);
      f.setAccessible(true);
      f.set(mc, value);
   }

   private boolean getInLocalTransaction(LocalManagedConnection mc) throws Exception
   {
      Field f = findField(mc.getClass(), "inLocalTransaction");
      f.setAccessible(true);
      return ((AtomicBoolean) f.get(mc)).get();
   }

   private void invokeCheckTransaction(LocalManagedConnection mc) throws Exception
   {
      Method m = findMethod(mc.getClass(), "checkTransaction");
      m.setAccessible(true);
      m.invoke(mc);
   }

   private Field findField(Class<?> clazz, String name)
   {
      while (clazz != null)
      {
         try
         {
            return clazz.getDeclaredField(name);
         }
         catch (NoSuchFieldException e)
         {
            clazz = clazz.getSuperclass();
         }
      }
      throw new RuntimeException("Field not found: " + name);
   }

   private Method findMethod(Class<?> clazz, String name)
   {
      while (clazz != null)
      {
         try
         {
            return clazz.getDeclaredMethod(name);
         }
         catch (NoSuchMethodException e)
         {
            clazz = clazz.getSuperclass();
         }
      }
      throw new RuntimeException("Method not found: " + name);
   }

   private static class NoOpConnectionEventListener implements ConnectionEventListener
   {
      @Override
      public void connectionClosed(ConnectionEvent event) {}

      @Override
      public void localTransactionStarted(ConnectionEvent event) {}

      @Override
      public void localTransactionCommitted(ConnectionEvent event) {}

      @Override
      public void localTransactionRolledback(ConnectionEvent event) {}

      @Override
      public void connectionErrorOccurred(ConnectionEvent event) {}
   }
}
