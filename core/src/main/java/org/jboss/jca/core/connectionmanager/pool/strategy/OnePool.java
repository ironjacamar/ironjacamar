/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.strategy;

import org.jboss.jca.core.connectionmanager.pool.AbstractPrefillPool;
import org.jboss.jca.core.connectionmanager.pool.InternalManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.PoolParams;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Single pool implementation.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class OnePool extends AbstractPrefillPool
{
   /**
    * Creates a new instance.
    * 
    * @param mcf managed connection factory
    * @param poolParams pool parameter
    * @param noTxSeparatePools notx seperate pool
    */
   public OnePool(final ManagedConnectionFactory mcf, final PoolParams poolParams, final boolean noTxSeparatePools)
   {
      super(mcf, poolParams, noTxSeparatePools);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Object getKey(final Subject subject, final ConnectionRequestInfo cri, boolean separateNoTx)
   {
      if (separateNoTx)
      {
         return Boolean.TRUE;
      }
      else
      {
         return Boolean.FALSE;
      }
   }

   /**
    * Prefill.
    * 
    * @param sub subject instance
    */
   public void prefill(Subject sub)
   {
      if (log.isDebugEnabled())
         log.debug("Attempting to prefill pool" + getClass());

      try
      {
         // WMP is this really the best way to do this?
         getSubPool(getKey(null, null, false), null, null);
      }
      catch (ResourceException e)
      {
         log.error("Prefill failed for pool instance " + getClass(), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void emptySubPool(InternalManagedConnectionPool pool)
   {
      // No-operation
   }
}
