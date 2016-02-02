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

import java.util.Iterator;
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
import static org.junit.Assert.assertTrue;

/**
 * CRI test case w/ Prefill
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class FlushGracefullyTestCase
{
   /**
    * The connection factory w/o Tx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityNoTxConnectionFactory")
   private UnifiedSecurityConnectionFactory noTxCf;

   /**
    * The noTxCf w/o Tx w/o Prefill
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityNoTxNoPrefillConnectionFactory")
   private UnifiedSecurityConnectionFactory noTxNoPrefillCf;


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
    * The activation W/o Tx
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private ResourceAdaptersDescriptor createNoTxActivation() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, null,
            TransactionSupport.TransactionSupportLevel.NoTransaction,
            "UnifiedSecurityNoTxConnectionFactory", true, 10);
   }


   /**
    * The activation w7o Tx w/o Prefill
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private ResourceAdaptersDescriptor createNoTxNoPrefillActivation() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, null,
            TransactionSupport.TransactionSupportLevel.NoTransaction,
            "UnifiedSecurityNoTxNoPrefillConnectionFactory", false, 0, "Gracefully");
   }



   /**
    * Deployment test w/ ManagedConnectionPool
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testWithinManagedConnectionPool() throws Throwable
   {
      assertNotNull(noTxNoPrefillCf);
      assertNotNull(dr);

      assertEquals(2, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityNoTxNoPrefillConnectionFactory");
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

      assertEquals(0, mcps.size());

      UnifiedSecurityConnection c = noTxNoPrefillCf.getConnection("user", "pwd");
      assertNotNull(c);
      assertEquals("user", c.getUserName());
      assertEquals("pwd", c.getPassword());

      UnifiedSecurityConnection c1 = noTxNoPrefillCf.getConnection("user", "pwd");
      assertNotNull(c1);
      assertEquals("user", c.getUserName());
      assertEquals("pwd", c.getPassword());

      UnifiedSecurityConnection c2 = noTxNoPrefillCf.getConnection("user", "pwd");
      assertNotNull(c2);
      assertEquals("user", c.getUserName());
      assertEquals("pwd", c.getPassword());

      assertNotEquals(c, c1);
      assertNotEquals(c, c2);
      assertNotEquals(c1, c2);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);

      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 3));
      c1.close();

      c.fail();


      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 1));

      assertEquals(1, mcps.size());

      assertEquals(listeners.getFirst().getState(), ConnectionListener.DESTROY);

      c2.close();


      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }


   /**
    * Deployment test w/ ManagedConnectionPool
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testAcrossManagedConnectionPool() throws Throwable
   {
      assertNotNull(noTxNoPrefillCf);
      assertNotNull(dr);

      assertEquals(2, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityNoTxNoPrefillConnectionFactory");
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

      assertEquals(0, mcps.size());

      UnifiedSecurityConnection firstConnection = noTxNoPrefillCf.getConnection("user", "pwd");
      assertNotNull(firstConnection);
      assertEquals("user", firstConnection.getUserName());
      assertEquals("pwd", firstConnection.getPassword());

      UnifiedSecurityConnection secondConnection = noTxNoPrefillCf.getConnection("user1", "pwd1");
      assertNotNull(secondConnection);
      assertEquals("user1", secondConnection.getUserName());
      assertEquals("pwd1", secondConnection.getPassword());

      assertEquals(2, mcps.size());

      Iterator<ManagedConnectionPool> iter = mcps.values().iterator();
      ManagedConnectionPool firstMcp = iter.next();
      ManagedConnectionPool secondMcp = iter.next();
      assertNotNull(firstMcp);

      ConcurrentLinkedDeque<ConnectionListener> firstListeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(firstMcp, "listeners");
      assertNotNull(firstListeners);

      ConcurrentLinkedDeque<ConnectionListener> secondListeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(secondMcp, "listeners");
      assertNotNull(firstListeners);

      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(firstListeners, 1));
      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(secondListeners, 1));

      firstConnection.fail();

      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(firstListeners, 0));
      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(secondListeners, 1));

      assertEquals(2, mcps.size());

      firstConnection.close();
      secondConnection.close();


      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }


}
