/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.rhq.test;

import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.api.management.DataSource;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.spi.statistics.StatisticsPlugin;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.embed.core.EmbeddedJcaDiscover;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import javax.naming.InitialContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class StatisticsTestCase
{
   
   /** deployed url */
   private static URL deployedUrl;
   
   /** deployed ds */
   private static URL ds;
   

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      EmbeddedJcaDiscover jca = EmbeddedJcaDiscover.getInstance();
      jca.start();
      deployedUrl = DsTestCase.class.getResource("/jdbc-local.rar");
      jca.deploy(deployedUrl);
      ds = DsTestCase.class.getResource("/h2-ds.xml");
      jca.deploy(ds);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      EmbeddedJcaDiscover jca = EmbeddedJcaDiscover.getInstance();
      jca.undeploy(ds);
      jca.undeploy(deployedUrl);
   }
   
   /**
    * Gets the associated DataSource
    * @return datasource
    */
   private DataSource getDataSource()
   {
      ManagementRepository manRepo = ManagementRepositoryManager.getManagementRepository();
      List<DataSource> datasources = manRepo.getDataSources();
      assertEquals(1, datasources.size());
      DataSource ds = datasources.get(0);
      return ds;
   }

   /**
    * Tests the enabled state of the PoolStatistics.
    * 
    * @throws Throwable the exception
    */
   @Test
   public void testPoolStatisticsEnable() throws Throwable
   {
      DataSource ds = getDataSource();
      PoolStatistics poolStatistics = ds.getPool().getStatistics();
      boolean oldEnabled = poolStatistics.isEnabled();
      assertTrue(oldEnabled);
      
      poolStatistics.setEnabled(false); // set enabled to false
      
      assertEquals(0, poolStatistics.getCreatedCount());
      assertEquals(0, poolStatistics.getActiveCount());
      assertEquals(0, poolStatistics.getMaxUsedCount());
      assertEquals(0, poolStatistics.getDestroyedCount());
      assertEquals(0, poolStatistics.getAvailableCount());
      assertTrue(poolStatistics.getMaxWaitCount() >= 0);
      
      InitialContext context = new InitialContext();
      javax.sql.DataSource sqlDS = (javax.sql.DataSource)context.lookup("java:/H2DS");
      Connection sqlConn = sqlDS.getConnection();
      
      poolStatistics.setEnabled(oldEnabled);
      
      // core statistics should NOT be disabled.
      assertTrue(poolStatistics.getCreatedCount() >= 1);
      assertTrue(poolStatistics.getActiveCount() >= 1);
      assertTrue(poolStatistics.getMaxUsedCount() >= 1);
      assertTrue(poolStatistics.getDestroyedCount() >= 0);
      assertTrue(poolStatistics.getAvailableCount() >= 1);
      assertTrue(poolStatistics.getMaxWaitCount() >= 0);
      
      sqlConn.close();
      
      poolStatistics.clear();
      // core statistics should NOT be cleared
      assertTrue(poolStatistics.getCreatedCount() >= 1);
      assertTrue(poolStatistics.getActiveCount() >= 1);
      assertTrue(poolStatistics.getMaxUsedCount() >= 1);
      assertTrue(poolStatistics.getDestroyedCount() >= 0);
      assertTrue(poolStatistics.getAvailableCount() >= 1);
      assertTrue(poolStatistics.getMaxWaitCount() >= 0);
      
   }
   
   /**
    * Tests DataSource PoolStatistics
    * 
    * @throws Throwable the exception
    */
   @Test
   public void testDsPoolStatistics() throws Throwable
   {
      DataSource ds = getDataSource();
      PoolStatistics poolStatistics = ds.getPool().getStatistics();
      
      int createdCount = poolStatistics.getCreatedCount();
      int activeCount = poolStatistics.getActiveCount();
      int maxUsedCount = poolStatistics.getMaxUsedCount();
      int availableCount = poolStatistics.getAvailableCount();
      
      assertTrue(createdCount >= 0);
      assertTrue(activeCount >= 0);
      assertTrue(maxUsedCount >= 0);
      assertTrue(poolStatistics.getDestroyedCount() >= 0);
      assertTrue(availableCount >= 0);
      
      InitialContext context = new InitialContext();
      javax.sql.DataSource sqlDS = (javax.sql.DataSource)context.lookup("java:/H2DS");
      Connection sqlConn = sqlDS.getConnection();
      Connection sqlConn2 = sqlDS.getConnection();
      
      assertTrue(poolStatistics.getCreatedCount() >= createdCount + 1);
      assertTrue(poolStatistics.getActiveCount() >= activeCount + 1);
      assertTrue(poolStatistics.getMaxUsedCount() >= maxUsedCount + 1);
      assertTrue(poolStatistics.getDestroyedCount() >= 0);
      assertTrue(poolStatistics.getAvailableCount() < availableCount);
      assertTrue(poolStatistics.getMaxWaitCount() >= 0);
      
      assertTrue(poolStatistics.getAverageCreationTime() > 0);
      assertTrue(poolStatistics.getAverageBlockingTime() >= 0);
      assertTrue(poolStatistics.getMaxCreationTime() > 0);
      assertTrue(poolStatistics.getTimedOut() >= 0);
      assertTrue(poolStatistics.getTotalBlockingTime() >= 0);
      assertTrue(poolStatistics.getTotalCreationTime() > 0);
      
      sqlConn.close();
      sqlConn2.close();
   }
   
   /**
    * Tests DataSource Statistics
    * 
    * @throws Throwable exception
    */
   @Test
   public void testDsStatistics() throws Throwable
   {
      DataSource ds = getDataSource();
      StatisticsPlugin statistics = ds.getStatistics();
      assertNotNull(statistics);
      assertTrue(statistics.isEnabled());
      
      PoolStatistics poolStatistics = ds.getPool().getStatistics();
      assertNotNull(poolStatistics);
      boolean oldEnable = poolStatistics.isEnabled();
      poolStatistics.setEnabled(false);
      
      assertFalse(poolStatistics.isEnabled()); 
      
      // ds statistics
      assertEquals(0L, statistics.getValue("PreparedStatementCacheAccessCount"));
      assertEquals(0L, statistics.getValue("PreparedStatementCacheAddCount"));
      assertEquals(0, statistics.getValue("PreparedStatementCacheCurrentSize"));
      assertEquals(0L, statistics.getValue("PreparedStatementCacheDeleteCount"));
      assertEquals(0, statistics.getValue("PreparedStatementCacheHitCount"));
      assertEquals(0, statistics.getValue("PreparedStatementCacheMissCount"));
      
      InitialContext context = new InitialContext();
      javax.sql.DataSource sqlDS = (javax.sql.DataSource)context.lookup("java:/H2DS");
      Connection sqlConn = sqlDS.getConnection();
      
      String sql = "SHOW TABLES";
      PreparedStatement pstmt = sqlConn.prepareStatement(sql);
      
      long accessCount = 1L;
      // ds statistics
      assertEquals(accessCount, statistics.getValue("PreparedStatementCacheAccessCount"));
      assertEquals(1L, statistics.getValue("PreparedStatementCacheAddCount"));
      assertEquals(1, statistics.getValue("PreparedStatementCacheCurrentSize"));
      assertEquals(0L, statistics.getValue("PreparedStatementCacheDeleteCount"));
      assertEquals(0, statistics.getValue("PreparedStatementCacheHitCount"));
      assertEquals(0, statistics.getValue("PreparedStatementCacheMissCount"));
      
      pstmt.close();
      // same SQL again
      pstmt = sqlConn.prepareStatement(sql);
      
      // ds statistics
      assertEquals(accessCount + 1, statistics.getValue("PreparedStatementCacheAccessCount"));
      assertEquals(1L, statistics.getValue("PreparedStatementCacheAddCount"));
      assertEquals(1, statistics.getValue("PreparedStatementCacheCurrentSize"));
      assertEquals(0L, statistics.getValue("PreparedStatementCacheDeleteCount"));
      assertEquals(1, statistics.getValue("PreparedStatementCacheHitCount"));
      assertEquals(0, statistics.getValue("PreparedStatementCacheMissCount"));
      
      String sql2 = "SHOW SCHEMAS";
      PreparedStatement pstmt2 = sqlConn.prepareStatement(sql2);
      
      // ds statistics
      assertEquals(accessCount + 2, statistics.getValue("PreparedStatementCacheAccessCount"));
      assertEquals(2L, statistics.getValue("PreparedStatementCacheAddCount"));
      assertEquals(2, statistics.getValue("PreparedStatementCacheCurrentSize"));
      assertEquals(0L, statistics.getValue("PreparedStatementCacheDeleteCount"));
      assertEquals(1, statistics.getValue("PreparedStatementCacheHitCount"));
      assertEquals(0, statistics.getValue("PreparedStatementCacheMissCount"));
      
      pstmt.close();
      pstmt2.close();
      sqlConn.close();
      
      // clear all values at last
      statistics.clear();
      poolStatistics.setEnabled(oldEnable);
      assertTrue(poolStatistics.isEnabled());
   }
   
   
}
