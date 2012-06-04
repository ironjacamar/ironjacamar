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
import org.jboss.jca.core.spi.workmanager.policy.Policy;
import org.jboss.jca.core.spi.workmanager.selector.Selector;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

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

   /**
    * Constructor
    */
   public DistributedWorkManagerImpl()
   {
      super();
      this.policy = null;
      this.selector = null;
      this.transport = null;
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
         policy.setDistributedWorkManager(this);
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
         selector.setDistributedWorkManager(this);
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
         transport.setDistributedWorkManager(this);
   }

   /**
    * {@inheritDoc}
    */
   public void localDoWork(Work work) throws WorkException
   {
      super.doWork(work, WorkManager.INDEFINITE, null, null);
   }

   /**
    * {@inheritDoc}
    */
   public void localScheduleWork(Work work) throws WorkException
   {
      super.scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }

   /**
    * {@inheritDoc}
    */
   public long localStartWork(Work work) throws WorkException
   {
      return super.startWork(work, WorkManager.INDEFINITE, null, null);
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
         DistributableWork dw = (DistributableWork)work;
         boolean executed = false;

         if (policy.shouldDistribute(dw))
         {
            String dwmId = selector.selectDistributedWorkManager(getId(), dw);
            if (dwmId != null)
            {
               transport.doWork(dwmId, dw);
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
         DistributableWork dw = (DistributableWork)work;

         if (policy.shouldDistribute(dw))
         {
            String dwmId = selector.selectDistributedWorkManager(getId(), dw);
            if (dwmId != null)
            {
               return transport.startWork(dwmId, dw);
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
         DistributableWork dw = (DistributableWork)work;
         boolean executed = false;

         if (policy.shouldDistribute(dw))
         {
            String dwmId = selector.selectDistributedWorkManager(getId(), dw);
            if (dwmId != null)
            {
               transport.scheduleWork(dwmId, dw);
               executed = true;
            }
         }

         if (!executed)
         {
            localScheduleWork(work);
         }
      }
   }
}
