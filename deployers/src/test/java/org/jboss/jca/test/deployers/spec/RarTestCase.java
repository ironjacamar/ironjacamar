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

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

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
 * Test cases for deploying resource adapter archives (.RAR)
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class RarTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(RarTestCase.class);

   private static final String JNDI_PREFIX = "java:/eis/";

   /*
    * Embedded
    */
   private static Embedded embedded;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * ra10dtdout.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa10dtdout() throws Throwable
   {
      URL archive = getURL("ra10dtdout.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra10dtdout");
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

         embedded.undeploy(archive);
      }
   }
   
   /**
    * ra15dtdinout.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa15dtdinout() throws Throwable
   {
      URL archive = getURL("ra15dtdinout.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra15dtdinout");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra15inoutjbossra.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa15inoutjbossra() throws Throwable
   {
      URL archive = getURL("ra15inoutjbossra.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra15inoutjbossra");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra15inout.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa15inout() throws Throwable
   {
      URL archive = getURL("ra15inout.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra15inout");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra15outjbossradefaultns.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa15outjbossradefaultns() throws Throwable
   {
      URL archive = getURL("ra15outjbossradefaultns.rar");
      Context context = null;
      
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra15outjbossradefaultns");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra15outjbossra.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa15outjbossra() throws Throwable
   {
      URL archive = getURL("ra15outjbossra.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra15outjbossra");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra15out.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa15out() throws Throwable
   {
      URL archive = getURL("ra15out.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra15out");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16dtdinout.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16dtdinout() throws Throwable
   {
      URL archive = getURL("ra16dtdinout.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16dtdinout");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16inoutanno.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16inoutanno() throws Throwable
   {
      URL archive = getURL("ra16inoutanno.rar");
      Context context = null;
      
      try
      {
         embedded.deploy(archive);

         /*
           TODO - as there are multiple connection factories

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16inoutanno");
         assertNotNull(o);
         */
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16inoutmultianno.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16inoutmultianno() throws Throwable
   {
      URL archive = getURL("ra16inoutmultianno.rar");
      Context context = null;
      
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16inoutmultianno");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16inoutjar.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16inoutjar() throws Throwable
   {
      URL archive = getURL("ra16inoutjar.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16inoutjar");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16inoutjbossra.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16inoutjbossra() throws Throwable
   {
      URL archive = getURL("ra16inoutjbossra.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16inoutjbossra");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16inoutnora.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16inoutnora() throws Throwable
   {
      URL archive = getURL("ra16inoutnora.rar");
      Context context = null;
      
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16inoutnora");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16inoutoverwrite.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16inoutoverwrite() throws Throwable
   {
      URL archive = getURL("ra16inoutoverwrite.rar");
      Context context = null;
      
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16inoutoverwrite");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16inout.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16inout() throws Throwable
   {
      URL archive = getURL("ra16inout.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16inout");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16outjbossradefaultns.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16outjbossradefaultns() throws Throwable
   {
      URL archive = getURL("ra16outjbossradefaultns.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16outjbossradefaultns");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16outjbossra.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16outjbossra() throws Throwable
   {
      URL archive = getURL("ra16outjbossra.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16outjbossra");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16outnora.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16outnora() throws Throwable
   {
      URL archive = getURL("ra16outnora.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16outnora");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16out.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16out() throws Throwable
   {
      URL archive = getURL("ra16out.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16out");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16standard303jbossra.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16standard303jbossra() throws Throwable
   {
      URL archive = getURL("ra16standard303jbossra.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16standard303jbossra");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16standard303.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16standard303() throws Throwable
   {
      URL archive = getURL("ra16standard303.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16standard303");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16user303jbossra.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16user303jbossra() throws Throwable
   {
      URL archive = getURL("ra16user303jbossra.rar");
 
      try
      {
         embedded.deploy(archive);
         fail("Deployment success");
      }
      catch (Throwable t)
      {
         // Ok
      }
      finally
      {
         embedded.undeploy(archive);
      }
   }

   /**
    * ra16user303.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16user303() throws Throwable
   {
      URL archive = getURL("ra16user303.rar");
      Context context = null;
 
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16user303");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16annoconfprop.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16annoconfprop() throws Throwable
   {
      URL archive = getURL("ra16annoconfprop.rar");
      Context context = null;
            
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16annoconfprop");
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

         embedded.undeploy(archive);
      }
   }

   /**
    * ra16asso.rar
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRa16asso() throws Throwable
   {
      URL archive = getURL("ra16asso.rar");
      Context context = null;
      
      try
      {
         embedded.deploy(archive);

         context = new InitialContext();
         Object o = context.lookup(JNDI_PREFIX + "ra16asso");
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
