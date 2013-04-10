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

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionRequestInfoImpl;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.*;

/**
 * 
 * A PoolByCriNoTxDeploymentInvalidIdleConnectionsFlushTestCase
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public class PoolByCriNoTxDeploymentInvalidIdleConnectionsFlushTestCase extends PoolByCriNoTxTestCaseAbstract
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
      return createNoTxDeployment(getCriIJ(FlushStrategy.INVALID_IDLE_CONNECTIONS));
   }

   @Override
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();
      PoolStatistics ps = pool.getStatistics();

      assertEquals(pool.getManagedConnectionPools().size(), 0);
      SimpleConnection c = cf.getConnection("A");
      SimpleConnection c0 = cf.getConnection("A");
      SimpleConnection c1 = cf.getConnection("B");
      SimpleConnection c2 = cf.getConnection("B");
      SimpleConnection c3 = cf.getConnection("B");

      assertEquals(pool.getManagedConnectionPools().size(), 2);
      checkStatistics(ps, 5, 5, 5);

      Object key1 = pool.getKey(null, new SimpleConnectionRequestInfoImpl("A"), false);
      Object key2 = pool.getKey(null, new SimpleConnectionRequestInfoImpl("B"), false);

      checkStatistics(pool.getManagedConnectionPools().get(key1).getStatistics(), 3, 2, 2);
      checkStatistics(pool.getManagedConnectionPools().get(key2).getStatistics(), 2, 3, 3);

      c1.close();
      c0.close();
      c3.close();
      c.fail();
      Thread.sleep(1000);
      assertEquals(pool.getManagedConnectionPools().size(), 2);
      // All connections in Pool "A"  destroyed
      checkStatistics(ps, 9, 1, 3, 2);
      checkStatistics(pool.getManagedConnectionPools().get(key1).getStatistics(), 5, 0, 0, 2);
      checkStatistics(pool.getManagedConnectionPools().get(key2).getStatistics(), 4, 1, 3);

      assertFalse(c2.isDetached());

      c2.fail();
      // One of two idle connections in Pool "B" destroyed too
      Thread.sleep(1000);

      assertEquals(pool.getManagedConnectionPools().size(), 2);
      checkStatistics(pool.getManagedConnectionPools().get(key1).getStatistics(), 5, 0, 0, 2);
      checkStatistics(pool.getManagedConnectionPools().get(key2).getStatistics(), 5, 0, 1, 2);
   }
}
