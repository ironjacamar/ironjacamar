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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * A class bundle factory
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ClassBundleFactory
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           ClassBundleFactory.class.getName());

   /**
    * Constructor
    */
   private ClassBundleFactory()
   {
   }

   /**
    * Create a class bundle
    * @param s The serializable
    * @return The bundle
    */
   public static ClassBundle createClassBundle(Serializable s)
   {
      if (s == null)
         return null;

      log.tracef("Creating class bundle for: %s", s);

      ClassBundle cb = new ClassBundle();

      Class<?>[] classes = s.getClass().getInterfaces();
      if (classes != null && classes.length > 0)
      {
         for (Class<?> clz : classes)
         {
            String name = clz.getName();

            if (!name.startsWith("java") && !name.startsWith("javax"))
            {
               log.tracef("Creating class definition for: %s", name);

               ClassDefinition cd = ClassDefinitionFactory.createClassDefinition(s, clz);
               if (!cb.getDefinitions().contains(cd))
                  cb.getDefinitions().add(cd);
            }
         }
      }
      else
      {
         if (log.isTraceEnabled())
            log.tracef("No interfaces for: %s", s.getClass().getName());
      }

      classes = SecurityActions.getDeclaredClasses(s.getClass());
      if (classes != null && classes.length > 0)
      {
         for (Class<?> clz : classes)
         {
            String name = clz.getName();

            if (!name.startsWith("java") && !name.startsWith("javax"))
            {
               log.tracef("Creating class definition for: %s", name);

               ClassDefinition cd = ClassDefinitionFactory.createClassDefinition(s, clz);
               if (!cb.getDefinitions().contains(cd))
                  cb.getDefinitions().add(cd);
            }
         }
      }
      else
      {
         if (log.isTraceEnabled())
            log.tracef("No classes for: %s", s.getClass().getName());
      }

      classes = getFields(s.getClass());
      if (classes != null && classes.length > 0)
      {
         for (Class<?> clz : classes)
         {
            String name = clz.getName();

            if (!name.startsWith("java") && !name.startsWith("javax"))
            {
               log.tracef("Creating class definition for: %s", name);

               ClassDefinition cd = ClassDefinitionFactory.createClassDefinition(s, clz);
               if (!cb.getDefinitions().contains(cd))
                  cb.getDefinitions().add(cd);
            }
         }
      }
      else
      {
         if (log.isTraceEnabled())
            log.tracef("No fields for: %s", s.getClass().getName());
      }

      Class<?> clz = s.getClass().getSuperclass();
      while (clz != null)
      {
         String name = clz.getName();
         if (!name.startsWith("java") && !name.startsWith("javax"))
         {
            log.tracef("Creating class definition for: %s", name);

            ClassDefinition cd = ClassDefinitionFactory.createClassDefinition(s, clz);
            if (!cb.getDefinitions().contains(cd))
               cb.getDefinitions().add(cd);

            clz = clz.getSuperclass();
         }
         else
         {
            clz = null;
         }
      }

      cb.getDefinitions().add(ClassDefinitionFactory.createClassDefinition(s));

      log.tracef("Class bundle: %s", cb);

      return cb;
   }

   /**
    * Get the classes for all the fields
    * @param clz The class
    * @return The classes; empty array if none
    */
   private static Class<?>[] getFields(Class<?> clz)
   {
      List<Class<?>> result = new ArrayList<Class<?>>();

      Class<?> c = clz;
      while (!c.equals(Object.class))
      {
         try
         {
            Field[] fields = SecurityActions.getDeclaredFields(c);
            if (fields.length > 0)
            {
               for (Field f : fields)
               {
                  Class<?> defClz = f.getType();
                  String defClzName = defClz.getName();

                  if (!defClz.isPrimitive() && !defClz.isArray() && 
                      !defClzName.startsWith("java") && !defClzName.startsWith("javax"))
                  {
                     if (!result.contains(defClz))
                     {
                        log.tracef("Adding field: %s", defClzName);
                        
                        result.add(defClz);
                     }
                  }
               }
            }
         }
         catch (Throwable t)
         {
            // Ignore
         }

         c = c.getSuperclass();
      }

      return result.toArray(new Class<?>[result.size()]);
   }
}
