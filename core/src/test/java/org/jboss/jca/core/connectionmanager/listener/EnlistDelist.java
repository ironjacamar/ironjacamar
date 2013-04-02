/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.tx.rars.txlog.TxLogConnection;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionFactory;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionFactoryImpl;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionImpl;
import org.jboss.jca.core.tx.rars.txlog.TxLogManagedConnectionFactory;
import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdaptersDescriptor;

import java.net.URL;

import org.jboss.logging.Logger;
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

/**
 * The base for enlist / delist test cases
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class EnlistDelist
{
   private static Logger log = Logger.getLogger(EnlistDelist.class);

   /** Embedded */
   protected static Embedded embedded = null;

   /**
    * Create .rar
    * @return The resource adapter archive
    */
   public ResourceAdapterArchive createRar()
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
    * @param tx The transaction support
    * @param interleaving Use interleaving
    * @return The resource adapter descriptor
    */
   private ResourceAdaptersDescriptor createDeployment(String tx, boolean interleaving)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "txlog-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("txlog.rar");
      dashRaXmlRt.transactionSupport(tx);

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(TxLogManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/TxLogConnectionFactory").poolName("TxLog");

      if ("XATransaction".equals(tx))
      {
         org.jboss.jca.embedded.dsl.resourceadapters11.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
            .minPoolSize(1).initialPoolSize(1).maxPoolSize(1);

         if (interleaving)
            dashRaXmlPt.interleaving();
      }
      else
      {
         org.jboss.jca.embedded.dsl.resourceadapters11.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(1).initialPoolSize(1).maxPoolSize(1);
      }

      return dashRaXml;
   }

   /**
    * Create an XATransaction deployment
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createXATxDeployment()
   {
      return createDeployment("XATransaction", false);
   }

   /**
    * Create an XATransaction deployment
    * @param interleaving Use interleaving
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createXATxDeployment(boolean interleaving)
   {
      return createDeployment("XATransaction", interleaving);
   }

   /**
    * Create a LocalTransaction deployment
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createLocalTxDeployment()
   {
      return createDeployment("LocalTransaction", false);
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create(false);

      // Startup
      embedded.startup();

      // Deploy
      URL stdio = EnlistDelist.class.getClassLoader().getResource("stdio.xml");
      URL naming = EnlistDelist.class.getClassLoader().getResource("naming.xml");
      URL transaction = EnlistDelist.class.getClassLoader().getResource("validating-transaction.xml");
      URL jca = EnlistDelist.class.getClassLoader().getResource("jca.xml");

      embedded.deploy(stdio);
      embedded.deploy(naming);
      embedded.deploy(transaction);
      embedded.deploy(jca);

      // Disable RAActivator
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
      // Undeploy
      URL stdio = EnlistDelist.class.getClassLoader().getResource("stdio.xml");
      URL naming = EnlistDelist.class.getClassLoader().getResource("naming.xml");
      URL transaction = EnlistDelist.class.getClassLoader().getResource("validating-transaction.xml");
      URL jca = EnlistDelist.class.getClassLoader().getResource("jca.xml");

      embedded.undeploy(jca);
      embedded.undeploy(transaction);
      embedded.undeploy(naming);
      embedded.undeploy(stdio);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
