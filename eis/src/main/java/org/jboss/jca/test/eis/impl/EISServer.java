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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class EISServer
{
   /** The handler server */
   private static Map<String, HandlerServer> handlerServers =
      Collections.synchronizedMap(new HashMap<String, HandlerServer>());

   /* The handler class name */
   private String handlerClassName;

   /* The class loader */
   private ClassLoader classLoader;

   /* The host */
   private String host;

   /* The port */
   private int port;

   /* The control port */
   private int controlPort;

   /**
    * Constructor
    */
   public EISServer()
   {
      this.handlerClassName = null;
      this.classLoader = null;
      this.host = "localhost";
      this.port = 1400;
      this.controlPort = 1401;
   }

   /**
    * Set the handler class name
    * @param v The name
    */
   public void setHandlerClassName(String v)
   {
      this.handlerClassName = v;
   }

   /**
    * Set class loader
    * @param v The value
    */
   public void setClassLoader(ClassLoader v)
   {
      this.classLoader = v;
   }

   /**
    * Set the host name
    * @param v The name
    */
   public void setHost(String v)
   {
      this.host = v;
   }

   /**
    * Set the port
    * @param v The value
    */
   public void setPort(int v)
   {
      this.port = v;
   }

   /**
    * Startup
    * @exception Throwable Thrown if an error occurs
    */
   public void startup() throws Throwable
   {
      if (handlerClassName == null || handlerClassName.trim().equals(""))
         throw new IllegalStateException("HandlerClassName isn't defined");

      shutdown();

      ClassLoader cl = classLoader;

      if (cl == null)
         cl = EISServer.class.getClassLoader();

      Class handler = Class.forName(handlerClassName, true, cl);

      if (!Handler.class.isAssignableFrom(handler))
         throw new IllegalArgumentException("The specified handler class doesn't implement the interface");

      HandlerServer handlerServer = new HandlerServer(host, port, handler);
      handlerServer.start();

      handlerServers.put(getKey(), handlerServer);
   }

   /**
    * Shutdown
    * @exception Throwable Thrown if an error occurs
    */
   public void shutdown() throws Throwable
   {
      HandlerServer handlerServer = handlerServers.get(getKey());

      if (handlerServer != null)
      {
         handlerServer.stop();
         handlerServer = null;
      }
   }

   /**
    * Get the key
    * @return The value
    */
   private String getKey()
   {
      return host + ":" + port;
   }

   /**
    * Main
    * @param args The arguments
    */
   public static void main(String[] args)
   {

   }
}
