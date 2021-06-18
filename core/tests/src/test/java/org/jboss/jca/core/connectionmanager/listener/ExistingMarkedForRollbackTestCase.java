/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.listener;

import org.jboss.jca.arquillian.embedded.Configuration;
import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnection;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionFactory;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionFactoryImpl;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionImpl;
import org.jboss.jca.core.tx.rars.txlog.TxLogManagedConnectionFactory;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdaptersDescriptor;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

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

import static org.junit.Assert.*;

/**
 * Existing connection during MARKED_FOR_ROLLBACK
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
@Configuration(autoActivate = false)
public class ExistingMarkedForRollbackTestCase
{
   private static Logger log = Logger.getLogger(ExistingMarkedForRollbackTestCase.class);

   /**
    * Create .rar
    * @return The resource adapter archive
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createRar()
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      OutboundResourceadapterType ort = rt.getOrCreateOutboundResourceadapter()
         .transactionSupport("XATransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt =
         ort.createConnectionDefinition()
            .managedconnectionfactoryClass(TxLogManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(TxLogConnectionFactory.class.getName())
            .connectionfactoryImplClass(TxLogConnectionFactoryImpl.class.getName())
            .connectionInterface(TxLogConnection.class.getName())
            .connectionImplClass(TxLogConnectionImpl.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "txlog.rar");
      
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "txlog.jar");
      ja.addPackage(TxLogConnection.class.getPackage());
      
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create deployment
    * @return The resource adapter descriptor
    */
   @Deployment(order = 2)
   public static ResourceAdaptersDescriptor createDeployment()
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "txlog-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("txlog.rar");
      dashRaXmlRt.transactionSupport("XATransaction");

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(TxLogManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/TxLogConnectionFactory").poolName("TxLog");

      org.jboss.jca.embedded.dsl.resourceadapters12.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
         .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);

      org.jboss.jca.embedded.dsl.resourceadapters12.api.RecoverType dashRaXmlRyt = dashRaXmlCdt.getOrCreateRecovery()
         .noRecovery(Boolean.TRUE);

      return dashRaXml;
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Resource(mappedName = "java:/eis/TxLogConnectionFactory")
   private TxLogConnectionFactory cf;
  
   @Inject(name = "UserTransaction")
   private UserTransaction ut;
  
   /**
    * Existing connection within transaction
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testExistingConnection() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(ut);

      ut.begin();

      TxLogConnection c = cf.getConnection();
      assertFalse(c.isInPool());
      assertEquals("3", c.getState());
      c.close();

      assertFalse(c.isInPool());
      assertEquals("3", c.getState());

      ut.setRollbackOnly();

      c = cf.getConnection();
      assertFalse(c.isInPool());
      assertEquals("3", c.getState());
      c.close();

      ut.rollback();

      assertTrue(c.isInPool());
      assertEquals("3C9", c.getState());
   }
}
