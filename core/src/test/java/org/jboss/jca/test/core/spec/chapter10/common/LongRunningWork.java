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

import javax.resource.spi.work.Work;

/**
 * LongRunningWork
 * @version $Revision: $
 */
public class LongRunningWork implements Work
{
   private boolean wasReleased;
   private long releaseThread;
   
   /**
    * Constructor.
    */
   public LongRunningWork()
   {
   }
   
   /**
    * release method
    */
   public void release()
   {
      wasReleased = true;
      releaseThread = Thread.currentThread().getId();
   }

   /**
    * run method
    */
   public void run()
   {
   }
   
   /**
    * run method
    * @return boolean wasReleased
    */
   public boolean getWasReleased()
   {
      return wasReleased;
   }
   
   /**
    * get releasing thread
    * @return long thread
    */
   public long getReleaseThread()
   {
      return releaseThread;
   }
}

