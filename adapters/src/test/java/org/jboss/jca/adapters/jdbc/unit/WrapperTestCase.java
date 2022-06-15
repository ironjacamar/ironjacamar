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
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for wrapper
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
public class WrapperTestCase
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
    * Define the -ds.xml
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("h2-ds.xml",
                                                            cl.getResourceAsStream("h2-ds.xml"));
      return isd;
   }

   //-------------------------------------------------------------------------------------||
   //---------------------- WHEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   //
   @Resource(mappedName = "java:/H2DS")
   private DataSource ds;

   //-------------------------------------------------------------------------------------||
   //---------------------- THEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Case 1
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testIsWraperForSelf() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      assertTrue(c.isWrapperFor(Connection.class));

      c.close();
   }

   /**
    * Case 1
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testUnwrapSelf() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      Connection c2 = c.unwrap(Connection.class);
      assertEquals(c, c2);

      c.close();
   }

   /**
    * Case 2
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testUnwrapDirect() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      org.h2.jdbc.JdbcConnection c2 = c.unwrap(org.h2.jdbc.JdbcConnection.class);
      assertNotNull(c2);

      c.close();
   }

   /**
    * Case 2
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testIsWrapperForDirect() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      assertTrue(c.isWrapperFor(org.h2.jdbc.JdbcConnection.class));

      c.close();
   }

   /**
    * c.isWrapperFor() on closed connection should not throw NPE.
    *
    * https://issues.jboss.org/browse/JBJCA-1389
    */
   @Test
   public void testIsWrapperForClosedConnection() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      assertTrue(c.isWrapperFor(org.h2.jdbc.JdbcConnection.class));

      c.close();

      try {
         c.isWrapperFor(org.h2.jdbc.JdbcConnection.class);
         fail("SQLException was expected.");
      } catch (Exception e) {
         assertTrue(e instanceof SQLException);
      }
   }

   /**
    * c.unwrap() on closed connection should not throw NPE.
    *
    * https://issues.jboss.org/browse/JBJCA-1389
    */
   @Test
   public void testUnwrapClosedConnection() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      assertNotNull(c.unwrap(org.h2.jdbc.JdbcConnection.class));

      c.close();

      try {
         c.unwrap(org.h2.jdbc.JdbcConnection.class);
         fail("SQLException was expected.");
      } catch (Exception e) {
         assertTrue(e instanceof SQLException);
      }
   }

   /**
    * Tests that autoCommit is propogated to the underlying connection.
    */
   @Test
   public void testAutoCommit() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertTrue(c.getAutoCommit());
      c.setAutoCommit(false);
      assertNotNull(c);

      org.h2.jdbc.JdbcConnection c2 = c.unwrap(org.h2.jdbc.JdbcConnection.class);
      assertNotNull(c2);

      assertFalse(c2.getAutoCommit());

      c.close();
   }

   /**
    * Tests that the closed information is represented correctly
    */
   @Test
   public void testIsClosed() throws Throwable {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertFalse(c.isClosed());
      c.close();
      assertTrue(c.isClosed());
   }
}
