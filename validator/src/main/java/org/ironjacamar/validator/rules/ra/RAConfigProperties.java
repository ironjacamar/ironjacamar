/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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


package org.ironjacamar.validator.rules.ra;

import org.ironjacamar.validator.Failure;
import org.ironjacamar.validator.Key;
import org.ironjacamar.validator.Rule;
import org.ironjacamar.validator.Validate;
import org.ironjacamar.validator.ValidateClass;
import org.ironjacamar.validator.rules.ConfigPropertiesHelper;

import java.util.List;
import java.util.ResourceBundle;

import javax.resource.spi.ResourceAdapter;

/**
 * ResourceAdapter must use the valid set of config-property-type
 */
public class RAConfigProperties implements Rule
{
   /** Section */
   private static final String SECTION = "20.7";

   /**
    * Constructor
    */
   public RAConfigProperties()
   {
   }

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
          Key.RESOURCE_ADAPTER == v.getKey() &&
          v.getClazz() != null &&
          ResourceAdapter.class.isAssignableFrom(v.getClazz()))
      {
         ValidateClass vo = (ValidateClass)v;
         if (vo.getConfigProperties() != null && !vo.getConfigProperties().isEmpty())
         {
            return ConfigPropertiesHelper.validateConfigPropertiesType(vo, SECTION,
                    rb.getString("ra.RAConfigProperties"));
         }
      }

      return null;
   }
}
