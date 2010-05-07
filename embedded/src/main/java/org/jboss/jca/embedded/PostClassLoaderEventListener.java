/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.embedded;

import java.lang.reflect.Method;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.events.Event;
import com.github.fungal.api.events.EventListener;

/**
 * An event listener for the POST_CLASSLOADER event
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
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
