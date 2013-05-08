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
package org.jboss.jca.core.mdr;

import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactoryImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleResourceAdapter;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdaptersDescriptor;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType;
import org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Deployment test cases for MDR
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a> 
 */
public class MDRDeploymentTestCase
{
   private static Embedded embedded;

   /**
    * Create the .rar
    * @return The rar
    */
   private static ResourceAdapterArchive createRar()
   {
      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "simple.rar");
      
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "simple.jar");
      ja.addPackage(SimpleManagedConnectionFactory.class.getPackage());
      
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(createStdXml().exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the ra.xml
    * @return The XML
    */
   private static ConnectorDescriptor createStdXml()
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml").version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter().resourceadapterClass(
         SimpleResourceAdapter.class.getName());

      OutboundResourceadapterType ort = rt.getOrCreateOutboundResourceadapter().transactionSupport("NoTransaction")
         .reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
         .managedconnectionfactoryClass(SimpleManagedConnectionFactory.class.getName())
         .connectionfactoryInterface(SimpleConnectionFactory.class.getName())
         .connectionfactoryImplClass(SimpleConnectionFactoryImpl.class.getName())
         .connectionInterface(SimpleConnection.class.getName())
         .connectionImplClass(SimpleConnectionImpl.class.getName());

      return raXml;
   }

   /**
    * Create the -ra.xml
    * @return The XML
    */
   private static ResourceAdaptersDescriptor createDeployment()
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "simple-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("simple.rar");

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(SimpleManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/SimpleConnectionFactory").poolName("SimpleConnectionFactory");

      return dashRaXml;
   }

   /**
    * Test: Constructor
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testDeployment() throws Throwable
   {
      MetadataRepository mdr = embedded.lookup("MDR", MetadataRepository.class);
      assertNotNull(mdr);

      assertNotNull(mdr.getResourceAdapters());
      assertEquals(0, mdr.getResourceAdapters().size());

      ResourceAdapterArchive raa = createRar();
      embedded.deploy(raa);
      assertEquals(1, mdr.getResourceAdapters().size());

      ResourceAdaptersDescriptor rad = createDeployment();
      embedded.deploy(rad);
      assertEquals(2, mdr.getResourceAdapters().size());

      embedded.undeploy(rad);
      assertEquals(1, mdr.getResourceAdapters().size());

      embedded.undeploy(raa);
      assertEquals(0, mdr.getResourceAdapters().size());
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create an embedded JCA instance
      embedded = EmbeddedFactory.create();

      // Startup
      embedded.startup();

      RAActivator raa = embedded.lookup("RAActivator", RAActivator.class);

      if (raa == null)
         throw new IllegalStateException("RAActivator not defined");
      
      raa.setEnabled(false);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
