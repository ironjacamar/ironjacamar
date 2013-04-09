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

package org.jboss.jca.as.converters.wls;

import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;
import org.jboss.jca.as.converters.wls.metadata.WeblogicRaPasrer;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapters;
import org.jboss.jca.common.metadata.resourceadapter.v11.ResourceAdapterParser;

import java.io.InputStream;
import java.util.List;

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
public class WlsRaConvertTestCase
{
   private static Logger log = Logger.getLogger(WlsRaConvertTestCase.class);
   
   private String wlsRaFilesName = "weblogic-ra.xml";
   private String ijRaFilesName = "expected-outcome-wls-ra.xml";
   
   /**
    * test xa ds convert
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRaConvert() throws Throwable
   {
      System.out.println("\nStart parse... " + wlsRaFilesName);

      InputStream in = WlsRaConvertTestCase.class.getClassLoader().getResourceAsStream("wlsra/" + wlsRaFilesName);
      WeblogicRaPasrer parser = new WeblogicRaPasrer();
      WeblogicConnector wlsConnector = parser.parse(in);

      assertNotNull(wlsConnector);
      
      ResourceAdapterParser raParser = new ResourceAdapterParser();
      InputStream ijRaIn = WlsRaConvertTestCase.class.getClassLoader().getResourceAsStream("wlsra/" + ijRaFilesName);
      ResourceAdapters ras = raParser.parse(ijRaIn);
      List<ResourceAdapter> listRa = ras.getResourceAdapters();
      
      assertNotNull(listRa);
      
      assertAdminObjects(wlsConnector, listRa);
      assertConfigProperties(wlsConnector, listRa);
   }

   private void assertAdminObjects(WeblogicConnector wlsConnector, List<ResourceAdapter> listRa)
   {
      ResourceAdapter ra = listRa.get(0);
      assertNotNull(ra);
      assertNotNull(ra.getAdminObjects());
      assertEquals(ra.getAdminObjects().size(), 1);
      
      CommonAdminObject ao = ra.getAdminObjects().get(0);
      assertEquals(ao.getClassName(), 
         wlsConnector.getAdminObjects().getAdminObjectGroup().get(0).getAdminObjectClass());
      assertTrue(ao.getJndiName().indexOf(wlsConnector.getAdminObjects().getAdminObjectGroup().get(0).
         getAdminObjectInstance().get(0).getJndiName()) > 0);
      assertEquals(ao.getConfigProperties().size(), 3);
      assertEquals(ao.getConfigProperties().get("aoi1name"), "aoi1value");
   }

   private void assertConfigProperties(WeblogicConnector wlsConnector, List<ResourceAdapter> listRa)
   {
      ResourceAdapter ra = listRa.get(0);
      assertNotNull(ra);
      assertNotNull(ra.getConfigProperties());
      assertEquals(ra.getConfigProperties().size(), 1);
      assertEquals(ra.getConfigProperties().get("ra1name"), "ra1value");
   }

}
