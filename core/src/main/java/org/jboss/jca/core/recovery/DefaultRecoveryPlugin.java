/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.recovery;

import org.jboss.jca.core.spi.recovery.RecoveryPlugin;

import java.lang.reflect.Method;

import org.jboss.logging.Logger;

public class DefaultRecoveryPlugin implements RecoveryPlugin
{

   /** Log instance */
   private static Logger log = Logger.getLogger(DefaultRecoveryPlugin.class);

   @Override
   public boolean isValid(Object c)
   {
      try
      {
         if (c instanceof javax.resource.cci.Connection)
         {
            return false;

         }
         else
         {
            Method method = null;

            method = c.getClass().getMethod("isValid", new Class[]{int.class});
            method.setAccessible(true);
            Boolean b = (Boolean) method.invoke(c, new Object[]{new Integer(5)});
            return b.booleanValue();
         }
      }
      catch (Throwable t)
      {
         log.warn("Error during recovery connection isValid", t);

         return false;
      }

   }

   @Override
   public boolean close(Object c)
   {
      try
      {
         if (c instanceof javax.resource.cci.Connection)
         {
            javax.resource.cci.Connection cci = (javax.resource.cci.Connection) c;
            cci.close();
         }
         else
         {
            Method method = c.getClass().getMethod("close", (Class<?>) null);
            method.setAccessible(true);
            method.invoke(c, (Object) null);
         }
      }
      catch (Throwable t)
      {
         log.warn(
            "No close() method defined on connection interface. Destroying managed connection to clean-up",
            t);

         if (log.isDebugEnabled())
            log.debug("Forcing recreate of managed connection");

         return false;

      }
      return true;
   }
}
