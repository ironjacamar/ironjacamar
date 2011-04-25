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
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.embed.core.EmbeddedJcaDiscover;

import java.net.URL;
import java.sql.Connection;
import java.util.List;

import javax.naming.InitialContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
   @Ignore
   @Test
   public void testPoolStatisticsEnable() throws Throwable
   {
      DataSource ds = getDataSource();
      PoolStatistics poolStatistics = ds.getPool().getStatistics();
      assertTrue(poolStatistics.isEnabled());  // now it is enabled
      poolStatistics.clear(); // clear all data
      
      poolStatistics.setEnabled(false); // set enabled to false
      
      assertEquals(0, poolStatistics.getCreatedCount());
      assertEquals(0, poolStatistics.getActiveCount());
      assertEquals(0, poolStatistics.getMaxUsedCount());
      assertEquals(0, poolStatistics.getDestroyedCount());
      assertEquals(0, poolStatistics.getAvailableCount());
      
      InitialContext context = new InitialContext();
      javax.sql.DataSource sqlDS = (javax.sql.DataSource)context.lookup("java:/H2DS");
      Connection sqlConn = sqlDS.getConnection();  // getConnection() should not affect data in poolStatistics
      
      poolStatistics.setEnabled(true);  // set the enabled to true after the connection gotten. 
      
      // no more operations, but the data in the PoolStatistics is not empty, 
      // which means data were collected even when PoolStatistics enabled is false.
      // is it a feature or a bug ?      
      
      assertEquals(0, poolStatistics.getCreatedCount());
      assertEquals(0, poolStatistics.getActiveCount());
      assertEquals(0, poolStatistics.getMaxUsedCount());
      assertEquals(0, poolStatistics.getDestroyedCount());
      assertEquals(0, poolStatistics.getAvailableCount());
      
      sqlConn.close();
      
   }
   
}
