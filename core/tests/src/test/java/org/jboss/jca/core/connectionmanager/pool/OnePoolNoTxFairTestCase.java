/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.connectionmanager.ConnectionManagerUtil;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactoryImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactoryImpl1;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionImpl1;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory1;
import org.jboss.jca.core.connectionmanager.rar.SimpleResourceAdapter;
import org.jboss.jca.embedded.dsl.ironjacamar13.api.ConnectionDefinitionType;
import org.jboss.jca.embedded.dsl.ironjacamar13.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.ironjacamar13.api.IronjacamarDescriptor;

import jakarta.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
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

import static org.junit.Assert.assertFalse;

/**
 * Test of fair setting
 * @author <a href="mailto:jesper.pedersen@redhat.com">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
public class OnePoolNoTxFairTestCase
{
   /**
    * The connection factory
    */
   @Resource(mappedName = "java:/eis/Pool")
   private SimpleConnectionFactory cf;

   /**
    * The archive
    * @return The archive
    */
   @Deployment
   public static ResourceAdapterArchive deployment()
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "pool.rar");
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class);
      ja.addPackage(SimpleConnectionFactory.class.getPackage());
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(getRaXml().exportAsString()), "ra.xml");
      raa.addAsManifestResource(new StringAsset(getIJXml().exportAsString()), "ironjacamar.xml");
      return raa;
   }

   /**
    * The IronJacamar descriptor
    * @return The descriptor
    */
   private static IronjacamarDescriptor getIJXml()
   {
      IronjacamarDescriptor ijXml = Descriptors.create(IronjacamarDescriptor.class);
      ConnectionDefinitionsType ijCdst = ijXml.getOrCreateConnectionDefinitions();
      ConnectionDefinitionType ijCdt = ijCdst.createConnectionDefinition()
         .className(SimpleManagedConnectionFactory.class.getName()).jndiName("java:/eis/Pool");
      ijCdt.getOrCreatePool().fair(false);

      return ijXml;
   }

   /**
    * 
    * create ConnectorDescriptor
    * 
    * @param tx Transaction support level
    * @return ConnectorDescriptor
    */
   private static ConnectorDescriptor getRaXml()
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

      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt1 = ort.createConnectionDefinition()
         .managedconnectionfactoryClass(SimpleManagedConnectionFactory1.class.getName())
         .connectionfactoryInterface(SimpleConnectionFactory.class.getName())
         .connectionfactoryImplClass(SimpleConnectionFactoryImpl1.class.getName())
         .connectionInterface(SimpleConnection.class.getName())
         .connectionImplClass(SimpleConnectionImpl1.class.getName());

      return raXml;
   }
   
   /**
    * Test fair
    * @exception Exception Thrown in case of an error
    */
   @Test
   public void testFair() throws Exception
   {
      AbstractPool pool = (AbstractPool)ConnectionManagerUtil.extract(cf).getPool();
      assertFalse(pool.getPoolConfiguration().isFair());
   }
}
