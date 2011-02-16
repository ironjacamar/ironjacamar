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
import javax.resource.spi.ManagedConnectionMetaData;

import org.jboss.logging.Logger;

/**
 * ReauthManagedConnectionMetaData
 *
 * @version $Revision: $
 */
public class ReauthManagedConnectionMetaData implements ManagedConnectionMetaData
{
   /** The logger */
   private static Logger log = Logger.getLogger(ReauthManagedConnectionMetaData.class);

   /** The socket */
   private Socket socket;

   /** Input */
   private ObjectInputStream ois;

   /** Output */
   private ObjectOutputStream oos;

   /**
    * Constructor
    * @param socket The socket
    */
   public ReauthManagedConnectionMetaData(Socket socket)
   {
      this.socket = socket;
      this.ois = null;
      this.oos = null;
   }

   /**
    * Returns Product name of the underlying EIS instance connected through the ManagedConnection.
    *
    * @return Product name of the EIS instance
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getEISProductName() throws ResourceException
   {
      log.tracef("getEISProductName()");

      return "Reauth resource adapter (CRI)";
   }

   /**
    * Returns Product version of the underlying EIS instance connected through the ManagedConnection.
    *
    * @return Product version of the EIS instance
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getEISProductVersion() throws ResourceException
   {
      log.tracef("getEISProductVersion()");

      return "1.0";
   }

   /**
    * Returns maximum limit on number of active concurrent connections 
    *
    * @return Maximum limit for number of active concurrent connections
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public int getMaxConnections() throws ResourceException
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
    * Returns name of the user associated with the ManagedConnection instance
    *
    * @return Name of the user
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getUserName() throws ResourceException
   {
      log.tracef("getUserName()");

      try
      {
         getOutput().writeByte(5);
         getOutput().flush();

         return (String)getInput().readObject();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during getUserName()", t);
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
