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
@MessageLogger(projectCode = "IJ2")
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
    * Connection error occurred
    * @param cl AbstractConnectionListener instance
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 305, value = "Connection error occurred: %s")
   public void connectionErrorOccurred(Object cl, @Cause Throwable t);

   /**
    * Unknown Connection error occurred
    * @param cl AbstractConnectionListener instance
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 306, value = "Unknown connection error occurred: %s")
   public void unknownConnectionErrorOccurred(Object cl, @Cause Throwable t);

   /**
    * Notified of error on a different managed connection
    */
   @LogMessage(level = WARN)
   @Message(id = 307, value = "Notified of error on a different managed connection")
   public void notifiedErrorDifferentManagedConnection();


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
    * Unable to fill pool
    * @param id the id of the pool
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 610, value = "Unable to fill pool id=%s")
   public void unableFillPool(String id, @Cause Throwable t);

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

   // NAMING (700)

   /**
    * Exception during unbind
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 701, value = "Exception during unbind")
   public void exceptionDuringUnbind(@Cause Throwable t);

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

}
