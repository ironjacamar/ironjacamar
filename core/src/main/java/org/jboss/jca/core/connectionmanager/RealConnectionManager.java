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

import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.transaction.JTATransactionChecker;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.tm.TransactionTimeoutConfiguration;

/**
 * Real connection manager contract.
 * <p>
 * <ul>
 *    <li>Responsible for managing cached connections over transactional 
 *    components via {@link ConnectionCacheListener}</li>
 *    <li>Responsible for managing connection instances using event listener 
 *    via {@link ConnectionListenerFactory}</li>
 *    <li>Responsible for managing transaction operations via 
 *    {@link TransactionTimeoutConfiguration} and {@link JTATransactionChecker}</li>.
 * </ul>
 * </p> 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Rev$ $Date$
 *
 */
public interface RealConnectionManager extends
   ConnectionCacheListener, 
   ConnectionListenerFactory, 
   TransactionTimeoutConfiguration, 
   JTATransactionChecker

{
   /**
    * Gets connection handle instance.
    * @param mcf managed connection factory
    * @param cri connection request info
    * @return ne wconnection
    * @throws ResourceException for exception
    */
   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cri) throws ResourceException;
}
