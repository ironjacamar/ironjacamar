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

package org.ironjacamar.core.bootstrapcontext;

import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.workmanager.WorkManagerCoordinator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.logging.Logger;

/**
 * Coordinator for BootstrapContext instances
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class BootstrapContextCoordinator
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           BootstrapContextCoordinator.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The bootstrap contexts */
   private ConcurrentMap<String, CloneableBootstrapContext> bootstrapContexts;

   /** The default bootstrap context */
   private CloneableBootstrapContext defaultBootstrapContext;

   /** The activate bootstrap contexts */
   private Map<String, CloneableBootstrapContext> activeBootstrapContexts;

   /** The ref count for activate bootstrap contexts */
   private Map<String, Integer> refCountBootstrapContexts;

   private  WorkManagerCoordinator workManagerCoordinator;

   /**
    * Constructor
    */
   public BootstrapContextCoordinator()
   {
      this.bootstrapContexts = new ConcurrentHashMap<String, CloneableBootstrapContext>();
      this.defaultBootstrapContext = null;
      this.workManagerCoordinator = null;
      this.activeBootstrapContexts = new HashMap<String, CloneableBootstrapContext>();
      this.refCountBootstrapContexts = new HashMap<String, Integer>();
   }


   /**
    * Register bootstrap context
    * @param bc The bootstrap context
    */
   public void registerBootstrapContext(CloneableBootstrapContext bc)
   {
      if (bc != null)
      {
         if (bc.getName() == null || bc.getName().trim().equals(""))
            throw new IllegalArgumentException("The name of BootstrapContext is invalid: " + bc);

         if (!bootstrapContexts.keySet().contains(bc.getName()))
         {
            bootstrapContexts.put(bc.getName(), bc);
         }
      }
   }

   /**
    * Unregister boostrap context
    * @param bc The bootstrap context
    */
   public void unregisterBootstrapContext(CloneableBootstrapContext bc)
   {
      if (bc != null)
      {
         if (bc.getName() == null || bc.getName().trim().equals(""))
            throw new IllegalArgumentException("The name of BootstrapContext is invalid: " + bc);

         if (bootstrapContexts.keySet().contains(bc.getName()))
         {
            bootstrapContexts.remove(bc.getName());
         }
      }
   }

   /**
    * Get the default bootstrap context
    * @return The bootstrap context
    */
   public CloneableBootstrapContext getDefaultBootstrapContext()
   {
      return defaultBootstrapContext;
   }

   /**
    * Set the default bootstrap context
    * @param bc The bootstrap context
    */
   public void setDefaultBootstrapContext(CloneableBootstrapContext bc)
   {
      if (trace)
         log.tracef("Default BootstrapContext: %s", bc);

      String currentName = null;

      if (defaultBootstrapContext != null)
         currentName = defaultBootstrapContext.getName();

      defaultBootstrapContext = bc;

      if (bc != null)
      {
         bootstrapContexts.put(bc.getName(), bc);
      }
      else if (currentName != null)
      {
         bootstrapContexts.remove(currentName);
      }
   }

   /**
    * Set the workManagerCoordinator
    * @param wcc The workManagerCoordinator
    */
   public void setWorkManagerCoordinator(WorkManagerCoordinator wcc)
   {
      if (trace)
         log.tracef("WorkManagerCoordinator: %s", wcc);

      this.workManagerCoordinator = wcc;
   }

   /**
    * Create a bootstrap context
    * @param id The id of the bootstrap context
    * @return The bootstrap context
    */
   public synchronized CloneableBootstrapContext createBootstrapContext(String id)
   {
      return createBootstrapContext(id, null);
   }

   /**
    * Get a bootstrap context
    * @param id The id of the bootstrap context
    * @param name The name of the bootstrap context; if <code>null</code> default value is used
    * @return The bootstrap context
    */
   public synchronized CloneableBootstrapContext createBootstrapContext(String id, String name)
   {
      if (id == null || id.trim().equals(""))
         throw new IllegalArgumentException("The id of BootstrapContext is invalid: " + id);

      // Check for an active BC
      if (activeBootstrapContexts.keySet().contains(id))
      {
         Integer i = refCountBootstrapContexts.get(id);
         refCountBootstrapContexts.put(id, Integer.valueOf(i.intValue() + 1));

         return activeBootstrapContexts.get(id);
      }

      try
      {
         // Create a new instance
         CloneableBootstrapContext template = null;
         if (name != null)
         {
            template = bootstrapContexts.get(name);
         }
         else
         {
            template = defaultBootstrapContext;
         }

         if (template == null)
            throw new IllegalArgumentException("The BootstrapContext wasn't found: " + name);

         CloneableBootstrapContext bc = template.clone();
         bc.setId(id);

         org.ironjacamar.core.api.workmanager.WorkManager wm =
            workManagerCoordinator.createWorkManager(id, bc.getWorkManagerName());

         bc.setWorkManager(wm);

         activeBootstrapContexts.put(id, bc);
         refCountBootstrapContexts.put(id, Integer.valueOf(1));

         if (trace)
            log.tracef("Created BootstrapContext: %s", bc);

         return bc;
      }
      catch (Throwable t)
      {
         throw new IllegalStateException("The BootstrapContext couldn't be created: " + name, t);
      }
   }

   /**
    * Remove a bootstrap context
    * @param id The id of the bootstrap context
    */
   public synchronized void removeBootstrapContext(String id)
   {
      if (id == null || id.trim().equals(""))
         throw new IllegalArgumentException("The id of BootstrapContext is invalid: " + id);

      Integer i = refCountBootstrapContexts.get(id);
      if (i != null)
      {
         int newValue = i.intValue() - 1;
         if (newValue == 0)
         {
            CloneableBootstrapContext cbc = activeBootstrapContexts.remove(id);
            refCountBootstrapContexts.remove(id);

            cbc.shutdown();

            workManagerCoordinator.removeWorkManager(id);
         }
         else
         {
            refCountBootstrapContexts.put(id, Integer.valueOf(newValue));
         }
      }
   }

   /**
    * Create an identifier
    * @param raClz The resource adapter class name
    * @param configProperties The config properties
    * @return The id
    */
   public String createIdentifier(String raClz, Collection<ConfigProperty> configProperties)
   {
      return createIdentifier(raClz, configProperties, null);
   }

   /**
    * Create an identifier
    * @param raClz The resource adapter class name
    * @param configProperties The config properties
    * @param bootstrapContextName The name of the bootstrap context
    * @return The id
    */
   public String createIdentifier(String raClz, Collection<ConfigProperty> configProperties,
         String bootstrapContextName)
   {
      if (defaultBootstrapContext == null)
         throw new IllegalArgumentException("No default BootstrapContext defined");

      StringBuffer sb = new StringBuffer();

      sb.append(raClz);

      if (configProperties != null && configProperties.size() > 0)
      {
         sb.append(";");

         Iterator<ConfigProperty> iterator = configProperties.iterator();
         while (iterator.hasNext())
         {
            ConfigProperty entry = iterator.next();
            sb.append(entry.getConfigPropertyName()).append("=").append(entry.getConfigPropertyValue());

            if (iterator.hasNext())
               sb.append(":");
         }
      }

      sb.append("-");
      if (bootstrapContextName != null && !bootstrapContextName.trim().equals(""))
      {
         sb.append(bootstrapContextName);
      }
      else
      {
         sb.append(defaultBootstrapContext.getName());
      }

      return sb.toString();
   }
}
