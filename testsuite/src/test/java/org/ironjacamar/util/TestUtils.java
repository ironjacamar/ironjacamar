/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test utility class
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TestUtils
{
   /**
    * Constructor
    */
   private TestUtils()
   {
   }

   /**
    * Extract a field from the passed object
    * @param obj The object
    * @param name The name of the field
    * @return The value; <code>null</code> if not found
    */
   public static Object extract(Object obj, String name)
   {
      Class<?> clz = obj.getClass();

      while (!Object.class.equals(clz))
      {
         try
         {
            Field[] fields = clz.getDeclaredFields();

            if (fields != null && fields.length > 0)
            {
               for (Field field : fields)
               {
                  if (field.getName().equals(name))
                  {
                     field.setAccessible(true);
                     return field.get(obj);
                  }
               }
            }
         }
         catch (Throwable t)
         {
            //t.printStackTrace();
         }
         clz = clz.getSuperclass();
      }

      return null;
   }

   /**
    * Check size of a collection populated in another thread. Collection is supposed to be thread safe
    * @param collection collection to check
    * @param size size limit
    * @param timeout the maximum time to wait
    * @param unit the time unit of the timeout argument

    * @return {@code true} if collection size is reached before timeout
    *         {@code false} if the timeout elapsed before collection size reached
    * @throws InterruptedException if interrupted while waiting
    */
   public static boolean isCorrectCollectionSizeTimeOut(Collection<?> collection,
         int size, long timeout, TimeUnit unit)
         throws InterruptedException
   {
      Runnable task = () -> {
         try
         {
            while (collection.size() != size)
            {
               TimeUnit.MILLISECONDS.sleep(10);
            }
         }
         catch (InterruptedException e)
         {
            throw new IllegalStateException("task interrupted", e);
         }
      };
      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(task);
      executor.shutdown();
      return executor.awaitTermination(timeout, unit);
   }

   /**
    * Check size of a collection populated in another thread. Collection is supposed to be thread safe
    * @param collection collection to check
    * @param size size limit
    * @return {@code true} if collection size is reached before timeout
    *         {@code false} if the timeout elapsed before collection size reached
    * @throws InterruptedException if interrupted while waiting
    */
   public static boolean isCorrectCollectionSizeTenSecTimeout(Collection<?> collection, int size)
         throws InterruptedException
   {
      return isCorrectCollectionSizeTimeOut(collection, size, 10, TimeUnit.SECONDS);
   }
}
