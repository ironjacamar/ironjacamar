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

import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup;
import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectInstance;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinition;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties;
import org.jboss.jca.as.converters.wls.api.metadata.TransactionSupport;
import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;
import org.jboss.jca.as.converters.wls.metadata.WeblogicRaPasrer;

import java.io.InputStream;

//import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A weblogic -ra.xml parser test case.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class WeblogicRaParserTestCase
{

   private String raFileName = "weblogic-ra.xml";

   /**
    * test xa ds parser
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRaParser() throws Throwable
   {
      System.out.println("\nStart parse... " + raFileName);
      InputStream in = WeblogicRaParserTestCase.class.getClassLoader().getResourceAsStream("wlsra/" + raFileName);
      WeblogicRaPasrer parser = new WeblogicRaPasrer();
      WeblogicConnector wlsConnector = parser.parse(in);
      assertNotNull(wlsConnector);
      assertNotNull(wlsConnector.getNativeLibdir());
      assertNotNull(wlsConnector.getJndiName());
      assertNotNull(wlsConnector.getSecurity());
      assertNotNull(wlsConnector.getSecurity().getSecurityWorkContext());
      assertFalse(wlsConnector.getSecurity().getSecurityWorkContext().getInboundMappingRequired());
      assertEquals(wlsConnector.getSecurity().getSecurityWorkContext().getGroupPrincipalDefaultMapped(), 
         "eisgroupname");
      
      assertNotNull(wlsConnector.getProperties());
      assertEquals(wlsConnector.getProperties().getProperty().size(), 1);
      assertEquals(wlsConnector.getProperties().getProperty().get(0).getName(), "ra1name");

      assertNotNull(wlsConnector.getAdminObjects());
      assertNotNull(wlsConnector.getAdminObjects().getDefaultProperties());
      assertEquals(wlsConnector.getAdminObjects().getDefaultProperties().getProperty().size(), 1);
      assertEquals(wlsConnector.getAdminObjects().getDefaultProperties().getProperty().get(0).getName(), "ao1name");
      
      assertNotNull(wlsConnector.getAdminObjects().getAdminObjectGroup());
      assertEquals(wlsConnector.getAdminObjects().getAdminObjectGroup().size(), 1);
      AdminObjectGroup aog = wlsConnector.getAdminObjects().getAdminObjectGroup().get(0);
      assertNotNull(aog);
      assertEquals(aog.getAdminObjectClass(), "aoclass");
      assertNotNull(aog.getDefaultProperties());
      assertEquals(aog.getDefaultProperties().getProperty().size(), 1);
      assertEquals(aog.getDefaultProperties().getProperty().get(0).getName(), "aog1name");
      
      assertNotNull(aog.getAdminObjectInstance());
      assertEquals(aog.getAdminObjectInstance().size(), 1);
      AdminObjectInstance aoi = aog.getAdminObjectInstance().get(0);
      assertNotNull(aoi);
      assertEquals(aoi.getJndiName(), "aojndi1name");
      assertNotNull(aoi.getProperties());
      assertEquals(aoi.getProperties().getProperty().size(), 1);
      assertEquals(aoi.getProperties().getProperty().get(0).getName(), "aoi1name");
      
      assertNotNull(wlsConnector.getOutboundResourceAdapter());
      ConnectionDefinitionProperties cdProps = 
         wlsConnector.getOutboundResourceAdapter().getDefaultConnectionProperties();
      assertNotNull(cdProps);
      assertNotNull(cdProps.getPoolParams());
      assertEquals(cdProps.getPoolParams().getInitialCapacity().intValue(), 5);
      assertEquals(cdProps.getPoolParams().getMaxCapacity().intValue(), 20);
      assertEquals(cdProps.getTransactionSupport(), TransactionSupport.XATransaction);
      assertEquals(cdProps.getProperties().getProperty().get(0).getName(), "dcp1name");
      
      assertNotNull(wlsConnector.getOutboundResourceAdapter().getConnectionDefinitionGroup());
      assertEquals(wlsConnector.getOutboundResourceAdapter().getConnectionDefinitionGroup().size(), 1);
      ConnectionDefinition cd = wlsConnector.getOutboundResourceAdapter().getConnectionDefinitionGroup().get(0);
      
      assertNotNull(cd);
      assertNotNull(cd.getConnectionInstance());
      assertEquals(cd.getConnectionInstance().size(), 1);
      assertEquals(cd.getConnectionInstance().get(0).getDescription(), "TuxDesc");
   }

}
