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

import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.connectionmanager.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * A OnePoolNoTxDeploymentUseStrictMinAndIdleConnectionsFlushTestCase
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public class OnePoolNoTxDeploymentUseStrictMinAndIdleConnectionsFlushTestCase extends PoolTestCaseAbstract
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
      return getDeploymentWith("ij-strict-idle.xml");
   }

   /**
    * 
    * checkConfig
    *
    */
   @Test 
   public void checkConfig()
   {
      checkConfiguration(NoTxConnectionManager.class, OnePool.class);
   }

   /**
    * 
    * checkPool
    * 
    * @throws Exception in case of error
    */
   @Test
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();

      assertEquals(pool.getManagedConnectionPools().size(), 0);
      PoolStatistics ps = pool.getStatistics();

      SimpleConnection c = cf.getConnection();
      Thread.sleep(1000);
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      checkStatistics(ps, 4, 1, 3);
      
      c.fail();
      Thread.sleep(1000);
      //1 failed + 2 idle connections destroyed
      log.info("PS after fail:" + ps.toString());
      checkStatistics(ps, 5, 0, 3, 3);
      
      c = cf.getConnection();
      checkStatistics(ps, 4, 1, 3, 3);

      SimpleConnection c1 = cf.getConnection();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      checkStatistics(ps, 3, 2, 3);
      for (ManagedConnectionPool mcp : pool.getManagedConnectionPools().values())
      {
         checkStatistics(mcp.getStatistics(), 3, 2, 3);
      }
      
      c1.fail();
      Thread.sleep(1000);
      log.info("PS after 2nd fail:" + ps.toString());
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      //1 failed + 1 idle connection destroyed
      checkStatistics(ps, 4, 1, 3, 5);
      
      c.close();
      log.info("PS after close:" + ps.toString());
      checkStatistics(ps, 5, 0, 3, 5);
   }
}
