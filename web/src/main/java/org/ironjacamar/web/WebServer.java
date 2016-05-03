/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.web;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;

import javax.management.MBeanServer;

import org.jboss.logging.Logger;

import com.github.fungal.spi.deployers.DeployerPhases;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.Configuration;

/**
 * The web server
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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

   /** The MBeanServer */
   private MBeanServer mbeanServer;
   
   /** The AcceptQueueSize */
   private int acceptQueueSize;

   /** Executor service */
   private ExecutorService executorService;

    /** Handlers for web apps **/
   private HandlerList handlers;

   /**
    * Constructs the web server
    */
   public WebServer()
   {
      SecurityActions.setSystemProperty("org.eclipse.jetty.util.log.class", JavaUtilLog.class.getName());
      Log.setLog(new JavaUtilLog());

      this.server = null;
      this.host = "localhost";
      this.port = 8080;
      this.mbeanServer = null;
      this.acceptQueueSize = 64;
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
    * Set the MBeanServer
    * @param v The v
    */
   public void setMBeanServer(MBeanServer v)
   {
      this.mbeanServer = v;
   }

   /**
    * Set the accept queue size
    * @param v The value
    */
   public void setAcceptQueueSize(int v)
   {
      this.acceptQueueSize = v;
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

      if (executorService != null)
      {
         server = new Server(new ExecutorThreadPool(executorService));
      }
      else
      {
         server = new Server();
      }

      MBeanContainer jmx = new MBeanContainer(mbeanServer != null ? mbeanServer :
                                              ManagementFactory.getPlatformMBeanServer());
      server.addBean(jmx);

      Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
      classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                          "org.eclipse.jetty.annotations.AnnotationConfiguration");
      
      ServerConnector connector = new ServerConnector(server);
      connector.setHost(host);
      connector.setPort(port);
      connector.setAcceptQueueSize(acceptQueueSize);

      server.setConnectors(new Connector[]{connector});
      
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
      if (server != null &&
          !server.isRunning() &&
          handlers != null && handlers.getHandlers() != null && handlers.getHandlers().length > 0)
      {
         try
         {
            ContextHandlerCollection chc = new ContextHandlerCollection();
            chc.setHandlers(handlers.getHandlers());

            server.setHandler(chc);
      
            server.start();
         }
         catch (Exception e)
         {
            log.error("Could not start Jetty webserver", e);
         }
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
