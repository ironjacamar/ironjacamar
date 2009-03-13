/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.core.spec.chapter10;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

/**
 * SimpleWork.

 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class SimpleWork implements Work
{
   /**
    * block time
    */
   public static final int BLOCK_TIME = 100;
   /**
    * follow time
    */
   public static final int FOLLOW_TIME = 50;
   
   private static StringBuffer buf = new StringBuffer();
   private static WorkManager workManager = null;
   
   private boolean callRun = false;
   private boolean callRelease = false;
   private boolean throwWorkAException = false;
   private boolean blockRun = false;
   private String name = "";
   private boolean nestDoWork = false;
   private boolean nestStartWork = false;

   /**
    * SimpleWork.
    */
   public SimpleWork()
   {
   }
   
   /**
    * SimpleWork.
    * @param name test name
    */
   public SimpleWork(String name)
   {
      this.name = name;
   }

   /**
    * The <code>WorkManager</code> might call this method to hint the
    * active <code>Work</code> instance to complete execution as soon as 
    * possible. This would be called on a seperate thread other than the
    * one currently executing the <code>Work</code> instance.
    */
   public void release()
   {
      synchronized (this)
      {
         callRelease = true;
      }
   }

   /**
    * When an object implementing interface <code>Runnable</code> is used 
    * to create a thread, 
    *
    * @see     java.lang.Thread#run()
    */
   public void run()
   {

      if (throwWorkAException)
         throw new WorkAException();
      
      if (nestDoWork)
      {
         SimpleWork workB = new SimpleWork("B");
         workB.setBlockRun(true);
         try
         {
            workManager.doWork(workB);
         } 
         catch (WorkException e)
         {
            e.printStackTrace();
         }
      }
      if (nestStartWork)
      {
         SimpleWork workB = new SimpleWork("B");
         workB.setBlockRun(true);
         try
         {
            workManager.startWork(workB);
         } 
         catch (WorkException e)
         {
            e.printStackTrace();
         }
      }
      if (blockRun)
      {
         try
         {
            Thread.currentThread().sleep(BLOCK_TIME);
         } 
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }
      synchronized (this)
      {
         callRun = true;
      }

      buf.append(name);
   }

   /**
    * setWorkManager.
    * @param workManager work manager
    */
   public static void setWorkManager(WorkManager workManager)
   {
      SimpleWork.workManager = workManager;
   }

   /**
    * call run.
    * @return boolean
    */   
   public boolean isCallRun()
   {
      return callRun;
   }

   /**
    * setWorkManager.
    * @param throwWorkAException if throw WorkAException
    */
   public void setThrowWorkAException(boolean throwWorkAException)
   {
      this.throwWorkAException = throwWorkAException;
   }

   /**
    * call release
    * @return boolean
    */   
   public boolean isCallRelease()
   {
      return callRelease;
   }

   /**
    * setBlockRun.
    * @param blockRun if block
    */
   public void setBlockRun(boolean blockRun)
   {
      this.blockRun = blockRun;
   }
   
   /**
    * setNestDoWork.
    * @param nestDoWork if nest doWork
    */
   public void setNestDoWork(boolean nestDoWork)
   {
      this.nestDoWork = nestDoWork;
   }
   
   /**
    * setNestStartWork.
    * @param nestStartWork if nest startWork
    */
   public void setNestStartWork(boolean nestStartWork)
   {
      this.nestStartWork = nestStartWork;
   }
   
   /**
    * get string buffer
    * @return String buffer
    */  
   public String getStringBuffer()
   {
      return buf.toString();
   }
   
   /**
    * reset string buffer
    */  
   public void resetStringBuffer()
   {
      buf = new StringBuffer();
   }

   /**
    * WorkAException
    */  
   public class WorkAException extends RuntimeException
   {
      private static final long serialVersionUID = 1L;
   }
}
