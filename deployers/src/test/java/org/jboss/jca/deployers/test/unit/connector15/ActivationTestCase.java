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

import org.jboss.jca.common.metadata.ra.ra15.Connector15Impl;
import org.jboss.jca.deployers.test.DeploymentTestBase;
import org.jboss.jca.deployers.test.rars.inout.SimpleAdminObject;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleManagedConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleResourceAdapter;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * 
 * A ActivationTestCase.
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class ActivationTestCase extends DeploymentTestBase
{
   /**
    * 
    * checks, if metadata is of appropriate type
    * 
    * @param piId - metadata name
    * @throws Exception in case of error
    */
   @Override
   public void checkMetadata(String piId) throws Exception
   {
      assertTrue(mdr.getResourceAdapter(piId) instanceof Connector15Impl);
   }

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
      return createDeployment("ra15inout.rar");
   }

   /**
    * 
    *  deployment with ironjacamar overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d2", order = 2)
   public static ResourceAdapterArchive deployment2() throws Exception
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
   @Deployment(name = "d3", order = 3)
   public static Descriptor deployment3() throws Exception
   {
      return createDescriptor("ra15inout.rar/ra-ra.xml");
   }

   /**
    * 
    * deployment with -ra.xml overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d4", order = 4)
   public static Descriptor deployment4() throws Exception
   {
      return createDescriptor("ra15inout.rar/ij-ra.xml");
   }

   /**
    * 
    * ij deployment without class-names
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d5", order = 5)
   public static ResourceAdapterArchive deployment5() throws Exception
   {
      ResourceAdapterArchive ra = createDeployment("ra15inout.rar");
      ra.addAsManifestResource("ra15inout.rar/META-INF/ironjacamar-wc.xml", "ironjacamar.xml");
      return ShrinkWrap.create(ResourceAdapterArchive.class, "ij2.rar").merge(ra);
   }
   
   /**
    * connection factories, registered after deployment
    */

   @Resource(mappedName = "java:/eis/ra15inout")
   private SimpleConnectionFactory connectionFactory1;

   @Resource(mappedName = "java:/ij-activation")
   private SimpleConnectionFactory connectionFactory2;

   @Resource(mappedName = "java:/ra-activation")
   private SimpleConnectionFactory connectionFactory3;

   @Resource(mappedName = "java:/ijra-activation")
   private SimpleConnectionFactory connectionFactory4;

   @Resource(mappedName = "java:/ijwc-activation")
   private SimpleConnectionFactory connectionFactory5;
   /**
    * administered objects
    */

   @Resource(mappedName = "java:/eis/ao/ra15inout")
   private SimpleAdminObject ao1;

   @Resource(mappedName = "java:/ao/ij-activation")
   private SimpleAdminObject ao2;

   @Resource(mappedName = "java:/ao/ra-activation")
   private SimpleAdminObject ao3;

   @Resource(mappedName = "java:/ao/ijra-activation")
   private SimpleAdminObject ao4;

   @Resource(mappedName = "java:/ao/ijwc-activation")
   private SimpleAdminObject ao5;

   /**
    * 
    * first connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void first() throws Exception
   {
      SimpleManagedConnectionFactory mcf = testSimpleCF(connectionFactory1, "aaa", 'c');
      testSimpleRA((SimpleResourceAdapter) mcf.getResourceAdapter(), "ra", true);
      testSimpleAO(ao1, "ao", 3.14);
   }

   /**
    * 
    * ij rewrite test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void ijRewriteTest() throws Exception
   {
      SimpleManagedConnectionFactory mcf = testSimpleCF(connectionFactory2, "ccc", 'a');
      testSimpleRA((SimpleResourceAdapter) mcf.getResourceAdapter(), "rara", false);
      testSimpleAO(ao2, "aoao", 0.99);
   }

   /**
    * 
    * -ra rewrite test test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void raRewriteTest() throws Exception
   {
      SimpleManagedConnectionFactory mcf = testSimpleCF(connectionFactory3, "aba", 'd');
      testSimpleRA((SimpleResourceAdapter) mcf.getResourceAdapter(), "ra-ra", false);
      testSimpleAO(ao3, "ao-ra", 2.18);
   }

   /**
    * 
    * -ra + ij rewrite test test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void raIjRewriteTest() throws Exception
   {
      SimpleManagedConnectionFactory mcf = testSimpleCF(connectionFactory4, "aba", 'd');
      testSimpleRA((SimpleResourceAdapter) mcf.getResourceAdapter(), "ra-ra", true);
      testSimpleAO(ao4, "ao-ra", 2.18);
   }

   /**
    * 
    * ij without class names rewrite test
    * 
    * @throws Exception in case of error
    */
   @Test
   @Ignore("JBJCA-915")
   public void ijWcRewriteTest() throws Exception
   {
      SimpleManagedConnectionFactory mcf = testSimpleCF(connectionFactory5, "ccc", 'a');
      testSimpleRA((SimpleResourceAdapter) mcf.getResourceAdapter(), "rara", false);
      testSimpleAO(ao5, "aoao", 0.99);
   }

   /**
    * Test metadata
    *     
    * @throws Exception in case of error
    */
   @Test
   public void testMetaDatas() throws Exception
   {
      testMetaData(5);
   }

}
