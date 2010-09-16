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

import org.jboss.jca.core.api.connectionmanager.transaction.JTATransactionChecker;

import org.jboss.tm.TransactionTimeoutConfiguration;

/**
 * Internal connection manager contract for transactional contexts.
 * <p>
 * <ul>
 *    <li>Responsible for managing transaction operations via 
 *    {@link TransactionTimeoutConfiguration} and {@link JTATransactionChecker}</li>.
 * </ul>
 * </p> 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a> 
 */
public interface TxConnectionManager extends ConnectionManager,
                                             TransactionTimeoutConfiguration, 
                                             JTATransactionChecker
{
   /**
    * Get the interleaving status
    * @return True if interleaving; otherwise false
    */
   public boolean isInterleaving();

   /**
    * Get the local transaction status
    * @return True if local transactions; false if XA
    */
   public boolean isLocalTransactions();

   /**
    * Get the XA resource transaction time out in seconds
    * @return The value
    */
   public int getXAResourceTimeout();

   /**
    * Get the IsSameRMOverride status
    * @return True if isSameRM is overridden; otherwise false
    */
   public boolean getIsSameRMOverride();

   /**
    * Get the wrap XAResource status
    * @return True if XAResource instances are wrapped; otherwise false
    */
   public boolean getWrapXAResource();

   /**
    * Get the PadXid status
    * @return True if Xids are padded; otherwise false
    */
   public boolean getPadXid();
}
