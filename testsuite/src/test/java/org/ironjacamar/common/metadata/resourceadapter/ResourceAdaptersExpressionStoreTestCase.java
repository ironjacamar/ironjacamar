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
package org.ironjacamar.common.metadata.resourceadapter;

import org.ironjacamar.common.api.metadata.resourceadapter.Activations;

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
public class ResourceAdaptersExpressionStoreTestCase
{
   /**
    * Test stored values changed by environment variables
    *  
    */
   @Test
   public void testStoreEnvVariables()
   {
      System.setProperty("Property4", "Value4");
      System.setProperty("bean-validation-group1", "bean2");
      System.setProperty("bean-validation-group2", "validation2");
      System.setProperty("bean-validation-group3", "group2");
      System.setProperty("Property5X", "nestedvalue5X");
      System.setProperty("initial-pool-size", "3");
      
      try (InputStream is = ResourceAdaptersExpressionStoreTestCase.class.getClassLoader().
            getResourceAsStream("../../resources/test/resourceadapter/expression.xml");
            ByteArrayOutputStream os = new ByteArrayOutputStream())
      {
         assertNotNull(is);

         XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

         ResourceAdapterParser parser = new ResourceAdapterParser();
         Activations ds =  parser.parse(xsr);
         assertNotNull(ds);
         
         XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
         
         parser.store(ds, writer);
         
         writer.flush();
         writer.close();
      
         Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
               .parse(new ByteArrayInputStream(os.toByteArray()));
         
         XPath xPath =  XPathFactory.newInstance().newXPath();
         
         assertNotNull(document);
         assertNotNull(xPath);
         
         String xpathRA = "/resource-adapters/resource-adapter/";
         String xpcon = xpathRA + "connection-definitions/connection-definition/";
         //Test simple expression without default value 
         test(document, xPath, xpathRA + "admin-objects/admin-object/config-property[2]",
               "${Property8}");

         //Test simple expression with default value, default value was not changed 
         test(document, xPath, xpcon + "config-property[1]",
               "${Property3:Property3}");

         //Test with no expression
         test(document, xPath, xpcon + "security/security-domain",
               "${security-domain:domain");

         //Test simple expression with default value, value is changed
         test(document, xPath, xpcon + "config-property[2]",
               "${Property4:Value4}");
  
            // complex expression, values are set
         test(document, xPath, xpathRA + "bean-validation-groups/bean-validation-group[1]",
               "${bean-validation-group1:bean2}-${bean-validation-group2:validation2}-"
                     + "${bean-validation-group3:group2}");

         // nested expression, value is set
         test(document, xPath, xpcon + "recovery/recover-plugin/config-property",
               "${Property5:${Property5X:nestedvalue5X}}");

         //Test simple expression with boolean default value, value is not changed
         test(document, xPath, xpcon + "@enabled",
               "${enabled:true}");

         //Test simple expression with integer default value, value is not changed
         test(document, xPath, xpcon + "xa-pool/min-pool-size",
               "${min-pool-size:0}");
         
         //Test simple expression with integer default value, value is changed
         test(document, xPath, xpcon + "xa-pool/initial-pool-size",
               "${initial-pool-size:3}");
      }
      catch (Exception e)
      {
         fail("Exception thrown: " + e.getMessage());
      }
      finally
      {
         System.clearProperty("Property4");
         System.clearProperty("bean-validation-group1");
         System.clearProperty("bean-validation-group2");
         System.clearProperty("bean-validation-group3");
         System.clearProperty("Property5X");
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
