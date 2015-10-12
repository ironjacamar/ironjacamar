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
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.*;

/**
 * 
 * A OnePoolNoTxDeploymentPrefilledBMTestCase
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
      @BMRule(name = "first check", 
         targetClass = "OnePoolNoTxDeploymentPrefilledBMTestCase", 
         targetMethod = "checkPool", 
         targetLocation = "LINE 101",
         condition = "$0.getPool().getStatistics().getActiveCount()<2||flagged(\"prefill start\")",
         action = "traceln(\"///Prefill didn't finish, wait for it \");" +
            "\n waitFor(\"filled\")"),
      @BMRule(name = "wait prefill2", 
         targetClass = "OnePoolNoTxDeploymentPrefilledBMTestCase", 
         targetMethod = "checkPool", 
         targetLocation = "LINE 113",
         action = "waitFor(\"filled\")"),
      @BMRule(name = "wait prefill3", 
         targetClass = "OnePoolNoTxDeploymentPrefilledBMTestCase", 
         targetMethod = "checkPool", 
         targetLocation = "LINE 116",
         action = "waitFor(\"filled\")"),
      @BMRule(name = "wait prefill4", 
         targetClass = "OnePoolNoTxDeploymentPrefilledBMTestCase", 
         targetMethod = "checkPool", 
         targetLocation = "LINE 119",
         action = "waitFor(\"filled\")"),
      @BMRule(name = "filled", 
         targetClass = "SemaphoreConcurrentLinkedDequeManagedConnectionPool", 
         targetMethod = "fillTo", 
         targetLocation = "EXIT",
         action = "signalWake(\"filled\", true)"),
      @BMRule(name = "prefill start", 
         targetClass = "SemaphoreConcurrentLinkedDequeManagedConnectionPool", 
         targetMethod = "fillTo", 
         targetLocation = "ENTRY",
         action = "flag(\"prefill start\")")
      })
public class OnePoolNoTxDeploymentPrefilledBMTestCase extends OnePoolNoTxTestCaseAbstract
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
      return createNoTxDeployment(getPrefilledIJ(null));
   }

   @Override
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();
      // prefill()
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      PoolStatistics ps = pool.getStatistics();
      checkStatistics(ps, 5, 0, 2);

      SimpleConnection c = cf.getConnection();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      checkStatistics(ps, 4, 1, 2);

      SimpleConnection c1 = cf.getConnection();
      SimpleConnection c2 = cf.getConnection();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      //prefill()
      checkStatistics(ps, 2, 3, 3);
      c.fail();
      //prefill()
      checkStatistics(ps, 3, 2, 2, 1);
      c1.fail();
      //prefill()
      checkStatistics(ps, 4, 1, 2, 2);
      c2.close();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      checkStatistics(ps, 5, 0, 2, 2);
   }
}
