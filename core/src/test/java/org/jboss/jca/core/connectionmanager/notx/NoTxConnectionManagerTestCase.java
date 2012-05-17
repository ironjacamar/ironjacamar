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
package org.jboss.jca.core.connectionmanager.notx;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.common.MockConnectionRequestInfo;
import org.jboss.jca.core.connectionmanager.common.MockHandle;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.NoTxConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * NonTxConnectionManagerTestCase.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class NoTxConnectionManagerTestCase
{
   private static ConnectionManager connectionManager = null;

   private static ManagedConnectionFactory mcf = null;

   /**
    * Initialize.
    */
   @BeforeClass
   public static void init()
   {
      TransactionManager tm = null;
      assertNull(tm);

      mcf = new MockManagedConnectionFactory();
      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true, true);

      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      connectionManager = 
         cmf.createNonTransactional(TransactionSupportLevel.NoTransaction, pool, null, null, false, null, true, true,
                                    FlushStrategy.FAILING_CONNECTION_ONLY,
                                    null, null);
      assertNotNull(connectionManager);

      assertTrue(connectionManager instanceof NoTxConnectionManager);
   }

   /**
    * Test allocate connection.
    *  @throws Exception in case of error and test fail
    */
   @Test
   public void allocateCOnnectionShouldReturnCorrectHandle() throws Exception
   {
      Object object = null;
      object = connectionManager.allocateConnection(mcf, new MockConnectionRequestInfo());

      assertNotNull(object);
      assertThat(object, instanceOf(MockHandle.class));
   }

   /**
    * connectionListenerInjectedIntoManagedConnectionShouldBeNoTx
    * @throws Exception in case of error and test fail
   */
   @Test
   public void connectionListenerInjectedIntoManagedConnectionShouldBeNoTx() throws Exception
   {
      ConnectionListener listener = null;

      NoTxConnectionManagerImpl noTxCm = ((NoTxConnectionManagerImpl) connectionManager);

      Subject subject = null;

      if (noTxCm.getSubjectFactory() != null && noTxCm.getSecurityDomain() != null)
      {
         subject = noTxCm.getSubjectFactory().createSubject(noTxCm.getSecurityDomain());
      }

      listener = noTxCm.getManagedConnection(subject, new MockConnectionRequestInfo());

      assertNotNull(listener);
      assertThat(listener, instanceOf(NoTxConnectionListener.class));
   }

   /**
    * testIsTransactional.
    * @throws Exception in case of error and test fail
   */
   @Test
   public void isTransactionalShouldReturnFalse() throws Exception
   {
      NoTxConnectionManagerImpl noTxCm = ((NoTxConnectionManagerImpl) connectionManager);

      assertThat(noTxCm.isTransactional(), is(false));
   }

   /**
    * Destroy.
    */
   @AfterClass
   public static void destroy()
   {
      connectionManager = null;
      mcf = null;
   }

}
