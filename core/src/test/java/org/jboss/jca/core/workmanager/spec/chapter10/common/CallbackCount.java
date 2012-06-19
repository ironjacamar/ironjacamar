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
package org.jboss.jca.core.workmanager.spec.chapter10.common;

/**
 * class for count called times
 */
public class CallbackCount
{
   /** count accept times */
   private int acceptCount;
   /** count start times */
   private int startCount;
   /** count completed times */
   private int completedCount;
   /** count rejected times */
   private int rejectedCount;
   
   /**
    * set accept count
    * @param acceptCount accept count
    */
   public void setAcceptCount(int acceptCount)
   {
      this.acceptCount = acceptCount;
   }
   /**
    * get accept count
    * @return int accept count
    */
   public int getAcceptCount()
   {
      return acceptCount;
   }
   /**
    * set start count
    * @param startCount start count
    */
   public void setStartCount(int startCount)
   {
      this.startCount = startCount;
   }
   /**
    * get start count
    * @return int start count
    */
   public int getStartCount()
   {
      return startCount;
   }
   /**
    * set complete count
    * @param completedCount complete count
    */
   public void setCompletedCount(int completedCount)
   {
      this.completedCount = completedCount;
   }
   /**
    * get complete count
    * @return int complete count
    */
   public int getCompletedCount()
   {
      return completedCount;
   }
   /**
    * set rejected count
    * @param rejectCount rejected count
    */
   public void setRejectedCount(int rejectCount)
   {
      this.rejectedCount = rejectCount;
   }
   /**
    * get rejected count
    * @return int rejected count
    */
   public int getRejectedCount()
   {
      return rejectedCount;
   }
}
