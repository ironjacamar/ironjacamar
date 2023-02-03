/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

/**
 * ResourceAdapter implementation for JDBC
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JDBCResourceAdapter implements ResourceAdapter
{
   /** BootstrapContext */
   private BootstrapContext bc;

   /**
    * Default constructor
    */
   public JDBCResourceAdapter()
   {
      this.bc = null;
   }

   /**
    * {@inheritDoc}
    */
   public void endpointActivation(MessageEndpointFactory endpointFactory,
                                  ActivationSpec spec)
      throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                    ActivationSpec spec)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      this.bc = ctx;
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
   }

   /**
    * {@inheritDoc}
    */
   public XAResource[] getXAResources(ActivationSpec[] specs)
      throws ResourceException
   {
      return null;
   }

   /**
    * Get the TSR
    * @return The instance
    */
   TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      // Non-JTA datasources doesn't need a BootstrapContext instance
      if (bc != null)
      {
         return bc.getTransactionSynchronizationRegistry();
      }

      return null;
   }

   /** 
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return 17;
   }

   /** 
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      if (other == null)
         return false;

      if (other == this)
         return true;

      if (!(other instanceof JDBCResourceAdapter))
         return false;

      JDBCResourceAdapter obj = (JDBCResourceAdapter)other;
      boolean result = bc == obj.bc;
      return result;
   }
}
