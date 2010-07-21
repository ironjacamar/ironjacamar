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

package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.notx.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;

import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.TransactionManager;

/**
 * The connection manager factory. 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ConnectionManagerFactory
{
   /**
    * Constructor
    */
   public ConnectionManagerFactory()
   {
   }

   /**
    * Create a connection manager
    * @param tsl The transaction support level
    * @param pool The pool for the connection manager
    * @param tm The transaction manager
    * @return The connection manager instance
    */
   public ConnectionManager create(final TransactionSupportLevel tsl,
                                   final Pool pool,
                                   final TransactionManager tm)
   {
      if (tsl == null)
         throw new IllegalArgumentException("TransactionSupportLevel is null");

      if (pool == null)
         throw new IllegalArgumentException("Pool is null");

      ConnectionManager cm = null;

      switch (tsl)
      {
         case NoTransaction:
            cm = new NoTxConnectionManager();
            break;

         case LocalTransaction:
            if (tm == null)
               throw new IllegalStateException("TransactionManager is null");

            cm = new TxConnectionManager(tm, true);
            break;

         case XATransaction:
            if (tm == null)
               throw new IllegalStateException("TransactionManager is null");

            cm = new TxConnectionManager(tm, false);
            break;

         default:
            throw new IllegalArgumentException("Unknown transaction support level " + tsl);
      }

      pool.setConnectionListenerFactory(cm);
      cm.setPool(pool);

      CachedConnectionManager ccm = new CachedConnectionManager(tm);
      cm.setCachedConnectionManager(ccm);

      return cm;
   }
}
