/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.unit;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManagerImpl;
import org.jboss.jca.core.connectionmanager.common.MockConnectionManager;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;

import javax.resource.ResourceException;
import javax.security.auth.Subject;

import org.jboss.security.SubjectFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * AbstractConnectionManagerTestCase.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class AbstractConnectionManagerTestCase
{
   /**
    * testPoolNotNull.
    */
   @Test
   public void testPoolNotNull()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getPool());

      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      Pool pool = pf.create(PoolStrategy.ONE_POOL, new MockManagedConnectionFactory(), pc, false);
      pool.setConnectionListenerFactory(connectionManager);
      connectionManager.setPool(pool);

      assertNotNull(connectionManager.getPool());
   }

   /**
    * testGetCachedConnectionManager.
    */
   @Test
   public void testGetCachedConnectionManager()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getCachedConnectionManager());
      connectionManager.setCachedConnectionManager(new CachedConnectionManagerImpl(null, null, null));
      assertNotNull(connectionManager.getCachedConnectionManager());
   }

   /**
    * testJndiName.
    */
   @Test
   public void testJndiName()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getJndiName());
      connectionManager.setJndiName("jndi_name");
      assertNotNull(connectionManager.getJndiName());
      assertEquals("jndi_name", connectionManager.getJndiName());
   }

   /**
    * testSecDomain.
    */
   @Test
   public void testSecDomain()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getSecurityDomain());
      connectionManager.setSecurityDomain("my_domain");
      assertNotNull(connectionManager.getSecurityDomain());
      assertEquals("my_domain", connectionManager.getSecurityDomain());
   }

   /**
    * testSubjectFactory.
    */
   @Test
   public void testSubjectFactory()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getSubjectFactory());
      SubjectFactory fact = new SubjectFactory()
      {

         public Subject createSubject()
         {
            return null;
         }

         public Subject createSubject(String arg0)
         {
            return null;
         }

      };
      connectionManager.setSubjectFactory(fact);
      assertNotNull(connectionManager.getSubjectFactory());
      assertEquals(fact, connectionManager.getSubjectFactory());
   }

   /**
    * testGetManagedConnectionFactory.
    */
   @Test
   public void testGetManagedConnectionFactory()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getManagedConnectionFactory());
      MockManagedConnectionFactory mcf = new MockManagedConnectionFactory();

      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, false);
      pool.setConnectionListenerFactory(connectionManager);
      connectionManager.setPool(pool);

      assertNotNull(connectionManager.getManagedConnectionFactory());
      assertEquals(mcf, connectionManager.getManagedConnectionFactory());
   }

   /**
    * testAllocationRetry.
    */
   @Test
   public void testGetAllocationRetry()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertEquals(0, connectionManager.getAllocationRetry());
      connectionManager.setAllocationRetry(5);
      assertEquals(5, connectionManager.getAllocationRetry());
   }

   /**
    * setAllocationRetryInMilisec.
    */
   @Test
   public void setAllocationRetryInMilisec()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertEquals(0L, connectionManager.getAllocationRetryWaitMillis());
      connectionManager.setAllocationRetryWaitMillis(5000L);
      assertEquals(5000L, connectionManager.getAllocationRetryWaitMillis());
   }


   /**
    * testGetTransactionManagerInstance.
    */
   @Test
   public void testGetTransactionManagerInstance()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getTransactionIntegration());
   }

   /**
    * testGetManagedConnectionFactoryIsNull.
    */
   @Test
   public void testGetManagedConnectionFactoryIsNull()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      connectionManager.setPool(null);
      assertNull(connectionManager.getManagedConnectionFactory());
   }

   /**
    * testGetManagedConnectionInShutdownedManager
    * @throws ResourceException for exception
    */
   @Test(expected = ResourceException.class)
   public void testGetManagedConnectionInShutdownedManager() throws ResourceException
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      connectionManager.shutdown();
      connectionManager.getManagedConnection(null, null);
   }

   /**
    * testAllocateConnectionPoolNull.
    * @throws ResourceException for exception
    */
   @Test(expected = ResourceException.class)
   public void testAllocateConnectionPoolNull() throws ResourceException
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      connectionManager.setPool(null);
      connectionManager.allocateConnection(null, null);
   }

   /**
    * testAllocateConnectionWrongMCF.
    * @throws ResourceException for exception
    */
   @Test(expected = ResourceException.class)
   public void testAllocateConnectionWrongMCF() throws ResourceException
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();

      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      Pool pool = pf.create(PoolStrategy.ONE_POOL, new MockManagedConnectionFactory(), pc, false);
      pool.setConnectionListenerFactory(connectionManager);

      connectionManager.setPool(pool);
      connectionManager.allocateConnection(new MockManagedConnectionFactory(), null);
   }

}
