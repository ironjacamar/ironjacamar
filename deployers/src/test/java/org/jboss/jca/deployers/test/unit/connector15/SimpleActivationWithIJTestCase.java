/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * 
 * An ActivationTestCase. Deploy archive, containing ironjacamar.xml. 
 * It should be activated by default. Trying also activate it with
 * -ra.xml descriptor 
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class SimpleActivationWithIJTestCase extends ActivationTestBase
{

   /**
    * 
    * first deployment
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive deployment1() throws Exception
   {
      return createIJDeployment("ra15inout.rar", "ij1.rar");
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
      return createDescriptor("ra15inout.rar/ij-ra.xml");
   }

   /**
    * connection factories, registered after deployment
    */

   @Resource(mappedName = "java:/ij-activation")
   private SimpleConnectionFactory connectionFactory1;

   @Resource(mappedName = "java:/ijra-activation")
   private SimpleConnectionFactory connectionFactory2;

   /**
    * administered objects
    */

   @Resource(mappedName = "java:/ao/ij-activation")
   private SimpleAdminObject ao1;

   @Resource(mappedName = "java:/ao/ijra-activation")
   private SimpleAdminObject ao2;

   /**
    * 
    * ironjacamar.xml activation test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void ijActivationTest() throws Exception
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
   public void raXmlActivationTest() throws Exception
   {
      allObjectsTest(connectionFactory2, "aba", 'd', "ra-ra", true, ao2, "ao-ra", 2.18);
   }

   /**
    * Test metadata
    *     
    * @throws Exception in case of error
    */
   @Test
   public void testMetaDatas() throws Exception
   {
      testMetaData(2);
   }

}
