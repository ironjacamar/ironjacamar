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
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.assertEquals;

/**
 * A PoolByCriNoTxDecrementCapacitySizePolicyNullTestCase
 * <p>
 * PoolByCri doesn't support prefill from a user PoV - connections
 * are explicit created using the specified credential or default which is <code>null</code>
 * in this test.
 * <p>
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 *
 * @author <a href="mailto:msimka@redhat.com">Martin Simka</a>
 */
public class PoolByCriNoTxDecrementCapacitySizePolicyNullTestCase extends PoolByCriNoTxTestCaseAbstract
{

   /**
    * deployment
    *
    * @return archive
    */
   @Deployment
   public static ResourceAdapterArchive deployment()
   {
      return createNoTxDeployment(getCriIJWithDecrementer("SizeDecrementer"));
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
      checkStatistics(ps, 5, 0, 4, 1);

      callRemoveIdleConnections(mcp);
      checkStatistics(ps, 5, 0, 3, 2);

      fillPoolToSize(5, "0");
      assertEquals(2, pool.getManagedConnectionPools().size());
      checkStatistics(ps, 5, 0, 8, 2);

      ConnectionRequestInfo cri = new SimpleConnectionRequestInfoImpl("0");
      mcp = pool.getManagedConnectionPools().get(pool.getKey(null, cri, false));
      callRemoveIdleConnections(mcp);
      checkStatistics(ps, 5, 0, 7, 3);

      callRemoveIdleConnections(mcp);
      checkStatistics(ps, 5, 0, 6, 4);
   }
}
