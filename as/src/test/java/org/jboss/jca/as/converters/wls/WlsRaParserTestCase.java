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

package org.jboss.jca.as.converters.wls;

import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;
import org.jboss.jca.as.converters.wls.metadata.WeblogicRaPasrer;

import java.io.InputStream;

import org.jboss.logging.Logger;

//import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A weblogic -ra.xml parser test case.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class WlsRaParserTestCase
{
   private static Logger log = Logger.getLogger(WlsRaParserTestCase.class);
   
   private String[] raFilesName = {
      "tux-ra.xml",
      "trans-ra.xml"
   };
   
   /**
    * test xa ds parser
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRaParser() throws Throwable
   {
      for (String raFileName : raFilesName)
      {
         System.out.println("\nStart parse... " + raFileName);
         InputStream in = WlsRaParserTestCase.class.getClassLoader().getResourceAsStream("wlsra/" + raFileName);
         WeblogicRaPasrer parser = new WeblogicRaPasrer();
         WeblogicConnector wlsConnector = parser.parse(in);

         assertNotNull(wlsConnector);
      }
   }
 
}
