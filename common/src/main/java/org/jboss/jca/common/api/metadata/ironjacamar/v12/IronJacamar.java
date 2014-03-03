/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.api.metadata.ironjacamar.v12;

import java.util.HashMap;
import java.util.Map;

/**
 * IronJacamar v1.2
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface IronJacamar extends org.jboss.jca.common.api.metadata.ironjacamar.v11.IronJacamar
{
   /**
    *
    * A Tag.
    *
    * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
    */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /** config-property tag
      *
      */
      CONFIG_PROPERTY("config-property"),

      /**
       * bean-validation-groups tag
       */
      BEAN_VALIDATION_GROUPS("bean-validation-groups"),

      /**
       * bean-validation-group tag
       */
      BEAN_VALIDATION_GROUP("bean-validation-group"),

      /**
       * bootstrap-context tag
       */
      BOOTSTRAP_CONTEXT("bootstrap-context"),

      /**
       * transaction-support tag
       */
      TRANSACTION_SUPPORT("transaction-support"),

      /**
       * workmanager tag
       */
      WORKMANAGER("workmanager"),

      /**
       * connection-definitions tag
       */
      CONNECTION_DEFINITIONS("connection-definitions"),

      /**
       * connection-definition tag
       */
      CONNECTION_DEFINITION("connection-definition"),

      /**
       * admin-objects tag
       */
      ADMIN_OBJECTS("admin-objects"),

      /**
       * admin-objects tag
       */
      ADMIN_OBJECT("admin-object");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
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

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
       * Set the value
       * @param v The name
       * @return The value
       */
      Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }
}

