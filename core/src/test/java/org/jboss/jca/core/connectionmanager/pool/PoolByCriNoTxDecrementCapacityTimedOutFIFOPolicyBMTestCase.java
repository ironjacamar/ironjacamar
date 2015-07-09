/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionRequestInfoImpl;

import javax.resource.spi.ConnectionRequestInfo;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.assertEquals;

/**
 * A PoolByCriNoTxDecrementCapacityTimedOutFIFOPolicyBMTestCase
 * <p>
 * PoolByCri doesn't support prefill from a user PoV - connections
 * are explicit created using the specified credential or default which is <code>null</code>
 * in this test.
 * <p>
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * <p>
 * <ol>Byteman rules description:
 * <li>start two counters</li>
 * <li>timeout two connections listeners and decrement both counters</li>
 * <li>only decrement second counter 3 times</li>
 * <li>timeout two connections listeners and decrement second counter</li>
 * </ol>
 *
 * @author <a href="mailto:msimka@redhat.com">Martin Simka</a>
 */
@BMRules(value =
      {
            @BMRule(name = "start counters",
                  targetClass = "PoolByCriNoTxDecrementCapacityTimedOutFIFOPolicyBMTestCase",
                  targetMethod = "checkPool",
                  action = "createCounter(\"counter1\", 2); createCounter(\"counter2\", 6)"),
            @BMRule(name = "decrement counter1",
                  targetClass = "AbstractConnectionListener",
                  targetMethod = "isTimedOut",
                  condition = "readCounter(\"counter1\")>0",
                  action = "decrementCounter(\"counter1\"); decrementCounter(\"counter2\"); RETURN true;"),
            @BMRule(name = "decrement counter2 - if 1 stop",
                  targetClass = "AbstractConnectionListener",
                  targetMethod = "isTimedOut",
                  condition = "readCounter(\"counter1\")==0 && readCounter(\"counter2\")>2",
                  action = "decrementCounter(\"counter2\")"),
            @BMRule(name = "decrement counter2",
                  targetClass = "AbstractConnectionListener",
                  targetMethod = "isTimedOut",
                  condition = "readCounter(\"counter2\")<3 && readCounter(\"counter2\")>0",
                  action = "decrementCounter(\"counter2\"); RETURN true")
      })
public class PoolByCriNoTxDecrementCapacityTimedOutFIFOPolicyBMTestCase extends PoolByCriNoTxTestCaseAbstract
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
      return createNoTxDeployment(getCriIJWithDecrementer("TimedOutFIFODecrementer"));
   }

   @Override
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();
      assertEquals(0, pool.getManagedConnectionPools().size());
      fillPoolToSize(5);
      assertEquals(1, pool.getManagedConnectionPools().size());
      PoolStatistics ps = pool.getStatistics();
      checkStatistics(ps, 5, 0, 5);
      ManagedConnectionPool mcp = pool.getManagedConnectionPools().get(pool.getKey(null, null, false));
      callRemoveIdleConnections(mcp);

      checkStatistics(ps, 5, 0, 3, 2);

      fillPoolToSize(5, "0");
      assertEquals(2, pool.getManagedConnectionPools().size());
      checkStatistics(ps, 5, 0, 8);

      ConnectionRequestInfo cri = new SimpleConnectionRequestInfoImpl("0");
      mcp = pool.getManagedConnectionPools().get(pool.getKey(null, cri, false));
      callRemoveIdleConnections(mcp);
      System.out.println(ps);
      checkStatistics(ps, 5, 0, 6, 4);
   }
}
