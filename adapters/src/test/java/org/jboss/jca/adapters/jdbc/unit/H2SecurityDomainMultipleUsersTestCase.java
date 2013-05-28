/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

import org.jboss.jca.adapters.ArquillianJCATestUtils;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for getting a connection from the H2 database backed by a security domain
 * and using multiple users
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
public class H2SecurityDomainMultipleUsersTestCase
{

   //-------------------------------------------------------------------------------------||
   //---------------------- GIVEN --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createArchive() throws Exception
   {
      return ArquillianJCATestUtils.buildShrinkwrapJdbcLocal();
   }

   /**
    * Define the security domain
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2, name = "security")
   public static Descriptor createSecurityDomain() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("h2-sd-mu.xml", 
                                                            cl.getResourceAsStream("h2-sd-mu.xml"));
      return isd;
   }

   /**
    * Define the -ds.xml
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 3, name = "datasources")
   public static Descriptor createDataSources() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("h2-sd-mu-ds.xml", 
                                                            cl.getResourceAsStream("h2-sd-mu-ds.xml"));
      return isd;
   }

   //-------------------------------------------------------------------------------------||
   //---------------------- WHEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   //
   @Resource(mappedName = "java:/H2DS")
   private DataSource ds;

   @Resource(mappedName = "java:/H2DS2")
   private DataSource ds2;

   //-------------------------------------------------------------------------------------||
   //---------------------- THEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Not null password
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testNotNullPassword() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection("sa", "sa");
      assertNotNull(c);
      c.close();
   }

   /**
    * Null password
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testNullPassword() throws Throwable
   {
      assertNotNull(ds);
      Connection c = null;
      try
      {
         c = ds.getConnection("sa", null);
         fail("Got connection");
      }
      catch (Exception t)
      {
         // Ok
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }

   /**
    * Not null password
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testNotNullPasswordOnDS2() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection("sa", "sa");
      assertNotNull(c);
      c.close();
   }

   /**
    * Null password
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testNullPasswordOnDS2() throws Throwable
   {
      assertNotNull(ds);
      Connection c = null;
      try
      {
         c = ds.getConnection("sa", null);
         fail("Got connection");
      }
      catch (Exception t)
      {
         // Ok
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }
}
