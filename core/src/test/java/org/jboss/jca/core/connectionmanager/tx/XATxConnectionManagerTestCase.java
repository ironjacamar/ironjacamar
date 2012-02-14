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
package org.jboss.jca.core.connectionmanager.tx;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.TxConnectionManager;
import org.jboss.jca.core.connectionmanager.common.MockConnectionRequestInfo;
import org.jboss.jca.core.connectionmanager.common.MockHandle;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.TxConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.TransactionManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * XATxConnectionManagerTestCase.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class XATxConnectionManagerTestCase
{
   /**Embedded JCA*/
   private static Embedded embedded = null;

   private static TxConnectionManager txConnectionManager = null;

   private static ManagedConnectionFactory mcf = null;

   /**
    * testTxAllocateConnection.
    * @throws Throwable for exception
    */
   @Test
   public void testAllocateConnection() throws Throwable
   {

      TransactionManager transactionManager = txConnectionManager.getTransactionIntegration().getTransactionManager();
      assertNotNull(transactionManager);

      try
      {
         transactionManager.begin();

         Object handle = txConnectionManager.allocateConnection(mcf, new MockConnectionRequestInfo());
         assertNotNull(handle);

         assertTrue(handle instanceof MockHandle);

         transactionManager.commit();
      }
      catch (Exception e)
      {
         transactionManager.rollback();
         throw e;
      }
   }

   /**
    * testGetTimeLeftBeforeTrsTimeout.
    * @throws Throwable for exception
    */
   @Test
   public void testGetTimeLeftBeforeTrsTimeout() throws Throwable
   {

      try
      {
         assertEquals(-1L, txConnectionManager.getTimeLeftBeforeTransactionTimeout(false));
      }
      catch (RollbackException e)
      {
         //No action
      }
   }

   /**
    * testEnlistInExistingTx.
    * @throws Exception for exception
    */
   @Test
   public void testEnlistInExistingTx() throws Exception
   {

   }

   /**
    * testEnlistCheckedOutConnectionInNewTx.
    * @throws Exception for exception
    */
   @Test
   public void testEnlistCheckedOutConnectionInNewTx() throws Exception
   {

   }

   /**
    * testReconnectConnectionHandlesOnNotification.
    * @throws Exception for exception.
    */
   @Test
   public void testReconnectConnectionHandlesOnNotification() throws Exception
   {

   }

   /**
    * testEnlistAfterMarkRollback.
    * @throws Exception for exception
    */
   @Test
   public void testEnlistAfterMarkRollback() throws Exception
   {

   }

   /**
    * testBrokenConnectionAndTrackByTx.
    * @throws Exception for exception.
    */
   @Test
   public void testBrokenConnectionAndTrackByTx() throws Exception
   {

   }

   /**
    * testFailedStartTx.
    * @throws Exception for exception.
    */
   @Test
   public void testFailedStartTx() throws Exception
   {

   }

   /**
    * testFailedEndTx.
    * @throws Exception for exception.
    */
   @Test
   public void testFailedEndTx() throws Exception
   {

   }

   /**
    * testIsTransactional.
    * @throws Exception in case of error and test fail
    */
   @Test
   public void isTransactionalShouldReturnTrueIfTxRunning() throws Exception
   {
      //given
      TransactionManager transactionManager = txConnectionManager.getTransactionIntegration().getTransactionManager();
      try
      {
         //when
         transactionManager.begin();

         //then
         assertThat(txConnectionManager.isTransactional(), is(true));
         transactionManager.commit();
      }
      catch (Exception e)
      {
         transactionManager.rollback();
         throw e;
      }
   }

   /**
    * testIsTransactional.
    * @throws Exception in case of error and test fail
    */
   @Test
   public void isTransactionalShouldReturnFalseIfTxNotRunning() throws Exception
   {
      //given
      TransactionManager transactionManager = txConnectionManager.getTransactionIntegration().getTransactionManager();

      //when no transactionManager.begin() called

      //then
      assertThat(txConnectionManager.isTransactional(), is(false));
   }

   /**
    * connectionListenerInjectedIntoManagedConnectionShouldBeNoTx
    * @throws Exception in case of error and test fail
   */
   @Test
   public void connectionListenerInjectedIntoManagedConnectionShouldBeTx() throws Exception
   {
      ConnectionListener listener = null;

      TxConnectionManagerImpl txCm = ((TxConnectionManagerImpl) txConnectionManager);

      Subject subject = null;

      if (txCm.getSubjectFactory() != null && txCm.getSecurityDomain() != null)
      {
         subject = txCm.getSubjectFactory().createSubject(txCm.getSecurityDomain());
      }

      listener = txCm.getManagedConnection(subject, new MockConnectionRequestInfo());

      assertNotNull(listener);
      assertThat(listener, instanceOf(TxConnectionListener.class));
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create(false);

      // Startup
      embedded.startup();

      // Deploy Naming and Transaction
      URL naming = XATxConnectionManagerTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = XATxConnectionManagerTestCase.class.getClassLoader().getResource("transaction.xml");

      embedded.deploy(naming);
      embedded.deploy(transaction);

      TransactionIntegration ti = embedded.lookup("TransactionIntegration", TransactionIntegration.class);
      assertNotNull(ti);

      mcf = new MockManagedConnectionFactory();
      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true, true);

      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager connectionManager =
         cmf.createTransactional(TransactionSupportLevel.XATransaction, pool,
                                 null, null, false, null, true, FlushStrategy.FAILING_CONNECTION_ONLY,
                                 null, null, ti,
                                 Boolean.FALSE, null, null, null, null);
      assertNotNull(connectionManager);

      assertTrue(connectionManager instanceof TxConnectionManager);

      txConnectionManager = (TxConnectionManager) connectionManager;

   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy Transaction and Naming
      URL naming = XATxConnectionManagerTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = XATxConnectionManagerTestCase.class.getClassLoader().getResource("transaction.xml");

      embedded.undeploy(transaction);
      embedded.undeploy(naming);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }

}
