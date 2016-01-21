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

package org.ironjacamar.core.connectionmanager;

import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.core.api.connectionmanager.ConnectionManagerConfiguration;
import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;

import javax.resource.spi.ManagedConnectionFactory;

/**
 * The connection manager factory
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnectionManagerFactory
{
   /**
    * Constructor
    */
   private ConnectionManagerFactory()
   {
   }

   /**
    * Create a connection manager
    * @param tse The transaction support level
    * @param mcf The managed connection factory
    * @param ccm The cached connection manager
    * @param cmc The connection manager configuration
    * @param ti The transaction integration
    * @return The connection manager
    */
   public static ConnectionManager createConnectionManager(TransactionSupportEnum tse,
                                                           ManagedConnectionFactory mcf,
                                                           CachedConnectionManager ccm,
                                                           ConnectionManagerConfiguration cmc,
                                                           TransactionIntegration ti)
   {
      if (tse == TransactionSupportEnum.NoTransaction)
      {
         return new NoTransactionConnectionManager(mcf, ccm, cmc);
      }
      else if (tse == TransactionSupportEnum.LocalTransaction)
      {
         return new LocalTransactionConnectionManager(mcf, ccm, cmc, ti);
      }
      else
      {
         return new XATransactionConnectionManager(mcf, ccm, cmc, ti);
      }
   }
}
