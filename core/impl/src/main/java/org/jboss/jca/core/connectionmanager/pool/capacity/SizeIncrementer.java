/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.pool.capacity;

import org.jboss.jca.core.connectionmanager.pool.api.CapacityIncrementer;

/**
 * Increment until the defined number of connection has been created.
 *
 * Default value is 1
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SizeIncrementer implements CapacityIncrementer
{
   /** Size */
   private int size;

   /**
    * Constructor
    */
   public SizeIncrementer()
   {
      this.size = 1;
   }

   /**
    * Set the size
    * @param v The value
    */
   public void setSize(int v)
   {
      if (v > 0)
         size = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean shouldCreate(int currentSize, int maxSize, int created)
   {
      return size > created;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return getClass().getName() + "(" + size + ")";
   }
}
