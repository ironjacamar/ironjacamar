/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.adapters.jdbc.unit;

import org.jboss.jca.embedded.EmbeddedJCA;

import java.io.File;
import java.net.URL;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for getting a XA connection from the H2 database
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class H2XATestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(H2XATestCase.class);

   private static final String JNDI_NAME = "java:/H2XADS";

   /*
    * Embedded
    */
   private static EmbeddedJCA embedded;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Get a connection from the database
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testConnection() throws Throwable
   {
      Context context = null;

      try
      {
         context = new InitialContext();
         
         DataSource ds = (DataSource)context.lookup(JNDI_NAME);
         assertNotNull(ds);

         Connection c = ds.getConnection();
         assertNotNull(c);
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
      }
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Get the URL for a test archive
    * @param archive The name of the test archive
    * @return The URL to the archive
    * @throws Throwable throwable exception
    */
   private static URL getURL(String archive) throws Throwable
   {
      File f = new File(System.getProperty("archives.dir") + File.separator + archive);
      return f.toURI().toURL();
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

      // Deploy jdbc-local.rar
      embedded.deploy(getURL("jdbc-xa.rar"));

      // Deploy H2 datasource
      embedded.deploy(getURL("test/h2-xa-ds.xml"));
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy H2 datasource
      embedded.undeploy(getURL("test/h2-xa-ds.xml"));

      // Undeploy jdbc-local.rar
      embedded.undeploy(getURL("jdbc-xa.rar"));

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
