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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

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
 * Get a connection while TX is in beforeCompletion mode
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SynchronizationNarayanaTestCase
{
   private static Logger log = Logger.getLogger(SynchronizationNarayanaTestCase.class);

   /** Embedded */
   protected static Embedded embedded = null;

   /** TSR */
   protected static TransactionSynchronizationRegistry tsr = null;

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
            .jndiName("java:/eis/TxLogConnectionFactory").poolName("TxLog").useCcm(false);

      if ("XATransaction".equals(tx))
      {
         org.jboss.jca.embedded.dsl.resourceadapters11.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
            .minPoolSize(0).initialPoolSize(0).maxPoolSize(1);

         if (interleaving)
            dashRaXmlPt.interleaving();
      }
      else
      {
         org.jboss.jca.embedded.dsl.resourceadapters11.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
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
    * Success
    *
    * @param dashRaXml The deployment metadata
    * @param expect The expected string
    * @throws Throwable Thrown in case of an error
    */
   public void testSuccess(ResourceAdaptersDescriptor dashRaXml, String expect) throws Throwable
   {
      System.setProperty("ironjacamar.tracer.callstacks", "true");
      Context context = null;

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

         ut.begin();

         tsr.registerInterposedSynchronization(new ConnectionSynchronization(cf));

         TxLogConnection c = cf.getConnection();
         c.close();

         ut.commit();

         // Verify
         c = cf.getConnection();

         log.infof("Connection=%s", c);
         assertEquals(expect, c.getState());

         c.clearState();
         c.close();
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

         System.setProperty("ironjacamar.tracer.callstacks", "false");

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
    * Failure
    *
    * @param dashRaXml The deployment metadata
    * @param expect The expected string
    * @param interleaving Interleaving test case
    * @throws Throwable Thrown in case of an error
    */
   public void testFailure(ResourceAdaptersDescriptor dashRaXml, String expect, boolean interleaving) throws Throwable
   {
      System.setProperty("ironjacamar.tracer.callstacks", "true");
      Context context = null;

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

         ut.begin();

         tsr.registerInterposedSynchronization(new ConnectionSynchronization(cf));

         TxLogConnection c = cf.getConnection();
         c.fail();

         if (!interleaving)
         {
            // Lets just continue
            c = cf.getConnection();
            c.close();
         }

         ut.rollback();

         // Verify
         c = cf.getConnection();

         log.infof("Connection=%s", c);
         assertEquals(expect, c.getState());

         c.clearState();
         c.close();
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

         System.setProperty("ironjacamar.tracer.callstacks", "false");

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
    * Connection synchronization
    */
   static class ConnectionSynchronization implements Synchronization
   {
      private TxLogConnectionFactory cf;

      /**
       * Constructor
       * @param cf The connection factory
       */
      ConnectionSynchronization(TxLogConnectionFactory cf)
      {
         this.cf = cf;
      }

      /**
       * {@inheritDoc}
       */
      public void afterCompletion(int status)
      {
         log.infof("ConnectionSynchronization: afterCompletion(%d)", status);

         // No-op
      }

      /**
       * {@inheritDoc}
       */
      public void beforeCompletion()
      {
         log.infof("ConnectionSynchronization: beforeCompletion()");

         TxLogConnection c = null;
         try
         {
            c = cf.getConnection();
         }
         catch (Exception e)
         {
            log.error(e.getMessage(), e);
         }
         finally
         {
            if (c != null)
               c.close();
         }
      }
   }

   /**
    * Local
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testSuccessLocal() throws Throwable
   {
      testSuccess(createLocalTxDeployment(), "01");
   }

   /**
    * XA
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testSuccessXA() throws Throwable
   {
      testSuccess(createXATxDeployment(), "3B8");
   }

   /**
    * XA/Interleaving
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testSuccessXAInterleaving() throws Throwable
   {
      testSuccess(createXATxDeployment(true), "3D5DB8");
   }

   /**
    * Local
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testFailureLocal() throws Throwable
   {
      testFailure(createLocalTxDeployment(), "", false);
   }

   /**
    * XA
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testFailureXA() throws Throwable
   {
      testFailure(createXATxDeployment(), "", false);
   }

   /**
    * XA/Interleaving
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testFailureXAInterleaving() throws Throwable
   {
      testFailure(createXATxDeployment(true), "", true);
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create(true);

      // Startup
      embedded.startup();

      // Resolve TSR
      tsr = embedded.lookup("TransactionSynchronizationRegistry", TransactionSynchronizationRegistry.class);

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
      // TSR
      tsr = null;

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
