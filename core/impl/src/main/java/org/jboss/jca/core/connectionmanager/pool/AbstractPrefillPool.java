/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.api.PrefillPool;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Abstract pool implementation which can be prefilled.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractPrefillPool extends AbstractPool implements PrefillPool
{
   /** Should prefill be performed */
   private boolean shouldPrefill = false;

   /**
    * Create a new prefill pool.
    * 
    * @param mcf the managed connection factory
    * @param pc the pool configuration
    * @param noTxSeparatePools noTxSeparatePool
    * @param sharable Are the connections sharable
    * @param mcp mcp
    */
   protected AbstractPrefillPool(final ManagedConnectionFactory mcf, final PoolConfiguration pc,
                                 final boolean noTxSeparatePools, final boolean sharable, final String mcp)
   {
      super(mcf, pc, noTxSeparatePools, sharable, mcp);
      this.shouldPrefill = pc.isPrefill() || pc.isStrictMin();
   }

   /**
    * {@inheritDoc}
    */   
   public void prefill(Subject subject, ConnectionRequestInfo cri, boolean noTxnSeperatePool)
   {
      if (shouldPrefill)
      {
         if (log.isDebugEnabled())
            log.debug("Attempting to prefill pool: " + getName());

         try
         {
            //Get pool key
            Object key = getKey(subject, cri, noTxnSeperatePool);
            
            //Get pool automatically initializes pool
            getManagedConnectionPool(key, subject, cri);
         }
         catch (Throwable t)
         {
            //No real need to throw here being that pool remains in the same state as before.
            log.error("Unable to prefill pool: " + getName(), t);
         }
      }
   }

   /**
    * Is prefill
    * @return The value
    */
   @Override
   protected boolean isPrefill()
   {
      return getPoolConfiguration().getMinSize() > 0;
   }
}
