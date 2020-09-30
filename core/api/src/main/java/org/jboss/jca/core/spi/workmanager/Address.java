/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.spi.workmanager;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines an address of a WorkManager
 */
public final class Address implements Comparable<Address>, Serializable
{
   /** Serial version UID */
   private static final long serialVersionUID = 2L;

   /** The id of the WorkManager */
   private String workManagerId;

   /** The name of the WorkManager */
   private String workManagerName;

   /** The id of the Transport */
   private String transportId;

   private static final String ID = "ID";
   private static final String NAME = "NAME";
   private static final String TRANSPORT_ID = "TRANSPORT_ID";

   /**
    * create an instance from a Map
    * @param map the map
    * @return the instance
    */
   public static Address fromMap(Map<String, String> map)
   {
      return new Address(map.get(ID), map.get(NAME), map.get(TRANSPORT_ID));

   }

   /**
    * return a map representing the instance
    * @return the map
    */
   public Map<String, String> toMap()
   {
      Map<String, String> returnMap = new LinkedHashMap<String, String>(3);
      returnMap.put(ID, this.getWorkManagerId());
      returnMap.put(NAME, this.getWorkManagerName());
      returnMap.put(TRANSPORT_ID, this.getTransportId());
      return returnMap;
   }

   /**
    * Constructor
    * @param workManagerId The id of the WorkManager
    * @param workManagerName The name of the WorkManager
    * @param transportId The id of the Transport
    */
   public Address(String workManagerId, String workManagerName, String transportId)
   {
      if (workManagerId == null || workManagerId.trim().equals(""))
         throw new IllegalArgumentException("WorkManagerId is undefined");
      
      if (workManagerName == null || workManagerName.trim().equals(""))
         throw new IllegalArgumentException("WorkManagerName is undefined");
      
      this.workManagerId = workManagerId;
      this.workManagerName = workManagerName;
      this.transportId = transportId;
   }

   /**
    * Get the WorkManager id
    * @return The value
    */
   public String getWorkManagerId()
   {
      return workManagerId;
   }

   /**
    * Get the WorkManager name
    * @return The value
    */
   public String getWorkManagerName()
   {
      return workManagerName;
   }

   /**
    * Get the Transport id
    * @return The value
    */
   public String getTransportId()
   {
      return transportId;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 37;

      result += 7 * workManagerId.hashCode();
      result += 7 * workManagerName.hashCode();
      result += transportId != null ? 7 * transportId.hashCode() : 7;

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object o)
   {
      if (this == o)
         return true;

      if (o == null || !(o instanceof Address))
         return false;

      Address a = (Address)o;

      if (!workManagerId.equals(a.workManagerId))
         return false;

      if (!workManagerName.equals(a.workManagerName))
         return false;

      if (transportId != null)
      {
         if (!transportId.equals(a.transportId))
            return false;
      }
      else
      {
         if (a.transportId != null)
            return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int compareTo(Address a)
   {
      int compare = workManagerId.compareTo(a.getWorkManagerId());
      if (compare != 0)
         return compare;

      compare = workManagerName.compareTo(a.getWorkManagerName());
      if (compare != 0)
         return compare;

      if (transportId != null)
      {
         if (a.getTransportId() != null)
         {
            return transportId.compareTo(a.getTransportId());
         }
         else
         {
            return 1;
         }
      }
      else
      {
         if (a.getTransportId() != null)
         {
            return -1;
         }
      }

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("Address@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[workManagerId=").append(workManagerId);
      sb.append(" workManagerName=").append(workManagerName);
      sb.append(" transportId=").append(transportId);
      sb.append("]");

      return sb.toString();
   }
}
