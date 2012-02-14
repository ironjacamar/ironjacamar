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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.Transaction;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * A OnePoolTestCase.
 *
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to AbstractPool's
 * package protected methods. Please don't move it, and keep this class packaging consistent with AbstractPool's
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class OnePoolTestCase
{

   /**
    *
    * getKeyShouldReturnSameBooleanValuePassedAsSeparateNoTx
    * @throws Exception in case of unexpected errors
    *
    */
   @Test
   public void getKeyShouldReturnSameBooleanValuePassedAsSeparateNoTx() throws Exception
   {
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      assertThat((Boolean) pool.getKey(null, null, true), is(true));
      assertThat((Boolean) pool.getKey(null, null, false), is(false));
   }

   /**
   *
   * getKeyShouldReturnSameBooleanValuePassedAsSeparateNoTx
   *  @throws Exception in case of unexpected errors
   */
   @Test(expected = IllegalArgumentException.class)
   public void constructorShouldThrowIllegalArgumentExceptionForNullManagedConnectionFactory() throws Exception
   {
      OnePool pool = new OnePool(null, mock(PoolConfiguration.class), false, true);
   }

   /**
   *
   * constructorShouldThrowIllegalArgumentExceptionForNullPoolConfiguration
   *  @throws Exception in case of unexpected errors
   */
   @Test(expected = IllegalArgumentException.class)
   public void constructorShouldThrowIllegalArgumentExceptionForNullPoolConfiguration() throws Exception
   {
      OnePool pool = new OnePool(mock(ManagedConnectionFactory.class), null, false, true);
   }

   /**
   *
   * emptySubPoolShouldDoNothing
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void emptySubPoolShouldDoNothing() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      //when (note: argument is not important, set to null just for convenience)
      ((OnePool) pool).emptySubPool(null);
      //then
      assertThat(pool.getManagedConnectionPools().get(pool.getKey(null, null, false)) == null, is(true));
   }

   /**
   *
   * flushPoolShouldLeaveSubPoolEmpty
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void flushPoolShouldLeaveSubPoolEmpty() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      //when (note: argument is not important, set to null just for convenience)
      ((OnePool) pool).flush();
      //then
      assertThat(pool.getManagedConnectionPools().size(), is(0));

   }

   /**
   *
   * shutdownShouldLeaveSubPoolEmpty
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void shutdownShouldLeaveSubPoolEmpty() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      //when (note: argument is not important, set to null just for convenience)
      ((OnePool) pool).shutdown();
      //then
      assertThat(pool.getManagedConnectionPools().size(), is(0));

   }

   /**
   *
   * getTransactionOldConnectionShouldThrowResourceExceptionIfLockFail
   *  @throws Exception in case of unexpected errors
   */
   @Test//(expected = ResourceException.class)
   public void getTransactionOldConnectionShouldThrowResourceExceptionIfLockFail() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      Transaction trackByTransaction = mock(Transaction.class);
      //doThrow(new InterruptedException()).lock(trackByTransaction);
      //when
      //pool.getTransactionOldConnection(trackByTransaction);
      //then exception
   }

   /**
   *
   * getTransactionOldConnectionShouldReturnNullIfClIsNullButCOrrectlyLockAndUnlock
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void getTransactionOldConnectionShouldReturnNullIfClIsNullButCorrectlyLockAndUnlock() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      Transaction trackByTransaction = mock(Transaction.class);
      //when
      //Object returnValue = pool.getTransactionOldConnection(trackByTransaction);
      //then
      //assertThat(returnValue == null, is(true));
   }

   /**
   *
   * getTransactionOldConnectionShouldReturnNullIfClIsNullButCOrrectlyLockAndUnlock
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void getTransactionOldConnectionShouldReturnValueIfClHasValueAndCorrectlyLockAndUnlock() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      Transaction trackByTransaction = mock(Transaction.class);
      ConnectionListener listener = mock(ConnectionListener.class);
      //when
      //ConnectionListener returnValue = pool.getTransactionOldConnection(trackByTransaction);
      //then
      //assertThat(returnValue, is(listener));
   }

   /**
   *
   * getTransactionNewConnectionShouldGetConnectionBeforeLocking
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void getTransactionNewConnectionShouldGetConnectionBeforeLocking() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      Transaction trackByTransaction = mock(Transaction.class);
      ConnectionListener listener = mock(ConnectionListener.class);
      ManagedConnectionPool mcp = mock(ManagedConnectionPool.class);
      Subject subject = new Subject();
      ConnectionRequestInfo cri = mock(ConnectionRequestInfo.class);
      //when
      //ConnectionListener returnValue = pool.getTransactionNewConnection(trackByTransaction, mcp, subject, cri);
      //then

      //note: it'simportant the order of inorder.verrify invocations, not inOrder() constructor
      /*
      InOrder inOrder = Mockito.inOrder(mcp, trackByTx);
      inOrder.verify(mcp, times(1)).getConnection(subject, cri);
      inOrder.verify(trackByTx, times(1)).lock(eq(trackByTransaction));
      //always unlock because it's on finally block
      inOrder.verify(trackByTx, times(1)).unlock(eq(trackByTransaction));
      */
   }

   /**
   *
   * getTransactionNewConnectionShouldThrowResourceExceptionAndReturnCOnnectionInCaseOfLockFails
   *  @throws Exception in case of unexpected errors
   */
   @Test//(expected = ResourceException.class)
   public void getTransactionNewConnectionShouldThrowResourceExceptionAndReturnCOnnectionInCaseOfLockFails()
      throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      Transaction trackByTransaction = mock(Transaction.class);
      ManagedConnectionPool mcp = mock(ManagedConnectionPool.class);
      ConnectionListener cl = mock(ConnectionListener.class);
      Subject subject = new Subject();
      ConnectionRequestInfo cri = mock(ConnectionRequestInfo.class);
      when(mcp.getConnection(subject, cri)).thenReturn(cl);

      //doThrow(new InterruptedException()).when(trackByTx).lock(trackByTransaction);
      //ConnectionListener returnValue = pool.getTransactionNewConnection(trackByTransaction, mcp, subject, cri);
      //then

      //note: it'simportant the order of inorder.verrify invocations, not inOrder() constructor
      /*
      InOrder inOrder = Mockito.inOrder(mcp, trackByTx);
      inOrder.verify(mcp, times(1)).getConnection(subject, cri);
      inOrder.verify(trackByTx, times(1)).lock(eq(trackByTransaction));
      inOrder.verify(mcp, times(1)).returnConnection(eq(cl), eq(false));
      //always unlock because it's on finally block
      inOrder.verify(trackByTx, times(1)).unlock(eq(trackByTransaction));
      */
   }

   /**
   *
   * getTransactionNewConnectionShouldThrowResourceExceptionAndReturnCOnnectionInCaseOfLockFails
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void getTransactionNewConnectionShouldUseTheOtherFromDifferentThreadIfAlreadyTracked() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      Transaction trackByTransaction = mock(Transaction.class);
      ManagedConnectionPool mcp = mock(ManagedConnectionPool.class);
      ConnectionListener cl = mock(ConnectionListener.class);
      ConnectionListener other = mock(ConnectionListener.class);

      Subject subject = new Subject();
      ConnectionRequestInfo cri = mock(ConnectionRequestInfo.class);
      when(mcp.getConnection(subject, cri)).thenReturn(cl);

      //when(trackByTx.get(eq(trackByTransaction))).thenReturn(other);
      //when
      //ConnectionListener returnValue = pool.getTransactionNewConnection(trackByTransaction, mcp, subject, cri);
      //then

      //note: it'simportant the order of inorder.verrify invocations, not inOrder() constructor
      /*
      InOrder inOrder = Mockito.inOrder(mcp, trackByTx, other);
      inOrder.verify(mcp, times(1)).getConnection(subject, cri);
      inOrder.verify(trackByTx, times(1)).lock(eq(trackByTransaction));
      inOrder.verify(mcp, times(1)).returnConnection(eq(cl), eq(false));
      inOrder.verify(other, times(1)).setTrackByTx(eq(true));
      inOrder.verify(trackByTx, times(1)).set(eq(other));
      //always unlock because it's on finally block
      inOrder.verify(trackByTx, times(1)).unlock(eq(trackByTransaction));
      */

      //assertThat(returnValue, is(other));
   }

   /**
   *
   * getTransactionNewConnectionShouldThrowResourceExceptionAndReturnCOnnectionInCaseOfLockFails
   *  @throws Exception in case of unexpected errors
   */
   @Test
   public void getTransactionNewConnectionShouldUseMcpReturnedCl() throws Exception
   {
      //given
      AbstractPool pool = new OnePool(mock(ManagedConnectionFactory.class), mock(PoolConfiguration.class), false, true);
      Transaction trackByTransaction = mock(Transaction.class);
      ManagedConnectionPool mcp = mock(ManagedConnectionPool.class);
      ConnectionListener cl = mock(ConnectionListener.class);
      ConnectionListener other = null;

      Subject subject = new Subject();
      ConnectionRequestInfo cri = mock(ConnectionRequestInfo.class);
      when(mcp.getConnection(subject, cri)).thenReturn(cl);

      //when(trackByTx.get(eq(trackByTransaction))).thenReturn(other);
      //when
      //ConnectionListener returnValue = pool.getTransactionNewConnection(trackByTransaction, mcp, subject, cri);
      //then

      //note: it'simportant the order of inorder.verrify invocations, not inOrder() constructor
      /*
      InOrder inOrder = Mockito.inOrder(mcp, trackByTx, cl);
      inOrder.verify(mcp, times(1)).getConnection(subject, cri);
      inOrder.verify(trackByTx, times(1)).lock(eq(trackByTransaction));
      inOrder.verify(cl, times(1)).setTrackByTx(eq(true));
      inOrder.verify(trackByTx, times(1)).set(eq(cl));
      //always unlock because it's on finally block
      inOrder.verify(trackByTx, times(1)).unlock(eq(trackByTransaction));
      */

      //assertThat(returnValue, is(cl));
   }

}
