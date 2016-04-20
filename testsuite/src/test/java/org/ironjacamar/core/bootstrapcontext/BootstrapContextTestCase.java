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
package org.ironjacamar.core.bootstrapcontext;

import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.InputStreamDescriptor;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.test.TestConnection;
import org.ironjacamar.rars.test.TestConnectionFactory;

import javax.annotation.Resource;
import javax.resource.spi.BootstrapContext;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Basic WorkManager test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class BootstrapContextTestCase
{
   /** The user transaction */
   @Resource(mappedName = "java:/eis/ConnectionFactory")
   private TestConnectionFactory cf;

   /** The user transaction */
   @Resource(mappedName = "java:/eis/ConnectionFactory2")
   private TestConnectionFactory cf2;


   @Deployment(order = 1)
   private InputStreamDescriptor createBootstrapContext() throws Throwable
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("second-bc.xml",
            cl.getResourceAsStream("second-bc.xml"));
      return isd;

   }

   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createTestRar();
   }

   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTestDeployment(0, null, 0, "ConnectionFactory", "TestAO", null);
   }

   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 4)
   private ResourceAdaptersDescriptor createActivation2() throws Throwable
   {
      return ResourceAdapterFactory.createTestDeployment(0, null, 0, "ConnectionFactory2", "TestAO2", null);
   }

   
   /**
    * Deployment
    * @throws Throwable In case of an error
    */
   @Test
   public void testDeployment() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(cf2);

      TestConnection conn = cf.getConnection();
      assertNotNull(conn);
      TestConnection conn2 = cf2.getConnection();
      assertNotNull(conn2);

      assertNotEquals(conn, conn2);

      assertEquals(conn.getWorkManager(), conn2.getWorkManager());

      assertTrue(conn.getBootstrapContext() instanceof BootstrapContext);
      assertTrue(conn.getBootstrapContext() instanceof org.ironjacamar.core.api.bootstrapcontext.BootstrapContext);
      assertTrue(conn.getBootstrapContext() instanceof CloneableBootstrapContext);

      assertEquals(((CloneableBootstrapContext) conn.getBootstrapContext()).getId(),
            ((CloneableBootstrapContext) conn2.getBootstrapContext()).getId());
      assertEquals(((CloneableBootstrapContext) conn.getBootstrapContext()).getName(),
            ((CloneableBootstrapContext) conn2.getBootstrapContext()).getName());
      assertEquals(conn.getBootstrapContext().getTransactionSynchronizationRegistry(),
            conn2.getBootstrapContext().getTransactionSynchronizationRegistry());

      conn.close();
      conn2.close();
   }
}
