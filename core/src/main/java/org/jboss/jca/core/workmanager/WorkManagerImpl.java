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
import org.jboss.jca.common.util.ClassUtil;
import org.jboss.jca.core.api.WorkManager;
import org.jboss.jca.core.api.WorkWrapper;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkRejectedException;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.tm.JBossXATerminator;
import org.jboss.util.threadpool.Task;

/**
 * The work manager implementation.
 * 
 * @author gurkanerdogdu
 * @version $Rev$ $Date$
 */
public class WorkManagerImpl implements WorkManager
{
   /** The logger */
   private static Logger log = Logger.getLogger(WorkManagerImpl.class);
   
   /**Supported work context set*/
   private static final Set<Class<? extends WorkContext>> SUPPORTED_WORK_CONTEXT_CLASSES = 
       new HashSet<Class<? extends WorkContext>>(); 

   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /** Running in spec compliant mode */
   private boolean specCompliant;

   /** The thread pool */
   private ThreadPool threadPool;

   /** The XA terminator */
   private JBossXATerminator xaTerminator;
   
   /**Work run method name*/
   private static final String RUN_METHOD_NAME = "run";
   
   /**Work release method name*/
   private static final String RELEASE_METHOD_NAME = "release";
   

   /**Default supported workcontext types*/
   static
   {
      SUPPORTED_WORK_CONTEXT_CLASSES.add(TransactionContext.class);
      SUPPORTED_WORK_CONTEXT_CLASSES.add(SecurityContext.class);
      SUPPORTED_WORK_CONTEXT_CLASSES.add(HintsContext.class);
   }
   
   /**
    * Default constructor.
    * <p>
    * Defines a default spec compliant.
    * </p>
    */
   public WorkManagerImpl()
   {
      specCompliant = true;
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
      checkAndVerifyWork(work, execContext);
      
      if (execContext == null)
      {
         execContext = new ExecutionContext();  
      }

      WorkWrapper wrapper = 
         new WorkWrapper(this, work, Task.WAIT_FOR_COMPLETE, startTimeout, execContext, workListener);

      //Submit Work Instance
      submitWork(wrapper);
      
      //Check Result
      checkWorkCompletionException(wrapper);

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
      checkAndVerifyWork(work, execContext);
      
      if (execContext == null)
      {
         execContext = new ExecutionContext();  
      }

      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_FOR_START, startTimeout, execContext, workListener);
      
      //Submit Work Instance
      submitWork(wrapper);
      
      //Check Result
      checkWorkCompletionException(wrapper);

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
      checkAndVerifyWork(work, execContext);
      
      if (execContext == null)
      {
         execContext = new ExecutionContext();  
      }

      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_NONE, startTimeout, execContext, workListener);
      
      //Submit Work Instance
      submitWork(wrapper);
      
      //Check Result
      checkWorkCompletionException(wrapper);
   }

   /**
    * Imports any work.
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void importWork(WorkWrapper wrapper) throws WorkException
   {
      if (wrapper == null)
      {
         return;  
      }
            
      trace = log.isTraceEnabled();
      
      if (trace)
      {
         log.trace("Importing work " + wrapper);  
      }

      ExecutionContext ctx = wrapper.getWorkContext(TransactionContext.class);
      if (ctx == null)
      {
         ctx = wrapper.getExecutionContext();
      }
      
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
      
      //Fires Context setup complete
      fireWorkContextSetupComplete(ctx);
      
      if (trace)
      {
         log.trace("Imported work " + wrapper);  
      }
   }
   
   /**
    * Submit the given work instance for executing by the thread pool.
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void submitWork(WorkWrapper wrapper) throws WorkException
   {
      if (wrapper == null)
      {
         return;  
      }
      
      if (trace)
      {
         log.trace("Submitting work to thread pool " + wrapper);  
      }

      threadPool.runTaskWrapper(wrapper);

      if (trace)
      {
         log.trace("Submitted work to thread pool " + wrapper);  
      }
   }

   /**
    * Starts given work instance.
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   public void startWork(WorkWrapper wrapper) throws WorkException
   {
      if (wrapper == null)
      {
         return;  
      }      

      if (trace)
      {
         log.trace("Setting up work contexts " + wrapper);  
      }
      
      //Setting up WorkContexts if an exist
      setupWorkContextProviders(wrapper);
      
      if (trace)
      {
         log.trace("Setted up work contexts " + wrapper);  
      }
      
      //Import work instance
      importWork(wrapper);
            
      if (trace)
      {
         log.trace("Starting work " + wrapper);  
      }

      ExecutionContext ctx = wrapper.getWorkContext(TransactionContext.class);
      if (ctx == null)
      {
         ctx = wrapper.getExecutionContext();
      }
      
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.startWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
      {
         log.trace("Started work " + wrapper);  
      }
   }

   /**
    * Ends given work instance.
    * @param wrapper the work wrapper
    */
   public void endWork(WorkWrapper wrapper)
   {
      if (wrapper == null)
      {
         return;  
      }
      
      if (trace)
      {
         log.trace("Ending work " + wrapper);  
      }

      ExecutionContext ctx = wrapper.getWorkContext(TransactionContext.class);
      if (ctx == null)
      {
         ctx = wrapper.getExecutionContext();
      }

      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.endWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
      {
         log.trace("Ended work " + wrapper);  
      }
   }

   /**
    * Cancels given work instance.
    * @param wrapper the work wrapper
    */
   public void cancelWork(WorkWrapper wrapper)
   {
      if (wrapper == null)
      {
         return;  
      }
      
      if (trace)
      {
         log.trace("Cancel work " + wrapper);  
      }

      ExecutionContext ctx = wrapper.getWorkContext(TransactionContext.class);
      if (ctx == null)
      {
         ctx = wrapper.getExecutionContext();
      }

      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.cancelWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
      {
         log.trace("Canceled work " + wrapper);  
      }
   }

   /**
    * Verify the given work instance.
    * @param work The work
    * @throws WorkException Thrown if a spec compliant issue is found
    */
   private void verifyWork(Work work) throws WorkException
   {     
      Class<?> workClass = work.getClass();
      boolean result = false;
      
      result = verfiyWorkMethods(workClass, RUN_METHOD_NAME, null, workClass.getName() + 
            ": Run method is not defined");
     
      if (!result)
      {
         throw new WorkException(workClass.getName() + ": Run method is synchronized");
      }
      
      result = verfiyWorkMethods(workClass, RELEASE_METHOD_NAME, null, workClass.getName() + 
            ": Release method is not defined");
      
      if (!result)
      {
         throw new WorkException(workClass.getName() + ": Release method is synchronized");
      }
   }
   
   /**
    * Setup work context's of the given work instance.
    * 
    * @param wrapper wrapper work instance
    * @throws WorkException if any exception occurs
    */
   private void setupWorkContextProviders(WorkWrapper wrapper) throws WorkException
   {
      if (trace)
      {
         log.trace("Starting checking work context providers");
      }

      Work work = wrapper.getWork();

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
                  if (trace)
                  {
                     log.trace("Not supported work context class : " + context.getClass());
                  }
                  
                  fireWorkContextSetupFailed(context, WorkContextErrorCodes.UNSUPPORTED_CONTEXT_TYPE);
                  
                  throw new WorkCompletedException("Unsupported WorkContext class : " + context.getClass(), 
                      WorkContextErrorCodes.UNSUPPORTED_CONTEXT_TYPE);
               }
               // Duplicate checks
               else
               {
                  // TransactionContext duplicate
                  if (isTransactionContext(contextType))
                  {
                     if (isTransactionContext)
                     {
                        if (trace)
                        {
                           log.trace("Duplicate transaction work context : " + context.getClass());
                        }

                        fireWorkContextSetupFailed(context, WorkContextErrorCodes.DUPLICATE_CONTEXTS);
                        
                        throw new WorkCompletedException("Duplicate TransactionWorkContext class : " + 
                            context.getClass(), WorkContextErrorCodes.DUPLICATE_CONTEXTS);
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
                        if (trace)
                        {
                           log.trace("Duplicate security work context : " + context.getClass());
                        }
                        
                        fireWorkContextSetupFailed(context, WorkContextErrorCodes.DUPLICATE_CONTEXTS);

                        throw new WorkCompletedException("Duplicate SecurityWorkContext class : " + context.getClass(), 
                              WorkContextErrorCodes.DUPLICATE_CONTEXTS);
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
                        if (trace)
                        {
                           log.trace("Duplicate hint work context : " + context.getClass());
                        }

                        fireWorkContextSetupFailed(context, WorkContextErrorCodes.DUPLICATE_CONTEXTS);
                        
                        throw new WorkCompletedException("Duplicate HintWorkContext class : " + context.getClass(), 
                              WorkContextErrorCodes.DUPLICATE_CONTEXTS);
                     }
                     else
                     {
                        isHintcontext = true;
                     }
                  }
                  // Normally, this must not be happened!i just safe check!
                  else
                  {
                     fireWorkContextSetupFailed(context, WorkContextErrorCodes.UNSUPPORTED_CONTEXT_TYPE);
                     
                     throw new WorkCompletedException("Unsupported WorkContext class : " + context.getClass(), 
                           WorkContextErrorCodes.UNSUPPORTED_CONTEXT_TYPE);
                  }
               }

               // Add workcontext instance to the work
               wrapper.addWorkContext(contextType, context);
            }
         }         
      }      
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
            // Supported by the server
            if (adaptorWorkContext.equals(supportedWorkContext))
            {
               return adaptorWorkContext;
            }
            else
            {
               // Fallback to super class
               return (Class<T>) adaptorWorkContext.getSuperclass();
            }
         }
      }

      return null;
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
    * Check and verfiy work before submitting.
    * @param work the work instance
    * @param executionContext any execution context that is passed by apadater
    * @throws WorkException if any exception occurs
    */
   private void checkAndVerifyWork(Work work, ExecutionContext executionContext) throws WorkException
   {
      if (work == null)
      {
         throw new WorkException("Null work");  
      }

      if (specCompliant)
      {
         verifyWork(work);  
      }   
      
      if (work instanceof WorkContextProvider)
      {
          //Implements WorkContextProvider and not-null ExecutionContext
         if (executionContext != null)
         {
            throw new WorkRejectedException("Work execution context must be null because " +
               "work instance implements WorkContextProvider!");
         }          
      }      
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
         throw wrapper.getWorkException();  
      }      
   }

   private boolean verfiyWorkMethods(Class<?> workClass, String methodName, 
         Class<?>[] parameterTypes, String errorMessage) throws WorkException
   {
      Method method = null;
      try
      {
         method = ClassUtil.getClassMethod(workClass, methodName, null);
         
         if (ClassUtil.modifiersHasSynchronizedKeyword(method.getModifiers()))
         {
            return false;  
         }
      }
      catch (NoSuchMethodException nsme)
      {
         throw new WorkException(errorMessage);
      }
      
      return true;
   }
   
   /**
    * Calls listener with given error code.
    * @param listener work context listener
    * @param errorCode error code
    */
   private void fireWorkContextSetupFailed(Object workContext, String errorCode)
   {
      if (workContext instanceof WorkContextLifecycleListener)
      {
         WorkContextLifecycleListener listener = (WorkContextLifecycleListener)workContext;
         listener.contextSetupFailed(errorCode);   
      }
      
   }
   
   /**
    * Calls listener after work context is setted up.
    * @param listener work context listener
    */
   private void fireWorkContextSetupComplete(Object workContext)
   {
      if (workContext instanceof WorkContextLifecycleListener)
      {
         WorkContextLifecycleListener listener = (WorkContextLifecycleListener)workContext;
         listener.contextSetupComplete();   
      }
      
   }
   
}
