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

import org.ironjacamar.common.api.metadata.ds.DataSource;
import org.ironjacamar.common.api.metadata.ds.DataSources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Expression Store tests
 * 
 * @author <a href="andrey.balyaev@gmail.com">Andrey Balyaev</a>
 *
 */
public class ExpressionStoreTestCase
{
   /**
    * Test stored values changed by environment variables
    *  
    */
   @Test
   public void testStoreEnvVariables()
   {
      System.setProperty("Property2", "Value2");
      System.setProperty("driver-class1", "org1");
      System.setProperty("driver-class2", "sqldb");
      System.setProperty("driver-class3", "jdbcDriver2");
      System.setProperty("reauth-plugin-prop2b", "Value2");
      System.setProperty("ConfigProp123", "Value123");
      System.setProperty("enabled", "false");
      System.setProperty("initial-pool-size", "3");
      
      try (InputStream is = ExpressionStoreTestCase.class.getClassLoader().
            getResourceAsStream("../../resources/test/ds/expression.xml");
            ByteArrayOutputStream os = new ByteArrayOutputStream())
      {
         assertNotNull(is);

         XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

         DsParser parser = new DsParser();
         DataSources ds =  parser.parse(xsr);
         assertNotNull(ds);
         
         XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
         
         assertNotNull(ds.getDataSource());
         
         DataSource datasource = ds.getDataSource().get(0);
         assertNotNull(datasource);
         parser.store(ds, writer);
         
         writer.flush();
         writer.close();
      
         Document document =  DocumentBuilderFactory.newInstance().newDocumentBuilder()
               .parse(new ByteArrayInputStream(os.toByteArray()));
         
         XPath xPath =  XPathFactory.newInstance().newXPath();
         
         //Test simple expression without default value 
         test(document, xPath, "/datasources/xa-datasource[1]/url-delimiter",
               "${url-delimiter}");
         
         //Test simple expression with an empty default value 
         test(document, xPath, "/datasources/xa-datasource[1]/url-property",
               "${url-property:}");

         //Test simple expression with default value, default value was not changed 
         test(document, xPath, "/datasources/datasource[1]/connection-property[@name='name1']",
               "${Property1:Property1}");

         //Test with no expression
         test(document, xPath, "/datasources/datasource[1]/security/reauth-plugin/config-property[@name='name1']",
               "ConfigProp123");

         //Test simple expression with default value, value is changed
         test(document, xPath, "/datasources/datasource[1]/connection-property[@name='name2']",
               "${Property2:Value2}");
  
            // complex expression, values are set
         test(document, xPath, "/datasources/datasource[1]/driver-class",
               "${driver-class1:org1}.${driver-class2:sqldb}.${driver-class3:jdbcDriver2}");

         // nested expression, value is set
         test(document, xPath, "/datasources/datasource[1]/security/reauth-plugin/config-property[@name='name2']",
               "${reauth-plugin-prop2a:${reauth-plugin-prop2b:Value2}}");

         //Test simple expression with boolean default value, value is changed
         test(document, xPath, "/datasources/xa-datasource[1]/@enabled",
               "${enabled:false}");
         
         //Test simple expression with integer default value, value is not changed
         test(document, xPath, "/datasources/xa-datasource[1]/xa-pool/min-pool-size",
               "${min-pool-size:1}");
         
         //Test simple expression with integer default value, value is changed
         test(document, xPath, "/datasources/xa-datasource[1]/xa-pool/initial-pool-size",
               "${initial-pool-size:3}");

      }
      catch (Exception e)
      {
         fail("Exception thrown: " + e.getMessage());
      }
      finally
      {
         System.clearProperty("Property2");
         System.clearProperty("driver-class1");
         System.clearProperty("driver-class2");
         System.clearProperty("driver-class3");
         System.clearProperty("reauth-plugin-prop2b");
         System.clearProperty("ConfigProp123");
         System.clearProperty("enabled");
         System.clearProperty("initial-pool-size");
      }      
   }

   
   /**
   *
   * Test the stored expression has the expected value
   * @param doc Xml document
   * @param xpath XPath object to find the stored value
   * @param xpathExpr the xpath expression to search the stored value
   * @param expected the expected stored value
   */
   private void test(Document doc, XPath xpath, String xpathExpr, String expected) throws XPathExpressionException
   {
      assertEquals(expected, xpath.compile(xpathExpr).evaluate(doc));
   }
}
