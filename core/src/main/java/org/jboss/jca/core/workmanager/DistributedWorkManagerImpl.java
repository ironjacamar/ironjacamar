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

package org.jboss.jca.core.workmanager;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.api.workmanager.DistributedWorkManagerStatistics;
import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.policy.Policy;
import org.jboss.jca.core.spi.workmanager.selector.Selector;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The distributed work manager implementation.
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DistributedWorkManagerImpl extends WorkManagerImpl implements DistributedWorkManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           DistributedWorkManagerImpl.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** Policy */
   private Policy policy;

   /** Selector */
   private Selector selector;

   /** Transport */
   private Transport transport;

   /** Notification listeners */
   private Collection<NotificationListener> listeners;

   /** Distributed statistics enabled */
   private boolean distributedStatisticsEnabled;

   /** Distributed statistics */
   private DistributedWorkManagerStatisticsImpl distributedStatistics;

   /** Should doWork be enabled for distribution */
   private boolean doWorkDistributionEnabled;

   /** Should startWork be enabled for distribution */
   private boolean startWorkDistributionEnabled;

   /** Should scheduleWork be enabled for distribution */
   private boolean scheduleWorkDistributionEnabled;

   /** Local address */
   private Address localAddress;

   /**
    * Constructor
    */
   public DistributedWorkManagerImpl()
   {
      super();
      this.policy = null;
      this.selector = null;
      this.transport = null;
      this.listeners = Collections.synchronizedList(new ArrayList<NotificationListener>(3));
      this.distributedStatisticsEnabled = true;
      this.distributedStatistics = null;
      this.doWorkDistributionEnabled = true;
      this.startWorkDistributionEnabled = true;
      this.scheduleWorkDistributionEnabled = true;
      this.localAddress = null;
   }

   /**
    * {@inheritDoc}
    */
   public Policy getPolicy()
   {
      return policy;
   }

   /**
    * {@inheritDoc}
    */
   public void setPolicy(Policy v)
   {
      policy = v;

      if (policy != null)
      {
         if (policy instanceof NotificationListener)
            listeners.add((NotificationListener)policy);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Selector getSelector()
   {
      return selector;
   }

   /**
    * {@inheritDoc}
    */
   public void setSelector(Selector v)
   {
      selector = v;

      if (selector != null)
      {
         if (selector instanceof NotificationListener)
            listeners.add((NotificationListener)selector);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Transport getTransport()
   {
      return transport;
   }

   /**
    * {@inheritDoc}
    */
   public void setTransport(Transport v)
   {
      transport = v;

      if (transport != null)
      {
         if (transport instanceof NotificationListener)
            listeners.add((NotificationListener)transport);

         initDistributedStatistics();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDistributedStatisticsEnabled()
   {
      return distributedStatisticsEnabled;
   }

   /**
    * {@inheritDoc}
    */
   public void setDistributedStatisticsEnabled(boolean v)
   {
      distributedStatisticsEnabled = v;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<NotificationListener> getNotificationListeners()
   {
      return listeners;
   }

   /**
    * Set the listeners
    * @param v The value
    */
   void setNotificationListeners(Collection<NotificationListener> v)
   {
      listeners = v;
   }

   /**
    * {@inheritDoc}
    */
   public void setDoWorkDistributionEnabled(boolean v)
   {
      doWorkDistributionEnabled = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDoWorkDistributionEnabled()
   {
      return doWorkDistributionEnabled;
   }

   /**
    * {@inheritDoc}
    */
   public void setStartWorkDistributionEnabled(boolean v)
   {
      startWorkDistributionEnabled = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isStartWorkDistributionEnabled()
   {
      return startWorkDistributionEnabled;
   }

   /**
    * {@inheritDoc}
    */
   public void setScheduleWorkDistributionEnabled(boolean v)
   {
      scheduleWorkDistributionEnabled = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isScheduleWorkDistributionEnabled()
   {
      return scheduleWorkDistributionEnabled;
   }

   /**
    * {@inheritDoc}
    */
   public void localDoWork(Work work) throws WorkException
   {
      checkTransport();

      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(getLocalAddress(),
                                         getLongRunningThreadPool().getNumberOfFreeThreads() - 1);
      }
      else
      {
         transport.updateShortRunningFree(getLocalAddress(),
                                          getShortRunningThreadPool().getNumberOfFreeThreads() - 1);
      }

      WorkEventListener wel = new WorkEventListener(WorkManagerUtil.isLongRunning(work),
                                                    getShortRunningThreadPool(),
                                                    getLongRunningThreadPool(),
                                                    getLocalAddress(),
                                                    transport);

      super.doWork(work, WorkManager.INDEFINITE, null, wel);
   }

   /**
    * {@inheritDoc}
    */
   public void localScheduleWork(Work work) throws WorkException
   {
      checkTransport();

      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(getLocalAddress(),
                                         getLongRunningThreadPool().getNumberOfFreeThreads() - 1);
      }
      else
      {
         transport.updateShortRunningFree(getLocalAddress(),
                                          getShortRunningThreadPool().getNumberOfFreeThreads() - 1);
      }

      WorkEventListener wel = new WorkEventListener(WorkManagerUtil.isLongRunning(work),
                                                    getShortRunningThreadPool(),
                                                    getLongRunningThreadPool(),
                                                    getLocalAddress(),
                                                    transport);

      super.scheduleWork(work, WorkManager.INDEFINITE, null, wel);
   }

   /**
    * {@inheritDoc}
    */
   public long localStartWork(Work work) throws WorkException
   {
      checkTransport();

      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(getLocalAddress(),
                                         getLongRunningThreadPool().getNumberOfFreeThreads() - 1);
      }
      else
      {
         transport.updateShortRunningFree(getLocalAddress(),
                                          getShortRunningThreadPool().getNumberOfFreeThreads() - 1);
      }

      WorkEventListener wel = new WorkEventListener(WorkManagerUtil.isLongRunning(work),
                                                    getShortRunningThreadPool(),
                                                    getLongRunningThreadPool(),
                                                    getLocalAddress(),
                                                    transport);

      return super.startWork(work, WorkManager.INDEFINITE, null, wel);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doWork(Work work) throws WorkException
   {
      if (policy == null || selector == null || transport == null ||
          work == null || !(work instanceof DistributableWork) || !doWorkDistributionEnabled)
      {
         localDoWork(work);
      }
      else
      {
         checkTransport();

         DistributableWork dw = (DistributableWork)work;
         boolean executed = false;

         if (policy.shouldDistribute(this, dw))
         {
            Address dwmAddress = selector.selectDistributedWorkManager(getLocalAddress(), dw);
            if (dwmAddress != null && !getLocalAddress().equals(dwmAddress))
            {
               transport.doWork(dwmAddress, dw);
               executed = true;
            }
         }

         if (!executed)
         {
            localDoWork(work);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long startWork(Work work) throws WorkException
   {
      if (policy == null || selector == null || transport == null ||
          work == null || !(work instanceof DistributableWork) || !startWorkDistributionEnabled)
      {
         return localStartWork(work);
      }
      else
      {
         checkTransport();

         DistributableWork dw = (DistributableWork)work;

         if (policy.shouldDistribute(this, dw))
         {
            Address dwmAddress = selector.selectDistributedWorkManager(getLocalAddress(), dw);
            if (dwmAddress != null && !getLocalAddress().equals(dwmAddress))
            {
               return transport.startWork(dwmAddress, dw);
            }
         }

         return localStartWork(work);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void scheduleWork(Work work) throws WorkException
   {
      if (policy == null || selector == null || transport == null ||
          work == null || !(work instanceof DistributableWork) || !scheduleWorkDistributionEnabled)
      {
         localScheduleWork(work);
      }
      else
      {
         checkTransport();

         DistributableWork dw = (DistributableWork)work;
         boolean executed = false;

         if (policy.shouldDistribute(this, dw))
         {
            Address dwmAddress = selector.selectDistributedWorkManager(getLocalAddress(), dw);
            if (dwmAddress != null && !getLocalAddress().equals(dwmAddress))
            {
               transport.scheduleWork(dwmAddress, dw);
               executed = true;
            }
         }

         if (!executed)
         {
            localScheduleWork(work);
         }
      }
   }

   /**
    * Check the transport
    * @exception WorkException In case of an error
    */
   private void checkTransport() throws WorkException
   {
      if (!transport.isInitialized())
      {
         try
         {
            transport.initialize();
            initialize();
         }
         catch (Throwable t)
         {
            WorkException we = new WorkException("Exception during transport initialization");
            we.initCause(t);
            throw we;
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public DistributedWorkManagerStatistics getDistributedStatistics()
   {
      return distributedStatistics;
   }

   /**
    * Set the distributed statistics
    * @param v The value
    */
   void setDistributedStatistics(DistributedWorkManagerStatisticsImpl v)
   {
      distributedStatistics = v;
   }

   /**
    * Init distributed statistics
    */
   private synchronized void initDistributedStatistics()
   {
      if (distributedStatistics == null)
      {
         distributedStatistics = new DistributedWorkManagerStatisticsImpl();
         listeners.add((NotificationListener)distributedStatistics);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaDoWorkAccepted()
   {
      if (trace)
         log.trace("deltaDoWorkAccepted");

      super.deltaDoWorkAccepted();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaDoWorkAccepted();
         }
         catch (WorkException we)
         {
            log.debugf("deltaDoWorkAccepted: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaDoWorkRejected()
   {
      if (trace)
         log.trace("deltaDoWorkRejected");

      super.deltaDoWorkRejected();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaDoWorkRejected();
         }
         catch (WorkException we)
         {
            log.debugf("deltaDoWorkRejected: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaStartWorkAccepted()
   {
      if (trace)
         log.trace("deltaStartWorkAccepted");

      super.deltaStartWorkAccepted();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaStartWorkAccepted();
         }
         catch (WorkException we)
         {
            log.debugf("deltaStartWorkAccepted: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaStartWorkRejected()
   {
      if (trace)
         log.trace("deltaStartWorkRejected");

      super.deltaStartWorkRejected();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaStartWorkRejected();
         }
         catch (WorkException we)
         {
            log.debugf("deltaStartWorkRejected: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaScheduleWorkAccepted()
   {
      if (trace)
         log.trace("deltaScheduleWorkAccepted");

      super.deltaScheduleWorkAccepted();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaScheduleWorkAccepted();
         }
         catch (WorkException we)
         {
            log.debugf("deltaScheduleWorkAccepted: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaScheduleWorkRejected()
   {
      if (trace)
         log.trace("deltaScheduleWorkRejected");

      super.deltaScheduleWorkRejected();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaScheduleWorkRejected();
         }
         catch (WorkException we)
         {
            log.debugf("deltaScheduleWorkRejected: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaWorkSuccessful()
   {
      if (trace)
         log.trace("deltaWorkSuccessful");

      super.deltaWorkSuccessful();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaWorkSuccessful();
         }
         catch (WorkException we)
         {
            log.debugf("deltaWorkSuccessful: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaWorkFailed()
   {
      if (trace)
         log.trace("deltaWorkFailed");

      super.deltaWorkFailed();

      if (distributedStatisticsEnabled)
      {
         try
         {
            checkTransport();
            distributedStatistics.sendDeltaWorkFailed();
         }
         catch (WorkException we)
         {
            log.debugf("deltaWorkFailed: %s", we.getMessage(), we);
         }
      }
   }

   /**
    * Get local address
    * @return The value
    */
   Address getLocalAddress()
   {
      if (localAddress == null)
         localAddress = new Address(getId(), getName(), transport.getId());

      return localAddress;
   }

   /**
    * Initialize
    */
   public void initialize()
   {
      distributedStatistics.setOwnId(getLocalAddress());
      distributedStatistics.setTransport(transport);
   }

   /**
    * Clone the WorkManager implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *
    */
   @Override
   public org.jboss.jca.core.api.workmanager.WorkManager clone() throws CloneNotSupportedException
   {
      DistributedWorkManagerImpl wm = (DistributedWorkManagerImpl)super.clone();
      wm.setPolicy(getPolicy());
      wm.setSelector(getSelector());
      wm.setTransport(getTransport());
      wm.setDistributedStatisticsEnabled(isDistributedStatisticsEnabled());
      wm.setDoWorkDistributionEnabled(isDoWorkDistributionEnabled());
      wm.setStartWorkDistributionEnabled(isStartWorkDistributionEnabled());
      wm.setScheduleWorkDistributionEnabled(isScheduleWorkDistributionEnabled());

      DistributedWorkManagerStatisticsImpl dwmsi = new DistributedWorkManagerStatisticsImpl();
      wm.setDistributedStatistics(dwmsi);
      
      if (getPolicy() instanceof NotificationListener)
         wm.listeners.add((NotificationListener)getPolicy());

      if (getSelector() instanceof NotificationListener)
         wm.listeners.add((NotificationListener)getSelector());

      if (getTransport() instanceof NotificationListener)
         wm.listeners.add((NotificationListener)getTransport());

      if (dwmsi instanceof NotificationListener)
         wm.listeners.add((NotificationListener)dwmsi);

      return wm;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void toString(StringBuilder sb)
   {
      sb.append(" policy=").append(policy);
      sb.append(" selector=").append(selector);
      sb.append(" transport=").append(transport);
      sb.append(" distributedStatisticsEnabled=").append(distributedStatisticsEnabled);
      sb.append(" distributedStatistics=").append(distributedStatistics);
      sb.append(" listeners=").append(listeners);
      sb.append(" doWorkDistributionEnabled=").append(doWorkDistributionEnabled);
      sb.append(" startWorkDistributionEnabled=").append(startWorkDistributionEnabled);
      sb.append(" scheduleWorkDistributionEnabled=").append(scheduleWorkDistributionEnabled);
   }
}
