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
package org.jboss.jca.core.connectionmanager.listener;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.TxConnectionManager;
import org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManagerImpl;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.listener.TxConnectionListener.TransactionSynchronization;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerTestCase;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 *
 * A TxConnectionListenerTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class TxConnectionListenerTestCase
{
   /**Embedded JCA*/
   private static Embedded embedded = null;

   private static TxConnectionManager txConnectionManager = null;

   private static ManagedConnectionFactory mcf = null;

   /**
    *
    * delistShouldSetTransactionSynchronizationToNullIfEverythingCommitted
    *
    * @throws Exception in case of error
    */
   @Test
   public void delistShouldSetTransactionSynchronizationToNullIfEverythingCommitted() throws Exception
   {
      //given
      TxConnectionListener listener = 
         new TxConnectionListener(txConnectionManager, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(false);
      Transaction tx = mock(Transaction.class);
      when(tx.getStatus()).thenReturn(Status.STATUS_COMMITTED);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(tx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.delist();
      //then
      assertThat(listener.getTransactionSynchronization(), is((TransactionSynchronization) null));
   }

   /**
    *
    * delistShouldNotSetTransactionSynchronizationToNullIfTrackByTx
    *
    * @throws Exception in case of error
    */
   @Test
   public void delistShouldNotSetTransactionSynchronizationToNullIfTrackByTx() throws Exception
   {
      //given
      TxConnectionListener listener =
         new TxConnectionListener(txConnectionManager, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      Transaction tx = mock(Transaction.class);
      when(tx.getStatus()).thenReturn(Status.STATUS_COMMITTED);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(tx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.delist();
      //then
      assertThat(listener.getTransactionSynchronization(), not(is((TransactionSynchronization) null)));
   }

   /**
   *
   * enlistShouldThrowSystemExceptionIfEnlistResourceFail
   *
   * @throws Exception in case of error
   */
   @Test
   public void deListShouldCompleteCorrectlyAfterARightEnlist() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(threadTx.enlistResource((XAResource) anyObject())).thenReturn(true);
      when(threadTx.delistResource((XAResource) anyObject(), anyInt())).thenReturn(true);

      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(false);
      TransactionSynchronization transactionSynchronization = null;
      listener.setTransactionSynchronization(transactionSynchronization);
      listener.enlist();
      //when
      listener.delist();

      //then complete without exception

   }

   /**
   *
   * enlistShouldThrowSystemExceptionIfEnlistResourceFail
   *
   * @throws Exception in case of error
   */
   @Test(expected = ResourceException.class)
   public void deListShouldThrowResourceExceptionIfDelistResourceFailOnTx() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(threadTx.enlistResource((XAResource) anyObject())).thenReturn(true);
      /*******this is the important GIVEN *******/
      when(threadTx.delistResource((XAResource) anyObject(), anyInt())).thenReturn(false);

      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(false);
      TransactionSynchronization transactionSynchronization = null;
      listener.setTransactionSynchronization(transactionSynchronization);
      listener.enlist();
      //when
      listener.delist();

      //then complete without exception

   }


   /**
   *
   * enlistShouldThrowIllegalStateExceptionWithCurrentTxAndStatusNoTransaction
   *
   * @throws Exception in case of error
   */
   @Test(expected = IllegalStateException.class)
   public void enlistShouldThrowIllegalStateExceptionWithCurrentTxAndStatusNoTransaction() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      when(tm.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);
      TxConnectionListener listener = 
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      Transaction tx = mock(Transaction.class);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(tx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then iLLEGALsTATEeXCEPTION

   }

   /**
   *
   * enlistShouldDoNothingForNoStransaction
   *
   * @throws Exception in case of error
   */
   @Test
   public void enlistShouldDoNothingForNoStransaction() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      when(tm.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);
      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      Transaction tx = mock(Transaction.class);
      TransactionSynchronization transactionSynch = mock(TxConnectionListener.TransactionSynchronization.class);
      listener.setTransactionSynchronization(transactionSynch);
      //when
      listener.enlist();
      //then
      verifyZeroInteractions(transactionSynch);
   }

   /**
   *
   * enlistShouldThrowIllegalStateExceptionWithCurrentTxAndStatusNoTransaction
   *
   * @throws Exception in case of error
   */
   @Test(expected = IllegalStateException.class)
   public void enlistShouldThrowIllegalStateExceptionWithNoActiveTransaction() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      when(tm.getStatus()).thenReturn(Status.STATUS_COMMITTING);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      Transaction tx = mock(Transaction.class);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(tx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then iLLEGALsTATEeXCEPTION

   }

   /**
   *
   * enlistShouldThrowIllegalStateExceptionWithCurrentTxAndStatusNoTransaction
   *
   * @throws Exception in case of error
   */
   @Test(expected = IllegalStateException.class)
   public void enlistShouldThrowIllegalStateExceptionTryingToChangeTransaction() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      Transaction tx = mock(Transaction.class);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(tx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then iLLEGALsTATEeXCEPTION

   }

   /**
   *
   * enlistShouldThrowIllegalStateExceptionForCantEnlistAlreadyTx
   *
   * @throws Exception in case of error
   */
   @Test(expected = IllegalStateException.class)
   public void enlistShouldThrowIllegalStateExceptionForCantEnlistAlreadyTx() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(false);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(threadTx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then iLLEGALsTATEeXCEPTION

   }

   /**
   *
   * enlistShouldThrowIllegalStateExceptionForCantEnlistAlreadyTx
   *
   * @throws Exception in case of error
   */
   @Test(expected = SystemException.class)
   public void enlistShouldThrowIllegalStateExceptionForCantEnlistAlreadyTx2() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      doThrow(new SystemException("ThreadTx Exception")).when(threadTx).registerSynchronization(
         (Synchronization) anyObject());
      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(threadTx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then iLLEGALsTATEeXCEPTION

   }

   /**
   *
   * enlistShouldThrowIllegalStateExceptionWithCurrentTxAndStatusNoTransaction
   *
   * @throws Exception in case of error
   */
   @Test(expected = IllegalStateException.class)
   public void enlistShouldThrowIllegalStateExceptionForNotEnlistedResources() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      TransactionSynchronization transactionSynchronization = listener.new TransactionSynchronization(threadTx, false);
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then iLLEGALsTATEeXCEPTION

   }

   /**
   *
   * enlistShouldThrowSystemExceptionIfEnlistResourceFail
   *
   * @throws Exception in case of error
   */
   @Test(expected = SystemException.class)
   public void enlistShouldThrowSystemExceptionIfEnlistResourceFail() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      /******* THIS IS THE IMPORTANT GIVEN ****/
      when(threadTx.enlistResource((XAResource) anyObject())).thenReturn(false);

      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      TransactionSynchronization transactionSynchronization = null;
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then SystemException

   }

   /**
   *
   * enlistShouldThrowSystemExceptionIfEnlistResourceFail
   *
   * @throws Exception in case of error
   */
   @Test
   public void enlistShouldCompleteWithoutExceptions() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      TransactionIntegration ti = mock(TransactionIntegration.class);
      TransactionManager tm = mock(TransactionManager.class);
      com.arjuna.ats.jta.transaction.Transaction threadTx = mock(com.arjuna.ats.jta.transaction.Transaction.class);
      when(threadTx.isAlive()).thenReturn(true);
      when(threadTx.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(threadTx.enlistResource((XAResource) anyObject())).thenReturn(true);

      when(tm.getStatus()).thenReturn(Status.STATUS_ACTIVE);
      when(tm.getTransaction()).thenReturn(threadTx);
      when(cm.getTransactionIntegration()).thenReturn(ti);
      when(ti.getTransactionManager()).thenReturn(tm);

      TxConnectionListener listener =
         new TxConnectionListener(cm, null, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      TransactionSynchronization transactionSynchronization = null;
      listener.setTransactionSynchronization(transactionSynchronization);
      //when
      listener.enlist();
      //then complete without exception

   }

   /**
   *
   * connectionClosedShouldThrowIllegalStateExceptionForDifferentManagedConnection
   *
   * @throws Exception in case of error
   */
   @Test(expected = IllegalArgumentException.class)
   public void connectionClosedShouldThrowIllegalStateExceptionForDifferentManagedConnection() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      ManagedConnection mc2 = mock(ManagedConnection.class);

      TxConnectionListener listener =
         new TxConnectionListener(cm, mc, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      ConnectionEvent ce = new ConnectionEvent(mc2, ConnectionEvent.CONNECTION_CLOSED);
      //when
      listener.connectionClosed(ce);
      //then complete without exception

   }

   /**
   *
   * connectionClosedShouldThrowIllegalStateExceptionForDifferentManagedConnection
   *
   * @throws Exception in case of error
   */
   @Test
   public void connectionClosedShouldInvokeRightMethodsForNonFreedConnection() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      CachedConnectionManagerImpl ccm = mock(CachedConnectionManagerImpl.class);
      when(cm.getCachedConnectionManager()).thenReturn(ccm);
      TxConnectionListener listener =
         new TxConnectionListener(cm, mc, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(false);
      ConnectionEvent ce = new ConnectionEvent(mc, ConnectionEvent.CONNECTION_CLOSED);
      //when
      listener.connectionClosed(ce);
      //then
      verify(ccm, times(1)).unregisterConnection((ConnectionCacheListener) anyObject(), anyObject());
      verify(cm, never()).returnManagedConnection(eq(listener), anyBoolean());
   }

   /**
   *
   * connectionClosedShouldInvokeRiightMethodsFreedConnection
   *
   * @throws Exception in case of error
   */
   @Test
   public void connectionClosedShouldInvokeRightMethodsForFreedConnection() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      CachedConnectionManagerImpl ccm = mock(CachedConnectionManagerImpl.class);
      when(cm.getCachedConnectionManager()).thenReturn(ccm);
      TxConnectionListener listener =
         new TxConnectionListener(cm, mc, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      ConnectionEvent ce = new ConnectionEvent(mc, ConnectionEvent.CONNECTION_CLOSED);
      //when
      listener.connectionClosed(ce);
      //then
      verify(ccm, times(1)).unregisterConnection((ConnectionCacheListener) anyObject(), anyObject());
      verify(cm, times(1)).returnManagedConnection(eq(listener), eq(false));
   }

   /**
   *
   * connectionClosedShouldInvokeRightMethodsInCaseOfExceptions
   *
   * @throws Exception in case of error
   */
   @Test
   public void connectionClosedShouldInvokeRightMethodsInCaseOfExceptions() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      CachedConnectionManagerImpl ccm = mock(CachedConnectionManagerImpl.class);
      when(cm.getCachedConnectionManager()).thenReturn(ccm);
      TxConnectionListener listener =
         new TxConnectionListener(cm, mc, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      listener.setTrackByTx(true);
      ConnectionEvent ce = mock(ConnectionEvent.class);
      when(ce.getSource()).thenReturn(mc);
      /****** this is the important given *****/
      when(ce.getConnectionHandle()).thenThrow(new RuntimeException());
      //when
      listener.connectionClosed(ce);
      //then
      verify(ccm, never()).unregisterConnection((ConnectionCacheListener) anyObject(), anyObject());
      verify(cm, times(1)).returnManagedConnection(eq(listener), eq(true));
   }

   /**
   *
   * tidyUpShouldDoNothingWithoutLocalTransaction
   *
   * @throws Exception in case of error
   */
   @Test
   public void tidyUpShouldDoNothingWithoutLocalTransaction() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      TxConnectionListener listener =
         new TxConnectionListener(cm, mc, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      ConnectionEvent ce = mock(ConnectionEvent.class);
      /****** this is the important given *****/
      listener.localTransactionCommitted(ce);
      //when
      listener.tidyup();
      //then
      verify(cm, times(0)).returnManagedConnection(eq(listener), eq(true));
   }

   /**
   *
   * tidyUpShouldDoNothingWithoutLocalTransaction
   *
   * @throws Exception in case of error
   */
   @Test(expected = ResourceException.class)
   public void tidyUpShouldRethrowExceptionOfGetLocalTransactionAsResourceException() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      TxConnectionListener listener =
         new TxConnectionListener(cm, mc, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      ConnectionEvent ce = mock(ConnectionEvent.class);
      /****** this is the important given *****/
      listener.localTransactionStarted(ce);
      when(mc.getLocalTransaction()).thenThrow(new ResourceException());
      //when
      listener.tidyup();
      //then

   }

   /**
   *
   * tidyUpShouldDoNothingWithoutLocalTransaction
   *
   * @throws Exception in case of error
   */
   @Test(expected = ResourceException.class)
   public void tidyUpShouldThrowResourceExceptionIfLocalTransactionIsNull() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      TxConnectionListener listener =
         new TxConnectionListener(cm, mc, null, null, FlushStrategy.FAILING_CONNECTION_ONLY, null);
      ConnectionEvent ce = mock(ConnectionEvent.class);
      /****** this is the important given *****/
      listener.localTransactionStarted(ce);
      when(mc.getLocalTransaction()).thenReturn(null);
      //when
      listener.tidyup();
      //then

   }

   /**
   *
   * tidyUpShouldDoNothingWithoutLocalTransaction
   *
   * @throws Exception in case of error
   */
   @Test
   public void tidyUpShouldInvokeRollebackOnLocalTransaction() throws Exception
   {
      //given
      TxConnectionManager cm = mock(TxConnectionManager.class);
      ManagedConnection mc = mock(ManagedConnection.class);
      TxConnectionListener listener = new TxConnectionListener(cm, mc, null, null,
                                                               FlushStrategy.FAILING_CONNECTION_ONLY, null);
      ConnectionEvent ce = mock(ConnectionEvent.class);
      /****** this is the important given *****/
      listener.localTransactionStarted(ce);
      LocalTransaction local = mock(LocalTransaction.class);
      when(mc.getLocalTransaction()).thenReturn(local);
      //when
      listener.tidyup();
      //then
      verify(local, times(1)).rollback();
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
      URL naming = TxConnectionManagerTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = TxConnectionManagerTestCase.class.getClassLoader().getResource("transaction.xml");

      embedded.deploy(naming);
      embedded.deploy(transaction);

      TransactionIntegration ti = embedded.lookup("TransactionIntegration", TransactionIntegration.class);

      mcf = new MockManagedConnectionFactory();
      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true, true);

      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager connectionManager =
         cmf.createTransactional(TransactionSupportLevel.LocalTransaction, pool,
                                 null, null, false, null, true, true, FlushStrategy.FAILING_CONNECTION_ONLY,
                                 null, null, ti, null, null, null, null, null);

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
      URL naming = TxConnectionManagerTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = TxConnectionManagerTestCase.class.getClassLoader().getResource("transaction.xml");

      embedded.undeploy(transaction);
      embedded.undeploy(naming);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }

}

