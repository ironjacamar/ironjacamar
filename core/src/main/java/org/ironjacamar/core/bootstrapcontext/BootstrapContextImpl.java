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
package org.ironjacamar.core.bootstrapcontext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * Basic BootstrapContext implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class BootstrapContextImpl implements BootstrapContext
{
   /** Work Manager */
   private WorkManager workManager;

   /** Transaction synchronization registry */
   private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

   /** XATerminator */
   private XATerminator xaTerminator;

   /** Supported contexts */
   private Set<Class> supportedContexts;

   /** Timers */
   private List<Timer> timers;

   /**
    * Constructor
    * @param wm The WorkManager
    * @param tsr The TransactionSynchronizationRegistry
    * @param terminator The XATerminator
    */
   public BootstrapContextImpl(WorkManager wm,
                               TransactionSynchronizationRegistry tsr,
                               XATerminator terminator)
   {
      this.workManager = wm;
      this.transactionSynchronizationRegistry = tsr;
      this.xaTerminator = terminator;
      this.supportedContexts = new HashSet<Class>(3);

      this.supportedContexts.add(HintsContext.class);
      this.supportedContexts.add(SecurityContext.class);
      this.supportedContexts.add(TransactionContext.class);

      this.timers = null;
   }

   /**
    * {@inheritDoc}
    */
   public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      return transactionSynchronizationRegistry;
   }

   /**
    * {@inheritDoc}
    */
   public WorkManager getWorkManager()
   {
      return workManager;
   }

   /**
    * {@inheritDoc}
    */
   public XATerminator getXATerminator()
   {
      return xaTerminator;
   }

   /**
    * Create a timer
    * @return The timer
    */
   public Timer createTimer()
   {
      Timer t = new Timer(true);

      if (timers == null)
         timers = new ArrayList<Timer>();

      timers.add(t);

      return t;
   }

   /**
    * Is the work context supported ?
    * @param workContextClass The work context class
    * @return True if supported; otherwise false
    */
   public boolean isContextSupported(Class<? extends WorkContext> workContextClass)
   {
      if (workContextClass == null)
         return false;

      return supportedContexts.contains(workContextClass);
   }

   /**
    * Shutdown
    */
   public void shutdown()
   {
      if (timers != null)
      {
         for (Timer t : timers)
         {
            t.cancel();
            t.purge();
         }
      }
   }
}
