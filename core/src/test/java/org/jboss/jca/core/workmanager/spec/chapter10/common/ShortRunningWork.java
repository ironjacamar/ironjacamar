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

import javax.resource.spi.work.Work;

/**
 * ShortRunningWork
 * @version $Revision: $
 */
public class ShortRunningWork implements Work
{
   private boolean wasReleased;
   private boolean callRun;
   private boolean throwTestWorkException;
   
   /**
    * Constructor.
    */
   public ShortRunningWork()
   {
   }
   
   /**
    * release method
    */
   public void release()
   {
      wasReleased = true;
   }

   /**
    * run method
    */
   public void run()
   {
      if (throwTestWorkException)
         throw new TestWorkException();
      callRun = true;
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
    * call run.
    * @return boolean
    */   
   public boolean hasCallRun()
   {
      return callRun;
   }
   
   /**
    * setWorkManager.
    * @param throwWorkException if throw TestWorkException
    */
   public void setThrowWorkException(boolean throwWorkException)
   {
      this.throwTestWorkException = throwWorkException;
   }
}

