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

import org.jboss.jca.core.connectionmanager.pool.capacity.DefaultCapacity;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Assert;

/**
 * A PoolByCriNoTxDecrementCapacityWatermarkPolicyTestCase
 *<p>
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
public class PoolByCriNoTxDecrementCapacityWatermarkPolicyTestCase extends PoolByCriNoTxTestCaseAbstract
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
      return createNoTxDeployment(getCriIJWithDecrementer("WatermarkDecrementer"));
   }

   @Override
   public void checkPool() throws Exception
   {
      // WatermarkDecrementer is invalid for PoolByCri, setting should be ignored and default used
      AbstractPool pool = getPool();
      Assert.assertEquals("Wrong decrement is used, default should be used",
            DefaultCapacity.DEFAULT_DECREMENTER.getClass(), pool.getCapacity().getDecrementer().getClass());
   }
}
