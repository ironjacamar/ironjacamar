/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.test.deployers.spec.rars.testcases;

import org.jboss.jca.embedded.dsl.InputStreamDescriptor;
import org.jboss.jca.test.deployers.spec.ArquillianJCATestUtils;
import org.jboss.jca.test.deployers.spec.rars.multiple.MultipleAdminObject1;
import org.jboss.jca.test.deployers.spec.rars.multiple.MultipleAdminObject2;
import org.jboss.jca.test.deployers.spec.rars.multiple.MultipleConnectionFactory1;
import org.jboss.jca.test.deployers.spec.rars.multiple.MultipleConnectionFactory2;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Test cases for deploying resource adapter archives (.RAR) using -ra.xml files
 * for activation
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class MultipleFullRaXmlTestCase
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
      String archiveName = "multiple-full.rar";
      String packageName = "org.jboss.jca.test.deployers.spec.rars.multiple";
      ResourceAdapterArchive raa = ArquillianJCATestUtils.buidShrinkwrapRa(archiveName, packageName);
      raa.addAsManifestResource(archiveName + "/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("multiple-full-ra.xml",
                                                            cl.getResourceAsStream("multiple-full-ra.xml"));
      return isd;
   }

   //-------------------------------------------------------------------------------------||
   //---------------------- WHEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   //
   @Resource(mappedName = "java:/eis/MultipleConnectionFactory1")
   private MultipleConnectionFactory1 connectionFactory1;

   @Resource(mappedName = "java:/eis/MultipleConnectionFactory1b")
   private MultipleConnectionFactory1 connectionFactory1b;


   @Resource(mappedName = "java:/eis/MultipleConnectionFactory2")
   private MultipleConnectionFactory2 connectionFactory2;

   @Resource(mappedName = "java:/eis/MultipleAdminObject1")
   private MultipleAdminObject1 adminObject1;

   @Resource(mappedName = "java:/eis/MultipleAdminObject1b")
   private MultipleAdminObject1 adminObject1b;


   @Resource(mappedName = "java:/eis/MultipleAdminObject2")
   private MultipleAdminObject2 adminObject2;

   //-------------------------------------------------------------------------------------||
   //---------------------- THEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      assertNotNull(connectionFactory1);
      assertNotNull(connectionFactory1b);
      assertThat(connectionFactory1.getMcf().getName(), equalTo("A1"));
      assertThat(connectionFactory1b.getMcf().getName(), equalTo("A2"));
      assertThat(connectionFactory1.getMcf().getSecond(), equalTo("value"));
      assertThat(connectionFactory1b.getMcf().getSecond(), is(nullValue()));
      assertThat(connectionFactory1.getMcf().getThird(), equalTo("myValue"));
      assertThat(connectionFactory1b.getMcf().getThird(), equalTo("default"));

      assertNotNull(connectionFactory2);
      assertThat(connectionFactory2.getMcf().getName(), equalTo("B"));
      assertNotNull(adminObject1);
      assertNotNull(adminObject1b);
      assertNotNull(adminObject2);
      assertThat(adminObject1.getName(), equalTo("C1"));
      assertThat(adminObject1b.getName(), equalTo("C2"));
      assertThat(adminObject1.getSecond(), equalTo("value"));
      assertThat(adminObject1b.getSecond(), is(nullValue()));
      assertThat(adminObject1.getThird(), equalTo("myValue"));
      assertThat(adminObject1b.getThird(), equalTo("default"));

      assertThat(adminObject2.getName(), equalTo("D"));

   }
}
