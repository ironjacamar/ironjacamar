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

import org.ironjacamar.core.api.workmanager.WorkManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkContext;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.validation.ValidatorFactory;

/**
 * Basic BootstrapContext implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class BootstrapContextImpl implements CloneableBootstrapContext
{
   /** Work Manager */
   private WorkManager workManager;

   /** Work Manager name */
   private String workManagerName;

   /** Transaction synchronization registry */
   private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

   /** XATerminator */
   private XATerminator xaTerminator;

   /** Supported contexts */
   private Set<Class> supportedContexts;

   /** Timers */
   private List<Timer> timers;

   /** the name */
   private String name;

   /**the id */
   private String id;

   /** the ValidatorFactory */
   private ValidatorFactory validatorFactory;

   /**
    * Constructor
    * @param wm The WorkManager
    * @param tsr The TransactionSynchronizationRegistry
    * @param terminator The XATerminator
    * @param validatorFactory the ValidatorFactory
    */
   public BootstrapContextImpl(WorkManager wm,
                               TransactionSynchronizationRegistry tsr,
                               XATerminator terminator,
                               ValidatorFactory validatorFactory)
   {
      this.workManager = wm;
      this.transactionSynchronizationRegistry = tsr;
      this.xaTerminator = terminator;
      this.supportedContexts = new HashSet<Class>(3);
      this.validatorFactory = validatorFactory;
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
    * Set the transaction synchronization registry
    * @param tsr The handle
    */
   public void setTransactionSynchronizationRegistry(TransactionSynchronizationRegistry tsr)
   {
      this.transactionSynchronizationRegistry = tsr;
   }

   @Override
   public void setWorkManager(org.ironjacamar.core.api.workmanager.WorkManager wm)
   {
      this.workManager = wm;
   }

   @Override
   public String getWorkManagerName()
   {
      return workManagerName;
   }

   @Override
   public void setWorkManagerName(String wmn)
   {
      this.workManagerName = wmn;
   }

   @Override
   public void setXATerminator(XATerminator xt)
   {
      this.xaTerminator = xt;
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

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public void setId(String v)
   {
      this.id = v;
   }

   /**
    * Get the name of the bootstrap context
    * @return The value
    */
   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public void setResourceAdapter(ResourceAdapter ra)
   {
      if (workManager != null)
         workManager.setResourceAdapter(ra);
   }

   /**
    * Set the name of the bootstrap context
    * @param v The value
    */
   public void setName(String v)
   {
      name = v;
   }

   /**
    * Get the validator factory
    * @return The factory
    */
   public ValidatorFactory getValidatorFactory()
   {
      return validatorFactory;
   }

   /**
    * Set validator factory
    * @param validatorFactory the validatorFactory
    */
   public void setValidatorFactory(ValidatorFactory validatorFactory)
   {
      this.validatorFactory = validatorFactory;
   }

   /**
    * Clone the BootstrapContext implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *
    */
   public BootstrapContextImpl clone() throws CloneNotSupportedException
   {
      BootstrapContextImpl bcbc = (BootstrapContextImpl)super.clone();
      bcbc.setTransactionSynchronizationRegistry(getTransactionSynchronizationRegistry());
      bcbc.setXATerminator(getXATerminator());
      bcbc.setName(getName());
      bcbc.setWorkManagerName(getWorkManagerName());
      bcbc.setValidatorFactory(getValidatorFactory());

      return bcbc;
   }

}
