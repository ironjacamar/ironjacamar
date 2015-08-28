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

package org.ironjacamar.core.security.picketbox;

import org.ironjacamar.core.CoreLogger;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;

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

   /** Trace */
   private static boolean trace = log.isTraceEnabled();

   /** Callback mappings */
   private org.ironjacamar.core.spi.security.Callback mappings;

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
   public PicketBoxCallbackHandler(org.ironjacamar.core.spi.security.Callback mappings)
   {
      this.mappings = mappings;
   }

   /**
    * {@inheritDoc}
    */
   public void handle(javax.security.auth.callback.Callback[] callbacks) throws UnsupportedCallbackException,
                                                                                IOException
   {
      if (trace)
         log.tracef("handle(%s)", Arrays.toString(callbacks));

      if (callbacks != null && callbacks.length > 0)
      {
         if (mappings != null)
         {
            List<javax.security.auth.callback.Callback> l =
               new ArrayList<javax.security.auth.callback.Callback>(callbacks.length);

            for (int i = 0; i < callbacks.length; i++)
            {
               javax.security.auth.callback.Callback callback = callbacks[i];

               if (callback instanceof CallerPrincipalCallback)
               {
                  CallerPrincipalCallback callerPrincipalCallback = (CallerPrincipalCallback)callback;
                  String name = null;
                  Principal p = null;

                  Principal callerPrincipal = callerPrincipalCallback.getPrincipal();
                  if (callerPrincipal != null)
                     name = callerPrincipal.getName();

                  if (name == null && callerPrincipalCallback.getName() != null)
                     name = callerPrincipalCallback.getName();

                  if (name != null)
                     p = mappings.mapPrincipal(name);

                  if (p != null)
                  {
                     l.add(new CallerPrincipalCallback(callerPrincipalCallback.getSubject(), p));
                  }
                  else
                  {
                     l.add(callback);
                  }
               }
               else if (callback instanceof GroupPrincipalCallback)
               {
                  GroupPrincipalCallback groupPrincipalCallback = (GroupPrincipalCallback)callback;

                  if (groupPrincipalCallback.getGroups() != null && groupPrincipalCallback.getGroups().length > 0)
                  {
                     List<String> gs = new ArrayList<String>(groupPrincipalCallback.getGroups().length);

                     for (String g : groupPrincipalCallback.getGroups())
                     {
                        String s = mappings.mapGroup(g);

                        if (s != null)
                        {
                           gs.add(s);
                        }
                        else
                        {
                           gs.add(g);
                        }
                     }

                     l.add(new GroupPrincipalCallback(groupPrincipalCallback.getSubject(),
                                                      gs.toArray(new String[gs.size()])));
                  }
                  else
                  {
                     l.add(callback);
                  }
               }
               else
               {
                  l.add(callback);
               }
            }

            callbacks = l.toArray(new javax.security.auth.callback.Callback[l.size()]);
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
