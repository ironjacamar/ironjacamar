/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.AbstractPrefillPool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * Single pool implementation.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class OnePool extends AbstractPrefillPool
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, OnePool.class.getName());

   /**
    * Creates a new instance.
    * 
    * @param mcf managed connection factory
    * @param pc pool configuration
    * @param noTxSeparatePools notx seperate pool
    * @param sharable Are the connections sharable
    */
   public OnePool(final ManagedConnectionFactory mcf, final PoolConfiguration pc,
                  final boolean noTxSeparatePools, final boolean sharable)
   {
      super(mcf, pc, noTxSeparatePools, sharable);
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
    * {@inheritDoc}
    */
   public void emptySubPool(ManagedConnectionPool pool)
   {
      // No-operation
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection()
   {
      return internalTestConnection(null);
   }

   /**
    * {@inheritDoc}
    */
   public CoreLogger getLogger()
   {
      return log;
   }
}
