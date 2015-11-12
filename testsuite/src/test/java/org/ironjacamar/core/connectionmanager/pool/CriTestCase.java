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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Basic CRI test case
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class CriTestCase
{
   /**
    * The connection factory
    */
   @Resource(mappedName = "java:/eis/UnifiedSecurityConnectionFactory")
   private UnifiedSecurityConnectionFactory cf;

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
    * The activation
    *
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createUnifiedSecurityDeployment(null, null);
   }

   /**
    * Deployment
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testOneConnection() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityConnectionFactory");
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

      UnifiedSecurityConnection c = cf.getConnection("user", "pwd");
      assertNotNull(c);
      assertEquals("user", c.getUserName());
      assertEquals("pwd", c.getPassword());

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertEquals("user", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getUserName());
      assertEquals("pwd", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getPassword());

      c.close();

      assertEquals(FREE, cl.getState());
      assertEquals(1, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }


   /**
    * Deployment
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testOneConnectionTwoCall() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityConnectionFactory");
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

      UnifiedSecurityConnection c =  cf.getConnection("user", "pwd");
      assertNotNull(c);
      assertEquals("user", c.getUserName());
      assertEquals("pwd", c.getPassword());

      UnifiedSecurityConnection c1 = cf.getConnection("user", "pwd");
      assertNotNull(c1);
      assertEquals("user", c1.getUserName());
      assertEquals("pwd", c1.getPassword());

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(2, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());
      assertEquals("user", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getUserName());
      assertEquals("pwd", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getPassword());

      c1.close();

      assertEquals(FREE, cl.getState());
      cl = listeners.getLast();
      assertEquals(IN_USE, cl.getState());
      assertEquals("user", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getUserName());
      assertEquals("pwd", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getPassword());

      c.close();

      assertEquals(FREE, cl.getState());


      assertEquals(2, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

   /**
    * Deployment
    *
    * @throws Throwable In case of an error
    */
   @Test
   public void testTwoConnection() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr
            .findByJndi("java:/eis/UnifiedSecurityConnectionFactory");
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

      UnifiedSecurityConnection firstConnection = cf.getConnection("user", "pwd");
      assertNotNull(firstConnection);
      assertEquals("user", firstConnection.getUserName());
      assertEquals("pwd", firstConnection.getPassword());

      UnifiedSecurityConnection secondConnection =
            cf.getConnection("user1", "pwd1");
      assertNotNull(secondConnection);
      assertEquals("user1", secondConnection.getUserName());
      assertEquals("pwd1", secondConnection.getPassword());

      assertEquals(2, mcps.size());

      Iterator<ManagedConnectionPool> iter = mcps.values().iterator();
      ManagedConnectionPool firstMcp = iter.next();
      assertNotNull(firstMcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(firstMcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());

      assertEquals("user", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getUserName());
      assertEquals("pwd", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getPassword());

      firstConnection.close();

      assertEquals(FREE, cl.getState());
      assertEquals(1, listeners.size());

      ManagedConnectionPool secondMcp = iter.next();
      assertNotNull(secondMcp);


      listeners = (ConcurrentLinkedDeque<ConnectionListener>) TestUtils
            .extract(secondMcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());

      assertEquals("user1", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getUserName());
      assertEquals("pwd1", ((UnifiedSecurityCri) cl.getCredential().getConnectionRequestInfo()).getPassword());


      secondConnection.close();

      assertEquals(FREE, cl.getState());


      assertEquals(1, listeners.size());
      assertEquals(2, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }

}
