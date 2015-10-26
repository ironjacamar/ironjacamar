/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.workmanager.ClassBundle;
import org.jboss.jca.core.workmanager.WorkClassLoader;
import org.jboss.jca.core.workmanager.WorkObjectInputStream;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Response;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The communication between client and server
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Communication implements Runnable
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, Communication.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

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
      WorkObjectInputStream wois = null;
      ObjectOutputStream oos = null;
      Serializable returnValue = null;
      Response response = null;
      try
      {
         wois = new WorkObjectInputStream(socket.getInputStream());
         int commandOrdinalPosition = wois.readInt();
         int numberOfParameters = wois.readInt();

         Request command = Request.values()[commandOrdinalPosition];

         switch (command)
         {
            case JOIN : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters, "JOIN"));

               String address = (String)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: JOIN(%s)", socket.getInetAddress(), address);

               Set<Address> workManagers = 
                  (Set<Address>)transport.sendMessage(address, Request.GET_WORKMANAGERS);
               if (workManagers != null)
               {
                  for (Address a : workManagers)
                  {
                     transport.join(a, address);

                     long shortRunningFree =
                        (long)transport.sendMessage(address, Request.GET_SHORTRUNNING_FREE, a);
                     long longRunningFree =
                        (long)transport.sendMessage(address, Request.GET_LONGRUNNING_FREE, a);

                     transport.localUpdateShortRunningFree(a, shortRunningFree);
                     transport.localUpdateLongRunningFree(a, longRunningFree);
                  }
               }

               response = Response.OK_VOID;
               break;
            }
            case LEAVE : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters, "LEAVE"));

               String address = (String)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: LEAVE(%s)", socket.getInetAddress(), address);

               transport.leave(address);
               response = Response.OK_VOID;
               break;
            }
            case GET_WORKMANAGERS : {
               if (numberOfParameters != 0)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "GET_WORKMANAGERS"));

               if (log.isTraceEnabled())
                  log.tracef("%s: GET_WORKMANAGERS()", socket.getInetAddress());

               returnValue = (Serializable)transport.getAddresses(transport.getOwnAddress());
               response = Response.OK_SERIALIZABLE;

               break;
            }
            case WORKMANAGER_ADD : {
               if (numberOfParameters != 2)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "WORKMANAGER_ADD"));

               Address id = (Address)wois.readObject();
               String address = (String)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: WORKMANAGER_ADD(%s, %s)", socket.getInetAddress(), id, address);

               transport.localWorkManagerAdd(id, address);
               response = Response.OK_VOID;

               break;
            }
            case WORKMANAGER_REMOVE : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "WORKMANAGER_REMOVE"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: WORKMANAGER_REMOVE(%s)", socket.getInetAddress(), id);

               transport.localWorkManagerRemove(id);
               response = Response.OK_VOID;

               break;
            }
            case PING : {
               if (numberOfParameters != 0)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "PING"));

               if (log.isTraceEnabled())
                  log.tracef("%s: PING()", socket.getInetAddress());

               transport.localPing();
               response = Response.OK_VOID;

               break;
            }
            case DO_WORK : {
               if (numberOfParameters != 3)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DO_WORK"));

               Address id = (Address)wois.readObject();
               ClassBundle cb = (ClassBundle)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("DO_WORK/ClassBundle: %s", cb);

               WorkClassLoader wcl = SecurityActions.createWorkClassLoader(cb);
               wois.setWorkClassLoader(wcl);

               DistributableWork work = (DistributableWork)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DO_WORK(%s, %s)", socket.getInetAddress(), id, work);

               transport.localDoWork(id, work);
               response = Response.OK_VOID;

               break;
            }
            case START_WORK : {
               if (numberOfParameters != 3)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "START_WORK"));

               Address id = (Address)wois.readObject();
               ClassBundle cb = (ClassBundle)wois.readObject();

               log.tracef("START_WORK/ClassBundle: %s", cb);

               WorkClassLoader wcl = SecurityActions.createWorkClassLoader(cb);
               wois.setWorkClassLoader(wcl);

               DistributableWork work = (DistributableWork)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: START_WORK(%s, %s)", socket.getInetAddress(), id, work);

               returnValue = transport.localStartWork(id, work);
               response = Response.OK_SERIALIZABLE;

               break;
            }
            case SCHEDULE_WORK : {
               if (numberOfParameters != 3)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "SCHEDULE_WORK"));

               Address id = (Address)wois.readObject();
               ClassBundle cb = (ClassBundle)wois.readObject();

               log.tracef("SCHEDULE_WORK/ClassBundle: %s", cb);

               WorkClassLoader wcl = SecurityActions.createWorkClassLoader(cb);
               wois.setWorkClassLoader(wcl);

               DistributableWork work = (DistributableWork)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: SCHEDULE_WORK(%s, %s)", socket.getInetAddress(), id, work);

               transport.localScheduleWork(id, work);
               response = Response.OK_VOID;

               break;
            }
            case GET_SHORTRUNNING_FREE : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "GET_SHORTRUNNING_FREE"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: GET_SHORTRUNNING_FREE(%s)", socket.getInetAddress(), id);

               returnValue = transport.localGetShortRunningFree(id);
               response = Response.OK_SERIALIZABLE;

               break;
            }
            case GET_LONGRUNNING_FREE : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "GET_LONGRUNNING_FREE"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: GET_LONGRUNNING_FREE(%s)", socket.getInetAddress(), id);

               returnValue = transport.localGetLongRunningFree(id);
               response = Response.OK_SERIALIZABLE;

               break;
            }
            case UPDATE_SHORTRUNNING_FREE : {
               if (numberOfParameters != 2)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "UPDATE_SHORTRUNNING_FREE"));

               Address id = (Address)wois.readObject();
               Long freeCount = (Long)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: UPDATE_SHORTRUNNING_FREE(%s, %d)", socket.getInetAddress(), id, freeCount);

               transport.localUpdateShortRunningFree(id, freeCount);
               response = Response.OK_VOID;

               break;
            }
            case UPDATE_LONGRUNNING_FREE : {
               if (numberOfParameters != 2)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "UPDATE_LONGRUNNING_FREE"));

               Address id = (Address)wois.readObject();
               Long freeCount = (Long)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: UPDATE_LONGRUNNING_FREE(%s, %d)", socket.getInetAddress(), id, freeCount);

               transport.localUpdateLongRunningFree(id, freeCount);
               response = Response.OK_VOID;

               break;
            }
            case GET_DISTRIBUTED_STATISTICS : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "GET_DISTRIBUTED_STATISTICS"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: GET_DISTRIBUTED_STATISTICS(%s)", socket.getInetAddress(), id);

               returnValue = transport.localGetDistributedStatistics(id);
               response = Response.OK_SERIALIZABLE;

               break;
            }
            case CLEAR_DISTRIBUTED_STATISTICS : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "CLEAR_DISTRIBUTED_STATISTICS"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: CLEAR_DISTRIBUTED_STATISTICS(%s)", socket.getInetAddress(), id);

               transport.localClearDistributedStatistics(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_DOWORK_ACCEPTED : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_DOWORK_ACCEPTED"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_DOWORK_ACCEPTED(%s)", socket.getInetAddress(), id);

               transport.localDeltaDoWorkAccepted(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_DOWORK_REJECTED : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_DOWORK_REJECTED"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_DOWORK_REJECTED(%s)", socket.getInetAddress(), id);

               transport.localDeltaDoWorkRejected(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_STARTWORK_ACCEPTED : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_STARTWORK_ACCEPTED"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_STARTWORK_ACCEPTED(%s)", socket.getInetAddress(), id);

               transport.localDeltaStartWorkAccepted(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_STARTWORK_REJECTED : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_STARTWORK_REJECTED"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_STARTWORK_REJECTED(%s)", socket.getInetAddress(), id);

               transport.localDeltaStartWorkRejected(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_SCHEDULEWORK_ACCEPTED : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_SCHEDULEWORK_ACCEPTED"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_SCHEDULEWORK_ACCEPTED(%s)", socket.getInetAddress(), id);

               transport.localDeltaScheduleWorkAccepted(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_SCHEDULEWORK_REJECTED : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_SCHEDULEWORK_REJECTED"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_SCHEDULEWORK_REJECTED(%s)", socket.getInetAddress(), id);

               transport.localDeltaScheduleWorkRejected(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_WORK_SUCCESSFUL : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_WORK_SUCCESSFUL"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_WORK_SUCCESSFUL(%s)", socket.getInetAddress(), id);

               transport.localDeltaWorkSuccessful(id);
               response = Response.OK_VOID;

               break;
            }
            case DELTA_WORK_FAILED : {
               if (numberOfParameters != 1)
                  throw new IllegalArgumentException(bundle.invalidNumberOfParameters(numberOfParameters,
                                                                                      "DELTA_WORK_FAILED"));

               Address id = (Address)wois.readObject();

               if (log.isTraceEnabled())
                  log.tracef("%s: DELTA_WORK_FAILED(%s)", socket.getInetAddress(), id);

               transport.localDeltaWorkFailed(id);
               response = Response.OK_VOID;

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
         if (log.isTraceEnabled())
            log.tracef("%s: WORK_EXCEPTION(%s)", socket.getInetAddress(), we.getMessage());

         sendResponse(Response.WORK_EXCEPTION, we);
      }
      catch (Throwable t)
      {
         if (log.isTraceEnabled())
            log.tracef("%s: THROWABLE(%s)", socket.getInetAddress(), t.getMessage());

         sendResponse(Response.GENERIC_EXCEPTION, t);
      }
      finally
      {
         if (wois != null)
         {
            try
            {
               wois.close();
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
      if (log.isTraceEnabled())
         log.tracef("Sending response: %s with %s", response,
                    parameters != null ? Arrays.toString(parameters) : "null");

      ObjectOutputStream oos = null;
      try
      {
         oos = new ObjectOutputStream(socket.getOutputStream());
         oos.writeInt(response.ordinal());
         oos.writeInt(response.getNumberOfParameter());
         if (response.getNumberOfParameter() > 0 && parameters != null)
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
            log.debugf("Error sending response: %s", t.getMessage());
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
