/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * The core bundle.
 * <p/>
 * Message ids ranging from 000000 to 009999 inclusively.
 */
@MessageBundle(projectCode = "IJ2")
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
    *
    * @param message The throwable description
    * @return The value
    */
   @Message(id = 251, value = "SecurityContext setup failed: %s")
   public String securityContextSetupFailed(String message);

   /**
    * SecurityContext setup failed since CallbackSecurity was null
    *
    * @return The value
    */
   @Message(id = 252,
         value = "SecurityContext setup failed since CallbackSecurity was null")
   public String securityContextSetupFailedSinceCallbackSecurityWasNull();

   /**
    * Work is null
    *
    * @return The value
    */
   @Message(id = 253, value = "Work is null")
   public String workIsNull();

   /**
    * StartTimeout is negative
    *
    * @param startTimeout timeout of start
    * @return The value
    */
   @Message(id = 254, value = "StartTimeout is negative: %s")
   public String startTimeoutIsNegative(long startTimeout);

   /**
    * Interrupted while requesting permit
    *
    * @return The value
    */
   @Message(id = 255, value = "Interrupted while requesting permit")
   public String interruptedWhileRequestingPermit();

   /**
    * Work execution context must be null because work instance implements WorkContextProvider
    *
    * @return The value
    */
   @Message(id = 256,
         value = "Work execution context must be null because " + "work instance implements WorkContextProvider")
   public String workExecutionContextMustNullImplementsWorkContextProvider();

   /**
    * Run method is synchronized
    *
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 257, value = "%s: Run method is synchronized")
   public String runMethodIsSynchronized(String classname);

   /**
    * Release method is synchronized
    *
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 258, value = "%s: Release method is synchronized")
   public String releaseMethodIsSynchronized(String classname);

   /**
    * Unsupported WorkContext class
    *
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 259, value = "Unsupported WorkContext class: %s")
   public String unsupportedWorkContextClass(String classname);

   /**
    * Duplicate TransactionWorkContext class
    *
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 260,
         value = "Duplicate TransactionWorkContext class: %s")
   public String duplicateTransactionWorkContextClass(String classname);

   /**
    * Duplicate SecurityWorkContext class
    *
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 261,
         value = "Duplicate SecurityWorkContext class: %s")
   public String duplicateSecurityWorkContextClass(String classname);

   /**
    * Duplicate HintWorkContext class
    *
    * @param classname class name of work
    * @return The value
    */
   @Message(id = 262, value = "Duplicate HintWorkContext class: %s")
   public String duplicateHintWorkContextClass(String classname);

   /**
    * WorkManager shutdown
    *
    * @return The value
    */
   @Message(id = 263, value = "WorkManager is shutting down")
   public String workmanagerShutdown();

   /**
    * SecurityContext setup failed since CallbackSecurity::Domain was empty
    *
    * @return The value
    */
   @Message(id = 264,
         value = "SecurityContext setup failed since CallbackSecurity::Domain was empty")
   public String securityContextSetupFailedSinceCallbackSecurityDomainWasEmpty();

   /**
    * ResourceAdapterAssociation failed
    *
    * @param clz The class name
    * @return The value
    */
   @Message(id = 265,
         value = "ResourceAdapterAssociation failed for %s")
   public String resourceAdapterAssociationFailed(String clz);

   /**
    * Invalid number of parameters
    *
    * @param number The number
    * @param c      The command
    * @return The value
    */
   @Message(id = 266, value = "Invalid number of parameters %d (%s)")
   public String invalidNumberOfParameters(int number, String c);

   // CONNECTION MANAGER LISTENER (300)

   /**
    * No exception was reported
    * @return The value
    */
   @Message(id = 351, value = "No exception was reported")
   public String noExceptionWasReported();


   /**
    * Failed to enlist
    * @return The value
    */
   @Message(id = 352, value = "Failed to enlist")
   public String failedToEnlist();

   /**
    * Unabled to enlist resource, see the previous warnings.
    * @return The value
    */
   @Message(id = 353, value = "Unabled to enlist resource, see the previous warnings.")
   public String unableToEnlist();


   // NAMING (700)

   /**
    * Deployment failed since jndi name is already deployed
    * @param className class name
    * @param jndiName jndi name
    * @return The value
    */
   @Message(id = 751, value = "Deployment %s failed, %s is already deployed")
   public String deploymentFailedSinceJndiNameHasDeployed(String className, String jndiName);


   // RECOVERY (900)

   /**
    * Error during connection close
    *
    * @return The value
    */
   @Message(id = 951, value = "Error during connection close")
   public String errorDuringConnectionClose();


   // TRANSCATION (1100)

   /**
    * Trying to start a new tx when old is not complete
    *
    * @param oldXid old xid
    * @param newXid new xid
    * @param flags  flags
    * @return The value
    */
   @Message(id = 1151,
         value = "Trying to start a new transaction when old is not complete: Old: %s, New %s, Flags %s")
   public String tryingStartNewTxWhenOldNotComplete(Object oldXid, Object newXid, int flags);

   /**
    * Trying to start a new tx with wrong flags
    *
    * @param xid   xid
    * @param flags flags
    * @return The value
    */
   @Message(id = 1152,
         value = "Trying to start a new transaction with wrong flags: New %s, Flags %s")
   public String tryingStartNewTxWithWrongFlags(Object xid, int flags);

   /**
    * Error trying to start local tx
    *
    * @return The value
    */
   @Message(id = 1153, value = "Error trying to start local transaction")
   public String errorTryingStartLocalTx();

   /**
    * Throwable trying to start local transaction
    *
    * @return The value
    */
   @Message(id = 1154,
         value = "Throwable trying to start local transaction")
   public String throwableTryingStartLocalTx();

   /**
    * Wrong xid in commit
    *
    * @param currentXid current xid
    * @param xid        xid
    * @return The value
    */
   @Message(id = 1155, value = "Wrong xid in commit: Expected: %s, Got: %s")
   public String wrongXidInCommit(Object currentXid, Object xid);

   /**
    * Could not commit local tx
    *
    * @return The value
    */
   @Message(id = 1156, value = "Could not commit local transaction")
   public String couldNotCommitLocalTx();

   /**
    * Forget not supported in local tx
    *
    * @return The value
    */
   @Message(id = 1157, value = "Forget not supported in local transaction")
   public String forgetNotSupportedInLocalTx();

   /**
    * No recover with local-tx only resource managers
    *
    * @return The value
    */
   @Message(id = 1158,
         value = "No recovery for LocalTransaction only resource manager")
   public String noRecoverWithLocalTxResourceManagers();

   /**
    * Wrong xid in rollback
    *
    * @param currentXid current xid
    * @param xid        xid
    * @return The value
    */
   @Message(id = 1159, value = "Wrong xid in rollback: Expected: %s, Got: %s")
   public String wrongXidInRollback(Object currentXid, Object xid);

   /**
    * Could not rollback local tx
    *
    * @return The value
    */
   @Message(id = 1160, value = "Could not rollback local transaction")
   public String couldNotRollbackLocalTx();

}
