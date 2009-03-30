/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.core.spec.chapter10.common;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.Work;

/**
 * BlockRunningWork.

 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class BlockRunningWork implements Work
{
   /**
    * enter run method, hasn't executed
    */
   private boolean preRun;
   
   /**
    * enter run method, has executed
    */
   private boolean postRun;
   /**
    * Latch when before run method
    */
   private CountDownLatch before;
   /**
    * Latch when before start block
    */
   private CountDownLatch hold;
   /**
    * Latch when enter run method
    */
   private CountDownLatch start;
   /**
    * Latch when leave run method
    */
   private CountDownLatch done;
   /**
    * current thread id
    */
   private long threadId;
   
  
   /**
    * Constructor.
    * @param before Latch when before run method
    * @param hold Latch when before start block
    * @param start Latch when enter run method
    * @param done Latch when leave run method
    */
   public BlockRunningWork(CountDownLatch before, CountDownLatch hold, CountDownLatch start, CountDownLatch done) 
   {
      this.before = before;
      this.hold = hold;
      this.start = start;
      this.done = done;
   }

   /**
    * release method
    */
   public void release()
   {
 
   }

   /**
    * run method
    */
   public void run()
   {
      try
      {
         before.await();
      } 
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
      preRun = true;
      hold.countDown(); 
      try
      {
         start.await();
      } 
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
      threadId = Thread.currentThread().getId();
      postRun = true;
      done.countDown(); 
   }
   
   /**
    * @return boolean if enter run method, hasn't executed
    */
   public boolean hasPreRun()
   {
      return preRun;
      
   }
   
   /**
    * @return boolean if enter run method, has executed
    */
   public boolean hasPostRun()
   {
      return postRun;
   }


   /**
    * @return long current thread id
    */
   public long getThreadId()
   {
      return threadId;
   }
}
