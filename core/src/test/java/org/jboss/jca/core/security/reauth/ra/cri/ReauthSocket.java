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
package org.jboss.jca.core.security.reauth.ra.cri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.resource.ResourceException;

import org.jboss.logging.Logger;

/**
 * ReauthSocket
 *
 * @version $Revision: $
 */
public class ReauthSocket
{
   /** The logger */
   private static Logger log = Logger.getLogger(ReauthSocket.class);

   /** The socket */
   private Socket socket;

   /** Input */
   private ObjectInputStream ois;

   /** Output */
   private ObjectOutputStream oos;

   /**
    * Constructor
    * @param server The server name
    * @param port The port
    * @exception ResourceException Thrown if an error occurs
    */
   public ReauthSocket(String server, int port) throws ResourceException
   {
      try
      {
         this.socket = new Socket(server, port);
         this.ois = null;
         this.oos = null;

         // Connect
         getOutput().writeByte(0);
         getOutput().flush();

         Boolean granted = (Boolean)getInput().readObject();
         
         if (!granted.booleanValue())
            throw new ResourceException("Connection not granted");
      }
      catch (Throwable t)
      {
         throw new ResourceException("Unable to establish a connection", t);
      }
   }

   /**
    * Call login
    * @param username String
    * @param password String
    * @return String
    * @exception ResourceException Thrown if an error occurs
    */
   public synchronized String login(String username, String password) throws ResourceException
   {
      log.tracef("login(%s, %s)", username, password);

      try
      {
         getOutput().writeByte(3);
         getOutput().writeUTF(username);
         getOutput().writeUTF(password);
         getOutput().flush();

         return (String)getInput().readObject();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during login", t);
      }
   }

   /**
    * Call logout
    * @return boolean
    * @exception ResourceException Thrown if an error occurs
    */
   public synchronized boolean logout() throws ResourceException
   {
      log.tracef("logout()");

      try
      {
         getOutput().writeByte(4);
         getOutput().flush();

         Boolean result = (Boolean)getInput().readObject();

         return result.booleanValue();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during logout", t);
      }
   }

   /**
    * get auth
    * @return String
    * @throws ResourceException Thrown if an error occurs
    */
   public synchronized String getAuth() throws ResourceException
   {
      log.tracef("getAuth()");

      try
      {
         getOutput().writeByte(5);
         getOutput().flush();

         return (String)getInput().readObject();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during getAuth", t);
      }
   }

   /**
    * Returns maximum limit on number of active concurrent connections 
    *
    * @return Maximum limit for number of active concurrent connections
    * @throws ResourceException Thrown if an error occurs
    */
   public synchronized int getMaxConnections() throws ResourceException
   {
      log.tracef("getMaxConnections()");

      try
      {
         getOutput().writeByte(6);
         getOutput().flush();

         Integer result = (Integer)getInput().readObject();

         return result.intValue();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during getUserName()", t);
      }
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public synchronized void cleanup() throws ResourceException
   {
      log.tracef("cleanup");

      try
      {
         // Unauth the interaction
         getOutput().writeByte(4);
         getOutput().flush();

         Boolean result = (Boolean)getInput().readObject();

         if (result.booleanValue())
         {
            log.debugf("Unauth successful");
         }
         else
         {
            log.debugf("Unauth unsuccessful");
         }
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during cleanup", t);
      }
   }

   /**
    * Destroys the physical connection to the underlying resource manager.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public synchronized void destroy() throws ResourceException
   {
      log.tracef("destroy");

      try
      {
         // Close the interaction
         getOutput().writeByte(1);
         getOutput().flush();

         socket.close();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during destroy", t);
      }
   }

   /**
    * Get input stream
    * @return The value
    * @exception IOException Thrown in case of an error
    */
   private ObjectInputStream getInput() throws IOException
   {
      if (ois == null)
         ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream(), 8192));
      
      return ois;
   }

   /**
    * Get output stream
    * @return The value
    * @exception IOException Thrown in case of an error
    */
   private ObjectOutputStream getOutput() throws IOException
   {
      if (oos == null)
         oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream(), 8192));
      
      return oos;
   }
}
