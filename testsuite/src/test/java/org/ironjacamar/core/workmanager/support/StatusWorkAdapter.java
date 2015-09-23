/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.core.workmanager.support;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkAdapter;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;

import static org.junit.Assert.*;

/**
 * StatusWorkAdapter
 */
public class StatusWorkAdapter extends WorkAdapter
{

   /** event source */
   private Object source;

   /** event work */
   private Work work;

   /** start duration time */
   private long startDuration;

   /** exception */
   private WorkException exception;

   private CallbackCount callbackCount;

   /**
    * accept work 
    *
    * @param e workEvent
    */
   @Override
   public void workAccepted(WorkEvent e)
   {
      if (e.getType() != WorkEvent.WORK_ACCEPTED)
         fail("Wrong accepted type");
      source = e.getSource();
      work = e.getWork();
      startDuration = e.getStartDuration();
      exception = e.getException();

      if (callbackCount != null)
      {
         synchronized (this)
         {
            callbackCount.setAcceptCount(callbackCount.getAcceptCount() + 1);
         }
      }

      super.workAccepted(e);
   }

   /**
    * start work 
    *
    * @param e workEvent
    */
   @Override
   public void workStarted(WorkEvent e)
   {
      if (e.getType() != WorkEvent.WORK_STARTED)
         fail("Wrong started type");

      if (callbackCount != null)
      {
         synchronized (this)
         {
            callbackCount.setStartCount(callbackCount.getStartCount() + 1);
         }
      }

      super.workStarted(e);
   }

   /**
    * start work 
    *
    * @param e workEvent
    */
   @Override
   public void workRejected(WorkEvent e)
   {
      if (e.getType() != WorkEvent.WORK_REJECTED)
         fail("Wrong rejected type");

      source = e.getSource();
      work = e.getWork();
      startDuration = e.getStartDuration();
      exception = e.getException();

      if (callbackCount != null)
      {
         synchronized (this)
         {
            callbackCount.setRejectedCount(callbackCount.getRejectedCount() + 1);
         }
      }

      super.workRejected(e);
   }

   /**
    * complete work 
    *
    * @param e workEvent
    */
   @Override
   public void workCompleted(WorkEvent e)
   {
      if (e.getType() != WorkEvent.WORK_COMPLETED)
         fail("Wrong completed type");

      if (callbackCount != null)
      {
         synchronized (this)
         {
            callbackCount.setCompletedCount(callbackCount.getCompletedCount() + 1);
         }
      }

      super.workCompleted(e);
   }

   /**
    * get event source
    *
    * @return Object source
    */
   public Object getSource()
   {
      return source;
   }

   /**
    * get event work
    *
    * @return Work work reference
    */
   public Work getWork()
   {
      return work;
   }

   /**
    * get start duration time
    *
    * @return long duration time
    */
   public long getStartDuration()
   {
      return startDuration;
   }

   /**
    * get exception 
    * @return exception
    */
   public WorkException getException()
   {
      return exception;
   }

   /**
    * set callback reference
    * @param callbackCount complete count
    */
   public void setCallbackCount(CallbackCount callbackCount)
   {
      this.callbackCount = callbackCount;
   }
}
