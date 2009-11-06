/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.web;

import java.util.concurrent.ExecutorService;

import org.jboss.logging.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;

/**
 * The web server
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WebServer
{
   private static Logger log = Logger.getLogger(WebServer.class);
   private static boolean trace = log.isTraceEnabled();

   /** The server */
   private Server server;

   /** The host */
   private String host;

   /** The port */
   private int port;

   /** Executor service */
   private ExecutorService executorService;

   /**
    * Constructs the web server
    */
   public WebServer()
   {
      this.server = null;
      this.host = "localhost";
      this.port = 8080;
      this.executorService = null;
   }

   /**
    * Get the web server
    * @return The server
    */
   public Server getServer()
   {
      return server;
   }

   /**
    * Get the host
    * @return The host
    */
   public String getHost()
   {
      return host;
   }

   /**
    * Set the host
    * @param host The host
    */
   public void setHost(String host)
   {
      this.host = host;
   }

   /**
    * Get the web server port
    * @return The port
    */
   public int getPort()
   {
      return port;
   }

   /**
    * Set the web server port
    * @param port The port
    */
   public void setPort(int port)
   {
      this.port = port;
   }

   /**
    * Set the executor service
    * @param service The service
    */
   public void setExecutorService(ExecutorService service)
   {
      this.executorService = service;
   }

   /**
    * Start
    * @exception Throwable If an error occurs
    */
   public void start() throws Throwable
   {
      stop();

      server = new Server();

      Connector connector = new SocketConnector();
      connector.setHost(host);
      connector.setPort(port);

      server.setConnectors(new Connector[]{connector});

      if (executorService != null)
         server.setThreadPool(new ExecutorThreadPool(executorService));

      server.start();

      log.info("Jetty " + Server.getVersion() + " started");
   }

   /**
    * Stop
    * @exception Throwable If an error occurs
    */
   public void stop() throws Throwable
   {
      if (server != null && server.isRunning())
      {
         server.stop();

         log.info("Jetty " + Server.getVersion() + " stopped");
      }
   }
}
