/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.common.api.ThreadPool;
import org.jboss.jca.core.api.WorkManager;
import org.jboss.jca.core.api.WorkWrapper;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.tm.JBossXATerminator;
import org.jboss.util.threadpool.Task;

/**
 * The work manager implementation
 */
public class WorkManagerImpl implements WorkManager
{
   /** The logger */
   private static Logger log = Logger.getLogger(WorkManagerImpl.class);

   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /** The thread pool */
   private ThreadPool threadPool;

   /** The XA terminator */
   private JBossXATerminator xaTerminator;

   /**
    * Default constructor
    */
   public WorkManagerImpl()
   {
   }

   /**
    * Retrieve the thread pool
    * @return the thread pool
    */
   public ThreadPool getThreadPool()
   {
      return threadPool;
   }

   /**
    * Set the thread pool
    * @param threadPool the thread pool
    */
   public void setThreadPool(ThreadPool threadPool)
   {
      this.threadPool = threadPool;
   }

   /**
    * Get the XATerminator
    * @return The XA terminator
    */
   public JBossXATerminator getXATerminator()
   {
      return xaTerminator;
   }

   /**
    * Set the XATerminator
    * @param xaTerminator The XA terminator
    */
   public void setXATerminator(JBossXATerminator xaTerminator)
   {
      this.xaTerminator = xaTerminator;
   }

   /**
    * {@inheritDoc}
    */
   public void doWork(Work work) throws WorkException
   {
      doWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * {@inheritDoc}
    */
   public void doWork(Work work,
                      long startTimeout, 
                      ExecutionContext execContext, 
                      WorkListener workListener) 
      throws WorkException
   {
      if (execContext == null)
         execContext = new ExecutionContext();
      WorkWrapper wrapper = 
         new WorkWrapper(this, work, Task.WAIT_FOR_COMPLETE, startTimeout, execContext, workListener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
   }
   
   /**
    * {@inheritDoc}
    */
   public long startWork(Work work) throws WorkException
   {
      return startWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * {@inheritDoc}
    */
   public long startWork(Work work, 
                         long startTimeout, 
                         ExecutionContext execContext, 
                         WorkListener workListener) 
      throws WorkException
   {
      if (execContext == null)
         execContext = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_FOR_START, startTimeout, execContext, workListener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
      return wrapper.getBlockedElapsed();
   }
   
   /**
    * {@inheritDoc}
    */
   public void scheduleWork(Work work) throws WorkException
   {
      scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * {@inheritDoc}
    */
   public void scheduleWork(Work work,
                            long startTimeout, 
                            ExecutionContext execContext, 
                            WorkListener workListener) 
      throws WorkException
   {
      if (execContext == null)
         execContext = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_NONE, startTimeout, execContext, workListener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
   }

   /**
    * Import any work
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void importWork(WorkWrapper wrapper) throws WorkException
   {
      trace = log.isTraceEnabled();
      if (trace)
         log.trace("Importing work " + wrapper);
      
      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            //JBAS-4002 base value is in seconds as per the API, here we convert to millis
            long timeout = (ctx.getTransactionTimeout() * 1000);
            xaTerminator.registerWork(wrapper.getWork(), xid, timeout);
         }
      }
      if (trace)
         log.trace("Imported work " + wrapper);
   }
   
   /**
    * Execute the work
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void executeWork(WorkWrapper wrapper) throws WorkException
   {
      if (trace)
         log.trace("Submitting work to thread pool " + wrapper);

      threadPool.runTaskWrapper(wrapper);

      if (trace)
         log.trace("Submitted work to thread pool " + wrapper);
   }

   /**
    * Start work
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   public void startWork(WorkWrapper wrapper) throws WorkException
   {
      if (trace)
         log.trace("Starting work " + wrapper);

      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.startWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
         log.trace("Started work " + wrapper);
   }

   /**
    * End work
    * @param wrapper the work wrapper
    */
   public void endWork(WorkWrapper wrapper)
   {
      if (trace)
         log.trace("Ending work " + wrapper);

      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.endWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
         log.trace("Ended work " + wrapper);
   }

   /**
    * Cancel work
    * @param wrapper the work wrapper
    */
   public void cancelWork(WorkWrapper wrapper)
   {
      if (trace)
         log.trace("Cancel work " + wrapper);

      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.cancelWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
         log.trace("Canceled work " + wrapper);
   }
}
