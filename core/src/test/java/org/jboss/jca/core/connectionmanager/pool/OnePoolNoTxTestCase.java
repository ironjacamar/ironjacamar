/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolStatistics;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;

import java.util.Locale;

import javax.resource.spi.ManagedConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * An OnePoolTestCase.
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public class OnePoolNoTxTestCase extends PoolTestCaseAbstract
{
   /**
    * 
    * deployment
    * 
    * @return archive
    */
   @Deployment
   public static ResourceAdapterArchive deployment()
   {
      return createNoTxDeployment(getBasicIJXml(SimpleManagedConnectionFactory.class.getName()));
   }

   /**
    * 
    * getKeyShouldReturnSameBooleanValuePassedAsSeparateNoTx
    * 
    * @throws Exception in case of unexpected errors
    * 
    */
   @Test
   public void getKeyShouldReturnSameBooleanValuePassedAsSeparateNoTx() throws Exception
   {
      AbstractPool pool = getPool();
      assertTrue((Boolean) pool.getKey(null, null, true));
      assertFalse((Boolean) pool.getKey(null, null, false));
   }

   /**
    * 
    * getManagedConnectionPoolShouldAlwaysReturnTheSameValueForSimilarKey
    * 
    * @throws Exception in case of unexpected errors
    * 
    */
   @Test
   public void getManagedConnectionPoolShouldAlwaysReturnTheSameValueForSimilarKey() throws Exception
   {
      AbstractPool pool = getPool();
      Object key = pool.getKey(null, null, true);
      assertTrue(pool.getManagedConnectionPool(key, null, null) == pool.getManagedConnectionPool(key, null, null));
   }

   /**
    * 
    * getManagedConnectionPoolShouldReturnDifferentValuesForDifferentKeys
    * 
    * @throws Exception in case of unexpected errors
    * 
    */
   @Test
   public void getManagedConnectionPoolShouldReturnDifferentValuesForDifferentKeys() throws Exception
   {
      AbstractPool pool = getPool();
      Object key1 = pool.getKey(null, null, true);
      Object key2 = pool.getKey(null, null, false);
      assertFalse(pool.getManagedConnectionPool(key1, null, null).equals(
         pool.getManagedConnectionPool(key2, null, null)));
   }

   /**
    * 
    * emptyManagedConnectionPoolTest -- should be a noop
    * 
    * @throws Exception in case of unexpected errors
    * 
    */
   @Test
   public void emptyManagedConnectionPoolTest() throws Exception
   {
      AbstractPool pool = getPool();
      Object key1 = pool.getKey(null, null, true);
      Object key2 = pool.getKey(null, null, false);
      ManagedConnectionPool mcp1 = pool.getManagedConnectionPool(key1, null, null);
      ManagedConnectionPool mcp2 = pool.getManagedConnectionPool(key2, null, null);
      assertEquals(pool.getManagedConnectionPools().size(), 2);
      pool.emptyManagedConnectionPool(mcp1);
      assertEquals(pool.getManagedConnectionPools().size(), 2);
      pool.emptyManagedConnectionPool(mcp2);
      assertEquals(pool.getManagedConnectionPools().size(), 2);
   }

   /**
    * 
    * constructorShouldThrowIllegalArgumentExceptionForNullManagedConnectionFactory
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test(expected = IllegalArgumentException.class)
   public void constructorShouldThrowIllegalArgumentExceptionForNullManagedConnectionFactory() throws Exception
   {
      new OnePool(null, null, false, true);
   }

   /**
    * 
    * constructorShouldThrowIllegalArgumentExceptionForNullPoolConfiguration
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test(expected = IllegalArgumentException.class)
   public void constructorShouldThrowIllegalArgumentExceptionForNullPoolConfiguration() throws Exception
   {
      new OnePool(cf.getMCF(), null, false, true);
   }

   /**
    * 
    * flushPoolShouldLeaveSubPoolEmpty
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void flushPoolShouldLeaveSubPoolEmpty() throws Exception
   {
      AbstractPool pool = getPool();
      Object key1 = pool.getKey(null, null, true);
      Object key2 = pool.getKey(null, null, false);
      pool.getManagedConnectionPool(key1, null, null);
      pool.getManagedConnectionPool(key2, null, null);
      assertEquals(pool.getManagedConnectionPools().size(), 2);
      ((OnePool) pool).flush();
      assertEquals(pool.getManagedConnectionPools().size(), 0);
   }

   /**
    * 
    * shutdownShouldLeaveSubPoolEmpty
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void shutdownShouldLeaveSubPoolEmpty() throws Exception
   {
      AbstractPool pool = getPool();
      Object key1 = pool.getKey(null, null, true);
      Object key2 = pool.getKey(null, null, false);
      pool.getManagedConnectionPool(key1, null, null);
      pool.getManagedConnectionPool(key2, null, null);
      assertEquals(pool.getManagedConnectionPools().size(), 2);
      ((OnePool) pool).shutdown();
      assertEquals(pool.getManagedConnectionPools().size(), 0);

   }

   /**
    * 
    * testConnection
    * 
    * @throws Exception in case of unexpected errors
    */
   @Override
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();
      assertTrue(((OnePool) pool).testConnection());
      assertTrue(((OnePool) pool).testConnection(null, null));
      assertTrue(pool.internalTestConnection(null, null));
   }

   /**
    * 
    * testConnection
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void getAndReturnConnectionTest() throws Exception
   {
      AbstractPool pool = getPool();
      pool.flush(true);
      ConnectionListener cl = pool.getConnection(null, null, null);
      log.info("Pools:" + pool.getManagedConnectionPools() + "//CL.pool=" + cl.getManagedConnectionPool());
      ManagedConnectionPool mcp = cl.getManagedConnectionPool();
      assertTrue(pool.getManagedConnectionPools().containsValue(mcp));
      ManagedConnectionPoolStatistics ms = mcp.getStatistics();
      PoolStatistics ps = pool.getStatistics();
      log.info(ps.toString());
      assertEquals(ps.getActiveCount(), 1);
      assertEquals(ms.getActiveCount(), 1);
      ManagedConnection mc = cl.getManagedConnection();
      Object ob = mc.getConnection(null, null);
      cl.registerConnection(ob);
      assertEquals(cl.getNumberOfConnections(), 1);
      assertTrue(ob instanceof SimpleConnection);
      assertEquals(pool.findConnectionListener(mc), cl);
      assertEquals(pool.findConnectionListener(mc, ob), cl);
      assertNull(pool.findConnectionListener(null));
      cl.unregisterConnection(ob);
      assertEquals(cl.getNumberOfConnections(), 0);
      pool.returnConnection(cl, true);
      assertEquals(ps.getActiveCount(), 0);
      assertEquals(ms.getActiveCount(), 0);
      log.info(ms.toString());
   }

   /**
    * 
    * testPoolStatistics
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void testPoolStatistics() throws Exception
   {
      AbstractPool pool = getPool();
      PoolStatistics ps = pool.getStatistics();
      ps.setEnabled(false);
      assertFalse(ps.isEnabled());
      for (String name : ps.getNames())
      {
         assertNotNull(ps.getDescription(name));
         assertNotNull(ps.getValue(name));
         assertNotNull(ps.getType(name));
      }
      assertNull(ps.getValue(null));
      assertEquals(ps.getDescription("WaitCount", Locale.TRADITIONAL_CHINESE), ps.getDescription("WaitCount"));
      ps.clear();
      ps.setEnabled(true);
      assertTrue(ps.isEnabled());
   }

   /**
    * 
    * testMCPoolStatistics
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void testMCPoolStatistics() throws Exception
   {
      AbstractPool pool = getPool();
      Object key = pool.getKey(null, null, false);
      ManagedConnectionPool mcp = pool.getManagedConnectionPool(key, null, null);
      ManagedConnectionPoolStatistics mcps = mcp.getStatistics();
      mcps.setEnabled(false);
      assertTrue(mcps.isEnabled());
      for (String name : mcps.getNames())
      {
         assertNotNull(mcps.getDescription(name));
         assertNotNull(mcps.getValue(name));
         assertNotNull(mcps.getType(name));
      }
      assertNull(mcps.getValue(null));
      assertEquals(mcps.getDescription("WaitCount", Locale.TRADITIONAL_CHINESE), mcps.getDescription("WaitCount"));
      mcps.clear();
      mcps.setEnabled(true);
      assertTrue(mcps.isEnabled());
   }

   /**
    * 
    * testPrefill
    * 
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void testPrefill() throws Exception
   {
      AbstractPool pl = getPool();
      PoolConfiguration pc = new PoolConfiguration();
      pc.setPrefill(true);
      pc.setMaxSize(5);
      pc.setMinSize(3);
      AbstractPrefillPool pool = new OnePool(pl.getManagedConnectionFactory(), pc, true, pl.isSharable());
      pool.setConnectionListenerFactory(pl.getConnectionListenerFactory());
      pool.setName("Prefilled");
      pool.flush();
      pool.prefill(null, null, true);
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      PoolStatistics ps = pool.getStatistics();
      Thread.sleep(1000);
      assertEquals(ps.getAvailableCount(), 5);
      assertEquals(ps.getActiveCount(), 3);
      for (ManagedConnectionPool mcp : pool.getManagedConnectionPools().values())
      {
         assertFalse(mcp.isEmpty());
         assertTrue(mcp.isRunning());
         ps = mcp.getStatistics();

         assertEquals(ps.getAvailableCount(), 5);
         assertEquals(ps.getActiveCount(), 3);
      }
   }

}
