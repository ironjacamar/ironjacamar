/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.as.converters.mbean;

import org.jboss.jca.as.converters.ConnectionFactories;
import org.jboss.jca.as.converters.LegacyCfParser;

import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A mbean parse test case.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class MbeanParserTestCase
{
   private String[] cfFilesName = {
      "mbean-multiattrs-ds.xml",
      "mbean-attr-ds.xml"
   };
   
   /**
    * test cf parser
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCfParser() throws Throwable
   {
      LegacyCfParser parser = new LegacyCfParser();
      
      for (String cfFileName : cfFilesName)
      {
         System.out.println("\nStart parse... " + cfFileName);
         InputStream in = MbeanParserTestCase.class.getClassLoader().getResourceAsStream("mbean/" + cfFileName);
         ConnectionFactories ds = parser.parse(in);
   
         assertTrue(ds.toString().indexOf("admin-object") > 0);
      }
   }
}
