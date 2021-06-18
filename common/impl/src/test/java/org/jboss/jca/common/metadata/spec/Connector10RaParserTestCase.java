/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.spec;

import org.jboss.jca.common.api.metadata.JCAMetadata;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.spec.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.CredentialInterfaceEnum;
import org.jboss.jca.common.api.metadata.spec.Icon;
import org.jboss.jca.common.api.metadata.spec.LicenseType;
import org.jboss.jca.common.api.metadata.spec.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.SecurityPermission;

import java.util.List;

import org.jboss.logging.Logger;

import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 *
 * Test case for connector 1.0
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class Connector10RaParserTestCase extends RaParserTestBase
{
   private static Logger log = Logger.getLogger(Connector10RaParserTestCase.class);

   /**
    * set up
    */
   @BeforeClass
   public static void setUp()
   {
      parsedFileName = "ra/unit/connector-1.0-sample.xml";
   }

   @Override
   public void checkMetadata(JCAMetadata result)
   {
      Connector connector = (Connector) result;
      assertTrue(connector instanceof ConnectorImpl);
      assertEquals(connector.getVersion(), Version.V_10);

      log.info(connector);

      ConnectorImpl con = (ConnectorImpl) connector;
      assertEquals("ID000", con.getId());
      assertEquals(createDescriptions(new LocalizedXsdString("something", "ID002")), con.getDescriptions());
      checkLocalizedXsdString(con.getDescriptions().get(0), "something", "ID002", "description", null);
      assertEquals(createDisplayNames(new LocalizedXsdString("something", "ID001")), con.getDisplayNames());

      List<Icon> icons = con.getIcons();
      assertEquals(1, icons.size());
      Icon icon = icons.get(0);
      assertEquals("ID003", icon.getId());
      checkXsdString(icon.getSmallIcon(), "small.gif", "ID004", "small-icon");
      checkXsdString(icon.getLargeIcon(), "large.gif", "ID005", "large-icon");

      checkXsdString(con.getVendorName(), "something", "ID006", "vendor-name");
      //checkXsdString(con.getSpecVersion(), "1.0", "ID007", "spec-version");
      checkXsdString(con.getEisType(), "something", "ID008", "eis-type");

      LicenseType license = con.getLicense();
      assertEquals("ID010", license.getId());
      assertEquals(createDescriptions(new LocalizedXsdString("something", "ID011")), license.getDescriptions());
      assertTrue(license.isLicenseRequired());
      assertEquals("ID012", license.getLicReqId());

      ResourceAdapter ra = con.getResourceadapter();
      assertTrue(ra instanceof ResourceAdapterImpl);
      ResourceAdapterImpl ra10 = (ResourceAdapterImpl) ra;
      assertEquals("ID013", ra10.getId());

      ConnectionDefinition cd = ra10.getOutboundResourceadapter().getConnectionDefinitions().get(0);
      checkXsdString(cd.getManagedConnectionFactoryClass(), "Class1", "ID014", "managedconnectionfactory-class");
      checkXsdString(cd.getConnectionFactoryInterface(), "Interface1", "ID015", "connectionfactory-interface");
      checkXsdString(cd.getConnectionFactoryImplClass(), "Class2", "ID016", "connectionfactory-impl-class");
      checkXsdString(cd.getConnectionInterface(), "Interface2", "ID017", "connection-interface");
      checkXsdString(cd.getConnectionImplClass(), "Class3", "ID018", "connection-impl-class");

      assertEquals(ra10.getOutboundResourceadapter().getTransactionSupport(), TransactionSupportEnum.NoTransaction);
      //assertEquals("ID019", ra10.getOutboundResourceadapter().getTransactionSupportId());

      List<ConfigProperty> cps = cd.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID020", "ID022", "aaa", "ID023", "java.lang.String", "ID024", "aaa",
         new LocalizedXsdString("something", "ID021"));
      checkConfigProperty(cps.get(1), "ID025", "ID027", "bbb", "ID028", "java.lang.Boolean", "ID029", "false",
         new LocalizedXsdString("something", "ID026"));

      List<AuthenticationMechanism> ams = ra10.getOutboundResourceadapter().getAuthenticationMechanisms();
      assertEquals(2, ams.size());
      checkAuthenticationMechanism(ams.get(0), "ID030", "ID032", "BasicPassword", "ID033",
         CredentialInterfaceEnum.PasswordCredential, new LocalizedXsdString("something", "ID031"));
      checkAuthenticationMechanism(ams.get(1), "ID034", "ID036", "Kerbv5", "ID037",
         CredentialInterfaceEnum.GenericCredential, new LocalizedXsdString("something", "ID035"));

      assertEquals(true, ra10.getOutboundResourceadapter().getReauthenticationSupport());
      //assertEquals("ID038", ra10.getReauthenticationSupportId());

      List<SecurityPermission> sps = ra10.getSecurityPermissions();
      assertEquals(2, sps.size());
      checkSecurityPermission(sps.get(0), "ID039", "ID041", "something", new LocalizedXsdString("something", "ID040"));
      checkSecurityPermission(sps.get(1), "ID042", "ID044", "something_else", new LocalizedXsdString("something",
                                                                                                     "ID043"));
   }
}
