/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.rars.generic.mcf.jdbc.local.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Test;

import org.jboss.test.rars.RARTest;

/**
 * JDBC Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class JDBCUnitTestCase extends RARTest
{
   public void testNoTx() throws Throwable
   {
      DataSource ds = (DataSource) getBean("ConnectionFactory");
      Connection c = ds.getConnection();
      assertNotNull("Should have a connection", c);
      try
      {
         Statement s = c.createStatement();
         s.execute("create table test (key varchar(10), value varchar(10))");
         Connection c1 = s.getConnection();
         assertTrue(c == c1);
         Statement s1 = c.createStatement();
         s1.executeQuery("select 1 from test");
         s1.close();
      }
      finally
      {
         c.close();
      }
      c = ds.getConnection("sa", "");
      assertNotNull("Should have a connection", c);
      try
      {
         DatabaseMetaData m = c.getMetaData();
         m.getCatalogs();
      }
      finally
      {
         c.close();
      }
   }

   public static Test suite() throws Exception
   {
      return suite(JDBCUnitTestCase.class);
   }

   public JDBCUnitTestCase (String name)
   {
      super(name);
   }
}
