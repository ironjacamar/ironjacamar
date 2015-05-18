/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.rars;

import org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionsType;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdapterType;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.rars.perf.PerfConnection;
import org.ironjacamar.rars.perf.PerfConnectionFactory;
import org.ironjacamar.rars.perf.PerfConnectionFactoryImpl;
import org.ironjacamar.rars.perf.PerfConnectionImpl;
import org.ironjacamar.rars.perf.PerfManagedConnectionFactory;
import org.ironjacamar.rars.txlog.TxLogConnection;
import org.ironjacamar.rars.txlog.TxLogConnectionFactory;
import org.ironjacamar.rars.txlog.TxLogConnectionFactoryImpl;
import org.ironjacamar.rars.txlog.TxLogConnectionImpl;
import org.ironjacamar.rars.txlog.TxLogManagedConnectionFactory;
import org.ironjacamar.rars.wm.WorkConnection;
import org.ironjacamar.rars.wm.WorkConnectionFactory;
import org.ironjacamar.rars.wm.WorkConnectionFactoryImpl;
import org.ironjacamar.rars.wm.WorkConnectionImpl;
import org.ironjacamar.rars.wm.WorkManagedConnectionFactory;
import org.ironjacamar.rars.wm.WorkResourceAdapter;

import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * A factory for resource adapter used in testing
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterFactory
{
   /**
    * Constructor
    */
   private ResourceAdapterFactory()
   {
   }

   /**
    * Create the perf.rar
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createPerfRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor raXml =
         Descriptors.create(org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      
      org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType ort =
         rt.getOrCreateOutboundResourceadapter()
         .transactionSupport("XATransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt =
         ort.createConnectionDefinition()
            .managedconnectionfactoryClass(PerfManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(PerfConnectionFactory.class.getName())
            .connectionfactoryImplClass(PerfConnectionFactoryImpl.class.getName())
            .connectionInterface(PerfConnection.class.getName())
            .connectionImplClass(PerfConnectionImpl.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "perf.rar");
      
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "perf.jar");
      ja.addPackages(true, PerfConnection.class.getPackage());
      
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the perf.rar deployment
    * @param tsl The transaction support level
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createPerfDeployment(TransactionSupportLevel tsl)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "perf-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("perf.rar");
      if (tsl == null || tsl == TransactionSupportLevel.NoTransaction)
      {
         dashRaXmlRt.transactionSupport("NoTransaction");
      }
      else if (tsl == TransactionSupportLevel.LocalTransaction)
      {
         dashRaXmlRt.transactionSupport("LocalTransaction");
      }
      else
      {
         dashRaXmlRt.transactionSupport("XATransaction");
      }

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(PerfManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/PerfConnectionFactory").poolName("PerfConnectionFactory");

      org.ironjacamar.embedded.dsl.resourceadapters20.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
         .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);

      if (tsl == TransactionSupportLevel.XATransaction)
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.RecoverType dashRaXmlRyt =
            dashRaXmlCdt.getOrCreateRecovery().noRecovery(Boolean.TRUE);
      }

      return dashRaXml;
   }

   /**
    * Create the txlog.rar
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createTxLogRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor raXml =
         Descriptors.create(org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      
      org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType ort =
         rt.getOrCreateOutboundResourceadapter()
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
      ja.addPackages(true, TxLogConnection.class.getPackage());
      
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the txlog.rar deployment
    * @param tsl The transaction support level
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createTxLogDeployment(TransactionSupportLevel tsl)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "txlog-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("txlog.rar");
      if (tsl == null || tsl == TransactionSupportLevel.NoTransaction)
      {
         dashRaXmlRt.transactionSupport("NoTransaction");
      }
      else if (tsl == TransactionSupportLevel.LocalTransaction)
      {
         dashRaXmlRt.transactionSupport("LocalTransaction");
      }
      else
      {
         dashRaXmlRt.transactionSupport("XATransaction");
      }

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(TxLogManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/TxLogConnectionFactory").poolName("TxLogConnectionFactory");

      org.ironjacamar.embedded.dsl.resourceadapters20.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
         .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);

      if (tsl == TransactionSupportLevel.XATransaction)
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.RecoverType dashRaXmlRyt =
            dashRaXmlCdt.getOrCreateRecovery().noRecovery(Boolean.TRUE);
      }

      return dashRaXml;
   }

   /**
    * Create the work.rar
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createWorkRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor raXml =
         Descriptors.create(org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor.class, "ra.xml")
         .version("1.6");
      
      org.jboss.shrinkwrap.descriptor.api.connector16.ResourceadapterType rt = raXml.getOrCreateResourceadapter()
         .resourceadapterClass(WorkResourceAdapter.class.getName());
      org.jboss.shrinkwrap.descriptor.api.connector16.OutboundResourceadapterType ort =
         rt.getOrCreateOutboundResourceadapter()
         .transactionSupport("NoTransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectionDefinitionType cdt =
         ort.createConnectionDefinition()
            .managedconnectionfactoryClass(WorkManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(WorkConnectionFactory.class.getName())
            .connectionfactoryImplClass(WorkConnectionFactoryImpl.class.getName())
            .connectionInterface(WorkConnection.class.getName())
            .connectionImplClass(WorkConnectionImpl.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "work.rar");
      
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "work.jar");
      ja.addPackages(true, WorkConnection.class.getPackage());
      
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the work.rar deployment
    * @param bc The BootstrapContext name; <code>null</code> if default
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createWorkDeployment(String bc)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "work-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("work.rar");
      if (bc != null)
         dashRaXmlRt.bootstrapContext(bc);

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(WorkManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/WorkConnectionFactory").poolName("WorkConnectionFactory");

      org.ironjacamar.embedded.dsl.resourceadapters20.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
         .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);

      return dashRaXml;
   }
}
