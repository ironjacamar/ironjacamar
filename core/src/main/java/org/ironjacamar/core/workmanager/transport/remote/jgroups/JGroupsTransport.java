/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.workmanager.transport.remote.jgroups;

import org.ironjacamar.core.CoreBundle;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.workmanager.ClassBundle;
import org.ironjacamar.core.workmanager.WorkClassLoader;
import org.ironjacamar.core.workmanager.WorkObjectInputStream;
import org.ironjacamar.core.workmanager.transport.remote.AbstractRemoteTransport;
import org.ironjacamar.core.workmanager.transport.remote.ProtocolMessages.Request;
import org.ironjacamar.core.workmanager.transport.remote.ProtocolMessages.ResponseValues;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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

import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.MethodLookup;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

/**
 * The JGroups transport
 *
 * @author <a href="mailto:stefano.maestri@redhat.com">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JGroupsTransport extends AbstractRemoteTransport<org.jgroups.Address> implements MembershipListener
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, JGroupsTransport.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** The Channel used by this transport **/
   private Channel channel;

   /** Timeout */
   private long timeout;

   /** the cluster name to join **/
   private String clusterName;

   private RpcDispatcher disp;

   private boolean initialized;

   private static final short JOIN_METHOD = 1;

   private static final short LEAVE_METHOD = 2;

   private static final short PING_METHOD = 3;

   private static final short GET_WORKMANAGERS_METHOD = 4;

   private static final short WORKMANAGER_ADD_METHOD = 5;

   private static final short WORKMANAGER_REMOVE_METHOD = 6;

   private static final short DO_WORK_METHOD = 7;

   private static final short START_WORK_METHOD = 8;

   private static final short SCHEDULE_WORK_METHOD = 9;

   private static final short GET_SHORTRUNNING_FREE_METHOD = 10;

   private static final short GET_LONGRUNNING_FREE_METHOD = 11;

   private static final short UPDATE_SHORTRUNNING_FREE_METHOD = 12;

   private static final short UPDATE_LONGRUNNING_FREE_METHOD = 13;

   private static final short GET_DISTRIBUTED_STATISTICS_METHOD = 14;

   private static final short CLEAR_DISTRIBUTED_STATISTICS_METHOD = 15;

   private static final short DELTA_DOWORK_ACCEPTED_METHOD = 16;

   private static final short DELTA_DOWORK_REJECTED_METHOD = 17;

   private static final short DELTA_STARTWORK_ACCEPTED_METHOD = 18;

   private static final short DELTA_STARTWORK_REJECTED_METHOD = 19;

   private static final short DELTA_SCHEDULEWORK_ACCEPTED_METHOD = 20;

   private static final short DELTA_SCHEDULEWORK_REJECTED_METHOD = 21;

   private static final short DELTA_WORK_SUCCESSFUL_METHOD = 22;

   private static final short DELTA_WORK_FAILED_METHOD = 23;

   private static Map<Short, Method> methods = new HashMap<Short, Method>();

   static
   {
      try
      {
         methods.put(JOIN_METHOD, 
                     SecurityActions.getMethod(JGroupsTransport.class, "join",
                                               org.ironjacamar.core.spi.workmanager.Address.class,
                                               org.jgroups.Address.class));
         
         methods.put(LEAVE_METHOD, 
                     SecurityActions.getMethod(JGroupsTransport.class, "leave",
                                               org.jgroups.Address.class));
         
         methods.put(PING_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localPing"));

         methods.put(GET_WORKMANAGERS_METHOD,
                     SecurityActions.getMethod(JGroupsTransport.class, "getWorkManagers"));

         methods.put(WORKMANAGER_ADD_METHOD, 
                     SecurityActions.getMethod(JGroupsTransport.class, "addWorkManager",
                                               org.ironjacamar.core.spi.workmanager.Address.class,
                                               org.jgroups.Address.class));

         methods.put(WORKMANAGER_REMOVE_METHOD, 
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localWorkManagerRemove",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DO_WORK_METHOD, 
                     SecurityActions.getMethod(JGroupsTransport.class, "executeDoWork",
                                               org.ironjacamar.core.spi.workmanager.Address.class,
                                               ClassBundle.class,
                                               byte[].class));

         methods.put(START_WORK_METHOD,
                     SecurityActions.getMethod(JGroupsTransport.class, "executeStartWork",
                                               org.ironjacamar.core.spi.workmanager.Address.class,
                                               ClassBundle.class,
                                               byte[].class));

         methods.put(SCHEDULE_WORK_METHOD,
                     SecurityActions.getMethod(JGroupsTransport.class, "executeScheduleWork",
                                               org.ironjacamar.core.spi.workmanager.Address.class,
                                               ClassBundle.class,
                                               byte[].class));

         methods.put(GET_SHORTRUNNING_FREE_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localGetShortRunningFree",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(GET_LONGRUNNING_FREE_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localGetLongRunningFree",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(UPDATE_SHORTRUNNING_FREE_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localUpdateShortRunningFree",
                                               org.ironjacamar.core.spi.workmanager.Address.class,
                                               Long.class));

         methods.put(UPDATE_LONGRUNNING_FREE_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localUpdateLongRunningFree",
                                               org.ironjacamar.core.spi.workmanager.Address.class,
                                               Long.class));

         methods.put(GET_DISTRIBUTED_STATISTICS_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localGetDistributedStatistics",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(CLEAR_DISTRIBUTED_STATISTICS_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localClearDistributedStatistics",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_DOWORK_ACCEPTED_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaDoWorkAccepted",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_DOWORK_REJECTED_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaDoWorkRejected",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_STARTWORK_ACCEPTED_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaStartWorkAccepted",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_STARTWORK_REJECTED_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaStartWorkRejected",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_SCHEDULEWORK_ACCEPTED_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaScheduleWorkAccepted",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_SCHEDULEWORK_REJECTED_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaScheduleWorkRejected",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_WORK_SUCCESSFUL_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaWorkSuccessful",
                                               org.ironjacamar.core.spi.workmanager.Address.class));

         methods.put(DELTA_WORK_FAILED_METHOD,
                     SecurityActions.getMethod(AbstractRemoteTransport.class, "localDeltaWorkFailed",
                                               org.ironjacamar.core.spi.workmanager.Address.class));
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
      this.timeout = 10000L;
   }

   /**
    * Delegator
    * @param logicalAddress The logical address
    * @param address The address
    */
   public void join(org.ironjacamar.core.spi.workmanager.Address logicalAddress, org.jgroups.Address address)
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
    * Get WorkManagers
    * @return The value
    */
   public Set<org.ironjacamar.core.spi.workmanager.Address> getWorkManagers()
   {
      return getAddresses(channel.getAddress());
   }

   /**
    * Delegator
    * @param logicalAddress The logical address
    * @param address The address
    */
   public void addWorkManager(org.ironjacamar.core.spi.workmanager.Address logicalAddress, org.jgroups.Address address)
   {
      super.localWorkManagerAdd(logicalAddress, address);
   }

   /**
    * Execute doWork
    * @param logicalAddress The logical address
    * @param classBundle The class bundle
    * @param b The bytes
    * @throws WorkException in case of error
    */
   public void executeDoWork(org.ironjacamar.core.spi.workmanager.Address logicalAddress,
                             ClassBundle classBundle, byte[] b)
      throws WorkException
   {
      ByteArrayInputStream bias = new ByteArrayInputStream(b);
      WorkObjectInputStream wois = null;
      try
      {
         WorkClassLoader wcl = SecurityActions.createWorkClassLoader(classBundle);
         
         wois = new WorkObjectInputStream(bias, wcl);

         DistributableWork dw = (DistributableWork)wois.readObject();

         localDoWork(logicalAddress, dw);
      }
      catch (WorkException we)
      {
         throw we;
      }
      catch (Throwable t)
      {
         throw new WorkException("Error during doWork: " + t.getMessage(), t);
      }
      finally
      {
         if (wois != null)
         {
            try
            {
               wois.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Execute startWork
    * @param logicalAddress The logical address
    * @param classBundle The class bundle
    * @param b The bytes
    * @return the start value
    * @throws WorkException in case of error
    */
   public long executeStartWork(org.ironjacamar.core.spi.workmanager.Address logicalAddress,
                                ClassBundle classBundle, byte[] b)
      throws WorkException
   {
      ByteArrayInputStream bias = new ByteArrayInputStream(b);
      WorkObjectInputStream wois = null;
      try
      {
         WorkClassLoader wcl = SecurityActions.createWorkClassLoader(classBundle);
         
         wois = new WorkObjectInputStream(bias, wcl);

         DistributableWork dw = (DistributableWork)wois.readObject();

         return localStartWork(logicalAddress, dw);
      }
      catch (WorkException we)
      {
         throw we;
      }
      catch (Throwable t)
      {
         throw new WorkException("Error during doWork: " + t.getMessage(), t);
      }
      finally
      {
         if (wois != null)
         {
            try
            {
               wois.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Execute scheduleWork
    * @param logicalAddress The logical address
    * @param classBundle The class bundle
    * @param b The bytes
    * @throws WorkException in case of error
    */
   public void executeScheduleWork(org.ironjacamar.core.spi.workmanager.Address logicalAddress,
                                   ClassBundle classBundle, byte[] b)
      throws WorkException
   {
      ByteArrayInputStream bias = new ByteArrayInputStream(b);
      WorkObjectInputStream wois = null;
      try
      {
         WorkClassLoader wcl = SecurityActions.createWorkClassLoader(classBundle);
         
         wois = new WorkObjectInputStream(bias, wcl);

         DistributableWork dw = (DistributableWork)wois.readObject();

         localScheduleWork(logicalAddress, dw);
      }
      catch (WorkException we)
      {
         throw we;
      }
      catch (Throwable t)
      {
         throw new WorkException("Error during doWork: " + t.getMessage(), t);
      }
      finally
      {
         if (wois != null)
         {
            try
            {
               wois.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }
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
      {
         try
         {
            disp.stop();
         }
         catch (Throwable t)
         {
            if (trace)
               log.tracef("Throwable during disp.stop(): %s", t.getMessage());
         }

         disp = null;
      }

      if (channel != null)
      {
         try
         {
            channel.disconnect();
         }
         catch (Throwable t)
         {
            if (trace)
               log.tracef("Throwable during channel.disconnect(): %s", t.getMessage());
         }

         try
         {
            channel.close();
         }
         catch (Throwable t)
         {
            if (trace)
               log.tracef("Throwable during channel.close(): %s", t.getMessage());
         }

         channel = null;
      }
   }

   @Override
   public Serializable sendMessage(org.jgroups.Address destAddress, Request request, Serializable... parameters)
      throws WorkException
   {
      Serializable returnValue = null;

      if (trace)
         log.tracef("%s: sending message=%s to %s", channel.getAddress(), request, destAddress);

      if (channel == null || !channel.isOpen() || !channel.isConnected())
      {
         if (trace)
            log.tracef("%s: channel not connected", channel != null ? channel.getAddress() : "<empty>");

         return null;
      }

      // Set request optiuons.
      // Note we are settings OOB flag for for the sync calls, to avoid the deadlocks.
      // The only diff to regular messages is that OOB RPCs are not ordered, but we don't need this anyway
      // as we're sending the next RPC only *after* we've received the response(s).
      RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, timeout).setFlags(Message.Flag.OOB);

      try
      {
         switch (request)
         {
            case JOIN : {
               org.jgroups.Address joiningAddress = (org.jgroups.Address) parameters[0];
               List<org.jgroups.Address> dests = destAddress == null ? null : Arrays.asList(destAddress);

               RspList<ResponseValues> rspList = disp
                     .callRemoteMethods(dests, new MethodCall(JOIN_METHOD, joiningAddress), opts);
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
               try
               {
                  returnValue = (Serializable) disp.callRemoteMethod(destAddress,
                                                                     new MethodCall(GET_WORKMANAGERS_METHOD),
                                                                     opts);
               }
               catch (WorkException we)
               {
                  throw we;
               }
               catch (Exception e)
               {
                  throw new WorkException(e);
               }

               break;
            }
            case WORKMANAGER_ADD : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];
               org.jgroups.Address physicalAddress = (org.jgroups.Address) parameters[1];

               disp.callRemoteMethod(destAddress,
                                     new MethodCall(WORKMANAGER_ADD_METHOD, address, physicalAddress), opts);

               break;
            }
            case WORKMANAGER_REMOVE : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress,
                                     new MethodCall(WORKMANAGER_REMOVE_METHOD, address), opts);

               break;
            }
            case PING : {
               try
               {
                  returnValue = (Long) disp.callRemoteMethod(destAddress, new MethodCall(PING_METHOD),
                     opts);
               }
               catch (WorkException we)
               {
                  throw we;
               }
               catch (Exception e)
               {
                  throw new WorkException(e);
               }

               break;
            }
            case DO_WORK : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];
               ClassBundle cb = (ClassBundle)parameters[1];
               DistributableWork work = (DistributableWork) parameters[2];
               try
               {
                  disp.callRemoteMethod(destAddress,
                                        new MethodCall(DO_WORK_METHOD, address, cb, getBytes(work)), opts);
               }
               catch (WorkException we)
               {
                  throw we;
               }
               catch (InvocationTargetException ite)
               {
                  throw ite.getTargetException();
               }
               catch (Exception e)
               {
                  throw new WorkException(e);
               }
               break;
            }
            case START_WORK : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];
               ClassBundle cb = (ClassBundle)parameters[1];
               DistributableWork work = (DistributableWork) parameters[2];

               returnValue = (Long) disp.callRemoteMethod(destAddress,
                                                          new MethodCall(START_WORK_METHOD, address, cb,
                                                                         getBytes(work)), opts);

               break;
            }
            case SCHEDULE_WORK : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];
               ClassBundle cb = (ClassBundle)parameters[1];
               DistributableWork work = (DistributableWork) parameters[2];

               disp.callRemoteMethod(destAddress,
                                     new MethodCall(SCHEDULE_WORK_METHOD, address, cb, getBytes(work)), opts);

               break;
            }
            case GET_SHORTRUNNING_FREE : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               returnValue = (Long) disp.callRemoteMethod(destAddress,
                                                          new MethodCall(GET_SHORTRUNNING_FREE_METHOD,
                                                                         address), opts);

               break;
            }
            case GET_LONGRUNNING_FREE : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               returnValue = (Long) disp.callRemoteMethod(destAddress,
                                                          new MethodCall(GET_LONGRUNNING_FREE_METHOD,
                                                                         address), opts);
               break;
            }
            case UPDATE_SHORTRUNNING_FREE : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];
               Long freeCount = (Long) parameters[1];

               disp.callRemoteMethod(destAddress,
                  new MethodCall(UPDATE_SHORTRUNNING_FREE_METHOD, address, freeCount), opts);

               break;
            }
            case UPDATE_LONGRUNNING_FREE : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];
               Long freeCount = (Long) parameters[1];

               disp.callRemoteMethod(destAddress,
                  new MethodCall(UPDATE_LONGRUNNING_FREE_METHOD, address, freeCount), opts);

               break;
            }
            case GET_DISTRIBUTED_STATISTICS : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               returnValue = 
                  (Serializable)disp.callRemoteMethod(destAddress,
                                                      new MethodCall(GET_DISTRIBUTED_STATISTICS_METHOD, address),
                                                      opts);

               break;
            }
            case CLEAR_DISTRIBUTED_STATISTICS : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress,
                                     new MethodCall(CLEAR_DISTRIBUTED_STATISTICS_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_DOWORK_ACCEPTED : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_DOWORK_ACCEPTED_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_DOWORK_REJECTED : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_DOWORK_REJECTED_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_STARTWORK_ACCEPTED : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_STARTWORK_ACCEPTED_METHOD, address),
                                     opts);
               
               break;
            }
            case DELTA_STARTWORK_REJECTED : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_STARTWORK_REJECTED_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_SCHEDULEWORK_ACCEPTED : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_SCHEDULEWORK_ACCEPTED_METHOD, address),
                                     opts);
               
               break;
            }
            case DELTA_SCHEDULEWORK_REJECTED : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_SCHEDULEWORK_REJECTED_METHOD, address),
                                     opts);
               
               break;
            }
            case DELTA_WORK_SUCCESSFUL : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

               disp.callRemoteMethod(destAddress, new MethodCall(DELTA_WORK_SUCCESSFUL_METHOD, address),
                                     opts);

               break;
            }
            case DELTA_WORK_FAILED : {
               org.ironjacamar.core.spi.workmanager.Address address =
                  (org.ironjacamar.core.spi.workmanager.Address) parameters[0];

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
      catch (WorkException we)
      {
         throw we;
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
               Throwable t = rsp.getException();
               if (t instanceof WorkException)
               {
                  throw (WorkException)t;
               }
               else
               {
                  WorkException we = new WorkException(rsp.getException().getMessage());
                  we.initCause(rsp.getException());
                  throw we;
               }
            }
         }
      }
   }

   /**
    * Get the physical address
    * @return The value
    */
   public org.jgroups.Address getOwnAddress()
   {
      return channel.getAddress();
   }

   /**
    * Get the channel.
    *
    * @return the channel.
    */
   public Channel getChannel()
   {
      return channel;
   }

   /**
    * Set the channel.
    *
    * @param channel The channel to set.
    */
   public void setChannel(Channel channel)
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

   /**
    * Get the timeout
    * @return The value
    */
   public long getTimeout()
   {
      return timeout;
   }

   /**
    * Set the timeout
    * @param v The value
    */
   public void setTimeout(long v)
   {
      timeout = v;
   }

   @Override
   public void viewAccepted(View view)
   {
      if (trace)
      {
         log.tracef("java.net.preferIPv4Stack=%s", SecurityActions.getSystemProperty("java.net.preferIPv4Stack"));
         log.tracef("viewAccepted called w/ View=%s", view);
      }

      synchronized (this)
      {
         for (org.jgroups.Address physicalAddress : nodes.values())
         {
            if (physicalAddress != null && !view.containsMember(physicalAddress))
            {
               leave(physicalAddress);
            }
         }
         for (org.jgroups.Address address : view.getMembers())
         {
            if (channel != null && !channel.getAddress().equals(address) && !nodes.containsValue(address))
            {
               try
               {
                  Set<org.ironjacamar.core.spi.workmanager.Address> logicalAddresses =
                     (Set<org.ironjacamar.core.spi.workmanager.Address>)sendMessage(address, Request.GET_WORKMANAGERS);

                  if (logicalAddresses != null && !logicalAddresses.isEmpty())
                  {
                     for (org.ironjacamar.core.spi.workmanager.Address logicalAddress : logicalAddresses)
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
                  log.error("ViewAccepted: " + t.getMessage(), t);
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

   /**
    * Get the byte[] of a DistributableWork instance
    * @param dw The instance
    * @return The value
    */
   private byte[] getBytes(DistributableWork dw)
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = null;
      try
      {
         oos = new ObjectOutputStream(baos);
         oos.writeObject(dw);
         oos.flush();
         return baos.toByteArray();
      }
      catch (Throwable t)
      {
         log.error("Error during getBytes: " + t.getMessage(), t);
      }
      finally
      {
         if (oos != null)
         {
            try
            {
               oos.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }

      return null;
   }

   @Override
   public String toString()
   {
      return "JGroupsTransport [channel=" + channel + ", clustername=" + clusterName + "]";
   }
}
