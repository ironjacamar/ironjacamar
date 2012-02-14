/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

import static org.junit.Assert.assertNotNull;

/**
 * Serializable test of the transaction connection manager
 *
 * We need this in IronJacamar 2.0
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@Ignore
public class SerializableTestCase
{
   /**Embedded JCA*/
   private static Embedded embedded = null;

   /**
    * testSerializable.
    * @throws Throwable for exception
    */
   public void testSerializable() throws Throwable
   {
      TransactionIntegration ti = embedded.lookup("TransactionIntegration", TransactionIntegration.class);
      assertNotNull(ti);

      ManagedConnectionFactory mcf = new MockManagedConnectionFactory();
      PoolConfiguration pc = new PoolConfiguration();
      PoolFactory pf = new PoolFactory();

      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true, true);

      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager connectionManager = cmf.createTransactional(TransactionSupportLevel.XATransaction,
                                                                    pool, null, null, false, null, true,
                                                                    FlushStrategy.FAILING_CONNECTION_ONLY,
                                                                    null, null, ti,
                                                                    Boolean.FALSE, null, null, null, null);
      assertNotNull(connectionManager);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);

      oos.writeObject(connectionManager);
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
      URL naming = SerializableTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = SerializableTestCase.class.getClassLoader().getResource("transaction.xml");

      embedded.deploy(naming);
      embedded.deploy(transaction);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy Transaction and Naming
      URL naming = SerializableTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = SerializableTestCase.class.getClassLoader().getResource("transaction.xml");

      embedded.undeploy(transaction);
      embedded.undeploy(naming);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }

}
