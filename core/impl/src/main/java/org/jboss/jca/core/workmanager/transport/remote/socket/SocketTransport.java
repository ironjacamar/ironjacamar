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

package org.jboss.jca.core.workmanager.transport.remote.socket;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.workmanager.transport.remote.AbstractRemoteTransport;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The socket transport
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SocketTransport extends AbstractRemoteTransport<String> implements Runnable
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, SocketTransport.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** The bind address */
   private String host;

   /** The bind port */
   private int port;

   /** The peers */
   private Set<String> peers;

   /** Is the server running ? */
   private AtomicBoolean running;

   /** The server socket */
   private ServerSocket ss;

   /** Is the transport initialized */
   private boolean initialized;

   /**
    * Constructor
    */
   public SocketTransport()
   {
      super();
      this.host = null;
      this.port = 0;
      this.peers = null;
      this.running = new AtomicBoolean(false);
      this.ss = null;
      this.initialized = false;
   }

   /**
    * {@inheritDoc}
    */
   public void startup() throws Throwable
   {
      if (!running.get())
      {
         InetSocketAddress address = new InetSocketAddress(host, port);

         ss = new ServerSocket();
         ss.bind(address);

         running.set(true);

         getExecutorService().submit(this);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isInitialized()
   {
      return initialized;
   }

   /**
    * {@inheritDoc}
    */
   public void initialize() throws Throwable
   {
      if (peers != null && !initialized)
      {
         for (String addr : peers)
         {
            log.tracef("Peer: %s", addr);

            try
            {
               // Let other node know of us
               sendMessage(addr, Request.JOIN, getOwnAddress());

               // Update the local information
               Set<Address> workManagers = (Set<Address>)sendMessage(addr, Request.GET_WORKMANAGERS);

               log.tracef("Peer WorkManagers: %s", workManagers);

               if (workManagers != null)
               {
                  for (Address a : workManagers)
                  {
                     join(a, addr);

                     long shortRunningFree =
                        (long)sendMessage(addr, Request.GET_SHORTRUNNING_FREE, a);
                     long longRunningFree =
                        (long)sendMessage(addr, Request.GET_LONGRUNNING_FREE, a);

                     localUpdateShortRunningFree(a, shortRunningFree);
                     localUpdateLongRunningFree(a, longRunningFree);
                  }
               }
            }
            catch (Throwable t)
            {
               log.error(t.getMessage(), t);
            }
         }
      }

      initialized = true;
   }

   /**
    * {@inheritDoc}
    */
   public void shutdown() throws Throwable
   {
      running.set(false);

      if (ss != null)
         ss.close();
   }

   @Override
   protected Serializable sendMessage(String address, Request request, Serializable... parameters)
      throws WorkException
   {
      String[] addressPart = address.split(":");
      Socket socket = null;
      ObjectOutputStream oos = null;

      if (log.isTraceEnabled())
         log.tracef("%s:%d: sending message=%s to %s:%s", ss.getInetAddress().getHostName(),
                    ss.getLocalPort(), request, addressPart[0], addressPart[1]);

      try
      {
         socket = new Socket(addressPart[0], Integer.valueOf(addressPart[1]));

         oos = new ObjectOutputStream(socket.getOutputStream());
         oos.writeInt(request.ordinal());
         oos.writeInt(request.getNumberOfParameter());
         if (parameters != null)
         {
            for (Serializable o : parameters)
            {
               oos.writeObject(o);
            }
         }

         oos.flush();

         return parseResponse(socket);
      }
      catch (Throwable t)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error sending command: " + t.getMessage(), t);
         }
         if (t instanceof WorkException)
         {
            throw (WorkException) t;
         }
         else
         {
            WorkException we = new WorkException(t.getMessage());
            we.initCause(t);
            throw we;
         }
      }
      finally
      {
         if (oos != null)
         {
            try
            {
               oos.close();
            }
            catch (IOException e)
            {
               //ignore it
            }
         }
         if (socket != null)
         {
            try
            {
               socket.close();
            }
            catch (IOException e)
            {
               //ignore it
            }
         }
      }
   }

   private Serializable parseResponse(Socket socket) throws Throwable
   {
      ObjectInputStream ois = null;

      try
      {
         ois = new ObjectInputStream(socket.getInputStream());

         int commandOrdinalPosition = ois.readInt();
         int numberOfParameters = ois.readInt();
         Serializable[] parameters = new Serializable[numberOfParameters];

         for (int i = 0; i < numberOfParameters; i++)
         {
            Serializable parameter = (Serializable)ois.readObject();
            parameters[i] = parameter;
         }

         Response response = Response.values()[commandOrdinalPosition];

         switch (response)
         {
            case OK_VOID : {
               return null;
            }
            case OK_SERIALIZABLE : {
               return parameters[0];
            }
            case WORK_EXCEPTION : {
               WorkException we = (WorkException)parameters[0];
               throw we;
            }
            case GENERIC_EXCEPTION : {
               Throwable t = (Throwable)parameters[0];
               throw t;
            }
            default :
               if (log.isDebugEnabled())
               {
                  log.debug("Unknown response received on socket Transport");
               }
               throw new WorkException("Unknown response received on socket Transport");
         }
      }
      finally
      {
         if (ois != null)
         {
            try
            {
               ois.close();
            }
            catch (IOException e)
            {
               //ignore it
            }
         }
      }
   }

   /**
    * Set the host.
    *
    * @param host The host to set.
    */
   public void setHost(String host)
   {
      this.host = host;
   }

   /**
    * Set the port.
    *
    * @param port The port to set.
    */
   public void setPort(int port)
   {
      this.port = port;
   }

   /**
    * Set the peers
    * @param peers The peers
    */
   public void setPeers(Set<String> peers)
   {
      this.peers = peers;
   }

   /**
    * Get the physical address
    * @return The value
    */
   public String getOwnAddress()
   {
      return host + ":" + port;
   }

   @Override
   public void run()
   {
      while (running.get())
      {
         try
         {
            java.net.Socket socket = ss.accept();

            Runnable r = new Communication(this, socket);
            this.getExecutorService().submit(r);
         }
         catch (Exception e)
         {
            if (log.isTraceEnabled())
               log.trace(e.getMessage());
         }
      }
   }

   @Override
   public String toString()
   {
      return "SocketTransport [host=" + host + ", port=" + port + ", running=" + running + ", ss=" + ss + "]";
   }
}
