/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.sjc.maven;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * An abstract host / port mojo
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractHostPortMojo extends AbstractMojo
{
   /** Host */
   private String host;
   
   /** Port */
   private int port;

   /** Constructor */
   public AbstractHostPortMojo()
   {
      host = "localhost";
      port = 1202;
   }


   /**
    * Get the host
    * @return The value
    */
   public String getHost()
   {
      return host;
   }

   /**
    * Set the host
    * @param v The value
    */
   public void setHost(String v)
   {
      this.host = v;
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
      this.port = v;
   }

   /**
    * Is local
    * @return True if local; otherwise false
    */
   protected boolean isLocal()
   {
      if ("localhost".equals(getHost()) || "127.0.0.1".equals(getHost()) || "::1".equals(getHost()))
         return true;

      return false;
   }

   /**
    * Is a command available
    * @param command The command name
    * @return True if available; otherwise false
    * @exception Throwable If an error occurs
    */
   protected boolean isCommandAvailable(String command) throws Throwable
   {
      Socket socket = null;
      try
      {
         socket = new Socket(host, port);

         ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
         output.writeUTF("getcommand");
         output.writeInt(1);
         output.writeUTF(command);

         output.flush();

         ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

         Serializable result = (Serializable)input.readObject();

         if (result == null || !(result instanceof Throwable))
            return true;

         return false;
      }
      finally
      {
         if (socket != null)
         {
            try
            {
               socket.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Execute command
    * @param command The command
    * @return The result
    * @exception Throwable If an error occurs
    */
   protected Serializable executeCommand(String command) throws Throwable
   {
      return executeCommand(command, null);
   }

   /**
    * Execute command
    * @param command The command
    * @param arguments The arguments
    * @return The result
    * @exception Throwable If an error occurs
    */
   protected Serializable executeCommand(String command, Serializable[] arguments) throws Throwable
   {
      Socket socket = null;
      try
      {
         socket = new Socket(host, port);

         ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
         
         oos.writeUTF(command);
         
         if (arguments != null)
         {
            oos.writeInt(arguments.length);
            for (Serializable argument : arguments)
            {
               oos.writeObject(argument);
            }
         }
         else
         {
            oos.writeInt(0);
         }
         
         oos.flush();

         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
         
         return (Serializable)ois.readObject();
      }
      catch (EOFException ee)
      {
         // Nothing
      }
      finally
      {
         try
         {
            if (socket != null)
               socket.close();
         }
         catch (IOException ignore)
         {
            // Ignore
         }
      }

      return null;
   }

   /**
    * Execute
    *
    * @throws MojoExecutionException Thrown if the plugin cant be executed
    * @throws MojoFailureException   Thrown if there is an error
    */
   public abstract void execute() throws MojoExecutionException, MojoFailureException;
}
