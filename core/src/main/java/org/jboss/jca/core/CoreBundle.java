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

package org.jboss.jca.core;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * The core bundle.
 *
 * Message ids ranging from 000000 to 009999 inclusively.
 */
@MessageBundle(projectCode = "IJ")
public interface CoreBundle
{

   // CACHED CONNECTION MANAGER (100)

   /**
    * Some connections were not closed
    * @return The value
    */
   @Message(id = 151, value = "Some connections were not closed, see the log for the allocation stacktraces")
   public String someConnectionsWereNotClosed();
   
   /**
    * Trying to return an unknown connection
    * @param connection The connection
    * @return The value
    */
   @Message(id = 152, value = "Trying to return an unknown connection: %s")
   public String tryingToReturnUnknownConnection(String connection);
   
   // WORK MANAGER (200)
   
   /**
    * SecurityContext setup failed
    * @param message The throwable description
    * @return The value
    */
   @Message(id = 251, value = "SecurityContext setup failed: %s")
   public String securityContextSetupFailed(String message);
   
   /**
    * SecurityContext setup failed since CallbackSecurity was null
    * @return The value
    */
   @Message(id = 252, value = "SecurityContext setup failed since CallbackSecurity was null")
   public String securityContextSetupFailedSinceCallbackSecurityWasNull();
   
   /**
    * Work is null
    * @return The value
    */
   @Message(id = 253, value = "Work is null")
   public String workIsNull();
   
   /**
    * StartTimeout is negative
    * @param startTimeout timeout of start
    * @return The value
    */
   @Message(id = 254, value = "StartTimeout is negative: %s")
   public String startTimeoutIsNegative(long startTimeout);
   
   /**
    * Interrupted while requesting permit
    * @return The value
    */
   @Message(id = 255, value = "Interrupted while requesting permit")
   public String interruptedWhileRequestingPermit();
   
   /**
    * Work execution context must be null because work instance implements WorkContextProvider
    * @return The value
    */
   @Message(id = 256, value = "Work execution context must be null because " +
         "work instance implements WorkContextProvider")
   public String workExecutionContextMustNullImplementsWorkContextProvider();

   /**
    * Run method is synchronized
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 257, value = "%s: Run method is synchronized")
   public String runMethodIsSynchronized(String classname);

   /**
    * Release method is synchronized
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 258, value = "%s: Release method is synchronized")
   public String releaseMethodIsSynchronized(String classname);

   /**
    * Unsupported WorkContext class
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 259, value = "Unsupported WorkContext class: %s")
   public String unsupportedWorkContextClass(String classname);

   /**
    * Duplicate TransactionWorkContext class
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 260, value = "Duplicate TransactionWorkContext class: %s")
   public String duplicateTransactionWorkContextClass(String classname);

   /**
    * Duplicate SecurityWorkContext class
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 261, value = "Duplicate SecurityWorkContext class: %s")
   public String duplicateSecurityWorkContextClass(String classname);

   /**
    * Duplicate HintWorkContext class
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 262, value = "Duplicate HintWorkContext class: %s")
   public String duplicateHintWorkContextClass(String classname);
   
   /**
    * WorkManager shutdown
    * @return The value
    */
   @Message(id = 263, value = "WorkManager is shutting down")
   public String workmanagerShutdown();

   /**
    * SecurityContext setup failed since CallbackSecurity::Domain was empty
    * @return The value
    */
   @Message(id = 264, value = "SecurityContext setup failed since CallbackSecurity::Domain was empty")
   public String securityContextSetupFailedSinceCallbackSecurityDomainWasEmpty();
   
   /**
    * ResourceAdapterAssociation failed
    * @param clz The class name
    * @return The value
    */
   @Message(id = 265, value = "ResourceAdapterAssociation failed for %s")
   public String resourceAdapterAssociationFailed(String clz);
   
   /**
    * Invalid number of parameters
    * @param number The number
    * @param c The command
    * @return The value
    */
   @Message(id = 266, value = "Invalid number of parameters %d (%s)")
   public String invalidNumberOfParameters(int number, String c);
   
   // CONNECTION MANAGER LISTENER (300)

   /**
    * Not correct type in class cast
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 351, value = "Not correct type: %s")
   public String notCorrectTypeWhenClassCast(String classname);

   /**
    * Failure to delist resource
    * @param obj listener instance
    * @return The value
    */
   @Message(id = 352, value = "Failure to delist resource: %s")
   public String failureDelistResource(Object obj);
   
   /**
    * Error in delist
    * @return The value
    */
   @Message(id = 353, value = "Error in delist")
   public String errorInDelist();
   
   /**
    * Unfinished local transaction - error getting local transaction from
    * @param obj listener instance
    * @return The value
    */
   @Message(id = 354, value = "Unfinished local transaction - error getting local transaction from %s")
   public String unfinishedLocalTransaction(Object obj);
   
   /**
    * Unfinished local transaction but managed connection does not provide a local transaction
    * @param obj listener instance
    * @return The value
    */
   @Message(id = 355, value = "Unfinished local transaction but managed connection does not " +
                  "provide a local transaction: %s")
   public String unfinishedLocalTransactionNotProvideLocalTransaction(Object obj);

   /**
    * System exception when failedToEnlist equals currentTx
    * @param throwable throwable instance
    * @param currentTx current transaction instance
    * @return The value
    */
   @Message(id = 356, value = "Failed to enlist: %s tx=%s")
   public String systemExceptionWhenFailedToEnlistEqualsCurrentTx(Object throwable, Object currentTx);
   
   /**
    * Error in dissociate
    * @return The value
    */
   @Message(id = 357, value = "Error in dissociate")
   public String errorInDissociate();
      
   // CONNECTION MANAGER (400)

   /**
    * The connection manager is shutdown 
    * @param jndiName jndi name
    * @return The value
    */
   @Message(id = 451, value = "The connection manager is shutdown: %s")
   public String connectionManagerIsShutdown(String jndiName);

   /**
    * Method getManagedConnection retry wait was interrupted
    * @param jndiName jndi name
    * @return The value
    */
   @Message(id = 452, value = "Method getManagedConnection retry wait was interrupted: %s")
   public String getManagedConnectionRetryWaitInterrupted(String jndiName);
   
   /**
    * Unable to get managed connection for 
    * @param jndiName jndi name
    * @return The value
    */
   @Message(id = 453, value = "Unable to get managed connection for %s")
   public String unableGetManagedConnection(String jndiName);
   
   /**
    * You are trying to use a connection factory that has been shut down ManagedConnectionFactory is null
    * @return The value
    */
   @Message(id = 454, value = "You are trying to use a connection factory that has been shut down: " +
         "ManagedConnectionFactory is null")
   public String tryingUseConnectionFactoryShutDown();
   
   /**
    * Wrong ManagedConnectionFactory sent to allocateConnection
    * @param pool The ManagedConnectionFactory used for the pool
    * @param mcf The ManagedConnectionFactory passed in
    * @return The value
    */
   @Message(id = 455, value = "Wrong ManagedConnectionFactory sent to allocateConnection (Pool=%s, MCF=%s)")
   public String wrongManagedConnectionFactorySentToAllocateConnection(Object pool, Object mcf);

   /**
    * Unchecked throwable in ManagedConnection.getConnection()
    * @param obj ConnectionListener instance
    * @return The value
    */
   @Message(id = 456, value = "Unchecked throwable in ManagedConnection.getConnection() cl=%s")
   public String uncheckedThrowableInManagedConnectionGetConnection(Object obj);

   /**
    * Unchecked throwable in managedConnectionReconnected()
    * @param obj ConnectionListener instance
    * @return The value
    */
   @Message(id = 457, value = "Unchecked throwable in managedConnectionReconnected() cl=%s")
   public String uncheckedThrowableInManagedConnectionReconnected(Object obj);
   
   /**
    * This method is not supported
    * @return The value
    */
   @Message(id = 458, value = "This method is not supported")
   public String thisMethodNotSupported();
   
   /**
    * Transaction is not active
    * @param obj transaction instance
    * @return The value
    */
   @Message(id = 459, value = "Transaction is not active: tx=%s")
   public String transactionNotActive(Object obj);
   
   /**
    * Error checking for a transaction.
    * @return The value
    */
   @Message(id = 460, value = "Error checking for a transaction")
   public String errorCheckingForTransaction();
   
   /**
    * Could not enlist in transaction on entering meta-aware object
    * @return The value
    */
   @Message(id = 461, value = "Could not enlist in transaction on entering meta-aware object")
   public String notEnlistInTransactionOnEnteringMetaAwareObject();
   
   /**
    * Could not delist resource, probably a transaction rollback
    * @return The value
    */
   @Message(id = 462, value = "Could not delist resource, probably a transaction rollback")
   public String couldNotDelistResourceThenTransactionRollback();
   
   /**
    * Unable to set XAResource transaction timeout
    * @param jndiName jndi name
    * @return The value
    */
   @Message(id = 463, value = "Unable to set XAResource transaction timeout: %s")
   public String unableSetXAResourceTransactionTimeout(String jndiName);
      
   /**
    * Unable to find connection listener
    * @return The value
    */
   @Message(id = 464, value = "Unable to find connection listener")
   public String unableToFindConnectionListener();
      
   /**
    * Connection is null
    * @return The value
    */
   @Message(id = 465, value = "Connection is null")
   public String connectionIsNull();

   /**
    * Enlistment not enabled
    * @return The value
    */
   @Message(id = 466, value = "Enlistment not enabled")
   public String enlistmentNotEnabled();
      
   /**
    * Managed connection not lazy enlistable
    * @param mc The managed connection
    * @return The value
    */
   @Message(id = 467, value = "Managed connection not lazy enlistable: %s")
   public String managedConnectionNotLazyEnlistable(Object mc);
      
   /**
    * Connection listener already enlisted
    * @param cl The connection listener
    * @return The value
    */
   @Message(id = 468, value = "Connection listener already enlisted: %s")
   public String connectionListenerAlreadyEnlisted(Object cl);

   /**
    * Error during enlistment
    * @return The value
    */
   @Message(id = 469, value = "Error during enlistment")
   public String errorDuringEnlistment();

   /**
    * You are trying to use a connection factory that has been shut down
    * @param name The name
    * @return The value
    */
   @Message(id = 470, value = "You are trying to use a connection factory that has been shut down: %s")
   public String tryingUseConnectionFactoryShutDown(String name);

   // TRANSACTION SYNCHRONIZER (500)
   
   // POOL MANAGER (600)
   
   /**
    * Unable to get managed connection pool
    * @return The value
    */
   @Message(id = 651, value = "Unable to get managed connection pool")
   public String unableGetManagedConnectionPool();
   
   /**
    * Unable to obtain lock
    * @return The value
    */
   @Message(id = 652, value = "Unable to obtain lock")
   public String unableObtainLock();
   
   /**
    * The pool has been shutdown
    * @param pool The pool
    * @param mcp The managed connection pool
    * @return The value
    */
   @Message(id = 653, value = "The pool has been shutdown (%s,%s)")
   public String thePoolHasBeenShutdown(String pool, String mcp);

   /**
    * Interrupted while requesting connection
    * @param end time of end
    * @return The value
    */
   @Message(id = 654, value = "Interrupted while requesting connection: Waited %s ms")
   public String interruptedWhileRequestingConnection(long end);

   /**
    * No ManagedConnections available within configured blocking timeout
    * @param blockingTimeout timeout of blocking
    * @return The value
    */
   @Message(id = 655, value = "No managed connections available within configured blocking timeout (%s [ms])")
   public String noMManagedConnectionsAvailableWithinConfiguredBlockingTimeout(long blockingTimeout);

   /**
    * This should never happen
    * @return The value
    */
   @Message(id = 656, value = "This should never happen")
   public String shouldNeverHappen();

   /**
    * Interrupted while requesting permit
    * @param end time of end
    * @return The value
    */
   @Message(id = 657, value = "Interrupted while requesting permit: Waited %s ms")
   public String interruptedWhileRequestingPermit(long end);
   
   /**
    * Unexpected throwable while trying to create a connection:
    * @param obj connection listener instance
    * @return The value
    */
   @Message(id = 658, value = "Unexpected throwable while trying to create a connection: %s")
   public String unexpectedThrowableWhileTryingCreateConnection(Object obj);
   
   /**
    * Unable to get connection listener
    * @return The value
    */
   @Message(id = 659, value = "Unable to get connection listener")
   public String unableGetConnectionListener();

   // NAMING (700)

   /**
    * Deployment failed since jndi name is already deployed
    * @param className class name
    * @param jndiName jndi name
    * @return The value
    */
   @Message(id = 751, value = "Deployment %s failed, %s is already deployed")
   public String deploymentFailedSinceJndiNameHasDeployed(String className, String jndiName);
   
   // RESOURCE ADPATER REPOSITORY (800)
   
   /**
    * ResourceAdapter instance not active
    * @return The value
    */
   @Message(id = 851, value = "Resource adapter instance not active")
   public String resourceAdapterInstanceNotActive();
   
   /**
    * Validation exception
    * @return The value
    */
   @Message(id = 852, value = "Validation exception")
   public String validationException();
   
   /**
    * The activation spec class is no longer available
    * @return The value
    */
   @Message(id = 853, value = "The activation spec class is no longer available")
   public String activationSpecClassNotAvailable();
   
   /**
    * The resource adapter is no longer available
    * @return The value
    */
   @Message(id = 854, value = "The resource adapter is no longer available")
   public String resourceAdapterNotAvailable();
   
   /**
    * Key isn't registered
    * @param key key name
    * @return The value
    */
   @Message(id = 855, value = "%s isn't registered")
   public String keyNotRegistered(String key);
   
   /**
    * Unable to lookup resource adapter in MDR
    * @param uniqueId key name
    * @return The value
    */
   @Message(id = 856, value = "Unable to lookup resource adapter in MDR: %s")
   public String unableLookupResourceAdapterInMDR(String uniqueId);
   
   // RECOVERY (900)
   
   /**
    * Error during connection close
    * @return The value
    */
   @Message(id = 951, value = "Error during connection close")
   public String errorDuringConnectionClose();
   
   /**
    * Error during recovery initialization
    * @return The value
    */
   @Message(id = 952, value = "Error during recovery initialization")
   public String errorDuringRecoveryInitialization();
   
   /**
    * Error during recovery shutdown
    * @return The value
    */
   @Message(id = 953, value = "Error during recovery shutdown")
   public String errorDuringRecoveryShutdown();
   
   // SECURITY (1000)

   
   // TRANSCATION (1100)

   /**
    * Trying to start a new tx when old is not complete
    * @param oldXid old xid
    * @param newXid new xid
    * @param flags flags
    * @return The value
    */
   @Message(id = 1151, value = "Trying to start a new transaction when old is not complete: Old: %s, New %s, Flags %s")
   public String tryingStartNewTxWhenOldNotComplete(Object oldXid, Object newXid, int flags);

   /**
    * Trying to start a new tx with wrong flags
    * @param xid xid
    * @param flags flags
    * @return The value
    */
   @Message(id = 1152, value = "Trying to start a new transaction with wrong flags: New %s, Flags %s")
   public String tryingStartNewTxWithWrongFlags(Object xid, int flags);
   
   /**
    * Error trying to start local tx
    * @return The value
    */
   @Message(id = 1153, value = "Error trying to start local transaction")
   public String errorTryingStartLocalTx();
   
   /**
    * Throwable trying to start local transaction
    * @return The value
    */
   @Message(id = 1154, value = "Throwable trying to start local transaction")
   public String throwableTryingStartLocalTx();

   /**
    * Wrong xid in commit
    * @param currentXid current xid
    * @param xid xid
    * @return The value
    */
   @Message(id = 1155, value = "Wrong xid in commit: Expected: %s, Got: %s")
   public String wrongXidInCommit(Object currentXid, Object xid);
   
   /**
    * Could not commit local tx
    * @return The value
    */
   @Message(id = 1156, value = "Could not commit local transaction")
   public String couldNotCommitLocalTx();
   
   /**
    * Forget not supported in local tx
    * @return The value
    */
   @Message(id = 1157, value = "Forget not supported in local transaction")
   public String forgetNotSupportedInLocalTx();
   
   /**
    * No recover with local-tx only resource managers
    * @return The value
    */
   @Message(id = 1158, value = "No recovery for LocalTransaction only resource manager")
   public String noRecoverWithLocalTxResourceManagers();
   
   /**
    * Wrong xid in rollback
    * @param currentXid current xid
    * @param xid xid
    * @return The value
    */
   @Message(id = 1159, value = "Wrong xid in rollback: Expected: %s, Got: %s")
   public String wrongXidInRollback(Object currentXid, Object xid);

   
   /**
    * Could not rollback local tx
    * @return The value
    */
   @Message(id = 1160, value = "Could not rollback local transaction")
   public String couldNotRollbackLocalTx();
   
   // METADATA REPOSITORY (1200)
}
