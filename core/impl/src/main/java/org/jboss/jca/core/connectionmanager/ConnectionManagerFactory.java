/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.management.ManagedEnlistmentTrace;
import org.jboss.jca.core.connectionmanager.notx.NoTxConnectionManagerImpl;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerImpl;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.TransactionManager;

/**
 * The connection manager factory.
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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
    * @param subjectFactory The subject factory
    * @param securityDomain The security domain 
    * @param useCcm Should the CCM be used
    * @param ccm The cached connection manager
    * @param sharable Enable sharable connections
    * @param enlistment Enable enlistment connections
    * @param connectable Enable connectable connections
    * @param tracking The tracking status
    * @param flushStrategy The flush strategy
    * @param allocationRetry The allocation retry value
    * @param allocationRetryWaitMillis The allocation retry millis value
    * @return The connection manager instance
    */
   public NoTxConnectionManager createNonTransactional(final TransactionSupportLevel tsl,
                                                       final Pool pool,
                                                       final SubjectFactory subjectFactory,
                                                       final String securityDomain,
                                                       final boolean useCcm,
                                                       final CachedConnectionManager ccm,
                                                       final boolean sharable,
                                                       final boolean enlistment,
                                                       final boolean connectable,
                                                       final Boolean tracking,
                                                       final FlushStrategy flushStrategy,
                                                       final Integer allocationRetry,
                                                       final Long allocationRetryWaitMillis)
   {
      if (tsl == null)
         throw new IllegalArgumentException("TransactionSupportLevel is null");

      if (pool == null)
         throw new IllegalArgumentException("Pool is null");

      if (flushStrategy == null)
         throw new IllegalArgumentException("FlushStrategy is null");

      NoTxConnectionManagerImpl cm = null;

      switch (tsl)
      {
         case NoTransaction:
            cm = new NoTxConnectionManagerImpl();
            break;

         case LocalTransaction:
            throw new IllegalArgumentException("Transactional connection manager not supported");

         case XATransaction:
            throw new IllegalArgumentException("Transactional connection manager not supported");

         default:
            throw new IllegalArgumentException("Unknown transaction support level " + tsl);
      }

      setProperties(cm, pool,
                    subjectFactory, securityDomain, 
                    useCcm, ccm,
                    sharable,
                    enlistment,
                    connectable,
                    tracking,
                    null,
                    flushStrategy,
                    allocationRetry, allocationRetryWaitMillis, 
                    null);
      setNoTxProperties(cm);

      return cm;
   }

   /**
    * Create a transactional connection manager
    * @param tsl The transaction support level
    * @param pool The pool for the connection manager
    * @param subjectFactory The subject factory
    * @param securityDomain The security domain 
    * @param useCcm Should the CCM be used
    * @param ccm The cached connection manager
    * @param sharable Enable sharable connections
    * @param enlistment Enable enlistment connections
    * @param connectable Enable connectable connections
    * @param tracking The tracking status
    * @param enlistmentTrace The enlistment trace
    * @param flushStrategy The flush strategy
    * @param allocationRetry The allocation retry value
    * @param allocationRetryWaitMillis The allocation retry millis value
    * @param txIntegration The transaction manager integration
    * @param interleaving Enable interleaving
    * @param xaResourceTimeout The transaction timeout for XAResource
    * @param isSameRMOverride Should isSameRM be overridden
    * @param wrapXAResource Should XAResource be wrapped
    * @param padXid Should Xids be padded
    * @return The connection manager instance
    */
   public TxConnectionManager createTransactional(final TransactionSupportLevel tsl,
                                                  final Pool pool,
                                                  final SubjectFactory subjectFactory,
                                                  final String securityDomain,
                                                  final boolean useCcm,
                                                  final CachedConnectionManager ccm,
                                                  final boolean sharable,
                                                  final boolean enlistment,
                                                  final boolean connectable,
                                                  final Boolean tracking,
                                                  final ManagedEnlistmentTrace enlistmentTrace,
                                                  final FlushStrategy flushStrategy,
                                                  final Integer allocationRetry,
                                                  final Long allocationRetryWaitMillis,
                                                  final TransactionIntegration txIntegration,
                                                  final Boolean interleaving,
                                                  final Integer xaResourceTimeout,
                                                  final Boolean isSameRMOverride,
                                                  final Boolean wrapXAResource,
                                                  final Boolean padXid)
   {
      if (tsl == null)
         throw new IllegalArgumentException("TransactionSupportLevel is null");

      if (pool == null)
         throw new IllegalArgumentException("Pool is null");

      if (txIntegration == null)
         throw new IllegalArgumentException("TransactionIntegration is null");

      if (flushStrategy == null)
         throw new IllegalArgumentException("FlushStrategy is null");

      TxConnectionManagerImpl cm = null;

      switch (tsl)
      {
         case NoTransaction:
            throw new IllegalArgumentException("Non transactional connection manager not supported");

         case LocalTransaction:
            cm = new TxConnectionManagerImpl(txIntegration, true);
            break;

         case XATransaction:
            cm = new TxConnectionManagerImpl(txIntegration, false);
            break;

         default:
            throw new IllegalArgumentException("Unknown transaction support level " + tsl);
      }

      setProperties(cm, pool, 
                    subjectFactory, securityDomain, 
                    useCcm, ccm,
                    sharable,
                    enlistment,
                    connectable,
                    tracking,
                    enlistmentTrace,
                    flushStrategy,
                    allocationRetry, allocationRetryWaitMillis,
                    txIntegration.getTransactionManager());
      setTxProperties(cm, interleaving, xaResourceTimeout, isSameRMOverride, wrapXAResource, padXid);

      return cm;
   }

   /**
    * Common properties
    * @param cm The connection manager
    * @param pool The pool
    * @param subjectFactory The subject factory
    * @param securityDomain The security domain
    * @param useCcm Should the CCM be used
    * @param ccm The cached connection manager
    * @param sharable Enable sharable connections
    * @param enlistment Enable enlistment connections
    * @param connectable Enable connectable connections
    * @param tracking The tracking status
    * @param enlistmentTrace The enlistment trace
    * @param flushStrategy The flush strategy
    * @param allocationRetry The allocation retry value
    * @param allocationRetryWaitMillis The allocation retry millis value
    * @param tm The transaction manager
    */
   private void setProperties(AbstractConnectionManager cm,
                              Pool pool,
                              SubjectFactory subjectFactory,
                              String securityDomain,
                              boolean useCcm,
                              CachedConnectionManager ccm,
                              boolean sharable,
                              boolean enlistment,
                              boolean connectable,
                              Boolean tracking,
                              ManagedEnlistmentTrace enlistmentTrace,
                              FlushStrategy flushStrategy,
                              Integer allocationRetry,
                              Long allocationRetryWaitMillis,
                              TransactionManager tm)
   {
      pool.setConnectionManager(cm);
      cm.setPool(pool);

      cm.setSubjectFactory(subjectFactory);
      cm.setSecurityDomain(securityDomain);

      cm.setFlushStrategy(flushStrategy);

      if (allocationRetry != null)
         cm.setAllocationRetry(allocationRetry.intValue());

      if (allocationRetryWaitMillis != null)
         cm.setAllocationRetryWaitMillis(allocationRetryWaitMillis.longValue());

      if (useCcm)
         cm.setCachedConnectionManager(ccm);

      cm.setSharable(sharable);
      cm.setEnlistment(enlistment);
      cm.setConnectable(connectable);
      cm.setTracking(tracking);
      cm.setEnlistmentTrace(enlistmentTrace);
   }

   /**
    * NoTxConnectionManager properties
    * @param cm The connection manager
    */
   private void setNoTxProperties(NoTxConnectionManagerImpl cm)
   {
   }

   /**
    * TxConnectionManager properties
    * @param cm The connection manager
    * @param interleaving Enable interleaving
    * @param xaResourceTimeout The transaction timeout for XAResource
    * @param isSameRMOverride Should isSameRM be overridden
    * @param wrapXAResource Should XAResource be wrapped
    * @param padXid Should Xids be padded
    */
   private void setTxProperties(TxConnectionManagerImpl cm,
                                Boolean interleaving,
                                Integer xaResourceTimeout,
                                Boolean isSameRMOverride,
                                Boolean wrapXAResource,
                                Boolean padXid)
   {
      if (interleaving != null)
         cm.setInterleaving(interleaving.booleanValue());

      if (xaResourceTimeout != null)
         cm.setXAResourceTimeout(xaResourceTimeout.intValue());

      cm.setIsSameRMOverride(isSameRMOverride);

      if (wrapXAResource != null)
         cm.setWrapXAResource(wrapXAResource.booleanValue());

      if (padXid != null)
         cm.setPadXid(padXid.booleanValue());
   }
}
