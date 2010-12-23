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

import org.jboss.jca.adapters.jdbc.spi.testimpl.TestExceptionSorter;
import org.jboss.jca.embedded.arquillian.ArquillianJCATestUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

/**
 *
 * A ExceptionSorterTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
@RunWith(Arquillian.class)
public class ExceptionSorterTestCase
{

   //-------------------------------------------------------------------------------------||
   //---------------------- GIVEN --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      String archiveName = "jdbc-local.rar";
      ResourceAdapterArchive raa = ArquillianJCATestUtils.buildShrinkwrapJdbcLocal(archiveName);
      ResourceAdapterArchive external = ShrinkWrap.create(ResourceAdapterArchive.class, "complex_" + archiveName);
      external.add(raa, "/");
      external.addResource("h2-exception-sorter-ds.xml", "datasources-ds.xml");
      return external;

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
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void shouldInjectCorrectExceptionSorter() throws Throwable
   {
      try
      {
         assertNotNull(ds);

         Connection c = ds.getConnection();
         assertNotNull(c);

         Statement st = c.createStatement();
         st.execute("dssd");
      }
      catch (SQLException e)
      {
         // nothing we need an exception ;)
      }

      finally
      {
         assertThat(TestExceptionSorter.isConstructorInvoked(), is(true));

         assertThat(TestExceptionSorter.isMethodInvoked(), is(true));

         assertThat(TestExceptionSorter.getStringInjected(), hasItems("MyTest", "MyTest2"));

      }

   }


}
