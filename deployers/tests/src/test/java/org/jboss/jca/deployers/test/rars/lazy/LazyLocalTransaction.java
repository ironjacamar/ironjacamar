/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.test.rars.lazy;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransaction;

import org.jboss.logging.Logger;

/**
 * Lazy local transaction
 */
public class LazyLocalTransaction implements LocalTransaction
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyLocalTransaction.class);

   /** The managed connection */
   private LazyManagedConnection mc;

   /**
    * Constructor
    * @param mc The managed connection
    */
   public LazyLocalTransaction(LazyManagedConnection mc)
   {
      this.mc = mc;
   }

   /**
    * {@inheritDoc}
    */
   public void begin() throws ResourceException
   {
      log.trace("begin");
      mc.setEnlisted(true);
   }

   /**
    * {@inheritDoc}
    */
   public void commit() throws ResourceException
   {
      log.trace("commit");
      mc.setEnlisted(false);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws ResourceException
   {
      log.trace("rollback");
      mc.setEnlisted(false);
   }
}
