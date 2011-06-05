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

package org.jboss.jca.core;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The core logger.
 *
 * Message ids ranging from 00001 to 05000 inclusively.
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
   @Message(id = 100, value = "Closing a connection for you.  Please close them yourself: %s")
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
            "Please close your own connections.")
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
   @Message(id = 302, value = "Unregistered handle that was not registered! %s" + 
         " for managedConnection: %s")
   public void unregisteredHandleNotRegistered(Object handle, Object managedConnection);
   
   /**
    * Unregistered a null handle for managedConnection
    * @param managedConnection The managedConnection instance
    */
   @LogMessage(level = INFO)
   @Message(id = 303, value = "Registered a null handle for managed connection: %s")
   public void unregisteredNullHandleManagedConnection(Object managedConnection);

   /**
    * Connection error occured
    * @param description throwable description
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 305, value = "Connection error occured %s")
   public void connectionErrorOccured(String description, @Cause Throwable t);
   
   /**
    * Unknown Connection error occured
    * @param description throwable description
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 306, value = "Unknown connection error occured %s")
   public void unknownConnectionErrorOccured(String description, @Cause Throwable t);
   
   
   /**
    * Notified of error on a different managed connection
    */
   @LogMessage(level = WARN)
   @Message(id = 307, value = "Notified of error on a different managed connection?")
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
   @Message(id = 313, value = "There is something wrong with the pooling?")
   public void somethingWrongWithPooling(@Cause Throwable t);
   
   
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
   @Message(id = 403, value = "Reconnecting a connection handle that still has a managedConnection! %s %s")
   public void reconnectingConnectionHandleHasManagedConnection(Object mc, Object connection);

   /**
    * Unchecked throwable in managedConnectionDisconnected()
    * @param cl The ConnectionListener name
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 404, value = "Unchecked throwable in managedConnectionDisconnected() cl= %s")
   public void uncheckedThrowableInManagedConnectionDisconnected(Object cl, @Cause Throwable t);
   
   
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
   @Message(id = 502, value = "Transaction %s error in before completion %s")
   public void transactionErrorInBeforeCompletion(Object transaction, Object synch, @Cause Throwable t);
   
   /**
    * Transaction error in after completion
    * @param transaction transaction
    * @param synch Synchronization 
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 503, value = "Transaction %s error in after completion %s")
   public void transactionErrorInAfterCompletion(Object transaction, Object synch, @Cause Throwable t);
}
