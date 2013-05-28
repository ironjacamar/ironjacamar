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
package org.jboss.jca.common.api.metadata.ds.v11;

import org.jboss.jca.common.api.metadata.common.CommonXaPool;

import java.util.HashMap;
import java.util.Map;

/**
 * A pool for an XA datasource.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface DsXaPool extends CommonXaPool
{
   /**
    * Are multiple users allowed to create connections
    * @return True, if multiple users are supported, otherwise false
    */
   public Boolean isAllowMultipleUsers();

   /**
   *
   * A Tag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * min-pool-size tag
       */
      MIN_POOL_SIZE("min-pool-size"),

      /**
      * maxPoolSize tag
      */
      MAX_POOL_SIZE("max-pool-size"),
      /**
      * prefill tag
      */
      PREFILL("prefill"),

      /**
       * use-strict-min tag
       */
      USE_STRICT_MIN("use-strict-min"),

      /**
       * flush-strategy tag
       */
      FLUSH_STRATEGY("flush-strategy"),

      /**
       * allow-multiple-users tag
       */
      ALLOW_MULTIPLE_USERS("allow-multiple-users"),

      /**
       * isSameRmOverrideValue tag
       */
      IS_SAME_RM_OVERRIDE("is-same-rm-override"),
      /**
      * interleaving tag
      */
      INTERLEAVING("interleaving"),
      /**
       * pad-xid tag
       */
      PAD_XID("pad-xid"),

      /**
       * wrap-xa-resource tag
       */
      WRAP_XA_RESOURCE("wrap-xa-resource"),

      /**
       * no-tx-separate-pools tag
       */
      NO_TX_SEPARATE_POOLS("no-tx-separate-pools");

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
      * Static method to get enum instance given localName XsdString
      *
      * @param localName a XsdString used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }
}
