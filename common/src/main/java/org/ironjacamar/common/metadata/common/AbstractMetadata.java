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
package org.ironjacamar.common.metadata.common;

import org.ironjacamar.common.api.metadata.JCAMetadata;

import java.util.Map;

/**
 * An abstract metadata node
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractMetadata implements JCAMetadata
{
   /** The key -> value map */
   private Map<String, String> m;

   /**
    * Constructor
    * @param m The map
    */
   public AbstractMetadata(Map<String, String> m)
   {
      this.m = m;
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasExpression(String key)
   {
      if (m == null)
         return false;
      
      return m.containsKey(key);
   }

   /**
    * {@inheritDoc}
    */
   public String getValue(String key, String v)
   {
      return getValue(key, null, v);
   }

   /**
    * {@inheritDoc}
    */
   public String getValue(String key, String subkey, String v)
   {
      if (m == null)
         return v;
      
      String k = key;

      if (subkey != null)
      {
         if (subkey.startsWith("${"))
         {
            subkey = subkey.substring(2, subkey.length() - 1);

            if (subkey.indexOf(":") != -1)
               subkey = subkey.substring(0, subkey.indexOf(":"));
         }

         k = k + "|" + subkey;
      }

      String value = m.get(k);

      if (value != null)
      {
         if (value.startsWith("${") && v != null && value.indexOf(":") != -1)
         {
            return value.substring(0, value.indexOf(":") + 1)  + v + "}";
         }
         return value;
      }
      return v;
   }
}
