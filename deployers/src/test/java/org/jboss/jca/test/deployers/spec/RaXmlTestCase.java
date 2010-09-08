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

package org.jboss.jca.test.deployers.spec;

import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.EmbeddedJCA;

import java.io.File;
import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for deploying resource adapter archives (.RAR) using -ra.xml files
 * for activation
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class RaXmlTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(RaXmlTestCase.class);

   private static final String JNDI_PREFIX = "java:/eis/";

   /*
    * Embedded
    */
   private static EmbeddedJCA embedded;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * ra16out.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16out() throws Throwable
   {
      URL archive = getURL("ra16out.rar");
      URL raXml = getURL("test" + File.separator + "ra16out-ra.xml");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);
         embedded.deploy(raXml);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16out-raxml");
         assertNotNull(o);
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         fail(t.getMessage());
      }
      finally
      {
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

         embedded.undeploy(raXml);
         embedded.undeploy(archive);
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
      // Create and set an embedded JCA instance
      embedded = new EmbeddedJCA();

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
