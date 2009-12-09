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

import java.util.List;

import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;

/**
 * Object wrapper for objects that should be validated
 */
public class ValidateObject
{
   /** Key */
   private int key;

   /** Onject */
   private Object object;

   /** config-property */
   private List<ConfigPropertyMetaData> configProperties;

   /**
    * Constructor
    * @param key The key
    * @param object The key
    */
   public ValidateObject(int key,
                         Object object)
   {
      this(key, object, null);
   }
   
   /**
    * Constructor
    * @param key The key
    * @param object The key
    * @param configProperties The list of config property metadata
    */
   public ValidateObject(int key,
                         Object object,
                         List<ConfigPropertyMetaData> configProperties)
   {
      this.key = key;
      this.object = object;
      this.configProperties = configProperties;
   }
   
   /**
    * Get the key
    * @return The key
    */
   public int getKey()
   {
      return key;
   }

   /**
    * Get the object
    * @return The object
    */
   public Object getObject()
   {
      return object;
   }

   /**
    * Get the list of config properties
    * @return The list
    */
   public List<ConfigPropertyMetaData> getConfigProperties()
   {
      return configProperties;
   }
}
