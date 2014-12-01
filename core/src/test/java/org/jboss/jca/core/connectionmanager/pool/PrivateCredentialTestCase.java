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

import org.jboss.jca.core.connectionmanager.ConnectionManagerUtil;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactoryImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.core.security.SimplePrincipal;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdaptersDescriptor;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

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
 * Private credential test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public class PrivateCredentialTestCase
{
   private static Logger log = Logger.getLogger(PrivateCredentialTestCase.class);

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
      org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(SimpleManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/SimpleConnectionFactory").poolName("Simple");
      org.jboss.jca.embedded.dsl.resourceadapters12.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
         .minPoolSize(0).initialPoolSize(0).maxPoolSize(10).prefill(false);
      org.jboss.jca.embedded.dsl.resourceadapters12.api.SecurityType dashRaXmlSt = dashRaXmlCdt.getOrCreateSecurity()
         .securityDomain("PrivateCredentialSubjectFactory");

      return dashRaXml;
   }

   /**
    * Define the subject factory deployment
    * @return The descriptor
    * @exception Exception If an error occurs
    */
   protected static InputStreamDescriptor createSubjectFactoryDeployment() throws Exception
   {
      StringBuilder sb = new StringBuilder();

      sb = sb.append("<deployment>");
      sb = sb.append("<bean name=\"PrivateCredentialSubjectFactory\" class=\"");
      sb = sb.append(PrivateCredentialSubjectFactory.class.getName());
      sb = sb.append("\">");
      sb = sb.append("</bean>");
      sb = sb.append("</deployment>");

      ByteArrayInputStream i = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));

      return new InputStreamDescriptor("sf.xml", i);
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test that private credentials matter
    * @throws Throwable If an error occurs
    */
   @Test
   public void testPrivateCredentials() throws Throwable
   {
      Context context = null;

      InputStreamDescriptor sf = createSubjectFactoryDeployment();
      ResourceAdapterArchive raa = createArchiveDeployment();
      ResourceAdaptersDescriptor dashRaXml = createActivationDeployment();

      try
      {
         embedded.deploy(sf);
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         SimpleConnectionFactory cf = (SimpleConnectionFactory)context.lookup("java:/eis/SimpleConnectionFactory");
         assertNotNull(cf);

         SimpleConnection c1 = cf.getConnection();
         SimpleConnection c2 = cf.getConnection();

         org.jboss.jca.core.connectionmanager.ConnectionManager cm = ConnectionManagerUtil.extract(cf);
         assertNotNull(cm);

         assertEquals(2,
            ((org.jboss.jca.core.connectionmanager.pool.AbstractPool)cm.getPool()).getManagedConnectionPools().size());

         c1.close();
         c2.close();
      }
      catch (Exception e)
      {
         throw e;
      }
      finally
      {
         embedded.undeploy(dashRaXml);
         embedded.undeploy(raa);
         embedded.undeploy(sf);

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
    * Private credential subject factory
    */
   static class PrivateCredentialSubjectFactory implements SubjectFactory
   {
      private Principal principal;
      private Object pubCred;
      private boolean odd;

      /**
       * Constructor
       */
      PrivateCredentialSubjectFactory()
      {
         principal = new SimplePrincipal("principal");
         pubCred = new Object();
         odd = false;
      }

      /**
       * {@inheritDoc}
       */
      public Subject createSubject()
      {
         return createSubject(null);
      }

      /**
       * {@inheritDoc}
       */
      public Subject createSubject(String sd)
      {
         Subject subject = new Subject();

         subject.getPrincipals().add(principal);
         subject.getPublicCredentials().add(pubCred);
         if (odd)
         {
            subject.getPrivateCredentials().add(Boolean.FALSE);
         }
         else
         {
            subject.getPrivateCredentials().add(Boolean.TRUE);
         }

         odd = !odd;

         return subject;
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
