/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.deployers.annotations;

import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.annotations.repository.jandex.AnnotationScannerImpl;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;

import java.io.File;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for the annotations handling
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
public class AnnotationsTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(AnnotationsTestCase.class);

   /*
    * Annotations
    */
   private Annotations annotations;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Process: Null arguemnts
    * @throws Throwable throwable exception
    */
   @Test(expected = ValidateException.class)
   public void testProcessNullArguments() throws Throwable
   {
      annotations.process(null, null, null);
   }

   /**
    * Process: Connector -- verification of the processConnector method
    * @throws Throwable throwable exception
    */
   @Test
   public void testProcessConnector() throws Throwable
   {
      proceed("ra16inoutanno");
   }

   /**
    * Process: Connector -- verification of the processConnector method
    * @throws Throwable throwable exception
    */
   @Test
   public void testProcessConnectorFail() throws Throwable
   {
      proceed("rafail2connector", false);
   }

   /**
    * Process: ConnectionDefinitions -- verification of the processConnectionDefinitions method
    * @throws Throwable throwable exception
    */
   @Test
   public void testProcessConnectionDefinitions() throws Throwable
   {
      proceed("ra16annoconndefs");
   }

   /**
    * Process: ConnectionDefinition -- verification of the processConnectionDefinition method
    * @throws Throwable throwable exception
    */
   @Test
   public void testProcessConnectionDefinition() throws Throwable
   {
      proceed("ra16annoconndef");
   }

   /**
    * Process: Activation -- verification of the processActivation method
    * @throws Throwable throwable exception
    */
   //@Test
   public void testProcessActivation() throws Throwable
   {
      proceed("ra16annoactiv");
   }

   /**
    * Process: AuthenticationMechanism -- verification of the processAuthenticationMechanism method
    * @throws Throwable throwable exception
    */
   @Test
   public void testProcessAuthenticationMechanism() throws Throwable
   {
      proceed("ra16annoauthmech");
   }

   /**
    * Process: AdministeredObject -- verification of the processAdministeredObject method
    * @throws Throwable throwable exception
    */
   @Test
   public void testProcessAdministeredObject() throws Throwable
   {
      proceed("ra16annoadminobj");
   }

   /**
    * Process: ConfigProperty -- verification of the processConfigProperty method
    * @throws Throwable throwable exception
    */
   @Test
   public void testProcessConfigProperty() throws Throwable
   {
      proceed("ra16annoconfprop");
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * be run before the Test method.
    * @throws Throwable throwable exception
    */
   @Before
   public void setup() throws Throwable
   {
      annotations = new Annotations();
   }

   /**
    * causes that method to be run after the Test method.
    * @throws Throwable throwable exception
    */
   @After
   public void tearDown() throws Throwable
   {
      annotations = null;
   }

   /**
    * 
    * Proceeds annotation parsing on archive
    * 
    * @param archive to proceed
    * @param shouldPass - is this a correct archive?
    */
   private void proceed(String archive, boolean shouldPass)
   {
      try
      {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         String pack = "org.jboss.jca.test.deployers.spec.rars";
         JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
         jar.addPackage(pack).addPackage(pack + "." + archive);

         String fileName = System.getProperty("archives.dir") + File.separator + archive + ".jar";
         File f = new File(fileName);
         jar.as(ZipExporter.class).exportTo(f, true);
         URL url = f.toURI().toURL();

         AnnotationScanner asf = new AnnotationScannerImpl();
         AnnotationRepository ar = asf.scan(new URL[] {url}, classLoader);
         Connector c = annotations.process(ar, null, classLoader);
         log.info("///Test:" + archive + c);
         if (!shouldPass)
            fail("Success");

         assertEquals(c, annotations.merge(null, ar, classLoader));
         assertEquals(c, annotations.merge(c, ar, classLoader));
      }
      catch (AssertionError e)
      {
         fail(e.getMessage());
      }
      catch (Throwable t)
      {
         t.printStackTrace();

         if (shouldPass)
         {
            fail(t.getMessage());
         }
      }

   }

   /**
    * 
    * Proceeds parsing of correct archive
    * 
    * @param archive to parse
    */
   private void proceed(String archive)
   {
      proceed(archive, true);
   }

}
