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
import org.ironjacamar.common.api.metadata.spec.Activationspec;
import org.ironjacamar.common.api.metadata.spec.AdminObject;
import org.ironjacamar.common.api.metadata.spec.AuthenticationMechanism;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.Connector.Version;
import org.ironjacamar.common.api.metadata.spec.CredentialInterfaceEnum;
import org.ironjacamar.common.api.metadata.spec.Icon;
import org.ironjacamar.common.api.metadata.spec.InboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.LicenseType;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.MessageListener;
import org.ironjacamar.common.api.metadata.spec.Messageadapter;
import org.ironjacamar.common.api.metadata.spec.OutboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.RequiredConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.SecurityPermission;
import org.ironjacamar.common.api.metadata.spec.XsdString;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JCA 1.7 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JCA17TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is = JCA17TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.7.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Connector c = parser.parse(xsr);
      assertNotNull(c);

      is.close();
      checkConnector(c);
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is = JCA16TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.7.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Connector c = parser.parse(xsr);
      assertNotNull(c);

      is.close();

      StringReader sr = new StringReader(c.toString());
      XMLStreamReader nxsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      Connector cn = parser.parse(nxsr);
      checkConnector(cn);
      assertEquals(c, cn);
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is1 = JCA17TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.7.xml");
      assertNotNull(is1);

      XMLStreamReader xsr1 = XMLInputFactory.newInstance().createXMLStreamReader(is1);

      Connector c1 = parser.parse(xsr1);
      assertNotNull(c1);

      is1.close();

      InputStream is2 = JCA17TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.7.xml");
      assertNotNull(is2);

      XMLStreamReader xsr2 = XMLInputFactory.newInstance().createXMLStreamReader(is2);

      Connector c2 = parser.parse(xsr2);
      assertNotNull(c2);

      is2.close();

      assertEquals(c1, c2);
   }

   /**
    * Checks connector
    * @param connector
    */
   private void checkConnector(Connector c) throws Exception
   {
      assertEquals("ID001", c.getId());
      assertEquals(Version.V_17, c.getVersion());
      assertTrue(c.isMetadataComplete());
      assertEquals("module-name0", c.getModuleName());
      Field fld =  c.getClass().getDeclaredField("moduleName");
      fld.setAccessible(true);
      assertEquals("ID003", ((XsdString)fld.get(c)).getId());
      List<LocalizedXsdString> lxs = c.getDescriptions();
      assertEquals(2, lxs.size());
      assertTrue(lxs.toString(), containsDescription(lxs, "description0", "en-US", "ID005"));
      assertTrue(lxs.toString(), containsDescription(lxs, "description1", "de", "ID007"));
      lxs = c.getDisplayNames();
      assertEquals(2, lxs.size());
      assertTrue(lxs.toString(), contains(lxs, "display-name", "display-name0", "en-US", "ID009"));
      assertTrue(lxs.toString(), contains(lxs, "display-name", "display-name1", "de", "ID011"));
      List<Icon> li = c.getIcons();
      assertEquals(2, li.size());
      for (Icon i : li)
      {
         switch (i.getId())
         {
            case "ID013" :
            {
               assertEquals("en-US", i.getLang());
               assertEquals(new XsdString("small-icon0", "ID015", "small-icon"), i.getSmallIcon());
               assertEquals(new XsdString("large-icon0", "ID017", "large-icon"), i.getLargeIcon());
               break;
            }
            case "ID019" :
            {
               assertEquals("de", i.getLang());
               assertEquals(new XsdString("small-icon1", "ID021", "small-icon"), i.getSmallIcon());
               assertEquals(new XsdString("large-icon1", "ID023", "large-icon"), i.getLargeIcon());
               break;
            }
            default :
            {
               fail("Unexpected ID:" + i.getId());
            }
         }
      }
      assertEquals(new XsdString("vendor-name0", "ID025", "vendor-name"), c.getVendorName());
      assertEquals(new XsdString("eis-type0", "ID027", "eis-type"), c.getEisType());
      assertEquals(new XsdString("resourceadapter-version0", "ID029", "resourceadapter-version"),
         c.getResourceadapterVersion());
      LicenseType l = c.getLicense();
      assertEquals("ID031", l.getId());
      assertEquals("ID037", l.getLicReqId());
      assertFalse(l.isLicenseRequired());
      lxs = l.getDescriptions();
      assertEquals(2, lxs.size());
      assertTrue(lxs.toString(), containsDescription(lxs, "description2", "en-US", "ID033"));
      assertTrue(lxs.toString(), containsDescription(lxs, "description3", "de", "ID035"));

      ResourceAdapter ra = c.getResourceadapter();
      assertEquals("ID039", ra.getId());
      assertEquals("resourceadapter-class0", ra.getResourceadapterClass());
      fld =  ra.getClass().getDeclaredField("resourceadapterClass");
      fld.setAccessible(true);
      assertEquals("ID041", ((XsdString)fld.get(ra)).getId());
      List<ConfigProperty> lcp = ra.getConfigProperties();
      assertEquals(2, lcp.size());
      for (ConfigProperty cp : lcp)
      {
         switch (cp.getId())
         {
            case "ID043" :
            {
               lxs = cp.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "description4", "en-US", "ID045"));
               assertTrue(lxs.toString(), containsDescription(lxs, "description5", "de", "ID047"));
               assertEquals(new XsdString("config-property-name0", "ID049", "config-property-name"),
                  cp.getConfigPropertyName());
               assertEquals(new XsdString("java.lang.Boolean", "ID051", "config-property-type"),
                  cp.getConfigPropertyType());
               assertEquals(new XsdString("true", "ID053", "config-property-value"), cp.getConfigPropertyValue());
               assertFalse(cp.getConfigPropertyIgnore());
               assertEquals("ID055", cp.getConfigPropertyIgnoreId());
               assertFalse(cp.getConfigPropertyConfidential());
               assertEquals("ID059", cp.getConfigPropertyConfidentialId());
               assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
               assertEquals("ID057", cp.getConfigPropertySupportsDynamicUpdatesId());
               break;
            }
            case "ID061" :
            {
               lxs = cp.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "description6", "en-US", "ID063"));
               assertTrue(lxs.toString(), containsDescription(lxs, "description7", "en-US", "ID065"));
               assertEquals(new XsdString("config-property-name1", "ID067", "config-property-name"),
                  cp.getConfigPropertyName());
               assertEquals(new XsdString("java.lang.String", "ID069", "config-property-type"),
                  cp.getConfigPropertyType());
               assertEquals(new XsdString("config-property-value1", "ID071", "config-property-value"),
                  cp.getConfigPropertyValue());
               assertTrue(cp.getConfigPropertyIgnore());
               assertEquals("ID073", cp.getConfigPropertyIgnoreId());
               assertFalse(cp.getConfigPropertyConfidential());
               assertEquals("ID077", cp.getConfigPropertyConfidentialId());
               assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
               assertEquals("ID075", cp.getConfigPropertySupportsDynamicUpdatesId());
               break;
            }
            default :
            {
               fail("Unexpected ID:" + cp.getId());
            }
         }
      }

      OutboundResourceAdapter ora = ra.getOutboundResourceadapter();
      assertEquals("ID079", ora.getId());
      List<ConnectionDefinition> cdl = ora.getConnectionDefinitions();
      assertEquals(2, cdl.size());
      for (ConnectionDefinition cd : cdl)
      {
         switch (cd.getId())
         {
            case "ID081" :
            {
               assertEquals(new XsdString("managedconnectionfactory-class0", "ID083", "managedconnectionfactory-class"),
                  cd.getManagedConnectionFactoryClass());
               assertEquals(new XsdString("connectionfactory-interface0", "ID121", "connectionfactory-interface"),
                  cd.getConnectionFactoryInterface());
               assertEquals(new XsdString("connectionfactory-impl-class0", "ID123", "connectionfactory-impl-class"),
                  cd.getConnectionFactoryImplClass());
               assertEquals(new XsdString("connection-interface0", "ID125", "connection-interface"),
                  cd.getConnectionInterface());
               assertEquals(new XsdString("connection-impl-class0", "ID127", "connection-impl-class"),
                  cd.getConnectionImplClass());
               List<ConfigProperty> cpl = cd.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID085" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description8", "en-US", "ID087"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description9", "en-US", "ID089"));
                        assertEquals(new XsdString("config-property-name2", "ID091", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.Float", "ID093", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("5", "ID095", "config-property-value"), cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID097", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID101", cp.getConfigPropertyConfidentialId());
                        assertTrue(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID099", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     case"ID103" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description10", "en-US", "ID105"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description11", "de", "ID107"));
                        assertEquals(new XsdString("config-property-name3", "ID109", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.Integer", "ID111", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("3", "ID113", "config-property-value"), cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID115", cp.getConfigPropertyIgnoreId());
                        assertTrue(cp.getConfigPropertyConfidential());
                        assertEquals("ID119", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID117", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     default :
                     {
                        fail("Unexpected ID:" + cp.getId());
                     }
                  }
               }
               break;
            }
            case "ID129" :
            {
               assertEquals(new XsdString("managedconnectionfactory-class1", "ID131", "managedconnectionfactory-class"),
                  cd.getManagedConnectionFactoryClass());
               assertEquals(new XsdString("connectionfactory-interface1", "ID169", "connectionfactory-interface"),
                  cd.getConnectionFactoryInterface());
               assertEquals(new XsdString("connectionfactory-impl-class1", "ID171", "connectionfactory-impl-class"),
                  cd.getConnectionFactoryImplClass());
               assertEquals(new XsdString("connection-interface1", "ID173", "connection-interface"),
                  cd.getConnectionInterface());
               assertEquals(new XsdString("connection-impl-class1", "ID175", "connection-impl-class"),
                  cd.getConnectionImplClass());
               List<ConfigProperty> cpl = cd.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID133" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description12", "en-US", "ID135"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description13", "en-US", "ID137"));
                        assertEquals(new XsdString("config-property-name4", "ID139", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID141", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value4", "ID143", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID145", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID149", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID147", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     case"ID151" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description14", "en-US", "ID153"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description15", "en-US", "ID155"));
                        assertEquals(new XsdString("config-property-name5", "ID157", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID159", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value5", "ID161", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID163", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID167", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID165", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     default :
                     {
                        fail("Unexpected ID:" + cp.getId());
                     }
                  }
               }
               break;
            }
            default :
            {
               fail("Unexpected ID:" + cd.getId());
            }
         }
      }
      assertEquals(TransactionSupportEnum.NoTransaction, ora.getTransactionSupport());
      assertEquals("ID177", ora.getTransactionSupportId());

      List<AuthenticationMechanism> ams = ora.getAuthenticationMechanisms();
      assertEquals(2, ams.size());
      for (AuthenticationMechanism am : ams)
      {
         switch (am.getId())
         {
            case "ID179" :
            {
               lxs = am.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "description16", "en-US", "ID181"));
               assertTrue(lxs.toString(), containsDescription(lxs, "description17", "de", "ID183"));
               assertEquals(new XsdString("authentication-mechanism-type0", "ID185", "authentication-mechanism-type"),
                  am.getAuthenticationMechanismType());
               assertEquals("ID187", am.getCredentialInterfaceId());
               assertEquals(CredentialInterfaceEnum.PasswordCredential, am.getCredentialInterface());
               break;
            }
            case "ID189" :
            {
               lxs = am.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "description18", "en-US", "ID191"));
               assertTrue(lxs.toString(), containsDescription(lxs, "description19", "en-US", "ID193"));
               assertEquals(new XsdString("authentication-mechanism-type1", "ID195", "authentication-mechanism-type"),
                  am.getAuthenticationMechanismType());
               assertEquals("ID197", am.getCredentialInterfaceId());
               assertEquals(CredentialInterfaceEnum.GSSCredential, am.getCredentialInterface());
               break;
            }
            default :
            {
               fail("Unexpected ID:" + am.getId());
            }
         }
      }
      assertEquals("ID199", ora.getReauthenticationSupportId());
      assertTrue(ora.getReauthenticationSupport());

      InboundResourceAdapter ira = ra.getInboundResourceadapter();
      assertEquals("ID201", ira.getId());
      Messageadapter ma = ira.getMessageadapter();
      assertEquals("ID203", ma.getId());
      List<MessageListener> mll = ma.getMessagelisteners();
      assertEquals(2, mll.size());
      for (MessageListener ml : mll)
      {
         switch (ml.getId())
         {
            case "ID205" :
            {
               assertEquals(new XsdString("messagelistener-type0", "ID207", "messagelistener-type"),
                  ml.getMessagelistenerType());
               Activationspec as = ml.getActivationspec();
               assertEquals("ID209", as.getId());
               assertEquals(new XsdString("activationspec-class0", "ID211", "activationspec-class"),
                  as.getActivationspecClass());
               List<ConfigProperty> cpl = as.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID1331" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description12", "en-US", "ID1351"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description13", "en-US", "ID1371"));
                        assertEquals(new XsdString("config-property-name4", "ID1391", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID1411", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value4", "ID1431", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID1451", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID1491", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID1471", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     case"ID1511" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description14", "en-US", "ID1531"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description15", "en-US", "ID1551"));
                        assertEquals(new XsdString("config-property-name5", "ID1571", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID1591", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value5", "ID1611", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID1631", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID1671", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID1651", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     default :
                     {
                        fail("Unexpected ID:" + cp.getId());
                     }
                  }
               }
               List<RequiredConfigProperty> rcpl = as.getRequiredConfigProperties();
               assertEquals(2, rcpl.size());
               for (RequiredConfigProperty rcp : rcpl)
               {
                  if (rcp.getId() != null && rcp.getId().equals("ID1530"))
                  {
                     lxs = rcp.getDescriptions();
                     assertEquals(2, lxs.size());
                     assertTrue(lxs.toString(), containsDescription(lxs, "description14", "en-US", "ID1532"));
                     assertTrue(lxs.toString(), containsDescription(lxs, "description15", "de", "ID1552"));
                     assertEquals(new XsdString("config-property-name5", "ID1554", "config-property-name"),
                        rcp.getConfigPropertyName());
                  }
                  else if (rcp.getId() == null)
                  {
                     lxs = rcp.getDescriptions();
                     assertEquals(2, lxs.size());
                     assertTrue(lxs.toString(), containsDescription(lxs, "description14", "en-US", "ID1533"));
                     assertTrue(lxs.toString(), containsDescription(lxs, "description15", "de", "ID1553"));
                     assertEquals(new XsdString("config-property-name4", null, "config-property-name"),
                        rcp.getConfigPropertyName());
                  }
                  else
                  {
                     fail("Unexpected ID:" + rcp.getId());
                  }
               }
               break;
            }
            case "ID213" :
            {
               assertEquals(new XsdString("messagelistener-type1", "ID215", "messagelistener-type"),
                  ml.getMessagelistenerType());
               Activationspec as = ml.getActivationspec();
               assertEquals("ID217", as.getId());
               assertEquals(new XsdString("activationspec-class1", "ID219", "activationspec-class"),
                  as.getActivationspecClass());
               assertEquals(0, as.getConfigProperties().size());
               assertEquals(0, as.getRequiredConfigProperties().size());
               break;
            }
            default :
            {
               fail("Unexpected ID:" + ml.getId());
            }
         }
      }
      List<AdminObject> lao = ra.getAdminObjects();
      for (AdminObject ao : lao)
      {
         switch (ao.getId())
         {
            case "ID221" :
            {
               assertEquals(new XsdString("adminobject-interface0", "ID223", "adminobject-interface"),
                  ao.getAdminobjectInterface());
               assertEquals(new XsdString("adminobject-class0", "ID225", "adminobject-class"),
                  ao.getAdminobjectClass());
               List<ConfigProperty> cpl = ao.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID227" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description20", "en-US", "ID229"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description21", "en-US", "ID231"));
                        assertEquals(new XsdString("config-property-name6", "ID233", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID235", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value6", "ID237", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID239", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID243", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID241", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     case"ID245" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description22", "en-US", "ID247"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description23", "en-US", "ID249"));
                        assertEquals(new XsdString("config-property-name7", "ID251", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID253", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value7", "ID255", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID257", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID261", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID259", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     default :
                     {
                        fail("Unexpected ID:" + cp.getId());
                     }
                  }
               }
               break;
            }
            case "ID263" :
            {
               assertEquals(new XsdString("adminobject-interface1", "ID265", "adminobject-interface"),
                  ao.getAdminobjectInterface());
               assertEquals(new XsdString("adminobject-class1", "ID267", "adminobject-class"),
                  ao.getAdminobjectClass());
               List<ConfigProperty> cpl = ao.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID269" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description24", "en-US", "ID271"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description25", "en-US", "ID273"));
                        assertEquals(new XsdString("config-property-name8", "ID275", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID277", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value8", "ID279", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID281", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID285", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID283", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     case"ID287" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "description26", "en-US", "ID289"));
                        assertTrue(lxs.toString(), containsDescription(lxs, "description27", "en-US", "ID291"));
                        assertEquals(new XsdString("config-property-name9", "ID293", "config-property-name"),
                           cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.String", "ID295", "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("config-property-value9", "ID297", "config-property-value"),
                           cp.getConfigPropertyValue());
                        assertFalse(cp.getConfigPropertyIgnore());
                        assertEquals("ID299", cp.getConfigPropertyIgnoreId());
                        assertFalse(cp.getConfigPropertyConfidential());
                        assertEquals("ID303", cp.getConfigPropertyConfidentialId());
                        assertFalse(cp.getConfigPropertySupportsDynamicUpdates());
                        assertEquals("ID301", cp.getConfigPropertySupportsDynamicUpdatesId());
                        break;
                     }
                     default :
                     {
                        fail("Unexpected ID:" + cp.getId());
                     }
                  }
               }
               break;
            }
            default :
            {
               fail("Unexpected ID:" + ao.getId());
            }
         }
      }
      List<SecurityPermission> ls = ra.getSecurityPermissions();
      assertEquals(2, ls.size());
      for (SecurityPermission s : ls)
      {
         switch (s.getId())
         {
            case "ID305" :
            {
               lxs = s.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "description28", "en-US", "ID307"));
               assertTrue(lxs.toString(), containsDescription(lxs, "description29", "de", "ID309"));
               assertEquals(new XsdString("security-permission-spec0", "ID311", "security-permission-spec"),
                  s.getSecurityPermissionSpec());
               break;
            }
            case "ID313" :
            {
               lxs = s.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "description30", "en-US", "ID315"));
               assertTrue(lxs.toString(), containsDescription(lxs, "description31", "en-US", "ID317"));
               assertEquals(new XsdString("security-permission-spec1", "ID319", "security-permission-spec"),
                  s.getSecurityPermissionSpec());
               break;
            }
            default:
            {
               fail("Unexpected ID:" + s.getId());
            }
         }
      }
      List<String> lr = c.getRequiredWorkContexts();
      assertEquals(2, lr.size());
      assertTrue(lr.contains("required-work-context0"));
      assertTrue(lr.contains("required-work-context1"));
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
