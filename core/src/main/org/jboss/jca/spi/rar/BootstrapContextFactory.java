/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.jca.spi.rar;

import java.util.Timer;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

/**
 * A BootstrapContextFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class BootstrapContextFactory
{
   /** The bootstrap context */
   protected JBossBootstrapContext bootstrapContext = new JBossBootstrapContext();

   /** The work manager */
   protected WorkManager workManager;
   
   /** The XA terminator */
   protected XATerminator xaTerminator;
   
   /** The timer factory */
   protected TimerFactory timerFactory;

   /**
    * Get the bootstrap context instance
    * 
    * @return the instance
    */
   public BootstrapContext getBootstrapContext()
   {
      return bootstrapContext;
   }

   /**
    * Get the workManager.
    * 
    * @return the workManager.
    */
   public WorkManager getWorkManager()
   {
      return workManager;
   }

   /**
    * Set the workManager.
    * 
    * @param workManager The workManager to set.
    */
   public void setWorkManager(WorkManager workManager)
   {
      this.workManager = workManager;
   }

   /**
    * Get the xaTerminator.
    * 
    * @return the xaTerminator.
    */
   public XATerminator getXaTerminator()
   {
      return xaTerminator;
   }

   /**
    * Set the xaTerminator.
    * 
    * @param xaTerminator The xaTerminator to set.
    */
   public void setXaTerminator(XATerminator xaTerminator)
   {
      this.xaTerminator = xaTerminator;
   }
   
   /**
    * Get the timerFactory.
    * 
    * @return the timerFactory.
    */
   public TimerFactory getTimerFactory()
   {
      return timerFactory;
   }

   /**
    * Set the timerFactory.
    * 
    * @param timerFactory The timerFactory to set.
    */
   public void setTimerFactory(TimerFactory timerFactory)
   {
      this.timerFactory = timerFactory;
   }

   /**
    * A JBossBootstrapContext.
    */
   private class JBossBootstrapContext implements BootstrapContext
   {
      public Timer createTimer() throws UnavailableException
      {
         if (timerFactory == null)
            throw new IllegalStateException("No TimerFactory configured");
         return timerFactory.createTimer();
      }

      public WorkManager getWorkManager()
      {
         if (workManager == null)
            throw new IllegalStateException("No WorkManager configured");
         return workManager;
      }

      public XATerminator getXATerminator()
      {
         if (xaTerminator == null)
            throw new IllegalStateException("No XATerminator configured");
         return xaTerminator;
      }
   }
}
