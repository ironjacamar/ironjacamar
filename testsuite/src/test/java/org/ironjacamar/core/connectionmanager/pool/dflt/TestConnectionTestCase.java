/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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
import org.ironjacamar.core.spi.security.SubjectFactory;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.test.TestConnectionFactory;
import org.ironjacamar.rars.test.TestConnectionRequestInfo;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test connection test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class TestConnectionTestCase
{
   /** The test connection factory */
   @Resource(mappedName = "java:/eis/TestConnectionFactory")
   private static TestConnectionFactory cf;

   /** The deployment repository */
   @Inject
   private static DeploymentRepository dr;
   
   /** The subject factory */
   @Inject
   @Named("DefaultSubjectFactory")
   private static SubjectFactory sf;
   
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
      return ResourceAdapterFactory.createTestDeployment(0, null, 0);
   }
   
   /**
    * testConnection
    * @throws Exception for exception.
    */
   @Test
   public void testConnection() throws Exception
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
      
      assertTrue(defaultPool.testConnection());      
   }

   /**
    * testConnection w/ CRI and Subject
    * @throws Exception for exception.
    */
   @Test
   public void testConnectionWithCRIAndSubject() throws Exception
   {
      assertNotNull(cf);
      assertNotNull(dr);
      assertNotNull(sf);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      org.ironjacamar.core.api.deploymentrepository.Pool p = dcf.getPool();
      assertNotNull(p);

      DefaultPool defaultPool = (DefaultPool)p.getPool();
      
      assertTrue(defaultPool.testConnection(new TestConnectionRequestInfo(), sf.createSubject("")));      
   }
}
