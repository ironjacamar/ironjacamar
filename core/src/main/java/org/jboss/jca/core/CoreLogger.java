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

import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The core logger.
 *
 * Message ids ranging from 000000 to 009999 inclusively.
 */
@MessageLogger(projectCode = "IJ")
public interface CoreLogger extends BasicLogger
{

   // CACHED CONNECTION MANAGER (100)

   /**
    * Closing connection
    * @param handle The hande
    */
   @LogMessage(level = INFO)
   @Message(id = 100, value = "Closing a connection for you. Please close them yourself: %s")
   public void closingConnection(Object handle);

   /**
    * Closing connection
    * @param handle The hande
    * @param t The exception
    */
   @LogMessage(level = INFO)
   public void closingConnection(Object handle, @Cause Throwable t);

   /**
    * Closing connection results in throwable
    * @param t The exception
    */
   @LogMessage(level = INFO)
   @Message(id = 102, value = "Throwable trying to close a connection for you, please close it yourself")
   public void closingConnectionThrowable(@Cause Throwable t);

   /**
    * No close method for closing connection
    * @param clz The class name
    */
   @LogMessage(level = INFO)
   @Message(id = 103, value = "Could not find a close method on alleged connection object (%s). " +
            "Please close your own connections")
   public void closingConnectionNoClose(String clz);
   
   
   // WORK MANAGER (200)

   /**
    * SecurityContext setup failed
    * @param description throwable description
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 201, value = "SecurityContext setup failed: %s")
   public void securityContextSetupFailed(String description, @Cause Throwable t);
   
   /**
    * SecurityContext setup failed since CallbackSecurity was null
    */
   @LogMessage(level = ERROR)
   @Message(id = 202, value = "SecurityContext setup failed since CallbackSecurity was null")
   public void securityContextSetupFailedCallbackSecurityNull();
   
   
   // CONNECTION MANAGER LISTENER (300)

   /**
    * Registered a null handle for managedConnection
    * @param managedConnection The managedConnection instance
    */
   @LogMessage(level = INFO)
   @Message(id = 301, value = "Registered a null handle for managed connection: %s")
   public void registeredNullHandleManagedConnection(Object managedConnection);
   
   /**
    * Unregistered handle that was not registered
    * @param handle Unregistered handle
    * @param managedConnection The managedConnection instance
    */
   @LogMessage(level = INFO)
   @Message(id = 302, value = "Unregistered handle that was not registered: %s" + 
         " for managed connection: %s")
   public void unregisteredHandleNotRegistered(Object handle, Object managedConnection);
   
   /**
    * Unregistered a null handle for managedConnection
    * @param managedConnection The managedConnection instance
    */
   @LogMessage(level = INFO)
   @Message(id = 303, value = "Unregistered a null handle for managed connection: %s")
   public void unregisteredNullHandleManagedConnection(Object managedConnection);

   /**
    * Connection error occured
    * @param cl AbstractConnectionListener instance
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 305, value = "Connection error occured: %s")
   public void connectionErrorOccured(Object cl, @Cause Throwable t);
   
   /**
    * Unknown Connection error occured
    * @param cl AbstractConnectionListener instance
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 306, value = "Unknown connection error occured: %s")
   public void unknownConnectionErrorOccured(Object cl, @Cause Throwable t);
   
   /**
    * Notified of error on a different managed connection
    */
   @LogMessage(level = WARN)
   @Message(id = 307, value = "Notified of error on a different managed connection")
   public void notifiedErrorDifferentManagedConnection();

   /**
    * throwable from unregister connection
    * @param t The exception
    */
   @LogMessage(level = INFO)
   @Message(id = 311, value = "Throwable from unregister connection")
   public void throwableFromUnregisterConnection(@Cause Throwable t);

   /**
    * Error while closing connection handle
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 312, value = "Error while closing connection handle")
   public void errorWhileClosingConnectionHandle(@Cause Throwable t);

   /**
    * There is something wrong with the pooling
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 313, value = "There is something wrong with the pooling")
   public void somethingWrongWithPooling(@Cause Throwable t);
   
   /**
    * Error during beforeCompletion
    * @param cl AbstractConnectionListener instance
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 314, value = "Error during beforeCompletion: %s")
   public void beforeCompletionErrorOccured(Object cl, @Cause Throwable t);
         
   /**
    * Active handles
    * @param pool The name of the pool
    * @param number The number of active handles
    */
   @LogMessage(level = ERROR)
   @Message(id = 315, value = "Pool %s has %d active handles")
   public void activeHandles(String pool, int number);
         
   /**
    * Active handle
    * @param handle The handle
    * @param e The trace
    */
   @LogMessage(level = ERROR)
   @Message(id = 316, value = "Handle allocation: %s")
   public void activeHandle(Object handle, @Cause Exception e);
         
   /**
    * TxConnectionListener boundary
    * @param e The trace
    */
   @LogMessage(level = ERROR)
   @Message(id = 317, value = "Transaction boundary")
   public void txConnectionListenerBoundary(@Cause Exception e);
         
   /**
    * TxConnectionListener delisting failed
    * @param pool The name of the pool
    * @param e The trace
    */
   @LogMessage(level = ERROR)
   @Message(id = 318, value = "Delisting resource in pool %s failed")
   public void delistingFailed(String pool, @Cause Exception e);
         
   // CONNECTION MANAGER (400)

   /**
    * Error during tidy up connection
    * @param cl The ConnectionListener name
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 401, value = "Error during tidy up connection: %s")
   public void errorDuringTidyUpConnection(Object cl, @Cause Throwable t);
   
   /**
    * resourceException in returning connection
    * @param mc The ManagedConnection name
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 402, value = "ResourceException in returning connection: %s")
   public void resourceExceptionReturningConnection(Object mc, @Cause Throwable t);
   
   /**
    * reconnecting a connection handle that still has a managedConnection
    * @param mc The ManagedConnection name
    * @param connection connection object
    */
   @LogMessage(level = WARN)
   @Message(id = 403, value = "Reconnecting a connection handle that still has a managed connection: %s %s")
   public void reconnectingConnectionHandleHasManagedConnection(Object mc, Object connection);

   /**
    * Unchecked throwable in managedConnectionDisconnected()
    * @param cl The ConnectionListener name
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 404, value = "Unchecked throwable in managedConnectionDisconnected() cl=%s")
   public void uncheckedThrowableInManagedConnectionDisconnected(Object cl, @Cause Throwable t);
   
   /**
    * Multiple LocalTransaction connection listener enlisted
    * @param name The pool name
    * @param cl The ConnectionListener
    */
   @LogMessage(level = WARN)
   @Message(id = 405, value = "Multiple LocalTransaction connection listeners enlisted for %s, cl=%s")
   public void multipleLocalTransactionConnectionListenerEnlisted(String name, Object cl);
   
   /**
    * Throwable in returning connection
    * @param mc The ManagedConnection name
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 406, value = "Throwable in returning connection: %s")
   public void throwableReturningConnection(Object mc, @Cause Throwable t);

   /**
    * No lazy enlistment available
    * @param pool The pool name
    */
   @LogMessage(level = WARN)
   @Message(id = 407, value = "No lazy enlistment available for %s")
   public void noLazyEnlistmentAvailable(String pool);

   /**
    * Deprecated pool
    * @param oldPool The old pool name
    * @param newPool The new pool name
    */
   @LogMessage(level = WARN)
   @Message(id = 408, value = "Deprecated pool: %s, using %s instead")
   public void deprecatedPool(String oldPool, String newPool);

   // TRANSACTION SYNCHRONIZER (500)

   /**
    * Thread is not the enlisting thread
    * @param currentThread current thread
    * @param enlistingThread enlisting thread
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 501, value = "Thread %s is not the enlisting thread %s")
   public void threadIsnotEnlistingThread(Object currentThread, Object enlistingThread, @Cause Throwable t);
   
   /**
    * Transaction error in before completion
    * @param transaction transaction
    * @param synch Synchronization 
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 502, value = "Transaction %s error in beforeCompletion %s")
   public void transactionErrorInBeforeCompletion(Object transaction, Object synch, @Cause Throwable t);
   
   /**
    * Transaction error in after completion
    * @param transaction transaction
    * @param synch Synchronization 
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 503, value = "Transaction %s error in afterCompletion %s")
   public void transactionErrorInAfterCompletion(Object transaction, Object synch, @Cause Throwable t);
   
   /**
    * Transaction not found
    * @param transaction transaction
    */
   @LogMessage(level = WARN)
   @Message(id = 504, value = "Transaction not found: %s")
   public void transactionNotFound(Object transaction);
   

   // POOL MANAGER (600)

   /**
    * ConnectionValidator has been interrupted
    */
   @LogMessage(level = INFO)
   @Message(id = 601, value = "ConnectionValidator has been interrupted")
   public void returningConnectionValidatorInterrupted();
   
   /**
    * ConnectionValidator ignored unexpected runtime exception
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 602, value = "ConnectionValidator ignored unexpected runtime exception")
   public void connectionValidatorIgnoredUnexpectedRuntimeException(@Cause Throwable t);
   
   /**
    * ConnectionValidator ignored unexpected error
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 603, value = "ConnectionValidator ignored unexpected error")
   public void connectionValidatorIgnoredUnexpectedError(@Cause Throwable t);
   
   /**
    * Throwable while attempting to get a new connection
    * @param cl The ConnectionListener
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 604, value = "Throwable while attempting to get a new connection: %s")
   public void throwableWhileAttemptingGetNewGonnection(Object cl, @Cause Throwable t);

   /**
    * Destroying connection that could not be successfully matched
    * @param cl The ConnectionListener
    * @param mcf managed connection factory
    */
   @LogMessage(level = WARN)
   @Message(id = 605, value = "Destroying connection that could not be successfully matched %s for %s")
   public void destroyingConnectionNotSuccessfullyMatched(Object cl, Object mcf);
   
   /**
    * Throwable while trying to match ManagedConnection, destroying connection
    * @param cl The ConnectionListener
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 606, value = "Throwable while trying to match managed connection, destroying connection: %s")
   public void throwableWhileTryingMatchManagedConnection(Object cl, @Cause Throwable t);
   
   /**
    * ResourceException cleaning up ManagedConnection
    * @param cl The ConnectionListener
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 607, value = "ResourceException cleaning up managed connection: %s")
   public void resourceExceptionCleaningUpManagedConnection(Object cl, @Cause Throwable t);
   
   /**
    * Destroying returned connection, maximum pool size exceeded
    * @param cl The ConnectionListener
    */
   @LogMessage(level = WARN)
   @Message(id = 608, value = "Destroying returned connection, maximum pool size exceeded %s")
   public void destroyingReturnedConnectionMaximumPoolSizeExceeded(Object cl);
   
   /**
    * Attempt to return connection twice
    * @param cl The ConnectionListener
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 609, value = "Attempt to return connection twice: %s")
   public void attemptReturnConnectionTwice(Object cl, @Cause Throwable t);
   
   /**
    * Unable to fill pool
    * @param t The exception
    * @param jndiName the jndi-name
    */
   @LogMessage(level = WARN)
   @Message(id = 610, value = "Unable to fill pool: %s")
   public void unableFillPool(@Cause Throwable t, String jndiName);
   
   /**
    * Background validation was specified with a non compliant ManagedConnectionFactory interface
    */
   @LogMessage(level = WARN)
   @Message(id = 611, value = "Warning: Background validation was specified with a non compliant " +
         "ManagedConnectionFactory interface")
   public void backgroundValidationNonCompliantManagedConnectionFactory();
   
   /**
    * Destroying connection that could not be successfully matched
    * @param cl The ConnectionListener
    */
   @LogMessage(level = WARN)
   @Message(id = 612, value = "Destroying connection that could not be successfully matched: %s")
   public void destroyingConnectionNotSuccessfullyMatched(Object cl);
   
   /**
    * Throwable while trying to match ManagedConnection, destroying connection
    * @param cl The ConnectionListener
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 613, value = "Throwable while trying to match managed connection, destroying connection: %s")
   public void throwableWhileTryingMatchManagedConnectionThenDestroyingConnection(Object cl, @Cause Throwable t);

   /**
    * Exception during createSubject()
    * @param jndiName The jndi-name
    * @param description throwable description
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 614, value = "Exception during createSubject() for %s: %s")
   public void exceptionDuringCreateSubject(String jndiName, String description, @Cause Throwable t);

   /**
    * Going to destroy connection listener during shutdown
    * @param poolName The pool name
    * @param mc The managed connection
    */
   @LogMessage(level = WARN)
   @Message(id = 615, value = "Destroying active connection in pool: %s (%s)")
   public void destroyingActiveConnection(String poolName, Object mc);
   
   /**
    * Connection leak
    * @param poolName The pool name
    * @param id The connection listener id
    * @param time The timestamp
    * @param t The trace
    */
   @LogMessage(level = ERROR)
   @Message(id = 616, value = "Leak detected in pool: %s (%s) (%d)")
   public void connectionLeak(String poolName, String id, long time, @Cause Throwable t);
   
   /**
    * Invalid incrementer policy
    * @param clz The class name
    */
   @LogMessage(level = WARN)
   @Message(id = 617, value = "Invalid incrementer capacity policy: %s")
   public void invalidCapacityIncrementer(String clz);
   
   /**
    * Invalid decrementer policy
    * @param clz The class name
    */
   @LogMessage(level = WARN)
   @Message(id = 618, value = "Invalid decrementer capacity policy: %s")
   public void invalidCapacityDecrementer(String clz);
   
   /**
    * Invalid policy option
    * @param key The property name
    * @param value The property value
    * @param policy The class name
    */
   @LogMessage(level = WARN)
   @Message(id = 619, value = "Invalid property '%s' with value '%s' for %s")
   public void invalidCapacityOption(String key, String value, String policy);
   
   /**
    * ValidateOnMatch was specified with a non compliant ManagedConnectionFactory interface
    * @param mcf The ManagedConnectionFactory
    */
   @LogMessage(level = WARN)
   @Message(id = 620, value = "Warning: ValidateOnMatch validation was specified with a non compliant " +
            "ManagedConnectionFactory: %s")
   public void validateOnMatchNonCompliantManagedConnectionFactory(String mcf);
   
   /**
    * Destroying connection that could not be validated
    * @param cl The ConnectionListener
    */
   @LogMessage(level = WARN)
   @Message(id = 621, value = "Destroying connection that could not be validated: %s")
   public void destroyingConnectionNotValidated(Object cl);

   /**
    * Unsupported pool implementation
    * @param clz The pool
    */
   @LogMessage(level = WARN)
   @Message(id = 622, value = "Unsupported pool implementation: %s")
   public void unsupportedPoolImplementation(String clz);

   // NAMING (700)

   /**
    * Exception during unbind
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 701, value = "Exception during unbind")
   public void exceptionDuringUnbind(@Cause Throwable t);
   
   
   // RESOURCE ADPATER REPOSITORY (800)
   
   
   // RECOVERY (900)

   /**
    * Error during connection close
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 901, value = "Error during connection close")
   public void exceptionDuringConnectionClose(@Cause Throwable t);

   /**
    * Error during inflow crash recovery
    * @param rar The resource adapter class name
    * @param as The activation spec
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 902, value = "Error during inflow crash recovery for '%s' (%s)")
   public void exceptionDuringCrashRecoveryInflow(String rar, Object as, @Cause Throwable t);
   
   /**
    * Error creating Subject for crash recovery
    * @param jndiName The JNDI name
    * @param reason The reason
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 903, value = "Error creating Subject for crash recovery: %s (%s)")
   public void exceptionDuringCrashRecoverySubject(String jndiName, String reason, @Cause Throwable t);
   
   /**
    * No security domain defined for crash recovery
    * @param jndiName The JNDI name
    */
   @LogMessage(level = WARN)
   @Message(id = 904, value = "No security domain defined for crash recovery: %s")
   public void noCrashRecoverySecurityDomain(String jndiName);

   /**
    * Subject for crash recovery was null
    * @param jndiName The JNDI name
    */
   @LogMessage(level = WARN)
   @Message(id = 905, value = "Subject for crash recovery was null: %s")
   public void nullSubjectCrashRecovery(String jndiName);

   /**
    * Error during crash recovery
    * @param jndiName The JNDI name
    * @param reason The reason
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 906, value = "Error during crash recovery: %s (%s)")
   public void exceptionDuringCrashRecovery(String jndiName, String reason, @Cause Throwable t);

   // SECURITY (1000)

   /**
    * No users.properties were found
    */
   @LogMessage(level = WARN)
   @Message(id = 1001, value = "No users.properties were found")
   public void noUsersPropertiesFound();

   /**
    * Error while loading users.properties
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 1002, value = "Error while loading users.properties")
   public void errorWhileLoadingUsersProperties(@Cause Throwable t);
   
   /**
    * No roles.properties were found
    */
   @LogMessage(level = WARN)
   @Message(id = 1003, value = "No roles.properties were found")
   public void noRolesPropertiesFound();

   /**
    * Error while loading roles.properties
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 1004, value = "Error while loading roles.properties")
   public void errorWhileLoadingRolesProperties(@Cause Throwable t);
   
   /**
    * No callback.properties were found
    */
   @LogMessage(level = WARN)
   @Message(id = 1005, value = "No callback.properties were found")
   public void noCallbackPropertiesFound();

   /**
    * Error while loading callback.properties
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 1006, value = "Error while loading callback.properties")
   public void errorWhileLoadingCallbackProperties(@Cause Throwable t);
   
   // TRANSCATION (1100)

   /**
    * Prepare called on a local tx
    */
   @LogMessage(level = WARN)
   @Message(id = 1101, value = "Prepare called on a local tx. Use of local transactions on a JTA " +
         "transaction with more than one branch may result in inconsistent data in some cases of failure")
   public void prepareCalledOnLocaltx();
   
   
   // METADATA REPOSITORY (1200)
}
