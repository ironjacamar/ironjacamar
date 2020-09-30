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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.assertEquals;

/**
 * 
 * A OnePoolNoTxDecrementCapacityTimedOutPolicyBMTestCase
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
      @BMRule(name = "start counter", 
         targetClass = "OnePoolNoTxDecrementCapacityTimedOutPolicyBMTestCase", 
         targetMethod = "checkPool", 
         action = "createCounter(\"counter\", 3)"),
      @BMRule(name = "decrement counter", 
         targetClass = "AbstractConnectionListener", 
         targetMethod = "isTimedOut", 
         condition = "decrementCounter(\"counter\")>0", 
         action = "RETURN true")
      })
public class OnePoolNoTxDecrementCapacityTimedOutPolicyBMTestCase extends
      OnePoolNoTxDecrementCapacityPolicyTestCaseAbstract
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
      return createNoTxDeployment(getIJWithDecrementer("TimedOutDecrementer"));
   }

   @Override
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();
      assertEquals(pool.getManagedConnectionPools().size(), 0);
      fillPoolToSize(5);
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      PoolStatistics ps = pool.getStatistics();
      checkStatistics(ps, 5, 0, 5);
      ManagedConnectionPool mcp = pool.getManagedConnectionPools().values().iterator().next();
      callRemoveIdleConnections(mcp);
      checkStatistics(ps, 5, 0, 3, 2);

   }

}
