/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;

/**
 * The communication between client and server
 * @author <a href="mailto:jesper.pedersen@comcast.net">Jesper Pedersen</a>
 */
public class Communication implements Runnable
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, SocketTransport.class.getName());

   /** Trace logging */
   private static boolean trace = log.isTraceEnabled();

   /** The socket */
   private final Socket socket;

   /** The trasport **/
   private final SocketTransport transport;

   /**
    * Create a new Communication.
    *
    * @param socket The socket
    * @param transport The Transport
    */
   public Communication(SocketTransport transport, Socket socket)
   {
      this.socket = socket;
      this.transport = transport;
   }

   /**
    * Run
    */
   public void run()
   {
      ObjectInputStream ois = null;
      ObjectOutputStream oos = null;
      Long returnValue = 0L;
      Response response = null;
      try
      {
         ois = new ObjectInputStream(socket.getInputStream());
         int commandOrdinalPosition = ois.readInt();
         int numberOfParameter = ois.readInt();

         Request command = Request.values()[commandOrdinalPosition];

         switch (command)
         {
            case JOIN : {
               String id = ois.readUTF();
               String address = ois.readUTF();

               if (trace)
                  log.tracef("%s: JOIN(%s, %s)", socket.getInetAddress(), id, address);

               transport.getWorkManagers().put(id, address);

               if (transport.getDistributedWorkManager().getPolicy() instanceof NotificationListener)
               {
                  ((NotificationListener) transport.getDistributedWorkManager().getPolicy()).join(id);
               }
               if (transport.getDistributedWorkManager().getSelector() instanceof NotificationListener)
               {
                  ((NotificationListener) transport.getDistributedWorkManager().getSelector()).join(id);
               }

               response = Response.VOID_OK;
               break;
            }
            case LEAVE : {
               String id = ois.readUTF();

               if (trace)
                  log.tracef("%s: LEAVE(%s)", socket.getInetAddress(), id);

               if (transport.getDistributedWorkManager().getPolicy() instanceof NotificationListener)
               {
                  ((NotificationListener) transport.getDistributedWorkManager().getPolicy()).leave(id);
               }
               if (transport.getDistributedWorkManager().getSelector() instanceof NotificationListener)
               {
                  ((NotificationListener) transport.getDistributedWorkManager().getSelector()).leave(id);
               }

               response = Response.VOID_OK;
               break;
            }
            case PING : {
               //do nothing, just send an answer.
               if (trace)
                  log.tracef("%s: PING()", socket.getInetAddress());

               response = Response.VOID_OK;

               break;
            }
            case DO_WORK : {
               DistributableWork work = (DistributableWork) ois.readObject();

               if (trace)
                  log.tracef("%s: DO_WORK(%s)", socket.getInetAddress(), work);

               transport.getDistributedWorkManager().localDoWork(work);
               response = Response.VOID_OK;

               break;
            }
            case START_WORK : {
               DistributableWork work = (DistributableWork) ois.readObject();

               if (trace)
                  log.tracef("%s: START_WORK(%s)", socket.getInetAddress(), work);

               returnValue = transport.getDistributedWorkManager().localStartWork(work);
               response = Response.LONG_OK;

               break;
            }
            case SCHEDULE_WORK : {
               DistributableWork work = (DistributableWork) ois.readObject();

               if (trace)
                  log.tracef("%s: SCHEDULE_WORK(%s)", socket.getInetAddress(), work);

               transport.getDistributedWorkManager().localScheduleWork(work);
               response = Response.VOID_OK;

               break;
            }
            default :
               if (log.isDebugEnabled())
               {
                  log.debug("Unknown command received on socket Transport");
               }
               break;
         }

         if (response != null)
         {
            sendResponse(response, returnValue);
         }
         else
         {
            sendResponse(Response.GENERIC_EXCEPTION, new Exception("Unknown command: " + commandOrdinalPosition));
         }
      }
      catch (WorkException we)
      {
         if (trace)
            log.tracef("%s: WORK_EXCEPTION(%s)", socket.getInetAddress(), we);

         sendResponse(Response.WORK_EXCEPTION, we);
      }
      catch (Throwable t)
      {
         if (trace)
            log.tracef("%s: THROWABLE(%s)", socket.getInetAddress(), t);

         sendResponse(Response.GENERIC_EXCEPTION, t);
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

   private void sendResponse(Response response, Serializable... parameters)
   {
      ObjectOutputStream oos = null;
      try
      {
         oos = new ObjectOutputStream(socket.getOutputStream());
         oos.writeInt(response.ordinal());
         oos.writeInt(response.getNumberOfParameter());
         if (parameters != null)
         {
            for (Serializable o : parameters)
            {
               oos.writeObject(o);
            }
         }

         oos.flush();

      }
      catch (Throwable t)
      {
         if (log.isDebugEnabled())
         {
            log.debug("error sending command");
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
      }
   }
}
