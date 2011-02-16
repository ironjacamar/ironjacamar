/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
 * ReauthConnectionImpl
 *
 * @version $Revision: $
 */
public class ReauthConnectionImpl implements ReauthConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger(ReauthConnectionImpl.class);

   /** The socket */
   private Socket socket;

   /** Input */
   private ObjectInputStream ois;

   /** Output */
   private ObjectOutputStream oos;

   /** The CRI */
   private ReauthCri cri;

   /**
    * Constructor
    * @param socket The socket
    * @param cri ConnectionRequestInfo instance
    * @exception ResourceException Thrown if an error occurs
    */
   public ReauthConnectionImpl(Socket socket, ReauthCri cri) throws ResourceException
   {
      log.tracef("constructor(%s, %s)", socket, cri);

      this.socket = socket;
      this.cri = cri;
   }

   /**
    * Call login
    * @param username String
    * @param password String
    * @return String
    * @exception ResourceException Thrown if an error occurs
    */
   public String login(String username, String password) throws ResourceException
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
   public boolean logout() throws ResourceException
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
   public String getAuth() throws ResourceException
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
    * Get the CRI
    * @return The value
    */
   ReauthCri getCri()
   {
      return cri;
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
