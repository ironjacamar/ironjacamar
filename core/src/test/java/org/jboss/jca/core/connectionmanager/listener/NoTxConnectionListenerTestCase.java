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
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.common.MockConnectionRequestInfo;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnection;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.notx.NoTxConnectionManagerImpl;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerTestCase;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 *
 * A NoTxConnectionListenerTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class NoTxConnectionListenerTestCase
{

   /**Embedded JCA*/
   private static Embedded embedded = null;

   private static NoTxConnectionManagerImpl noTxCm = null;

   private static ManagedConnectionFactory mcf = null;

   private static Pool pool = null;

   /**
    *
    * testConnectionClosed
    *
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void testConnectionClosed() throws Exception
   {
      ConnectionListener listener = null;

      Subject subject = null;

      if (noTxCm.getSubjectFactory() != null && noTxCm.getSecurityDomain() != null)
      {
         subject = noTxCm.getSubjectFactory().createSubject(noTxCm.getSecurityDomain());
      }
      MockConnectionRequestInfo cri = new MockConnectionRequestInfo();

      Object connection = noTxCm.allocateConnection(mcf, cri);
      listener = noTxCm.getManagedConnection(subject, cri);

      assertNotNull(listener);
      assertThat(listener, instanceOf(NoTxConnectionListener.class));

      ConnectionEvent event = new ConnectionEvent(listener.getManagedConnection(), ConnectionEvent.CONNECTION_CLOSED);

      ((NoTxConnectionListener) listener).connectionClosed(event);

      assertThat(((MockManagedConnection) listener.getManagedConnection()).cleanUpCalled(), is(1));
   }

   /**
    *
    * connectionErrorOccuredShouldFreeManagedCOnnection
    *
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void connectionErrorOccuredShouldFreeManagedCOnnection() throws Exception
   {
      ConnectionListener listener = null;

      Subject subject = null;

      if (noTxCm.getSubjectFactory() != null && noTxCm.getSecurityDomain() != null)
      {
         subject = noTxCm.getSubjectFactory().createSubject(noTxCm.getSecurityDomain());
      }
      MockConnectionRequestInfo cri = new MockConnectionRequestInfo();

      Object connection = noTxCm.allocateConnection(mcf, cri);
      listener = noTxCm.getManagedConnection(subject, cri);

      assertNotNull(listener);
      assertThat(listener, instanceOf(NoTxConnectionListener.class));
      listener.registerConnection(connection);
      ConnectionEvent event = new ConnectionEvent(listener.getManagedConnection(),
                                                  ConnectionEvent.CONNECTION_ERROR_OCCURRED);

      listener.connectionErrorOccurred(event);
      assertThat(listener.isManagedConnectionFree(), is(true));
   }

   /**
    *
    * unregisterAssociationShouldFreeManagedCOnnection
    *
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void unregisterAssociationShouldFreeManagedCOnnection() throws Exception
   {
      ConnectionListener listener = null;

      Subject subject = null;

      if (noTxCm.getSubjectFactory() != null && noTxCm.getSecurityDomain() != null)
      {
         subject = noTxCm.getSubjectFactory().createSubject(noTxCm.getSecurityDomain());
      }
      MockConnectionRequestInfo cri = new MockConnectionRequestInfo();

      Object connection = noTxCm.allocateConnection(mcf, cri);
      listener = noTxCm.getManagedConnection(subject, cri);

      assertNotNull(listener);
      assertThat(listener, instanceOf(NoTxConnectionListener.class));
      listener.registerConnection(connection);

      noTxCm.unregisterAssociation(listener, connection);

      assertThat(listener.isManagedConnectionFree(), is(true));
   }

   /**
    *
    * unregisterNotYetCreatedAssociationShouldNotThrowException
    *
    * @throws Exception in case of unexpected errors
    */
   @Test
   public void unregisterNotYetCreatedAssociationShouldNotThrowException() throws Exception
   {
      ConnectionListener listener = null;

      Subject subject = null;

      if (noTxCm.getSubjectFactory() != null && noTxCm.getSecurityDomain() != null)
      {
         subject = noTxCm.getSubjectFactory().createSubject(noTxCm.getSecurityDomain());
      }
      MockConnectionRequestInfo cri = new MockConnectionRequestInfo();

      Object connection = noTxCm.allocateConnection(mcf, cri);
      listener = noTxCm.getManagedConnection(subject, cri);

      assertNotNull(listener);
      assertThat(listener, instanceOf(NoTxConnectionListener.class));

      noTxCm.unregisterAssociation(listener, connection);

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

      TransactionManager tm = embedded.lookup("RealTransactionManager", TransactionManager.class);

      mcf = new MockManagedConnectionFactory();
      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true, true);

      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager connectionManager = 
         cmf.createNonTransactional(TransactionSupportLevel.NoTransaction, pool,
                                    null, null, false, null, true, true,
                                    FlushStrategy.FAILING_CONNECTION_ONLY,
                                    null, null);

      noTxCm = ((NoTxConnectionManagerImpl) connectionManager);

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
