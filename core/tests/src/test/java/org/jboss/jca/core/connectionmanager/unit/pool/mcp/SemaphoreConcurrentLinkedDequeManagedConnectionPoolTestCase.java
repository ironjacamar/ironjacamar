/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2018, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.unit.pool.mcp;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.connections.adapter.TestConnectionRequestInfo;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.api.Capacity;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityDecrementer;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityIncrementer;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.capacity.ExplicitCapacity;
import org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer;
import org.jboss.jca.core.connectionmanager.pool.capacity.TimedOutDecrementer;
import org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer;
import org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreConcurrentLinkedDequeManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SemaphoreConcurrentLinkedDequeManagedConnectionPoolTestCase
{
   private static final int POOL_SIZE = 5;
   boolean fail = false;
   private FailingManagedCF mcf;
   private Pool pool;
   private ConnectionManager cm;
   private PoolConfiguration poolConfig;

   @Before public void setUp() throws Exception
   {
      mcf = new FailingManagedCF();
      poolConfig = prefillPoolConfiguration();
      pool = new OnePool(mcf, poolConfig, false, false, "test");
      cm = new TestConnectionManager(pool);
   }

   /*
    * Failing validation causes SemaphoreConcurrentLinkedDequeManagedConnectionPool#removeConnectionListenerFromPool
    * to be called twice during getConnection call.
    * See https://issues.jboss.org/browse/JBJCA-1385 for details.
    */
   @Test public void testRemovingTheSameConnectionTwice() throws Exception
   {
      SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp = new SemaphoreConcurrentLinkedDequeManagedConnectionPool();
      mcp.initialize(mcf, cm, null, null, poolConfig, pool);

      waitForChangesToPropagate(mcp);

      mcf.setFailing(true);

      try
      {
         mcp.getConnection(null, null);
      } catch (ResourceException e)
      {
         // ignore
      }

      Assert.assertEquals("Only a single conenction should have been removed", POOL_SIZE - 1, mcp.getActive());
   }

   @Test public void testIncreaseCapacity() throws Exception
   {
      final SizeIncrementer sizeIncrementer = new SizeIncrementer();
      sizeIncrementer.setSize(4);
      final WatermarkDecrementer watermarkDecrementer = new WatermarkDecrementer();
      watermarkDecrementer.setWatermark(1);
      pool.setCapacity(new ExplicitCapacity(sizeIncrementer, watermarkDecrementer));
      poolConfig.setPrefill(false);
      poolConfig.setStrictMin(false);
      SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp = new SemaphoreConcurrentLinkedDequeManagedConnectionPool();
      mcp.initialize(mcf, cm, null, null, poolConfig, pool);

      // notice sizeIncrementer.size is 4, but we expect 3 as the final pool size result after increaseCapacity
      // the reason for this is that increaseCapacity assumes that 1 connection has been created before it is invoked
      // so it creates n - 1 connections to make it up for that.
      // the assumption is correct because the connection request info, that triggers increaseCapacity call
      // is invoked by getConnection method, after a first connection is created.
      // as a conclusion, for the test purposes, we need to assert that the pool size is 3
      ConnectionRequestInfo connectionRequestInfo = new TestConnectionRequestInfo();
      mcp.increaseCapacity(null, connectionRequestInfo);
      Assert.assertEquals(3, mcp.getActive());

      mcp.increaseCapacity(null, connectionRequestInfo);
      Assert.assertEquals(5, mcp.getActive());

      mcp.increaseCapacity(null, connectionRequestInfo);
      Assert.assertEquals(5, mcp.getActive());

      mcp.increaseCapacity(null, connectionRequestInfo);
      Assert.assertEquals(5, mcp.getActive());

      mcp.increaseCapacity(null, connectionRequestInfo);
      Assert.assertEquals(5, mcp.getActive());
   }

   @Test public void testFillTo() throws Exception
   {
      final SizeIncrementer sizeIncrementer = new SizeIncrementer();
      pool.setCapacity(null);
      poolConfig.setPrefill(true);
      // prevent pool filler thread from running in parallel with the test, fillTo was not designed to be run concurrently
      poolConfig.setMinSize(0);
      poolConfig.setStrictMin(true);
      SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp = new SemaphoreConcurrentLinkedDequeManagedConnectionPool();
      mcp.initialize(mcf, cm, null, null, poolConfig, pool);

      ConnectionRequestInfo connectionRequestInfo = new TestConnectionRequestInfo();
      mcp.fillTo(3);
      Assert.assertEquals(3, mcp.getActive());

      mcp.fillTo(5);
      Assert.assertEquals(5, mcp.getActive());

      mcp.fillTo(5);
      Assert.assertEquals(5, mcp.getActive());

      mcp.fillTo(5);
      Assert.assertEquals(5, mcp.getActive());

      mcp.fillTo(5);
      Assert.assertEquals(5, mcp.getActive());
   }

   @Test public void testPrefillPoolAfterReturnedConnectionHasBeenDestroyed() throws ResourceException, InterruptedException
   {
      poolConfig.setValidateOnMatch(false);
      poolConfig.setUseFastFail(false);
      poolConfig.setInitialSize(POOL_SIZE);
      final SizeIncrementer sizeIncrementer = new SizeIncrementer();
      final TimedOutDecrementer timedOutDecrementer = new TimedOutDecrementer();
      pool.setCapacity(new ExplicitCapacity(sizeIncrementer, timedOutDecrementer));
      SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp = new SemaphoreConcurrentLinkedDequeManagedConnectionPool();
      mcp.initialize(mcf, cm, null, null, poolConfig, pool);

      waitForChangesToPropagate(mcp);
      Assert.assertEquals(POOL_SIZE, mcp.getActive());

      ConnectionListener cl = mcp.getConnection(null, null);
      mcp.returnConnection(cl, true);

      waitForChangesToPropagate(mcp);
      Assert.assertEquals(POOL_SIZE, mcp.getActive());
   }

   @Test public void testReturnConnectionDestroyOutsideLock() throws Exception
   {
      poolConfig.setValidateOnMatch(false);
      poolConfig.setUseFastFail(false);
      poolConfig.setInitialSize(1);
      poolConfig.setMinSize(1);
      poolConfig.setMaxSize(1);
      poolConfig.setPrefill(true);
      poolConfig.setStrictMin(true);

      SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp = new SemaphoreConcurrentLinkedDequeManagedConnectionPool();
      mcp.initialize(mcf, cm, null, null, poolConfig, pool);

      waitForChangesToPropagate(mcp, 1);

      ConnectionListener cl = mcp.getConnection(null, null);
      final Object monitor = new Object();
      final CountDownLatch removeListenerCalled = new CountDownLatch(1);
      final CountDownLatch workerCanFinish = new CountDownLatch(1);
      final AtomicBoolean deadlocked = new AtomicBoolean(false);

      SimpleManagedConnection smc = (SimpleManagedConnection) cl.getManagedConnection();

      Thread workerThread = new Thread(() -> {
         synchronized (monitor)
         {
            removeListenerCalled.countDown();
            try
            {
               workerCanFinish.await(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
            try
            {
               mcp.returnConnection(cl, true);
            }
            catch (Exception e)
            {
               // expected
            }
         }
      }, "SimulatedRAWorker");

      smc.removeConnectionEventListener(new ConnectionEventListener()
      {
         @Override public void connectionClosed(jakarta.resource.spi.ConnectionEvent event) {}
         @Override public void connectionErrorOccurred(jakarta.resource.spi.ConnectionEvent event) {}
         @Override public void localTransactionStarted(jakarta.resource.spi.ConnectionEvent event) {}
         @Override public void localTransactionCommitted(jakarta.resource.spi.ConnectionEvent event) {}
         @Override public void localTransactionRolledback(jakarta.resource.spi.ConnectionEvent event) {}
      });

      workerThread.start();
      Assert.assertTrue("Worker thread should have started", removeListenerCalled.await(5, TimeUnit.SECONDS));

      Thread returnThread = new Thread(() -> {
         try
         {
            mcp.returnConnection(cl, true);
         }
         catch (Exception e)
         {
            // expected
         }
      }, "ReturnConnectionThread");

      returnThread.start();
      workerCanFinish.countDown();

      returnThread.join(5000);
      workerThread.join(5000);

      if (returnThread.isAlive() || workerThread.isAlive())
      {
         deadlocked.set(true);
         returnThread.interrupt();
         workerThread.interrupt();
      }

      Assert.assertFalse("Deadlock detected: returnConnection should not deadlock with external RA monitors",
            deadlocked.get());
   }

   @Test public void testConcurrentReturnConnectionAndConnectionErrorOccurred() throws Exception
   {
      poolConfig.setValidateOnMatch(false);
      poolConfig.setUseFastFail(false);
      poolConfig.setInitialSize(2);
      poolConfig.setMinSize(2);
      poolConfig.setMaxSize(2);
      poolConfig.setPrefill(true);
      poolConfig.setStrictMin(true);

      SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp = new SemaphoreConcurrentLinkedDequeManagedConnectionPool();
      mcp.initialize(mcf, cm, null, null, poolConfig, pool);

      waitForChangesToPropagate(mcp, 2);

      ConnectionListener cl1 = mcp.getConnection(null, null);
      ConnectionListener cl2 = mcp.getConnection(null, null);

      final CountDownLatch bothReady = new CountDownLatch(2);
      final CountDownLatch go = new CountDownLatch(1);
      final AtomicBoolean deadlocked = new AtomicBoolean(false);

      Thread thread1 = new Thread(() -> {
         bothReady.countDown();
         try
         {
            go.await(5, TimeUnit.SECONDS);
            mcp.returnConnection(cl1, true);
         }
         catch (Exception e)
         {
            // expected
         }
      }, "ReturnConnection-1");

      Thread thread2 = new Thread(() -> {
         bothReady.countDown();
         try
         {
            go.await(5, TimeUnit.SECONDS);
            mcp.returnConnection(cl2, true);
         }
         catch (Exception e)
         {
            // expected
         }
      }, "ReturnConnection-2");

      thread1.start();
      thread2.start();

      Assert.assertTrue("Both threads should be ready", bothReady.await(5, TimeUnit.SECONDS));
      go.countDown();

      thread1.join(5000);
      thread2.join(5000);

      if (thread1.isAlive() || thread2.isAlive())
      {
         deadlocked.set(true);
         thread1.interrupt();
         thread2.interrupt();
      }

      Assert.assertFalse("Deadlock detected: concurrent returnConnection with kill=true should not deadlock",
            deadlocked.get());
   }

   private static void waitForChangesToPropagate(SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp) throws InterruptedException
   {
      waitForChangesToPropagate(mcp, POOL_SIZE);
   }

   private static void waitForChangesToPropagate(SemaphoreConcurrentLinkedDequeManagedConnectionPool mcp, int expectedSize) throws InterruptedException
   {
      int maxWait = 50;
      while (mcp.getActive() != expectedSize && maxWait-- > 0)
      {
         Thread.sleep(100);
      }
   }

   private PoolConfiguration incrementerPoolConfiguration()
   {
      PoolConfiguration pc = new PoolConfiguration();
      pc.setPrefill(false);
      pc.setStrictMin(false);
      pc.setMinSize(1);
      pc.setMaxSize(5);
      pc.setValidateOnMatch(true);
      pc.setUseFastFail(true);

      return pc;
   }

   private PoolConfiguration prefillPoolConfiguration()
   {
      PoolConfiguration pc = new PoolConfiguration();
      pc.setPrefill(true);
      pc.setStrictMin(true);
      pc.setMinSize(POOL_SIZE);
      pc.setMaxSize(POOL_SIZE);
      pc.setValidateOnMatch(true);
      pc.setUseFastFail(true);
      return pc;
   }
}
