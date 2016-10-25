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

import org.ironjacamar.embedded.dsl.resourceadapters20.api.CapacityType;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionsType;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdapterType;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.rars.lazy.LazyConnection;
import org.ironjacamar.rars.lazy.LazyConnectionFactory;
import org.ironjacamar.rars.lazy.LazyConnectionFactoryImpl;
import org.ironjacamar.rars.lazy.LazyConnectionImpl;
import org.ironjacamar.rars.lazy.LazyManagedConnectionFactory;
import org.ironjacamar.rars.lazy.LazyResourceAdapter;
import org.ironjacamar.rars.perf.PerfConnection;
import org.ironjacamar.rars.perf.PerfConnectionFactory;
import org.ironjacamar.rars.perf.PerfConnectionFactoryImpl;
import org.ironjacamar.rars.perf.PerfConnectionImpl;
import org.ironjacamar.rars.perf.PerfManagedConnectionFactory;
import org.ironjacamar.rars.security.UnifiedSecurityConnection;
import org.ironjacamar.rars.security.UnifiedSecurityConnectionFactory;
import org.ironjacamar.rars.security.UnifiedSecurityConnectionFactoryImpl;
import org.ironjacamar.rars.security.UnifiedSecurityConnectionImpl;
import org.ironjacamar.rars.security.UnifiedSecurityManagedConnectionFactory;
import org.ironjacamar.rars.security.UnifiedSecurityResourceAdapter;
import org.ironjacamar.rars.test.TestAdminObject;
import org.ironjacamar.rars.test.TestAdminObjectImpl;
import org.ironjacamar.rars.test.TestConnection;
import org.ironjacamar.rars.test.TestConnectionFactory;
import org.ironjacamar.rars.test.TestConnectionFactoryImpl;
import org.ironjacamar.rars.test.TestConnectionImpl;
import org.ironjacamar.rars.test.TestManagedConnectionFactory;
import org.ironjacamar.rars.test.TestResourceAdapter;
import org.ironjacamar.rars.test.inflow.TestActivationSpec;
import org.ironjacamar.rars.test.inflow.TestMessageListener;
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
import org.ironjacamar.rars.wm.WorkManagedConnectionFactoryNoHashCode;
import org.ironjacamar.rars.wm.WorkResourceAdapter;

import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * A factory for resource adapter used in testing
 *
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
    * Create the lazy.rar
    *
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createLazyRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor raXml = Descriptors
            .create(org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor.class, "ra.xml").version("1.5");

      org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType rt = raXml.getOrCreateResourceadapter()
            .resourceadapterClass(LazyResourceAdapter.class.getName());

      rt.createConfigProperty().configPropertyName("Enable")
         .configPropertyType(Boolean.class.getName()).configPropertyValue(Boolean.TRUE.toString());

      rt.createConfigProperty().configPropertyName("LocalTransaction")
         .configPropertyType(Boolean.class.getName()).configPropertyValue(Boolean.FALSE.toString());

      rt.createConfigProperty().configPropertyName("XATransaction")
         .configPropertyType(Boolean.class.getName()).configPropertyValue(Boolean.FALSE.toString());

      org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType ort = rt
            .getOrCreateOutboundResourceadapter().transactionSupport("XATransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
            .managedconnectionfactoryClass(LazyManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(LazyConnectionFactory.class.getName())
            .connectionfactoryImplClass(LazyConnectionFactoryImpl.class.getName())
            .connectionInterface(LazyConnection.class.getName())
            .connectionImplClass(LazyConnectionImpl.class.getName());

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "lazy.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "lazy.jar");
      ja.addPackages(true, LazyConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the lazy.rar deployment
    *
    * @param tsl              The transaction support level
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createLazyDeployment(TransactionSupportLevel tsl)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "lazy-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("lazy.rar");
      if (tsl == null || tsl == TransactionSupportLevel.NoTransaction)
      {
         dashRaXmlRt.transactionSupport("NoTransaction");
      }
      else if (tsl == TransactionSupportLevel.LocalTransaction)
      {
         dashRaXmlRt.transactionSupport("LocalTransaction");
         dashRaXmlRt.createConfigProperty().name("LocalTransaction").text(Boolean.TRUE.toString());
      }
      else
      {
         dashRaXmlRt.transactionSupport("XATransaction");
         dashRaXmlRt.createConfigProperty().name("XATransaction").text(Boolean.TRUE.toString());
      }

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt = dashRaXmlCdst
         .createConnectionDefinition().className(LazyManagedConnectionFactory.class.getName())
         .jndiName("java:/eis/LazyConnectionFactory").id("LazyConnectionFactory").sharable(true).enlistment(true);

      org.ironjacamar.embedded.dsl.resourceadapters20.api.TimeoutType dashRaXmlTt = dashRaXmlCdt.getOrCreateTimeout()
         .blockingTimeoutMillis(100).idleTimeoutMinutes(Integer.valueOf(0));

      if (tsl == TransactionSupportLevel.XATransaction)
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(1);

         org.ironjacamar.embedded.dsl.resourceadapters20.api.RecoverType dashRaXmlRyt = dashRaXmlCdt
            .getOrCreateRecovery().noRecovery(Boolean.TRUE);
      }
      else
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(1);
      }

      return dashRaXml;
   }

   /**
    * Create the perf.rar
    *
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createPerfRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor raXml = Descriptors
            .create(org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor.class, "ra.xml").version("1.5");

      org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType ort = rt
            .getOrCreateOutboundResourceadapter().transactionSupport("XATransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
            .managedconnectionfactoryClass(PerfManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(PerfConnectionFactory.class.getName())
            .connectionfactoryImplClass(PerfConnectionFactoryImpl.class.getName())
            .connectionInterface(PerfConnection.class.getName())
            .connectionImplClass(PerfConnectionImpl.class.getName());

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "perf.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "perf.jar");
      ja.addPackages(true, PerfConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the perf.rar deployment
    *
    * @param tsl              The transaction support level
    * @param ccm              Use CCM
    * @param txBeginDuration  The begin duration for the transaction
    * @param txCommitDuration The commit duration for the transaction
    * @param poolSize         The pool size
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createPerfDeployment(TransactionSupportLevel tsl, boolean ccm,
         long txBeginDuration, long txCommitDuration, int poolSize)
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
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt = dashRaXmlCdst
            .createConnectionDefinition().className(PerfManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/PerfConnectionFactory").id("PerfConnectionFactory").useCcm(ccm);

      dashRaXmlCdt.createConfigProperty().name("TxBeginDuration").text(Long.toString(txBeginDuration));
      dashRaXmlCdt.createConfigProperty().name("TxCommitDuration").text(Long.toString(txCommitDuration));

      org.ironjacamar.embedded.dsl.resourceadapters20.api.TimeoutType dashRaXmlTt = dashRaXmlCdt.getOrCreateTimeout()
            .idleTimeoutMinutes(Integer.valueOf(0));

      if (tsl == TransactionSupportLevel.XATransaction)
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
               .minPoolSize(poolSize).initialPoolSize(poolSize).maxPoolSize(poolSize).prefill(Boolean.TRUE)
               .wrapXaResource(Boolean.FALSE);

         org.ironjacamar.embedded.dsl.resourceadapters20.api.RecoverType dashRaXmlRyt = dashRaXmlCdt
               .getOrCreateRecovery().noRecovery(Boolean.TRUE);
      }
      else
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
               .minPoolSize(poolSize).initialPoolSize(poolSize).maxPoolSize(poolSize).prefill(Boolean.TRUE);
      }

      return dashRaXml;
   }

   /**
    * Create the txlog.rar
    *
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createTxLogRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor raXml = Descriptors
            .create(org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor.class, "ra.xml").version("1.5");

      org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType ort = rt
            .getOrCreateOutboundResourceadapter().transactionSupport("XATransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
            .managedconnectionfactoryClass(TxLogManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(TxLogConnectionFactory.class.getName())
            .connectionfactoryImplClass(TxLogConnectionFactoryImpl.class.getName())
            .connectionInterface(TxLogConnection.class.getName())
            .connectionImplClass(TxLogConnectionImpl.class.getName());

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "txlog.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "txlog.jar");
      ja.addPackages(true, TxLogConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the txlog.rar deployment
    *
    * @param tsl The transaction support level
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createTxLogDeployment(TransactionSupportLevel tsl)
   {
      return createTxLogDeployment(tsl, "");
   }

   /**
    * Create the txlog.rar deployment
    *
    * @param tsl     The transaction support level
    * @param postfix The JNDI postfix
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createTxLogDeployment(TransactionSupportLevel tsl, String postfix)
   {
      return createTxLogDeployment(tsl, postfix, null);
   }

   /**
    * Create the txlog.rar deployment
    *
    * @param tsl     The transaction support level
    * @param postfix The JNDI postfix
    * @param tracking Do tracking
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createTxLogDeployment(TransactionSupportLevel tsl,
                                                                  String postfix,
                                                                  Boolean tracking)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors
            .create(ResourceAdaptersDescriptor.class, "txlog" + postfix + "-ra.xml");

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
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt = dashRaXmlCdst
         .createConnectionDefinition().className(TxLogManagedConnectionFactory.class.getName())
         .jndiName("java:/eis/TxLogConnectionFactory" + postfix).id("TxLogConnectionFactory" + postfix);

      if (tracking != null)
         dashRaXmlCdt.tracking(tracking);

      if (tsl == null ||
          tsl == TransactionSupportLevel.NoTransaction ||
          tsl == TransactionSupportLevel.LocalTransaction)
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);
      }
      else
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.TimeoutType dashRaXmlTt = dashRaXmlCdt.getOrCreateTimeout()
            .xaResourceTimeout(200);

         org.ironjacamar.embedded.dsl.resourceadapters20.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);

         dashRaXmlCdt.getOrCreateRecovery().getOrCreateRecoveryCredential().securityDomain("DefaultSecurityDomain");
      }

      return dashRaXml;
   }

   /**
    * Create the work.rar
    *
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createWorkRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor raXml = Descriptors
            .create(org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor.class, "ra.xml").version("1.6");

      org.jboss.shrinkwrap.descriptor.api.connector16.ResourceadapterType rt = raXml.getOrCreateResourceadapter()
            .resourceadapterClass(WorkResourceAdapter.class.getName());
      org.jboss.shrinkwrap.descriptor.api.connector16.OutboundResourceadapterType ort = rt
            .getOrCreateOutboundResourceadapter().transactionSupport("NoTransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
            .managedconnectionfactoryClass(WorkManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(WorkConnectionFactory.class.getName())
            .connectionfactoryImplClass(WorkConnectionFactoryImpl.class.getName())
            .connectionInterface(WorkConnection.class.getName())
            .connectionImplClass(WorkConnectionImpl.class.getName());

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "work.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "work.jar");
      ja.addPackages(true, WorkConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the work.rar
    *
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createWorkRarMCFNoHashCode()
   {
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor raXml = Descriptors
            .create(org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor.class, "ra.xml").version("1.6");

      org.jboss.shrinkwrap.descriptor.api.connector16.ResourceadapterType rt = raXml.getOrCreateResourceadapter()
            .resourceadapterClass(WorkResourceAdapter.class.getName());
      org.jboss.shrinkwrap.descriptor.api.connector16.OutboundResourceadapterType ort = rt
            .getOrCreateOutboundResourceadapter().transactionSupport("NoTransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
            .managedconnectionfactoryClass(WorkManagedConnectionFactoryNoHashCode.class.getName())
            .connectionfactoryInterface(WorkConnectionFactory.class.getName())
            .connectionfactoryImplClass(WorkConnectionFactoryImpl.class.getName())
            .connectionInterface(WorkConnection.class.getName())
            .connectionImplClass(WorkConnectionImpl.class.getName());

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "work.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "work.jar");
      ja.addPackages(true, WorkConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }


   /**
    * Create the work.rar deployment
    *
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
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt = dashRaXmlCdst
            .createConnectionDefinition().className(WorkManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/WorkConnectionFactory").id("WorkConnectionFactory");

      org.ironjacamar.embedded.dsl.resourceadapters20.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);

      return dashRaXml;
   }

   /**
    * Create the work.rar deployment
    *
    * @param bc The BootstrapContext name; <code>null</code> if default
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createWorkDeploymentMCFNoHashCode(String bc)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "work-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("work.rar");
      if (bc != null)
         dashRaXmlRt.bootstrapContext(bc);
      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt = dashRaXmlCdst
            .createConnectionDefinition().className(WorkManagedConnectionFactoryNoHashCode.class.getName())
            .jndiName("java:/eis/WorkConnectionFactory").id("WorkConnectionFactory");

      org.ironjacamar.embedded.dsl.resourceadapters20.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(10);

      return dashRaXml;
   }


   /**
    * Create the unified-security.rar
    *
    *
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createUnifiedSecurityRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor raXml = Descriptors
            .create(org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor.class, "ra.xml").version("1.6");

      org.jboss.shrinkwrap.descriptor.api.connector16.ResourceadapterType rt = raXml.getOrCreateResourceadapter()
            .resourceadapterClass(UnifiedSecurityResourceAdapter.class.getName());
      org.jboss.shrinkwrap.descriptor.api.connector16.OutboundResourceadapterType ort = rt
            .getOrCreateOutboundResourceadapter().transactionSupport("XATransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
            .managedconnectionfactoryClass(UnifiedSecurityManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(UnifiedSecurityConnectionFactory.class.getName())
            .connectionfactoryImplClass(UnifiedSecurityConnectionFactoryImpl.class.getName())
            .connectionInterface(UnifiedSecurityConnection.class.getName())
            .connectionImplClass(UnifiedSecurityConnectionImpl.class.getName());

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "unified-security.rar");


      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "unified-security.jar");
      ja.addPackages(true, UnifiedSecurityConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create the work.rar deployment
    *
    * @param bc The BootstrapContext name; <code>null</code> if default
    * @param securityDomain The SecurityDomain name; <code>null</code> if default
    * @param tsl     The transaction support level
    * @param id The JNDI postfix and id
    * @param prefill boolean to prefill or not
    * @param initialPoolSize The initial pool size value
    *
    *
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createUnifiedSecurityDeployment(String bc, String securityDomain,
         TransactionSupportLevel tsl, String id, boolean prefill, int initialPoolSize)
   {
      return createUnifiedSecurityDeployment(bc, securityDomain, tsl, id, prefill, initialPoolSize,
            "FailingConnectionOnly");
   }


   /**
    * Create the work.rar deployment
    *
    * @param bc The BootstrapContext name; <code>null</code> if default
    * @param securityDomain The SecurityDomain name; <code>null</code> if default
    * @param tsl     The transaction support level
    * @param id The JNDI postfix and id
    * @param prefill boolean to prefill or not
    * @param initialPoolSize The initial pool size value
    * @param flushStrategy  the flush strategy
    *
    *
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createUnifiedSecurityDeployment(String bc, String securityDomain,
         TransactionSupportLevel tsl, String id, boolean prefill, int initialPoolSize, String flushStrategy)
   {
      return createUnifiedSecurityDeployment(bc, securityDomain, tsl, id, prefill, initialPoolSize,
            flushStrategy, false, null, null);
   }
   /**
    * Create the work.rar deployment
    *
    * @param bc The BootstrapContext name; <code>null</code> if default
    * @param securityDomain The SecurityDomain name; <code>null</code> if default
    * @param tsl     The transaction support level
    * @param id The JNDI postfix and id
    * @param prefill boolean to prefill or not
    * @param initialPoolSize The initial pool size value
    * @param flushStrategy  the flush strategy
    * @param validateOnMatch if true validate On match is enabled
    * @param incrementerClass the incrementer class name for capacity settings
    * @param decrementerClass the decrementer class name for capacity settings
    * @return The resource adapter descriptor
    *
    *
    */
   public static ResourceAdaptersDescriptor createUnifiedSecurityDeployment(String bc, String securityDomain,
         TransactionSupportLevel tsl, String id, boolean prefill, int initialPoolSize, String flushStrategy,
         boolean validateOnMatch, String incrementerClass, String decrementerClass)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors
            .create(ResourceAdaptersDescriptor.class, "unified-security-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("unified-security.rar");
      if (bc != null)
         dashRaXmlRt.bootstrapContext(bc);


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
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt = dashRaXmlCdst
            .createConnectionDefinition().className(UnifiedSecurityManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/" + id).id(id);

      if (validateOnMatch)
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.ValidationType dashRaXmlVt = dashRaXmlCdt
               .getOrCreateValidation();

         dashRaXmlVt.validateOnMatch(Boolean.TRUE);
      }


      if (securityDomain != null)
      {
         dashRaXmlCdt.getOrCreateSecurity().securityDomain(securityDomain);
      }

      if (tsl != TransactionSupportLevel.XATransaction)
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
               .minPoolSize(initialPoolSize).initialPoolSize(initialPoolSize).maxPoolSize(20).prefill(prefill)
               .flushStrategy(flushStrategy);
         if (incrementerClass != null || decrementerClass != null)
         {
            CapacityType capacityType = dashRaXmlPt.getOrCreateCapacity();
            capacityType.getOrCreateDecrementer().className(decrementerClass);
            capacityType.getOrCreateIncrementer().className(incrementerClass);
         }

      }
      else
      {
         org.ironjacamar.embedded.dsl.resourceadapters20.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
               .minPoolSize(initialPoolSize).initialPoolSize(initialPoolSize).maxPoolSize(20).prefill(prefill)
               .flushStrategy(flushStrategy);
         if (incrementerClass != null || decrementerClass != null)
         {
            CapacityType capacityType = dashRaXmlPt.getOrCreateCapacity();
            capacityType.getOrCreateDecrementer().className(decrementerClass);
            capacityType.getOrCreateIncrementer().className(incrementerClass);
         }
      }


      return dashRaXml;
   }



   /**
    * Create the work.rar deployment
    * @param bc The BootstrapContext name; <code>null</code> if default
    * @param securityDomain The SecurityDomain name; <code>null</code> if default
    * @param tsl     The transaction support level
    * @param id               The JNDI postfix and id
    * @param decrementerClass the decrementer calss name
    * @param incrementerClass the incrementer className
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createCapacityActivation(String bc, String securityDomain,
         TransactionSupportLevel tsl, String id, String incrementerClass, String decrementerClass)
   {
      return createUnifiedSecurityDeployment(bc, securityDomain, tsl, id, true, 1, "FailingConnectionOnly", false,
            incrementerClass, decrementerClass);
   }


   /**
    * Create the test.rar
    *
    * @return The resource adapter archive
    */
   public static ResourceAdapterArchive createTestRar()
   {
      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor raXml = Descriptors
            .create(org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor.class, "ra.xml").version("1.6");

      org.jboss.shrinkwrap.descriptor.api.connector16.ResourceadapterType rt = raXml.getOrCreateResourceadapter()
         .resourceadapterClass(TestResourceAdapter.class.getName());

      org.jboss.shrinkwrap.descriptor.api.connector16.OutboundResourceadapterType ort = rt
         .getOrCreateOutboundResourceadapter().transactionSupport("NoTransaction").reauthenticationSupport(false);

      org.jboss.shrinkwrap.descriptor.api.connector16.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
         .managedconnectionfactoryClass(TestManagedConnectionFactory.class.getName())
         .connectionfactoryInterface(TestConnectionFactory.class.getName())
         .connectionfactoryImplClass(TestConnectionFactoryImpl.class.getName())
         .connectionInterface(TestConnection.class.getName())
         .connectionImplClass(TestConnectionImpl.class.getName());

      cdt.createConfigProperty().configPropertyName("CreateFailureCount")
         .configPropertyType(Integer.class.getName()).configPropertyValue("0");

      cdt.createConfigProperty().configPropertyName("InvalidConnectionFailureCount")
         .configPropertyType(Integer.class.getName()).configPropertyValue("0");

      rt.createAdminobject().adminobjectInterface(TestAdminObject.class.getName())
        .adminobjectClass(TestAdminObjectImpl.class.getName());

      rt.getOrCreateInboundResourceadapter().getOrCreateMessageadapter().getOrCreateMessagelistener()
         .messagelistenerType(TestMessageListener.class.getName())
         .getOrCreateActivationspec().activationspecClass(TestActivationSpec.class.getName())
         .getOrCreateRequiredConfigProperty().configPropertyName("Name");
      
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "test.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "test.jar");
      ja.addPackages(true, PerfConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }


   /**
    * Create the test.rar deployment
    *
    * @param allocationRetry The number of allocation retries
    * @param validation True is foreground, false is background, null is none
    * @param invalidConnectionFailureCount The invalid connection failure count
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createTestDeployment(int allocationRetry,
         Boolean validation, int invalidConnectionFailureCount)
   {
      return createTestDeployment(allocationRetry, validation, invalidConnectionFailureCount,
            "TestConnectionFactory", "TestAdminObject", null);
   }
   /**
    * Create the test.rar deployment
    *
    * @param allocationRetry The number of allocation retries
    * @param validation True is foreground, false is background, null is none
    * @param invalidConnectionFailureCount The invalid connection failure count
    * @param connectionDefID the id of ConnectionDefinition
    * @param adminObjectID the id of AdminObject
    * @param bc the BootstrapContext
    * @return The resource adapter descriptor
    */
   public static ResourceAdaptersDescriptor createTestDeployment(int allocationRetry, Boolean validation,
         int invalidConnectionFailureCount, String connectionDefID, String adminObjectID, String bc)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "test-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("test.rar");
      if (bc != null)
         dashRaXmlRt.bootstrapContext(bc);

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt = dashRaXmlCdst
         .createConnectionDefinition().className(TestManagedConnectionFactory.class.getName())
         .jndiName("java:/eis/" + connectionDefID).id(connectionDefID);

      if (allocationRetry > 0)
      {
         dashRaXmlCdt.createConfigProperty().name("CreateFailureCount")
            .text(Integer.toString(allocationRetry));

         dashRaXmlCdt.getOrCreateTimeout().allocationRetry(Integer.valueOf(allocationRetry))
            .allocationRetryWaitMillis(100);
      }
      
      if (validation != null)
      {
         dashRaXmlCdt.createConfigProperty().name("InvalidConnectionFailureCount")
            .text(Integer.toString(invalidConnectionFailureCount));

         org.ironjacamar.embedded.dsl.resourceadapters20.api.ValidationType dashRaXmlVt =
            dashRaXmlCdt.getOrCreateValidation();

         if (validation)
         {
            dashRaXmlVt.validateOnMatch(Boolean.TRUE);
         }
         else
         {
            dashRaXmlVt.backgroundValidation(Boolean.TRUE).backgroundValidationMillis(0);
         }
      }

      dashRaXmlRt.getOrCreateAdminObjects().createAdminObject().className(TestAdminObjectImpl.class.getName())
         .jndiName("java:/eis/" + adminObjectID);
      
      return dashRaXml;
   }
}
