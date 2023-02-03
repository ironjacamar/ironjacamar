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
import org.jboss.jca.adapters.jdbc.WrappedConnection;
import org.jboss.jca.arquillian.embedded.Configuration;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.sql.Connection;
import java.sql.SQLException;

import jakarta.annotation.Resource;
import javax.sql.DataSource;

import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.arquillian.junit.Arquillian;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for wrapper
 *
 * @author <a href="mailto:johnathonlee@ironjacamar.org">Johnathon Lee</a>
 */
@RunWith(Arquillian.class)
@Configuration(autoActivate = false)
@BMRule(name = "Verify BaseWrapperManagedConnection receives the error",
           targetClass = "BaseWrapperManagedConnection",
           targetMethod = "connectionError",
           action = "flag(\"exceptionThrown\"); org.junit.Assert.assertEquals(\"DESIRED ERROR\", $1.getMessage())")
@Ignore
//TODO after https://issues.redhat.com/browse/BYTEMAN-402
public class CheckedExceptionBMTestCase
{

   private static Logger log = Logger.getLogger(CheckedExceptionBMTestCase.class);
   
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
    * Byteman
    * @exception Throwable Thrown if case of an error
    */
   @Test
   @BMRules({
   @BMRule(name = "Throw exception on getUnderlyingConnection",
           targetClass = "BaseWrapperManagedConnection",
           targetMethod = "getRealConnection",
           targetLocation = "ENTRY",
           action = "clear(\"exceptionThrown\"); throw new java.sql.SQLException(\"DESIRED ERROR\")"),
   @BMRule(name = "check for exceptionThrown flag",
           targetClass = "WrappedConnection",
           targetMethod = "getUnderlyingConnection",
           targetLocation = "THROW",
           condition = "!flagged(\"exceptionThrown\")",
           action = "throw new RuntimeException(\"FAILED\")") })
   public void testGetUnderlying() throws Throwable
   {
      assertNotNull(ds);
      
      Connection c = null;

      try
      {
         c = ds.getConnection();
         
         ((WrappedConnection) c).getUnderlyingConnection();
         fail("No Error on getUnderlyingConnection");
      }
      catch (SQLException se)
      {
         assertEquals("DESIRED ERROR", se.getMessage());
      }
      catch (Throwable t)
      {
         fail(t.getMessage());
         throw t;
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }
}
