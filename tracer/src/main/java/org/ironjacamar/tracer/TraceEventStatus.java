/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.tracer;

/**
 * Trace event status
 */
public enum TraceEventStatus
{
   /** Green */
   GREEN("Green", "green"),

   /** Yellow */
   YELLOW("Yellow", "yellow"),

   /** Red */
   RED("Red", "red");

   private String description;
   private String color;

   /**
    * Constructor
    * @param desc The description
    * @param c The color
    */
   TraceEventStatus(String desc, String c)
   {
      this.description = desc;
      this.color = c;
   }

   /**
    * Get the description
    * @return The value
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Get the color
    * @return The value
    */
   public String getColor()
   {
      return color;
   }
}
