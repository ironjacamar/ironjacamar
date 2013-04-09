/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

import org.jboss.jca.codegenerator.code.RaCodeGen;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A JCA16AnnoProfile test case.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class JCA16AnnoProfileTestCase
{
   private static Logger log = Logger.getLogger(JCA16AnnoProfileTestCase.class);
   
  
   /**
    * test process
    * @throws Throwable throwable exception 
    */
   @Test
   public void testProcessFile() throws Throwable
   {
      Definition def = new Definition();
      def.setRaPackage("org.jboss.jca.test");
      def.setRaClass("BaseResourceAdapter");

      List<ConfigPropType> props = new ArrayList<ConfigPropType>();
      ConfigPropType config = new ConfigPropType("myProp", "String", "Hello", false);
      props.add(config);
      def.setRaConfigProps(props);
      
      StringWriter writer = new StringWriter();

      RaCodeGen codeGen = new RaCodeGen();
      codeGen.writeClassBody(def, writer);
      assertTrue(writer.toString().indexOf("BaseResourceAdapter") > 0);
      assertTrue(writer.toString().indexOf("getMyProp") > 0);
   }
}
