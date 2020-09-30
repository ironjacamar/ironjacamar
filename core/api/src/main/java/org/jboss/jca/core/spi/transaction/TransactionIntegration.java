/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.spi.transaction;

import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecoveryRegistry;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry;
import org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper;
import org.jboss.jca.core.spi.transaction.xa.XATerminator;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

/**
 * This interface defines the contract for a transaction manager integration
 * with the IronJacamar container.
 *
 * The methods allows the IronJacamar container to create or get transaction
 * related information from the transaction manager implementation and allows
 * the implementation of this information to be specific for each instance of
 * this interface.
 *
 * If a feature isn't supported by the transaction manager a <code>null</code>
 * value should be returned. That way it is disabled in the IronJacamar container. 
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface TransactionIntegration
{
   /**
    * Get the transaction manager
    * @return The value
    */
   public TransactionManager getTransactionManager();

   /**
    * Get the transaction synchronization registry
    * @return The value
    */
   public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry();

   /**
    * Get the user transaction registry
    * @return The value
    */
   public UserTransactionRegistry getUserTransactionRegistry();

   /**
    * Get the recovery registry
    * @return The value
    */
   public XAResourceRecoveryRegistry getRecoveryRegistry();

   /**
    * Get the XATerminator
    * @return The value
    */
   public XATerminator getXATerminator();

   /**
    * Create an XAResourceRecovery instance
    *
    * @param rar The resource adapter
    * @param as The activation spec
    * @param productName The product name
    * @param productVersion The product version
    * @return The value
    */
   public XAResourceRecovery createXAResourceRecovery(ResourceAdapter rar,
                                                      ActivationSpec as,
                                                      String productName, String productVersion);

   /**
    * Create an XAResourceRecovery instance
    *
    * @param mcf The managed connection factory
    * @param pad Should the branch qualifier for Xid's be padded
    * @param override Should the isSameRM value be overriden; <code>null</code> for instance equally check
    * @param wrapXAResource Should the XAResource be wrapped
    * @param recoverUserName The user name for recovery
    * @param recoverPassword The password for recovery
    * @param recoverSecurityDomain The security domain for recovery
    * @param subjectFactory The subject factory
    * @param plugin The recovery plugin
    * @param xastat The XAResource statistics implementation
    * @return The value
    */
   public XAResourceRecovery createXAResourceRecovery(ManagedConnectionFactory mcf,
                                                      Boolean pad, Boolean override, 
                                                      Boolean wrapXAResource,
                                                      String recoverUserName, String recoverPassword, 
                                                      String recoverSecurityDomain,
                                                      SubjectFactory subjectFactory,
                                                      RecoveryPlugin plugin,
                                                      XAResourceStatistics xastat);

   /**
    * Create a LocalXAResource instance
    * @param cm The connection manager
    * @param productName The product name
    * @param productVersion The product version
    * @param jndiName The JNDI name for the resource
    * @param xastat The XAResource statistics implementation
    * @return The value
    */
   public LocalXAResource createLocalXAResource(ConnectionManager cm, 
                                                String productName, String productVersion,
                                                String jndiName,
                                                XAResourceStatistics xastat);

   /**
    * Create a connectable LocalXAResource instance
    * @param cm The connection manager
    * @param productName The product name
    * @param productVersion The product version
    * @param jndiName The JNDI name for the resource
    * @param cr The connectable resource
    * @param xastat The XAResource statistics implementation
    * @return The value
    */
   public LocalXAResource createConnectableLocalXAResource(ConnectionManager cm, 
                                                           String productName, String productVersion,
                                                           String jndiName, ConnectableResource cr,
                                                           XAResourceStatistics xastat);

   /**
    * Create a connectable LocalXAResource instance
    * @param cm The connection manager
    * @param productName The product name
    * @param productVersion The product version
    * @param jndiName The JNDI name for the resource
    * @param mc The managed connection
    * @param xastat The XAResource statistics implementation
    * @return The value
    */
   public LocalXAResource createConnectableLocalXAResource(ConnectionManager cm, 
                                                           String productName, String productVersion,
                                                           String jndiName, ManagedConnection mc,
                                                           XAResourceStatistics xastat);

   /**
    * Create an XAResource wrapper instance
    * @param xares The XAResource instance
    * @param pad Should the branch qualifier for Xid's be padded
    * @param override Should the isSameRM value be overriden; <code>null</code> for instance equally check
    * @param productName The product name
    * @param productVersion The product version
    * @param jndiName The JNDI name for the resource
    * @param firstResource Is the resource a first resource
    * @param xastat The XAResource statistics implementation
    * @return The value
    */
   public XAResourceWrapper createXAResourceWrapper(XAResource xares,
                                                    boolean pad, Boolean override, 
                                                    String productName, String productVersion,
                                                    String jndiName, boolean firstResource,
                                                    XAResourceStatistics xastat);

   /**
    * Create a connectable XAResource wrapper instance
    * @param xares The XAResource instance
    * @param pad Should the branch qualifier for Xid's be padded
    * @param override Should the isSameRM value be overriden; <code>null</code> for instance equally check
    * @param productName The product name
    * @param productVersion The product version
    * @param jndiName The JNDI name for the resource
    * @param cr The connectable resource
    * @param xastat The XAResource statistics implementation
    * @return The value
    */
   public XAResourceWrapper createConnectableXAResourceWrapper(XAResource xares,
                                                               boolean pad, Boolean override, 
                                                               String productName, String productVersion,
                                                               String jndiName, ConnectableResource cr,
                                                               XAResourceStatistics xastat);

   /**
    * Create a connectable XAResource wrapper instance
    * @param xares The XAResource instance
    * @param pad Should the branch qualifier for Xid's be padded
    * @param override Should the isSameRM value be overriden; <code>null</code> for instance equally check
    * @param productName The product name
    * @param productVersion The product version
    * @param jndiName The JNDI name for the resource
    * @param mc The managed connection
    * @param xastat The XAResource statistics implementation
    * @return The value
    */
   public XAResourceWrapper createConnectableXAResourceWrapper(XAResource xares,
                                                               boolean pad, Boolean override, 
                                                               String productName, String productVersion,
                                                               String jndiName, ManagedConnection mc,
                                                               XAResourceStatistics xastat);

   /**
    * Is a first resource
    * @param mc The managed connection
    * @return The value
    */
   public boolean isFirstResource(ManagedConnection mc);

   /**
    * Is a connectable resource
    * @param mc The managed connection
    * @return The value
    */
   public boolean isConnectableResource(ManagedConnection mc);

   /**
    * Get the identifier for the transaction
    * @param tx The transaction
    * @return Its stable identifier
    */
   public Object getIdentifier(Transaction tx);
}
