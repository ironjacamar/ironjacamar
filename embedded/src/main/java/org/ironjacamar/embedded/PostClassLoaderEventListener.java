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

package org.ironjacamar.embedded;

import java.lang.reflect.Method;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.events.Event;
import com.github.fungal.api.events.EventListener;

/**
 * An event listener for the POST_CLASSLOADER event
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class PostClassLoaderEventListener implements EventListener
{
   /**
    * Default constructor
    */
   PostClassLoaderEventListener()
   {
   }

   /**
    * Event
    * @param kernel The kernel
    * @param event The event
    */
   @SuppressWarnings("unchecked")
   public void event(Kernel kernel, Event event)
   {
      if (event == Event.POST_CLASSLOADER)
      {
         try
         {
            Class clz = Class.forName("org.jboss.logmanager.log4j.BridgeRepositorySelector", 
                                      true, 
                                      kernel.getKernelClassLoader());

            Method mStart = clz.getMethod("start", (Class[])null);
            Object brs = clz.newInstance();

            mStart.invoke(brs, (Object[])null);
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
   }
}
