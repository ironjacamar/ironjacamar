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

package org.jboss.jca.core.workmanager;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.workmanager.Address;

import org.jboss.logging.Logger;

/**
 * A WorkManager event
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkManagerEvent
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           WorkManagerEvent.class.getName());

   /** JOIN */
   public static final int TYPE_JOIN = 0;

   /** LEAVE */
   public static final int TYPE_LEAVE = 1;

   /** UPDATE_SHORT_RUNNING */
   public static final int TYPE_UPDATE_SHORT_RUNNING = 2;

   /** UPDATE_LONG_RUNNING */
   public static final int TYPE_UPDATE_LONG_RUNNING = 3;

   /** The type */
   private int type;

   /** The address */
   private Address address;

   /** The value */
   private long value;

   /**
    * Constructor
    * @param type The type
    * @param address The address
    */
   public WorkManagerEvent(int type, Address address)
   {
      this(type, address, 0L);
   }

   /**
    * Constructor
    * @param type The type
    * @param address The address
    * @param value The value for the type
    */
   public WorkManagerEvent(int type, Address address, long value)
   {
      this.type = type;
      this.address = address;
      this.value = value;
   }

   /**
    * Get the type
    * @return The value
    */
   public int getType()
   {
      return type;
   }

   /**
    * Get the address
    * @return The value
    */
   public Address getAddress()
   {
      return address;
   }

   /**
    * Get the value
    * @return The value
    */
   public long getValue()
   {
      return value;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;

      result += 7 * type;
      result += 7 * address.hashCode();

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object o)
   {
      if (o == this)
         return true;

      if (o == null)
         return false;

      if (!(o instanceof WorkManagerEvent))
         return false;

      WorkManagerEvent wme = (WorkManagerEvent)o;

      if (type != wme.getType())
         return false;

      if (!address.equals(wme.getAddress()))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("WorkManagerEvent@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[type=").append(type);
      sb.append(" address=").append(address);
      sb.append(" value=").append(value);
      sb.append("]");

      return sb.toString();
   }
}
