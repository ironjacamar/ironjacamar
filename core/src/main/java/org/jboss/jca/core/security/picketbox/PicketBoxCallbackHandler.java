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

package org.jboss.jca.core.security.picketbox;

import org.jboss.jca.core.CoreLogger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.jboss.logging.Logger;
import org.jboss.security.auth.callback.JASPICallbackHandler;

/**
 * An implementation of the callback SPI using PicketBox
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PicketBoxCallbackHandler implements CallbackHandler, Serializable
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, PicketBoxCallbackHandler.class.getName());

   /** Callback mappings */
   private org.jboss.jca.core.spi.security.Callback mappings;

   /**
    * Constructor
    */
   public PicketBoxCallbackHandler()
   {
      this(null);
   }

   /**
    * Constructor
    * @param mappings The mappings
    */
   public PicketBoxCallbackHandler(org.jboss.jca.core.spi.security.Callback mappings)
   {
      this.mappings = mappings;
   }

   /**
    * {@inheritDoc}
    */
   public void handle(javax.security.auth.callback.Callback[] callbacks) throws UnsupportedCallbackException,
                                                                                IOException
   {
      if (log.isTraceEnabled())
         log.tracef("handle(%s)", Arrays.toString(callbacks));

      if (callbacks != null && callbacks.length > 0)
      {
         if (mappings != null && mappings.isMappingRequired())
         {
            callbacks = mappings.mapCallbacks(callbacks);
         }

         JASPICallbackHandler jaspi = new JASPICallbackHandler();
         jaspi.handle(callbacks);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("PicketBoxCallbackHandler@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[mappings=").append(mappings);
      sb.append("]");

      return sb.toString();
   }
}
