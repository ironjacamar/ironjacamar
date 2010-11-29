/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.adapters.jdbc.spi;

import org.jboss.jca.adapters.jdbc.spi.testimpl.TestValidConnectionChecker;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 *
 * A StaleConectionCheckerTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ValidConnectionCheckerTestCase
{

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * shouldInjectCorrectStaleConnectionChecker
    * @throws Throwable in case of error
    */
   @Test
   public void shouldInjectCorrectStaleConnectionChecker() throws Throwable
   {
      /*
       * Embedded
       */
      Embedded embedded = null;

      try
      {
         // Create and set an embedded JCA instance
         embedded = EmbeddedFactory.create();

         // Startup
         embedded.startup();

         // Deploy jdbc-local.rar
         embedded.deploy(getURL("jdbc-local.rar"));

         // Deploy H2 datasource
         URL url = getURL("test/h2-valid-connection-checker-ds.xml");

         embedded.deploy(url);

         Context context = null;

         try
         {
            context = new InitialContext();

            DataSource ds = (DataSource) context.lookup("java:/H2DS");
            assertNotNull(ds);

            Connection c = ds.getConnection();
            assertNotNull(c);
            c.close();
            c = ds.getConnection();

            Statement st = c.createStatement();
            st.execute("dssd");

         }
         catch (SQLException e)
         {
            // nothing we need an exception ;)
         }

         finally
         {
            assertThat(TestValidConnectionChecker.isConstructorInvoked(), is(true));

            assertThat(TestValidConnectionChecker.isMethodInvoked(), is(true));

            //assertThat(TestValidConnectionChecker.getInjectedInteger(), is(100));

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
      finally
      {
         if (embedded != null)
         {
            // Undeploy H2 datasource
            embedded.undeploy(getURL("test/h2-stale-connection-checker-ds.xml"));

            // Undeploy jdbc-local.rar
            embedded.undeploy(getURL("jdbc-local.rar"));

            // Shutdown embedded
            embedded.shutdown();

            // Set embedded to null
            embedded = null;
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

}
