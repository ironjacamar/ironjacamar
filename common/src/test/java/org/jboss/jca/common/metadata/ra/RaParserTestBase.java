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

import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.CredentialInterfaceEnum;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.metadata.XMLParserTestBase;
import org.jboss.jca.common.metadata.ra.common.AuthenticationMechanismImpl;
import org.jboss.jca.common.metadata.ra.common.ConfigPropertyImpl;
import org.jboss.jca.common.metadata.ra.ra16.ConfigProperty16Impl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * A RaParserTestBase.
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public abstract class RaParserTestBase extends XMLParserTestBase
{

   static
   {
      parser = new RaParser();
   }


   /**
    * 
    * Creates list of XsdStrings
    * @param <T> class that extends XsdString
    * @param tag for all elements
    * @param elements in list
    * @return list
    */
   protected <T extends XsdString> List<T> createList(String tag, T... elements)
   {
      List<T> result = new ArrayList<T>();
      if (elements != null)
         for (T el : elements)
         {
            el.setTag(tag);
            result.add(el);
         }
      return result;
   }

   /**
    * creates list of descriptions
    * @param elements to add
    * @return list
    */
   protected List<LocalizedXsdString> createDescriptions(LocalizedXsdString... elements)
   {
      return createList("description", elements);
   }

   /**
    * creates list of displayNames
    * @param elements to add
    * @return list
    */
   protected List<LocalizedXsdString> createDisplayNames(LocalizedXsdString... elements)
   {
      return createList("display-name", elements);
   }

   /**
    * Checks, if XsdString contains all needed parameters
    * @param str string to check
    * @param value to check
    * @param id to check
    * @param tag to check
    */
   protected void checkXsdString(XsdString str, String value, String id, String tag)
   {
      assertEquals(str, new XsdString(value, id, tag));
      assertEquals(str.getValue(), value);
      assertEquals(str.getId(), id);
      assertEquals(str.getTag(), tag);
   }

   /**
    * Checks, if LocalizedXsdString contains all needed parameters
    * @param str string to check
    * @param value to check
    * @param id to check
    * @param tag to check
    * @param lang to check
    */
   protected void checkLocalizedXsdString(LocalizedXsdString str, String value, String id, String tag, String lang)
   {
      assertEquals(str, new LocalizedXsdString(value, id, lang, tag));
      assertEquals(str.getValue(), value);
      assertEquals(str.getId(), id);
      assertEquals(str.getTag(), tag);
      assertEquals(str.getLang(), lang);
   }

   /**
    * checks if authentication mechanism contains all parameters
    * @param am authentication mechanism
    * @param id of am
    * @param amId id of type
    * @param amType type
    * @param ciid ci id
    * @param ci credential interface
    * @param descriptions list
    */
   protected void checkAuthenticationMechanism(AuthenticationMechanism am, String id, String amId, String amType,
      String ciid, CredentialInterfaceEnum ci, LocalizedXsdString... descriptions)
   {
      assertEquals(id, am.getId());
      assertEquals(createDescriptions(descriptions), am.getDescriptions());
      checkXsdString(am.getAuthenticationMechanismType(), amType, amId, "authentication-mechanism-type");
      assertEquals(am.getCredentialInterface(), ci);
      assertEquals(((AuthenticationMechanismImpl) am).getCIId(), ciid);
   }

   /**
    * Checks config property
    * @param cp config property
    * @param id its 
    * @param nameId name id
    * @param name config property name
    * @param typeId type id
    * @param type config property type
    * @param valueId value id
    * @param value config property value
    * @param ignoreId ignore id
    * @param ignore config property ignore
    * @param upId up id
    * @param upd supports dynamic updates
    * @param confId conf id
    * @param conf confidential
    * @param descriptions list
    */
   protected void checkConfigProperty(ConfigProperty cp, String id, String nameId, String name, String typeId,
      String type, String valueId, String value, String ignoreId, Boolean ignore, String upId, Boolean upd,
      String confId, Boolean conf, LocalizedXsdString... descriptions)
   {

      assertEquals(id, cp.getId());
      assertEquals(createDescriptions(descriptions), cp.getDescriptions());
      checkXsdString(cp.getConfigPropertyName(), name, nameId, "config-property-name");
      checkXsdString(cp.getConfigPropertyType(), type, typeId, "config-property-type");
      if (valueId == null && value == null)
         assertTrue(XsdString.isNull(cp.getConfigPropertyValue()));
      else
         checkXsdString(cp.getConfigPropertyValue(), value, valueId, "config-property-value");

      if (ignoreId == null && ignore == null && upId == null && upd == null && confId == null && conf == null)
         assertTrue(cp instanceof ConfigPropertyImpl);
      else
      {
         assertTrue(cp instanceof ConfigProperty16Impl);
         ConfigProperty16Impl cp16 = (ConfigProperty16Impl) cp;
         assertTrue(cp16.getAttachedClassName() == null);
         assertEquals(ignore, cp16.getConfigPropertyIgnore());
         assertEquals(ignoreId, cp16.getConfigPropertyIgnoreId());
         assertEquals(upd, cp16.getConfigPropertySupportsDynamicUpdates());
         assertEquals(upId, cp16.getConfigPropertySupportsDynamicUpdatesId());
         assertEquals(conf, cp16.getConfigPropertyConfidential());
         assertEquals(confId, cp16.getConfigPropertyConfidentialId());

      }
   }

   /**
    * Checks config property for connector version &lt 1.6
    * @param cp config property
    * @param id its 
    * @param nameId name id
    * @param name config property name
    * @param typeId type id
    * @param type config property type
    * @param valueId value id
    * @param value config property value
    * @param descriptions list
    */
   protected void checkConfigProperty(ConfigProperty cp, String id, String nameId, String name, String typeId,
      String type, String valueId, String value, LocalizedXsdString... descriptions)
   {
      checkConfigProperty(cp, id, nameId, name, typeId, type, valueId, value, null, null, null, null, null, null,
         descriptions);
   }

   /**
    * Checks security permission
    * @param sp security permission
    * @param id its id
    * @param spsId sps id
    * @param sps security-permission-spec
    * @param descriptions list
    */
   protected void checkSecurityPermission(SecurityPermission sp, String id, String spsId, String sps,
      LocalizedXsdString... descriptions)
   {
      assertEquals(id, sp.getId());
      assertEquals(createDescriptions(descriptions), sp.getDescriptions());
      checkXsdString(sp.getSecurityPermissionSpec(), sps, spsId, "security-permission-spec");
   }

   /**
    * Checks required config property
    * @param rcp required config property
    * @param id its id
    * @param nameId name id
    * @param name config-property-name
    * @param descriptions list
    */
   protected void checkRequiredConfigProperty(RequiredConfigProperty rcp, String id, String nameId, String name,
      LocalizedXsdString... descriptions)
   {
      assertEquals(id, rcp.getId());
      assertEquals(createDescriptions(descriptions), rcp.getDescriptions());
      checkXsdString(rcp.getConfigPropertyName(), name, nameId, "config-property-name");
   }
}
