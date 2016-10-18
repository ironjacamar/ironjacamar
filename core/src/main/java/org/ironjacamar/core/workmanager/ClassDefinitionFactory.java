/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.workmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * A class definition factory
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ClassDefinitionFactory
{
   /**
    * Constructor
    */
   private ClassDefinitionFactory()
   {
   }

   /**
    * Create a class definition
    * @param s The serializable
    * @return The definition
    */
   public static ClassDefinition createClassDefinition(Serializable s)
   {
      return createClassDefinition(s, s.getClass());
   }

   /**
    * Create a class definition
    * @param s The serializable
    * @param clz The class
    * @return The definition
    */
   public static ClassDefinition createClassDefinition(Serializable s, Class<?> clz)
   {
      if (s == null || clz == null)
         return null;

      String name = clz.getName();
      long serialVersionUID = 0L;
      byte[] data;

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      InputStream is = null;
      try
      {
         try
         {
            Field svuf = getSerialVersionUID(clz);
            if (svuf != null)
               serialVersionUID = (long)svuf.get(s);
         }
         catch (Throwable t)
         {
            // No serialVersionUID value
         }

         String clzName = name.replace('.', '/') + ".class";

         is = SecurityActions.getClassLoader(s.getClass()).getResourceAsStream(clzName);
         int i = is.read();
         while (i != -1)
         {
            baos.write(i);
            i = is.read();
         }

         data = baos.toByteArray();

         return new ClassDefinition(name, serialVersionUID, data);
      }
      catch (Throwable t)
      {
         // Ignore
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }
      return null;
   }

   /**
    * Find the serialVersionUID field
    * @param clz The class
    * @return The field or <code>null</code> if none were found
    */
   private static Field getSerialVersionUID(Class<?> clz)
   {
      Class<?> c = clz;
      while (c != null)
      {
         try
         {
            Field svuf = SecurityActions.getDeclaredField(clz, "serialVersionUID");
            SecurityActions.setAccessible(svuf);
            return svuf;
         }
         catch (Throwable t)
         {
            // No serialVersionUID definition
         }
         c = c.getSuperclass();
      }

      return null;
   }
}
