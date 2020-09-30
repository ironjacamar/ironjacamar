/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;

import java.lang.reflect.Method;

import javax.resource.ResourceException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * Default implementation of a recovery plugin.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DefaultRecoveryPlugin implements RecoveryPlugin
{
   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, DefaultRecoveryPlugin.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /**
    * Constructor
    */
   public DefaultRecoveryPlugin()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isValid(Object c) throws ResourceException
   {
      if (c != null)
      {
         try
         {
            Method method = SecurityActions.getMethod(c.getClass(), "isValid", new Class[] {int.class});
            SecurityActions.setAccessible(method, true);
            Boolean b = (Boolean)method.invoke(c, new Object[] {Integer.valueOf(5)});
            return b.booleanValue();
         }
         catch (Throwable t)
         {
            log.debugf("No isValid(int) method defined on connection interface (%s)", c.getClass().getName());
         }
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void close(Object c) throws ResourceException
   {
      if (c != null)
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
               Method method = SecurityActions.getMethod(c.getClass(), "close", (Class<?>[])null);
               SecurityActions.setAccessible(method, true);
               method.invoke(c, (Object[])null);
            }
            catch (Throwable t)
            {
               log.debug("Error during connection close()", t);
               throw new ResourceException(bundle.errorDuringConnectionClose(), t);
            }
         }
      }
   }
}
