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

import org.ironjacamar.core.api.connectionmanager.ConnectionManagerConfiguration;
import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

/**
 * The XATransaction connection manager
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XATransactionConnectionManager extends AbstractTransactionalConnectionManager
{
   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param ccm The cached connection manager
    * @param cmc The connection manager configuration
    * @param ti The transaction integration
    */
   public XATransactionConnectionManager(ManagedConnectionFactory mcf,
                                         CachedConnectionManager ccm,
                                         ConnectionManagerConfiguration cmc,
                                         TransactionIntegration ti)
   {
      super(mcf, ccm, cmc, ti);
   }

   /**
    * {@inheritDoc}
    */
   public TransactionSupportLevel getTransactionSupport()
   {
      return TransactionSupportLevel.XATransaction;
   }
}
