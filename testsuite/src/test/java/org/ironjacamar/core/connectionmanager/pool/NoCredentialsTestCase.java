/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.dflt.DefaultPool;
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
import org.ironjacamar.rars.security.UnifiedSecurityCri;
import org.ironjacamar.util.TestUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.spi.TransactionSupport;
import javax.transaction.UserTransaction;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * No credential test case
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class NoCredentialsTestCase
{
   /**
    * The noTxCf w/o Tx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityNoTxConnectionFactory")
   private UnifiedSecurityConnectionFactory noTxCf;


   /**
    * The connection factory w/ Local Tx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityLocalTxConnectionFactory")
   private UnifiedSecurityConnectionFactory localTxCf;


   /**
    * The connection factory w/ XA Tx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityXATxConnectionFactory")
   private UnifiedSecurityConnectionFactory xaTxCf;

   /**
    * The deployment repository
    */
   @Inject
   private static DeploymentRepository dr;


   /** The UserTransaction */
   @Inject
   private static UserTransaction ut;

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
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, null,
            TransactionSupport.TransactionSupportLevel.NoTransaction,
            "UnifiedSecurityNoTxConnectionFactory");
   }

   /**
    * The activation
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private ResourceAdaptersDescriptor createActivationLocalTx() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, null,
            TransactionSupport.TransactionSupportLevel.LocalTransaction,
            "UnifiedSecurityLocalTxConnectionFactory");
   }

   /**
    * The activation
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 4)
   private ResourceAdaptersDescriptor createActivationXaTx() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, null,
            TransactionSupport.TransactionSupportLevel.XATransaction,
            "UnifiedSecurityXATxConnectionFactory");
   }



   /**
    * Deployment test w/ LocalTx
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testLocalTx() throws Throwable
   {
      assertNotNull(localTxCf);
      assertNotNull(dr);

      assertEquals(3, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityLocalTxConnectionFactory");
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

      ut.begin();

      UnifiedSecurityConnection c = localTxCf.getConnection();
      assertNotNull(c);
      assertNull(c.getUserName());
      assertNull(c.getPassword());

      UnifiedSecurityConnection c1 = localTxCf.getConnection();
      assertNotNull(c1);
      assertNull(c1.getUserName());
      assertNull(c1.getPassword());

      assertNotEquals(c, c1);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertNull(((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()));

      c.close();

      c1.close();

      ut.commit();

      assertEquals(FREE, cl.getState());
      assertEquals(1, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

   /**
    * Deployment test w/ XATx
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testXATx() throws Throwable
   {
      assertNotNull(xaTxCf);
      assertNotNull(dr);

      assertEquals(3, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityXATxConnectionFactory");
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

      ut.begin();

      UnifiedSecurityConnection c = xaTxCf.getConnection();
      assertNotNull(c);
      assertNull(c.getUserName());
      assertNull(c.getPassword());


      UnifiedSecurityConnection c1 = xaTxCf.getConnection();
      assertNotNull(c1);
      assertNull(c1.getUserName());
      assertNull(c1.getPassword());

      assertNotEquals(c, c1);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertNull(((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()));

      c.close();
      c1.close();

      ut.commit();

      assertEquals(FREE, cl.getState());
      assertEquals(1, listeners.size());
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
   public void testNoTx() throws Throwable
   {
      assertNotNull(noTxCf);
      assertNotNull(dr);

      assertEquals(3, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityNoTxConnectionFactory");
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

      UnifiedSecurityConnection c = noTxCf.getConnection();
      assertNotNull(c);
      assertNull(c.getUserName());
      assertNull(c.getPassword());

      UnifiedSecurityConnection c1 = noTxCf.getConnection();
      assertNotNull(c1);
      assertNull(c1.getUserName());
      assertNull(c1.getPassword());

      assertNotEquals(c, c1);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(2, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertNull(((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()));

      c1.close();
      assertEquals(FREE, cl.getState());

      cl = listeners.getLast();

      assertEquals(IN_USE, cl.getState());
      assertNull(((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()));

      c.close();

      assertEquals(FREE, cl.getState());
      assertEquals(2, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }
}
