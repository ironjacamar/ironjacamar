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

package org.jboss.jca.core.security.reauth.eis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;

/**
 * An interaction between client and server
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Interaction implements Runnable
{
   /** The logger */
   private Logger log = Logger.getLogger(Interaction.class);

   /** The socket */
   private Socket socket;

   /** Granted */
   private boolean granted;

   /** Callback */
   private AtomicInteger callback;

   /** Input */
   private ObjectInputStream ois;

   /** Output */
   private ObjectOutputStream oos;

   /** The user name */
   private String userName;

   /**
    * Constructor
    * @param socket The socket
    * @param granted Was full access granted
    * @param callback The close callback
    */
   public Interaction(Socket socket, boolean granted, AtomicInteger callback)
   {
      this.socket = socket;
      this.granted = granted;
      this.callback = callback;
      this.ois = null;
      this.oos = null;
      this.userName = null;
   }

   /**
    * Run
    */
   public void run()
   {
      log.debugf("Interaction started for: %s", socket);

      try
      {
         boolean close = false;
               
         while (!close)
         {
            byte command = getInput().readByte();
            Serializable[] arguments = null;

            Invoker invoker = null;

            log.debugf("Command: %d for %s", command, socket);

            if (command == Commands.CONNECT)
            {
               invoker = new Connect();

               arguments = new Serializable[1];
               arguments[0] = granted ? Boolean.TRUE : Boolean.FALSE;
            }
            else if (command == Commands.CLOSE)
            {
               close = true;
            }
            else if (granted && command == Commands.ECHO)
            {
               invoker = new Echo(this);

               arguments = new Serializable[1];
               arguments[0] = (Serializable)ois.readObject();
            }
            else if (granted && command == Commands.AUTH)
            {
               invoker = new Auth(this);

               arguments = new Serializable[2];
               arguments[0] = (Serializable)ois.readUTF();
               arguments[1] = (Serializable)ois.readUTF();
            }
            else if (granted && command == Commands.UNAUTH)
            {
               invoker = new Unauth(this);
            }
            else
            {
               log.warnf("Unknown command: %d for %s", command, socket);
            }

            if (!close)
            {
               if (invoker != null)
               {
                  Serializable result = invoker.invoke(arguments);

                  log.infof("Sending reply: %s to: %s", result, socket);

                  getOutput().writeObject(result);
                  getOutput().flush();
               }
            }
         }
      }
      catch (Throwable t)
      {
         log.debug(t.getMessage(), t);
      }
      finally
      {
         log.debugf("Interaction ended for: %s", socket);

         try
         {
            if (socket != null)
               socket.close();
         }
         catch (IOException ioe)
         {
            log.debug(ioe.getMessage(), ioe);
         }

         callback.decrementAndGet();
      }
   }

   /**
    * Get the user name
    * @return The value
    */
   String getUserName()
   {
      return userName;
   }

   /**
    * Set the user name
    * @param v The value
    */
   void setUserName(String v)
   {
      userName = v;
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
