/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.jca.plugins.rar.work;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.tm.JBossXATerminator;
import org.jboss.util.threadpool.Task;
import org.jboss.util.threadpool.ThreadPool;

/**
 * The work manager implementation
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class JBossWorkManager
{
   /** The logger */
   private static final Logger log = Logger.getLogger(JBossWorkManager.class);

   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();
   
   /** The work manager */
   private WorkManager workManager = new WorkManagerImpl();
   
   /** The thread pool */
   protected ThreadPool threadPool;

   /** The xa terminator */
   protected JBossXATerminator xaTerminator;

   /**
    * Retrieve the thread pool
    *
    * @return the thread pool
    */
   public ThreadPool getThreadPool()
   {
      return threadPool;
   }

   /**
    * Set the thread pool
    *
    * @param threadPool the thread pool
    */
   public void setThreadPool(ThreadPool threadPool)
   {
      this.threadPool = threadPool;
   }

   /**
    * Get the xaTerminator.
    * 
    * @return the xaTerminator.
    */
   public JBossXATerminator getXaTerminator()
   {
      return xaTerminator;
   }

   /**
    * Set the xaTerminator.
    * 
    * @param xaTerminator The xaTerminator to set.
    */
   public void setXaTerminator(JBossXATerminator xaTerminator)
   {
      this.xaTerminator = xaTerminator;
   }
   
   /**
    * Get the work manager implementation
    * 
    * @return the work manager
    */
   public WorkManager getWorkManager()
   {
      return workManager;
   }
   
   public void doWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
   {
      if (ctx == null)
         ctx = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_FOR_COMPLETE, startTimeout, ctx, listener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
   }

   public void doWork(Work work) throws WorkException
   {
      doWork(work, WorkManager.INDEFINITE, null, null);
   }

   public long startWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
   {
      if (ctx == null)
         ctx = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_FOR_START, startTimeout, ctx, listener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
      return wrapper.getBlockedElapsed();
   }

   public long startWork(Work work) throws WorkException
   {
      return startWork(work, WorkManager.INDEFINITE, null, null);
   }

   public void scheduleWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
   {
      if (ctx == null)
         ctx = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_NONE, startTimeout, ctx, listener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
   }

   public void scheduleWork(Work work) throws WorkException
   {
      scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }

   /**
    * Import any work
    * 
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void importWork(WorkWrapper wrapper) throws WorkException
   {
      trace = log.isTraceEnabled();
      if (trace)
         log.trace("Importing work " + wrapper);

      if (xaTerminator == null)
         throw new WorkRejectedException("No XATermintor", WorkException.INTERNAL);
      
      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            long timeout = ctx.getTransactionTimeout();
            xaTerminator.registerWork(wrapper.getWork(), xid, timeout);
         }
      }
      if (trace)
         log.trace("Imported work " + wrapper);
   }
   
   /**
    * Execute the work
    * 
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void executeWork(WorkWrapper wrapper) throws WorkException
   {
      if (trace)
         log.trace("Submitting work to thread pool " + wrapper);

      if (threadPool == null)
         throw new WorkRejectedException("No thread pool", WorkException.INTERNAL);

      threadPool.runTaskWrapper(wrapper);

      if (trace)
         log.trace("Submitted work to thread pool " + wrapper);
   }

   /**
    * Start work
    * 
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void startWork(WorkWrapper wrapper) throws WorkException
   {
      if (trace)
         log.trace("Starting work " + wrapper);

      if (xaTerminator == null)
         throw new WorkRejectedException("No XATermintor", WorkException.INTERNAL);

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
    * 
    * @param wrapper the work wrapper
    */
   protected void endWork(WorkWrapper wrapper)
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
    * 
    * @param wrapper the work wrapper
    */
   protected void cancelWork(WorkWrapper wrapper)
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

   /**
    * A WorkManagerImpl.
    */
   private class WorkManagerImpl implements WorkManager
   {
      public void doWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
      {
         JBossWorkManager.this.doWork(work, startTimeout, ctx, listener);
      }

      public void doWork(Work work) throws WorkException
      {
         JBossWorkManager.this.doWork(work);
      }

      public void scheduleWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
      {
         JBossWorkManager.this.scheduleWork(work, startTimeout, ctx, listener);
      }

      public void scheduleWork(Work work) throws WorkException
      {
         JBossWorkManager.this.scheduleWork(work);
      }

      public long startWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
      {
         return JBossWorkManager.this.startWork(work, startTimeout, ctx, listener);
      }

      public long startWork(Work work) throws WorkException
      {
         return JBossWorkManager.this.startWork(work);
      }
   }
}
