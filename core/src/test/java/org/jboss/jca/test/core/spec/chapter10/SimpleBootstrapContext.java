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

import java.util.Timer;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkManager;

import javax.transaction.TransactionSynchronizationRegistry;
/**
 * SimpleBootstrapContext.

 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class SimpleBootstrapContext implements BootstrapContext
{
   private WorkManager workManager;
   private XATerminator xATerminator;

   /**
    * Creates a new <code>java.util.Timer</code> instance. 
    *
    * @throws UnavailableException UnavailableException
    *
    * @return a new <code>Timer</code> instance.
    */
   public Timer createTimer() throws UnavailableException
   {
      return null;
   }

   /**
    * Provides a handle to a <code>TransactionSynchronization</code> instance.
    *
    * @return a <code>TransactionSynchronizationRegistry</code> instance.
    */
   public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      return null;
   }

   /**
    * Provides a handle to a <code>WorkManager</code> instance.
    *
    * @return a <code>WorkManager</code> instance.
    */
   public WorkManager getWorkManager()
   {
      return workManager;
   }

   /**
    * Provides a handle to a <code>WorkManager</code> instance.
    *
    * @param workManager instance
    */
   public void setWorkManager(WorkManager workManager)
   {
      this.workManager = workManager;
   }
   
   /**
    * Provides a handle to a <code>XATerminator</code> instance.
    * 
    * @return a <code>XATerminator</code> instance.
    */
   public XATerminator getXATerminator()
   {
      return xATerminator;
   }

   /**
    * Provides a handle to a <code>XATerminator</code> instance.
    * 
    * @param terminator instance
    */
   public void setXATerminator(XATerminator terminator)
   {
      xATerminator = terminator;
   }

   /**
    * A resource adapter can check an application server’s support 
    * for a particular InflowContext type through this method. 
    *
    * @param inflowContextClass inflowContextClass
    * @return true if the <code>inflowContextClass</code> is supported
    * by the application server. 
    */
   public boolean isContextSupported(
         Class<? extends WorkContext> inflowContextClass)
   {
      return false;
   }

}
