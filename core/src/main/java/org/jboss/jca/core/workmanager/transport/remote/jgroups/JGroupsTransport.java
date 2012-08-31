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

package org.jboss.jca.core.workmanager.transport.remote.jgroups;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.workmanager.DistributedWorkManagerImpl;
import org.jboss.jca.core.workmanager.transport.remote.AbstractRemoteTransport;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.ResponseValues;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Messages;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.MethodLookup;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

/**
 * The socket transport
 *
 * @author <a href="mailto:stefano.maestri@redhat.com">Stefano Maestri</a>
 */
public class JGroupsTransport extends AbstractRemoteTransport<Address> implements MembershipListener
{
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** The JChannel used by this transport **/

   private JChannel channel;

   /** the cluster name to join **/
   private String clusterName;

   private RpcDispatcher disp;

   /** Trace logging */
   private static boolean trace = log.isTraceEnabled();

   private static final short JOIN_METHOD = 1;

   private static final short LEAVE_METHOD = 2;

   private static final short PING_METHOD = 3;

   private static final short DO_WORK_METHOD = 4;

   private static final short START_WORK_METHOD = 5;

   private static final short SCHEDULE_WORK_METHOD = 6;

   private static final short GET_SHORTRUNNING_FREE_METHOD = 7;

   private static final short GET_LONGRUNNING_FREE_METHOD = 8;

   private static final short UPDATE_SHORTRUNNING_FREE_METHOD = 9;

   private static final short UPDATE_LONGRUNNING_FREE_METHOD = 10;

   private static Map<Short, Method> methods = new HashMap<Short, Method>(10);

   static
   {
      try
      {
         methods.put(JOIN_METHOD, AbstractRemoteTransport.class.getMethod("join", String.class, Address.class));

         methods.put(LEAVE_METHOD, AbstractRemoteTransport.class.getMethod("leave", String.class));

         methods.put(PING_METHOD, AbstractRemoteTransport.class.getMethod("localPing"));

         methods.put(DO_WORK_METHOD, AbstractRemoteTransport.class.getMethod("localDoWork", DistributableWork.class));

         methods.put(START_WORK_METHOD,
            AbstractRemoteTransport.class.getMethod("localStartWork", DistributableWork.class));

         methods.put(SCHEDULE_WORK_METHOD,
            AbstractRemoteTransport.class.getMethod("localScheduleWork", DistributableWork.class));

         methods.put(GET_SHORTRUNNING_FREE_METHOD,
            AbstractRemoteTransport.class.getMethod("localGetShortRunningFree"));

         methods.put(GET_LONGRUNNING_FREE_METHOD, AbstractRemoteTransport.class.getMethod("localGetLongRunningFree"));

         methods.put(UPDATE_SHORTRUNNING_FREE_METHOD,
            AbstractRemoteTransport.class.getMethod("localUpdateShortRunningFree", String.class, Long.class));

         methods.put(UPDATE_LONGRUNNING_FREE_METHOD,
            AbstractRemoteTransport.class.getMethod("localUpdateLongRunningFree", String.class, Long.class));

      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Constructor
    */
   public JGroupsTransport()
   {
      this.dwm = null;
      this.executorService = null;
      this.workManagers = Collections.synchronizedMap(new HashMap<String, Address>());

   }

   @Override
   protected void init()
   {
      try
      {
         ((DistributedWorkManagerImpl) dwm).setId(channel.getAddressAsString());
         disp = new RpcDispatcher(channel, null, this, this);

         disp.setMethodLookup(new MethodLookup()
         {

            @Override
            public Method findMethod(short key)
            {
               return methods.get(key);
            }
         });

         channel.connect("clusterName");
      }
      catch (Exception e)
      {
         log.error("Error", e);
      }
   }
   /**
    * Start method for bean lifecycle
    *
    * @throws Throwable in case of error
    */
   public void start() throws Throwable
   {




   }

   /**
    * Stop method for bean lifecycle
    *
    * @throws Throwable in case of error
    */
   public void stop() throws Throwable
   {
      //sendMessage(null, Request.LEAVE, dwm.getId());

      disp.stop();

      channel.close();

   }

   @Override
   public Long sendMessage(Address destAddress, Request request, Serializable... parameters)
      throws WorkException
   {
      Long returnValue = -1L;

      log.tracef("%s: sending message2=%s to %s", channel.getAddressAsString(), request, destAddress);
      RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, 10000);
      try
      {
         switch (request)
         {
            case JOIN : {
               String id = (String) parameters[0];
               Address joiningAddress = (Address) parameters[1];
               List<Address> dests = destAddress == null ? null : Arrays.asList(destAddress);

               RspList<ResponseValues> rspList = disp.callRemoteMethods(dests, new MethodCall(JOIN_METHOD,
                                                                                              id,
                                                                                              joiningAddress),
                  opts);
               throwWorkExceptionIfHasExption(rspList);
               returnValue = 0L;
               break;
            }
            case LEAVE : {
               String id = (String) parameters[0];

               List<Address> dests = destAddress == null ? null : Arrays.asList(destAddress);

               RspList<ResponseValues> rspList = disp.callRemoteMethods(dests, new MethodCall(LEAVE_METHOD,
                                                                                              id), opts);
               throwWorkExceptionIfHasExption(rspList);
               returnValue = 0L;
               break;
            }
            case PING : {
               try
               {
                  returnValue = (Long) disp.callRemoteMethod(destAddress, new MethodCall(PING_METHOD),
                     opts);
               }
               catch (Exception e)
               {
                  throw new WorkException(e);
               }

               break;
            }
            case DO_WORK : {
               DistributableWork work = (DistributableWork) parameters[0];
               try
               {
                  disp.callRemoteMethod(destAddress, new MethodCall(DO_WORK_METHOD, work), opts);
                  returnValue = 0L;
               }
               catch (Exception e)
               {
                  throw new WorkException(e);
               }
               break;
            }
            case START_WORK : {
               DistributableWork work = (DistributableWork) parameters[0];

               returnValue = (Long) disp.callRemoteMethod(destAddress, new MethodCall(START_WORK_METHOD,
                                                                                      work), opts);

               break;
            }
            case SCHEDULE_WORK : {
               DistributableWork work = (DistributableWork) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(SCHEDULE_WORK_METHOD, work), opts);
               returnValue = 0L;

               break;
            }
            case GET_SHORTRUNNING_FREE : {
               returnValue = (Long) disp.callRemoteMethod(destAddress,
                  new MethodCall(GET_SHORTRUNNING_FREE_METHOD), opts);

               break;
            }
            case GET_LONGRUNNING_FREE : {
               returnValue = (Long) disp.callRemoteMethod(destAddress,
                  new MethodCall(GET_LONGRUNNING_FREE_METHOD),
                  opts);
               break;
            }
            case UPDATE_SHORTRUNNING_FREE : {
               String id = (String) parameters[0];
               Long freeCount = (Long) parameters[1];

               disp.callRemoteMethod(destAddress,
                  new MethodCall(UPDATE_SHORTRUNNING_FREE_METHOD, id, freeCount), opts);

               returnValue = 0L;

               break;
            }
            case UPDATE_LONGRUNNING_FREE : {
               String id = (String) parameters[0];
               Long freeCount = (Long) parameters[1];
               disp.callRemoteMethod(destAddress,
                  new MethodCall(UPDATE_LONGRUNNING_FREE_METHOD, id, freeCount), opts);
               break;
            }
            default :
               if (log.isDebugEnabled())
               {
                  log.debug("Unknown command received on socket Transport");
               }
               break;
         }
      }
      catch (Exception e)
      {
         throw new WorkException(e);
      }
      return returnValue;

   }


   private void throwWorkExceptionIfHasExption(RspList<ResponseValues> rspList) throws WorkException
   {
      if (rspList != null && rspList.getFirst() != null)
      {
         for (Rsp<ResponseValues> rsp : rspList)
         {
            if (rsp.hasException())
            {
               throw new WorkException(rsp.getException());
            }
         }

      }
   }


   @Override
   public String toString()
   {
      return "JGroupsTransport [channel=" + channel + ", clustername=" + clusterName +
             ", dwm=" + dwm + ", executorService=" + executorService + ", workManagers=" + workManagers + "]";
   }

   /**
    * Get the channel.
    *
    * @return the channel.
    */
   public final JChannel getChannel()
   {
      return channel;
   }

   /**
    * Set the channel.
    *
    * @param channel The channel to set.
    */
   public final void setChannel(JChannel channel)
   {
      this.channel = channel;
   }

   /**
    * Get the clustername.
    *
    * @return the clustername.
    */
   public final String getClusterName()
   {
      return clusterName;
   }

   /**
    * Set the clustername.
    *
    * @param clustername The clustername to set.
    */
   public final void setClusterName(String clustername)
   {
      this.clusterName = clustername;
   }

   @Override
   public void viewAccepted(View view)
   {
      log.tracef("java.net.preferIPv4Stack=%s", System.getProperty("java.net.preferIPv4Stack"));
      log.tracef("viewAccepted called w/ View=%s", view);
      synchronized (this)
      {
         for (Entry<String, Address> entry : workManagers.entrySet())
         {
            if (!view.containsMember(entry.getValue()))
            {
               leave(entry.getKey());
            }
         }
         for (Address address : view.getMembers())
         {
            if (!workManagers.containsKey(address.toString()))
            {
               join(address.toString(), address);
               Long shortRunning = getShortRunningFree(address.toString());
               Long longRunning = getShortRunningFree(address.toString());

               localUpdateShortRunningFree(address.toString(), shortRunning);
               localUpdateLongRunningFree(address.toString(), longRunning);
            }
         }
      }

   }

   @Override
   public void block()
   {
      log.tracef("Block called");

   }

   @Override
   public void suspect(Address arg0)
   {
      log.tracef("suspect called w/ Adress=%s", arg0);

   }

   @Override
   public void unblock()
   {
      log.tracef("UnBlock called");

   }

}
