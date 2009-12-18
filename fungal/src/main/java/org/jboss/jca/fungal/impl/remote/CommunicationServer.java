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

package org.jboss.jca.fungal.impl.remote;

import org.jboss.jca.fungal.impl.KernelImpl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The communication server
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CommunicationServer implements Runnable
{
   /** The kernel */
   private KernelImpl kernel;

   private AtomicBoolean running;

   private ServerSocket ss;

   /** Logging */
   private static Object logging;

   /**
    * Constructor
    * @param kernel The kernel
    * @param bindAddress The bind address
    * @param bindPort The bind port
    * @exception IOException Thrown if a server socket can not be created
    */
   public CommunicationServer(KernelImpl kernel, String bindAddress, int bindPort) throws IOException
   {
      this.kernel = kernel;

      if (bindAddress == null)
         bindAddress = "localhost";

      InetSocketAddress address = new InetSocketAddress(bindAddress, bindPort); 

      this.ss = new ServerSocket();
      this.ss.bind(address);

      this.running = new AtomicBoolean(true);

      if (logging == null)
         initLogging(kernel.getKernelClassLoader());
   }

   /**
    * Run
    */
   public void run()
   {
      while (running.get())
      {
         try
         {
            Socket socket = ss.accept();

            Runnable r = new Communication(socket, kernel.getMainDeployer());
            Future<?> result = kernel.getExecutorService().submit(r);
         }
         catch (IOException ioe)
         {
            if (isDebugEnabled())
               debug(ioe.getMessage());
         }
      }
   }

   /**
    * Stop
    */
   public void stop()
   {
      running.set(false);

      if (ss != null)
      {
         try
         {
            ss.close();
         }
         catch (IOException ioe)
         {
            if (isDebugEnabled())
               debug(ioe.getMessage());
         }
      }
   }

   /**
    * Init logging
    */
   @SuppressWarnings("unchecked") 
   private static void initLogging(ClassLoader cl)
   {
      try
      {
         Class clz = Class.forName("org.jboss.logging.Logger", true, cl);
         
         Method mGetLogger = clz.getMethod("getLogger", String.class);

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.jboss.jca.fungal.impl.remote.Communication"});
      }
      catch (Throwable t)
      {
         // Nothing we can do
      }
   }

   /**
    * Logging: ERROR
    * @param o The object
    * @param t The throwable
    */
   @SuppressWarnings("unchecked") 
   private static void error(Object o, Throwable t)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mError = clz.getMethod("error", Object.class, Throwable.class);
            mError.invoke(logging, new Object[] {o, t});
         }
         catch (Throwable th)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(o.toString());
         t.printStackTrace(System.out);
      }
   }

   /**
    * Logging: WARN
    * @param o The object
    */
   @SuppressWarnings("unchecked") 
   private static void warn(Object o)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mWarn = clz.getMethod("warn", Object.class);
            mWarn.invoke(logging, new Object[] {o});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(o.toString());
      }
   }

   /**
    * Logging: INFO
    * @param o The object
    */
   @SuppressWarnings("unchecked") 
   private static void info(Object o)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mInfo = clz.getMethod("info", Object.class);
            mInfo.invoke(logging, new Object[] {o});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(o.toString());
      }
   }

   /**
    * Logging: Is DEBUG enabled
    * @return True if debug is enabled; otherwise false
    */
   @SuppressWarnings("unchecked") 
   private static boolean isDebugEnabled()
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mIsDebugEnabled = clz.getMethod("isDebugEnabled", (Class[])null);
            return ((Boolean)mIsDebugEnabled.invoke(logging, (Object[])null)).booleanValue();
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      return true;
   }

   /**
    * Logging: DEBUG
    * @param o The object
    */
   @SuppressWarnings("unchecked") 
   private static void debug(Object o)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mDebug = clz.getMethod("debug", Object.class);
            mDebug.invoke(logging, new Object[] {o});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(o.toString());
      }
   }
}
