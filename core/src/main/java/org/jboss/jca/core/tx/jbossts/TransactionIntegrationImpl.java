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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecoveryRegistry;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry;
import org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper;
import org.jboss.jca.core.spi.transaction.xa.XATerminator;

import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

import org.jboss.security.SubjectFactory;

/**
 * This class provide an implementation of the transaction integration for
 * the IronJacamar container using JBossTS.
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TransactionIntegrationImpl implements TransactionIntegration
{
   /** The transaction manager */
   private TransactionManager tm;

   /** The transaction synchronization registry */
   private TransactionSynchronizationRegistry tsr;

   /** User transaction registry */
   private org.jboss.tm.usertx.UserTransactionRegistry utr;

   /** XATerminator */
   private org.jboss.tm.JBossXATerminator terminator;

   /** Recovery registry */
   private org.jboss.tm.XAResourceRecoveryRegistry rr;

   /**
    * Constructor
    * @param tm The transaction manager
    * @param tsr The transaction synchronization registry
    * @param utr The user transaction registry
    * @param terminator The XA terminator
    * @param rr The recovery registry
    */
   public TransactionIntegrationImpl(TransactionManager tm,
                                     TransactionSynchronizationRegistry tsr,
                                     org.jboss.tm.usertx.UserTransactionRegistry utr,
                                     org.jboss.tm.JBossXATerminator terminator,
                                     org.jboss.tm.XAResourceRecoveryRegistry rr)
   {
      if (tm instanceof org.jboss.tm.TransactionTimeoutConfiguration)
      {
         this.tm = new TransactionManagerDelegator(tm);
      }
      else
      {
         this.tm = tm;
      }
      this.tsr = tsr;
      this.utr = utr;
      this.terminator = terminator;
      this.rr = rr;
   }

   /**
    * Get the transaction manager
    * @return The value
    */
   public TransactionManager getTransactionManager()
   {
      return tm;
   }

   /**
    * Get the transaction synchronization registry
    * @return The value
    */
   public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      return tsr;
   }

   /**
    * Get the user transaction registry
    * @return The value
    */
   public UserTransactionRegistry getUserTransactionRegistry()
   {
      return new UserTransactionRegistryImpl(utr);
   }

   /**
    * Get the recovery registry
    * @return The value
    */
   public XAResourceRecoveryRegistry getRecoveryRegistry()
   {
      return new XAResourceRecoveryRegistryImpl(rr);
   }

   /**
    * Get the XATerminator
    * @return The value
    */
   public XATerminator getXATerminator()
   {
      return new XATerminatorImpl(terminator);
   }

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
    * @return The value
    */
   public XAResourceRecovery createXAResourceRecovery(ManagedConnectionFactory mcf,
                                                      Boolean pad, Boolean override, 
                                                      Boolean wrapXAResource,
                                                      String recoverUserName, String recoverPassword, 
                                                      String recoverSecurityDomain,
                                                      SubjectFactory subjectFactory,
                                                      RecoveryPlugin plugin)
   {
      return new XAResourceRecoveryImpl(this, mcf, pad, override, wrapXAResource,
                                        recoverUserName, recoverPassword, recoverSecurityDomain,
                                        subjectFactory, plugin);
   }

   /**
    * Create a LocalXAResource instance
    * @param cm The connection manager
    * @return The value
    */
   public LocalXAResource createLocalXAResource(ConnectionManager cm)
   {
      LocalXAResource result = new LocalXAResourceImpl();
      result.setConnectionManager(cm);

      return result;
   }

   /**
    * Create an XAResource wrapper instance
    * @param xares The XAResource instance
    * @param pad Should the branch qualifier for Xid's be padded
    * @param override Should the isSameRM value be overriden; <code>null</code> for instance equally check
    * @param productName The product name
    * @param productVersion The product version
    * @param jndiName The JNDI name for the resource
    * @return The value
    */
   public XAResourceWrapper createXAResourceWrapper(XAResource xares,
                                                    boolean pad, Boolean override, 
                                                    String productName, String productVersion,
                                                    String jndiName)
   {
      return new XAResourceWrapperImpl(xares, pad, override, productName, productVersion, jndiName);
   }
}
