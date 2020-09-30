/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.api.management;

/**
 * Represents a config property
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConfigProperty
{
   /** The name */
   private String name;

   /** The dynamic flag */
   private boolean dynamic;

   /** The confidential flag */
   private boolean confidential;

   /**
    * Constructor
    * @param name The name
    */
   public ConfigProperty(String name)
   {
      this(name, false, false);
   }

   /**
    * Constructor
    * @param name The name
    * @param dynamic Does this config property support dynamic updates
    * @param confidential Is this config property confidential
    */
   public ConfigProperty(String name, boolean dynamic, boolean confidential)
   {
      this.name = name;
      this.dynamic = dynamic;
      this.confidential = confidential;
   }

   /**
    * Get the name
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the dynamic flag
    * @return The value
    */
   public boolean isDynamic()
   {
      return dynamic;
   }

   /**
    * Get the confidential flag
    * @return The value
    */
   public boolean isConfidential()
   {
      return confidential;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ConfigProperty@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[name=").append(name);
      sb.append(" dynamic=").append(dynamic);
      sb.append(" confidential=").append(confidential);
      sb.append("]");

      return sb.toString();
   }
}
