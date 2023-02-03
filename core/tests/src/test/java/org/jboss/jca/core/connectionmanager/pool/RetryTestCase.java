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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.connectionmanager.pool.retry.RetryConnection;
import org.jboss.jca.core.connectionmanager.pool.retry.RetryConnectionFactory;
import org.jboss.jca.core.connectionmanager.pool.retry.RetryManagedConnectionFactory;
import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdaptersDescriptor;

import java.util.UUID;

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
 * Retry test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public class RetryTestCase
{
   private static Logger log = Logger.getLogger(RetryTestCase.class);

   /** Embedded */
   protected static Embedded embedded = null;

   // --------------------------------------------------------------------------------||
   // Deployments --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Define the resource adapter deployment
    * @param tx Support transactions
    * @return The deployment archive
    */
   protected static ResourceAdapterArchive createArchiveDeployment(boolean tx)
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      OutboundResourceadapterType ort = rt.getOrCreateOutboundResourceadapter()
         .reauthenticationSupport(false);

      if (tx)
      {
         ort.transactionSupport("LocalTransaction");
      }
      else
      {
         ort.transactionSupport("NoTransaction");
      }

      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt =
         ort.createConnectionDefinition()
            .managedconnectionfactoryClass(RetryManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(RetryConnectionFactory.class.getName())
            .connectionfactoryImplClass(RetryConnectionFactory.class.getName())
            .connectionInterface(RetryConnection.class.getName())
            .connectionImplClass(RetryConnection.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "retry.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(RetryConnectionFactory.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Define the activation deployment with no retry
    * @return The deployment archive
    */
   protected static ResourceAdaptersDescriptor createActivationNoRetryDeployment()
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "retry-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("retry.rar");

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(RetryManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/RetryConnectionFactory").poolName("Retry");
      org.jboss.jca.embedded.dsl.resourceadapters11.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
         .minPoolSize(0).initialPoolSize(0).maxPoolSize(1).prefill(false);

      return dashRaXml;
   }

   /**
    * Define the activation deployment with 5 retries
    * @return The deployment archive
    */
   protected static ResourceAdaptersDescriptor createActivation5RetriesDeployment()
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "retry-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("retry.rar");

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(RetryManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/RetryConnectionFactory").poolName("Retry");
      org.jboss.jca.embedded.dsl.resourceadapters11.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
         .minPoolSize(0).initialPoolSize(0).maxPoolSize(1).prefill(false);
      org.jboss.jca.embedded.dsl.resourceadapters11.api.TimeoutType dashRaXmlTt = dashRaXmlCdt.getOrCreateTimeout()
         .allocationRetry(5).allocationRetryWaitMillis(10);

      return dashRaXml;
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * NoTransaction: Test no retry
    * @throws Throwable throwable exception
    */
   @Test
   public void testNoTransactionNoRetry() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment(false);
      ResourceAdaptersDescriptor dashRaXml = createActivationNoRetryDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         RetryManagedConnectionFactory.clearRetryableCounter();

         context = new InitialContext();

         RetryConnectionFactory cf = (RetryConnectionFactory)context.lookup("java:/eis/RetryConnectionFactory");
         assertNotNull(cf);

         RetryConnection c = cf.getConnection();
         c.close();

         fail("Should not be here");
      }
      catch (Exception e)
      {
         log.info("Error: " + e.getMessage(), e);
         assertEquals(2, RetryManagedConnectionFactory.getRetryableCounter());
      }
      finally
      {
         RetryManagedConnectionFactory.clearRetryableCounter();

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
    * NoTransaction: Test 5 retries
    * @throws Throwable throwable exception
    */
   @Test
   public void testNoTransaction5Retries() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment(false);
      ResourceAdaptersDescriptor dashRaXml = createActivation5RetriesDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         RetryManagedConnectionFactory.clearRetryableCounter();

         context = new InitialContext();

         RetryConnectionFactory cf = (RetryConnectionFactory)context.lookup("java:/eis/RetryConnectionFactory");
         assertNotNull(cf);

         RetryConnection c = cf.getConnection();
         c.close();

         fail("Should not be here");
      }
      catch (Exception e)
      {
         log.info("Error: " + e.getMessage(), e);
         assertEquals(6, RetryManagedConnectionFactory.getRetryableCounter());
      }
      finally
      {
         RetryManagedConnectionFactory.clearRetryableCounter();

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
    * LocalTransaction: Test no retry
    * @throws Throwable throwable exception
    */
   @Test
   public void testLocalTransactionNoRetry() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment(true);
      ResourceAdaptersDescriptor dashRaXml = createActivationNoRetryDeployment();

      UserTransaction ut = null;
      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         RetryManagedConnectionFactory.clearRetryableCounter();

         context = new InitialContext();

         ut = (UserTransaction)context.lookup("java:/UserTransaction");
         assertNotNull(ut);
         ut.begin();

         RetryConnectionFactory cf = (RetryConnectionFactory)context.lookup("java:/eis/RetryConnectionFactory");
         assertNotNull(cf);

         RetryConnection c = cf.getConnection();
         c.close();

         ut.commit();
         fail("Should not be here");
      }
      catch (Exception e)
      {
         log.info("Error: " + e.getMessage(), e);
         assertEquals(2, RetryManagedConnectionFactory.getRetryableCounter());
      }
      finally
      {
         ut.rollback();

         RetryManagedConnectionFactory.clearRetryableCounter();

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
    * LocalTransaction: Test 5 retries
    * @throws Throwable throwable exception
    */
   @Test
   public void testLocalTransaction5Retries() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment(true);
      ResourceAdaptersDescriptor dashRaXml = createActivation5RetriesDeployment();

      UserTransaction ut = null;
      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         RetryManagedConnectionFactory.clearRetryableCounter();

         context = new InitialContext();

         ut = (UserTransaction)context.lookup("java:/UserTransaction");
         assertNotNull(ut);
         ut.begin();

         RetryConnectionFactory cf = (RetryConnectionFactory)context.lookup("java:/eis/RetryConnectionFactory");
         assertNotNull(cf);

         RetryConnection c = cf.getConnection();
         c.close();

         ut.commit();
         fail("Should not be here");
      }
      catch (Exception e)
      {
         log.info("Error: " + e.getMessage(), e);
         assertEquals(6, RetryManagedConnectionFactory.getRetryableCounter());
      }
      finally
      {
         ut.rollback();

         RetryManagedConnectionFactory.clearRetryableCounter();

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
      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
