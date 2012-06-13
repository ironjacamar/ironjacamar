/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.spi.workmanager.transport.Transport;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The in-vm transport
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class SocketTransport implements Transport, Runnable
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, SocketTransport.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** Distributed work manager instance */
   protected DistributedWorkManager dwm;

   /** The kernel executorService*/
   private ExecutorService executorService;

   /** The bind address */
   private String host;

   /** The bind port */
   private int port;

   /** Is the server running ? */
   private final AtomicBoolean running;

   /** The server socket */
   private ServerSocket ss;

   /** The work manager */
   private Map<String, String> workManagers;

   /** socket time out **/
   private long timeout;

   /**
    * Constructor
    */
   public SocketTransport()
   {
      this.workManagers = Collections.synchronizedMap(new HashMap<String, String>());

      this.dwm = null;
      this.running = new AtomicBoolean(false);
      this.ss = null;

   }

   /**
    *
    * Start method for bean lifecycle
    *
    * @throws Throwable in case of error
    */
   public void start() throws Throwable
   {
      if (!running.get())

      {
         InetSocketAddress address = new InetSocketAddress(host, port);

         ss = new ServerSocket();
         ss.bind(address);

         running.set(true);

         for (Map.Entry<String, String> entry : this.getWorkManagers().entrySet())
         {
            sendMessage(entry.getValue(), Request.JOIN, this.getDistributedWorkManager().getId(),
                    this.getHost() + ":" + this.getPort());
         }

      }
   }

   /**
    *
    * Stop method for bean lifecycle
    *
    * @throws Throwable in case of error
    */
   public void stop() throws Throwable
   {
      ss.close();
      for (Map.Entry<String, String> entry : this.getWorkManagers().entrySet())
      {
         sendMessage(entry.getValue(), Request.LEAVE, this.getDistributedWorkManager().getId());
      }
      running.set(false);
   }

   private Long sendMessage(String address, Request request, Serializable... parameters)
      throws WorkException
   {

      String[] addressPart = address.split(":");
      Socket socket = null;
      ObjectOutputStream oos = null;
      try
      {
         socket = new Socket(addressPart[0], Integer.valueOf(addressPart[1]));

         oos = new ObjectOutputStream(socket.getOutputStream());
         oos.writeInt(request.ordinal());
         oos.writeInt(request.getNumberOfParameter());
         for (Serializable o : parameters)
         {
            oos.writeObject(o);
         }

         oos.flush();

         return parseResponse(socket);

      }
      catch (Throwable t)
      {
         if (log.isDebugEnabled())
         {
            log.debug("error sending command");
         }
         if (t instanceof WorkException)
         {
            throw (WorkException) t;
         }
         else
         {
            throw new WorkException(t);
         }
      }
      finally
      {

         try
         {
            oos.close();
            socket.close();
         }
         catch (IOException e)
         {
            //ignore it
         }

      }
   }

   private Long parseResponse(Socket socket) throws Throwable
   {
      ObjectInputStream ois = null;

      try
      {
         ois = new ObjectInputStream(socket.getInputStream());

         int commandOrdinalPosition = ois.readInt();
         int numberOfParameter = ois.readInt();
         Response response = Response.values()[commandOrdinalPosition];

         switch (response)
         {
            case VOID_OK : {
               return 0L;

            }
            case LONG_OK : {
               return ois.readLong();

            }
            case WORK_EXCEPTION : {
               WorkException we = (WorkException) ois.readObject();
               throw we;

            }
            case GENERIC_EXCEPTION : {
               Throwable t = (Throwable) ois.readObject();
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

   /**
    * {@inheritDoc}
    */
   @Override
   public void setDistributedWorkManager(DistributedWorkManager dwm)
   {
      this.dwm = dwm;
   }

   /**
    * get The distributed work manager
    * @return the ditributed work manager
    */
   public DistributedWorkManager getDistributedWorkManager()
   {
      return dwm;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long ping(String dwm)
   {
      long start = System.currentTimeMillis();

      try
      {
         sendMessage(dwm, Request.PING);
      }
      catch (WorkException e1)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error", e1);
         }
         return Long.MAX_VALUE;
      }

      return System.currentTimeMillis() - start;

   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doWork(String id, DistributableWork work) throws WorkException
   {

      String dwm = workManagers.get(id);

      sendMessage(dwm, Request.DO_WORK, work);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void scheduleWork(String id, DistributableWork work) throws WorkException
   {
      String dwm = workManagers.get(id);

      sendMessage(dwm, Request.SCHEDULE_WORK, work);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long startWork(String id, DistributableWork work) throws WorkException
   {
      String dwm = workManagers.get(id);

      return sendMessage(dwm, Request.START_WORK, work);

   }

   /**
    * Get the host.
    *
    * @return the host.
    */
   public String getHost()
   {
      return host;
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
    * Get the port.
    *
    * @return the port.
    */
   public int getPort()
   {
      return port;
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
    * Get the executorService.
    *
    * @return the executorService.
    */
   public ExecutorService getExecutorService()
   {
      return executorService;
   }

   /**
    * Set the executorService.
    *
    * @param executorService The executorService to set.
    */
   public void setExecutorService(ExecutorService executorService)
   {
      this.executorService = executorService;
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
         catch (IOException ioe)
         {
            if (log.isTraceEnabled())
               log.trace(ioe.getMessage());
         }
      }

   }

   /**
    * Get the workManagers.
    *
    * @return the workManagers.
    */
   public Map<String, String> getWorkManagers()
   {
      return workManagers;
   }

   /**
    * Set the workManagers.
    *
    * @param workManagers The workManagers to set.
    */
   public void setWorkManagers(Map<String, String> workManagers)
   {
      this.workManagers = workManagers;
   }

   /**
    * Get the timeout.
    *
    * @return the timeout.
    */
   public long getTimeout()
   {
      return timeout;
   }

   /**
    * Set the timeout.
    *
    * @param timeout The timeout to set.
    */
   public void setTimeout(long timeout)
   {
      this.timeout = timeout;
   }

   @Override
   public String toString()
   {
      return "SocketTransport [dwm=" + dwm + ", executorService=" + executorService + ", host=" + host + ", port=" +
             port + ", running=" + running + ", ss=" + ss + ", workManagers=" + workManagers + ", timeout=" +
             timeout + "]";
   }

}
