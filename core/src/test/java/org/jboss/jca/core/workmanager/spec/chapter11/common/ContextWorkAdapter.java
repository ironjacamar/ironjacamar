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
package org.jboss.jca.core.workmanager.spec.chapter11.common;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkAdapter;
import javax.resource.spi.work.WorkEvent;

import static org.junit.Assert.*;

/**
 * ContextWorkAdapter
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
public class ContextWorkAdapter extends WorkAdapter
{
   /**timestamp for check*/
   private long timeAccepted = 0;

   /**timestamp for check*/
   private long timeStarted = 0;

   /**timestamp for check*/
   private long timeRejected = 0;

   /**timestamp for check*/
   private long timeCompleted = 0;
   
   /**
    * start string buffer
    */
   private StringBuffer start = new StringBuffer();
   /**
    * done string buffer
    */
   private StringBuffer done = new StringBuffer();
   /**
    * reject string buffer
    */
   private StringBuffer reject = new StringBuffer();


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
      timeAccepted = System.currentTimeMillis();
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
      timeStarted = System.currentTimeMillis();
      super.workStarted(e);
      Work work = e.getWork();
      if (work instanceof NestProviderWork)
      {
         NestProviderWork nw = (NestProviderWork) work;
         start.append(nw.getName());
      }

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
      timeRejected = System.currentTimeMillis();
      super.workRejected(e);
      Work work = e.getWork();
      if (work instanceof NestProviderWork)
      {
         NestProviderWork nw = (NestProviderWork) work;
         reject.append(nw.getName());
      }

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
      timeCompleted = System.currentTimeMillis();
      super.workCompleted(e);
      Work work = e.getWork();
      if (work instanceof NestProviderWork)
      {
         NestProviderWork nw = (NestProviderWork) work;
         done.append(nw.getName());
      }

   }

   /**
    * getter
    * @return timestamp
    */
   public long getTimeAccepted()
   {
      return timeAccepted;
   }

   /**
    * getter
    * @return timestamp
    */
   public long getTimeStarted()
   {
      return timeStarted;
   }

   /**
    * getter
    * @return timestamp
    */
   public long getTimeRejected()
   {
      return timeRejected;
   }

   /**
    * getter
    * @return timestamp
    */
   public long getTimeCompleted()
   {
      return timeCompleted;
   }
   
   /**
    * getter
    * @return start buffer 
    */
   public String getStart()
   {
      return start.toString();
   }
   
   /**
    * getter
    * @return done buffer 
    */
   public String getDone()
   {
      return done.toString();
   }
   
   /**
    * getter
    * @return done buffer 
    */
   public String getReject()
   {
      return reject.toString();
   }

}
