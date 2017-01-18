/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2017, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security;

import org.jboss.jca.core.spi.security.Callback;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;


/**
 * Abstract callback class.
 *
 *
 * @author Flavia Rainone
 */
public abstract class AbstractCallback implements Callback
{
   @Override
   public javax.security.auth.callback.Callback[] mapCallbacks(javax.security.auth.callback.Callback[] callbacks)
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
               p = this.mapPrincipal(name);

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
                  String s = this.mapGroup(g);

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

      return l.toArray(new javax.security.auth.callback.Callback[l.size()]);
   }
}
