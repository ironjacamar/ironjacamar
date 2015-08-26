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


package org.ironjacamar.core.api.workmanager;


import org.ironjacamar.core.spi.graceful.GracefulShutdown;
import org.ironjacamar.core.spi.security.Callback;
import org.ironjacamar.core.spi.security.SecurityIntegration;
import org.ironjacamar.core.spi.transaction.xa.XATerminator;

import javax.resource.spi.ResourceAdapter;

import org.jboss.threads.BlockingExecutor;

/**
 * The JBoss specific work manager interface
 */
public interface WorkManager extends javax.resource.spi.work.WorkManager, GracefulShutdown, Cloneable
{
   /**
    * Get the unique id of the work manager
    * @return The value
    */
   public String getId();

   /**
    * Set the id of the work manager
    * @param v The value
    */
   public void setId(String v);

   /**
    * Get the name of the work manager
    * @return The value
    */
   public String getName();

   /**
    * Set the name of the work manager
    * @param v The value
    */
   public void setName(String v);

   /**
    * Set the resource adapter
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra);

   /**
    * Retrieve the executor for short running tasks
    * @return The executor
    */
   public StatisticsExecutor getShortRunningThreadPool();

   /**
    * Set the executor for short running tasks
    * @param executor The executor
    */
   public void setShortRunningThreadPool(BlockingExecutor executor);

   /**
    * Retrieve the executor for long running tasks
    * @return The executor
    */
   public StatisticsExecutor getLongRunningThreadPool();

   /**
    * Set the executor for long running tasks
    * @param executor The executor
    */
   public void setLongRunningThreadPool(BlockingExecutor executor);

   /**
    * Get the XATerminator
    * @return The XA terminator
    */
   public XATerminator getXATerminator();

   /**
    * Set the XATerminator
    * @param xaTerminator The XA terminator
    */
   public void setXATerminator(XATerminator xaTerminator);

   /**
    * Is spec compliant
    * @return True if spec compliant; otherwise false
    */
   public boolean isSpecCompliant();

   /**
    * Set spec compliant flag
    * @param v The value
    */
   public void setSpecCompliant(boolean v);

   /**
    * Is statistics enabled
    * @return True if enabled; otherwise false
    */
   public boolean isStatisticsEnabled();

   /**
    * Set the statistics enabled flag
    * @param v The value
    */
   public void setStatisticsEnabled(boolean v);

   /**
    * Get the callback security module
    * @return The value
    */
   public Callback getCallbackSecurity();

   /**
    * Set callback security module
    * @param v The value
    */
   public void setCallbackSecurity(Callback v);

   /**
    * Get the security integration module
    * @return The value
    */
   public SecurityIntegration getSecurityIntegration();

   /**
    * Set security integration module
    * @param v The value
    */
   public void setSecurityIntegration(SecurityIntegration v);

   /**
    * Get the statistics
    * @return The value
    */
   public WorkManagerStatistics getStatistics();

   /**
    * Clone the WorkManager implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *
    */
   public WorkManager clone() throws CloneNotSupportedException;
}
