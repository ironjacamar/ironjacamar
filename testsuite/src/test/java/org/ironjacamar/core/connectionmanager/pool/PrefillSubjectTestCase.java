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
import org.ironjacamar.core.security.DefaultSubjectFactory;
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Subject test case w/ Prefill
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class PrefillSubjectTestCase
{
   /**
    * The connection factory w/o Tx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityNoTxConnectionFactory")
   private UnifiedSecurityConnectionFactory noTxCf;

   /**
    * The deployment repository
    */
   @Inject
   private static DeploymentRepository dr;

   @Inject
   private static DefaultSubjectFactory defaultSubjectFactory;

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
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, "DefaultSecurityDomain",
            TransactionSupport.TransactionSupportLevel.NoTransaction,
            "UnifiedSecurityNoTxConnectionFactory", true, 10);
   }

   /**
    * test w/o Tx 2 w/ 2 different credential -> 2 connections -> 2 CLs
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testMinPoolSize() throws Throwable
   {
      assertNotNull(noTxCf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());

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
      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 10));

      UnifiedSecurityConnection firstConnection = noTxCf.getConnection();
      assertNotNull(firstConnection);
      assertEquals("user", firstConnection.getUserName());
      assertEquals(1, mcps.size());

      firstConnection.fail();

      assertEquals(listeners.size(), 9);

      defaultPool.prefill();

      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 10));


      firstConnection.close();
      // We cheat and shutdown the pool to clear out mcps


      defaultPool.shutdown();
   }

   /**
    * test w/o Tx 2 w/ 2 different credential -> 2 connections -> 2 CLs
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testInitialPoolSize() throws Throwable
   {
      assertNotNull(noTxCf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());

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
      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertTrue(TestUtils.isCorrectCollectionSizeTenSecTimeout(listeners, 10));

      UnifiedSecurityConnection firstConnection = noTxCf.getConnection();
      assertNotNull(firstConnection);
      assertEquals("user", firstConnection.getUserName());
      assertEquals(1, mcps.size());

      defaultSubjectFactory.setUserName("user1");
      defaultSubjectFactory.setPassword("pwd1");
      UnifiedSecurityConnection secondConnection =
            noTxCf.getConnection();
      assertNotNull(secondConnection);
      assertEquals("user1", secondConnection.getUserName());

      assertEquals(2, mcps.size());

      assertNotEquals(firstConnection, secondConnection);


      firstConnection.close();
      secondConnection.close();
      // We cheat and shutdown the pool to clear out mcps

      defaultSubjectFactory.setUserName("user");
      defaultSubjectFactory.setPassword("pwd");

      defaultPool.shutdown();
   }

   private PasswordCredential getPasswordCredential(Subject s)
   {
      Set<PasswordCredential> credentials = this.getPasswordCredentials(s);
      assertNotNull(credentials);
      assertFalse(credentials.isEmpty());
      return credentials.iterator().next();
   }

   /**
    * Get the PasswordCredential from the Subject
    *
    * @param subject The subject
    * @return The instances
    */
   private Set<PasswordCredential> getPasswordCredentials(final Subject subject)
   {
      if (System.getSecurityManager() == null)
         return subject.getPrivateCredentials(PasswordCredential.class);

      return AccessController.doPrivileged(
            (PrivilegedAction<Set<PasswordCredential>>) () -> subject.getPrivateCredentials(PasswordCredential.class));
   }

}
