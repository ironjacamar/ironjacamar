/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security.reauth.eis;

import org.jboss.logging.Logger;

/**
 * The reauthentication EIS server
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ReauthServer
{
   /** Default host name */
   private static final String DEFAULT_HOST_NAME = "localhost";

   /** The logger */
   private static Logger log = Logger.getLogger(ReauthServer.class);

   /** The host name */
   private String hostName;

   /** The port */
   private int port;

   /** Maximum number of connections */
   private int maxConnections;

   /** EIS server */
   private EISServer eis;

   /**
    * Constructor
    */
   public ReauthServer()
   {
      this.hostName = DEFAULT_HOST_NAME;
      this.port = 19000;
      this.maxConnections = 1;
      this.eis = null;
   }

   /**
    * Get the host name
    * @return The value
    */
   public String getHostName()
   {
      return hostName;
   }

   /**
    * Set the host name
    * @param v The value
    */
   public void setHostName(String v)
   {
      if (v != null)
         hostName = v;
   }

   /**
    * Get the port
    * @return The value
    */
   public int getPort()
   {
      return port;
   }

   /**
    * Set the port
    * @param v The value
    */
   public void setPort(int v)
   {
      port = v;
   }

   /**
    * Get the maximum number of connections
    * @return The value
    */
   public int getMaxConnections()
   {
      return maxConnections;
   }

   /**
    * Set the maximum number of connections
    * @param v The value
    */
   public void setMaxConnections(int v)
   {
      maxConnections = v;
   }

   /**
    * Start
    * @exception Throwable Thrown if an error occurs
    */
   public void start() throws Throwable
   {
      if (eis != null)
         eis.stop();

      eis = new EISServer(hostName, port, maxConnections);
      eis.start();

      Thread t = new Thread(eis);
      t.start();
   }

   /**
    * Stop
    * @exception Throwable Thrown if an error occurs
    */
   public void stop() throws Throwable
   {
      if (eis != null)
         eis.stop();

      eis = null;
   }
}
