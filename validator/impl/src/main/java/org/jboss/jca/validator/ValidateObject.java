/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.spec.ConfigProperty;

import java.util.List;


/**
 * Object wrapper for objects that should be validated
 */
public class ValidateObject extends ValidateClass
{
   /** Onject */
   private final Object object;

   /**
    * Constructor
    * @param key The key
    * @param object The key
    */
   public ValidateObject(Key key,
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
   public ValidateObject(Key key,
                         Object object,
                         List<? extends ConfigProperty> configProperties)
   {
      super(key, object != null ? object.getClass() : null, configProperties);
      this.object = object;
   }

   /**
    * Get the object
    * @return The object
    */
   public Object getObject()
   {
      return object;
   }

}
