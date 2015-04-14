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

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityDecrementer;
import org.jboss.jca.core.connectionmanager.pool.api.CapacityIncrementer;
import org.jboss.jca.core.util.Injection;

import java.util.Map;

import org.jboss.logging.Logger;

/**
 * The capacity factory
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class CapacityFactory
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, CapacityFactory.class.getName());

   /**
    * Constructor
    */
   private CapacityFactory()
   {
   }

   /**
    * Create a capacity instance based on the metadata
    * @param metadata The metadata
    * @param isCRI Is the pool CRI based
    * @return The instance
    */
   public static org.jboss.jca.core.connectionmanager.pool.api.Capacity
   create(org.jboss.jca.common.api.metadata.common.Capacity metadata,
          boolean isCRI)
   {
      if (metadata == null)
         return DefaultCapacity.INSTANCE;

      CapacityIncrementer incrementer = null;
      CapacityDecrementer decrementer = null;

      // Incrementer
      if (metadata.getIncrementer() != null && metadata.getIncrementer().getClassName() != null)
      {
         incrementer = loadIncrementer(metadata.getIncrementer().getClassName());

         if (incrementer != null)
         {
            if (metadata.getIncrementer().getConfigPropertiesMap().size() > 0)
            {
               Injection injector = new Injection();

               Map<String, String> properties = metadata.getIncrementer().getConfigPropertiesMap();
               for (Map.Entry<String, String> property : properties.entrySet())
               {
                  try
                  {
                     injector.inject(incrementer, property.getKey(), property.getValue());
                  }
                  catch (Throwable t)
                  {
                     log.invalidCapacityOption(property.getKey(),
                                               property.getValue(), incrementer.getClass().getName());
                  }
               }
            }
         }
         else
         {
            log.invalidCapacityIncrementer(metadata.getIncrementer().getClassName());
         }
      }

      if (incrementer == null)
         incrementer = DefaultCapacity.DEFAULT_INCREMENTER;

      // Decrementer
      if (metadata.getDecrementer() != null && metadata.getDecrementer().getClassName() != null)
      {
         if (!isCRI)
         {
            decrementer = loadDecrementer(metadata.getDecrementer().getClassName());

            if (decrementer != null)
            {
               if (metadata.getDecrementer().getConfigPropertiesMap().size() > 0)
               {
                  Injection injector = new Injection();

                  Map<String, String> properties = metadata.getDecrementer().getConfigPropertiesMap();
                  for (Map.Entry<String, String> property : properties.entrySet())
                  {
                     try
                     {
                        injector.inject(decrementer, property.getKey(), property.getValue());
                     }
                     catch (Throwable t)
                     {
                        log.invalidCapacityOption(property.getKey(),
                                                  property.getValue(), decrementer.getClass().getName());
                     }
                  }
               }
            }
            else
            {
               log.invalidCapacityDecrementer(metadata.getDecrementer().getClassName());
            }
         }
         else
         {
            // Explicit allow TimedOutDecrementer, MinPoolSizeDecrementer and SizeDecrementer for CRI based pools
            if (TimedOutDecrementer.class.getName().equals(metadata.getDecrementer().getClassName()) ||
                TimedOutFIFODecrementer.class.getName().equals(metadata.getDecrementer().getClassName()) ||
                MinPoolSizeDecrementer.class.getName().equals(metadata.getDecrementer().getClassName()) ||
                SizeDecrementer.class.getName().equals(metadata.getDecrementer().getClassName()))
            {
               decrementer = loadDecrementer(metadata.getDecrementer().getClassName());

               if (metadata.getDecrementer().getConfigPropertiesMap().size() > 0)
               {
                  Injection injector = new Injection();

                  Map<String, String> properties = metadata.getDecrementer().getConfigPropertiesMap();
                  for (Map.Entry<String, String> property : properties.entrySet())
                  {
                     try
                     {
                        injector.inject(decrementer, property.getKey(), property.getValue());
                     }
                     catch (Throwable t)
                     {
                        log.invalidCapacityOption(property.getKey(),
                                                  property.getValue(), decrementer.getClass().getName());
                     }
                  }
               }
            }
            else
            {
               log.invalidCapacityDecrementer(metadata.getDecrementer().getClassName());
            }
         }
      }

      if (decrementer == null)
         decrementer = DefaultCapacity.DEFAULT_DECREMENTER;

      return new ExplicitCapacity(incrementer, decrementer);
   }

   /**
    * Load the incrementer
    * @param clz The incrementer class name
    * @return The incrementer
    */
   private static CapacityIncrementer loadIncrementer(String clz)
   {
      Object result = loadClass(clz);

      if (result != null && result instanceof CapacityIncrementer)
      {
         return (CapacityIncrementer)result;
      }

      log.debugf("%s wasn't a CapacityIncrementer", clz);

      return null;
   }

   /**
    * Load the decrementer
    * @param clz The decrementer class name
    * @return The decrementer
    */
   private static CapacityDecrementer loadDecrementer(String clz)
   {
      Object result = loadClass(clz);

      if (result != null && result instanceof CapacityDecrementer)
      {
         return (CapacityDecrementer)result;
      }

      log.debugf("%s wasn't a CapacityDecrementer", clz);

      return null;
   }

   /**
    * Load the class
    * @param clz The class name
    * @return The object
    */
   private static Object loadClass(String clz)
   {
      try
      {
         Class<?> c = Class.forName(clz, true, SecurityActions.getClassLoader(CapacityFactory.class));
         return c.newInstance();
      }
      catch (Throwable t)
      {
         log.tracef("Throwable while loading %s using own classloader: %s", clz, t.getMessage());
      }

      try
      {
         Class<?> c = Class.forName(clz, true, SecurityActions.getThreadContextClassLoader());
         return c.newInstance();
      }
      catch (Throwable t)
      {
         log.tracef("Throwable while loading %s using TCCL: %s", clz, t.getMessage());
      }

      return null;
   }
}
