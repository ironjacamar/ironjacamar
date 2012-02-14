/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.connections;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.connections.adapter.TestConnection;
import org.jboss.jca.core.connectionmanager.connections.adapter.TestManagedConnection;
import org.jboss.jca.core.connectionmanager.connections.adapter.TestManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerImpl;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.util.HashSet;
import java.util.Set;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;

import org.jboss.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * InterleavinfTestCase
 */
public class InterleavingTestCase 
{
   private static Logger log = Logger.getLogger(InterleavingTestCase.class);

   private static final int POOL_SIZE = 5;

   private Embedded embedded = null;

   private TransactionManager tm;
   private CachedConnectionManager ccm;
   private TestManagedConnectionFactory mcf;
   private TxConnectionManagerImpl cm;
   private ConnectionRequestInfo cri;

   /**
    * Test: Get connection
    * @exception Exception If error
    */
   @Test
   public void testGetConnection() throws Exception
   {
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertTrue("Connection is null", c != null);
      c.close();
   }

   /**
    * Test: Enlist in existing tx
    * @exception Exception If error
    */
   @Test
   public void testEnlistInExistingTx() throws Exception
   {
      tm.begin();
      TestConnection c = null;
      try
      {
         c = (TestConnection)cm.allocateConnection(mcf, cri);
         try
         {
            assertTrue("Connection not enlisted in tx!", c.isInTx());
         }
         finally
         {
            c.close();
         }
         assertTrue("Connection still enlisted in tx!", !c.isInTx());
      }
      finally
      {
         if (tm.getStatus() == Status.STATUS_ACTIVE)
            tm.commit();
         else
            tm.rollback();
      }
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }

   /**
    * Test: Enlist checked out connection in new transaction
    * @exception Exception If error
    */
   @Ignore
   public void testEnlistCheckedOutConnectionInNewTx() throws Exception
   {
      Object key = this;
      Set unshared = new HashSet();
      ccm.pushMetaAwareObject(key, unshared);
      try
      {
         TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
         try
         {
            assertTrue("Connection already enlisted in tx!", !c.isInTx());
            tm.begin();
            try
            {
               assertTrue("Connection not enlisted in tx!", c.isInTx());
            }
            finally
            {
               if (tm.getStatus() == Status.STATUS_ACTIVE)
                  tm.commit();
               else
                  tm.rollback();
            }
            assertTrue("Connection still enlisted in tx!", !c.isInTx());
         }
         finally
         {
            c.close();
         }
      }
      finally
      {
         ccm.popMetaAwareObject(unshared);
      }
   }

   /** 
    * Tests the spec required behavior of reconnecting connection
    * handles left open on return from an ejb method call.
    *
    * @exception Exception If error
    */
   @Ignore
   public void testReconnectConnectionHandlesOnNotification() throws Exception
   {
      //ccm.setSpecCompliant(true);

      Object key1 = new Object();
      Object key2 = new Object();
      Set unshared = new HashSet();
      ccm.pushMetaAwareObject(key1, unshared);
      try
      {
         TestConnection c = null;
         tm.begin();
         try
         {
            ccm.pushMetaAwareObject(key2, unshared);
            try
            {
               c = (TestConnection)cm.allocateConnection(mcf, cri);
               assertTrue("Connection not enlisted in tx!", c.isInTx());
            }
            finally
            {
               ccm.popMetaAwareObject(unshared);
            }
         }
         finally
         {
            if (tm.getStatus() == Status.STATUS_ACTIVE)
               tm.commit();
            else
               tm.rollback();
         }
         tm.begin();
         try
         {
            ccm.pushMetaAwareObject(key2, unshared);
            try
            {
               assertTrue("Connection not enlisted in tx!", c.isInTx());
            }
            finally
            {
               ccm.popMetaAwareObject(unshared);
            }
         }
         finally
         {
            if (tm.getStatus() == Status.STATUS_ACTIVE)
               tm.commit();
            else
               tm.rollback();
         }
         assertTrue("Connection still enlisted in tx!", !c.isInTx());
         ccm.pushMetaAwareObject(key2, unshared);
         try
         {
            if (c != null)
               c.close();
         }
         finally
         {
            ccm.popMetaAwareObject(unshared);
         }
      }
      finally
      {
         ccm.popMetaAwareObject(unshared);
      }
   }

   /** 
    * Test: Enlist after mark rollback
    * @exception Exception If error
    */
   @Test
   public void testEnlistAfterMarkRollback() throws Exception
   {
      // Get a transaction and mark it for rollback
      tm.begin();
      try
      {
         tm.setRollbackOnly();
         // Allocate a connection upto the pool size all should fail
         for (int i = 0; i < POOL_SIZE; i++)
         {
            try
            {
               cm.allocateConnection(mcf, cri);
               fail("Should not be allowed to allocate a connection with setRollbackOnly()");
            }
            catch (Exception e)
            {
               log.debug("Error allocating connection", e);
            }
         }
      }
      finally
      {
         tm.rollback();
      }

      // We should be able to get a connection now
      testGetConnection();
   }

   /** 
    * Test: Broken connection and track-by-tx
    * @exception Exception If error
    */
   @Test
   public void testBrokenConnectionAndTrackByTx() throws Exception
   {
      //cm.setTrackConnectionByTx(true);
      tm.begin();
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      c.fireConnectionError();
      try
      {
         c.close();
      }
      catch (Exception e)
      {
         // Ignore
      }
      try
      {
         tm.commit();
         fail("Should not be here");
      }
      catch (RollbackException re)
      {
         // Expected
      }
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }
  
   /** 
    * Test: Failed start transaction
    * @exception Exception If error
    */
   @Test
   public void testFailedStartTx() throws Exception
   {
      TestManagedConnection.setFailInStart(false, XAException.XAER_RMFAIL);
      tm.begin();
      TestConnection conn = null;
      TestConnection conn2 = null;
      
      try
      {
         assertTrue("Connection in pool!", cm.getPool().getStatistics().getActiveCount() == 0);
         conn = (TestConnection)cm.allocateConnection(mcf, cri);
         
         //One should have been created
         assertTrue(cm.getPool().getStatistics().getActiveCount() == 1);

         TestManagedConnection.setFailInStart(true, XAException.XAER_RMFAIL);
        
         conn2 = (TestConnection)cm.allocateConnection(mcf, cri);
        
         fail("Should not be here.");
      }
      catch (Throwable t)
      {
         // Ignore
      }      
      conn.close();
      tm.rollback();
      assertNull(conn2);            
      //assertTrue("Count: " + cm.getPool().getStatistics().getCreatedCount(),
      //           cm.getPool().getStatistics().getCreatedCount() == 1);
   }
  
   /** 
    * Test: Failed end transaction
    * @exception Exception If error
    */
   @Test
   public void testFailedEndTx() throws Exception
   {
      TestManagedConnection.setFailInStart(false, XAException.XAER_RMFAIL);
      TestManagedConnection.setFailInEnd(false, XAException.XAER_RMFAIL);
      tm.begin();
      TestConnection conn = null;
      TestConnection conn2 = null;
     
      try
      {
         assertTrue("Connection in pool!", cm.getPool().getStatistics().getActiveCount() == 0);
         conn = (TestConnection)cm.allocateConnection(mcf, cri);
         
         // One should have been created
         assertTrue(cm.getPool().getStatistics().getActiveCount() == 1);
         conn.close();
         
         TestManagedConnection.setFailInEnd(true, XAException.XAER_RMFAIL);
         
         conn2 = (TestConnection)cm.allocateConnection(mcf, cri);
         conn2.close();
         tm.commit();
        
         fail("Should not be here.");
      }
      catch (Throwable t)
      {
         log.debug(t.getMessage(), t);
      }      
     
      TestManagedConnection.setFailInEnd(false, 0);
      TestManagedConnection.setFailInStart(false, 0);
      
      assertNotNull(conn2);
      assertTrue(conn2.getMCIsNull());     
      assertTrue("Connection count" + cm.getPool().getStatistics().getActiveCount(), 
                 cm.getPool().getStatistics().getActiveCount() == 0);
      assertTrue("Failed endTx should destroy Connection", 
                 cm.getPool().getStatistics().getDestroyedCount() > 0);
   }
  
   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @Before
   public void before() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create();

      // Startup
      embedded.startup();
      
      tm = embedded.lookup("RealTransactionManager", TransactionManager.class);

      ccm = embedded.lookup("CCM", CachedConnectionManager.class);
      
      mcf = new TestManagedConnectionFactory();
      cm = buildTxConnectionManager(mcf);
   }

   private TxConnectionManagerImpl buildTxConnectionManager(ManagedConnectionFactory mcf) throws Throwable
   {
      TransactionIntegration ti = embedded.lookup("TransactionIntegration", TransactionIntegration.class);
      assertNotNull(ti);
      
      PoolConfiguration pc = new PoolConfiguration();
      pc.setMaxSize(POOL_SIZE);

      PoolFactory pf = new PoolFactory();
    
      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true, true);
      
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      TxConnectionManagerImpl tcm =
         (TxConnectionManagerImpl)cmf.createTransactional(TransactionSupportLevel.XATransaction, pool,
                                                          null, null, false, null, true,
                                                          FlushStrategy.FAILING_CONNECTION_ONLY,
                                                          null, null, ti, null, null, null, null, null);
      tcm.setInterleaving(true);

      return tcm;
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @After
   public void after() throws Throwable
   {
      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
