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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for StringUtils.restoreExpression() methods
 *
 * @author <a href="vrastseluev@gmail.com">Vladimir Rastseluev</a>
 *
 */
public class ExpressionRestoreTestCase
{
   /**
    * expressions map
    */
   Map<String, String> map;

   /**
    * constant
    */
   private String key = "key";

   /**
    * Overwritten version of the test
    * @param expected result
    * @param key of the map
    * @param v restored value
    */
   public void test(String expected, String key, String v)
   {
      test(expected, key, null, v);
   }

   /**
    *
    * Test the restoreExpression method with the expression map
    * @param expected result
    * @param key of the map
    * @param subkey of the map
    * @param v resolved value
    */
   public void test(String expected, String key, String subkey, String v)
   {
      assertEquals(expected, StringUtils.restoreExpression(map, key, subkey, v));
   }

   /**
    * Basic initialization
    */
   @Before
   public void setUp()
   {
      map = new HashMap<>();
   }

   /**
    * Test complex key
    */
   @Test
   public void keyTest()
   {
      map.put(key + "|subkey", "${value}");
      test("${value}", key, "subkey", null);
   }

   /**
    * Test complex key with expression
    */
   @Test
   public void keyWithExpressionTest()
   {
      map.put(key + "|subkey", "${value}");
      test("${value}", key, "${subkey}", null);
   }

   /**
    * Test complex key with expression and a default value
    */
   @Test
   public void keyWithExpressionAndValueTest()
   {
      map.put(key + "|subkey", "${value}");
      test("${value}", key, "${subkey:subvalue}", null);
   }

   /**
    * Test complex key with expression and a default value
    */
   @Test
   public void keyWithIncorrectExpressionTest()
   {
      map.put(key + "|${subkey", "${value}");
      test("${value}", key, "${subkey", null);
   }

   /**
    * Test special case /
    */
   @Test
   public void specialTest1()
   {
      map.put(key, "${/}");
      test("${/}", key, "\\");
   }

   /**
    * Test special case :
    */
   @Test
   public void specialTest2()
   {
      map.put(key, "${:}");
      test("${:}", key, ";");
   }

   /**
    * Test with no expression
    */
   @Test
   public void simpleTest()
   {
      test("value", null, "value");
   }

   /**
    * Test with simple expression
    */
   @Test
   public void simpleExpressionTest()
   {
      map.put(key, "${value}");
      test("${value}", key, "value");
   }

   /**
    * Test with simple value instead of expression
    */
   @Test
   public void simpleValueTest()
   {
      map.put(key, "value");
      test("value1", key, "value1");
   }

   /**
    * Test simple expression with default value
    */
   @Test
   public void simpleExpressionWithDefaultValueTest()
   {
      map.put(key, "${value:default}");
      test("${value:default}", key, null);
   }

   /**
    * Test simple resolved expression with default value
    */
   @Test
   public void simpleResolvedExpressionWithDefaultValueTest()
   {
      map.put(key, "${value:default}");
      test("${value:resolved}", key, "resolved");
   }

   /**
    * Test nested expression
    */
   @Test
   public void nestedExpressionTest()
   {
      map.put(key, "${nested:${nested1}}");
      test("${nested:${nested1}}", key, null);
   }

   /**
    * Test double nested expression
    */
   @Test
   public void nestedTwiceExpressionTest()
   {
      map.put(key, "${nested:${nested1:${nested2}}}");
      test("${nested:${nested1:${nested2}}}", key, null);
   }

   /**
    * Test nested expression with default value
    */
   @Test
   public void nestedExpressionWithDefaultValueTest()
   {
      map.put(key, "${nested:${nested1:default}}");
      test("${nested:${nested1:default}}", key, null);
   }

   /**
    * Test nested resolved expression
    */
   @Test
   public void nestedExpressionResolvedTest()
   {
      map.put(key, "${nested:${nested1}}");
      test("${nested:${nested1}}", key, "value");
   }

   /**
    * Test double resolved nested expression
    */
   @Test
   public void nestedTwiceExpressionResolvedTest()
   {
      map.put(key, "${nested:${nested1:${nested2}}}");
      test("${nested:${nested1:${nested2}}}", key, "value");
   }

   /**
    * Test nested resolved expression with default value
    */
   @Test
   public void nestedExpressionResolvedWithDefaultValueTest1()
   {
      map.put(key, "${nested:${nested1:default}}");
      test("${nested:${nested1:value}}", key, "value");
   }

   /**
    * Test complex expression
    */
   @Test
   public void complexExpressionTest()
   {
      map.put(key, "${simple}:${nested}");
      test("${simple}:${nested}", key, null);
   }

   /**
    * Test complex expression with default values
    */
   @Test
   public void complexExpressionTestWithDefaults()
   {
      map.put(key, "a${simple:1}:${nested:2}_");
      test("a${simple:1}:${nested:2}_", key, null);
   }

   /**
    * Test complex expression with default values and an equal new value resolved
    */
   @Test
   public void complexExpressionTestWithDefaultsAndEqualNewValue()
   {
      map.put(key, "a${simple:1}:${nested:2}_");
      test("a${simple:1}:${nested:2}_", key, "a1:2_");
   }

   /**
    * Test complex expression with default values and non-equal new value resolved
    */
   @Test
   public void complexExpressionTestWithDefaultsAndNonEqualNewValue()
   {
      map.put(key, "a${simple:1}:${nested:2}_");
      test("c", key, "c");
   }

}
