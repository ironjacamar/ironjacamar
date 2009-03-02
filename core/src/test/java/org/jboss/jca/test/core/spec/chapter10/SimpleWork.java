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

public class SimpleWork implements Work
{
   public static int BLOCK_TIME = 100;
   public static int Follow_TIME = 50;
   
   private static StringBuffer buf = new StringBuffer();
   private static WorkManager workManager = null;
   
   private boolean callRun = false;
   private boolean callRelease = false;
   private boolean throwWorkAException = false;
   private boolean blockRun = false;
   private String name = "";
   private boolean nestDoWork = false;
   private boolean nestStartWork = false;


   public SimpleWork()
   {
   }
   
   public SimpleWork(String name)
   {
      this.name = name;
   }

   public void release()
   {
      synchronized(this)
      {
         callRelease = true;
      }
   }

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
         } catch (WorkException e)
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
         } catch (WorkException e)
         {
            e.printStackTrace();
         }
      }
      if (blockRun)
      {
         try
         {
            Thread.currentThread().sleep(BLOCK_TIME);
         } catch (InterruptedException e)
         {
         }
      }
      synchronized(this)
      {
         callRun = true;
      }

      buf.append(name);
   }

   public static void setWorkManager(WorkManager workManager)
   {
      SimpleWork.workManager = workManager;
   }

   public boolean isCallRun()
   {
      return callRun;
   }

   public void setThrowWorkAException(boolean throwWorkAException)
   {
      this.throwWorkAException = throwWorkAException;
   }

   public boolean isCallRelease()
   {
      return callRelease;
   }

   public void setBlockRun(boolean blockRun)
   {
      this.blockRun = blockRun;
   }
   
   public void setNestDoWork(boolean nestDoWork)
   {
      this.nestDoWork = nestDoWork;
   }
   
   public void setNestStartWork(boolean nestStartWork)
   {
      this.nestStartWork = nestStartWork;
   }
   
   public String getStringBuffer()
   {
      return buf.toString();
   }
   public void resetStringBuffer()
   {
      buf = new StringBuffer();
   }

   public class WorkAException extends RuntimeException
   {
      private static final long serialVersionUID = 1L;
   }
}
