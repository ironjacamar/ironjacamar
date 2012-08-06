/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.embedded.unit;

import org.jboss.jca.embedded.arquillian.Configuration;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdaptersDescriptor;
import org.jboss.jca.embedded.rars.simple.TestConnection;
import org.jboss.jca.embedded.rars.simple.TestConnectionFactory;
import org.jboss.jca.embedded.rars.simple.TestConnectionFactoryImpl;
import org.jboss.jca.embedded.rars.simple.TestConnectionImpl;
import org.jboss.jca.embedded.rars.simple.TestManagedConnectionFactory;
import org.jboss.jca.embedded.rars.simple.TestResourceAdapter;

import java.util.UUID;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType;
import org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Unit test for ShrinkWrap/Descriptors integration
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
@Configuration(autoActivate = false)
public class ShrinkWrapDescriptorsTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(ShrinkWrapDescriptorsTestCase.class);

   private static String deploymentName = "sd.rar";

   /**
    * Define the resource adapter archive
    * @return The archive
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createArchive()
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter()
         .resourceadapterClass(TestResourceAdapter.class.getName());
      OutboundResourceadapterType ort = rt.getOrCreateOutboundResourceadapter()
         .transactionSupport("NoTransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt =
         ort.createConnectionDefinition()
            .managedconnectionfactoryClass(TestManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(TestConnectionFactory.class.getName())
            .connectionfactoryImplClass(TestConnectionFactoryImpl.class.getName())
            .connectionInterface(TestConnection.class.getName())
            .connectionImplClass(TestConnectionImpl.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, deploymentName);

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(TestConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Define the deployment descriptor
    * @return The descriptor
    */
   @Deployment(order = 2)
   public static ResourceAdaptersDescriptor createDeployment()
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "sd-ra.xml");
      ResourceAdapterType rt = dashRaXml.createResourceAdapter().archive(deploymentName);
      ConnectionDefinitionsType cdst = rt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionType cdt =
         cdst.createConnectionDefinition()
            .className(TestManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/TestConnectionFactory").poolName("TestConnectionFactory");

      return dashRaXml;
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Resource(mappedName = "java:/eis/TestConnectionFactory")
   private TestConnectionFactory connectionFactory;
   
   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      assertNotNull(connectionFactory);

      TestConnection c = connectionFactory.getConnection();
      assertNotNull(c);

      c.callMe();
      c.close();
   }
}
