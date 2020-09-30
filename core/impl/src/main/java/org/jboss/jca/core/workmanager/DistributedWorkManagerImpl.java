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
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class   DistributedWorkManagerImpl extends WorkManagerImpl implements DistributedWorkManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           DistributedWorkManagerImpl.class.getName());

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
   public synchronized void setPolicy(Policy v)
   {
      if (policy != null)
      {
         if (policy instanceof NotificationListener)
            listeners.remove((NotificationListener)policy);
      }

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
   public synchronized void setSelector(Selector v)
   {
      if (selector != null)
      {
         if (selector instanceof NotificationListener)
            listeners.remove((NotificationListener)selector);
      }

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
   public synchronized void setTransport(Transport v)
   {
      if (transport != null)
      {
         if (transport instanceof NotificationListener)
            listeners.remove((NotificationListener)transport);

         removeDistributedStatistics();
      }

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
      if (transport != null)
      {
         checkTransport();

         if (getLongRunningThreadPool() != null && WorkManagerUtil.isLongRunning(work))
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
      else
      {
         super.doWork(work);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void localScheduleWork(Work work) throws WorkException
   {
      if (transport != null)
      {
         checkTransport();

         if (getLongRunningThreadPool() != null && WorkManagerUtil.isLongRunning(work))
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
      else
      {
         super.scheduleWork(work);
      }
   }

   /**
    * {@inheritDoc}
    */
   public long localStartWork(Work work) throws WorkException
   {
      if (transport != null)
      {
         checkTransport();

         if (getLongRunningThreadPool() != null && WorkManagerUtil.isLongRunning(work))
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
      else
      {
         return super.startWork(work);
      }
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
         doFirstChecks(work, WorkManager.INDEFINITE, null);
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
         doFirstChecks(work, WorkManager.INDEFINITE, null);
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
         doFirstChecks(work, WorkManager.INDEFINITE, null);
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
    * Remove distributed statistics
    */
   private synchronized void removeDistributedStatistics()
   {
      if (distributedStatistics != null)
      {
         listeners.remove((NotificationListener)distributedStatistics);
         distributedStatistics.setTransport(null);
         distributedStatistics = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaDoWorkAccepted()
   {
      log.trace("deltaDoWorkAccepted");

      super.deltaDoWorkAccepted();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
      log.trace("deltaDoWorkRejected");

      super.deltaDoWorkRejected();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
      log.trace("deltaStartWorkAccepted");

      super.deltaStartWorkAccepted();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
      log.trace("deltaStartWorkRejected");

      super.deltaStartWorkRejected();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
      log.trace("deltaScheduleWorkAccepted");

      super.deltaScheduleWorkAccepted();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
      log.trace("deltaScheduleWorkRejected");

      super.deltaScheduleWorkRejected();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
      log.trace("deltaWorkSuccessful");

      super.deltaWorkSuccessful();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
      log.trace("deltaWorkFailed");

      super.deltaWorkFailed();

      if (distributedStatisticsEnabled && distributedStatistics != null && transport != null)
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
         localAddress = new Address(getId(), getName(), transport != null ? transport.getId() : null);

      return localAddress;
   }

   /**
    * Initialize
    */
   public void initialize()
   {
      if (distributedStatistics != null)
      {
         distributedStatistics.setOwnId(getLocalAddress());
         distributedStatistics.setTransport(transport);
      }
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
      wm.listeners = Collections.synchronizedList(new ArrayList<NotificationListener>(3));
      wm.setPolicy(getPolicy());
      wm.setSelector(getSelector());
      wm.setTransport(getTransport());
      wm.setDistributedStatisticsEnabled(isDistributedStatisticsEnabled());
      wm.setDoWorkDistributionEnabled(isDoWorkDistributionEnabled());
      wm.setStartWorkDistributionEnabled(isStartWorkDistributionEnabled());
      wm.setScheduleWorkDistributionEnabled(isScheduleWorkDistributionEnabled());
      
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
      sb.append(" listeners(").append(listeners.hashCode()).append("=").append(listeners);
      sb.append(" doWorkDistributionEnabled=").append(doWorkDistributionEnabled);
      sb.append(" startWorkDistributionEnabled=").append(startWorkDistributionEnabled);
      sb.append(" scheduleWorkDistributionEnabled=").append(scheduleWorkDistributionEnabled);
   }
}
