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
package org.jboss.jca.test.core.connectionmanager.unit;

import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;
import org.jboss.jca.test.core.connectionmanager.common.MockConnectionManager;
import org.jboss.jca.test.core.connectionmanager.common.MockManagedConnectionFactory;

import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.security.SubjectFactory;
import org.jboss.util.NotImplementedException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * AbstractConnectionManagerTestCase.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Rev$ $Date$
 *
 */
public class AbstractConnectionManagerTestCase
{
   /**Mock instance*/
   private static AbstractConnectionManager connectionManager = null;

   /**
    * Init.
    */
   @BeforeClass
   public static void init()
   {
      connectionManager = new MockConnectionManager();
   }
   
   /**
    * testPoolingStrategyNotNull. 
    */
   @Test
   public void testPoolingStrategyNotNull()
   {
      assertNull(connectionManager.getPoolingStrategy());
      connectionManager.setPoolingStrategy(new OnePool(null, null, false));
      assertNotNull(connectionManager.getPoolingStrategy());
      assertTrue(connectionManager.getPoolingStrategy() instanceof OnePool);
   }
   
   /**
    * testGetCachedConnectionManager.
    */
   @Test
   public void testGetCachedConnectionManager()
   {
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
      assertNull(connectionManager.getManagedConnectionFactory());
      MockManagedConnectionFactory mcf = new MockManagedConnectionFactory();
      connectionManager.setPoolingStrategy(new OnePool(mcf, null, false));
      assertNotNull(connectionManager.getManagedConnectionFactory());
      assertEquals(mcf, connectionManager.getManagedConnectionFactory());
   }
   
   /**
    * testAllocationRetry.
    */
   @Test
   public void testAllocationRetry()
   {
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
      assertNull(connectionManager.getTransactionManagerInstance());
   }
   
   /**
    * testIsTransactional.
    */
   @Test
   public void testIsTransactional()
   {
      assertFalse(connectionManager.isTransactional());
   }
   
   /**
    * testGetTimeLeftBeforeTrsTimeout.
    */
   @Test
   public void testGetTimeLeftBeforeTrsTimeout()
   {
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
      connectionManager.setPoolingStrategy(null);
      assertNull(connectionManager.getManagedConnectionFactory());
   }
   
   /**
    * testGetManagedConnectionInShutdownedManager
    * @throws ResourceException for exception
    */
   @Test(expected = ResourceException.class)
   public void testGetManagedConnectionInShutdownedManager() throws ResourceException
   {
      connectionManager.setShutDown(true);
      connectionManager.getManagedConnection(null, null);
   }
   
   /**
    * testAllocateConnectionPoolingStrategyNull.
    * @throws ResourceException for exception
    */
   @Test(expected = ResourceException.class)
   public void testAllocateConnectionPoolingStrategyNull() throws ResourceException
   {
      connectionManager.setPoolingStrategy(null);
      connectionManager.allocateConnection(null, null);
   }
   
   /**
    * testAllocateConnectionWrongMCF.
    * @throws ResourceException for exception
    */
   @Test(expected = ResourceException.class)
   public void testAllocateConnectionWrongMCF() throws ResourceException
   {
      OnePool pool = new OnePool(new MockManagedConnectionFactory(), null, false);
      connectionManager.setPoolingStrategy(pool);
      connectionManager.allocateConnection(new MockManagedConnectionFactory(), null);
   }
   
   
   /**
    * Destroy.
    */
   @AfterClass
   public static void destroy()
   {
      connectionManager = null;
   }
}
