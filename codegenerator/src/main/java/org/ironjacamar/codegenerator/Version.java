/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.codegenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * JCA Version
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public enum Version
{

   /**
    * Unknown
    */
   UNKNOWN(null),
   /**
    * 1.0
    */
   V_10("1.0"),
   /**
    * 1.5
    */
   V_15("1.5"),
   /**
    * 1.6
    */
   V_16("1.6"),
   /**
    * 1.7
    */
   V_17("1.7");

   private String name;

   /**
    * Constructor
    *
    * @param name a name
    */
   Version(String name)
   {
      this.name = name;
   }

   /**
    * Get the name
    *
    * @return The value
    */
   public String getLocalName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return name;
   }

   private static final Map<String, Version> MAP;

   static
   {
      final Map<String, Version> map = new HashMap<String, Version>();
      for (Version element : values())
      {
         final String name = element.getLocalName();
         if (name != null)
            map.put(name, element);
      }
      MAP = map;
   }

   /**
    * Set the value
    *
    * @param v The name
    * @return The value
    */
   Version value(String v)
   {
      name = v;
      return this;
   }

   /**
    * Static method to get enum instance given localName XsdString
    *
    * @param localName a XsdString used as localname (typically tag name as defined in xsd)
    * @return the enum instance
    */
   public static Version forName(String localName)
   {
      final Version element = MAP.get(localName);
      return element == null ? UNKNOWN.value(localName) : element;
   }
}
