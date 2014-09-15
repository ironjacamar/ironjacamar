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

import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerUtil;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactoryImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.core.spi.graceful.GracefulCallback;
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
 * Shutdown test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public class ShutdownTestCase
{
   private static Logger log = Logger.getLogger(ShutdownTestCase.class);

   /** Embedded */
   protected static Embedded embedded = null;

   // --------------------------------------------------------------------------------||
   // Deployments --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Define the resource adapter deployment
    * @return The deployment archive
    */
   protected static ResourceAdapterArchive createArchiveDeployment()
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      OutboundResourceadapterType ort = rt.getOrCreateOutboundResourceadapter()
         .transactionSupport("NoTransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt =
         ort.createConnectionDefinition()
            .managedconnectionfactoryClass(SimpleManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(SimpleConnectionFactory.class.getName())
            .connectionfactoryImplClass(SimpleConnectionFactoryImpl.class.getName())
            .connectionInterface(SimpleConnection.class.getName())
            .connectionImplClass(SimpleConnectionImpl.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "simple.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(SimpleConnectionFactory.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Define the activation deployment
    * @return The deployment archive
    */
   protected static ResourceAdaptersDescriptor createActivationDeployment()
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "simple-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("simple.rar");

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters11.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(SimpleManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/SimpleConnectionFactory").poolName("Simple");

      /*
         org.jboss.jca.embedded.dsl.resourceadapters11.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(1).initialPoolSize(1).maxPoolSize(1);
      */

      return dashRaXml;
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test shutdown followed by getConnection
    * @throws Throwable throwable exception
    */
   @Test
   public void testShutdownGetConnection() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);
         cm.shutdown();

         SimpleConnection c = cf.getConnection();
         c.close();

         fail("Should not be here");
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
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
    * Test getConnection followed by shutdown
    * @throws Throwable throwable exception
    */
   @Test
   public void testGetConnectionShutdown() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         SimpleConnection c = cf.getConnection();

         ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);
         cm.shutdown();

         assertTrue(c.isDetached());

         c.close();
      }
      catch (Exception e)
      {
         fail("Should not be here");
         log.error(e.getMessage(), e);
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
    * Test getConnection followed by prepare shutdown
    * @throws Throwable throwable exception
    */
   @Test
   public void testGetConnectionPrepareShutdown() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         SimpleConnection c = cf.getConnection();

         ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);
         cm.prepareShutdown();

         SimpleConnection c2 = null;
         try
         {
            c2 = cf.getConnection();
            fail("Got 2nd connection");
         }
         catch (Exception inner)
         {
            // Ok
         }

         c.close();
      }
      catch (Exception e)
      {
         fail("Should not be here");
         log.error(e.getMessage(), e);
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
    * Test getConnection followed by prepare shutdown, and cancel
    * @throws Throwable throwable exception
    */
   @Test
   public void testGetConnectionPrepareShutdownAndCancel() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         SimpleConnection c = cf.getConnection();

         ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);
         cm.prepareShutdown();

         SimpleConnection c2 = null;
         try
         {
            c2 = cf.getConnection();
            fail("Got 2nd connection");
         }
         catch (Exception inner)
         {
            // Ok
         }

         cm.cancelShutdown();

         c.close();

         c2 = cf.getConnection();
         c2.close();

         assertNotNull(cm.getPool());
      }
      catch (Exception e)
      {
         fail("Should not be here");
         log.error(e.getMessage(), e);
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
    * Test getConnection followed by prepare shutdown with seconds
    * @throws Throwable throwable exception
    */
   @Test
   public void testGetConnectionPrepareShutdownWithSeconds() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         SimpleConnection c = cf.getConnection();

         ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);
         cm.prepareShutdown(1);

         Thread.sleep(1500L);

         SimpleConnection c2 = null;
         try
         {
            c2 = cf.getConnection();
            fail("Got 2nd connection");
         }
         catch (Exception inner)
         {
            // Ok
         }

         c.close();
      }
      catch (Exception e)
      {
         fail("Should not be here");
         log.error(e.getMessage(), e);
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
    * Test getConnection followed by prepare shutdown with seconds, and cancel
    * @throws Throwable throwable exception
    */
   @Test
   public void testGetConnectionPrepareShutdownWithSecondsAndCancel() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         SimpleConnection c = cf.getConnection();

         ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);
         cm.prepareShutdown(1);

         SimpleConnection c2 = null;
         try
         {
            c2 = cf.getConnection();
            fail("Got 2nd connection");
         }
         catch (Exception inner)
         {
            // Ok
         }

         cm.cancelShutdown();

         c.close();

         c2 = cf.getConnection();
         c2.close();

         assertNotNull(cm.getPool());
      }
      catch (Exception e)
      {
         fail("Should not be here");
         log.error(e.getMessage(), e);
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
    * Test getConnection followed by prepare shutdown with seconds, and callback
    * @throws Throwable throwable exception
    */
   @Test
   public void testGetConnectionPrepareShutdownWithSecondsAndCallback() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         SimpleConnection c = cf.getConnection();

         ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);

         ShutdownCallback cb = new ShutdownCallback();

         cm.prepareShutdown(1, cb);

         Thread.sleep(1500L);

         c.close();

         assertTrue(cb.wasCalled());
      }
      catch (Exception e)
      {
         fail("Should not be here");
         log.error(e.getMessage(), e);
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
    * Callback
    */
   static class ShutdownCallback implements GracefulCallback
   {
      private boolean called;

      /**
       * Constructor
       */
      ShutdownCallback()
      {
         this.called = false;
      }

      /**
       * Was called
       * @return The result
       */
      boolean wasCalled()
      {
         return called;
      }

      /**
       * {@inheritDoc}
       */
      public void done()
      {
         called = true;
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
