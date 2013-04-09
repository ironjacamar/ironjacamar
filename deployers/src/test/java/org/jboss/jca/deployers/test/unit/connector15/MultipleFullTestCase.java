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

package org.jboss.jca.deployers.test.unit.connector15;

import org.jboss.jca.deployers.test.rars.inout.SimpleAdminObject;
import org.jboss.jca.deployers.test.rars.inout.SimpleAdminObject1;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory1;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for deploying resource adapter archives (.RAR) with multiple
 * mananged connection factories and admin objects
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(Arquillian.class)
public class MultipleFullTestCase extends Activation15TestBase
{
   //-------------------------------------------------------------------------------------||
   //---------------------- GIVEN --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      ResourceAdapterArchive raa = createDeployment("ra15outmulti.rar");
      raa.addAsManifestResource(raa.getName() + "/META-INF/" + "ironjacamar-2.xml", "ironjacamar.xml");
      return ShrinkWrap.create(ResourceAdapterArchive.class, "ra2.rar").merge(raa);
   }

   /**
    * 
    * deployment with -ra.xml overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d2", order = 2)
   public static Descriptor deployment2() throws Exception
   {
      return createDescriptor("ra15outmulti.rar/full-ra.xml");
   }

   /**
    * connection factories, registered after deployment
    */

   @Resource(mappedName = "java:/ij-activation")
   private SimpleConnectionFactory connectionFactory1;

   @Resource(mappedName = "java:/ijra-activation")
   private SimpleConnectionFactory connectionFactory2;

   @Resource(mappedName = "java:/ij-activation1")
   private SimpleConnectionFactory1 connectionFactory3;

   @Resource(mappedName = "java:/ijra-activation1")
   private SimpleConnectionFactory connectionFactory4;

   @Resource(mappedName = "java:/ijra-activation2")
   private SimpleConnectionFactory1 connectionFactory5;

   /**
    * administered objects
    */

   @Resource(mappedName = "java:/ao/ij-activation")
   private SimpleAdminObject ao1;

   @Resource(mappedName = "java:/ao/ijra-activation")
   private SimpleAdminObject ao2;

   @Resource(mappedName = "java:/ao/ij-activation1")
   private SimpleAdminObject1 ao3;

   @Resource(mappedName = "java:/ao/ijra-activation1")
   private SimpleAdminObject ao4;

   @Resource(mappedName = "java:/ao/ijra-activation2")
   private SimpleAdminObject1 ao5;

   /**
    * 
    * ironjacamar.xml activation test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void ijActivationTest1() throws Exception
   {
      allObjectsTest(connectionFactory1, "ccc", 'a', "rara", false, ao1, "aoao", 0.99);
   }

   /**
    * 
    * -ra.xml + ironjacamar.xml activation test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void raXmlActivationTest2() throws Exception
   {
      allObjectsTest(connectionFactory2, "aba", 'd', "ra-ra", true, ao2, "ao-ra", 2.18);
   }

   /**
    * 
    * ironjacamar.xml activation test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void ijActivationTest3() throws Exception
   {
      allObjectsTest1(connectionFactory3, "ddd", (byte) 5, "rara", false, ao3, "ao2", 0.01);
   }

   /**
    * 
    * -ra.xml + ironjacamar.xml activation test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void raXmlActivationTest4() throws Exception
   {
      allObjectsTest(connectionFactory4, "bab", 'e', "ra-ra", true, ao4, "ao-ra1", 3.18);
   }

   /**
    * 
    * -ra.xml + ironjacamar.xml activation test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void raXmlActivationTest5() throws Exception
   {
      allObjectsTest1(connectionFactory5, "ddd", (byte) 7, "ra-ra", true, ao5, "ao-ra2", 4.18);
   }

}
