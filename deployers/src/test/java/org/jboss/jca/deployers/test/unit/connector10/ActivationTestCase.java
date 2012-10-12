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

package org.jboss.jca.deployers.test.unit.connector10;

import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory1;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for deploying resource adapter archives (.RAR) using -ra.xml files
 * for activation
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class ActivationTestCase extends Ra10TestBase
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
      return createDeployment("ra10dtdout.rar");
   }

   /**
    * 
    * second deployment
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d2", order = 2)
   public static ResourceAdapterArchive deployment2() throws Exception
   {
      return createDeployment("ra10dtdoutoverwrite.rar");
   }

   /**
    * 
    * third deployment with ironjacamar overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d3", order = 3)
   public static ResourceAdapterArchive deployment3() throws Exception
   {
      return createIJDeployment("ra10dtdout.rar", "ij1.rar");
   }

   /**
    * 
    * forth deployment with ironjacamar overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d4", order = 4)
   public static ResourceAdapterArchive deployment4() throws Exception
   {
      return createIJDeployment("ra10dtdoutoverwrite.rar", "ij2.rar");
   }

   /**
    * 
    * deployment with -ra.xml overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d5", order = 5)
   public static Descriptor deployment5() throws Exception
   {
      return createDescriptor("ra10dtdout.rar/rar-ra.xml");
   }

   /**
    * 
    * deployment with -ra.xml overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d6", order = 6)
   public static Descriptor deployment6() throws Exception
   {
      return createDescriptor("ra10dtdoutoverwrite.rar/rar-ra.xml");
   }

   /**
    * 
    * deployment with -ra.xml overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d7", order = 7)
   public static Descriptor deployment7() throws Exception
   {
      return createDescriptor("ra10dtdout.rar/ij-ra.xml");
   }

   /**
    * 
    * deployment with -ra.xml overwrite
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d8", order = 8)
   public static Descriptor deployment8() throws Exception
   {
      return createDescriptor("ra10dtdoutoverwrite.rar/ij-ra.xml");
   }

   /**
    * connection factories, registered after deployment
    */
   @Resource(mappedName = "java:/eis/ra10dtdout")
   private SimpleConnectionFactory connectionFactory1;

   @Resource(mappedName = "java:/eis/ra10dtdoutoverwrite")
   private SimpleConnectionFactory1 connectionFactory2;

   @Resource(mappedName = "java:/ij-activation")
   private SimpleConnectionFactory connectionFactory3;

   @Resource(mappedName = "java:/ij-activation2")
   private SimpleConnectionFactory1 connectionFactory4;

   @Resource(mappedName = "java:/ra-activation")
   private SimpleConnectionFactory connectionFactory5;

   @Resource(mappedName = "java:/ra-activation2")
   private SimpleConnectionFactory1 connectionFactory6;

   @Resource(mappedName = "java:/ra-activation3")
   private SimpleConnectionFactory connectionFactory7;

   @Resource(mappedName = "java:/ra-activation4")
   private SimpleConnectionFactory1 connectionFactory8;

   /**
    * 
    * first connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void first() throws Exception
   {
      testBasic(connectionFactory1, "aaa", 'c');
   }

   /**
    * 
    * second connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void second() throws Exception
   {
      testBasic1(connectionFactory2, "bbb", (byte) 5);
   }

   /**
    * 
    * third connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void third() throws Exception
   {
      testBasic(connectionFactory3, "ccc", 'a');
   }

   /**
    * 
    * forth connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void forth() throws Exception
   {
      testBasic1(connectionFactory4, "ddd", (byte) 4);
   }

   /**
    * 
    * fifth connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void fifth() throws Exception
   {
      testBasic(connectionFactory5, "eee", 'e');
   }

   /**
    * 
    * sixth connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void sixth() throws Exception
   {
      testBasic1(connectionFactory6, "fff", (byte) 3);
   }

   /**
    * 
    * seventh connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void seventh() throws Exception
   {
      testBasic(connectionFactory7, "eee", 'e');
   }

   /**
    * 
    * eighth connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void eighth() throws Exception
   {
      testBasic1(connectionFactory8, "fff", (byte) 3);
   }

   /**
    * Test metadata
    *     
    * @throws Exception in case of error
    */
   @Test
   public void testMetaDatas() throws Exception
   {
      testMetaData(8);
   }
}
