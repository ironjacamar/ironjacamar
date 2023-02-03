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
import jakarta.resource.spi.ConnectionRequestInfo;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.connections.adapter.TestConnectionRequestInfo;
import org.jboss.jca.core.connectionmanager.pool.api.Capacity;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityDecrementer;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityIncrementer;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.capacity.ExplicitCapacity;
import org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer;
import org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer;
import org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreConcurrentLinkedDequeManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;

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

      while (mcp.getActive() != POOL_SIZE)
      {
         Thread.sleep(100);
      }

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
