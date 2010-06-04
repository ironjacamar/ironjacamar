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

import com.github.fungal.spi.deployers.DeployerPhases;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerList;

/**
 * The web server
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WebServer implements DeployerPhases
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

    /** Handlers for web apps **/
   private HandlerList handlers;

   /**
    * Constructs the web server
    */
   public WebServer()
   {
      this.server = null;
      this.host = "localhost";
      this.port = 8080;
      this.executorService = null;
      this.handlers = new HandlerList();
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
         handlers = new HandlerList();

         log.info("Jetty " + Server.getVersion() + " stopped");
      }
   }

   /**
    * Pre deploy
    * @exception Throwable Thrown if an error occurs
    */
   public void preDeploy() throws Throwable
   {
   }

   /**
    * Post deploy
    * @exception Throwable Thrown if an error occurs
    */
   public void postDeploy() throws Throwable
   {
      try
      {
         server.setHandler(handlers);
      
         server.start();
      }
      catch (Exception e)
      {
         log.error("Could not start Jetty webserver");
      }
   }

   /**
    * Pre undeploy
    * @exception Throwable Thrown if an error occurs
    */
   public void preUndeploy() throws Throwable
   {
   }

   /**
    * Post undeploy
    * @exception Throwable Thrown if an error occurs
    */
   public void postUndeploy() throws Throwable
   {
   }

   /**
    * Add a handle
    * @param handler The handle
    */
   void addHandler(Handler handler)
   {
      handler.setServer(server);
      handlers.addHandler(handler);
   }
}
