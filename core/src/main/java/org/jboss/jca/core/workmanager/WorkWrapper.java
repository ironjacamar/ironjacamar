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

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SimplePrincipal;
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
   private static Logger log = Logger.getLogger(WorkWrapper.class);

   /** Whether we are tracing */
   private static boolean trace = log.isTraceEnabled();
   
   /** The work */
   private Work work;

   /** The execution context */
   private ExecutionContext executionContext;
   
   /**If work is an instance of WorkContextProvider, it may contain WorkContext instances */
   private Map<Class<? extends WorkContext>, WorkContext> workContexts;

   /** the work listener */
   private WorkListener workListener;   

   /** The work manager */
   private org.jboss.jca.core.api.WorkManager workManager;

   /** The blocked time */
   private long blockedTime;

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
    * @throws IllegalArgumentException for null work, execution context or a negative start timeout
    */
   public WorkWrapper(org.jboss.jca.core.api.WorkManager workManager, 
                      Work work, 
                      ExecutionContext executionContext, 
                      WorkListener workListener,
                      CountDownLatch startedLatch,
                      CountDownLatch completedLatch)
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
      this.workContexts = null;
   }
   
   /**
    * Get the work manager
    *
    * @return the work manager
    */
   public org.jboss.jca.core.api.WorkManager getWorkManager()
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
    * Run
    */
   public void run()
   {
      if (trace)
         log.trace("Starting work " + this);  

      org.jboss.security.SecurityContext oldSC = SecurityContextAssociation.getSecurityContext();

      try
      {
         start();

         if (startedLatch != null)
            startedLatch.countDown();

         work.run();

         end();
      }
      catch (Exception e)
      {
         exception = new WorkCompletedException(e);

         cancel();
      } 
      finally
      {
         work.release();

         if (workListener != null)
         {
            WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_COMPLETED, work, exception);
            workListener.workCompleted(event);
         }

         SecurityContextAssociation.setSecurityContext(oldSC);

         if (startedLatch != null)
         {
            while (startedLatch.getCount() != 0)
               startedLatch.countDown();
         }

         if (completedLatch != null)
            completedLatch.countDown();

         if (trace)
            log.trace("Executed work " + this);  
      }
   }

   /**
    * Start
    * @throws WorkException for any error 
    */
   protected void start() throws WorkException
   {
      if (trace)
      {
         log.trace("Starting work " + this);  
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
         try
         {
            org.jboss.security.SecurityContext sc = SecurityContextFactory.createSecurityContext("work");  
            SecurityContextAssociation.setSecurityContext(sc);

            // Setup callbacks
            CallbackHandler cbh = new JASPICallbackHandler();
            List<Callback> callbacks = new ArrayList<Callback>();

            Set<String> users = workManager.getCallbackSecurity().getUsers();

            if (users != null && users.size() > 0)
            {
               for (String user : users)
               {
                  Subject subject = new Subject();
                  Principal principal = new SimplePrincipal(user);
                  char[] cred = workManager.getCallbackSecurity().getCredential(user);
                  String[] roles = workManager.getCallbackSecurity().getRoles(user);

                  GroupPrincipalCallback gpc = new GroupPrincipalCallback(subject, roles);
                  CallerPrincipalCallback cpc = new CallerPrincipalCallback(subject, principal);
                  PasswordValidationCallback pvc = new PasswordValidationCallback(subject, principal.getName(), cred);

                  callbacks.add(gpc);
                  callbacks.add(cpc);
                  callbacks.add(pvc);
               }
            }
            else
            {
               if (log.isDebugEnabled())
                  log.debug("No users defined");
            }

            Callback[] cb = new Callback[callbacks.size()];
            cbh.handle(callbacks.toArray(cb));

            // Subjects for execution environment
            Subject executionSubject = new Subject();
            Subject serviceSubject = null;
         
            // Resource adapter callback
            securityContext.setupSecurityContext(cbh, executionSubject, serviceSubject);

            // Set the authenticated subject
            sc.getSubjectInfo().setAuthenticatedSubject(executionSubject);
         }
         catch (Throwable t)
         {
            log.error("SecurityContext setup failed: " + t.getMessage(), t);
            fireWorkContextSetupFailed(ctx);
            throw new WorkException("SecurityContext setup failed: " + t.getMessage(), t);
         }
      }
      else if (securityContext != null && workManager.getCallbackSecurity() == null)
      {
         log.error("SecurityContext setup failed since CallbackSecurity was null");
         fireWorkContextSetupFailed(ctx);
         throw new WorkException("SecurityContext setup failed since CallbackSecurity was null");
      }
      
      //Fires Context setup complete
      fireWorkContextSetupComplete(ctx);
      
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            workManager.getXATerminator().startWork(work, xid);
         }
      }

      if (workListener != null)
      {
         WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_STARTED, work, null);
         workListener.workStarted(event);
      }

      if (trace)
      {
         log.trace("Started work " + this);  
      }
   }

   /**
    * End
    */
   protected void end()
   {
      if (trace)
      {
         log.trace("Ending work " + this);  
      }

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
      {
         log.trace("Ended work " + this);  
      }
   }

   /**
    * Cancel
    */
   protected void cancel()
   {
      if (trace)
         log.trace("Cancel work " + this);  

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
         log.trace("Canceled work " + this);  
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

      if (workListener != null)
         buffer.append(" workListener=").append(workListener);
      if (exception != null)
         buffer.append(" exception=").append(exception);
      buffer.append("]");
      return buffer.toString();
   }
}
