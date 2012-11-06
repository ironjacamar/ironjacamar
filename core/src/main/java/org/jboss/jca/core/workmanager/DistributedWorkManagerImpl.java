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

         //policy.setDistributedWorkManager(this);
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

         //selector.setDistributedWorkManager(this);
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

         //transport.setDistributedWorkManager(this);
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
    * {@inheritDoc}
    */
   public void localDoWork(Work work) throws WorkException
   {
      checkTransport();

      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(new Address(getId(), transport.getId()),
                                         getLongRunningThreadPool().getNumberOfFreeThreads() - 1);
      }
      else
      {
         transport.updateShortRunningFree(new Address(getId(), transport.getId()),
                                          getShortRunningThreadPool().getNumberOfFreeThreads() - 1);
      }

      super.doWork(work, WorkManager.INDEFINITE, null, null);

      /*
        TODO
      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(new Address(getId(), transport.getId()),
                                         getLongRunningThreadPool().getNumberOfFreeThreads());
      }
      else
      {
         transport.updateShortRunningFree(new Address(getId(), transport.getId()),
                                          getShortRunningThreadPool().getNumberOfFreeThreads());
      }
      */
   }

   /**
    * {@inheritDoc}
    */
   public void localScheduleWork(Work work) throws WorkException
   {
      checkTransport();

      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(new Address(getId(), transport.getId()),
                                         getLongRunningThreadPool().getNumberOfFreeThreads() - 1);
      }
      else
      {
         transport.updateShortRunningFree(new Address(getId(), transport.getId()),
                                          getShortRunningThreadPool().getNumberOfFreeThreads() - 1);
      }

      super.scheduleWork(work, WorkManager.INDEFINITE, null, null);

      /*
        TODO
      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(new Address(getId(), transport.getId()),
                                         getLongRunningThreadPool().getNumberOfFreeThreads());
      }
      else
      {
         transport.updateShortRunningFree(new Address(getId(), transport.getId()),
                                          getShortRunningThreadPool().getNumberOfFreeThreads());
      }
      */
   }

   /**
    * {@inheritDoc}
    */
   public long localStartWork(Work work) throws WorkException
   {
      checkTransport();

      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(new Address(getId(), transport.getId()),
                                         getLongRunningThreadPool().getNumberOfFreeThreads() - 1);
      }
      else
      {
         transport.updateShortRunningFree(new Address(getId(), transport.getId()),
                                          getShortRunningThreadPool().getNumberOfFreeThreads() - 1);
      }

      return super.startWork(work, WorkManager.INDEFINITE, null, null);

      /*
        TODO
      if (WorkManagerUtil.isLongRunning(work))
      {
         transport.updateLongRunningFree(new Address(getId(), transport.getId()),
                                         getLongRunningThreadPool().getNumberOfFreeThreads());
      }
      else
      {
         transport.updateShortRunningFree(new Address(getId(), transport.getId()),
                                          getShortRunningThreadPool().getNumberOfFreeThreads());
      }
      */
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doWork(Work work) throws WorkException
   {
      if (policy == null || selector == null || transport == null ||
          work == null || !(work instanceof DistributableWork))
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
            Address localAddress = new Address(getId(), transport.getId());

            Address dwmAddress = selector.selectDistributedWorkManager(localAddress, dw);
            if (dwmAddress != null && !localAddress.equals(dwmAddress))
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
          work == null || !(work instanceof DistributableWork))
      {
         return localStartWork(work);
      }
      else
      {
         checkTransport();

         DistributableWork dw = (DistributableWork)work;

         if (policy.shouldDistribute(this, dw))
         {
            Address localAddress = new Address(getId(), transport.getId());

            Address dwmAddress = selector.selectDistributedWorkManager(localAddress, dw);
            if (dwmAddress != null && !localAddress.equals(dwmAddress))
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
          work == null || !(work instanceof DistributableWork))
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
            Address localAddress = new Address(getId(), transport.getId());

            Address dwmAddress = selector.selectDistributedWorkManager(localAddress, dw);
            if (dwmAddress != null && !localAddress.equals(dwmAddress))
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
      if (transport == null)
         throw new WorkException("Transport is null");

      if (!transport.isInitialized())
      {
         transport.register(new Address(getId(), transport.getId()));

         try
         {
            transport.initialize();
            initDistributedStatistics();
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
         distributedStatistics = new DistributedWorkManagerStatisticsImpl(new Address(getId(), transport.getId()),
                                                                          transport);
         listeners.add((NotificationListener)distributedStatistics);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void deltaDoWorkAccepted()
   {
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
      wm.setDistributedStatistics(distributedStatistics);

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
      sb.append(" distributedStatistics=").append(distributedStatistics);
   }
}
