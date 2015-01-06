/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.spec.AuthenticationMechanism;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.CredentialInterfaceEnum;
import org.ironjacamar.common.api.metadata.spec.Icon;
import org.ironjacamar.common.api.metadata.spec.LicenseType;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.OutboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.ResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.SecurityPermission;
import org.ironjacamar.common.api.metadata.spec.XsdString;

import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * JCA 1.0 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JCA10TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is = JCA10TestCase.class.getClassLoader().getResourceAsStream("../../resources/test/spec/ra-1.0.xml");
      assertNotNull(is);

      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

      XMLStreamReader xsr = inputFactory.createXMLStreamReader(is);

      Connector c = parser.parse(xsr);
      assertNotNull(c);

      is.close();
      checkConnector(c);
   }

   /**
    * Checks connector
    * @param connector
    */
   private void checkConnector(Connector c)
   {
      assertEquals("ID000", c.getId());
      List<LocalizedXsdString> lxs = c.getDescriptions();
      assertEquals(1, lxs.size());
      assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID002"));
      lxs = c.getDisplayNames();
      assertEquals(1, lxs.size());
      assertTrue(lxs.toString(), contains(lxs, "display-name", "something", null, "ID001"));
      List<Icon> li = c.getIcons();
      assertEquals(1, li.size());
      Icon i = li.get(0);
      assertEquals("ID003", i.getId());
      assertNull(i.getLang());
      assertEquals(new XsdString("small.gif", "ID004", "small-icon"), i.getSmallIcon());
      assertEquals(new XsdString("large.gif", "ID005", "large-icon"), i.getLargeIcon());
      assertEquals(new XsdString("something", "ID006", "vendor-name"), c.getVendorName());
      assertEquals(Connector.Version.V_10, c.getVersion());
      assertEquals(new XsdString("something", "ID008", "eis-type"), c.getEisType());
      assertEquals(new XsdString("1.0", "ID009", "version"), c.getResourceadapterVersion());
      LicenseType l = c.getLicense();
      assertEquals("ID010", l.getId());
      assertEquals("ID012", l.getLicReqId());
      assertTrue(l.isLicenseRequired());
      lxs = l.getDescriptions();
      assertEquals(1, lxs.size());
      assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID011"));

      ResourceAdapter ra = c.getResourceadapter();
      assertEquals("ID013", ra.getId());
      assertNull(ra.getResourceadapterClass());
      OutboundResourceAdapter ora = ra.getOutboundResourceadapter();
      List<ConnectionDefinition> cdl = ora.getConnectionDefinitions();
      assertEquals(1, cdl.size());

      ConnectionDefinition cd = cdl.get(0);
      assertEquals(new XsdString("Class1", "ID014", "managedconnectionfactory-class"),
         cd.getManagedConnectionFactoryClass());
      assertEquals(new XsdString("Interface1", "ID015", "connectionfactory-interface"),
         cd.getConnectionFactoryInterface());
      assertEquals(new XsdString("Class2", "ID016", "connectionfactory-impl-class"),
         cd.getConnectionFactoryImplClass());
      assertEquals(new XsdString("Interface2", "ID017", "connection-interface"),
         cd.getConnectionInterface());
      assertEquals(new XsdString("Class3", "ID018", "connection-impl-class"),
         cd.getConnectionImplClass());
      assertEquals(TransactionSupportEnum.NoTransaction, ora.getTransactionSupport());
      assertEquals("ID019", ora.getTransactionSupportId());

      List<ConfigProperty> cpl = cd.getConfigProperties();
      assertEquals(2, cpl.size());
      for (ConfigProperty cp : cpl)
      {
         if (cp.getId().equals("ID020"))
         {
            lxs = cp.getDescriptions();
            assertEquals(1, lxs.size());
            assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID021"));
            assertEquals(new XsdString("aaa", "ID022", "config-property-name"), cp.getConfigPropertyName());
            assertEquals(new XsdString("java.lang.String", "ID023", "config-property-type"),
               cp.getConfigPropertyType());
            assertEquals(new XsdString("aaa", "ID024", "config-property-value"), cp.getConfigPropertyValue());
         }
         else if (cp.getId().equals("ID025"))
         {
            lxs = cp.getDescriptions();
            assertEquals(1, lxs.size());
            assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID026"));
            assertEquals(new XsdString("bbb", "ID027", "config-property-name"), cp.getConfigPropertyName());
            assertEquals(new XsdString("java.lang.Boolean", "ID028", "config-property-type"),
               cp.getConfigPropertyType());
            assertEquals(new XsdString("false", "ID029", "config-property-value"), cp.getConfigPropertyValue());
         }
         else
         {
            fail("Unexpected ID:" + cp.getId());
         }
      }
      List<AuthenticationMechanism> ams = ora.getAuthenticationMechanisms();
      assertEquals(2, ams.size());
      for (AuthenticationMechanism am : ams)
      {
         if (am.getId().equals("ID030"))
         {
            lxs = am.getDescriptions();
            assertEquals(1, lxs.size());
            assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID031"));
            assertEquals(new XsdString("BasicPassword", "ID032", "authentication-mechanism-type"),
               am.getAuthenticationMechanismType());
            assertEquals("ID033", am.getCredentialInterfaceId());
            assertEquals(CredentialInterfaceEnum.PasswordCredential, am.getCredentialInterface());
         }
         else if (am.getId().equals("ID034"))
         {
            lxs = am.getDescriptions();
            assertEquals(1, lxs.size());
            assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID035"));
            assertEquals(new XsdString("Kerbv5", "ID036", "authentication-mechanism-type"),
               am.getAuthenticationMechanismType());
            assertEquals("ID037", am.getCredentialInterfaceId());
            assertEquals(CredentialInterfaceEnum.GenericCredential, am.getCredentialInterface());
         }
         else
         {
            fail("Unexpected ID:" + am.getId());
         }
      }
      assertEquals("ID038", ora.getReauthenticationSupportId());
      assertTrue(ora.getReauthenticationSupport());

      List<SecurityPermission> ls = ra.getSecurityPermissions();
      assertEquals(2, ls.size());
      for (SecurityPermission s : ls)
      {
         if (s.getId().equals("ID039"))
         {
            lxs = s.getDescriptions();
            assertEquals(1, lxs.size());
            assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID040"));
            assertEquals(new XsdString("something", "ID041", "security-permission-spec"),
               s.getSecurityPermissionSpec());
         }
         else if (s.getId().equals("ID042"))
         {
            lxs = s.getDescriptions();
            assertEquals(1, lxs.size());
            assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID043"));
            assertEquals(new XsdString("something_else", "ID044", "security-permission-spec"),
               s.getSecurityPermissionSpec());
         }
         else
         {
            fail("Unexpected ID:" + s.getId());
         }
      }
   }

   /**
    * Checks if a list contains the LocalizedXsdString
    * @param lxs list
    * @param tag of LocalizedXsdString
    * @param value of LocalizedXsdString
    * @param lang attribute of LocalizedXsdString
    * @param id attribute of LocalizedXsdString
    * @return true if contains, false otherwise
    */
   private boolean contains(List<LocalizedXsdString> lxs, String tag, String value, String lang, String id)
   {
      return lxs.contains(new LocalizedXsdString(value, id, lang, tag));
   }

   /**
    * Checks if a list contains the LocalizedXsdString with a description tag
    * @param lxs list
    * @param value of LocalizedXsdString
    * @param lang attribute of LocalizedXsdString
    * @param id attribute of LocalizedXsdString
    * @return true if contains, false otherwise
    */
   private boolean containsDescription(List<LocalizedXsdString> lxs, String value, String lang, String id)
   {
      return contains(lxs, "description", value, lang, id);
   }
}
