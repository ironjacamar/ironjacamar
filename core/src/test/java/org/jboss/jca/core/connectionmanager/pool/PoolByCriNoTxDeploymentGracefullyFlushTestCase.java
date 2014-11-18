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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.*;

/**
 * 
 * A PoolByCriNoTxDeploymentGacefullyFlushTestCase
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public class PoolByCriNoTxDeploymentGracefullyFlushTestCase extends PoolByCriNoTxTestCaseAbstract
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
      return createNoTxDeployment(getCriIJ(FlushStrategy.GRACEFULLY));
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
      checkStatistics(ps, 0, 5, 5);

      c0.close();
      c1.close();
      c.fail();
      Thread.sleep(1000);
      assertEquals(pool.getManagedConnectionPools().size(), 2);
      //check, that pool "A" was flushed 
      checkStatistics(ps, 3, 2, 3, 2);
 
      assertFalse(c3.isDetached());
      assertFalse(c2.isDetached());
      assertFalse(c1.isDetached());

      c2.fail();
      //graceful flush of pool "B", 1 connection remains 
      Thread.sleep(1000);
      assertFalse(c3.isDetached());
      assertTrue(c2.isDetached());
      assertTrue(c1.isDetached());

      assertEquals(pool.getManagedConnectionPools().size(), 2);
      checkStatistics(ps, 4, 1, 1, 4);
      c3.close();
      checkStatistics(ps, 5, 0, 0, 5);
   }
}
