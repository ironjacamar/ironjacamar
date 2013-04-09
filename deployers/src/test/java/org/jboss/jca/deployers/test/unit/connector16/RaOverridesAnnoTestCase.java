/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.test.unit.connector16;

import org.jboss.jca.deployers.test.rars.anno.AnnoAdminObject;
import org.jboss.jca.deployers.test.rars.anno.AnnoConnectionFactory;
import org.jboss.jca.deployers.test.rars.anno.AnnoMessageListener;
import org.jboss.jca.deployers.test.rars.anno.AnnoMessageListener1;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Multiple activation of annotated RA
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class RaOverridesAnnoTestCase extends Activation16TestBase
{

   /**
    * Define the deployment
    *
    * @return The deployment archive
    * @throws Exception in case of error
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      ResourceAdapterArchive raa = buidShrinkwrapRa("ra16anno.rar", AnnoConnectionFactory.class.getPackage());
      raa.addAsManifestResource("ra16anno.rar/META-INF/ra-over.xml", "ra.xml");
      addIJXml(raa);
      return raa;
   }

   /**
    * 
    * Define descriptor deployment
    * 
    * @return descriptor
    * @throws Exception in case of error
    */
   @Deployment(order = 2)
   public static Descriptor create() throws Exception
   {
      return createDescriptor("ra16anno.rar/over-ra.xml");
   }

   /** Resource */
   @Resource(mappedName = "java:/eis/ij")
   private AnnoConnectionFactory connectionFactory1;

   /** Resource */
   @Resource(mappedName = "java:/eis/ao/ij")
   private AnnoAdminObject adminObject;

   /** Resource */
   @Resource(mappedName = "java:/eis/ra")
   private AnnoConnectionFactory connectionFactory2;

   /** Resource */
   @Resource(mappedName = "java:/eis/ao/ra")
   private AnnoAdminObject adminObject2;

   /**
    * Test getConnection
    *
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testConnection1() throws Throwable
   {
      testConnection(connectionFactory1, (byte) 23, (short) 0);
   }

   /**
    * Test getConnection
    *
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testConnection2() throws Throwable
   {
      testConnection(connectionFactory2, (byte) 23, (short) 0);
   }

   /**
    * Test admin objects
   *
   * @exception Throwable Thrown if case of an error
   */
   @Test
   @Ignore
   public void testAdminOjbect1() throws Throwable
   {
      testAdminOjbect(adminObject, 54321, true);
   }

   /**
    * Test admin objects
   *
   * @exception Throwable Thrown if case of an error
   */
   @Test
   @Ignore
   public void testAdminOjbect2() throws Throwable
   {
      testAdminOjbect(adminObject2, 54321, true);
   }

   /**
    * test activation 1
    * 
    * @exception Throwable
    *                Thrown if case of an error
    */
   @Test
   @Ignore
   public void testActivation1() throws Throwable
   {
      testActivation(AnnoMessageListener.class, 'U', 0.5, "G", 5);
   }
   /**
    * test activation 1
    * 
    * @exception Throwable
    *                Thrown if case of an error
    */
   @Test
   public void testActivation2() throws Throwable
   {
      testActivation(AnnoMessageListener1.class, 'C', 0.5, "G", 5);
   }

   /**
    * Test metadata
    *     
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testMetaData() throws Throwable
   {
      testMetaData(2);
   }

}
