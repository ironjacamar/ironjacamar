/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.spec.chapter11.api;

import org.jboss.jca.core.workmanager.rars.wm.ContextConnection;
import org.jboss.jca.core.workmanager.rars.wm.ContextConnectionFactory;
import org.jboss.jca.core.workmanager.rars.wm.ContextConnectionFactoryImpl;
import org.jboss.jca.core.workmanager.rars.wm.ContextConnectionImpl;
import org.jboss.jca.core.workmanager.rars.wm.ContextManagedConnection;
import org.jboss.jca.core.workmanager.rars.wm.ContextManagedConnectionFactory;
import org.jboss.jca.core.workmanager.rars.wm.ContextManagedConnectionMetaData;
import org.jboss.jca.core.workmanager.rars.wm.ContextRaMetaData;
import org.jboss.jca.core.workmanager.rars.wm.ImplementedContextResourceAdapter;
import org.jboss.jca.core.workmanager.rars.wm.MixedContextResourceAdapter;
import org.jboss.jca.core.workmanager.rars.wm.SupportedContextResourceAdapter;
import org.jboss.jca.core.workmanager.rars.wm.UnsupportedContextResourceAdapter;
import org.jboss.jca.core.workmanager.spec.chapter11.common.HintsContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.TransactionContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.UnsupportedContext;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.TransactionContext;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * WorkContextDeploymentTestCase
 *
 * Tests for the RA deployments with different work contexts
 * JCA 1.6 spec, chapter 11.4.1
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
public class WorkContextDeploymentTestCase
{
   private static Logger log = Logger.getLogger(WorkContextDeploymentTestCase.class);

   private final String pref = "java:/eis/";

   /*
    * Embedded
    */
   private static Embedded embedded;
   
   /**
    * Creates an archive, including parameter class
    * @param classRA - to be included
    * @return .rar archive
    */
   public static ResourceAdapterArchive createAnnotationDeployment(Class classRA)
   {
      String name = classRA.toString();
      name = name.substring(name.lastIndexOf(".") + 1);

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, name + ".rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "work.jar");
      ja.addClasses(ContextConnection.class, ContextConnectionFactory.class, ContextConnectionFactoryImpl.class,
            ContextConnectionImpl.class, ContextManagedConnection.class, ContextManagedConnectionFactory.class,
            ContextManagedConnectionMetaData.class, ContextRaMetaData.class, classRA, HintsContextCustom.class,
            TransactionContextCustom.class, UnsupportedContext.class, SecurityContext.class, TransactionContext.class);

      raa.addAsLibrary(ja);

      return raa;
   }

   /**
    * Creates an archive, using defined descriptor name
    * @param name of ra.xml descriptor
    * @return .rar archive
    */
   public static ResourceAdapterArchive createDescriptorDeployment(String name)
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, name + ".rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "arch.jar");
      ja.addPackage(ContextConnection.class.getPackage()).addClasses(HintsContextCustom.class,
            TransactionContextCustom.class, UnsupportedContext.class, SecurityContext.class, TransactionContext.class);

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("rars/wm/META-INF/ra-" + name + ".xml", "ra.xml");

      return raa;
   }

   /**
    * Deploys .rar archive and tries to look up for name of connection factory
    * @param raa - archive
    * @param name - to find
    * @throws Throwable throwable exception
    */
   public void testDeployment(ResourceAdapterArchive raa, String name) throws Throwable
   {
      InitialContext context = null;
      log.info("///////BeforeDeployment");

      embedded.deploy(raa);
      log.info("///////AfterDeployment");
      context = new InitialContext();
      ContextConnectionFactory cf = (ContextConnectionFactory) context.lookup(pref + name);
      assertNotNull(cf);
      log.info("///////ConnectionFactory:" + cf);

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

      embedded.undeploy(raa);

   }

   /**
    * Tests deployment with ResourceAdapter implementation class, containing annotatiots
    * @param classRA - implementation
    * @throws Throwable throwable exception
    */
   public void testAnnoDeployment(Class classRA) throws Throwable
   {
      String name = classRA.toString();
      name = name.substring(name.lastIndexOf(".") + 1);

      log.info("/////ANNO " + name + " started ////");
      testDeployment(createAnnotationDeployment(classRA), name);
   }

   /**
    * Tests deployment with ra.xml descriptor
    * @param name of appropriate descriptor 
    * @throws Throwable throwable exception
    */
   public void testDescDeployment(String name) throws Throwable
   {
      log.info("/////DESC " + name + " started ////");
      testDeployment(createDescriptorDeployment(name), name);
   }

   /**
    * Test for RAR, containing annotated class with supported WorkContexts
    * @throws Throwable throwable exception
    */
   @Test
   public void testAnnoSuportedContext() throws Throwable
   {
      testAnnoDeployment(SupportedContextResourceAdapter.class);
   }

   /**
    * Test for RAR, containing ra.xml descriptor with supported WorkContexts
    * @throws Throwable throwable exception
    */
   @Test
   public void testDescSuportedContext() throws Throwable
   {
      testDescDeployment("supported");
   }

   /**
    * Test for RAR, containing annotated class with unsupported WorkContexts
    * @throws Throwable throwable exception
    */
   @Test(expected = Exception.class)
   public void testAnnoUnSuportedContext() throws Throwable
   {
      testAnnoDeployment(UnsupportedContextResourceAdapter.class);
   }

   /**
    * Test for RAR, containing ra.xml descriptor with unsupported WorkContexts
    * @throws Throwable throwable exception
    */
   @Test(expected = Exception.class)
   public void testDescUnSuportedContext() throws Throwable
   {
      testDescDeployment("unsupported");
   }

   /**
    * Test for RAR, containing annotated class with implemented WorkContexts
    * @throws Throwable throwable exception
    */
   @Test(expected = Exception.class)
   public void testAnnoImplementedContext() throws Throwable
   {
      testAnnoDeployment(ImplementedContextResourceAdapter.class);
   }

   /**
    * Test for RAR, containing ra.xml descriptor with implemented WorkContexts
    * @throws Throwable throwable exception
    */
   @Test(expected = Exception.class)
   public void testDescImplementedContext() throws Throwable
   {
      testDescDeployment("implemented");
   }

   /**
    * Test for RAR, containing annotated class with supported and unsupported WorkContexts
    * @throws Throwable throwable exception
    */
   @Test(expected = Exception.class)
   public void testAnnoMixedContext() throws Throwable
   {
      testAnnoDeployment(MixedContextResourceAdapter.class);
   }

   /**
    * Test for RAR, containing ra.xml descriptor with supported and unsupported WorkContexts
    * @throws Throwable throwable exception
    */
   @Test(expected = Exception.class)
   public void testDescMixedContext() throws Throwable
   {
      testDescDeployment("mixed");
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create();

      // Startup
      embedded.startup();
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
