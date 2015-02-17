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
package org.ironjacamar.common.metadata.common;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Utility functions tests
 *
 * @author <a href="vrastseluev@gmail.com">Vladimir Rastseluev</a>
 */
public class ExpressionTransformerTestCase
{
   /**
    * The main test for all expressions. Asserts that expression should be transformed
    * correctly into expected result
    *
    * @param expression to transform
    * @param expected result
    */
   private void expressionTransformTest(String expression, String expected)
   {
      assertEquals(expected, StringUtils.transformExpression(expression));
   }

   /**
    * Clear system properties set by tests
    */
   @After
   public void tearDown()
   {
      System.clearProperty("Prop3");
      System.clearProperty("Prop4");
   }

   /**
    * Simply test
    */
   @Test
   public void testSimple()
   {
      expressionTransformTest("${Prop1:Property2}", "Property2");
   }

   /**
    * Test with empty default value
    */
   @Test
   public void testEmptyDefault()
   {
      expressionTransformTest("${Prop1:}", "");
   }

   /**
    * Set the system property and  test
    */
   @Test
   public void testWithPropertySet()
   {
      System.setProperty("Prop3", "1");
      expressionTransformTest("${Prop3:Property1}", "1");
   }

   /**
    * Test an incorrect expression
    */
   @Test
   public void testWithIncorrectExpression()
   {
      expressionTransformTest("${Prop2:Property1", "${Prop2:Property1");
   }

   /**
    * Test a complex expression
    */
   @Test
   public void testComplexExpression()
   {
      expressionTransformTest("a${Prop1:Property1}_${Prop3:3}b", "aProperty1_3b");
   }

   /**
    * Test a complex expression with properties set
    */
   @Test
   public void testComplexExpressionWithProperties()
   {
      System.setProperty("Prop3", "4");
      System.setProperty("Prop4", "5");
      expressionTransformTest("a${Prop3:Property1}_${Prop4:3}b", "a4_5b");
   }

   /**
    * Test an expression without a default value
    */
   @Test
   public void testWithoutDefaultValue()
   {
      expressionTransformTest("${Prop1}", "");
   }

   /**
    * Test an expression without a default value. Property is set.
    */
   @Test
   public void testWithoutDefaultValuePropertySet()
   {
      System.setProperty("Prop3", "6");
      expressionTransformTest("${Prop3}", "6");
   }

   /**
    * Test a complex expression without a default value
    */
   @Test
   public void testComplexWithoutDefaultValue()
   {
      expressionTransformTest("1${Prop1}2${Prop3}4", "124");
   }

   /**
    * Test a complex expression without a default value. Property is set.
    */
   @Test
   public void testComplexWithoutDefaultValuePropertySet()
   {
      System.setProperty("Prop3", "4");
      System.setProperty("Prop4", "5");
      expressionTransformTest("a${Prop3}_${Prop4}b", "a4_5b");
   }

   /**
    * Test a complex expression with mixed properties.
    */
   @Test
   public void testComplexMixedProperties()
   {
      System.setProperty("Prop3", "8");
      expressionTransformTest("a${Prop3:6}_${Prop9}7:b", "a8_7:b");
   }

   /**
    * Test special symbols substituted by File constants
    */
   @Test
   public void testSpecialCases()
   {
      expressionTransformTest("a${:}_${/}b", "a" + File.pathSeparator + "_" + File.separator + "b");
   }

   /**
    * Test nested expressions
    */
   @Test
   public void testNestedExpressions()
   {
      expressionTransformTest("${Prop1:${Prop2:default}}", "default");
   }

   /**
    * Test nested expressions with some properties set
    */
   @Test
   public void testNestedExpressionsWithPropertiesSet1()
   {
      System.setProperty("Prop3", "3");
      expressionTransformTest("${Prop3:${Prop2:default}}", "3");
   }

   /**
    * Test nested expressions with some properties set
    */
   @Test
   public void testNestedExpressionsWithPropertiesSet2()
   {
      System.setProperty("Prop4", "4");
      expressionTransformTest("${Prop3:${Prop4:default}}", "4");
   }

   /**
    * Test nested complex expressions with some properties set
    */
   @Test
   public void testComplexNestedExpressionsWithPropertiesSet()
   {
      System.setProperty("Prop4", "4");
      System.setProperty("Prop3", "3");
      expressionTransformTest("${Prop1:${Prop4:default}${Prop3}}", "43");
   }

}
