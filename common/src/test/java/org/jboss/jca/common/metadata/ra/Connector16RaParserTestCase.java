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
package org.jboss.jca.common.metadata.ra;

import org.jboss.jca.common.api.metadata.JCAMetadata;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.CredentialInterfaceEnum;
import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.Messageadapter;
import org.jboss.jca.common.api.metadata.ra.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.metadata.ra.common.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter1516Impl;
import org.jboss.jca.common.metadata.ra.ra15.Activationspec15Impl;
import org.jboss.jca.common.metadata.ra.ra16.Activationspec16Impl;
import org.jboss.jca.common.metadata.ra.ra16.Connector16Impl;

import java.util.List;

import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 *
 * Test case for connector 1.6
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class Connector16RaParserTestCase extends RaParserTestBase
{

   /**
    * set up
    */
   @BeforeClass
   public static void setUp()
   {
      parsedFileName = "ra/unit/connector-1.6-sample.xml";
   }

   /**
    * checks connector 1.6
    * @param result of parsing
    */
   @Override
   public void checkMetadata(JCAMetadata result)
   {
      Connector connector = (Connector) result;
      assertTrue(connector instanceof Connector16Impl);
      assertEquals(connector.getVersion(), Version.V_16);

      Connector16Impl con = (Connector16Impl) connector;
      assertEquals("ID001", con.getId());
      assertFalse(con.isMetadataComplete());
      assertEquals("module-name0", con.getModuleName());
      assertEquals(
         con.getDescriptions(),
         createDescriptions(new LocalizedXsdString("description0", "ID005", "en-US"),
            new LocalizedXsdString("description1", "ID007", "de")));
      assertEquals(
         con.getDisplayNames(),
         createDisplayNames(new LocalizedXsdString("display-name0", "ID009", "en-US"),
            new LocalizedXsdString("display-name1", "ID011", "de")));

      List<Icon> icons = con.getIcons();
      assertEquals(2, icons.size());
      Icon icon = icons.get(0);
      assertEquals("ID013", icon.getId());
      assertEquals("en-US", icon.getLang());
      checkXsdString(icon.getSmallIcon(), "small-icon0", "ID015", "small-icon");
      checkXsdString(icon.getLargeIcon(), "large-icon0", "ID017", "large-icon");
      icon = icons.get(1);
      assertEquals("ID019", icon.getId());
      assertEquals("de", icon.getLang());
      checkXsdString(icon.getSmallIcon(), "small-icon1", "ID021", "small-icon");
      checkXsdString(icon.getLargeIcon(), "large-icon1", "ID023", "large-icon");

      checkXsdString(con.getVendorName(), "vendor-name0", "ID025", "vendor-name");
      checkXsdString(con.getResourceadapterVersion(), "resourceadapter-version0", "ID029", "resourceadapter-version");
      checkXsdString(con.getEisType(), "eis-type0", "ID027", "eis-type");

      LicenseType license = con.getLicense();
      assertEquals("ID031", license.getId());
      assertEquals(
         license.getDescriptions(),
         createDescriptions(new LocalizedXsdString("description2", "ID033", "en-US"),
            new LocalizedXsdString("description3", "ID035", "de")));
      assertFalse(license.isLicenseRequired());
      assertEquals("ID037", license.getLicReqId());

      ResourceAdapter rai = con.getResourceadapter();
      assertTrue(rai instanceof ResourceAdapter1516Impl);
      ResourceAdapter1516Impl ra = (ResourceAdapter1516Impl) rai;
      assertEquals("ID039", ra.getId());
      assertEquals(ra.getResourceadapterClass(), "resourceadapter-class0");

      List<? extends ConfigProperty> cps = ra.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID043", "ID049", "config-property-name0", "ID051", "java.lang.Boolean", "ID053",
         "true", "ID055", false, "ID057", false, "ID059", false, new LocalizedXsdString("description4", "ID045",
                                                                                        "en-US"),
         new LocalizedXsdString("description5", "ID047", "de"));
      checkConfigProperty(cps.get(1), "ID061", "ID067", "config-property-name1", "ID069", "java.lang.String", "ID071",
         "config-property-value1", "ID073", true, "ID075", false, "ID077", false,
         new LocalizedXsdString("description6", "ID063", "en-US"), new LocalizedXsdString("description7", "ID065",
                                                                                          "en-US"));

      OutboundResourceAdapterImpl out = (OutboundResourceAdapterImpl) ra.getOutboundResourceadapter();
      assertEquals("ID079", out.getId());
      List<ConnectionDefinition> cds = out.getConnectionDefinitions();
      assertEquals(2, cds.size());

      ConnectionDefinition cd = cds.get(0);
      assertEquals("ID081", cd.getId());
      checkXsdString(cd.getManagedConnectionFactoryClass(), "managedconnectionfactory-class0", "ID083",
         "managedconnectionfactory-class");
      checkXsdString(cd.getConnectionFactoryInterface(), "connectionfactory-interface0", "ID121",
         "connectionfactory-interface");
      checkXsdString(cd.getConnectionFactoryImplClass(), "connectionfactory-impl-class0", "ID123",
         "connectionfactory-impl-class");
      checkXsdString(cd.getConnectionInterface(), "connection-interface0", "ID125", "connection-interface");
      checkXsdString(cd.getConnectionImplClass(), "connection-impl-class0", "ID127", "connection-impl-class");

      cps = cd.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID085", "ID091", "config-property-name2", "ID093", "java.lang.Float", "ID095",
         "5", "ID097", false, "ID099", true, "ID101", false, new LocalizedXsdString("description8", "ID087", "en-US"),
         new LocalizedXsdString("description9", "ID089", "en-US"));
      checkConfigProperty(cps.get(1), "ID103", "ID109", "config-property-name3", "ID111", "java.lang.Integer", "ID113",
         "3", "ID115", false, "ID117", false, "ID119", true, new LocalizedXsdString("description10", "ID105", "en-US"),
         new LocalizedXsdString("description11", "ID107", "de"));

      cd = cds.get(1);
      assertEquals("ID129", cd.getId());
      checkXsdString(cd.getManagedConnectionFactoryClass(), "managedconnectionfactory-class1", "ID131",
         "managedconnectionfactory-class");
      checkXsdString(cd.getConnectionFactoryInterface(), "connectionfactory-interface1", "ID169",
         "connectionfactory-interface");
      checkXsdString(cd.getConnectionFactoryImplClass(), "connectionfactory-impl-class1", "ID171",
         "connectionfactory-impl-class");
      checkXsdString(cd.getConnectionInterface(), "connection-interface1", "ID173", "connection-interface");
      checkXsdString(cd.getConnectionImplClass(), "connection-impl-class1", "ID175", "connection-impl-class");

      cps = cd.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID133", "ID139", "config-property-name4", "ID141", "java.lang.String", "ID143",
         "config-property-value4", "ID145", false, "ID147", false, "ID149", false,
         new LocalizedXsdString("description12", "ID135", "en-US"), new LocalizedXsdString("description13", "ID137",
                                                                                           "en-US"));
      checkConfigProperty(cps.get(1), "ID151", "ID157", "config-property-name5", "ID159", "java.lang.String", "ID161",
         "config-property-value5", "ID163", false, "ID165", false, "ID167", false,
         new LocalizedXsdString("description14", "ID153", "en-US"), new LocalizedXsdString("description15", "ID155",
                                                                                           "en-US"));

      assertEquals(out.getTransactionSupport(), TransactionSupportEnum.NoTransaction);
      assertEquals("ID177", out.getTransactionSupportId());

      List<AuthenticationMechanism> ams = out.getAuthenticationMechanisms();
      assertEquals(2, ams.size());
      checkAuthenticationMechanism(ams.get(0), "ID179", "ID185", "authentication-mechanism-type0", "ID187",
         CredentialInterfaceEnum.PasswordCredential, new LocalizedXsdString("description16", "ID181", "en-US"),
         new LocalizedXsdString("description17", "ID183", "de"));
      checkAuthenticationMechanism(ams.get(1), "ID189", "ID195", "authentication-mechanism-type1", "ID197",
         CredentialInterfaceEnum.GSSCredential, new LocalizedXsdString("description18", "ID191", "en-US"),
         new LocalizedXsdString("description19", "ID193", "en-US"));

      assertEquals(true, out.getReauthenticationSupport());
      assertEquals("ID199", out.getReauthenticationSupportId());

      InboundResourceAdapter in = ra.getInboundResourceadapter();
      assertEquals("ID201", in.getId());

      Messageadapter ma = in.getMessageadapter();
      assertEquals("ID203", ma.getId());
      List<MessageListener> mls = ma.getMessagelisteners();
      assertEquals(2, mls.size());

      MessageListener ml = mls.get(0);
      assertEquals("ID205", ml.getId());
      checkXsdString(ml.getMessagelistenerType(), "messagelistener-type0", "ID207", "messagelistener-type");
      Activationspec16Impl as = (Activationspec16Impl) ml.getActivationspec();
      assertEquals("ID209", as.getId());
      checkXsdString(as.getActivationspecClass(), "activationspec-class0", "ID211", "activationspec-class");

      cps = as.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID1331", "ID1391", "config-property-name4", "ID1411", "java.lang.String",
         "ID1431", "config-property-value4", "ID1451", false, "ID1471", false, "ID1491", false,
         new LocalizedXsdString("description12", "ID1351", "en-US"), new LocalizedXsdString("description13", "ID1371",
                                                                                            "en-US"));
      checkConfigProperty(cps.get(1), "ID1511", "ID1571", "config-property-name5", "ID1591", "java.lang.String",
         "ID1611", "config-property-value5", "ID1631", false, "ID1651", false, "ID1671", false,
         new LocalizedXsdString("description14", "ID1531", "en-US"), new LocalizedXsdString("description15", "ID1551",
                                                                                            "en-US"));

      List<RequiredConfigProperty> rcps = as.getRequiredConfigProperties();
      assertEquals(2, rcps.size());
      checkRequiredConfigProperty(rcps.get(0), "ID1530", "ID1554", "config-property-name5",
         new LocalizedXsdString("description14", "ID1532", "en-US"), new LocalizedXsdString("description15", "ID1552",
                                                                                            "de"));
      checkRequiredConfigProperty(rcps.get(1), null, null, "config-property-name4",
         new LocalizedXsdString("description14", "ID1533", "en-US"), new LocalizedXsdString("description15", "ID1553",
                                                                                            "de"));

      ml = mls.get(1);
      assertEquals("ID213", ml.getId());
      checkXsdString(ml.getMessagelistenerType(), "messagelistener-type1", "ID215", "messagelistener-type");
      assertTrue(ml.getActivationspec() instanceof Activationspec15Impl);
      Activationspec15Impl as1 = (Activationspec15Impl) ml.getActivationspec();
      assertEquals("ID217", as1.getId());
      checkXsdString(as1.getActivationspecClass(), "activationspec-class1", "ID219", "activationspec-class");
      assertEquals(0, as1.getRequiredConfigProperties().size());

      List<AdminObject> aos = ra.getAdminObjects();
      assertEquals(2, aos.size());
      AdminObject ao = aos.get(0);
      assertEquals("ID221", ao.getId());
      checkXsdString(ao.getAdminobjectInterface(), "adminobject-interface0", "ID223", "adminobject-interface");
      checkXsdString(ao.getAdminobjectClass(), "adminobject-class0", "ID225", "adminobject-class");

      cps = ao.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID227", "ID233", "config-property-name6", "ID235", "java.lang.String", "ID237",
         "config-property-value6", "ID239", false, "ID241", false, "ID243", false,
         new LocalizedXsdString("description20", "ID229", "en-US"), new LocalizedXsdString("description21", "ID231",
                                                                                           "en-US"));
      checkConfigProperty(cps.get(1), "ID245", "ID251", "config-property-name7", "ID253", "java.lang.String", "ID255",
         "config-property-value7", "ID257", false, "ID259", false, "ID261", false,
         new LocalizedXsdString("description22", "ID247", "en-US"), new LocalizedXsdString("description23", "ID249",
                                                                                           "en-US"));

      ao = aos.get(1);
      assertEquals("ID263", ao.getId());
      checkXsdString(ao.getAdminobjectInterface(), "adminobject-interface1", "ID265", "adminobject-interface");
      checkXsdString(ao.getAdminobjectClass(), "adminobject-class1", "ID267", "adminobject-class");

      cps = ao.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID269", "ID275", "config-property-name8", "ID277", "java.lang.String", "ID279",
         "config-property-value8", "ID281", false, "ID283", false, "ID285", false,
         new LocalizedXsdString("description24", "ID271", "en-US"), new LocalizedXsdString("description25", "ID273",
                                                                                           "en-US"));
      checkConfigProperty(cps.get(1), "ID287", "ID293", "config-property-name9", "ID295", "java.lang.String", "ID297",
         "config-property-value9", "ID299", false, "ID301", false, "ID303", false,
         new LocalizedXsdString("description26", "ID289", "en-US"), new LocalizedXsdString("description27", "ID291",
                                                                                           "en-US"));

      List<SecurityPermission> sps = ra.getSecurityPermissions();
      assertEquals(2, sps.size());
      checkSecurityPermission(sps.get(0), "ID305", "ID311", "security-permission-spec0",
         new LocalizedXsdString("description28", "ID307", "en-US"), new LocalizedXsdString("description29", "ID309",
                                                                                           "de"));
      checkSecurityPermission(sps.get(1), "ID313", "ID319", "security-permission-spec1",
         new LocalizedXsdString("description30", "ID315", "en-US"), new LocalizedXsdString("description31", "ID317",
                                                                                           "en-US"));

      List<String> wcs = con.getRequiredWorkContexts();
      assertEquals(2, wcs.size());
      assertEquals("required-work-context0", wcs.get(0));
      assertEquals("required-work-context1", wcs.get(1));
   }

}
