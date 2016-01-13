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

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.core.api.connectionmanager.pool.PoolConfiguration;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.pool.dflt.DefaultPool;
import org.ironjacamar.core.connectionmanager.pool.stable.StablePool;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The pool factory
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolFactory
{
   /** Custom pool types */
   private static Map<String, Class<? extends Pool>> customPoolTypes = new HashMap<>();

   static
   {
      ServiceLoader<Pool> sl = ServiceLoader.load(Pool.class, PoolFactory.class.getClassLoader());
      try
      {
         for (Pool p : sl)
         {
            customPoolTypes.put(p.getType(), (Class<? extends Pool>)p.getClass());
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Exception during loading of pool services", e);
      }
   }

   /**
    * Constructor
    */
   private PoolFactory()
   {
   }

   /**
    * Create a pool
    * @param type The type
    * @param cm The connection manager
    * @param pc The pool configuration
    * @return The pool
    */
   public static Pool createPool(String type, ConnectionManager cm, PoolConfiguration pc)
   {
      if (type == null || type.equals(""))
         return new DefaultPool(cm, pc);

      type = type.toLowerCase(Locale.US);
      
      switch (type)
      {
         case "default":
            return new DefaultPool(cm, pc);
         case "stable":
            return new StablePool(cm, pc);
         default:
         {
            Class<? extends Pool> clz = customPoolTypes.get(type);

            if (clz == null)
               throw new RuntimeException(type + " can not be found");

            try
            {
               Constructor constructor = clz.getConstructor(ConnectionManager.class, PoolConfiguration.class);
               return (Pool)constructor.newInstance(cm, pc);
            }
            catch (Exception e)
            {
               throw new RuntimeException(type + " can not be created", e);
            }
         }
      }
   }
}
