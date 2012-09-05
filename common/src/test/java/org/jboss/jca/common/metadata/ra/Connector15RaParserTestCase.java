/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.metadata.ra.common.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter1516Impl;
import org.jboss.jca.common.metadata.ra.ra15.Activationspec15Impl;
import org.jboss.jca.common.metadata.ra.ra15.Connector15Impl;

import java.util.List;

import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 *
 * Test case for connector 1.5
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class Connector15RaParserTestCase extends RaParserTestBase
{

   /**
    * set up
    */
   @BeforeClass
   public static void setUp()
   {
      connectorFileName = "connector-1.5-sample.xml";
   }

   /**
    * checks connector 1.5
    * @param connector to check
    */
   @Override
   public void checkConnector(Connector connector)
   {
      assertTrue(connector instanceof Connector15Impl);
      assertEquals(connector.getVersion(), Version.V_15);

      Connector15Impl con = (Connector15Impl) connector;
      assertEquals("ID001", con.getId());
      assertEquals(
         con.getDescriptions(),
         createDescriptions(new LocalizedXsdString("something", "ID001A"), new LocalizedXsdString("something",
                                                                                                  "ID001B", "en"),
            new LocalizedXsdString("something", "ID001C", "de")));
      checkLocalizedXsdString(con.getDescriptions().get(1), "something", "ID001B", "description", "en");
      assertEquals(con.getDisplayNames(),
         createDisplayNames(new LocalizedXsdString("something", null), 
            new LocalizedXsdString("something", null, "de")));

      List<Icon> icons = con.getIcons();
      assertEquals(2, icons.size());
      Icon icon = icons.get(0);
      assertEquals("ID001D", icon.getId());
      assertEquals(null, icon.getLang());
      checkXsdString(icon.getSmallIcon(), "small.gif", "ID001F", "small-icon");
      checkXsdString(icon.getLargeIcon(), "large.gif", "ID001E", "large-icon");
      icon = icons.get(1);
      assertEquals("ID001G", icon.getId());
      assertEquals("de", icon.getLang());
      assertEquals(icon.getSmallIcon(), null);
      assertEquals(icon.getLargeIcon(), null);

      checkXsdString(con.getVendorName(), "something", "ID002", "vendor-name");
      checkXsdString(con.getResourceadapterVersion(), "5", "ID006A", "resourceadapter-version");
      checkXsdString(con.getEisType(), "something", "ID004", "eis-type");

      LicenseType license = con.getLicense();
      assertEquals("ID003", license.getId());
      assertEquals(
         license.getDescriptions(),
         createDescriptions(new LocalizedXsdString("something", "ID003A"), new LocalizedXsdString("something", null,
                                                                                                  "de")));
      assertFalse(license.isLicenseRequired());
      assertEquals("ID003B", license.getLicReqId());

      ResourceAdapter rai = con.getResourceadapter();
      assertTrue(rai instanceof ResourceAdapter1516Impl);
      ResourceAdapter1516Impl ra = (ResourceAdapter1516Impl) rai;
      assertEquals("ID005", ra.getId());
      assertEquals(ra.getResourceadapterClass(), "Class1");

      List<? extends ConfigProperty> cps = ra.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID007", "ID006", "aaa", null, "java.lang.Short", null, "12",
         new LocalizedXsdString("something", "ID007B", "en"), new LocalizedXsdString("something", "ID007C", "de"));
      checkConfigProperty(cps.get(1), "ID009", null, "bbb", null, "java.lang.Integer", null, "35674",
         new LocalizedXsdString("something", "ID009B", "en"), new LocalizedXsdString("something", "ID009C", "de"));

      OutboundResourceAdapterImpl out = (OutboundResourceAdapterImpl) ra.getOutboundResourceadapter();
      assertEquals("ID011", out.getId());
      List<ConnectionDefinition> cds = out.getConnectionDefinitions();
      assertEquals(2, cds.size());

      ConnectionDefinition cd = cds.get(0);
      assertEquals("ID013", cd.getId());
      checkXsdString(cd.getManagedConnectionFactoryClass(), "Class2", "ID013A", "managedconnectionfactory-class");
      checkXsdString(cd.getConnectionFactoryInterface(), "Interface1", "ID017A", "connectionfactory-interface");
      checkXsdString(cd.getConnectionFactoryImplClass(), "Class3", "ID017B", "connectionfactory-impl-class");
      checkXsdString(cd.getConnectionInterface(), "Interface2", "ID017C", "connection-interface");
      checkXsdString(cd.getConnectionImplClass(), "Class4", "ID017D", "connection-impl-class");

      cps = cd.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID015", null, "ccc", null, "java.lang.Character", null, "c",
         new LocalizedXsdString("something", null), new LocalizedXsdString("something", "ID014"));
      checkConfigProperty(cps.get(1), "ID016", null, "dddd", null, "java.lang.Byte", null, "24",
         new LocalizedXsdString("12345", null, "ru"), new LocalizedXsdString("", null));

      cd = cds.get(1);
      assertEquals("ID019", cd.getId());
      checkXsdString(cd.getManagedConnectionFactoryClass(), "", null, "managedconnectionfactory-class");
      checkXsdString(cd.getConnectionFactoryInterface(), "", null, "connectionfactory-interface");
      checkXsdString(cd.getConnectionFactoryImplClass(), "", null, "connectionfactory-impl-class");
      checkXsdString(cd.getConnectionInterface(), "", null, "connection-interface");
      checkXsdString(cd.getConnectionImplClass(), "", null, "connection-impl-class");

      cps = cd.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID021", null, "", null, "", null, "", new LocalizedXsdString("", null),
         new LocalizedXsdString("", null));
      checkConfigProperty(cps.get(1), "ID023", null, "", null, "", null, "", new LocalizedXsdString("", null),
         new LocalizedXsdString("", null));

      assertEquals(out.getTransactionSupport(), TransactionSupportEnum.LocalTransaction);
      assertEquals("ID024", out.getTsId());

      List<AuthenticationMechanism> ams = out.getAuthenticationMechanisms();
      assertEquals(2, ams.size());
      checkAuthenticationMechanism(ams.get(0), "ID025", "ID025A", "type", "ID025B",
         CredentialInterfaceEnum.PasswordCredential, new LocalizedXsdString("", null), new LocalizedXsdString("a",
                                                                                                              null,
                                                                                                              "en"));
      checkAuthenticationMechanism(ams.get(1), "ID027", null, "", null, CredentialInterfaceEnum.GenericCredential,
         new LocalizedXsdString("", null), new LocalizedXsdString("", null));

      assertEquals(false, out.getReauthenticationSupport());
      assertEquals("ID028", out.getRsId());

      InboundResourceAdapter in = ra.getInboundResourceadapter();
      assertEquals("ID029", in.getId());

      Messageadapter ma = in.getMessageadapter();
      assertEquals("ID031", ma.getId());
      List<MessageListener> mls = ma.getMessagelisteners();
      assertEquals(2, mls.size());

      MessageListener ml = mls.get(0);
      assertEquals("ID033", ml.getId());
      checkXsdString(ml.getMessagelistenerType(), "type", "ID034", "messagelistener-type");
      Activationspec15Impl as = (Activationspec15Impl) ml.getActivationspec();
      assertEquals("ID035", as.getId());
      checkXsdString(as.getActivationspecClass(), "class6", null, "activationspec-class");
      List<RequiredConfigProperty> rcps = as.getRequiredConfigProperties();
      assertEquals(2, rcps.size());
      checkRequiredConfigProperty(rcps.get(0), "ID035A", "ID035D", "bbb", new LocalizedXsdString("something", "ID035B",
                                                                                                 "en"),
         new LocalizedXsdString("something", "ID035C", "de"));
      checkRequiredConfigProperty(rcps.get(1), null, null, "ccc", new LocalizedXsdString("something", null),
         new LocalizedXsdString("", null));

      ml = mls.get(1);
      assertEquals("ID037", ml.getId());
      checkXsdString(ml.getMessagelistenerType(), "", null, "messagelistener-type");
      as = (Activationspec15Impl) ml.getActivationspec();
      assertEquals("ID039", as.getId());
      checkXsdString(as.getActivationspecClass(), "", null, "activationspec-class");
      rcps = as.getRequiredConfigProperties();
      assertEquals(1, rcps.size());
      assertEquals(null, rcps.get(0).getId());
      assertEquals(createDescriptions(null), rcps.get(0).getDescriptions());
      assertTrue(XsdString.isNull(rcps.get(0).getConfigPropertyName()));

      List<AdminObject> aos = ra.getAdminObjects();
      assertEquals(2, aos.size());
      AdminObject ao = aos.get(0);
      assertEquals("ID041", ao.getId());
      checkXsdString(ao.getAdminobjectInterface(), "Interface5", "ID041A", "adminobject-interface");
      checkXsdString(ao.getAdminobjectClass(), "class7", "ID042", "adminobject-class");

      cps = ao.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID043", null, "eee", null, "java.lang.Double", null, "3.14",
         new LocalizedXsdString("", null), new LocalizedXsdString("", null));
      checkConfigProperty(cps.get(1), "ID045", null, "sss", null, "java.lang.Float", null, null,
         new LocalizedXsdString("", null), new LocalizedXsdString("", null));

      ao = aos.get(1);
      assertEquals("ID047", ao.getId());
      checkXsdString(ao.getAdminobjectInterface(), "", null, "adminobject-interface");
      checkXsdString(ao.getAdminobjectClass(), "", null, "adminobject-class");

      cps = ao.getConfigProperties();
      assertEquals(2, cps.size());
      checkConfigProperty(cps.get(0), "ID049", null, "", null, "", null, "", new LocalizedXsdString("", null),
         new LocalizedXsdString("", null));
      checkConfigProperty(cps.get(1), "ID051", null, "", null, "", null, "", new LocalizedXsdString("", null),
         new LocalizedXsdString("", null));

      List<SecurityPermission> sps = ra.getSecurityPermissions();
      assertEquals(2, sps.size());
      checkSecurityPermission(sps.get(0), "ID053", "ID054", "spec1", new LocalizedXsdString("", null),
         new LocalizedXsdString("", null));
      checkSecurityPermission(sps.get(1), "ID055", null, "", new LocalizedXsdString("", null),
         new LocalizedXsdString("", null));
   }

}
