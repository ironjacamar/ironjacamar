/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.core.connectionmanager.pool.capacity;

import org.ironjacamar.core.api.connectionmanager.pool.CapacityDecrementer;
import org.ironjacamar.core.api.connectionmanager.pool.CapacityIncrementer;
import org.ironjacamar.core.connectionmanager.pool.Capacity;

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
