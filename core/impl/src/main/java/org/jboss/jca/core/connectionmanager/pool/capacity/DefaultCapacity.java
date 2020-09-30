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

import org.jboss.jca.core.connectionmanager.pool.api.Capacity;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityDecrementer;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityIncrementer;

/**
 * The default capacity policy
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DefaultCapacity implements Capacity
{
   /** The instance */
   public static final Capacity INSTANCE = new DefaultCapacity();

   /** The default incrementer */
   public static final CapacityIncrementer DEFAULT_INCREMENTER = null;

   /** The default decrementer */
   public static final CapacityDecrementer DEFAULT_DECREMENTER = new TimedOutDecrementer();

   /**
    * Constructor
    */
   private DefaultCapacity()
   {
   }

   /**
    * {@inheritDoc}
    */
   public CapacityIncrementer getIncrementer()
   {
      return DEFAULT_INCREMENTER;
   }

   /**
    * {@inheritDoc}
    */
   public CapacityDecrementer getDecrementer()
   {
      return DEFAULT_DECREMENTER;
   }
}
