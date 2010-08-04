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
package org.jboss.jca.validator;

import org.jboss.jca.common.api.metadata.ra.ConfigProperty;

import java.util.List;

/**
 * ValidateClass for objects that should be validated
 */
public class ValidateClass implements Validate
{
   /** Key */
   private final Key key;

   /** config-property */
   private final List<? extends ConfigProperty> configProperties;

   /** Clazz */
   private Class<?> clazz;

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
                         List<? extends ConfigProperty> configProperties)
   {
      this.key = key;
      this.clazz = clazz;
      this.configProperties = configProperties;
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
      }
      catch (Exception cne)
      {
         this.clazz = null;
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
   public List<? extends ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }
}
