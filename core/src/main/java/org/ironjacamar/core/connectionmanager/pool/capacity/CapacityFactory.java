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

import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.api.connectionmanager.pool.CapacityDecrementer;
import org.ironjacamar.core.api.connectionmanager.pool.CapacityIncrementer;
import org.ironjacamar.core.spi.classloading.ClassLoaderPlugin;
import org.ironjacamar.core.util.Injection;

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
    * @param classLoaderPlugin the ClassLoaderPlugin used to load incrementer/decrementer classes
    * @return The instance
    */
   public static org.ironjacamar.core.connectionmanager.pool.Capacity create(
         org.ironjacamar.common.api.metadata.common.Capacity metadata,
         ClassLoaderPlugin classLoaderPlugin)
   {
      if (metadata == null)
         return DefaultCapacity.INSTANCE;

      CapacityIncrementer incrementer = null;
      CapacityDecrementer decrementer = null;

      // Incrementer
      if (metadata.getIncrementer() != null && metadata.getIncrementer().getClassName() != null)
      {
         incrementer = loadIncrementer(metadata.getIncrementer(), classLoaderPlugin);

         if (incrementer != null)
         {
            injectProperties(metadata.getIncrementer().getConfigPropertiesMap(), incrementer);
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

         decrementer = loadDecrementer(metadata.getDecrementer(), classLoaderPlugin);

         if (decrementer != null)
         {
            injectProperties(metadata.getDecrementer().getConfigPropertiesMap(), decrementer);

         }
         else
         {
            // Explicit allow TimedOutDecrementer, MinPoolSizeDecrementer and SizeDecrementer for CRI based pools
            if (TimedOutDecrementer.class.getName().equals(metadata.getDecrementer().getClassName()) ||
                TimedOutFIFODecrementer.class.getName().equals(metadata.getDecrementer().getClassName()) ||
                MinPoolSizeDecrementer.class.getName().equals(metadata.getDecrementer().getClassName()) ||
                SizeDecrementer.class.getName().equals(metadata.getDecrementer().getClassName()))
            {
               decrementer = loadDecrementer(metadata.getDecrementer(), classLoaderPlugin);

               injectProperties(metadata.getDecrementer().getConfigPropertiesMap(), decrementer);
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


   private static void injectProperties(Map<String, String> properties, Object decrementer)
   {
      if (!properties.isEmpty())
      {
         Injection injector = new Injection();

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

   /**
    * Load the incrementer
    * @param incrementer The incrementer metadata to load as class instance
    * @param classLoaderPlugin class loader plugin to use to load class
    * @return The incrementer
    */
   private static CapacityIncrementer loadIncrementer(Extension incrementer, ClassLoaderPlugin classLoaderPlugin)
   {
      Object result = loadExtension(incrementer, classLoaderPlugin);

      if (result != null && result instanceof CapacityIncrementer)
      {
         return (CapacityIncrementer)result;
      }

      log.debugf("%s wasn't a CapacityIncrementer", incrementer.getClassName());

      return null;
   }

   /**
    * Load the decrementer
    * @param decrementer The incrementer metadata to load as class instance
    * @param classLoaderPlugin class loader plugin to use to load class
    * @return The decrementer
    */
   private static CapacityDecrementer loadDecrementer(Extension decrementer, ClassLoaderPlugin classLoaderPlugin)
   {
      Object result = loadExtension(decrementer, classLoaderPlugin);

      if (result != null && result instanceof CapacityDecrementer)
      {
         return (CapacityDecrementer)result;
      }

      log.debugf("%s wasn't a CapacityDecrementer", decrementer.getClassName());

      return null;
   }

   /**
    * Load the class
    * @param extension The extension metadata to load as class instance
    * @param classLoaderPlugin class loader plugin to use to load class
    * @return The object
    */
   private static Object loadExtension(Extension extension, ClassLoaderPlugin classLoaderPlugin)
   {
      try
      {
         Class<?> c = classLoaderPlugin.loadClass(extension.getClassName(),
               extension.getModuleName(), extension.getModuleSlot());
         return c.newInstance();
      }
      catch (Throwable t)
      {
         log.tracef("Throwable while loading %s using own classloader: %s", extension.getClassName(), t.getMessage());
      }

      return null;
   }
}
