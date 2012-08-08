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

import org.jboss.jca.adapters.ArquillianJCATestUtils;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for prepared statement cache
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class PreparedStatementTestCase
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
      InputStreamDescriptor isd = new InputStreamDescriptor("h2-prepared-statement-ds.xml",
                                                            cl.getResourceAsStream("h2-prepared-statement-ds.xml"));
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
    * One prepared statement
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testPreparedStatementOne() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      Statement st = c.createStatement();
      assertNotNull(st);
      
      // Create
      st.execute("CREATE TABLE Employees (Employee_ID INTEGER, Name VARCHAR(30));");
      st.execute("INSERT INTO Employees VALUES (1, '#1')");
      st.execute("INSERT INTO Employees VALUES (2, '#2')");

      // Prepared statement
      PreparedStatement pstmt = c.prepareStatement("SELECT * FROM Employees WHERE Employee_ID = ?");
      assertNotNull(pstmt);

      pstmt.setInt(1, 1);

      ResultSet rs = pstmt.executeQuery();
      assertNotNull(rs);

      assertTrue(rs.next());

      // Drop
      st.execute("DROP TABLE Employees");

      rs.close();
      pstmt.close();
      st.close();
      c.close();
   }

   /**
    * One prepared statement (closed)
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testPreparedStatementOneClosed() throws Throwable
   {
      Connection c = null;
      Statement st = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try
      {
         assertNotNull(ds);
         c = ds.getConnection();
         assertNotNull(c);

         st = c.createStatement();
         assertNotNull(st);
      
         // Create
         st.execute("CREATE TABLE Employees (Employee_ID INTEGER, Name VARCHAR(30));");
         st.execute("INSERT INTO Employees VALUES (1, '#1')");
         st.execute("INSERT INTO Employees VALUES (2, '#2')");
         
         // Prepared statement
         pstmt = c.prepareStatement("SELECT * FROM Employees WHERE Employee_ID = ?");
         assertNotNull(pstmt);

         pstmt.close();

         pstmt.setInt(1, 1);
         
         rs = pstmt.executeQuery();

         fail("Able to execute prepared statement");
      }
      catch (SQLException e)
      {
         // Ok
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException ignore)
            {
               // Ignore
            }
         }

         if (pstmt != null)
         {
            try
            {
               pstmt.close();
            }
            catch (SQLException ignore)
            {
               // Ignore
            }
         }

         if (st != null)
         {
            // Drop
            st.execute("DROP TABLE Employees");

            try
            {
               st.close();
            }
            catch (SQLException ignore)
            {
               // Ignore
            }
         }

         if (c != null)
         {
            try
            {
               c.close();
            }
            catch (SQLException ignore)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Two prepared statements
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testPreparedStatementTwo() throws Throwable
   {
      assertNotNull(ds);
      Connection c = ds.getConnection();
      assertNotNull(c);

      Statement st = c.createStatement();
      assertNotNull(st);

      // Create
      st.execute("CREATE TABLE Employees (Employee_ID INTEGER, Name VARCHAR(30));");
      st.execute("INSERT INTO Employees VALUES (1, '#1')");
      st.execute("INSERT INTO Employees VALUES (2, '#2')");

      // Prepared statement 1
      PreparedStatement pstmt1 = c.prepareStatement("SELECT * FROM Employees WHERE Employee_ID = ?");
      assertNotNull(pstmt1);

      pstmt1.setInt(1, 1);

      ResultSet rs1 = pstmt1.executeQuery();
      assertNotNull(rs1);

      assertTrue(rs1.next());

      // Prepared statement 2
      PreparedStatement pstmt2 = c.prepareStatement("SELECT * FROM Employees WHERE Name = ?");
      assertNotNull(pstmt2);

      pstmt2.setString(1, "#1");

      ResultSet rs2 = pstmt2.executeQuery();
      assertNotNull(rs2);

      assertTrue(rs2.next());

      // Prepared statement 1
      pstmt1.setInt(1, 2);

      ResultSet rs3 = pstmt1.executeQuery();
      assertNotNull(rs3);

      assertTrue(rs3.next());



      // Drop
      st.execute("DROP TABLE Employees");

      rs1.close();
      rs3.close();
      pstmt1.close();

      rs2.close();
      pstmt2.close();

      st.close();
      c.close();
   }
}
