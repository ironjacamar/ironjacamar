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

package org.jboss.jca.test.deployers.annotations;

import org.jboss.jca.deployers.fungal.Annotations;
import org.jboss.jca.embedded.EmbeddedJCA;
import org.jboss.jca.fungal.deployers.DeployException;

import java.io.File;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.papaki.AnnotationRepository;
import org.jboss.papaki.AnnotationScanner;
import org.jboss.papaki.AnnotationScannerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for the annotations handling
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class AnnotationsTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger LOG = Logger.getLogger(AnnotationsTestCase.class);

   /*
    * Embedded
    */
   private static EmbeddedJCA embedded;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Process: Null arguemnts
    * @throws Throwable throwable exception 
    */
   @Test(expected = DeployException.class)
   public void testProcessNullArguments() throws Throwable
   {
      Annotations.process(null, null);
   }

   /**
    * Process: Null arguemnt for annotation repository
    * @throws Throwable throwable exception 
    */
   @Test(expected = DeployException.class)
   public void testProcessNullAnnotationRepository() throws Throwable
   {
      ConnectorMetaData cmd = new ConnectorMetaData();
      Annotations.process(cmd, null);
   }

   /**
    * Process: Null arguemnt for connector metadata
    * @throws Throwable throwable exception 
    */
   @Test
   public void testProcessNullConnectorMetaData() throws Throwable
   {
      try
      {
         URL url = getURL("ra16inout.rar");
         AnnotationScanner asf = AnnotationScannerFactory.getDefault();
         AnnotationRepository ar = asf.scan(new URL[] {url});

         Annotations.process(null, ar);
      }
      catch (Throwable t)
      {
         fail(t.getMessage());
      }
   }

   /**
    * Process: Connector -- verification of the processConnector method
    * @throws Throwable throwable exception 
    */
   @Test
   public void testProcessConnector() throws Throwable
   {
      try
      {
         URL url = getURL("ra16inoutnora.rar");
         AnnotationScanner asf = AnnotationScannerFactory.getDefault();
         AnnotationRepository ar = asf.scan(new URL[] {url});

         Annotations.process(null, ar);
      }
      catch (Throwable t)
      {
         fail(t.getMessage());
      }
   }

   /**
    * Process: Connector -- verification of the processConnector method
    * @throws Throwable throwable exception 
    */
   @Test
   public void testProcessConnectorFail() throws Throwable
   {
      try
      {
         URL url = getURL("rafail2connector.rar");
         AnnotationScanner asf = AnnotationScannerFactory.getDefault();
         AnnotationRepository ar = asf.scan(new URL[] {url});

         Annotations.process(null, ar);

         fail("Success");
      }
      catch (Throwable t)
      {
         // Ok
      }
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
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
   }

   /**
    * Get the URL for a test archive
    * @param archive The name of the test archive
    * @return The URL to the archive
    * @throws Throwable throwable exception
    */
   public URL getURL(String archive) throws Throwable
   {
      File f = new File(System.getProperty("archives.dir") + File.separator + archive);
      return f.toURI().toURL();
   }
}
