/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.deployers.test.unit.connector10;

import org.jboss.jca.deployers.test.DeploymentTestBase;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory1;
import org.jboss.jca.embedded.Embedded;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for re-deploying resource adapter archives and descriptors 
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class ReDeploymentTestCase extends DeploymentTestBase
{

   @ArquillianResource
   private static Embedded embedded;

   
   /**
    * 
    * Get connection factory and make basic test
    * 
    * @param cfName JNDI name for lookup
    * @param first property value
    * @param second property value
    * @throws Throwable in case of error
    */
   public void connectionTest(String cfName, String first, byte second) throws Throwable
   {
      InitialContext context = new InitialContext();
      SimpleConnectionFactory1 cf = (SimpleConnectionFactory1) context.lookup(cfName);
      testSimpleCF1(cf, first,  second);
      if (context != null)
      {
         try
         {
            context.close();
         }
         catch (NamingException ne)
         {
            // Ignore
         }
      }

   }

   /**
    * 
    * testReDeployRa
    * 
    * @throws Throwable in case of error
    */
   @Test
   public void testReDeployRa() throws Throwable
   {
      ResourceAdapterArchive ra = createDeployment("ra10dtdoutoverwrite.rar");
      embedded.deploy(ra);
      embedded.undeploy(ra);
      embedded.deploy(ra);
      connectionTest("java:/eis/ra10dtdoutoverwrite", "bbb", (byte) 5);
      embedded.undeploy(ra);
   }

   /**
    * 
    * testReDeployIj
    * 
    * @throws Throwable in case of error
    */

   @Test
   public void testReDeployIj() throws Throwable
   {
      ResourceAdapterArchive ra = createIJDeployment("ra10dtdoutoverwrite.rar", "ij2.rar");
      embedded.deploy(ra);
      embedded.undeploy(ra);
      embedded.deploy(ra);
      connectionTest("java:/ij-activation2", "ddd", (byte) 4);
      embedded.undeploy(ra);
   }

   /**
    * 
    * testReDeployIjRa
    * 
    * @throws Throwable in case of error
    */
   @Test
   public void testReDeployIjRa() throws Throwable
   {
      ResourceAdapterArchive ra = createIJDeployment("ra10dtdoutoverwrite.rar", "ij2.rar");
      Descriptor d = createDescriptor("ra10dtdoutoverwrite.rar/ij-ra.xml");
      embedded.deploy(ra);
      embedded.deploy(d);
      embedded.undeploy(ra);
      embedded.deploy(ra);
      connectionTest("java:/ra-activation4", "fff", (byte) 3);
      log.info("///first check");
      embedded.undeploy(d);
      d = createDescriptor("ra10dtdoutoverwrite.rar/ij-ra.xml");
      embedded.deploy(d);
      connectionTest("java:/ra-activation4", "fff", (byte) 3);
      log.info("///second check");
      embedded.undeploy(d);
      embedded.undeploy(ra);
      embedded.deploy(ra);
      d = createDescriptor("ra10dtdoutoverwrite.rar/ij-ra.xml");
      embedded.deploy(d);
      connectionTest("java:/ra-activation4", "fff", (byte) 3);
      log.info("///third check");
      embedded.undeploy(d);
      embedded.undeploy(ra);
   }

}
