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

package org.ironjacamar.core.recovery;

import org.ironjacamar.core.CoreBundle;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.spi.recovery.RecoveryPlugin;

import java.lang.reflect.Method;

import javax.resource.ResourceException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * Configurable implementation of a recovery plugin.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConfigurableRecoveryPlugin implements RecoveryPlugin
{
   /** Log instance */
   private static CoreLogger log =
      Logger.getMessageLogger(CoreLogger.class, ConfigurableRecoveryPlugin.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Enable isValid */
   private boolean enableIsValid;

   /** isValid override */
   private boolean isValidOverride;

   /* isValid value */
   private int isValidValue;

   /** Enable close */
   private boolean enableClose;

   /* Close method */
   private String closeMethod;

   /**
    * Constructor
    */
   public ConfigurableRecoveryPlugin()
   {
      this.enableIsValid = true;
      this.isValidValue = 5;
      this.isValidOverride = false;
      this.enableClose = true;
      this.closeMethod = "close";
   }

   /**
    * Enable isValid method call
    * @param v The value
    */
   public void setEnableIsValid(boolean v)
   {
      enableIsValid = v;
   }

   /**
    * Set isValid method parameter
    * @param v The value
    */
   public void setIsValidValue(int v)
   {
      isValidValue = v;
   }

   /**
    * Override value for isValid method call if not enabled
    * @param v The value
    */
   public void setIsValidOverride(boolean v)
   {
      isValidOverride = v;
   }

   /**
    * Enable close method call
    * @param v The value
    */
   public void setEnableClose(boolean v)
   {
      enableClose = v;
   }

   /**
    * Set close method name
    * @param v The value
    */
   public void setCloseMethod(String v)
   {
      closeMethod = v;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isValid(Object c) throws ResourceException
   {
      if (enableIsValid)
      {
         if (c != null)
         {
            try
            {
               Method method = SecurityActions.getMethod(c.getClass(), "isValid", new Class[] {int.class});
               SecurityActions.setAccessible(method, true);
               Boolean b = (Boolean)method.invoke(c, new Object[] {Integer.valueOf(isValidValue)});
               return b.booleanValue();
            }
            catch (Throwable t)
            {
               log.debugf("No isValid(int) method defined on connection interface (%s)", c.getClass().getName());
            }
         }
      }
      else
      {
         return isValidOverride;
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void close(Object c) throws ResourceException
   {
      if (enableClose && c != null)
      {
         if (c instanceof javax.resource.cci.Connection)
         {
            try
            {
               javax.resource.cci.Connection cci = (javax.resource.cci.Connection)c;
               cci.close();
            }
            catch (ResourceException re)
            {
               log.exceptionDuringConnectionClose(re);
               throw new ResourceException(bundle.errorDuringConnectionClose(), re);
            }
         }
         else
         {
            try
            {
               Method method = SecurityActions.getMethod(c.getClass(), closeMethod, (Class<?>[])null);
               SecurityActions.setAccessible(method, true);
               method.invoke(c, (Object[])null);
            }
            catch (Throwable t)
            {
               log.debugf(t, "Error during connection %s()", closeMethod);
               throw new ResourceException(bundle.errorDuringConnectionClose(), t);
            }
         }
      }
   }
}
