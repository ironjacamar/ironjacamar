/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.codegenerator;

import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A SimpleTemplate test case.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class SimpleTemplateTestCase
{
   private static Logger log = Logger.getLogger(SimpleTemplateTestCase.class);
   
   /**
    * test process
    * @throws Throwable throwable exception 
    */
   @Test
   public void testProcessString() throws Throwable
   {
      Template template = new SimpleTemplate("Hello ${name}");
      Map<String, String> varMap = new HashMap<String, String>();
      varMap.put("name", "Jeff");
      StringWriter writer = new StringWriter();
      template.process(varMap, writer);
      assertEquals(writer.toString(), "Hello Jeff");
   }
   
   /**
    * test replace
    * @throws Throwable throwable exception 
    */
   @Test
   public void testReplaceOk() throws Throwable
   {
      SimpleTemplate template = new SimpleTemplate("Hello ${firstname} ${lastname}");
      Map<String, String> varMap = new HashMap<String, String>();
      varMap.put("firstname", "Jeff");
      varMap.put("lastname", "Zhang");
      String replacedString = template.replace(varMap);
      assertEquals(replacedString, "Hello Jeff Zhang");
   }
   
   /**
    * test replace
    * @throws Throwable throwable exception 
    */
   @Test
   public void testReplaceDouble() throws Throwable
   {
      SimpleTemplate template = new SimpleTemplate("Hello ${firstname} ${firstname}");
      Map<String, String> varMap = new HashMap<String, String>();
      varMap.put("firstname", "Jeff");
      varMap.put("lastname", "Zhang");
      String replacedString = template.replace(varMap);
      assertEquals(replacedString, "Hello Jeff Jeff");
   }
   
   /**
    * test replace
    * @throws Throwable throwable exception 
    */
   @Test
   public void testReplaceNothing() throws Throwable
   {
      SimpleTemplate template = new SimpleTemplate("Hello ${firstname} ${firstname}");
      Map<String, String> varMap = new HashMap<String, String>();
      //varMap.put("firstname", "Jeff");
      varMap.put("lastname", "Zhang");
      String replacedString = template.replace(varMap);
      assertEquals(replacedString, "Hello ${firstname} ${firstname}");
   }
   
   /**
    * test replace
    * @throws Throwable throwable exception 
    */
   @Test
   public void testReplaceJustOne() throws Throwable
   {
      SimpleTemplate template = new SimpleTemplate("Hello ${firstname ${firstname} ${lastname}");
      Map<String, String> varMap = new HashMap<String, String>();
      //varMap.put("firstname", "Jeff");
      varMap.put("lastname", "Zhang");
      String replacedString = template.replace(varMap);
      assertEquals(replacedString, "Hello ${firstname ${firstname} Zhang");
   }
   
   /**
    * test process
    * @throws Throwable throwable exception 
    */
   @Test
   public void testProcessFile() throws Throwable
   {
      URL file = SimpleTemplateTestCase.class.getResource("/test.template");
      SimpleTemplate template = new SimpleTemplate(file);
      Map<String, String> varMap = new HashMap<String, String>();
      varMap.put("package.name", "org.jboss.jca.test");
      varMap.put("class.name", "BaseResourceAdapter");
      StringWriter writer = new StringWriter();
      template.process(varMap, writer);
      assertTrue(writer.toString().indexOf("org.jboss.jca.test") > 0);
   }
}
