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
import org.jboss.jca.core.connectionmanager.ConnectionManagerUtil;
import org.jboss.jca.core.connectionmanager.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * A DeploymentSimpleOnePoolTestCase.
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public class DeploymentSimpleOnePoolTestCase extends PoolTestCaseAbstract
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
      return getDeployment();
   }

   /**
    * connection factory
    */
   @Resource(mappedName = "java:/eis/Pool")
   SimpleConnectionFactory cf;

   @Override
   public AbstractPool getPool()
   {
      return (OnePool) ConnectionManagerUtil.extract(cf).getPool();
   }

   /**
    * 
    * checkConfiguration
    * 
    */
   @Test
   public void checkConfiguration()
   {
      assertTrue(ConnectionManagerUtil.extract(cf) instanceof NoTxConnectionManager);
      AbstractPool pool = getPool();
      assertEquals(pool.getManagedConnectionFactory(), cf.getMCF());
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
      SimpleConnection c = cf.getConnection();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      PoolStatistics ps = pool.getStatistics();
      assertEquals(ps.getActiveCount(), 1);
      assertEquals(ps.getInUseCount(), 1);
      assertEquals(ps.getAvailableCount(), 19);

      c.close();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      assertEquals(ps.getActiveCount(), 1);
      assertEquals(ps.getInUseCount(), 0);
      assertEquals(ps.getAvailableCount(), 20);

      c = cf.getConnection();
      SimpleConnection c1 = cf.getConnection();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      assertEquals(ps.getActiveCount(), 2);
      assertEquals(ps.getInUseCount(), 2);
      assertEquals(ps.getAvailableCount(), 18);
      for (ManagedConnectionPool mcp : pool.getManagedConnectionPools().values())
      {
         assertEquals(mcp.getStatistics().getActiveCount(), 2);
         assertEquals(mcp.getStatistics().getInUseCount(), 2);
         assertEquals(mcp.getStatistics().getAvailableCount(), 18);
      }

      c.close();
      c1.close();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      assertEquals(ps.getActiveCount(), 2);
      assertEquals(ps.getInUseCount(), 0);
      assertEquals(ps.getAvailableCount(), 20);

   }
}
