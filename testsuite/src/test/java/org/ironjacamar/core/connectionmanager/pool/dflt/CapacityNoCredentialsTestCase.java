/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.connectionmanager.pool.dflt;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;
import org.ironjacamar.core.connectionmanager.pool.capacity.MaxPoolSizeIncrementer;
import org.ironjacamar.core.connectionmanager.pool.capacity.MinPoolSizeDecrementer;
import org.ironjacamar.core.connectionmanager.pool.capacity.TimedOutDecrementer;
import org.ironjacamar.core.util.Injection;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.security.UnifiedSecurityConnection;
import org.ironjacamar.rars.security.UnifiedSecurityConnectionFactory;
import org.ironjacamar.util.TestUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.spi.TransactionSupport;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * No credential test case w/ Prefill
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class CapacityNoCredentialsTestCase
{
   /**
    * The noTxCf w/o Tx
    */
   @Resource(mappedName = "java:/eis/CapacityNoTxConnectionFactory")
   private UnifiedSecurityConnectionFactory noTxCf;

   /**
    * The noTxCf w/o Tx
    */
   @Resource(mappedName = "java:/eis/CapacityNoTxConnectionFactoryFILO")
   private UnifiedSecurityConnectionFactory noTxCfFILO;

   /**
    * The deployment repository
    */
   @Inject
   private static DeploymentRepository dr;



   /**
    * The resource adapter
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityRar();
   }


   /**
    * The activation w7o Tx
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private ResourceAdaptersDescriptor createNoTxActivation() throws Throwable
   {
      return ResourceAdapterFactory
            .createCapacityActivation(null, null, TransactionSupport.TransactionSupportLevel.NoTransaction,
                  "CapacityNoTxConnectionFactory", MaxPoolSizeIncrementer.class.getName(),
                  MinPoolSizeDecrementer.class.getName());
   }

   /**
    * The activation w7o Tx
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private ResourceAdaptersDescriptor createNoTxActivationFILO() throws Throwable
   {
      return ResourceAdapterFactory
            .createCapacityActivation(null, null, TransactionSupport.TransactionSupportLevel.NoTransaction,
                  "CapacityNoTxConnectionFactoryFILO", MaxPoolSizeIncrementer.class.getName(),
                  TimedOutDecrementer.class.getName());
   }


   /**
    * Deployment test w/o Tx
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testMinPoolSize() throws Throwable
   {
      assertNotNull(noTxCf);
      assertNotNull(dr);

      assertEquals(2, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/CapacityNoTxConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf = d.getConnectionFactories().iterator()
            .next();
      assertNotNull(dcf);

      org.ironjacamar.core.api.deploymentrepository.Pool p = dcf.getPool();
      assertNotNull(p);

      DefaultPool defaultPool = (DefaultPool) p.getPool();

      ConcurrentHashMap<Credential, ManagedConnectionPool> mcps =
            (ConcurrentHashMap<Credential, ManagedConnectionPool>) TestUtils
            .extract(defaultPool, "pools");
      assertNotNull(mcps);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 1));

      UnifiedSecurityConnection c = noTxCf.getConnection();
      assertNotNull(c);
      assertNull(c.getUserName());
      assertNull(c.getPassword());
      UnifiedSecurityConnection c1 = noTxCf.getConnection();
      assertNotNull(c1);
      assertNull(c1.getUserName());
      assertNull(c1.getPassword());


      assertEquals(1, mcps.size());
      assertEquals(20, TestUtils.collectionSizeTenSecTimeout(listeners, 20));


      c.close();
      c1.close();

      Injection injection = new Injection();
      injection.inject(mcp, "lastIdleCheck", Long.MIN_VALUE, long.class.getName(), true);

      mcp.removeIdleConnections();
      assertEquals(1, TestUtils.collectionSizeTenSecTimeout(listeners, 1));
      assertEquals(1, mcps.size());



      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }


   /**
    * Deployment test w/o Tx
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testNotIdleSize() throws Throwable
   {
      assertNotNull(noTxCf);
      assertNotNull(dr);

      assertEquals(2, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/CapacityNoTxConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf = d.getConnectionFactories().iterator()
            .next();
      assertNotNull(dcf);

      org.ironjacamar.core.api.deploymentrepository.Pool p = dcf.getPool();
      assertNotNull(p);

      DefaultPool defaultPool = (DefaultPool) p.getPool();

      ConcurrentHashMap<Credential, ManagedConnectionPool> mcps =
            (ConcurrentHashMap<Credential, ManagedConnectionPool>) TestUtils
                  .extract(defaultPool, "pools");
      assertNotNull(mcps);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, TestUtils.collectionSizeTenSecTimeout(listeners, 1));
      //assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 10));

      UnifiedSecurityConnection c = noTxCf.getConnection();
      assertNotNull(c);
      assertNull(c.getUserName());
      assertNull(c.getPassword());

      UnifiedSecurityConnection c1 = noTxCf.getConnection();
      assertNotNull(c1);
      assertNull(c1.getUserName());
      assertNull(c1.getPassword());

      assertNotEquals(c, c1);
      assertEquals(20, TestUtils.collectionSizeTenSecTimeout(listeners, 20));

      assertEquals(1, mcps.size());

      Injection injection = new Injection();
      injection.inject(mcp, "lastIdleCheck", Long.MIN_VALUE, long.class.getName(), true);

      mcp.removeIdleConnections();
      assertEquals(2, TestUtils.collectionSizeTenSecTimeout(listeners, 2));


      c1.close();

      c.close();

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

   /**
    * Deployment test w/o Tx
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testFIFO() throws Throwable
   {
      assertNotNull(noTxCf);
      assertNotNull(dr);

      assertEquals(2, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/CapacityNoTxConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf = d.getConnectionFactories().iterator()
            .next();
      assertNotNull(dcf);

      org.ironjacamar.core.api.deploymentrepository.Pool p = dcf.getPool();
      assertNotNull(p);

      DefaultPool defaultPool = (DefaultPool) p.getPool();

      ConcurrentHashMap<Credential, ManagedConnectionPool> mcps =
            (ConcurrentHashMap<Credential, ManagedConnectionPool>) TestUtils.extract(defaultPool, "pools");
      assertNotNull(mcps);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 1));

      UnifiedSecurityConnection c = noTxCf.getConnection();
      assertNotNull(c);
      assertNull(c.getUserName());
      assertNull(c.getPassword());
      UnifiedSecurityConnection c1 = noTxCf.getConnection();
      assertNotNull(c1);
      assertNull(c1.getUserName());
      assertNull(c1.getPassword());

      assertEquals(1, mcps.size());
      assertEquals(20, TestUtils.collectionSizeTenSecTimeout(listeners, 20));

      c.close();
      c1.close();
      assertEquals(noTxCf.getConnection().getListenerIdentity(), System.identityHashCode(listeners.getFirst()));

      Injection injection = new Injection();
      injection.inject(mcp, "lastIdleCheck", Long.MIN_VALUE, long.class.getName(), true);

      mcp.removeIdleConnections();
      assertEquals(1, TestUtils.collectionSizeTenSecTimeout(listeners, 1));
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

   /**
    * Deployment test w/o Tx
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testFILO() throws Throwable
   {
      assertNotNull(noTxCfFILO);
      assertNotNull(dr);

      assertEquals(2, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/CapacityNoTxConnectionFactoryFILO");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf = d.getConnectionFactories().iterator()
            .next();
      assertNotNull(dcf);

      org.ironjacamar.core.api.deploymentrepository.Pool p = dcf.getPool();
      assertNotNull(p);

      DefaultPool defaultPool = (DefaultPool) p.getPool();

      ConcurrentHashMap<Credential, ManagedConnectionPool> mcps =
            (ConcurrentHashMap<Credential, ManagedConnectionPool>) TestUtils.extract(defaultPool, "pools");
      assertNotNull(mcps);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 1));

      UnifiedSecurityConnection c = noTxCfFILO.getConnection();
      assertNotNull(c);
      assertNull(c.getUserName());
      assertNull(c.getPassword());
      UnifiedSecurityConnection c1 = noTxCfFILO.getConnection();
      assertNotNull(c1);
      assertNull(c1.getUserName());
      assertNull(c1.getPassword());

      assertEquals(1, mcps.size());
      assertEquals(20, TestUtils.collectionSizeTenSecTimeout(listeners, 20));

      c.close();
      c1.close();
      assertEquals(noTxCfFILO.getConnection().getListenerIdentity(), System.identityHashCode(listeners.getLast()));

      Injection injection = new Injection();
      injection.inject(mcp, "lastIdleCheck", Long.MIN_VALUE, long.class.getName(), true);

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

}
