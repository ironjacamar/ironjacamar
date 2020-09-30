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

package org.jboss.jca.core.api.bootstrap;

import org.jboss.jca.core.api.workmanager.WorkManager;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.XATerminator;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * The cloneable bootstrap context interface which defines
 * the contract for all BootstrapContext implementations
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface CloneableBootstrapContext extends Cloneable, BootstrapContext
{
   /**
    * Get the id of the bootstrap context
    * @return The value
    */
   public String getId();

   /**
    * Set the id of the bootstrap context
    * @param v The value
    */
   public void setId(String v);

   /**
    * Get the name of the bootstrap context
    * @return The value
    */
   public String getName();

   /**
    * Set the resource adapter
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra);

   /**
    * Set the transaction synchronization registry
    * @param tsr The handle
    */
   public void setTransactionSynchronizationRegistry(TransactionSynchronizationRegistry tsr);

   /**
    * Set the work manager - internal use only
    * @param wm The handle
    */
   public void setWorkManager(WorkManager wm);

   /**
    * Get the name of the work manager
    * @return The value
    */
   public String getWorkManagerName();

   /**
   /**
    * Set the name of the work manager
    * @param wmn The name
    */
   public void setWorkManagerName(String wmn);

   /**
    * Set the XA terminator
    * @param xt The handle
    */
   public void setXATerminator(XATerminator xt);

   /**
    * Shutdown
    */
   public void shutdown();

   /**
    * Clone the BootstrapContext implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *  
    */
   public CloneableBootstrapContext clone() throws CloneNotSupportedException;
}
