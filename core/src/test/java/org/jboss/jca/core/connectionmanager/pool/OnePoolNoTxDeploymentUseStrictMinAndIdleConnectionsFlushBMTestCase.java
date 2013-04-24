/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ConnectionDefinitionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.IronjacamarDescriptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.*;

/**
 * 
 * A OnePoolNoTxDeploymentUseStrictMinAndIdleConnectionsFlushBMTestCase
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
@BMRules(value =
      {
      @BMRule(name = "wait prefill1", 
         targetClass = "SemaphoreArrayListManagedConnectionPool", 
         targetMethod = "initialize", 
         targetLocation = "EXIT",
         action = "waitFor(\"filled\")"),
      @BMRule(name = "wait prefill3", 
         targetClass = "OnePoolNoTxDeploymentUseStrictMinAndIdleConnectionsFlushBMTestCase", 
         targetMethod = "checkPool", 
         targetLocation = "LINE 118",
         action = "waitFor(\"filled\")"),
      @BMRule(name = "wait prefill4", 
         targetClass = "OnePoolNoTxDeploymentUseStrictMinAndIdleConnectionsFlushBMTestCase", 
         targetMethod = "checkPool", 
         targetLocation = "LINE 134",
         action = "waitFor(\"filled\")"),
      @BMRule(name = "filled", 
         targetClass = "SemaphoreArrayListManagedConnectionPool", 
         targetMethod = "fillTo", 
         targetLocation = "EXIT",
         action = "signalWake(\"filled\", true)") 
      })
public class OnePoolNoTxDeploymentUseStrictMinAndIdleConnectionsFlushBMTestCase extends OnePoolNoTxTestCaseAbstract
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
      return createNoTxDeployment(getIJ());
   }

   /**
    * 
    * get IronjacamarDescriptor for deployment
    * 
    * @return IronjacamarDescriptor
    */
   public static IronjacamarDescriptor getIJ()
   {
      IronjacamarDescriptor ij = getBasicIJXml(SimpleManagedConnectionFactory.class.getName());
      ConnectionDefinitionType ijCdt = ij.getOrCreateConnectionDefinitions().getOrCreateConnectionDefinition();
      ijCdt.removePool().getOrCreatePool().minPoolSize(3).maxPoolSize(5).useStrictMin(true)
         .flushStrategy("IdleConnections");

      return ij;
   }

   @Override
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();
      
      assertEquals(pool.getManagedConnectionPools().size(), 0);
      PoolStatistics ps = pool.getStatistics();

      SimpleConnection c = cf.getConnection();
      //prefill() called during mcp initialization and we get connection from pool 
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      checkStatistics(ps, 4, 1, 3);

      c.fail();
      //prefill() 
      //1 failed + 2 idle connections destroyed
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
      //prefill()
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      //1 failed + 1 idle connection destroyed
      checkStatistics(ps, 4, 1, 3, 5);

      c.close();
      checkStatistics(ps, 5, 0, 3, 5);
   }
}
