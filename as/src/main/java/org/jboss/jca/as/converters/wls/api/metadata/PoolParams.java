/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.converters.wls.api.metadata;

import java.util.HashMap;
import java.util.Map;
   
/**
 *
 * A generic PoolParams.
 *
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 *
 */
public interface PoolParams extends WlsMetadata
{
   /**
    * getInitialCapacity
    * 
    * @return the InitialCapacity
    */
   public Integer getInitialCapacity();
   
   /**
    * getMaxCapacity
    * 
    * @return the MaxCapacity
    */
   public Integer getMaxCapacity();
   
   /**
    * getCapacityIncrement
    * 
    * @return the CapacityIncrement
    */
   public Boolean getCapacityIncrement();
   
   /**
    * getShrinkingEnabled
    * 
    * @return the ShrinkingEnabled
    */
   public Boolean getShrinkingEnabled();
   
   /**
    * getShrinkFrequencySeconds
    * 
    * @return the ShrinkFrequencySeconds
    */
   public Integer getShrinkFrequencySeconds();
   
   /**
    * getHighestNumWaiters
    * 
    * @return the HighestNumWaiters
    */
   public Integer getHighestNumWaiters();
   
   /**
    * getHighestNumUnavailable
    * 
    * @return the HighestNumUnavailable
    */
   public Integer getHighestNumUnavailable();
   
   /**
    * getConnectionCreationRetryFrequencySeconds
    * 
    * @return the ConnectionCreationRetryFrequencySeconds
    */
   public Integer getConnectionCreationRetryFrequencySeconds();
   
   /**
    * getConnectionReserveTimeoutSeconds
    * 
    * @return the ConnectionReserveTimeoutSeconds
    */
   public Integer getConnectionReserveTimeoutSeconds();
   
   /**
    * getTestFrequencySeconds
    * 
    * @return the TestFrequencySeconds
    */
   public Integer getTestFrequencySeconds();
   
   /**
    * getTestConnectionsOnCreate
    * 
    * @return the TestConnectionsOnCreate
    */
   public Boolean getTestConnectionsOnCreate();
   
   /**
    * getTestConnectionsOnRelease
    * 
    * @return the TestConnectionsOnRelease
    */
   public Boolean getTestConnectionsOnRelease();
   
   /**
    * getTestConnectionsOnReserve
    * 
    * @return the TestConnectionsOnReserve
    */
   public Boolean getTestConnectionsOnReserve();
   
   /**
    * getProfileHarvestFrequencySeconds
    * 
    * @return the ProfileHarvestFrequencySeconds
    */
   public Integer getProfileHarvestFrequencySeconds();
   
   /**
    * getIgnoreInUseConnectionsEnabled
    * 
    * @return the IgnoreInUseConnectionsEnabled
    */
   public Boolean getIgnoreInUseConnectionsEnabled();
   
   /**
    * getMatchConnectionsSupported
    * 
    * @return the MatchConnectionsSupported
    */
   public Boolean getMatchConnectionsSupported();
   
   /**
    * getUseFirstAvailable
    * 
    * @return the UseFirstAvailable
    */
   public Boolean getUseFirstAvailable();
   
   
   /**
   *
   * A Tag.
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * initial-capacity tag
       */
      INITIAL_CAPACITY("initial-capacity"),

      /**
       * max-capacity tag
       */
      MAX_CAPACITY("max-capacity"),

      /**
       * capacity-increment tag
       */
      CAPACITY_INCREMENT("capacity-increment"),

      /**
       * shrinking-enabled tag
       */
      SHRINKING_ENABLED("shrinking-enabled"),

      /**
       * shrink-frequency-seconds tag
       */
      SHRINK_FREQUENCY_SECONDS("shrink-frequency-seconds"),

      /**
       * highest-num-waiters tag
       */
      HIGHEST_NUM_WAITERS("highest-num-waiters"),

      /**
       * highest-num-unavailable tag
       */
      HIGHEST_NUM_UNAVILABE("highest-num-unavailable"),

      /**
       * connection-creation-retry-frequency-seconds tag
       */
      CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS("connection-creation-retry-frequency-seconds"),

      /**
       * connection-reserve-timeout-seconds tag
       */
      CONNECTION_RESERVE_TIMWOUT_SECONDS("connection-reserve-timeout-seconds"),

      /**
       * test-frequency-seconds tag
       */
      TEST_FREQUENCY_SECONDS("test-frequency-seconds"),

      /**
       * test-connections-on-create tag
       */
      TEST_CONNECTION_ON_CREATE("test-connections-on-create"),

      /**
       * test-connections-on-release tag
       */
      TEST_CONNECTION_ON_RELEASE("test-connections-on-release"),

      /**
       * test-connections-on-reserve tag
       */
      TEST_CONNECTION_ON_RESERVE("test-connections-on-reserve"),

      /**
       * profile-harvest-frequency-seconds tag
       */
      PROFILE_HARVEST_FREQUENCY_SECONDS("profile-harvest-frequency-seconds"),

      /**
       * ignore-in-use-connections-enabled tag
       */
      IGNORE_IN_USE_CONNECTION_ENABLED("ignore-in-use-connections-enabled"),

      /**
       * match-connections-supported tag
       */
      MATCH_CONNECTIONS_SUPPORTED("match-connections-supported"),

      /**
       * use-first-available tag
       */
      USE_FIRST_AVAILABLE("use-first-available");

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

   /**
    *
    * A Attribute.
    *
    */
   public enum Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Attribute(final String name)
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

      private static final Map<String, Attribute> MAP;

      static
      {
         final Map<String, Attribute> map = new HashMap<String, Attribute>();
         for (Attribute element : values())
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
      Attribute value(String v)
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
      public static Attribute forName(String localName)
      {
         final Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }
}
