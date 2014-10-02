/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.api.workmanager.DistributableContext;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.workmanager.WorkManagerCoordinator;

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

/**
 * The base implementation of the cloneable bootstrap context
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class BaseCloneableBootstrapContext implements CloneableBootstrapContext
{
   /** Transaction synchronization registry */
   private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

   /** Work Manager */
   private WorkManager workManager;

   /** Work Manager name */
   private String workManagerName;

   /** XATerminator */
   private XATerminator xaTerminator;

   /** Supported contexts */
   private Set<Class> supportedContexts;

   /** Timers */
   private List<Timer> timers;

   /** The id */
   private String id;

   /** The name */
   private String name;

   /**
    * Constructor
    */
   public BaseCloneableBootstrapContext()
   {
      this.transactionSynchronizationRegistry = null;
      this.workManager = null;
      this.workManagerName = null;
      this.xaTerminator = null;
      this.supportedContexts = new HashSet<Class>(4);

      this.supportedContexts.add(HintsContext.class);
      this.supportedContexts.add(SecurityContext.class);
      this.supportedContexts.add(TransactionContext.class);
      this.supportedContexts.add(DistributableContext.class);

      this.timers = null;

      this.id = null;
      this.name = null;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public void setId(String v)
   {
      id = v;
   }

   /**
    * Get the name of the bootstrap context
    * @return The value
    */
   public String getName()
   {
      return name;
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
      if (workManager == null)
         workManager = WorkManagerCoordinator.getInstance().createWorkManager(id, workManagerName);

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
    * Get the work manager name
    * @return The value
    */
   public String getWorkManagerName()
   {
      return workManagerName;
   }

   /**
    * Set the work manager name
    * @param wmn The value
    */
   public void setWorkManagerName(String wmn)
   {
      this.workManagerName = wmn;
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
      StringBuilder sb = new StringBuilder();

      if (name != null)
      {
         sb.append(name);
         if (id != null)
            sb.append(":");
      }

      if (id != null)
         sb.append(id);

      Timer t = null;

      if (sb.length() > 0)
      {
         t = new Timer(sb.toString(), true);
      }
      else
      {
         t = new Timer(true);
      }

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
      bcbc.setXATerminator(getXATerminator());
      bcbc.setName(getName());
      bcbc.setWorkManagerName(getWorkManagerName());

      return bcbc;
   }
}
