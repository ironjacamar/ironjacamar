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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.XAResourceStatistics;
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
 * This class provide an implementation of the transaction integration for
 * the IronJacamar container using JBossTS.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TransactionIntegrationImpl implements TransactionIntegration
{
   /** The transaction manager */
   private TransactionManager tm;

   /** The transaction synchronization registry */
   private TransactionSynchronizationRegistry tsr;

   /** User transaction registry */
   private UserTransactionRegistry utr;

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
      this.utr = new UserTransactionRegistryImpl(utr);
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
      return utr;
   }

   /**
    * Get the recovery registry
    * @return The value
    */
   public XAResourceRecoveryRegistry getRecoveryRegistry()
   {
      if (rr == null)
         return null;

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
    * {@inheritDoc}
    */
   public XAResourceRecovery createXAResourceRecovery(ResourceAdapter rar,
                                                      ActivationSpec as,
                                                      String productName, String productVersion)
   {
      return new XAResourceRecoveryInflowImpl(rar, as, productName, productVersion);
   }

   /**
    * {@inheritDoc}
    */
   public XAResourceRecovery createXAResourceRecovery(ManagedConnectionFactory mcf,
                                                      Boolean pad, Boolean override, 
                                                      Boolean wrapXAResource,
                                                      String recoverUserName, String recoverPassword, 
                                                      String recoverSecurityDomain,
                                                      SubjectFactory subjectFactory,
                                                      RecoveryPlugin plugin,
                                                      XAResourceStatistics xastat)
   {
      return new XAResourceRecoveryImpl(this, mcf, pad, override, wrapXAResource,
                                        recoverUserName, recoverPassword, recoverSecurityDomain,
                                        subjectFactory, plugin, xastat);
   }

   /**
    * {@inheritDoc}
    */
   public LocalXAResource createConnectableLocalXAResource(ConnectionManager cm,
                                                           String productName, String productVersion,
                                                           String jndiName, ConnectableResource cr,
                                                           XAResourceStatistics xastat)
   {
      LocalXAResource result = null;
      if (xastat != null && xastat.isEnabled())
      {
         result = new LocalConnectableXAResourceStatImpl(productName, productVersion, jndiName, cr, xastat);
      }
      else
      {
         result = new LocalConnectableXAResourceImpl(productName, productVersion, jndiName, cr);
      }
      result.setConnectionManager(cm);

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public LocalXAResource createConnectableLocalXAResource(ConnectionManager cm,
                                                           String productName, String productVersion,
                                                           String jndiName, ManagedConnection mc,
                                                           XAResourceStatistics xastat)
   {
      LocalXAResource result = null;
      if (xastat != null && xastat.isEnabled())
      {
         result = new LocalConnectableXAResourceStatImpl(productName, productVersion, jndiName,
                                                         (org.jboss.tm.ConnectableResource)mc,
                                                         xastat);
      }
      else
      {
         result = new LocalConnectableXAResourceImpl(productName, productVersion, jndiName,
                                                     (org.jboss.tm.ConnectableResource)mc);
      }
      result.setConnectionManager(cm);

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public LocalXAResource createLocalXAResource(ConnectionManager cm,
                                                String productName, String productVersion,
                                                String jndiName,
                                                XAResourceStatistics xastat)
   {
      LocalXAResource result = null;
      if (xastat != null && xastat.isEnabled())
      {
         result = new LocalXAResourceStatImpl(productName, productVersion, jndiName, xastat);
      }
      else
      {
         result = new LocalXAResourceImpl(productName, productVersion, jndiName);
      }
      result.setConnectionManager(cm);

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public XAResourceWrapper createConnectableXAResourceWrapper(XAResource xares,
                                                               boolean pad, Boolean override, 
                                                               String productName, String productVersion,
                                                               String jndiName,
                                                               ConnectableResource cr,
                                                               XAResourceStatistics xastat)
   {
      if (cr instanceof org.jboss.jca.core.spi.transaction.FirstResource ||
          cr instanceof org.jboss.tm.FirstResource)
      {
         if (xastat != null && xastat.isEnabled())
         {
            return new FirstResourceConnectableXAResourceWrapperStatImpl(xares, pad, override,
                                                                         productName, productVersion, jndiName,
                                                                         cr, xastat);
         }
         else
         {
            return new FirstResourceConnectableXAResourceWrapperImpl(xares, pad, override,
                                                                     productName, productVersion, jndiName,
                                                                     cr);
         }
      }
      else
      {
         if (xastat != null && xastat.isEnabled())
         {
            return new ConnectableXAResourceWrapperStatImpl(xares, pad, override,
                                                            productName, productVersion, jndiName,
                                                            cr, xastat);
         }
         else
         {
            return new ConnectableXAResourceWrapperImpl(xares, pad, override,
                                                        productName, productVersion, jndiName,
                                                        cr);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public XAResourceWrapper createConnectableXAResourceWrapper(XAResource xares,
                                                               boolean pad, Boolean override, 
                                                               String productName, String productVersion,
                                                               String jndiName,
                                                               ManagedConnection mc,
                                                               XAResourceStatistics xastat)
   {
      if (mc instanceof org.jboss.jca.core.spi.transaction.FirstResource ||
          mc instanceof org.jboss.tm.FirstResource)
      {
         if (xastat != null && xastat.isEnabled())
         {
            return new FirstResourceConnectableXAResourceWrapperStatImpl(xares, pad, override,
                                                                         productName, productVersion, jndiName,
                                                                         (org.jboss.tm.ConnectableResource)mc,
                                                                         xastat);
         }
         else
         {
            return new FirstResourceConnectableXAResourceWrapperImpl(xares, pad, override,
                                                                     productName, productVersion, jndiName,
                                                                     (org.jboss.tm.ConnectableResource)mc);
         }
      }
      else
      {
         if (xastat != null && xastat.isEnabled())
         {
            return new ConnectableXAResourceWrapperStatImpl(xares, pad, override,
                                                            productName, productVersion, jndiName,
                                                            (org.jboss.tm.ConnectableResource)mc, xastat);
         }
         else
         {
            return new ConnectableXAResourceWrapperImpl(xares, pad, override,
                                                        productName, productVersion, jndiName,
                                                        (org.jboss.tm.ConnectableResource)mc);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public XAResourceWrapper createXAResourceWrapper(XAResource xares,
                                                    boolean pad, Boolean override, 
                                                    String productName, String productVersion,
                                                    String jndiName, boolean firstResource,
                                                    XAResourceStatistics xastat)
   {
      if (firstResource)
      {
         if (xastat != null && xastat.isEnabled())
         {
            return new FirstResourceXAResourceWrapperStatImpl(xares, pad, override, productName, productVersion,
                                                              jndiName, xastat);
         }
         else
         {
            return new FirstResourceXAResourceWrapperImpl(xares, pad, override, productName, productVersion, jndiName);
         }
      }
      else
      {
         if (xastat != null && xastat.isEnabled())
         {
            return new XAResourceWrapperStatImpl(xares, pad, override, productName, productVersion, jndiName, xastat);
         }
         else
         {
            return new XAResourceWrapperImpl(xares, pad, override, productName, productVersion, jndiName);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isFirstResource(ManagedConnection mc)
   {
      return mc != null && mc instanceof org.jboss.tm.FirstResource;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isConnectableResource(ManagedConnection mc)
   {
      return mc != null && mc instanceof org.jboss.tm.ConnectableResource;
   }

   /**
    * {@inheritDoc}
    */
   public Object getIdentifier(Transaction tx)
   {
      return tx;
   }
}
