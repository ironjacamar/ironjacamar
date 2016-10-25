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

import org.ironjacamar.common.api.metadata.spec.ConfigProperty;

import java.util.Collection;
import java.util.List;




/**
 * ValidateClass for objects that should be validated
 */
public class ValidateClass implements Validate
{
   /** Key */
   private final Key key;

   /** config-property */
   private final Collection<? extends ConfigProperty> configProperties;

   /** Clazz */
   private Class<?> clazz;

   /** The class name */
   private String className;

   /** The class loader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param key The key
    * @param clazz The class
    */
   public ValidateClass(Key key,
                        Class<?> clazz)
   {
      this(key, clazz, null);
   }

   /**
    * Constructor
    * @param key The key
    * @param clazz The class
    * @param configProperties The list of config property metadata
    */
   public ValidateClass(Key key,
                        Class<?> clazz,
                        Collection<? extends ConfigProperty> configProperties)
   {
      this.key = key;
      this.clazz = clazz;
      this.configProperties = configProperties;
      this.className = null;
      this.cl = null;
   }

   /**
    * Constructor
    * @param key The key
    * @param className The class
    * @param cl the class loader used to load class
    * @param configProperties The list of config property metadata
    */
   public ValidateClass(Key key, String className, ClassLoader cl, List<? extends ConfigProperty> configProperties)
   {
      this.key = key;
      try
      {
         this.clazz = Class.forName(className, true, cl);
         this.className = null;
         this.cl = null;
      }
      catch (Exception cne)
      {
         this.clazz = null;
         this.className = className;
         this.cl = cl;
      }
      this.configProperties = configProperties;
   }

   /**
    * Get the key
    * @return The key
    */
   public Key getKey()
   {
      return key;
   }

   /**
    * Get the clazz
    * @return The clazz
    */
   public Class<?> getClazz()
   {
      return clazz;
   }

   /**
    * Get the list of config properties
    * @return The list
    */
   public Collection<? extends ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }

   /**
    * Get the class name.
    *
    * This property is set if a class can't be resolved
    * @return The value
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Get the class loader.
    *
    * This property is set if a class can't be resolved
    * @return The value
    */
   public ClassLoader getClassLoader()
   {
      return cl;
   }
}
