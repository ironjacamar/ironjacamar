/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.deployers.common.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Validator
 */
public class Validator
{
   /** 
    * The fully qualified class name of the rules
    * as we may want to externalize them into a 
    * properties file
    */
   private static final String[] CLASS_RULES = {
      "org.jboss.jca.deployers.common.validator.rules.mcf.MCF",
      "org.jboss.jca.deployers.common.validator.rules.mcf.MCFConstructor",
      "org.jboss.jca.deployers.common.validator.rules.mcf.MCFHashCode",
      "org.jboss.jca.deployers.common.validator.rules.mcf.MCFEquals",
      "org.jboss.jca.deployers.common.validator.rules.mcf.MCFConfigProperties",
      "org.jboss.jca.deployers.common.validator.rules.mc.MC",
      "org.jboss.jca.deployers.common.validator.rules.mc.MCGetMetaData",
      "org.jboss.jca.deployers.common.validator.rules.ra.RANull",
      "org.jboss.jca.deployers.common.validator.rules.ra.RA",
      "org.jboss.jca.deployers.common.validator.rules.ra.RAHashCode",
      "org.jboss.jca.deployers.common.validator.rules.ra.RAEquals",
      "org.jboss.jca.deployers.common.validator.rules.ra.RAConfigProperties",
      "org.jboss.jca.deployers.common.validator.rules.cf.CFNull",
      "org.jboss.jca.deployers.common.validator.rules.cf.CFSerializable",
      "org.jboss.jca.deployers.common.validator.rules.cf.CFReferenceable",
      "org.jboss.jca.deployers.common.validator.rules.as.ASConfigProperties",
      "org.jboss.jca.deployers.common.validator.rules.ao.AOConfigProperties"
   };
   
   private static final String[] OBJECT_RULES = {
      "org.jboss.jca.deployers.common.validator.rules.mc.MCGetMetaData",
   };
   
   private static String[] allRules;
   static
   {
      List<String> arrayList = new ArrayList<String>();
      arrayList.addAll(Arrays.asList(CLASS_RULES));
      arrayList.addAll(Arrays.asList(OBJECT_RULES)); 
      allRules = arrayList.toArray(new String[CLASS_RULES.length + OBJECT_RULES.length]);
   };

   /**
    * Constructor
    */
   public Validator()
   {
   }

   /**
    * Get the resource bundle
    * @return The resource bundle
    */
   public ResourceBundle getResourceBundle()
   {
      return ResourceBundle.getBundle("validator", Locale.US, Validator.class.getClassLoader());
   }

   /**
    * Validate
    * @param objects Objects that should be validated
    * @return The list of failures; <code>null</code> if no errors
    */
   public List<Failure> validate(Validate[] objects)
   {
      if (objects == null || objects.length == 0)
         return null;

      List<Rule> rules = new ArrayList<Rule>(allRules.length);

      for (int i = 0; i < allRules.length; i++)
      {
         try
         {
            Class clz = Class.forName(allRules[i], true, Validator.class.getClassLoader());
            Rule rule = (Rule)clz.newInstance();

            rules.add(rule);
         }
         catch (Throwable t)
         {
            throw new IllegalArgumentException(allRules[i], t);
         }
      }

      ResourceBundle resourceBundle = getResourceBundle();

      List<Failure> result = null;

      for (Rule rule : rules)
      {
         for (Validate obj : objects)
         {
            List<Failure> failures = rule.validate(obj, resourceBundle);

            if (failures != null)
            {
               if (result == null)
                  result = new ArrayList<Failure>();

               result.addAll(failures);
            }
         }
      }

      return result;
   }
}
