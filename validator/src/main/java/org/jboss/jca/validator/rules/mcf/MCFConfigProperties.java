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

package org.jboss.jca.validator.rules.mcf;

import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Key;
import org.jboss.jca.validator.Rule;
import org.jboss.jca.validator.Validate;
import org.jboss.jca.validator.ValidateClass;
import org.jboss.jca.validator.rules.ConfigPropertiesHelper;

import java.util.List;
import java.util.ResourceBundle;

import javax.resource.spi.ManagedConnectionFactory;

/**
 * A ManagedConnectionFactory must use the valid set of config-property-type
 */
public class MCFConfigProperties implements Rule
{
   /** Section */
   private static final String SECTION = "20.7";

   /**
    * Validate
    * @param v The validate object
    * @param rb The resource bundle
    * @return The list of failures found; <code>null</code> if none
    */
   @SuppressWarnings("unchecked")
   public List<Failure> validate(Validate v, ResourceBundle rb)
   {
      if (v != null &&
          Key.MANAGED_CONNECTION_FACTORY == v.getKey() &&
          v.getClazz() != null &&
          ManagedConnectionFactory.class.isAssignableFrom(v.getClazz()))
      {
         ValidateClass vo = (ValidateClass)v;
         if (vo.getConfigProperties() != null && vo.getConfigProperties().size() > 0)
         {
            return ConfigPropertiesHelper.validateConfigPropertiesType(vo, SECTION, 
               rb.getString("mcf.MCFConfigProperties"));
         }
      }

      return null;
   }
}
