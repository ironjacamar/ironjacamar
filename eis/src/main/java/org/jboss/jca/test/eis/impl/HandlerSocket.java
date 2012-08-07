/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.eis.impl;

import org.jboss.jca.test.eis.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;

/**
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class HandlerSocket implements Runnable
{
   /** The handler class */
   private Class handler;

   /** The socket */
   private Socket socket;

   /** The server */
   private HandlerServer server;

   /**
    * Constructor
    * @param handler The handler class
    * @param socket The socket
    * @param server The server
    */
   public HandlerSocket(Class handler, Socket socket, HandlerServer server)
   {
      this.handler = handler;
      this.socket = socket;
      this.server = server;
   }

   /**
    * Run
    */
   public void run()
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(handler.getClassLoader());

         InputStream is = socket.getInputStream();
         OutputStream os = socket.getOutputStream();

         Handler instance = createHandler();
         instance.handle(is, os);
      }
      catch (Throwable t)
      {
         t.printStackTrace(System.err);
      }
      finally
      {
         shutdown();
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * Shutdown
    */
   void shutdown()
   {
      try
      {
         if (socket != null)
            socket.close();
      }
      catch (IOException ignore)
      {
         // Ignore
      }
   }

   /**
    * Create handler
    * @exception Throwable Thrown in case of an error
    */
   @SuppressWarnings("unchecked")
   private Handler createHandler() throws Throwable
   {
      Class clz = handler;

      while (clz != Object.class)
      {
         try
         {
            Constructor c = clz.getDeclaredConstructor((Class[])null);
            c.setAccessible(true);

            return (Handler)c.newInstance((Object[])null);
         }
         catch (Throwable t)
         {
            t.printStackTrace(System.err);
         }

         clz = clz.getSuperclass();
      }

      throw new IllegalStateException("Unable to find default constructor");
   }
}
