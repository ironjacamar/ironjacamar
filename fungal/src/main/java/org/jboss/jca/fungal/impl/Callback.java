/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.impl;

import java.lang.reflect.Method;

/**
 * The data structure for callbacks
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Callback
{
   /** The type */
   private Class<?> type;

   /** The method */
   private Method method;

   /** The object instance */
   private Object instance;

   /** Hash code */
   private transient Integer hashCode;

   /**
    * Constructor
    * @param type The registration type
    * @param method The method that should be invoked
    * @param instance The object instance where the method is located
    */
   public Callback(Class<?> type,
                   Method method, 
                   Object instance)
   {
      if (type == null)
         throw new IllegalArgumentException("Type is null");

      if (method == null)
         throw new IllegalArgumentException("Method is null");

      if (instance == null)
         throw new IllegalArgumentException("Instance is null");

      this.type = type;
      this.method = method;
      this.instance = instance;
      this.hashCode = null;
   }

   /**
    * Get the type
    * @return The value
    */
   public Class<?> getType()
   {
      return type;
   }

   /**
    * Get the method
    * @return The value
    */
   public Method getMethod()
   {
      return method;
   }

   /**
    * Get the instance
    * @return The value
    */
   public Object getInstance()
   {
      return instance;
   }

   /**
    * Hash code
    * @return The value
    */
   public int hashCode()
   {
      if (hashCode == null)
      {
         int result = 7;
         
         result += 7 * type.hashCode();
         result += 7 * method.hashCode();
         result += 7 * instance.hashCode();
         
         hashCode = Integer.valueOf(result);
      }

      return hashCode.intValue();
   }

   /**
    * Equality
    * @param obj The other object
    * @return True if equal; otherwise false
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;

      if (this == obj)
         return true;

      if (!(obj instanceof Callback))
         return false;

      Callback cb = (Callback)obj;

      boolean result = type.equals(cb.getType());

      if (result)
         result = method.equals(cb.getMethod());

      if (result)
         result = instance.equals(cb.getInstance());

      return result;
   }

   /**
    * String representation
    * @return The value
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder("Callback[");
      sb = sb.append("Type=" + type + ",");
      sb = sb.append("Method=" + method + ",");
      sb = sb.append("Instance=" + instance);
      sb = sb.append("]");

      return sb.toString();
   }
}
