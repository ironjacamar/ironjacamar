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
      super();
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
      Response response = Response.VOID_OK;
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
               transport.getWorkManagers().put(id, address);
               ((NotificationListener) transport.getDistributedWorkManager().getSelector()).join(id);
               response = Response.VOID_OK;
               break;
            }
            case LEAVE : {
               String id = ois.readUTF();
               ((NotificationListener) transport.getDistributedWorkManager().getSelector()).leave(id);
               response = Response.VOID_OK;
               break;
            }
            case PING : {
               //do nothing, just send an answer.
               response = Response.VOID_OK;

               break;
            }
            case DO_WORK : {
               DistributableWork work = (DistributableWork) ois.readObject();
               transport.getDistributedWorkManager().localDoWork(work);
               response = Response.VOID_OK;

               break;
            }
            case START_WORK : {
               DistributableWork work = (DistributableWork) ois.readObject();
               returnValue = transport.getDistributedWorkManager().localStartWork(work);
               response = Response.LONG_OK;

               break;
            }
            case SCHEDULE_WORK : {
               DistributableWork work = (DistributableWork) ois.readObject();
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

         sendResponse(response, returnValue);

      }
      catch (WorkException we)
      {
         sendResponse(Response.WORK_EXCEPTION, we);
      }
      catch (Throwable t)
      {

         sendResponse(Response.GENERIC_EXCEPTION, t);

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

   private void sendResponse(Response response, Serializable... parameters)
   {
      ObjectOutputStream oos = null;
      try
      {
         oos = new ObjectOutputStream(socket.getOutputStream());
         oos.writeInt(response.ordinal());
         oos.writeInt(response.getNumberOfParameter());
         for (Serializable o : parameters)
         {
            oos.writeObject(o);
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
