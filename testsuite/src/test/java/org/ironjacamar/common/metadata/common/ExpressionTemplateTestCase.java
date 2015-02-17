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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for expression templates
 *
 * @author <a href="vrastseluev@gmail.com">Vladimir Rastseluev</a>
 *
 */
public class ExpressionTemplateTestCase
{
   /**
    * clean after test
    */
   @After
   public void clean()
   {
      System.clearProperty("simple");
      System.clearProperty("nested");
   }
   /**
    * A basic test for getExpressionKey() method
    * @param input string
    * @param expected result
    */
   private void testGetExpressionKey(String input, String expected)
   {
      assertEquals(expected, StringUtils.getExpressionKey(input));
   }

   /**
    * test for existing expression key
    */
   @Test
   public void testExpressionKeyExists()
   {
      testGetExpressionKey(StringUtils.createKey(100), StringUtils.createKey(100));
   }

   /**
    * test for nested expression key
    */
   @Test
   public void testExpressionKeyInString()
   {
      testGetExpressionKey("a" + StringUtils.createKey(12) + "b", StringUtils.createKey(12));
   }

   /**
    * test for not existing expression key
    */
   @Test
   public void testExpressionKeyDoesNotExist()
   {
      testGetExpressionKey("some string", null);
   }

   /**
    * test for wrong expression key
    */
   @Test
   public void testWrongExpressionKey()
   {
      testGetExpressionKey(">?&%<some string<%&?>", null);
   }

   /**
    * A basic test for Expression.getValue() method
    * @param e Expression object
    * @param expected value
    */
   private void expressionValueTest(Expression e, String expected)
   {
      assertEquals(expected, e.getValue());
   }

   /**
    * Checks the value of the expression when it isn't resolved
    */
   @Test
   public void testGetValueUnresolved()
   {
      expressionValueTest(new Expression("a", "b", null) , "b");
   }

   /**
    * Checks the value of the expression when it is resolved to empty String
    */
   @Test
   public void testGetValueResolvedEmpty()
   {
      expressionValueTest(new Expression("a", "b", "  ") , "b");
   }

   /**
    * Checks the value of the expression when it isn't resolved and have no default value
    */
   @Test
   public void testGetValueUnresolvedNoDefault()
   {
      expressionValueTest(new Expression("a", null, null) , "");
   }

   /**
    * Checks the value of the expression when it is resolved to empty String
    * and a default value is empty String too
    */
   @Test
   public void testGetValueResolvedAndDefaultEmpty()
   {
      expressionValueTest(new Expression("a", "", "  ") , "");
   }

   /**
    * Checks the value of the expression when it is resolved to non-empty String
    */
   @Test
   public void testGetValueResolved1()
   {
      expressionValueTest(new Expression("a", "b", "c") , "c");
   }

   /**
    * Checks the value of the expression when it is resolved to non-empty String
    * and a default value is empty null
    */
   @Test
   public void testGetValueResolved2()
   {
      expressionValueTest(new Expression("a", null, "c") , "c");
   }

   /**
    * A basic test for Expression.getValue() method
    * @param e Expression object
    * @param expected value
    */
   private void expressionSubstitutionTest(Expression e, String expected)
   {
      assertEquals(expected, e.toSubstitution());
   }

   /**
    * Checks the substitution of the expression when it isn't resolved
    */
   @Test
   public void testToSubstitutionUnresolved()
   {
      expressionSubstitutionTest(new Expression("a", "b", null) , "${a:b}");
   }

   /**
    * Checks the substitution of the expression when it is resolved to empty String
    */
   @Test
   public void testToSubstitutionResolvedEmpty()
   {
      expressionSubstitutionTest(new Expression("a", "b", "  ") , "${a:  }");
   }

   /**
    * Checks the substitution of the expression when it isn't resolved and have no default value
    */
   @Test
   public void testToSubstitutionUnresolvedNoDefault()
   {
      expressionSubstitutionTest(new Expression("a", null, null) , "${a}");
   }

   /**
    * Checks the substitution of the expression when it is unresolved
    * and a default value is empty String too
    */
   @Test
   public void testToSubstitutionResolvedAndDefaultEmpty()
   {
      expressionSubstitutionTest(new Expression("a", "", null) , "${a:}");
   }

   /**
    * Checks the substitution of the expression when it is resolved to non-empty String
    */
   @Test
   public void testToSubstitutionResolved1()
   {
      expressionSubstitutionTest(new Expression("a", "b", "c") , "${a:c}");
   }

   /**
    * Checks the substitution of the expression when it is resolved to non-empty String
    * and a default value is empty null
    */
   @Test
   public void testToSubstitutionResolved2()
   {
      expressionSubstitutionTest(new Expression("a", null, "c") , "${a}");
   }

   /**
    * Checks ExpressionTemplate for a text with wrong expression
    */
   @Test
   public void testExpressionTemplateWrongExpression()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple text");
      assertEquals("${simple text", t.getTemplate());
      assertEquals(0, t.getEntities().size());
   }

   /**
    * Checks ExpressionTemplate for a text without expressions
    */
   @Test
   public void testExpressionTemplateWithoutExpressions()
   {
      ExpressionTemplate t = new ExpressionTemplate("simple text");
      assertEquals("simple text", t.getTemplate());
      assertEquals(0, t.getEntities().size());
   }

   /**
    * Checks ExpressionTemplate for a text with simple expression
    */
   @Test
   public void testExpressionTemplateWithSimpleExpression()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple}");
      assertEquals(StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple}", t.getSubstitution());
      assertEquals("", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with simple expression with default value
    */
   @Test
   public void testExpressionTemplateWithSimpleExpressionWithDefault()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple:default}");
      assertEquals(StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("default", t.getEntities().get(StringUtils.createKey(0)).getDefaultValue());
      assertEquals("${simple:default}", t.getSubstitution());
      assertEquals("default", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with simple expression with resolved value
    */
   @Test
   public void testExpressionTemplateWithSimpleExpressionWithResolved()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple}");
      assertEquals(StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("value", t.getEntities().get(StringUtils.createKey(0)).getResolvedValue());
      assertEquals("${simple}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with simple expression with default and
    * resolved values set
    */
   @Test
   public void testExpressionTemplateWithSimpleExpressionWithBoth()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:default}");
      assertEquals(StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("default", t.getEntities().get(StringUtils.createKey(0)).getDefaultValue());
      assertEquals("${simple:value}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with nested expression
    */
   @Test
   public void testExpressionTemplateWithNestedExpression()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested}}", t.getSubstitution());
      assertEquals("", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with resolved nested expression
    */
   @Test
   public void testExpressionTemplateWithResolvedNestedExpression()
   {
      System.setProperty("nested", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested}}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with nested expression
    * with default value
    */
   @Test
   public void testExpressionTemplateWithNestedExpressionWithDefault()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested:default}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested:default}}", t.getSubstitution());
      assertEquals("default", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with resolved nested expression
    * with default value
    */
   @Test
   public void testExpressionTemplateWithResolvedNestedExpressionWithDefault()
   {
      System.setProperty("nested", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested:default}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested:value}}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with resolved outer and a nested expression
    */
   @Test
   public void testExpressionTemplateWithResolvedOuterAndNestedExpression()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested}}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with resolved outer and a nested expression
    * with default value
    */
   @Test
   public void testExpressionTemplateWithResolvedOuterAndNestedExpressionWithDefault()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested:default}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested:value}}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with resolved both outer and a nested expression
    */
   @Test
   public void testExpressionTemplateWithResolvedBothOuterAndNestedExpression()
   {
      System.setProperty("simple", "value");
      System.setProperty("nested", "value1");
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested}}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Checks ExpressionTemplate for a text with resolved both outer and a nested expression
    * with default value
    */
   @Test
   public void testExpressionTemplateWithResolvedBothOuterAndNestedExpressionWithDefault()
   {
      System.setProperty("simple", "value");
      System.setProperty("nested", "value1");
      ExpressionTemplate t = new ExpressionTemplate("${simple:${nested:default}}");
      assertEquals(StringUtils.createKey(1), t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:${nested:value}}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Test a complex template
    */
   @Test
   public void testComplexTemplate()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${simple}:${nested}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple}:${nested}b", t.getSubstitution());
      assertEquals("a:b", t.getValue());
   }

   /**
    * Test a complex template with one default value set
    */
   @Test
   public void testComplexTemplateWithDefault1()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${simple:c}:${nested}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple:c}:${nested}b", t.getSubstitution());
      assertEquals("ac:b", t.getValue());
   }

   /**
    * Test a complex template with one default value set
    */
   @Test
   public void testComplexTemplateWithDefault2()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${simple}:${nested:c}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple}:${nested:c}b", t.getSubstitution());
      assertEquals("a:cb", t.getValue());
   }

   /**
    * Test a complex template with both default values set
    */
   @Test
   public void testComplexTemplateWithBothDefault()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${simple:c}:${nested:d}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple:c}:${nested:d}b", t.getSubstitution());
      assertEquals("ac:db", t.getValue());
   }

   /**
    * Test a complex template resolved
    */
   @Test
   public void testComplexTemplateResolved()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("a${simple}:${nested}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple}:${nested}b", t.getSubstitution());
      assertEquals("avalue:b", t.getValue());
   }

   /**
    * Test a complex resolved template with one default value set
    */
   @Test
   public void testComplexTemplateResolvedWithDefault1()
   {
      System.setProperty("nested", "value");
      ExpressionTemplate t = new ExpressionTemplate("a${simple:c}:${nested}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple:c}:${nested}b", t.getSubstitution());
      assertEquals("ac:valueb", t.getValue());
   }

   /**
    * Test a complex resolved template with one default value set
    */
   @Test
   public void testComplexTemplateResolvedWithDefault2()
   {
      System.setProperty("nested", "value");
      ExpressionTemplate t = new ExpressionTemplate("a${simple}:${nested:c}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple}:${nested:value}b", t.getSubstitution());
      assertEquals("a:valueb", t.getValue());
   }

   /**
    * Test a complex template with both default values set
    */
   @Test
   public void testComplexTemplateResolvedWithBothDefault()
   {
      System.setProperty("nested", "value1");
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("a${simple:c}:${nested:d}b");
      assertEquals("a" + StringUtils.createKey(1) + ":" + StringUtils.createKey(0) + "b",
            t.getTemplate());
      assertEquals(2, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(1)).getKey());
      assertEquals("nested", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple:value}:${nested:value1}b", t.getSubstitution());
      assertEquals("avalue:value1b", t.getValue());
   }

   /**
    * Test a complex template with prefix
    */
   @Test
   public void testComplexTemplateWithPrefix()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${simple}");
      assertEquals("a" + StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple}", t.getSubstitution());
      assertEquals("a", t.getValue());
   }

   /**
    * Test a complex template with prefix resolved
    */
   @Test
   public void testComplexTemplateWithPrefixResolved()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("a${simple}");
      assertEquals("a" + StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple}", t.getSubstitution());
      assertEquals("avalue", t.getValue());
   }

   /**
    * Test a complex template with a default value and prefix resolved
    */
   @Test
   public void testComplexTemplateWithDefaultAndPrefixResolved()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("a${simple:c}");
      assertEquals("a" + StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple:value}", t.getSubstitution());
      assertEquals("avalue", t.getValue());
   }

   /**
    * Test a complex template with a default value and prefix
    */
   @Test
   public void testComplexTemplateWithDefaultAndPrefix()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${simple:c}");
      assertEquals("a" + StringUtils.createKey(0), t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${simple:c}", t.getSubstitution());
      assertEquals("ac", t.getValue());
   }

   /**
    * Test a complex template with postfix
    */
   @Test
   public void testComplexTemplateWithPostfix()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple}b");
      assertEquals(StringUtils.createKey(0) + "b", t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple}b", t.getSubstitution());
      assertEquals("b", t.getValue());
   }

   /**
    * Test a complex template with postfix resolved
    */
   @Test
   public void testComplexTemplateWithPostfixResolved()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple}b");
      assertEquals(StringUtils.createKey(0) + "b", t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple}b", t.getSubstitution());
      assertEquals("valueb", t.getValue());
   }

   /**
    * Test a complex template with a default value and postfix resolved
    */
   @Test
   public void testComplexTemplateWithDefaultAndPostfixResolved()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:c}b");
      assertEquals(StringUtils.createKey(0) + "b", t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:value}b", t.getSubstitution());
      assertEquals("valueb", t.getValue());
   }

   /**
    * Test a complex template with a default value and postfix
    */
   @Test
   public void testComplexTemplateWithDefaultAndPostfix()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple:c}b");
      assertEquals(StringUtils.createKey(0) + "b", t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("simple", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("${simple:c}b", t.getSubstitution());
      assertEquals("cb", t.getValue());
   }

   /**
    * Test a path separator
    */
   @Test
   public void testTemplateSpecialCase1()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${:}b");
      assertEquals("a" + StringUtils.createKey(0) + "b", t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals(":", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${:}b", t.getSubstitution());
      assertEquals("a" + File.pathSeparator + "b", t.getValue());
   }

   /**
    * Test a file separator
    */
   @Test
   public void testTemplateSpecialCase2()
   {
      ExpressionTemplate t = new ExpressionTemplate("a${/}b");
      assertEquals("a" + StringUtils.createKey(0) + "b", t.getTemplate());
      assertEquals(1, t.getEntities().size());
      assertEquals("/", t.getEntities().get(StringUtils.createKey(0)).getKey());
      assertEquals("a${/}b", t.getSubstitution());
      assertEquals("a" + File.separator + "b", t.getValue());
   }

   /**
    * Test a complex template with special symbols
    */
   @Test
   public void testComplexTemplateWithSpecialCases()
   {
      ExpressionTemplate t = new ExpressionTemplate("http${:}${/}${/}some{simple:property}");
      assertEquals(3, t.getEntities().size());
      assertEquals("http${:}${/}${/}some{simple:property}", t.getSubstitution());
      assertEquals("http" + File.pathSeparator + File.separator + File.separator + "some{simple:property}",
            t.getValue());
   }

   /**
    * Test a complex template with special symbols
    */
   @Test
   public void testComplexTemplateWithSpecialCases1()
   {
      ExpressionTemplate t = new ExpressionTemplate("http${:}${/}${/}some${simple:property}");
      assertEquals(4, t.getEntities().size());
      assertEquals("http${:}${/}${/}some${simple:property}", t.getSubstitution());
      assertEquals("http" + File.pathSeparator + File.separator + File.separator + "someproperty",
            t.getValue());
   }

   /**
    * Test a complex template with special symbols
    */
   @Test
   public void testComplexTemplateWithSpecialCases2()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("http${:}${/}${/}some${simple:property}");
      assertEquals(4, t.getEntities().size());
      assertEquals("http${:}${/}${/}some${simple:value}", t.getSubstitution());
      assertEquals("http" + File.pathSeparator + File.separator + File.separator + "somevalue",
            t.getValue());
   }

   /**
    * Test a nested complex template
    */
   @Test
   public void testNestedComplexTemplate()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple:a_${nested}_${expr}_}");
      assertEquals(3, t.getEntities().size());
      assertEquals("${simple:a_${nested}_${expr}_}", t.getSubstitution());
      assertEquals("a___", t.getValue());
   }

   /**
    * Test a nested complex template with default values
    */
   @Test
   public void testNestedComplexTemplateWithDefault()
   {
      ExpressionTemplate t = new ExpressionTemplate("${simple:a_${nested:nested}_${expr:expression}_}");
      assertEquals(3, t.getEntities().size());
      assertEquals("${simple:a_${nested:nested}_${expr:expression}_}", t.getSubstitution());
      assertEquals("a_nested_expression_", t.getValue());
   }

   /**
    * Test a nested complex template with default and resolved values
    */
   @Test
   public void testNestedComplexTemplateWithDefaultAndResolved()
   {
      System.setProperty("nested", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:a_${nested:nested}_${expr:expression}_}");
      assertEquals(3, t.getEntities().size());
      assertEquals("${simple:a_${nested:value}_${expr:expression}_}", t.getSubstitution());
      assertEquals("a_value_expression_", t.getValue());
   }

   /**
    * Test a nested complex template the resolved outer part
    */
   @Test
   public void testNestedComplexTemplateWithOuterResolver()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("${simple:a_${nested:nested}_${expr:expression}_}");
      assertEquals(3, t.getEntities().size());
      assertEquals("${simple:value}", t.getSubstitution());
      assertEquals("value", t.getValue());
   }

   /**
    * Test a complex template with a nested expression
    */
   @Test
   public void testComplexTemplateWithNested()
   {
      ExpressionTemplate t = new ExpressionTemplate("a_${expr}_${simple:${nested}}_");
      assertEquals(3, t.getEntities().size());
      assertEquals("a_${expr}_${simple:${nested}}_", t.getSubstitution());
      assertEquals("a___", t.getValue());
   }

   /**
    * Test a complex template with a nested expression
    */
   @Test
   public void testComplexTemplateWithNested1()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("a_${expr}_${simple:${nested}}_");
      assertEquals(3, t.getEntities().size());
      assertEquals("a_${expr}_${simple:${nested}}_", t.getSubstitution());
      assertEquals("a__value_", t.getValue());
   }

   /**
    * Test a complex template with default values and a nested expression
    */
   @Test
   public void testComplexTemplateWithDefaultValuesAndNestedExpression()
   {
      ExpressionTemplate t = new ExpressionTemplate("a_${expr:expression}_${simple:${nested:value}}_");
      assertEquals(3, t.getEntities().size());
      assertEquals("a_${expr:expression}_${simple:${nested:value}}_", t.getSubstitution());
      assertEquals("a_expression_value_", t.getValue());
   }

   /**
    * Test a complex template with default and resolved values and a nested expression
    */
   @Test
   public void testComplexTemplateWithDefaultNestedExpressionResolved()
   {
      System.setProperty("nested", "value");
      ExpressionTemplate t = new ExpressionTemplate("a_${expr:expression}_${simple:${nested:nested}}_");
      assertEquals(3, t.getEntities().size());
      assertEquals("a_${expr:expression}_${simple:${nested:value}}_", t.getSubstitution());
      assertEquals("a_expression_value_", t.getValue());
   }

   /**
    * Test a complex template with default and resolved values and a nested expression
    */
   @Test
   public void testComplexTemplateWithDefaultAndResolvedValuesAndNestedExpression()
   {
      System.setProperty("simple", "value");
      ExpressionTemplate t = new ExpressionTemplate("a_${expr:expression}_${simple:${nested:nested}}_");
      assertEquals(3, t.getEntities().size());
      assertEquals("a_${expr:expression}_${simple:${nested:value}}_", t.getSubstitution());
      assertEquals("a_expression_value_", t.getValue());
   }

   /**
    * Test if the template is complex
    */
   private boolean isComplexTemplate(String expression)
   {
      return new ExpressionTemplate(expression).isComplex();
   }

   /**
    * Test isComplex() for a non-expression
    */
   @Test
   public void testIsComplexNonExpression()
   {
      assertFalse(isComplexTemplate("something"));
   }

   /**
    * Test isComplex() for a simple expression
    */
   @Test
   public void testIsComplexSimpleExpression()
   {
      assertFalse(isComplexTemplate("${value:default}"));
   }

   /**
    * Test isComplex() for a wrong expression
    */
   @Test
   public void testIsComplexWrongExpression()
   {
      assertFalse(isComplexTemplate("${t${a:${cb}_${c:d}}"));
   }

   /**
    * Test isComplex() for a nested expression
    */
   @Test
   public void testIsComplexNestedExpression()
   {
      assertFalse(isComplexTemplate("${a:${b:c}}"));
   }

   /**
    * Test isComplex() for a complex expression
    */
   @Test
   public void testIsComplexExpression()
   {
      assertTrue(isComplexTemplate("${a:b}_${c}"));
   }

   /**
    * Test isComplex() for a complex nested expression
    */
   @Test
   public void testIsComplexExpressionWhenNested1()
   {
      assertTrue(isComplexTemplate("${a:b}_${c:${d}}"));
   }

   /**
    * Test isComplex() for a complex nested expression
    */
   @Test
   public void testIsComplexExpressionWhenNested2()
   {
      assertTrue(isComplexTemplate("${a:${d}${b}}"));
   }
}
