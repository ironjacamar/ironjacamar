/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.arquillian.unit;

import org.jboss.jca.arquillian.embedded.Configuration;
import org.jboss.jca.arquillian.rars.simple.TestConnection;
import org.jboss.jca.arquillian.rars.simple.TestConnectionFactory;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.util.UUID;

import javax.annotation.Resource;
import javax.resource.ResourceException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Unit test for Byteman integration
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
@Configuration(autoActivate = false)
@Ignore
public class BytemanBMTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(BytemanBMTestCase.class);

   /**
    * Define the deployment
    * @return The deployment archive
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createDeployment()
   {
      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "byteman.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(TestConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("simple.rar/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Define the activation
    * @return The deployment archive
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor()
   {
      ClassLoader cl = BytemanBMTestCase.class.getClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("byteman-ra.xml", 
                                                            cl.getResourceAsStream("byteman-ra.xml"));
      return isd;
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Resource(mappedName = "java:/eis/BytemanTest")
   private TestConnectionFactory connectionFactory;

   /**
    * Byteman
    * @exception Throwable Thrown if case of an error
    */
   @Test
   @BMRule(name = "Throw exception on allocateConnection",
           targetClass = "org.jboss.jca.core.connectionmanager.AbstractConnectionManager",
           targetMethod = "allocateConnection",
           action = "throw new javax.resource.ResourceException()")
   public void testByteman() throws Throwable
   {
      assertNotNull(connectionFactory);

      TestConnection c = null;
      try
      {
         c = connectionFactory.getConnection();
         fail("Got a connection");
      }
      catch (ResourceException re)
      {
         // Ok
      }
      catch (Throwable t)
      {
         fail(t.getMessage());
         throw t;
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }
}
