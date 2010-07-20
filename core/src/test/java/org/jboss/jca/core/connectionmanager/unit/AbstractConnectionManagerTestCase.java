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

import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.common.MockConnectionManager;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;

import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.security.SubjectFactory;
import org.jboss.util.NotImplementedException;

import org.junit.Test;

import static org.junit.Assert.*;

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
      connectionManager.setCachedConnectionManager(new CachedConnectionManager());
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
    * testSecDomainJndiName.
    */
   @Test
   public void testSecDomainJndiName()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertNull(connectionManager.getSecurityDomainJndiName());
      connectionManager.setSecurityDomainJndiName("jndi_name");
      assertNotNull(connectionManager.getSecurityDomainJndiName());
      assertEquals("jndi_name", connectionManager.getSecurityDomainJndiName());      
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
      assertNull(connectionManager.getTransactionManager());
   }
   
   /**
    * testIsTransactional.
    */
   @Test
   public void testIsTransactional()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      assertFalse(connectionManager.isTransactional());
   }
   
   /**
    * testGetTimeLeftBeforeTrsTimeout.
    */
   @Test
   public void testGetTimeLeftBeforeTrsTimeout()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      try
      {
         assertEquals(-1L, connectionManager.getTimeLeftBeforeTransactionTimeout(false));
      }
      catch (RollbackException e)
      {
         //No action
      }
   }
   
   /**
    * testGetTransactionTimeout.
    */
   @Test(expected = NotImplementedException.class)
   public void testGetTransactionTimeout()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      try
      {
         connectionManager.getTransactionTimeout();
      }
      catch (SystemException e)
      {
         //No action
      }
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
      connectionManager.setShutDown(true);
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
   
   /**
    * testGetManagedConnections.
    */
   @Test
   public void testGetManagedConnections()
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      
   }
   
   /**
    * testIdleTimeout.
    * @throws Exception for exception
    */
   @Test
   public void testIdleTimeout() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      
   }
   
   /**
    * testPartialIdleTimeout.
    * @throws Exception for exception.
    */
   @Test
   public void testPartialIdleTimeout() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
      
   }
   
   /**
    * testFillToMin.
    * @throws Exception for exception
    */
   @Test
   public void testFillToMin() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
   
   /**
    * testPrefillPool.
    * @throws Exception for exception
    */
   @Test
   public void testPrefillPool() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
   
   /**
    * testNonStrictMinPool.
    * @throws Exception for exception
    */
   @Test
   public void testNonStrictMinPool() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
   
   /**
    * testStrictMinPool.
    * @throws Exception for exception
    */
   @Test
   public void testStrictMinPool() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
   
   /**
    * testMisConfiguredFillToMin.
    * @throws Exception for exception
    */
   public void testMisConfiguredFillToMin() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
   
   /**
    * testChangedMaximum.
    * @throws Exception for exception.
    */
   @Test
   public void testChangedMaximum() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
   
   /**
    * testAllocationRetry.
    * @throws Exception for exception.
    */
   @Test
   public void testAllocationRetry() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
   
   /**
    * testAllocationRetryMultiThread. 
    * @throws Exception for exception
    */
   public void testAllocationRetryMultiThread() throws Exception
   {
      AbstractConnectionManager connectionManager = new MockConnectionManager();
   }
}
