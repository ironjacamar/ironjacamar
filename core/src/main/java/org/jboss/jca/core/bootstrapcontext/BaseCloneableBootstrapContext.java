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

package org.jboss.jca.core.bootstrapcontext;

import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.api.workmanager.WorkManager;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkContext;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * The base implementation of the cloneable bootstrap context
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class BaseCloneableBootstrapContext implements CloneableBootstrapContext
{
   /** Transaction synchronization registry */
   private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

   /** Work Manager */
   private WorkManager workManager;

   /** XATerminator */
   private XATerminator xaTerminator;

   /** Supported contexts */
   private Set<Class> supportedContexts;

   /**
    * Constructor
    */
   public BaseCloneableBootstrapContext()
   {
      this.transactionSynchronizationRegistry = null;
      this.workManager = null;
      this.xaTerminator = null;
      this.supportedContexts = new HashSet<Class>(3);

      this.supportedContexts.add(HintsContext.class);
      this.supportedContexts.add(SecurityContext.class);
      this.supportedContexts.add(TransactionContext.class);
   }

   /**
    * Set the resource adapter
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      if (workManager != null)
         workManager.setResourceAdapter(ra);
   }

   /**
    * Get the transaction synchronization registry
    * @return The handle
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

   /**
    * Get the work manager
    * @return The handle
    */
   public WorkManager getWorkManager()
   {
      return workManager;
   }

   /**
    * Set the work manager
    * @param wm The handle
    */
   public void setWorkManager(WorkManager wm)
   {
      this.workManager = wm;
   }

   /**
    * Get the XA terminator
    * @return The handle
    */
   public XATerminator getXATerminator()
   {
      return xaTerminator;
   }

   /**
    * Set the XA terminator
    * @param xt The handle
    */
   public void setXATerminator(XATerminator xt)
   {
      this.xaTerminator = xt;
   }

   /**
    * Create a timer
    * @return The timer
    */
   public Timer createTimer()
   {
      return new Timer(true);
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
    * Clone the BootstrapContext implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *  
    */
   public CloneableBootstrapContext clone() throws CloneNotSupportedException
   {
      BaseCloneableBootstrapContext bcbc = (BaseCloneableBootstrapContext)super.clone();
      bcbc.setTransactionSynchronizationRegistry(getTransactionSynchronizationRegistry());
      bcbc.setWorkManager(getWorkManager().clone());
      bcbc.setXATerminator(getXATerminator());

      return bcbc;
   }
}
