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
package org.ironjacamar.core.connectionmanager.pool.dflt;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.test.TestConnection;
import org.ironjacamar.rars.test.TestConnectionFactory;
import org.ironjacamar.util.TestUtils;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Back ground validation test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class BackgroundValidationTestCase
{
   /** The test connection factory */
   @Resource(mappedName = "java:/eis/TestConnectionFactory")
   private static TestConnectionFactory cf;

   /** The deployment repository */
   @Inject
   private static DeploymentRepository dr;
   
   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createTestRar();
   }
   
   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTestDeployment(false, 1);
   }
   
   /**
    * Back ground validation
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testBackgroundValidation() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      org.ironjacamar.core.api.deploymentrepository.Pool p = dcf.getPool();
      assertNotNull(p);

      DefaultPool defaultPool = (DefaultPool)p.getPool();

      ConcurrentHashMap<Credential, ManagedConnectionPool> mcps =
         (ConcurrentHashMap<Credential, ManagedConnectionPool>)TestUtils.extract(defaultPool, "pools");
      assertNotNull(mcps);
      assertEquals(0, mcps.size());
      
      TestConnection c = cf.getConnection();
      assertNotNull(c);

      assertEquals(1, c.getFailureCount());

      c.close();
      
      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      mcp.validateConnections();

      c = cf.getConnection();
      assertNotNull(c);

      assertEquals(0, c.getFailureCount());
      c.close();

      // We cheat and shutdown the pool to clear out mcps
      defaultPool.shutdown();
   }
}
