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
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.workmanager.ClassBundle;
import org.jboss.jca.core.workmanager.transport.remote.AbstractRemoteTransport;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.ResponseValues;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

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
public class JGroupsTransport extends AbstractRemoteTransport<org.jgroups.Address> implements MembershipListener
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, JGroupsTransport.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** The JChannel used by this transport **/
   private JChannel channel;

   /** the cluster name to join **/
   private String clusterName;

   private RpcDispatcher disp;

   private boolean initialized;

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

   private static final short GET_DISTRIBUTED_STATISTICS_METHOD = 11;

   private static final short DELTA_DOWORK_ACCEPTED_METHOD = 12;

   private static final short DELTA_DOWORK_REJECTED_METHOD = 13;

   private static final short DELTA_STARTWORK_ACCEPTED_METHOD = 14;

   private static final short DELTA_STARTWORK_REJECTED_METHOD = 15;

   private static final short DELTA_SCHEDULEWORK_ACCEPTED_METHOD = 16;

   private static final short DELTA_SCHEDULEWORK_REJECTED_METHOD = 17;

   private static final short DELTA_WORK_SUCCESSFUL_METHOD = 18;

   private static final short DELTA_WORK_FAILED_METHOD = 19;

   private static Map<Short, Method> methods = new HashMap<Short, Method>();

   static
   {
      try
      {
         methods.put(JOIN_METHOD, 
                     JGroupsTransport.class.getMethod("join",
                                                      org.jboss.jca.core.spi.workmanager.Address.class,
                                                      org.jgroups.Address.class));
         
         methods.put(LEAVE_METHOD, 
                     JGroupsTransport.class.getMethod("leave",
                                                      org.jgroups.Address.class));
         
         methods.put(PING_METHOD,
                     AbstractRemoteTransport.class.getMethod("localPing"));

         methods.put(DO_WORK_METHOD, 
                     AbstractRemoteTransport.class.getMethod("localDoWork",
                                                             org.jboss.jca.core.spi.workmanager.Address.class,
                                                             DistributableWork.class));

         methods.put(START_WORK_METHOD,
                     AbstractRemoteTransport.class.getMethod("localStartWork",
                                                             org.jboss.jca.core.spi.workmanager.Address.class,
                                                             DistributableWork.class));

         methods.put(SCHEDULE_WORK_METHOD,
                     AbstractRemoteTransport.class.getMethod("localScheduleWork",
                                                             org.jboss.jca.core.spi.workmanager.Address.class,
                                                             DistributableWork.class));

         methods.put(GET_SHORTRUNNING_FREE_METHOD,
                     AbstractRemoteTransport.class.getMethod("localGetShortRunningFree",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(GET_LONGRUNNING_FREE_METHOD,
                     AbstractRemoteTransport.class.getMethod("localGetLongRunningFree",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(UPDATE_SHORTRUNNING_FREE_METHOD,
                     AbstractRemoteTransport.class.getMethod("localUpdateShortRunningFree",
                                                             org.jboss.jca.core.spi.workmanager.Address.class,
                                                             Long.class));

         methods.put(UPDATE_LONGRUNNING_FREE_METHOD,
                     AbstractRemoteTransport.class.getMethod("localUpdateLongRunningFree",
                                                             org.jboss.jca.core.spi.workmanager.Address.class,
                                                             Long.class));

         methods.put(GET_DISTRIBUTED_STATISTICS_METHOD,
                     AbstractRemoteTransport.class.getMethod("localGetDistributedStatistics",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(DELTA_DOWORK_ACCEPTED_METHOD,
                     AbstractRemoteTransport.class.getMethod("localDeltaDoWorkAccepted",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(DELTA_DOWORK_REJECTED_METHOD,
                     AbstractRemoteTransport.class.getMethod("localDeltaDoWorkRejected",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(DELTA_STARTWORK_ACCEPTED_METHOD,
                     AbstractRemoteTransport.class.getMethod("localDeltaStartWorkAccepted",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));
         methods.put(DELTA_STARTWORK_REJECTED_METHOD,
                     AbstractRemoteTransport.class.getMethod("localDeltaStartWorkRejected",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(DELTA_SCHEDULEWORK_ACCEPTED_METHOD,
                     AbstractRemoteTransport.class.getMethod("localDeltaScheduleWorkAccepted",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));
         methods.put(DELTA_SCHEDULEWORK_REJECTED_METHOD,
                     AbstractRemoteTransport.class.getMethod("localDeltaScheduleWorkRejected",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(DELTA_WORK_SUCCESSFUL_METHOD, AbstractRemoteTransport.class.getMethod("localDeltaWorkSuccessful",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));

         methods.put(DELTA_WORK_FAILED_METHOD, AbstractRemoteTransport.class.getMethod("localDeltaWorkFailed",
                                                             org.jboss.jca.core.spi.workmanager.Address.class));
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
      super();
      this.channel = null;
      this.clusterName = null;
      this.disp = null;
      this.initialized = false;
   }

   /**
    * Delegator
    * @param logicalAddress The logical address
    * @param address The address
    */
   public void join(org.jboss.jca.core.spi.workmanager.Address logicalAddress, org.jgroups.Address address)
   {
      super.join(logicalAddress, address);
   }

   /**
    * Delegator
    * @param address The address
    */
   public void leave(org.jgroups.Address address)
   {
      super.leave(address);
   }

   /**
    * {@inheritDoc}
    */
   public void startup() throws Throwable
   {
      disp = new RpcDispatcher(channel, null, this, this);

      disp.setMethodLookup(new MethodLookup()
      {
         @Override
         public Method findMethod(short key)
         {
            return methods.get(key);
         }
      });

      if (clusterName == null)
         clusterName = "jca";

      channel.connect(clusterName);
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
      initialized = true;
   }

   /**
    * {@inheritDoc}
    */
   public void shutdown() throws Throwable
   {
      if (disp != null)
         disp.stop();

      if (channel != null)
         channel.close();
   }

   @Override
   public Serializable sendMessage(org.jgroups.Address destAddress, Request request, Serializable... parameters)
      throws WorkException
   {
      Serializable returnValue = null;

      if (trace)
         log.tracef("%s: sending message=%s to %s", channel.getAddressAsString(), request, destAddress);

      RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, 10000);
      try
      {
         switch (request)
         {
            case JOIN : {
               org.jgroups.Address joiningAddress = (org.jgroups.Address) parameters[0];
               List<org.jgroups.Address> dests = destAddress == null ? null : Arrays.asList(destAddress);

               RspList<ResponseValues> rspList = disp.callRemoteMethods(dests, new MethodCall(JOIN_METHOD,
                                                                                              joiningAddress),
                  opts);
               throwWorkExceptionIfHasExption(rspList);
               break;
            }
            case LEAVE : {
               org.jgroups.Address leavingAddress = (org.jgroups.Address) parameters[0];

               List<org.jgroups.Address> dests = destAddress == null ? null : Arrays.asList(destAddress);

               RspList<ResponseValues> rspList = disp.callRemoteMethods(dests, new MethodCall(LEAVE_METHOD,
                                                                                              leavingAddress), opts);
               throwWorkExceptionIfHasExption(rspList);
               break;
            }
            case GET_WORKMANAGERS : {

               // TODO

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
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];
               ClassBundle cb = (ClassBundle)parameters[1];
               DistributableWork work = (DistributableWork) parameters[2];
               try
               {
                  disp.callRemoteMethod(destAddress,
                                        new MethodCall(DO_WORK_METHOD, address, work), opts);
               }
               catch (Exception e)
               {
                  throw new WorkException(e);
               }
               break;
            }
            case START_WORK : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];
               ClassBundle cb = (ClassBundle)parameters[1];
               DistributableWork work = (DistributableWork) parameters[2];

               returnValue = (Long) disp.callRemoteMethod(destAddress,
                                                          new MethodCall(START_WORK_METHOD, address,
                                                                         work), opts);

               break;
            }
            case SCHEDULE_WORK : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];
               ClassBundle cb = (ClassBundle)parameters[1];
               DistributableWork work = (DistributableWork) parameters[2];

               disp.callRemoteMethod(destAddress,
                                     new MethodCall(SCHEDULE_WORK_METHOD, address, work), opts);

               break;
            }
            case GET_SHORTRUNNING_FREE : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               returnValue = (Long) disp.callRemoteMethod(destAddress,
                                                          new MethodCall(GET_SHORTRUNNING_FREE_METHOD,
                                                                         address), opts);

               break;
            }
            case GET_LONGRUNNING_FREE : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               returnValue = (Long) disp.callRemoteMethod(destAddress,
                                                          new MethodCall(GET_LONGRUNNING_FREE_METHOD,
                                                                         address), opts);
               break;
            }
            case UPDATE_SHORTRUNNING_FREE : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];
               Long freeCount = (Long) parameters[1];

               disp.callRemoteMethod(destAddress,
                  new MethodCall(UPDATE_SHORTRUNNING_FREE_METHOD, address, freeCount), opts);

               break;
            }
            case UPDATE_LONGRUNNING_FREE : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];
               Long freeCount = (Long) parameters[1];

               disp.callRemoteMethod(destAddress,
                  new MethodCall(UPDATE_LONGRUNNING_FREE_METHOD, address, freeCount), opts);

               break;
            }
            case GET_DISTRIBUTED_STATISTICS : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               returnValue = 
                  (Serializable)disp.callRemoteMethod(destAddress,
                                                      new MethodCall(GET_DISTRIBUTED_STATISTICS_METHOD, address),
                                                      opts);

               break;
            }
            case DELTA_DOWORK_ACCEPTED : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_DOWORK_ACCEPTED_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_DOWORK_REJECTED : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_DOWORK_REJECTED_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_STARTWORK_ACCEPTED : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_STARTWORK_ACCEPTED_METHOD, address),
                                     opts);
               
               break;
            }
            case DELTA_STARTWORK_REJECTED : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_STARTWORK_REJECTED_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_SCHEDULEWORK_ACCEPTED : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_SCHEDULEWORK_ACCEPTED_METHOD, address),
                                     opts);
               
               break;
            }
            case DELTA_SCHEDULEWORK_REJECTED : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_SCHEDULEWORK_REJECTED_METHOD, address),
                                     opts);
               
               break;
            }
            case DELTA_WORK_SUCCESSFUL : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_WORK_SUCCESSFUL_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_WORK_FAILED : {
               org.jboss.jca.core.spi.workmanager.Address address =
                  (org.jboss.jca.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_WORK_FAILED_METHOD, address),
                                     opts);

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
      catch (Throwable t)
      {
         WorkException we = new WorkException(t.getMessage());
         we.initCause(t);
         throw we;
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
               WorkException we = new WorkException(rsp.getException().getMessage());
               we.initCause(rsp.getException());
               throw we;
            }
         }
      }
   }

   /**
    * Get the channel.
    *
    * @return the channel.
    */
   public JChannel getChannel()
   {
      return channel;
   }

   /**
    * Set the channel.
    *
    * @param channel The channel to set.
    */
   public void setChannel(JChannel channel)
   {
      this.channel = channel;
   }

   /**
    * Get the clustername.
    *
    * @return the clustername.
    */
   public String getClusterName()
   {
      return clusterName;
   }

   /**
    * Set the clustername.
    *
    * @param clustername The clustername to set.
    */
   public void setClusterName(String clustername)
   {
      this.clusterName = clustername;
   }

   @Override
   public void viewAccepted(View view)
   {
      if (trace)
      {
         log.tracef("java.net.preferIPv4Stack=%s", System.getProperty("java.net.preferIPv4Stack"));
         log.tracef("viewAccepted called w/ View=%s", view);
      }

      synchronized (this)
      {
         for (org.jgroups.Address physicalAddress : nodes.values())
         {
            if (!view.containsMember(physicalAddress))
            {
               leave(physicalAddress);
            }
         }
         for (org.jgroups.Address address : view.getMembers())
         {
            if (!nodes.containsValue(address))
            {
               try
               {
                  Set<org.jboss.jca.core.spi.workmanager.Address> logicalAddresses =
                     (Set<org.jboss.jca.core.spi.workmanager.Address>)sendMessage(address, Request.GET_WORKMANAGERS);

                  if (logicalAddresses != null && logicalAddresses.size() > 0)
                  {
                     for (org.jboss.jca.core.spi.workmanager.Address logicalAddress : logicalAddresses)
                     {
                        join(logicalAddress, address);

                        Long shortRunning = getShortRunningFree(logicalAddress);
                        Long longRunning = getLongRunningFree(logicalAddress);
                     
                        localUpdateShortRunningFree(logicalAddress, shortRunning);
                        localUpdateLongRunningFree(logicalAddress, longRunning);
                     }
                  }
               }
               catch (Throwable t)
               {
                  // Nothing
               }
            }
         }
      }
   }

   @Override
   public void block()
   {
      if (trace)
         log.tracef("block called");
   }

   @Override
   public void suspect(org.jgroups.Address address)
   {
      if (trace)
         log.tracef("suspect called w/ Address=%s", address);
   }

   @Override
   public void unblock()
   {
      if (trace)
         log.tracef("unblock called");
   }

   @Override
   public String toString()
   {
      return "JGroupsTransport [channel=" + channel + ", clustername=" + clusterName + "]";
   }
}
