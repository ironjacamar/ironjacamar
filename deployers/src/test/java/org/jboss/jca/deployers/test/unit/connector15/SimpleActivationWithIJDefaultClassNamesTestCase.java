/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * Deployment with ironjacamar.xml file, containing connection-definition and
 * admin-object elements without class names
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
@Ignore("JBJCA-915")
public class SimpleActivationWithIJDefaultClassNamesTestCase extends Activation15TestBase
{

   /**
    * 
    * ij deployment
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(order = 2, name = "d")
   public static ResourceAdapterArchive deployment1() throws Exception
   {
      ResourceAdapterArchive ra = createDeployment("ra15inout.rar");
      ra.addAsManifestResource("ra15inout.rar/META-INF/ironjacamar-wc.xml", "ironjacamar.xml");
      return ShrinkWrap.create(ResourceAdapterArchive.class, "ij2.rar").merge(ra);
   }

   /**
    * 
    * first deployment
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive deployment0() throws Exception
   {
      return createDeployment("ra15inout.rar");
   }

   /**
    * connection factories, registered after deployment
    */

   @Resource(mappedName = "java:/ijwc-activation")
   private SimpleConnectionFactory connectionFactory1;

   /**
    * administered objects
    */

   @Resource(mappedName = "java:/ao/ijwc-activation")
   private SimpleAdminObject ao1;

   /**
    * 
    *  test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void test() throws Exception
   {
      allObjectsTest(connectionFactory1, "ccc", 'a', "rara", false, ao1, "aoao", 0.99);
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
