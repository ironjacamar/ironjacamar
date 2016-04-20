/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.bootstrapcontext;

import org.ironjacamar.core.api.bootstrapcontext.BootstrapContext;
import org.ironjacamar.core.api.workmanager.WorkManager;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.XATerminator;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.validation.ValidatorFactory;

/**
 * The cloneable bootstrap context interface which defines
 * IronJacamar private API for all BootstrapContext implementations
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface CloneableBootstrapContext extends Cloneable, BootstrapContext
{
   /**
    * Get the id of the bootstrap context
    * @return The value
    */
   String getId();

   /**
    * Set the id of the bootstrap context
    * @param v The value
    */
   void setId(String v);

   /**
    * Set the resource adapter
    * @param ra The handle
    */
   void setResourceAdapter(ResourceAdapter ra);

   /**
    * Set the transaction synchronization registry
    * @param tsr The handle
    */
   void setTransactionSynchronizationRegistry(TransactionSynchronizationRegistry tsr);

   /**
    * Set the work manager - internal use only
    * @param wm The handle
    */
   void setWorkManager(WorkManager wm);

   /**
    * Get the name of the work manager
    * @return The value
    */
   String getWorkManagerName();

   /**
   /**
    * Set the name of the work manager
    * @param wmn The name
    */
   void setWorkManagerName(String wmn);

   /**
    * Set the XA terminator
    * @param xt The handle
    */
   void setXATerminator(XATerminator xt);


   /**
    * Set validator factory
    * @param validatorFactory the validatorFactory
    */
   void setValidatorFactory(ValidatorFactory validatorFactory);

   /**
    * Shutdown
    */
   void shutdown();

   /**
    * Clone the BootstrapContext implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *
    */
   CloneableBootstrapContext clone() throws CloneNotSupportedException;

}
