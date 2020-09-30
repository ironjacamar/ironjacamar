/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.api.workmanager.DistributableContext;
import org.jboss.jca.core.api.workmanager.StatisticsExecutor;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.api.workmanager.WorkManagerStatistics;
import org.jboss.jca.core.spi.graceful.GracefulCallback;
import org.jboss.jca.core.spi.security.Callback;
import org.jboss.jca.core.spi.security.SecurityIntegration;
import org.jboss.jca.core.spi.transaction.xa.XATerminator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextErrorCodes;
import javax.resource.spi.work.WorkContextLifecycleListener;
import javax.resource.spi.work.WorkContextProvider;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;
import org.jboss.threads.BlockingExecutor;
import org.jboss.threads.ExecutionTimedOutException;

/**
 * The work manager implementation.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkManagerImpl implements WorkManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, WorkManagerImpl.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /**Work run method name*/
   private static final String RUN_METHOD_NAME = "run";

   /**Work release method name*/
   private static final String RELEASE_METHOD_NAME = "release";

   /**Supported work context set*/
   private static final Set<Class<? extends WorkContext>> SUPPORTED_WORK_CONTEXT_CLASSES =
         new HashSet<Class<? extends WorkContext>>(4);

   /** The id */
   private String id;

   /** The name */
   private String name;

   /** Running in spec compliant mode */
   private boolean specCompliant;

   /** The short running executor */
   private StatisticsExecutor shortRunningExecutor;

   /** The long running executor */
   private StatisticsExecutor longRunningExecutor;

   /** The XA terminator */
   private XATerminator xaTerminator;

   /** Validated work instances */
   private Set<String> validatedWork;

   /** Security module for callback */
   private Callback callbackSecurity;

   /** Security integration module */
   private SecurityIntegration securityIntegration;

   /** Resource adapter */
   private ResourceAdapter resourceAdapter;

   /** Shutdown */
   private AtomicBoolean shutdown;

   /** Scheduled executor for graceful shutdown */
   private ScheduledExecutorService scheduledExecutorService;

   /** Graceful job */
   private ScheduledFuture scheduledGraceful;

   /** Graceful call back */
   private GracefulCallback gracefulCallback;

   /** Active work wrappers */
   private Set<WorkWrapper> activeWorkWrappers;

   /** Enable statistics */
   private boolean statisticsEnabled;

   /** Statistics */
   private WorkManagerStatisticsImpl statistics;

   /**Default supported workcontext types*/
   static
   {
      SUPPORTED_WORK_CONTEXT_CLASSES.add(TransactionContext.class);
      SUPPORTED_WORK_CONTEXT_CLASSES.add(SecurityContext.class);
      SUPPORTED_WORK_CONTEXT_CLASSES.add(HintsContext.class);
      SUPPORTED_WORK_CONTEXT_CLASSES.add(DistributableContext.class);
   }

   /**
    * Constructor - by default the WorkManager is running in spec-compliant mode
    */
   public WorkManagerImpl()
   {
      id = null;
      name = null;
      specCompliant = true;
      validatedWork = new HashSet<String>();
      resourceAdapter = null;
      shutdown = new AtomicBoolean(false);
      scheduledExecutorService = null;
      scheduledGraceful = null;
      gracefulCallback = null;
      activeWorkWrappers = new HashSet<WorkWrapper>();
      statisticsEnabled = true;
      statistics = new WorkManagerStatisticsImpl();
   }

   /**
    * Get the unique id of the work manager
    * @return The value
    */
   public String getId()
   {
      if (id == null)
         return name;

      return id;
   }

   /**
    * Set the unique id of the work manager
    * @param v The value
    */
   public void setId(String v)
   {
      id = v;
   }

   /**
    * Get the name of the work manager
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name of the work manager
    * @param v The value
    */
   public void setName(String v)
   {
      name = v;
   }

   /**
    * Retrieve the executor for short running tasks
    * @return The executor
    */
   public StatisticsExecutor getShortRunningThreadPool()
   {
      return shortRunningExecutor;
   }

   /**
    * Set the executor for short running tasks
    * @param executor The executor
    */
   public void setShortRunningThreadPool(BlockingExecutor executor)
   {
      if (log.isTraceEnabled())
         log.trace("short running executor:" + (executor != null ? executor.getClass() : "null"));

      if (executor != null)
      {
         if (executor instanceof StatisticsExecutor)
         {
            this.shortRunningExecutor = (StatisticsExecutor) executor;
         }
         else
         {
            this.shortRunningExecutor = new StatisticsExecutorImpl(executor);
         }
      }
   }

   /**
    * Retrieve the executor for long running tasks
    * @return The executor
    */
   public StatisticsExecutor getLongRunningThreadPool()
   {
      return longRunningExecutor;
   }

   /**
    * Set the executor for long running tasks
    * @param executor The executor
    */
   public void setLongRunningThreadPool(BlockingExecutor executor)
   {
      if (log.isTraceEnabled())
         log.trace("long running executor:" + (executor != null ? executor.getClass() : "null"));
 
      if (executor != null)
      {
         if (executor instanceof StatisticsExecutor)
         {
            this.longRunningExecutor = (StatisticsExecutor) executor;
         }
         else
         {
            this.longRunningExecutor = new StatisticsExecutorImpl(executor);
         }
      }
   }

   /**
    * Get the XATerminator
    * @return The XA terminator
    */
   public XATerminator getXATerminator()
   {
      return xaTerminator;
   }

   /**
    * Set the XATerminator
    * @param xaTerminator The XA terminator
    */
   public void setXATerminator(XATerminator xaTerminator)
   {
      this.xaTerminator = xaTerminator;
   }

   /**
    * Is spec compliant
    * @return True if spec compliant; otherwise false
    */
   public boolean isSpecCompliant()
   {
      return specCompliant;
   }

   /**
    * Set spec compliant flag
    * @param v The value
    */
   public void setSpecCompliant(boolean v)
   {
      specCompliant = v;
   }

   /**
    * Get the callback security module
    * @return The value
    */
   public Callback getCallbackSecurity()
   {
      return callbackSecurity;
   }

   /**
    * Set callback security module
    * @param v The value
    */
   public void setCallbackSecurity(Callback v)
   {
      callbackSecurity = v;
   }

   /**
    * Get the security integration module
    * @return The value
    */
   public SecurityIntegration getSecurityIntegration()
   {
      return securityIntegration;
   }

   /**
    * Set security intergation module
    * @param v The value
    */
   public void setSecurityIntegration(SecurityIntegration v)
   {
      securityIntegration = v;
   }

   /**
    * Get the resource adapter
    * @return The value
    */
   public ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * Set the resource adapter
    * @param v The value
    */
   public void setResourceAdapter(ResourceAdapter v)
   {
      resourceAdapter = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isStatisticsEnabled()
   {
      return statisticsEnabled;
   }

   /**
    * {@inheritDoc}
    */
   public void setStatisticsEnabled(boolean v)
   {
      statisticsEnabled = v;
   }

   /**
    * Get the statistics
    * @return The value
    */
   public WorkManagerStatistics getStatistics()
   {
      return statistics;
   }

   /**
    * Set the statistics
    * @param v The value
    */
   void setStatistics(WorkManagerStatisticsImpl v)
   {
      statistics = v;
   }

   /**
    * Clone the WorkManager implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *
    */
   @Override
   public WorkManager clone() throws CloneNotSupportedException
   {
      WorkManagerImpl wm = (WorkManagerImpl) super.clone();
      wm.setId(getId());
      wm.setName(getName());
      wm.setShortRunningThreadPool(getShortRunningThreadPool());
      wm.setLongRunningThreadPool(getLongRunningThreadPool());
      wm.setXATerminator(getXATerminator());
      wm.setSpecCompliant(isSpecCompliant());
      wm.setCallbackSecurity(getCallbackSecurity());
      wm.setSecurityIntegration(getSecurityIntegration());
      wm.setStatistics(statistics);

      return wm;
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
   public void doWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener)
      throws WorkException
   {
      log.tracef("doWork(%s, %s, %s, %s)", work, startTimeout, execContext, workListener);

      WorkException exception = null;
      WorkWrapper wrapper = null;
      try
      {
         doFirstChecks(work, startTimeout, execContext);

         if (workListener != null)
         {
            WorkEvent event = new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null);
            workListener.workAccepted(event);
         }

         deltaDoWorkAccepted();

         if (execContext == null)
         {
            execContext = new ExecutionContext();
         }

         final CountDownLatch completedLatch = new CountDownLatch(1);

         wrapper = createWorkWrapper(securityIntegration, work, execContext, workListener, null, completedLatch);

         setup(wrapper, workListener);

         BlockingExecutor executor = getExecutor(work);

         if (startTimeout == WorkManager.INDEFINITE)
         {
            executor.executeBlocking(wrapper);
         }
         else
         {
            executor.executeBlocking(wrapper, startTimeout, TimeUnit.MILLISECONDS);
         }

         completedLatch.await();
      }
      catch (ExecutionTimedOutException etoe)
      {
         exception = new WorkRejectedException(etoe);
         exception.setErrorCode(WorkRejectedException.START_TIMED_OUT);
      }
      catch (RejectedExecutionException ree)
      {
         exception = new WorkRejectedException(ree);
      }
      catch (WorkCompletedException wce)
      {
         if (wrapper != null)
            wrapper.setWorkException(wce);
      }
      catch (WorkException we)
      {
         exception = we;
      }
      catch (InterruptedException ie)
      {
         Thread.currentThread().interrupt();
         exception = new WorkRejectedException(bundle.interruptedWhileRequestingPermit());
      }
      finally
      {
         if (exception != null)
         {
            if (workListener != null)
            {
               WorkEvent event = new WorkEvent(this, WorkEvent.WORK_REJECTED, work, exception);
               workListener.workRejected(event);
            }

            log.tracef("Exception %s for %s", exception, this);

            deltaDoWorkRejected();

            throw exception;
         }

         if (wrapper != null)
         {
            checkWorkCompletionException(wrapper);
         }
      }
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
   public long startWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener)
      throws WorkException
   {
      log.tracef("startWork(%s, %s, %s, %s)", work, startTimeout, execContext, workListener);

      WorkException exception = null;
      WorkWrapper wrapper = null;
      try
      {
         long started = System.currentTimeMillis();

         doFirstChecks(work, startTimeout, execContext);

         if (workListener != null)
         {
            WorkEvent event = new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null);
            workListener.workAccepted(event);
         }

         deltaStartWorkAccepted();

         if (execContext == null)
         {
            execContext = new ExecutionContext();
         }

         final CountDownLatch startedLatch = new CountDownLatch(1);

         wrapper = createWorkWrapper(securityIntegration, work, execContext, workListener, startedLatch, null);

         setup(wrapper, workListener);

         BlockingExecutor executor = getExecutor(work);

         if (startTimeout == WorkManager.INDEFINITE)
         {
            executor.executeBlocking(wrapper);
         }
         else
         {
            executor.executeBlocking(wrapper, startTimeout, TimeUnit.MILLISECONDS);
         }

         startedLatch.await();

         return System.currentTimeMillis() - started;
      }
      catch (ExecutionTimedOutException etoe)
      {
         exception = new WorkRejectedException(etoe);
         exception.setErrorCode(WorkRejectedException.START_TIMED_OUT);
      }
      catch (RejectedExecutionException ree)
      {
         exception = new WorkRejectedException(ree);
      }
      catch (WorkCompletedException wce)
      {
         if (wrapper != null)
            wrapper.setWorkException(wce);
      }
      catch (WorkException we)
      {
         exception = we;
      }
      catch (InterruptedException ie)
      {
         Thread.currentThread().interrupt();
         exception = new WorkRejectedException(bundle.interruptedWhileRequestingPermit());
      }
      finally
      {
         if (exception != null)
         {
            if (workListener != null)
            {
               WorkEvent event = new WorkEvent(this, WorkEvent.WORK_REJECTED, work, exception);
               workListener.workRejected(event);
            }

            log.tracef("Exception %s for %s", exception, this);

            deltaStartWorkRejected();

            throw exception;
         }

         if (wrapper != null)
            checkWorkCompletionException(wrapper);
      }

      return WorkManager.UNKNOWN;
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
   public void scheduleWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener)
      throws WorkException
   {
      log.tracef("scheduleWork(%s, %s, %s, %s)", work, startTimeout, execContext, workListener);

      WorkException exception = null;
      WorkWrapper wrapper = null;
      try
      {
         doFirstChecks(work, startTimeout, execContext);

         if (workListener != null)
         {
            WorkEvent event = new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null);
            workListener.workAccepted(event);
         }

         deltaScheduleWorkAccepted();

         if (execContext == null)
         {
            execContext = new ExecutionContext();
         }

         wrapper = createWorkWrapper(securityIntegration, work, execContext, workListener, null, null);

         setup(wrapper, workListener);

         BlockingExecutor executor = getExecutor(work);

         if (startTimeout == WorkManager.INDEFINITE)
         {
            executor.executeBlocking(wrapper);
         }
         else
         {
            executor.executeBlocking(wrapper, startTimeout, TimeUnit.MILLISECONDS);
         }
      }
      catch (ExecutionTimedOutException etoe)
      {
         exception = new WorkRejectedException(etoe);
         exception.setErrorCode(WorkRejectedException.START_TIMED_OUT);
      }
      catch (RejectedExecutionException ree)
      {
         exception = new WorkRejectedException(ree);
      }
      catch (WorkCompletedException wce)
      {
         if (wrapper != null)
            wrapper.setWorkException(wce);
      }
      catch (WorkException we)
      {
         exception = we;
      }
      catch (InterruptedException ie)
      {
         Thread.currentThread().interrupt();
         exception = new WorkRejectedException(bundle.interruptedWhileRequestingPermit());
      }
      finally
      {
         if (exception != null)
         {
            if (workListener != null)
            {
               WorkEvent event = new WorkEvent(this, WorkEvent.WORK_REJECTED, work, exception);
               workListener.workRejected(event);
            }

            log.tracef("Exception %s for %s", exception, this);

            deltaScheduleWorkRejected();

            throw exception;
         }

         if (wrapper != null)
            checkWorkCompletionException(wrapper);
      }
   }

   /**
    * Crestes a wrapper for work
    *
    * @param securityIntegration the security integration
    * @param work                the work
    * @param executionContext    the execution context
    * @param workListener        the work listener
    * @param startedLatch        latch that will be notified when work starts execution. Can be null.
    * @param completedLatch      latch that will be notified when work completes execution. Can be null.
    * @return the created work wrapper
    */
   protected WorkWrapper createWorkWrapper(SecurityIntegration securityIntegration, Work work,
         ExecutionContext executionContext, WorkListener workListener, CountDownLatch startedLatch,
         CountDownLatch completedLatch)
   {
      return new WorkWrapper(this, securityIntegration, work, executionContext, workListener,
            startedLatch, completedLatch, System.currentTimeMillis());
   }

   /**
    * Delta doWork accepted
    */
   protected void deltaDoWorkAccepted()
   {
      if (statisticsEnabled)
         statistics.deltaDoWorkAccepted();
   }

   /**
    * Delta doWork rejected
    */
   protected void deltaDoWorkRejected()
   {
      if (statisticsEnabled)
         statistics.deltaDoWorkRejected();
   }

   /**
    * Delta startWork accepted
    */
   protected void deltaStartWorkAccepted()
   {
      if (statisticsEnabled)
         statistics.deltaStartWorkAccepted();
   }

   /**
    * Delta startWork rejected
    */
   protected void deltaStartWorkRejected()
   {
      if (statisticsEnabled)
         statistics.deltaStartWorkRejected();
   }

   /**
    * Delta scheduleWork accepted
    */
   protected void deltaScheduleWorkAccepted()
   {
      if (statisticsEnabled)
         statistics.deltaScheduleWorkAccepted();
   }

   /**
    * Delta scheduleWork rejected
    */
   protected void deltaScheduleWorkRejected()
   {
      if (statisticsEnabled)
         statistics.deltaScheduleWorkRejected();
   }

   /**
    * Delta work successful
    */
   protected void deltaWorkSuccessful()
   {
      if (statisticsEnabled)
         statistics.deltaWorkSuccessful();
   }

   /**
    * Delta work failed
    */
   protected void deltaWorkFailed()
   {
      if (statisticsEnabled)
         statistics.deltaWorkFailed();
   }

   /**
    * Do first checks for work starting methods
    * @param work to check
    * @param startTimeout to check
    * @param execContext to check
    * @throws WorkException in case of check don't pass
    */
   public void doFirstChecks(Work work, long startTimeout, ExecutionContext execContext) throws WorkException
   {
      if (isShutdown())
         throw new WorkRejectedException(bundle.workmanagerShutdown());

      if (work == null)
         throw new WorkRejectedException(bundle.workIsNull());

      if (startTimeout < 0)
         throw new WorkRejectedException(bundle.startTimeoutIsNegative(startTimeout));

      checkAndVerifyWork(work, execContext);
   }

   /**
    * {@inheritDoc}
    */
   public boolean cancelShutdown()
   {
      if (scheduledGraceful != null)
      {
         boolean result = scheduledGraceful.cancel(false);

         if (result)
         {
            shutdown.set(false);

            if (gracefulCallback != null)
               gracefulCallback.cancel();

            scheduledGraceful = null;
            gracefulCallback = null;
         }
         else
         {
            return false;
         }
      }
      else if (shutdown.get())
      {
         shutdown.set(false);

         if (gracefulCallback != null)
            gracefulCallback.cancel();

         gracefulCallback = null;
      }
      else
      {
         return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown()
   {
      prepareShutdown(0, null);
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown(GracefulCallback cb)
   {
      prepareShutdown(0, cb);
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown(int seconds)
   {
      prepareShutdown(seconds, null);
   }

   /**
    * {@inheritDoc}
    */
   public void prepareShutdown(int seconds, GracefulCallback cb)
   {
      shutdown.set(true);

      if (gracefulCallback == null) 
         gracefulCallback = cb;

      if (seconds > 0)
      {
         if (scheduledGraceful == null)
         {
            if (scheduledExecutorService == null)
               scheduledExecutorService = Executors.newScheduledThreadPool(1);

            scheduledGraceful =
               scheduledExecutorService.schedule(new WorkManagerShutdown(this), seconds, TimeUnit.SECONDS);
         }
      }
      else
      {
         synchronized (activeWorkWrappers)
         {
            if (activeWorkWrappers.size() == 0)
               shutdown();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void shutdown()
   {
      shutdown.set(true);

      synchronized (activeWorkWrappers)
      {
         for (WorkWrapper ww : activeWorkWrappers)
         {
            ww.getWork().release();
         }
      }

      if (scheduledExecutorService != null)
      {
         if (scheduledGraceful != null && !scheduledGraceful.isDone())
            scheduledGraceful.cancel(true);

         scheduledGraceful = null;
         scheduledExecutorService.shutdownNow();
         scheduledExecutorService = null;
      }

      if (gracefulCallback != null)
      {
         gracefulCallback.done();
         gracefulCallback = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isShutdown()
   {
      return shutdown.get();
   }

   /**
    * {@inheritDoc}
    */
   public int getDelay()
   {
      if (scheduledGraceful != null)
         return (int)scheduledGraceful.getDelay(TimeUnit.SECONDS);

      if (shutdown.get())
         return Integer.MIN_VALUE;
      
      return Integer.MAX_VALUE;
   }

   /**
    * Add work wrapper to active set
    * @param ww The work wrapper
    */
   void addWorkWrapper(WorkWrapper ww)
   {
      synchronized (activeWorkWrappers)
      {
         activeWorkWrappers.add(ww);

         if (statisticsEnabled)
            statistics.setWorkActive(activeWorkWrappers.size());
      }
   }

   /**
    * Remove work wrapper from active set
    * @param ww The work wrapper
    */
   void removeWorkWrapper(WorkWrapper ww)
   {
      synchronized (activeWorkWrappers)
      {
         activeWorkWrappers.remove(ww);

         if (statisticsEnabled)
            statistics.setWorkActive(activeWorkWrappers.size());
      }
   }

   /**
    * Get the executor
    * @param work The work instance
    * @return The executor
    */
   private BlockingExecutor getExecutor(Work work)
   {
      BlockingExecutor executor = shortRunningExecutor;
      if (longRunningExecutor != null && WorkManagerUtil.isLongRunning(work))
      {
         executor = longRunningExecutor;
      }

      fireHintsComplete(work);

      return executor;
   }

   /**
    * Fire complete for HintsContext
    * @param work The work instance
    */
   private void fireHintsComplete(Work work)
   {
      if (work != null && work instanceof WorkContextProvider)
      {
         WorkContextProvider wcProvider = (WorkContextProvider) work;
         List<WorkContext> contexts = wcProvider.getWorkContexts();

         if (contexts != null && contexts.size() > 0)
         {
            Iterator<WorkContext> it = contexts.iterator();
            while (it.hasNext())
            {
               WorkContext wc = it.next();
               if (wc instanceof HintsContext)
               {
                  HintsContext hc = (HintsContext) wc;
                  if (hc instanceof WorkContextLifecycleListener)
                  {
                     WorkContextLifecycleListener listener = (WorkContextLifecycleListener)hc;
                     listener.contextSetupComplete();   
                  }
               }
            }
         }
      }
   }

   /**
    * Check and verify work before submitting.
    * @param work the work instance
    * @param executionContext any execution context that is passed by apadater
    * @throws WorkException if any exception occurs
    */
   private void checkAndVerifyWork(Work work, ExecutionContext executionContext) throws WorkException
   {
      if (specCompliant)
      {
         verifyWork(work);
      }

      if (work instanceof WorkContextProvider)
      {
         //Implements WorkContextProvider and not-null ExecutionContext
         if (executionContext != null)
         {
            throw new WorkRejectedException(bundle.workExecutionContextMustNullImplementsWorkContextProvider());
         }
      }
   }

   /**
    * Verify the given work instance.
    * @param work The work
    * @throws WorkException Thrown if a spec compliant issue is found
    */
   private void verifyWork(Work work) throws WorkException
   {
      Class<? extends Work> workClass = work.getClass();
      String className = workClass.getName();

      if (!validatedWork.contains(className))
      {

         if (isWorkMethodSynchronized(workClass, RUN_METHOD_NAME))
            throw new WorkException(bundle.runMethodIsSynchronized(className));

         if (isWorkMethodSynchronized(workClass, RELEASE_METHOD_NAME))
            throw new WorkException(bundle.releaseMethodIsSynchronized(className));

         validatedWork.add(className);
      }
   }

   /**
    * Checks, if Work implementation class method is synchronized
    * @param workClass - implementation class
    * @param methodName - could be "run" or "release"
    * @return true, if method is synchronized, false elsewhere
    */
   private boolean isWorkMethodSynchronized(Class<? extends Work> workClass, String methodName)
   {
      try
      {
         Method method = SecurityActions.getMethod(workClass, methodName, new Class[0]);
         if (Modifier.isSynchronized(method.getModifiers()))
            return true;
      }
      catch (NoSuchMethodException e)
      {
         //Never happens, Work implementation should have these methods
      }

      return false;
   }

   /**
    * Checks work completed status.
    * @param wrapper work wrapper instance
    * @throws {@link WorkException} if work is completed with an exception
    */
   private void checkWorkCompletionException(WorkWrapper wrapper) throws WorkException
   {
      if (wrapper.getWorkException() != null)
      {
         log.tracef("Exception %s for %s", wrapper.getWorkException(), this);

         deltaWorkFailed();

         throw wrapper.getWorkException();
      }

      deltaWorkSuccessful();
   }

   /**
    * Setup work context's of the given work instance.
    *
    * @param wrapper The work wrapper instance
    * @param workListener The work listener
    * @throws WorkCompletedException if any work context related exceptions occurs
    * @throws WorkException if any exception occurs
    */
   private void setup(WorkWrapper wrapper, WorkListener workListener) throws WorkCompletedException, WorkException
   {
      log.tracef("Setting up work: %s, work listener: %s", wrapper, workListener);

      Work work = wrapper.getWork();

      if (resourceAdapter != null)
      {
         if (SecurityActions.getClassLoader(work.getClass()) instanceof WorkClassLoader)
         {
            WorkClassLoader wcl = (WorkClassLoader)SecurityActions.getClassLoader(work.getClass());
            ResourceAdapterClassLoader racl =
               new ResourceAdapterClassLoader(SecurityActions.getClassLoader(resourceAdapter.getClass()),
                                              wcl);

            wcl.setResourceAdapterClassLoader(racl);
         }
         if (work instanceof ResourceAdapterAssociation)
         {
            try
            {
               ResourceAdapterAssociation raa = (ResourceAdapterAssociation)work;
               raa.setResourceAdapter(resourceAdapter);
            }
            catch (Throwable t)
            {
               throw new WorkException(bundle.resourceAdapterAssociationFailed(work.getClass().getName()), t);
            }
         }
      }

      //If work is an instanceof WorkContextProvider
      if (work instanceof WorkContextProvider)
      {
         WorkContextProvider wcProvider = (WorkContextProvider) work;
         List<WorkContext> contexts = wcProvider.getWorkContexts();

         if (contexts != null && contexts.size() > 0)
         {
            boolean isTransactionContext = false;
            boolean isSecurityContext = false;
            boolean isHintcontext = false;

            for (WorkContext context : contexts)
            {
               Class<? extends WorkContext> contextType = null;

               // Get supported work context class
               contextType = getSupportedWorkContextClass(context.getClass());

               // Not supported
               if (contextType == null)
               {
                  if (log.isTraceEnabled())
                  {
                     log.tracef("Not supported work context class : %s", context.getClass().getName());
                  }

                  WorkCompletedException wce =
                     new WorkCompletedException(bundle.unsupportedWorkContextClass(context.getClass().getName()),
                                                WorkContextErrorCodes.UNSUPPORTED_CONTEXT_TYPE);

                  fireWorkContextSetupFailed(context, WorkContextErrorCodes.UNSUPPORTED_CONTEXT_TYPE,
                                             workListener, work, wce);

                  throw wce;
               }
               // Duplicate checks
               else
               {
                  // TransactionContext duplicate
                  if (isTransactionContext(contextType))
                  {
                     if (isTransactionContext)
                     {
                        if (log.isTraceEnabled())
                        {
                           log.tracef("Duplicate transaction work context : %s", context.getClass().getName());
                        }

                        WorkCompletedException wce =
                           new WorkCompletedException(bundle.duplicateTransactionWorkContextClass(context.getClass()
                              .getName()), WorkContextErrorCodes.DUPLICATE_CONTEXTS);

                        fireWorkContextSetupFailed(context, WorkContextErrorCodes.DUPLICATE_CONTEXTS,
                                                   workListener, work, wce);

                        throw wce;
                     }
                     else
                     {
                        isTransactionContext = true;
                     }
                  }
                  // SecurityContext duplicate
                  else if (isSecurityContext(contextType))
                  {
                     if (isSecurityContext)
                     {
                        if (log.isTraceEnabled())
                        {
                           log.tracef("Duplicate security work context : %s", context.getClass().getName());
                        }

                        WorkCompletedException wce =
                           new WorkCompletedException(bundle.duplicateSecurityWorkContextClass(context.getClass()
                              .getName()), WorkContextErrorCodes.DUPLICATE_CONTEXTS);

                        fireWorkContextSetupFailed(context, WorkContextErrorCodes.DUPLICATE_CONTEXTS,
                                                   workListener, work, wce);

                        throw wce;
                     }
                     else
                     {
                        isSecurityContext = true;
                     }
                  }
                  // HintContext duplicate
                  else if (isHintContext(contextType))
                  {
                     if (isHintcontext)
                     {
                        if (log.isTraceEnabled())
                        {
                           log.tracef("Duplicate hint work context : %s", context.getClass().getName());
                        }

                        WorkCompletedException wce =
                           new WorkCompletedException(bundle.duplicateHintWorkContextClass(context.getClass()
                              .getName()), WorkContextErrorCodes.DUPLICATE_CONTEXTS);

                        fireWorkContextSetupFailed(context, WorkContextErrorCodes.DUPLICATE_CONTEXTS,
                                                   workListener, work, wce);

                        throw wce;
                     }
                     else
                     {
                        isHintcontext = true;
                     }
                  }
               }

               // Add workcontext instance to the work
               wrapper.addWorkContext(contextType, context);
            }
         }
      }
   }

   /**
    * Calls listener with given error code.
    * @param workContext work context listener
    * @param errorCode error code
    * @param workListener work listener
    * @param work work instance
    * @param exception exception
    */
   private void fireWorkContextSetupFailed(Object workContext, String errorCode,
                                           WorkListener workListener, Work work, WorkException exception)
   {
      if (workListener != null)
      {
         WorkEvent event = new WorkEvent(this, WorkEvent.WORK_STARTED, work, null);
         workListener.workStarted(event);
      }

      if (workContext instanceof WorkContextLifecycleListener)
      {
         WorkContextLifecycleListener listener = (WorkContextLifecycleListener) workContext;
         listener.contextSetupFailed(errorCode);
      }

      if (workListener != null)
      {
         WorkEvent event = new WorkEvent(this, WorkEvent.WORK_COMPLETED, work, exception);
         workListener.workCompleted(event);
      }
   }

   /**
    * Returns true if contexts is a transaction context.
    *
    * @param workContextType context type
    * @return true if contexts is a transaction context
    */
   private boolean isTransactionContext(Class<? extends WorkContext> workContextType)
   {
      if (workContextType.isAssignableFrom(TransactionContext.class))
      {
         return true;
      }

      return false;
   }

   /**
    * Returns true if contexts is a security context.
    *
    * @param workContextType context type
    * @return true if contexts is a security context
    */
   private boolean isSecurityContext(Class<? extends WorkContext> workContextType)
   {
      if (workContextType.isAssignableFrom(SecurityContext.class))
      {
         return true;
      }

      return false;
   }

   /**
    * Returns true if contexts is a hint context.
    *
    * @param workContextType context type
    * @return true if contexts is a hint context
    */
   private boolean isHintContext(Class<? extends WorkContext> workContextType)
   {
      if (workContextType.isAssignableFrom(HintsContext.class))
      {
         return true;
      }

      return false;
   }

   /**
    * Returns work context class if given work context is supported by server,
    * returns null instance otherwise.
    *
    * @param <T> work context class
    * @param adaptorWorkContext adaptor supplied work context class
    * @return work context class
    */
   @SuppressWarnings("unchecked")
   private <T extends WorkContext> Class<T> getSupportedWorkContextClass(Class<T> adaptorWorkContext)
   {
      for (Class<? extends WorkContext> supportedWorkContext : SUPPORTED_WORK_CONTEXT_CLASSES)
      {
         // Assignable or not
         if (supportedWorkContext.isAssignableFrom(adaptorWorkContext))
         {
            Class clz = adaptorWorkContext;

            while (clz != null)
            {
               // Supported by the server
               if (clz.equals(supportedWorkContext))
               {
                  return clz;
               }

               clz = clz.getSuperclass();
            }
         }
      }

      return null;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append(getClass().getName()).append("@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[id=").append(getId());
      sb.append(" name=").append(name);
      sb.append(" specCompliant=").append(specCompliant);
      sb.append(" shortRunningExecutor=").append(shortRunningExecutor);
      sb.append(" longRunningExecutor=").append(longRunningExecutor);
      sb.append(" xaTerminator=").append(xaTerminator);
      sb.append(" validatedWork=").append(validatedWork);
      sb.append(" callbackSecurity=").append(callbackSecurity);
      sb.append(" securityIntegration=").append(securityIntegration);
      sb.append(" resourceAdapter=").append(resourceAdapter);
      sb.append(" shutdown=").append(shutdown);
      sb.append(" activeWorkWrappers=[");
      synchronized (activeWorkWrappers)
      {
         if (activeWorkWrappers.size() > 0)
         {
            Iterator<WorkWrapper> it = activeWorkWrappers.iterator();

            while (it.hasNext())
            {
               WorkWrapper ww = it.next();
               sb.append("WorkWrapper@").append(Integer.toHexString(System.identityHashCode(ww)));

               if (it.hasNext())
                  sb.append(", ");
            }
         }
      }
      sb.append("]");

      sb.append(" statistics=").append(statistics);

      toString(sb);

      sb.append("]");

      return sb.toString();
   }

   /**
    * Additional string representation
    * @param sb The string builder
    */
   public void toString(StringBuilder sb)
   {
   }
}
