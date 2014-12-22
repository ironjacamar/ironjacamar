/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.api.metadata.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Flush strategy for the pools
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public enum FlushStrategy
{
   /**
    * Unknown
    */
   UNKNOWN(null),

   /**
    * Failing connection only (default)
    */
   FAILING_CONNECTION_ONLY("FailingConnectionOnly"),

   /**
    * Invalid idle connections
    */
   INVALID_IDLE_CONNECTIONS("InvalidIdleConnections"),

   /**
    * Idle connections
    */
   IDLE_CONNECTIONS("IdleConnections"),

   /**
    * Gracefully
    */
   GRACEFULLY("Gracefully"),

   /**
    * Entire pool
    */
   ENTIRE_POOL("EntirePool"),

   /**
    * All invalid idle connections
    */
   ALL_INVALID_IDLE_CONNECTIONS("AllInvalidIdleConnections"),

   /**
    * All idle connections
    */
   ALL_IDLE_CONNECTIONS("AllIdleConnections"),

   /**
    * All gracefully
    */
   ALL_GRACEFULLY("AllGracefully"),

   /**
    * All connections
    */
   ALL_CONNECTIONS("AllConnections");
   
   private final String name;

   /**
    * Create a new FlushStrategy.
    * @param name The value
    */
   FlushStrategy(final String name)
   {
      this.name = name;
   }

   /**
    * Get the name of this element.
    * @return The value
    */
   public String getName()
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

   private static final Map<String, FlushStrategy> MAP;
   
   static
   {
      final Map<String, FlushStrategy> map = new HashMap<String, FlushStrategy>();
      for (FlushStrategy element : values())
      {
         final String name = element.getName();
         if (name != null)
            map.put(name, element);
      }
      MAP = map;
   }

   /**
    * Static method to get enum instance given localName XsdString
    * @param name The strategy name
    * @return The enum instance
    */
   public static FlushStrategy forName(String name)
   {
      final FlushStrategy element = MAP.get(name);
      return element == null ? UNKNOWN : element;
   }
}
