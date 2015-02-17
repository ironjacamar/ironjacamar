/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.metadata.ds;

import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.ds.DataSource;
import org.ironjacamar.common.api.metadata.ds.DataSources;
import org.ironjacamar.common.api.metadata.ds.DsPool;
import org.ironjacamar.common.api.metadata.ds.XaDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Expression tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ExpressionTestCase
{
   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is = ExpressionTestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/expression.xml");
      assertNotNull(is);

      StringBuilder sb = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      String line = br.readLine();
      while (line != null)
      {
         String data = line.trim();
         sb.append(data);
         if (!data.equals("") && !data.endsWith(">"))
            sb.append(" ");
         line = br.readLine();
      }

      is.close();

      is = ExpressionTestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/expression.xml");
      assertNotNull(is);
      
      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      DataSources ds = parser.parse(xsr);
      assertNotNull(ds);

      is.close();

      StringWriter sw = new StringWriter();
      XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
      xsw.setDefaultNamespace("");

      xsw.writeStartDocument("UTF-8", "1.0");
      parser.store(ds, xsw);
      xsw.writeEndDocument();

      xsw.flush();
      xsw.close();

      assertEquals(sb.toString(), sw.toString());
   }

   /**
    * Tests if correct expression values set in some cases
    */
   @Test
   public void testExpressionParsing()
   {
      System.setProperty("Property1", "Value1");
      System.setProperty("connection-type", "jdbc2");
      System.setProperty("DBCloseDelay", "100");
      System.setProperty("url-delimiter", ":");
      System.setProperty("user-name", "DBUser");
      System.setProperty("password2", "sa2");
      System.setProperty("min-pool-size", "3");
      System.setProperty("background-validation", "false");

      try (InputStream is = DataSources10TestCase.class.getClassLoader().
            getResourceAsStream("../../resources/test/ds/expression.xml"))
      {
         assertNotNull(is);

         XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

         DataSources ds = new DsParser().parse(xsr);
         assertNotNull(ds);

         List<DataSource> listDs = ds.getDataSource();
         assertEquals(1, listDs.size());

         DataSource d = listDs.get(0);
         Map<String, String> properties = d.getConnectionProperties();
         assertEquals(2, properties.size());

         assertEquals("Value1", properties.get("name1"));
         assertEquals("Property2", properties.get("name2"));

         // A complex expression with set properties
         assertEquals("jdbc2:h2:mem:test;DB_CLOSE_DELAY=100", d.getConnectionUrl());

         // A complex expression without properties
         assertEquals("org.hsqldb.jdbcDriver", d.getDriverClass());

         List<XaDataSource> listXADs = ds.getXaDataSource();
         assertEquals(1, listXADs.size());
         XaDataSource dxa = listXADs.get(0);

         // An expression without a default value
         assertEquals("", dxa.getUrlProperty());

         // An expression without a default value. Property is set
         assertEquals(":", dxa.getUrlDelimiter());

         // Test a complex expression without a default value
         DsPool pool = d.getPool();
         assertEquals("AllConnections", pool.getFlushStrategy().toString());

         // Test with empty default value
         assertEquals("", dxa.getUrlProperty());

         // Test an incorrect expression
         assertEquals("${security-domain:HsqlDbRealm", dxa.getSecurity().getSecurityDomain());

         Extension ext = dxa.getSecurity().getReauthPlugin();
         // Test nested expressions
         assertEquals("someClass1", ext.getClassName());

         // Test nested expressions with some properties set
         assertEquals("DBUser", d.getSecurity().getUserName());

         // Test nested expressions with some properties set
         assertEquals("sa2", d.getSecurity().getPassword());

         // integer property with property set
         assertSame(3,  pool.getMinPoolSize());

         // boolean property with property set
         assertSame(false,  d.getValidation().isBackgroundValidation());

      }
      catch (Exception e)
      {
         fail("Exception thrown: " + e.getMessage());
      }
      finally
      {
         System.clearProperty("Property1");
         System.clearProperty("connection-type");
         System.clearProperty("DBCloseDelay");
         System.clearProperty("url-delimiter");
         System.clearProperty("user-name");
         System.clearProperty("password2");
         System.clearProperty("min-pool-size");
         System.clearProperty("background-validation");
      }
   }
}
