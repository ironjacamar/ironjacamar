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

import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.Activations;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
   @Ignore
   public void testWrite() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ExpressionTestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/expression.xml");
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
         getResourceAsStream("../../resources/test/resourceadapter/expression.xml");
      assertNotNull(is);
      
      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activations a = parser.parse(xsr);
      assertNotNull(a);

      is.close();

      StringWriter sw = new StringWriter();
      XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
      xsw.setDefaultNamespace("");

      xsw.writeStartDocument("UTF-8", "1.0");
      parser.store(a, xsw);
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
      System.setProperty("bean-validation-group1", "BeanX");
      System.setProperty("bean-validation-group2", "ValidationX");
      System.setProperty("bean-validation-group3", "GroupX");
      System.setProperty("bean-validation-group1-4", "bean-validation-group4");
      System.setProperty("bean-validation-group1-5", "Bean5");
      System.setProperty("bean-validation-group2-5", "Validation5");
      System.setProperty("bean-validation-group3-5", "Group5");
      System.setProperty("pool-name", "Pool");
      System.setProperty("pool-number", "1");
      System.setProperty("Property6", "Value6");
      System.setProperty("Property7X", "Value7");
      System.setProperty("background-validation-millis", "6000");
      System.setProperty("sharable", "true");
      System.setProperty("PropertyPrefix", "Value5");
      System.setProperty("PropertyPostfix", "Value6");
      System.setProperty("Property7-2", "NestedValue7");
      System.setProperty("Property8-2", "NestedValue8");
    
      try (InputStream is = ExpressionTestCase.class.getClassLoader().
            getResourceAsStream("../../resources/test/resourceadapter/expression.xml"))
      {
         assertNotNull(is);
         XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

         ResourceAdapterParser parser = new ResourceAdapterParser();

         Activations activations = parser.parse(xsr);
         assertNotNull(activations);

         Activation a = activations.getActivations().get(0);
         
         // simple expression  
         Map<String, String> configProp = a.getConfigProperties();
         assertEquals("Value1", configProp.get("Property1"));
         
         // A complex expression with set properties
         assertEquals("BeanX-ValidationX-GroupX", a.getBeanValidationGroups().get(0));
         
         // A complex expression without properties
         assertEquals("bean-validation-group2", a.getBeanValidationGroups().get(1));
         
         // An expression without a default value
         assertEquals("Bean5Validation5Group5", a.getBeanValidationGroups().get(4));
         
         // An expression without a default value. Property is set
         assertEquals("bean-validation-group4", a.getBeanValidationGroups().get(3));
         
         // Test a complex expression without a default value
         ConnectionDefinition conndef = a.getConnectionDefinitions().get(0);
         
         assertEquals("Pool1-X", conndef.getPoolName());
 
         // Test with empty default value
         assertEquals("", a.getBeanValidationGroups().get(2));

         // Test an incorrect expression
         assertEquals("${security-domain:domain", conndef.getSecurity().getSecurityDomain());

         // Test nested expressions without set properties
         assertEquals("Property5", conndef.getRecovery().getRecoverPlugin().getConfigPropertiesMap().get("Property5"));
 
         // Test nested expressions with some properties set
         assertEquals("Value6", conndef.getRecovery().getRecoverPlugin().getConfigPropertiesMap().get("Property6"));

         // Test nested expressions with some properties set
         assertEquals("Value7", conndef.getRecovery().getRecoverPlugin().getConfigPropertiesMap().get("Property7"));

         // integer property with property set
         assertEquals(6000L,  conndef.getValidation().getBackgroundValidationMillis().longValue());

         // boolean property with property set
         assertEquals(true,  conndef.isSharable());
         
         // a simple expression with prefix, property is set
         assertEquals("SomePrefix-Value5", conndef.getConfigProperties().get("Property5"));

         // a simple expression with postfix, property is set
         assertEquals("Value6-SomePostfix", conndef.getConfigProperties().get("Property6"));
         
         // a nested expression with prefix, the second property is set
         assertEquals("SomePrefix-NestedValue7", conndef.getConfigProperties().get("Property7"));

         // a nested expression with postfix, the second property is set
         assertEquals("NestedValue8-SomePostfix", conndef.getConfigProperties().get("Property8"));
         
         
      }
      catch (Exception e)
      {
         fail("Exception thrown: " + e.getMessage());
      }
      finally
      {
         System.clearProperty("Property1");
         System.clearProperty("bean-validation-group1");
         System.clearProperty("bean-validation-group2");
         System.clearProperty("bean-validation-group3");
         System.clearProperty("bean-validation-group1-4");
         System.clearProperty("bean-validation-group1-5");
         System.clearProperty("bean-validation-group2-5");
         System.clearProperty("bean-validation-group3-5");
         System.clearProperty("pool-name");
         System.clearProperty("pool-number");
         System.clearProperty("Property6");
         System.clearProperty("Property7X");
         System.clearProperty("background-validation-millis");
         System.clearProperty("sharable");
         System.clearProperty("PropertyPrefix");
         System.clearProperty("PropertyPostfix");
         System.clearProperty("Property7-2");
         System.clearProperty("Property8-2");
      }      
   }   
}
