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


package org.ironjacamar.validator;

import org.ironjacamar.validator.rules.ao.AOConfigProperties;
import org.ironjacamar.validator.rules.ao.AOConstructor;
import org.ironjacamar.validator.rules.ao.AONull;
import org.ironjacamar.validator.rules.ao.AORAA;
import org.ironjacamar.validator.rules.as.AS;
import org.ironjacamar.validator.rules.as.ASConfigProperties;
import org.ironjacamar.validator.rules.as.ASConstructor;
import org.ironjacamar.validator.rules.as.ASNull;
import org.ironjacamar.validator.rules.cf.CFConstructor;
import org.ironjacamar.validator.rules.cf.CFNull;
import org.ironjacamar.validator.rules.cf.CFReferenceable;
import org.ironjacamar.validator.rules.cf.CFSerializable;
import org.ironjacamar.validator.rules.mc.MC;
import org.ironjacamar.validator.rules.mc.MCGetMetaData;
import org.ironjacamar.validator.rules.mcf.MCF;
import org.ironjacamar.validator.rules.mcf.MCFConfigProperties;
import org.ironjacamar.validator.rules.mcf.MCFConstructor;
import org.ironjacamar.validator.rules.mcf.MCFEquals;
import org.ironjacamar.validator.rules.mcf.MCFHashCode;
import org.ironjacamar.validator.rules.mcf.MCFNull;
import org.ironjacamar.validator.rules.ra.RA;
import org.ironjacamar.validator.rules.ra.RAConfigProperties;
import org.ironjacamar.validator.rules.ra.RAConstructor;
import org.ironjacamar.validator.rules.ra.RAEquals;
import org.ironjacamar.validator.rules.ra.RAHashCode;
import org.ironjacamar.validator.rules.ra.RANull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
   private static final Rule[] CLASS_RULES = {
      new MCFNull(),
      new MCF(),
      new MCFConstructor(),
      new MCFHashCode(),
      new MCFEquals(),
      new MCFConfigProperties(),
      new MC(),
      new MCGetMetaData(),
      new RANull(),
      new RA(),
      new RAConstructor(),
      new RAHashCode(),
      new RAEquals(),
      new RAConfigProperties(),
      new CFConstructor(),
      new CFNull(),
      new CFSerializable(),
      new CFReferenceable(),
      new ASNull(),
      new AS(),
      new ASConstructor(),
      new ASConfigProperties(),
      new AONull(),
      new AOConstructor(),
      new AOConfigProperties(),
      new AORAA()
   };

   private static final Rule[] OBJECT_RULES = {
      new MCGetMetaData(),
   };

   private static Rule[] allRules;
   static
   {
      List<Rule> arrayList = new ArrayList<>();
      arrayList.addAll(Arrays.asList(CLASS_RULES));
      arrayList.addAll(Arrays.asList(OBJECT_RULES));
      allRules = arrayList.toArray(new Rule[CLASS_RULES.length + OBJECT_RULES.length]);
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
      return ResourceBundle.getBundle("validator", Locale.US,
                                      SecurityActions.getClassLoader(Validator.class));
   }

   /**
    * Validate
    *
    * @param objects Objects that should be validated
    * @return The list of failures; <code>null</code> if no errors
    */
   public List<Failure> validate(List<Validate> objects)
   {
      if (objects == null || objects.isEmpty())
         return null;

      return execRulesOnValidates(objects);
   }

   /**
    * exec rules
    * @param objects to be validated
    * @return The list of failures; an Empty list if no errors
    */
   private List<Failure> execRulesOnValidates(List<Validate> objects)
   {
      ResourceBundle resourceBundle = getResourceBundle();

      List<Failure> result = null;

      for (Rule rule : allRules)
      {
         for (Validate obj : objects)
         {
            List<Failure> failures = rule.validate(obj, resourceBundle);

            if (failures != null)
            {
               if (result == null)
               {
                  result = new LinkedList<>();
               }
               result.addAll(failures);
            }
         }
      }

      return result;
   }
}
