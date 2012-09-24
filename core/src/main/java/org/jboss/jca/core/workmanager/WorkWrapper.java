/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextErrorCodes;
import javax.resource.spi.work.WorkContextLifecycleListener;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.auth.callback.JASPICallbackHandler;

/**
 * Wraps the resource adapter's work.
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: 71538 $
 */
public class WorkWrapper implements Runnable
{
   /** The log */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
      WorkWrapper.class.getName());

   /** Whether we are tracing */
   private static boolean trace = log.isTraceEnabled();
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** The work */
   private Work work;

   /** The execution context */
   private ExecutionContext executionContext;
   
   /**If work is an instance of WorkContextProvider, it may contain WorkContext instances */
   private Map<Class<? extends WorkContext>, WorkContext> workContexts;

   /** the work listener */
   private WorkListener workListener;   

   /** The work manager */
   private WorkManagerImpl workManager;

   /** The start time */
   private long startTime;

   /** Any exception */
   private WorkException exception;

   /** Started latch */
   private CountDownLatch startedLatch;

   /** Completed latch */
   private CountDownLatch completedLatch;

   /**
    * Create a new WorkWrapper
    *
    * @param workManager the work manager
    * @param work the work
    * @param executionContext the execution context
    * @param workListener the WorkListener
    * @param startedLatch The latch for when work has started
    * @param completedLatch The latch for when work has completed
    * @param startTime The start time
    * @throws IllegalArgumentException for null work, execution context or a negative start timeout
    */
   public WorkWrapper(WorkManagerImpl workManager, 
                      Work work, 
                      ExecutionContext executionContext, 
                      WorkListener workListener,
                      CountDownLatch startedLatch,
                      CountDownLatch completedLatch,
                      long startTime)
   {
      super();

      if (work == null)
         throw new IllegalArgumentException("Null work");
      if (executionContext == null)
         throw new IllegalArgumentException("Null execution context");

      this.workManager = workManager;
      this.work = work;
      this.executionContext = executionContext;
      this.workListener = workListener;
      this.startedLatch = startedLatch;
      this.completedLatch = completedLatch;
      this.startTime = startTime;
      this.workContexts = null;
   }
   
   /**
    * Get the work manager
    *
    * @return the work manager
    */
   public org.jboss.jca.core.api.workmanager.WorkManager getWorkManager()
   {
      return workManager;
   }

   /**
    * Retrieve the work
    *
    * @return the work
    */
   public Work getWork()
   {
      return work;
   }

   /**
    * Retrieve the exection context
    *
    * @return the execution context
    */
   public ExecutionContext getExecutionContext()
   {
      return executionContext;
   }

   /**
    * Retrieve the work listener
    *
    * @return the WorkListener
    */
   public WorkListener getWorkListener()
   {
      return workListener;
   }
   
   /**
    * Get any exception
    * 
    * @return the exception or null if there is none
    */
   public WorkException getWorkException()
   {
      return exception;
   }

   /**
    * Set work exception
    * @param e The exception
    */
   void setWorkException(WorkException e)
   {
      exception = e;
   }

   /**
    * Run
    */
   public void run()
   {
      ClassLoader oldCL = SecurityActions.getThreadContextClassLoader();
      SecurityActions.setThreadContextClassLoader(work.getClass().getClassLoader());

      org.jboss.security.SecurityContext oldSC = SecurityContextAssociation.getSecurityContext();

      try
      {
         start();
         workManager.addWorkWrapper(this);

         if (startedLatch != null)
            startedLatch.countDown();

         work.run();

         end();
      }
      catch (Throwable t)
      {
         exception = new WorkCompletedException(t.getMessage(), t);

         cancel();
      } 
      finally
      {
         workManager.removeWorkWrapper(this);
         work.release();

         if (workListener != null)
         {
            WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_COMPLETED, work, exception);
            workListener.workCompleted(event);
         }

         SecurityContextAssociation.setSecurityContext(oldSC);
         SecurityActions.setThreadContextClassLoader(oldCL);

         if (startedLatch != null)
         {
            while (startedLatch.getCount() != 0)
               startedLatch.countDown();
         }

         if (completedLatch != null)
            completedLatch.countDown();

         if (trace)
            log.tracef("Executed work: %s", this);
      }
   }

   /**
    * Start
    * @throws WorkException for any error 
    */
   protected void start() throws WorkException
   {
      if (trace)
         log.tracef("Starting work: %s", this);  

      if (workListener != null)
      {
         long duration = System.currentTimeMillis() - startTime;
         if (duration < 0)
            duration = javax.resource.spi.work.WorkManager.UNKNOWN;

         WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_STARTED, work, null, duration);
         workListener.workStarted(event);
      }

      // Transaction setup
      ExecutionContext ctx = getWorkContext(TransactionContext.class);
      if (ctx == null)
      {
         ctx = getExecutionContext();
      }
      
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            //JBAS-4002 base value is in seconds as per the API, here we convert to millis
            long timeout = (ctx.getTransactionTimeout() * 1000);
            workManager.getXATerminator().registerWork(work, xid, timeout);
         }
      }

      // Security setup
      javax.resource.spi.work.SecurityContext securityContext = 
         getWorkContext(javax.resource.spi.work.SecurityContext.class);
      if (securityContext != null && workManager.getCallbackSecurity() != null)
      {
         if (trace)
            log.tracef("Setting security context: %s", securityContext);

         try
         {
            // Security context
            org.jboss.security.SecurityContext sc = null;

            // Setup callbacks
            CallbackHandler cbh = new JASPICallbackHandler();

            // Subjects for execution environment
            Subject executionSubject = null;
            Subject serviceSubject = null;
         
            if (trace)
               log.tracef("Callback security: %s", workManager.getCallbackSecurity());

            if (SecurityContextAssociation.getSecurityContext() == null ||
                workManager.getCallbackSecurity().getDomain() != null)
            {
               String scDomain = workManager.getCallbackSecurity().getDomain();

               if (trace)
                  log.tracef("Creating security context: %s", scDomain);

               if (scDomain == null || scDomain.trim().equals(""))
               {
                  fireWorkContextSetupFailed(ctx);
                  throw new WorkException(bundle.securityContextSetupFailedSinceCallbackSecurityDomainWasEmpty());
               }

               sc = SecurityContextFactory.createSecurityContext(scDomain);
               SecurityContextAssociation.setSecurityContext(sc);
            }
            else
            {
               sc = SecurityContextAssociation.getSecurityContext();

               if (trace)
                  log.tracef("Using security context: %s", sc);
            }

            executionSubject = sc.getSubjectInfo().getAuthenticatedSubject();

            if (executionSubject == null)
            {
               if (trace)
                  log.tracef("Creating empty subject");

               executionSubject = new Subject();
            }

            // Resource adapter callback
            securityContext.setupSecurityContext(cbh, executionSubject, serviceSubject);

            List<Callback> callbacks = new ArrayList<Callback>();
            if (workManager.getCallbackSecurity().isMappingRequired())
            {
               // JCA 1.6: 16.4.4
            }

            if (workManager.getCallbackSecurity().getDefaultPrincipal() != null)
            {
               Principal defaultPrincipal = workManager.getCallbackSecurity().getDefaultPrincipal();
               CallerPrincipalCallback cpc =
                  new CallerPrincipalCallback(executionSubject, defaultPrincipal);

               callbacks.add(cpc);
            }

            if (workManager.getCallbackSecurity().getDefaultGroups() != null)
            {
               String[] defaultGroups = workManager.getCallbackSecurity().getDefaultGroups();
               GroupPrincipalCallback gpc = 
                  new GroupPrincipalCallback(executionSubject, defaultGroups);

               callbacks.add(gpc);
            }

            if (callbacks.size() > 0)
            {
               Callback[] cb = new Callback[callbacks.size()];
               cbh.handle(callbacks.toArray(cb));
            }

            if (trace)
               log.tracef("Setting authenticated subject (%s) on security context (%s)", executionSubject, sc);

            // Set the authenticated subject
            sc.getSubjectInfo().setAuthenticatedSubject(executionSubject);
         }
         catch (Throwable t)
         {
            log.securityContextSetupFailed(t.getMessage(), t);
            fireWorkContextSetupFailed(ctx);
            throw new WorkException(bundle.securityContextSetupFailed(t.getMessage()), t);
         }
      }
      else if (securityContext != null && workManager.getCallbackSecurity() == null)
      {
         log.securityContextSetupFailedCallbackSecurityNull();
         fireWorkContextSetupFailed(ctx);
         throw new WorkException(bundle.securityContextSetupFailedSinceCallbackSecurityWasNull());
      }
   
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            workManager.getXATerminator().startWork(work, xid);
         }
      }

      //Fires Context setup complete
      fireWorkContextSetupComplete(ctx);
      
      if (trace)
         log.tracef("Started work: %s", this);  
   }

   /**
    * End
    */
   protected void end()
   {
      if (trace)
         log.tracef("Ending work: %s", this);  

      ExecutionContext ctx = getWorkContext(TransactionContext.class);
      if (ctx == null)
      {
         ctx = getExecutionContext();
      }

      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            workManager.getXATerminator().endWork(work, xid);
         }
      }

      if (trace)
         log.tracef("Ended work: %s", this);  
   }

   /**
    * Cancel
    */
   protected void cancel()
   {
      if (trace)
         log.tracef("Cancel work: %s", this);  

      ExecutionContext ctx = getWorkContext(TransactionContext.class);
      if (ctx == null)
      {
         ctx = getExecutionContext();
      }

      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            workManager.getXATerminator().cancelWork(work, xid);
         }
      }

      if (trace)
         log.tracef("Canceled work: %s", this);  
   }

   /**
    * Returns work context instance.
    * 
    * @param <T> class type info
    * @param workContextClass work context type
    * @return work context instance
    */
   public <T> T getWorkContext(Class<T> workContextClass)
   {
      T instance = null;

      if (workContexts != null && workContexts.containsKey(workContextClass))
      {
         instance = workContextClass.cast(workContexts.get(workContextClass));
      }

      return instance;
   }

   /**
    * Adds new work context.
    * 
    * @param workContext new work context
    * @param workContextClass work context class
    */
   public void addWorkContext(Class<? extends WorkContext> workContextClass, WorkContext workContext)
   {
      if (workContextClass == null)
      {
         throw new IllegalArgumentException("Work context class is null");
      }

      if (workContext == null)
      {
         throw new IllegalArgumentException("Work context is null");
      }

      if (workContexts == null)
      {
         workContexts = new HashMap<Class<? extends WorkContext>, WorkContext>(1);
      }

      if (trace)
         log.tracef("Adding work context %s for %s", workContextClass, this);  

      workContexts.put(workContextClass, workContext);
   }
   
   /**
    * Calls listener after work context is setted up.
    * @param listener work context listener
    */
   private void fireWorkContextSetupComplete(Object workContext)
   {
      if (workContext != null && workContext instanceof WorkContextLifecycleListener)
      {
         if (trace)
            log.tracef("WorkContextSetupComplete(%s) for %s", workContext, this);  

         WorkContextLifecycleListener listener = (WorkContextLifecycleListener)workContext;
         listener.contextSetupComplete();   
      }
   }
   
   /**
    * Calls listener if setup failed
    * @param listener work context listener
    */
   private void fireWorkContextSetupFailed(Object workContext)
   {
      if (workContext != null && workContext instanceof WorkContextLifecycleListener)
      {
         if (trace)
            log.tracef("WorkContextSetupFailed(%s) for %s", workContext, this);  

         WorkContextLifecycleListener listener = (WorkContextLifecycleListener)workContext;
         listener.contextSetupFailed(WorkContextErrorCodes.CONTEXT_SETUP_FAILED);   
      }
   }
   
   /**
    * String representation
    * @return The string
    */
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(100);
      buffer.append("WorkWrapper@").append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[workManger=").append(workManager);
      buffer.append(" work=").append(work);

      if (executionContext != null && executionContext.getXid() != null)
      {
         buffer.append(" xid=").append(executionContext.getXid());
         buffer.append(" txTimeout=").append(executionContext.getTransactionTimeout());
      }

      buffer.append(" workListener=").append(workListener);

      buffer.append(" workContexts=").append(workContexts);

      buffer.append(" exception=").append(exception);

      buffer.append("]");

      return buffer.toString();
   }
}
