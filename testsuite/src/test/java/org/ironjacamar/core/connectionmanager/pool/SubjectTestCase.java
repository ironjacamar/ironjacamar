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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.UserTransaction;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Subject test case
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class SubjectTestCase
{
   /**
    * The connection factory w/o Tx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityNoTxConnectionFactory")
   private UnifiedSecurityConnectionFactory noTxCf;

   /**
    * The connection factory w/ LocalTx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityLocalTxConnectionFactory")
   private UnifiedSecurityConnectionFactory localTxCf;

   /**
    * The connection factory w/ XaTx
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityXATxConnectionFactory")
   private UnifiedSecurityConnectionFactory xaTxCf;


   /**
    * The deployment repository
    */
   @Inject
   private static DeploymentRepository dr;

   @Inject
   private static DefaultSubjectFactory defaultSubjectFactory;


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
    * The activation W/o Tx
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private ResourceAdaptersDescriptor createNoTxActivation() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, "DefaultSecurityDomain",
            TransactionSupport.TransactionSupportLevel.NoTransaction,
            "UnifiedSecurityNoTxConnectionFactory");
   }

   /**
    * The activation W/ LocalTx
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private ResourceAdaptersDescriptor createLocalTxActivation() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, "DefaultSecurityDomain",
            TransactionSupport.TransactionSupportLevel.LocalTransaction,
            "UnifiedSecurityLocalTxConnectionFactory");
   }


   /**
    * The activation W/ XATx
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 4)
   private ResourceAdaptersDescriptor createXATxActivation() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, "DefaultSecurityDomain",
            TransactionSupport.TransactionSupportLevel.XATransaction,
            "UnifiedSecurityXATxConnectionFactory");
   }


   /**
    * test w/o Tx, 2 calls w/ same credentials
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testOneCredentialNoTxWithoutTransaction() throws Throwable
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
      assertEquals("user", c.getUserName());

      UnifiedSecurityConnection c1 = noTxCf.getConnection();
      assertNotNull(c1);
      assertEquals("user", c1.getUserName());

      assertEquals(1, mcps.size());

      assertNotEquals(c, c1);

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(2, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertNotNull(cl.getCredential().getSubject());
      PasswordCredential firstPC = getPasswordCredential(cl.getCredential().getSubject());
      assertEquals("user", firstPC.getUserName());

      c1.close();

      assertEquals(FREE, cl.getState());
      cl = listeners.getLast();
      assertEquals(IN_USE, cl.getState());
      assertNotNull(cl.getCredential().getSubject());
      firstPC = getPasswordCredential(cl.getCredential().getSubject());
      assertEquals("user", firstPC.getUserName());

      c.close();
      assertEquals(FREE, cl.getState());


      assertEquals(2, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

   /**
    * test w/o Tx 2 w/ 2 different credential -> 2 connections -> 2 CLs
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testTwoCredentialsNoTxWithoutTransaction() throws Throwable
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

      UnifiedSecurityConnection firstConnection = noTxCf.getConnection();
      assertNotNull(firstConnection);
      assertEquals("user", firstConnection.getUserName());

      defaultSubjectFactory.setUserName("user1");
      defaultSubjectFactory.setPassword("pwd1");
      UnifiedSecurityConnection secondConnection =
            noTxCf.getConnection();
      assertNotNull(secondConnection);
      assertEquals("user1", secondConnection.getUserName());

      assertEquals(2, mcps.size());

      assertNotEquals(firstConnection, secondConnection);

      Iterator<ManagedConnectionPool> iter = mcps.values().iterator();
      ManagedConnectionPool firstMcp = iter.next();
      assertNotNull(firstMcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(firstMcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener firstCL = listeners.getFirst();
      assertEquals(IN_USE, firstCL.getState());
      assertNotNull(firstCL.getCredential().getSubject());
      PasswordCredential firstPC = getPasswordCredential(firstCL.getCredential().getSubject());
      assertEquals("user1", firstPC.getUserName());

      secondConnection.close();
      assertEquals(FREE, firstCL.getState());
      assertEquals(1, listeners.size());

      ManagedConnectionPool secondMcp = iter.next();
      assertNotNull(secondMcp);


      listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(secondMcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener secondCL = listeners.getFirst();
      assertEquals(IN_USE, secondCL.getState());
      assertNotNull(secondCL.getCredential().getSubject());
      firstPC = getPasswordCredential(secondCL.getCredential().getSubject());
      assertEquals("user", firstPC.getUserName());

      firstConnection.close();

      assertEquals(FREE, secondCL.getState());
      assertEquals(FREE, firstCL.getState());

      assertNotEquals(firstCL, secondCL);

      assertEquals(2, mcps.size());

      // We cheat and shutdown the pool to clear out mcps

      defaultSubjectFactory.setUserName("user");
      defaultSubjectFactory.setPassword("pwd");

      defaultPool.shutdown();
   }

   /**
    * test w/ LocalTx, calls w/ same credentials -> 1 CL
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testOneCredentialLocalTxWithTransaction() throws Throwable
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
      assertEquals("user", c.getUserName());

      UnifiedSecurityConnection c1 = localTxCf.getConnection();
      assertNotNull(c1);
      assertEquals("user", c1.getUserName());

      assertEquals(1, mcps.size());

      assertNotEquals(c, c1);

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertNotNull(cl.getCredential().getSubject());
      PasswordCredential firstPC = getPasswordCredential(cl.getCredential().getSubject());
      assertEquals("user", firstPC.getUserName());

      c1.close();

      assertEquals(IN_USE, cl.getState());
      assertNotNull(cl.getCredential().getSubject());
      firstPC = getPasswordCredential(cl.getCredential().getSubject());
      assertEquals("user", firstPC.getUserName());

      c.close();
      ut.commit();

      assertEquals(FREE, cl.getState());


      assertEquals(1, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }


   /**
    * test w/ XATx, calls w/ same credentials -> 1 CL
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testOneCredentialXATxWithTransaction() throws Throwable
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
      assertEquals("user", c.getUserName());

      UnifiedSecurityConnection c1 = xaTxCf.getConnection();
      assertNotNull(c1);
      assertEquals("user", c1.getUserName());

      assertEquals(1, mcps.size());

      assertNotEquals(c, c1);

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertNotNull(cl.getCredential().getSubject());
      PasswordCredential firstPC = getPasswordCredential(cl.getCredential().getSubject());
      assertEquals("user", firstPC.getUserName());

      c1.close();

      assertEquals(IN_USE, cl.getState());
      assertNotNull(cl.getCredential().getSubject());
      firstPC = getPasswordCredential(cl.getCredential().getSubject());
      assertEquals("user", firstPC.getUserName());

      c.close();
      ut.commit();

      assertEquals(FREE, cl.getState());


      assertEquals(1, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

   /**
    * test w/ XA Tx, 2 calls w/ different credentials -> 2 CLS
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testTwoCredentialsXATxWithTransaction () throws Throwable
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

      UnifiedSecurityConnection firstConnection = xaTxCf.getConnection();
      assertNotNull(firstConnection);
      assertEquals("user", firstConnection.getUserName());

      defaultSubjectFactory.setUserName("user1");
      defaultSubjectFactory.setPassword("pwd1");

      UnifiedSecurityConnection secondConnection = xaTxCf.getConnection();

      assertNotNull(secondConnection);
      assertEquals("user1", secondConnection.getUserName());

      assertEquals(2, mcps.size());

      assertNotEquals(firstConnection, secondConnection);

      Iterator<ManagedConnectionPool> iter = mcps.values().iterator();
      ManagedConnectionPool firstMcp = iter.next();
      assertNotNull(firstMcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(firstMcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener firstCL = listeners.getFirst();
      assertEquals(IN_USE, firstCL.getState());

      assertNotNull(firstCL.getCredential().getSubject());
      PasswordCredential firstPC = getPasswordCredential(firstCL.getCredential().getSubject());
      assertEquals("user1", firstPC.getUserName());

      firstConnection.close();

      assertEquals(IN_USE, firstCL.getState());
      assertEquals(1, listeners.size());

      ManagedConnectionPool secondMcp = iter.next();
      assertNotNull(secondMcp);


      listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(secondMcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener secondCL = listeners.getFirst();
      assertEquals(IN_USE, secondCL.getState());

      assertNotNull(secondCL.getCredential().getSubject());
      PasswordCredential secondPC = getPasswordCredential(secondCL.getCredential().getSubject());
      assertEquals("user", secondPC.getUserName());


      secondConnection.close();

      assertEquals(IN_USE, secondCL.getState());

      ut.commit();

      assertEquals(FREE, firstCL.getState());
      assertEquals(FREE, secondCL.getState());

      assertEquals(1, listeners.size());
      assertEquals(2, mcps.size());

      assertNotEquals(firstCL, secondCL);


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
