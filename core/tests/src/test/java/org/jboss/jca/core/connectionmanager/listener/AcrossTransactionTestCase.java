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

import org.jboss.jca.core.tx.rars.txlog.TxLogConnection;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionFactory;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionFactoryImpl;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionImpl;
import org.jboss.jca.core.tx.rars.txlog.TxLogManagedConnectionFactory;
import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdaptersDescriptor;

import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.UserTransaction;

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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Using a connection across transaction boundaries
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AcrossTransactionTestCase
{
   private static Logger log = Logger.getLogger(AcrossTransactionTestCase.class);

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
      org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(TxLogManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/TxLogConnectionFactory").poolName("TxLog").tracking(true);

      if ("XATransaction".equals(tx))
      {
         org.jboss.jca.embedded.dsl.resourceadapters12.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(1);

         if (interleaving)
            dashRaXmlPt.interleaving();
      }
      else
      {
         org.jboss.jca.embedded.dsl.resourceadapters12.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(1);
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
    * Create a NoTransaction deployment
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createNoTxDeployment()
   {
      return createDeployment("NoTransaction", false);
   }

   /**
    * Base
    *
    * @param dashRaXml The deployment metadata
    * @param expect The expected string
    * @throws Throwable Thrown in case of an error
    */
   public void testBase(ResourceAdaptersDescriptor dashRaXml, String expect) throws Throwable
   {
      Context context = null;
      TxLogConnection c = null;

      ResourceAdapterArchive raa = createRar();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         UserTransaction ut = (UserTransaction)context.lookup("java:/UserTransaction");
         assertNotNull(ut);

         TxLogConnectionFactory cf = (TxLogConnectionFactory)context.lookup("java:/eis/TxLogConnectionFactory");
         assertNotNull(cf);

         log.infof("Get connection");

         ut.begin();

         c = cf.getConnection();

         ut.commit();

         log.infof("Close connection");

         ut.begin();

         c.close();

         ut.commit();

         assertEquals(expect, c.getState());
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         fail(t.getMessage());
      }
      finally
      {
         embedded.undeploy(dashRaXml);
         embedded.undeploy(raa);

         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * No
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testNo() throws Throwable
   {
      log.infof("testNo()");
      testBase(createNoTxDeployment(), "");
   }

   /**
    * Local
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testLocal() throws Throwable
   {
      log.infof("testLocal()");
      testBase(createLocalTxDeployment(), "01");
   }

   /**
    * XA
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testXA() throws Throwable
   {
      log.infof("testXA()");
      testBase(createXATxDeployment(), "3B8");
   }

   /**
    * XA/Interleaving
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testXAInterleaving() throws Throwable
   {
      log.infof("testXAInterleaving()");
      testBase(createXATxDeployment(true), "3B8");
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
      URL stdio = AcrossTransactionTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = AcrossTransactionTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = AcrossTransactionTestCase.class.getClassLoader().getResource("validating-transaction.xml");
      URL jca = AcrossTransactionTestCase.class.getClassLoader().getResource("jca.xml");

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
      URL stdio = AcrossTransactionTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = AcrossTransactionTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = AcrossTransactionTestCase.class.getClassLoader().getResource("validating-transaction.xml");
      URL jca = AcrossTransactionTestCase.class.getClassLoader().getResource("jca.xml");

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
