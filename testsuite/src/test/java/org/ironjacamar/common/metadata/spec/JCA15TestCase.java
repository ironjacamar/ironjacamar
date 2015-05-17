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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JCA 1.5 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JCA15TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
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

      InputStream is = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
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

      InputStream is1 = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
      assertNotNull(is1);

      XMLStreamReader xsr1 = XMLInputFactory.newInstance().createXMLStreamReader(is1);

      Connector c1 = parser.parse(xsr1);
      assertNotNull(c1);

      is1.close();

      InputStream is2 = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
      assertNotNull(is2);

      XMLStreamReader xsr2 = XMLInputFactory.newInstance().createXMLStreamReader(is2);

      Connector c2 = parser.parse(xsr2);
      assertNotNull(c2);

      is2.close();

      assertEquals(c1, c2);
   }
   
   /**
    * Copy
    * @throws Exception In case of an error
    */
   @Test
   public void testCopy() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is1 = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
      assertNotNull(is1);

      XMLStreamReader xsr1 = XMLInputFactory.newInstance().createXMLStreamReader(is1);

      Connector c1 = parser.parse(xsr1);
      assertNotNull(c1);

      is1.close();

      Connector c2 = c1.copy();
      
      assertEquals(c1, c2);
      
      assertFalse(c1 == c2);
      
      assertEquals(c1.toString(), c2.toString());
   }
   
   /**
    * Checks connector
    * @param connector
    */
   private void checkConnector(Connector c) throws Exception
   {
      assertEquals("ID001", c.getId());
      assertEquals(Version.V_15, c.getVersion());
      List<LocalizedXsdString> lxs = c.getDescriptions();
      assertEquals(3, lxs.size());
      assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID001A"));
      assertTrue(lxs.toString(), containsDescription(lxs, "something", "en", "ID001B"));
      assertTrue(lxs.toString(), containsDescription(lxs, "something", "de", "ID001C"));
      lxs = c.getDisplayNames();
      assertEquals(2, lxs.size());
      assertTrue(lxs.toString(), contains(lxs, "display-name", "something", null, null));
      assertTrue(lxs.toString(), contains(lxs, "display-name", "something", "de", null));
      List<Icon> li = c.getIcons();
      assertEquals(2, li.size());
      for (Icon i : li)
      {
         switch (i.getId())
         {
            case "ID001D" :
            {
               assertNull(i.getLang());
               assertEquals(new XsdString("small.gif", "ID001F", "small-icon"), i.getSmallIcon());
               assertEquals(new XsdString("large.gif", "ID001E", "large-icon"), i.getLargeIcon());
               break;
            }
            case "ID001G" :
            {
               assertEquals("de", i.getLang());
               assertNull(i.getSmallIcon());
               assertNull(i.getLargeIcon());
               break;
            }
            default :
            {
               fail("Unexpected ID:" + i.getId());
            }
         }
      }
      assertEquals(new XsdString("something", "ID002", "vendor-name"), c.getVendorName());
      assertEquals(new XsdString("something", "ID004", "eis-type"), c.getEisType());
      assertEquals(new XsdString("5", "ID006A", "resourceadapter-version"), c.getResourceadapterVersion());
      LicenseType l = c.getLicense();
      assertEquals("ID003", l.getId());
      assertEquals("ID003B", l.getLicReqId());
      assertFalse(l.isLicenseRequired());
      lxs = l.getDescriptions();
      assertEquals(2, lxs.size());
      assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID003A"));
      assertTrue(lxs.toString(), containsDescription(lxs, "something", "de", null));

      ResourceAdapter ra = c.getResourceadapter();
      assertEquals("ID005", ra.getId());
      assertEquals("Class1", ra.getResourceadapterClass());
      Field fld =  ra.getClass().getDeclaredField("resourceadapterClass");
      fld.setAccessible(true);
      assertEquals("ID005A", ((XsdString)fld.get(ra)).getId());
      List<ConfigProperty> lcp = ra.getConfigProperties();
      assertEquals(2, lcp.size());
      for (ConfigProperty cp : lcp)
      {
         switch (cp.getId())
         {
            case "ID007" :
            {
               lxs = cp.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "something", "en", "ID007B"));
               assertTrue(lxs.toString(), containsDescription(lxs, "something", "de", "ID007C"));
               assertEquals(new XsdString("aaa", "ID006", "config-property-name"), cp.getConfigPropertyName());
               assertEquals(new XsdString("java.lang.Short", null, "config-property-type"),
                  cp.getConfigPropertyType());
               assertEquals(new XsdString("12", null, "config-property-value"), cp.getConfigPropertyValue());
               break;
            }
            case "ID009" :
            {
               lxs = cp.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "something", "en", "ID009B"));
               assertTrue(lxs.toString(), containsDescription(lxs, "something", "de", "ID009C"));
               assertEquals(new XsdString("bbb", null, "config-property-name"), cp.getConfigPropertyName());
               assertEquals(new XsdString("java.lang.Integer", null, "config-property-type"),
                  cp.getConfigPropertyType());
               assertEquals(new XsdString("35674", null, "config-property-value"), cp.getConfigPropertyValue());
               break;
            }
            default :
            {
               fail("Unexpected ID:" + cp.getId());
            }
         }
      }

      OutboundResourceAdapter ora = ra.getOutboundResourceadapter();
      assertEquals("ID011", ora.getId());
      List<ConnectionDefinition> cdl = ora.getConnectionDefinitions();
      assertEquals(2, cdl.size());
      for (ConnectionDefinition cd : cdl)
      {
         switch (cd.getId())
         {
            case "ID013" :
            {
               assertEquals(new XsdString("Class2", "ID013A", "managedconnectionfactory-class"),
                  cd.getManagedConnectionFactoryClass());
               assertEquals(new XsdString("Interface1", "ID017A", "connectionfactory-interface"),
                  cd.getConnectionFactoryInterface());
               assertEquals(new XsdString("Class3", "ID017B", "connectionfactory-impl-class"),
                  cd.getConnectionFactoryImplClass());
               assertEquals(new XsdString("Interface2", "ID017C", "connection-interface"),
                  cd.getConnectionInterface());
               assertEquals(new XsdString("Class4", "ID017D", "connection-impl-class"),
                  cd.getConnectionImplClass());
               List<ConfigProperty> cpl = cd.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID015" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "something", null, null));
                        assertTrue(lxs.toString(), containsDescription(lxs, "something", null, "ID014"));
                        assertEquals(new XsdString("ccc", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.Character", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("c", null, "config-property-value"), cp.getConfigPropertyValue());
                        break;
                     }
                     case"ID016" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "12345", "ru", null));
                        assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                        assertEquals(new XsdString("dddd", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.Byte", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("24", null, "config-property-value"), cp.getConfigPropertyValue());
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
            case "ID019" :
            {
               assertEquals(new XsdString("", null, "managedconnectionfactory-class"),
                  cd.getManagedConnectionFactoryClass());
               assertEquals(new XsdString("", null, "connectionfactory-interface"),
                  cd.getConnectionFactoryInterface());
               assertEquals(new XsdString("", null, "connectionfactory-impl-class"),
                  cd.getConnectionFactoryImplClass());
               assertEquals(new XsdString("", null, "connection-interface"),
                  cd.getConnectionInterface());
               assertEquals(new XsdString("", null, "connection-impl-class"),
                  cd.getConnectionImplClass());
               List<ConfigProperty> cpl = cd.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID021" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                        assertEquals(new XsdString("", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("", null, "config-property-value"), cp.getConfigPropertyValue());
                        break;
                     }
                     case"ID023" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                        assertEquals(new XsdString("", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("", null, "config-property-value"), cp.getConfigPropertyValue());
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
      assertEquals(TransactionSupportEnum.LocalTransaction, ora.getTransactionSupport());
      assertEquals("ID024", ora.getTransactionSupportId());

      List<AuthenticationMechanism> ams = ora.getAuthenticationMechanisms();
      assertEquals(2, ams.size());
      for (AuthenticationMechanism am : ams)
      {
         switch (am.getId())
         {
            case "ID025" :
            {
               lxs = am.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "a", "en", null));
               assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
               assertEquals(new XsdString("type", "ID025A", "authentication-mechanism-type"),
                  am.getAuthenticationMechanismType());
               assertEquals("ID025B", am.getCredentialInterfaceId());
               assertEquals(CredentialInterfaceEnum.PasswordCredential, am.getCredentialInterface());
               break;
            }
            case "ID027" :
            {
               lxs = am.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
               assertEquals(new XsdString("", null, "authentication-mechanism-type"),
                  am.getAuthenticationMechanismType());
               assertNull(am.getCredentialInterfaceId());
               assertEquals(CredentialInterfaceEnum.GenericCredential, am.getCredentialInterface());
               break;
            }
            default :
            {
               fail("Unexpected ID:" + am.getId());
            }
         }
      }
      assertEquals("ID028", ora.getReauthenticationSupportId());
      assertFalse(ora.getReauthenticationSupport());

      InboundResourceAdapter ira = ra.getInboundResourceadapter();
      assertEquals("ID029", ira.getId());
      Messageadapter ma = ira.getMessageadapter();
      assertEquals("ID031", ma.getId());
      List<MessageListener> mll = ma.getMessagelisteners();
      assertEquals(2, mll.size());
      for (MessageListener ml : mll)
      {
         switch (ml.getId())
         {
            case "ID033" :
            {
               assertEquals(new XsdString("type", "ID034", "messagelistener-type"), ml.getMessagelistenerType());
               Activationspec as = ml.getActivationspec();
               assertEquals("ID035", as.getId());
               assertEquals(new XsdString("class6", null, "activationspec-class"), as.getActivationspecClass());
               List<RequiredConfigProperty> rcpl = as.getRequiredConfigProperties();
               assertEquals(2, rcpl.size());
               for (RequiredConfigProperty rcp : rcpl)
               {
                  if (rcp.getId() != null && rcp.getId().equals("ID035A"))
                  {
                     lxs = rcp.getDescriptions();
                     assertEquals(2, lxs.size());
                     assertTrue(lxs.toString(), containsDescription(lxs, "something", "de", "ID035C"));
                     assertTrue(lxs.toString(), containsDescription(lxs, "something", "en", "ID035B"));
                     assertEquals(new XsdString("bbb", "ID035D", "config-property-name"),
                        rcp.getConfigPropertyName());
                  }
                  else if (rcp.getId() != null)
                  {
                     fail("Unexpected ID:" + rcp.getId());
                  }
                  else
                  {
                     lxs = rcp.getDescriptions();
                     assertEquals(2, lxs.size());
                     assertTrue(lxs.toString(), containsDescription(lxs, "something", null, null));
                     assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                     assertEquals(new XsdString("ccc", null, "config-property-name"), rcp.getConfigPropertyName());
                  }
               }
               break;
            }
            case "ID037" :
            {
               assertEquals(new XsdString("", null, "messagelistener-type"), ml.getMessagelistenerType());
               Activationspec as = ml.getActivationspec();
               assertEquals("ID039", as.getId());
               assertEquals(new XsdString("", null, "activationspec-class"), as.getActivationspecClass());
               List<RequiredConfigProperty> rcpl = as.getRequiredConfigProperties();
               assertEquals(1, rcpl.size());
               RequiredConfigProperty rcp = rcpl.get(0);
               lxs = rcp.getDescriptions();
               assertEquals(0, lxs.size());
               assertEquals(XsdString.NULL_XSDSTRING, rcp.getConfigPropertyName());
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
            case "ID041" :
            {
               assertEquals(new XsdString("Interface5", "ID041A", "adminobject-interface"),
                  ao.getAdminobjectInterface());
               assertEquals(new XsdString("class7", "ID042", "adminobject-class"),
                  ao.getAdminobjectClass());
               List<ConfigProperty> cpl = ao.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID043" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                        assertEquals(new XsdString("eee", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.Double", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("3.14", null, "config-property-value"), cp.getConfigPropertyValue());
                        break;
                     }
                     case"ID045" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                        assertEquals(new XsdString("sss", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("java.lang.Float", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(XsdString.NULL_XSDSTRING, cp.getConfigPropertyValue());
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
            case "ID047" :
            {
               assertEquals(new XsdString("", null, "adminobject-interface"),
                  ao.getAdminobjectInterface());
               assertEquals(new XsdString("", null, "adminobject-class"),
                  ao.getAdminobjectClass());
               List<ConfigProperty> cpl = ao.getConfigProperties();
               assertEquals(2, cpl.size());
               for (ConfigProperty cp : cpl)
               {
                  switch (cp.getId())
                  {
                     case "ID049" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                        assertEquals(new XsdString("", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("", null, "config-property-value"), cp.getConfigPropertyValue());
                        break;
                     }
                     case"ID051" :
                     {
                        lxs = cp.getDescriptions();
                        assertEquals(2, lxs.size());
                        assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
                        assertEquals(new XsdString("", null, "config-property-name"), cp.getConfigPropertyName());
                        assertEquals(new XsdString("", null, "config-property-type"),
                           cp.getConfigPropertyType());
                        assertEquals(new XsdString("", null, "config-property-value"), cp.getConfigPropertyValue());
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
            case "ID053" :
            {
               lxs = s.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
               assertEquals(new XsdString("spec1", "ID054", "security-permission-spec"),
                  s.getSecurityPermissionSpec());
               break;
            }
            case "ID055" :
            {
               lxs = s.getDescriptions();
               assertEquals(2, lxs.size());
               assertTrue(lxs.toString(), containsDescription(lxs, "", null, null));
               assertEquals(new XsdString("", null, "security-permission-spec"),
                  s.getSecurityPermissionSpec());
               break;
            }
            default:
            {
               fail("Unexpected ID:" + s.getId());
            }
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
