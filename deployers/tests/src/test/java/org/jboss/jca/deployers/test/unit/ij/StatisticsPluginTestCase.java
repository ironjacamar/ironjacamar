/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package org.jboss.jca.deployers.test.unit.ij;

import org.jboss.jca.deployers.test.rars.stat.StatisticsAdminObject;
import org.jboss.jca.deployers.test.rars.stat.StatisticsConnectionFactory;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import jakarta.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Test case for the IronJacamar statistics plugins
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
public class StatisticsPluginTestCase
{
   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "stat.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "stat.jar");
      ja.addPackage(StatisticsConnectionFactory.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("stat.rar/META-INF/ra.xml", "ra.xml");
      raa.addAsManifestResource("stat.rar/META-INF/MANIFEST.MF", "MANIFEST.MF");

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
      ClassLoader cl = StatisticsPluginTestCase.class.getClassLoader();

      InputStreamDescriptor isd =
         new InputStreamDescriptor("stat-ra.xml", cl.getResourceAsStream("stat.rar/stat-ra.xml"));

      return isd;
   }

   @Resource(mappedName = "java:/eis/StatisticsConnectionFactory")
   private StatisticsConnectionFactory connectionFactory;

   @Resource(mappedName = "java:/eis/ao/StatisticsAdminObject")
   private StatisticsAdminObject adminObject;

   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      assertNotNull(connectionFactory);
      assertNotNull(adminObject);
   }
}
