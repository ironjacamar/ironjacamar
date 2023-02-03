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

import org.jboss.arquillian.extension.byteman.api.BMRule;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * OnePoolNoTxMatchConnectionBMTestCaseAbstract
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */

public abstract class OnePoolNoTxMatchConnectionBMTestCaseAbstract extends OnePoolNoTxTestCaseAbstract
{

   /**
    * testExceptionOnMatchConnection
    * @throws Throwable in case of unexpected errors
    */
   @Test
   @BMRule(name = "Throw exception on matchConnection", 
      targetClass = "org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory", 
      targetMethod = "matchManagedConnections", 
      action = " throw new jakarta.resource.ResourceException()")
   public void testExceptionOnMatchConnection() throws Throwable
   {
      test();
   }

   /**
    * testExceptionOnMatchConnection
    * @throws Throwable in case of unexpected errors
    */
   @Test
   @BMRule(name = "Returns null on matchConnection", 
   targetClass = "org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory", 
   targetMethod = "matchManagedConnections", action = " RETURN null")
   public void testMatchConnectionReturnsNull() throws Throwable
   {
      test();
   }

   /**
    * 
    * Unified test for all cases
    * @param wholePoolShouldBeDestroyed if true - all active connections in pool should be destroyed
    * otherwise - just one of them
    * 
    * @throws Throwable in case of error
    */
   public void test(boolean wholePoolShouldBeDestroyed) throws Throwable
   {
      AbstractPool pool = getPool();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      PoolStatistics ps = pool.getStatistics();
      int destroyed = ps.getDestroyedCount();
      int active = ps.getActiveCount();
      SimpleConnection c = cf.getConnection();
      assertEquals(ps.getDestroyedCount(), destroyed + (wholePoolShouldBeDestroyed ? active : 1));
      c.close();
   }

   /**
    * 
    * Test to be implemented
    * @throws Throwable in case of error
    *
    */
   public abstract void test() throws Throwable;
}
