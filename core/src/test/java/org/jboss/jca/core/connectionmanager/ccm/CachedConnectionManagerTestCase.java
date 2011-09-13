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
package org.jboss.jca.core.connectionmanager.ccm;

import org.jboss.jca.core.connectionmanager.ConnectionRecord;
import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * A CachedConnectionManagerTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CachedConnectionManagerTestCase
{

   /**
    *
    * userTransactionStartedShoudStartTransactionOnEveryConnectionCacheListener
    *
    * @throws Exception in case of error
    */
   @Test
   public void userTransactionStartedShoudStartTransactionOnEveryConnectionCacheListener() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);
      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);
      CopyOnWriteArrayList<ConnectionRecord> crList0 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr0,
         cr1));
      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm0, crList0);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      //when
      ccm.getCurrentObjects().set(keys);
      //then
      ccm.userTransactionStarted();

      verify(cm0, times(1)).transactionStarted(eq(crList0));
      verify(cm1, times(1)).transactionStarted(eq(crList1));

   }

   /**
   *
   * userTransactionStartedShoudDoNothingForCurrentObjectsNull
   *
   * @throws Exception in case of error
   */
   @Test
   public void userTransactionStartedShoudDoNothingForCurrentObjectsNull() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);

      ccm.getCurrentObjects().set(null);
      //then
      ccm.userTransactionStarted();
   }

   /**
   *
   * popMetaAwareObjectShoudRemoveAssociationAndDisconnectFromListener
   *
   * @throws Exception in case of error
   */
   @Ignore
   public void popMetaAwareObjectShoudRemoveAssociationAndDisconnectFromListener() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);
      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);
      CopyOnWriteArrayList<ConnectionRecord> crList0 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr0,
         cr1));
      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm0, crList0);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      ccm.getCurrentObjects().set(keys);
      //when
      Set unsharableResources = mock(Set.class);
      //then
      ccm.popMetaAwareObject(unsharableResources);

      verify(cm0, times(1)).disconnect(eq(crList0), eq(unsharableResources));
      verify(cm1, times(1)).disconnect(eq(crList1), eq(unsharableResources));
      assertThat(ccm.getCurrentObjects().get().size(), is(0));

   }

   /**
   *
   * registerConnectionShoudDoNothingForCurrentObjectsNull
   *
   * @throws Exception in case of error
   */
   @Test
   public void registerConnectionShoudDoNothingForCurrentObjectsNull() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);

      ccm.getCurrentObjects().set(null);
      //then (parameters are not important the key of this test is that given ccm have no associations
      ccm.registerConnection(null, null, null, null);
   }

   /**
   *
   * registerConnectionShoudAddConnectionRecordToExistingKey
   *
   * @throws Exception in case of error
   */
   @Test
   public void registerConnectionShoudAddConnectionRecordToExistingKey() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);
      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);
      CopyOnWriteArrayList<ConnectionRecord> crList0 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr0,
         cr1));
      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm0, crList0);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      ccm.getCurrentObjects().set(keys);
      //when
      ccm.registerConnection(cm0, mock(ConnectionListener.class), mock(Object.class),
         mock(ConnectionRequestInfo.class));

      //then

      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> actualCmMap =
         ((KeyConnectionAssociation) keys.getLast()).getCMToConnectionsMap();
      assertThat(actualCmMap.get(cm0).size(), is(3));
      assertThat(actualCmMap.get(cm1).size(), is(2));

   }

   /**
   *
   * registerConnectionShoudAddConnectionRecordToExistingKey
   *
   * @throws Exception in case of error
   */
   @Test
   public void registerConnectionShoudAddKey() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);
      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm2 = mock(ConnectionCacheListener.class);

      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);
      CopyOnWriteArrayList<ConnectionRecord> crList0 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr0,
         cr1));
      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm0, crList0);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      ccm.getCurrentObjects().set(keys);
      //when
      ccm.registerConnection(cm2, mock(ConnectionListener.class), mock(Object.class),
         mock(ConnectionRequestInfo.class));

      //then

      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> actualCmMap =
         ((KeyConnectionAssociation) keys.getLast()).getCMToConnectionsMap();
      assertThat(actualCmMap.size(), is(3));
      assertThat(actualCmMap.get(cm0).size(), is(2));
      assertThat(actualCmMap.get(cm1).size(), is(2));
      assertThat(actualCmMap.get(cm2).size(), is(1));

   }

   /**
   *
   * unregisterConnectionShoudDoNothingForCurrentObjectsNull
   *
   * @throws Exception in case of error
   */
   @Test
   public void unregisterConnectionShoudDoNothingForCurrentObjectsNull() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);

      ccm.getCurrentObjects().set(null);
      //then (parameters are not important the key of this test is that given ccm have no associations
      ccm.unregisterConnection(null, null);
   }

   /**
   *
   * unregisterConnectionShoudDoNothingForNullConnectionsInMatchedKey
   *
   * @throws Exception in case of error
   */
   @Test
   public void unregisterConnectionShoudDoNothingForNullConnectionsInMatchedKey() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);
      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      Object connection = mock(Object.class);
      when(cr0.getConnection()).thenReturn(connection);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);

      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      ccm.getCurrentObjects().set(keys);
      //when
      ccm.unregisterConnection(cm0, connection);

      //then

      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> actualCmMap =
         ((KeyConnectionAssociation) keys.getLast()).getCMToConnectionsMap();
      assertThat(actualCmMap.size(), is(1));
      assertThat(actualCmMap.get(cm1).size(), is(2));

   }

   /**
   *
   * unregisterConnectionShoudRemovedMatchedConnection
   *
   * @throws Exception in case of error
   */
   @Test
   public void unregisterConnectionShoudRemovedMatchedConnection() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);
      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      Object connection = mock(Object.class);
      when(cr0.getConnection()).thenReturn(connection);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);
      CopyOnWriteArrayList<ConnectionRecord> crList0 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr0,
         cr1));
      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm0, crList0);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      ccm.getCurrentObjects().set(keys);
      //when
      ccm.unregisterConnection(cm0, connection);

      //then

      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> actualCmMap =
         ((KeyConnectionAssociation) keys.getLast()).getCMToConnectionsMap();
      assertThat(actualCmMap.get(cm0).size(), is(1));
      assertThat(actualCmMap.get(cm1).size(), is(2));

   }

   /**
   *
   * pushMetaAwareObjectShoudCreateStack
   *
   * @throws Exception in case of error
   */
   @Test
   public void pushMetaAwareObjectShoudPushANewKey() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);

      //when
      ccm.pushMetaAwareObject(mock(Object.class), null);

      //then
      assertThat(ccm.getCurrentObjects().get() == null, is(false));

   }

   /**
   *
   * pushMetaAwareObjectShoudCreateStack
   *
   * @throws Exception in case of error
   */
   @Test
   public void pushMetaAwareObjectShoudCreateStack() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);

      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      Object connection = mock(Object.class);
      when(cr0.getConnection()).thenReturn(connection);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);
      CopyOnWriteArrayList<ConnectionRecord> crList0 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr0,
         cr1));
      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm0, crList0);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      ccm.getCurrentObjects().set(keys);

      Object rawKey = mock(Object.class);

      //when
      ccm.pushMetaAwareObject(rawKey, null);

      //then
      assertThat(ccm.getCurrentObjects().get() == null, is(false));
      assertThat((KeyConnectionAssociation) ccm.getCurrentObjects().get().getLast(),
         is(new KeyConnectionAssociation(rawKey)));

   }

   /**
   *
   * pushMetaAwareObjectShoudReconnectAPreviouslyDisconnectedConnection
   *
   * @throws Exception in case of error
   */
   @Ignore
   public void pushMetaAwareObjectShoudReconnectAPreviouslyDisconnectedConnection() throws Exception
   {
      //given
      TransactionManager tm = mock(TransactionManager.class);
      TransactionSynchronizationRegistry tsr = mock(TransactionSynchronizationRegistry.class);
      UserTransactionRegistry utr = mock(UserTransactionRegistry.class);
      CachedConnectionManagerImpl ccm = new CachedConnectionManagerImpl(tm, tsr, utr);

      ConnectionCacheListener cm0 = mock(ConnectionCacheListener.class);
      ConnectionCacheListener cm1 = mock(ConnectionCacheListener.class);
      ConnectionRecord cr0 = mock(ConnectionRecord.class);
      Object connection = mock(Object.class);
      when(cr0.getConnection()).thenReturn(connection);
      ConnectionRecord cr1 = mock(ConnectionRecord.class);
      ConnectionRecord cr2 = mock(ConnectionRecord.class);
      ConnectionRecord cr3 = mock(ConnectionRecord.class);
      CopyOnWriteArrayList<ConnectionRecord> crList0 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr0,
         cr1));
      CopyOnWriteArrayList<ConnectionRecord> crList1 = new CopyOnWriteArrayList<ConnectionRecord>(Arrays.asList(cr2,
         cr3));
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         new ConcurrentHashMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>(2);
      cmToConnectionsMap.put(cm0, crList0);
      cmToConnectionsMap.put(cm1, crList1);

      LinkedList<Object> keys = new LinkedList<Object>();
      KeyConnectionAssociation key0 = new KeyConnectionAssociation("stringKey0");
      key0.setCMToConnectionsMap(cmToConnectionsMap);
      keys.add(key0);

      ccm.getCurrentObjects().set(keys);

      Set unsharableResources = mock(Set.class);
      ccm.popMetaAwareObject(unsharableResources);

      //when
      ccm.pushMetaAwareObject("stringKey0", unsharableResources);

      //then

      verify(cm0, times(1)).disconnect(eq(crList0), eq(unsharableResources));
      verify(cm1, times(1)).disconnect(eq(crList1), eq(unsharableResources));
      verify(cm0, times(1)).reconnect(eq(crList0), eq(unsharableResources));
      verify(cm1, times(1)).reconnect(eq(crList1), eq(unsharableResources));

      assertThat(ccm.getCurrentObjects().get() == null, is(false));
      assertThat(ccm.getCurrentObjects().get().size(), is(1));

   }

}
