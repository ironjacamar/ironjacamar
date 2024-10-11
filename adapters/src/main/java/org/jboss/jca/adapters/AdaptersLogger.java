/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The adapters logger.
 *
 * Message ids ranging from 030000 to 039999 inclusively.
 */
@MessageLogger(projectCode = "IJ")
public interface AdaptersLogger extends BasicLogger
{
   // BaseWrapperManagedConnectionFactory

   /**
    * Unable to load connection listener
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30000, value = "Unable to load connection listener: %s")
   public void unableToLoadConnectionListener(String msg, @Cause Throwable t);

   /**
    * Disable exception sorter
    * @param jndi The JNDI name
    */
   @LogMessage(level = WARN)
   @Message(id = 30001, value = "Disabling exception sorter for: %s")
   public void disableExceptionSorter(String jndi);

   /**
    * Disable exception sorter
    * @param jndi The JNDI name
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30002, value = "Disabling exception sorter for: %s")
   public void disableExceptionSorterExt(String jndi, @Cause Throwable t);

   /**
    * Error during exception sorter
    * @param jndi The JNDI name
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30003, value = "Error checking exception fatality for: %s")
   public void errorDuringExceptionSorter(String jndi, @Cause Throwable t);

   /**
    * Disable validation checker
    * @param jndi The JNDI name
    */
   @LogMessage(level = WARN)
   @Message(id = 30004, value = "Disabling validation connection checker for: %s")
   public void disableValidationChecker(String jndi);

   /**
    * Disable validation checker
    * @param jndi The JNDI name
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30005, value = "Disabling validation connection checker for: %s")
   public void disableValidationCheckerExt(String jndi, @Cause Throwable t);

   /**
    * Disable stale checker
    * @param jndi The JNDI name
    */
   @LogMessage(level = WARN)
   @Message(id = 30006, value = "Disabling stale connection checker for: %s")
   public void disableStaleChecker(String jndi);

   /**
    * Disable stale checker
    * @param jndi The JNDI name
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30007, value = "Disabling stale connection checker for: %s")
   public void disableStaleCheckerExt(String jndi, @Cause Throwable t);

   /**
    * HA detected
    * @param jndi The JNDI name
    */
   @LogMessage(level = WARN)
   @Message(id = 30008, value = "HA setup detected for %s")
   public void haDetected(String jndi);

   // BaseWrapperManagedConnection

   /**
    * Queued thread name
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30020, value = "Detected queued threads during cleanup from: %s")
   public void queuedThreadName(String msg, @Cause Throwable t);

   /**
    * Queued thread
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30021, value = "Queued thread: %s")
   public void queuedThread(String msg, @Cause Throwable t);

   /**
    * Lock owned
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30022, value = "Lock owned during cleanup: %s")
   public void lockOwned(String msg, @Cause Throwable t);

   /**
    * Lock owned w/o owner
    */
   @LogMessage(level = WARN)
   @Message(id = 30023, value = "Lock is locked during cleanup without an owner")
   public void lockOwnedWithoutOwner();

   /**
    * Transaction isolation reset
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30024, value = "Error resetting transaction isolation for: %s")
   public void transactionIsolationReset(String msg, @Cause Throwable t);

   /**
    * Error during connection listener activation
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30025, value = "Error during connection listener activation for: %s")
   public void errorDuringConnectionListenerActivation(String msg, @Cause Throwable t);

   /**
    * Error during connection listener passivation
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30026, value = "Error during connection listener passivation for: %s")
   public void errorDuringConnectionListenerPassivation(String msg, @Cause Throwable t);

   /**
    * Invalid connection
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30027, value = "Destroying connection that is not valid, due to the following exception: %s")
   public void invalidConnection(String msg, @Cause Throwable t);

   /**
    * Error notifying a connection listener
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30028, value = "Error notifying of connection error for listener: %s")
   public void errorNotifyingConnectionListener(String msg, @Cause Throwable t);


   // WrappedConnection

   /**
    * Closing statement
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30040, value = "Closing a statement you left open, please do your own housekeeping for: %s")
   public void closingStatement(String msg, @Cause Throwable t);

   /**
    * Error during closing statement
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30041, value = "Error during closing a statement for: %s")
   public void errorDuringClosingStatement(String msg, @Cause Throwable t);

   /**
    * Closing result set
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30042, value = "Closing a result set you left open, please do your own housekeeping for: %s")
   public void closingResultSet(String msg, @Cause Throwable t);

   /**
    * Error during closing result set
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30043, value = "Error during closing a result set for: %s")
   public void errorDuringClosingResultSet(String msg, @Cause Throwable t);

   // LocalManagedConnectionFactory

   /**
    * Error creating connection
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30050, value = "Error creating connection for: %s")
   public void errorCreatingConnection(String msg, @Cause Throwable t);

   /**
    * Undefined URLSelectStrategy
    * @param msg The message
    */
   @LogMessage(level = ERROR)
   @Message(id = 30051, value = "Unable to load undefined URLSelectStrategy for: %s")
   public void undefinedURLSelectStrategy(String msg);

   /**
    * Error URLSelectStrategy
    * @param msg The message
    * @param jndi The JNDI
    */
   @LogMessage(level = ERROR)
   @Message(id = 30052, value = "Unable to load %s URLSelectStrategy for: %s")
   public void errorURLSelectStrategy(String msg, String jndi);

   /**
    * Error URLSelectStrategy
    * @param msg The message
    * @param jndi The JNDI
    * @param t The throwable
    */
   @LogMessage(level = ERROR)
   @Message(id = 30053, value = "Unable to load %s URLSelectStrategy for: %s")
   public void errorURLSelectStrategyExt(String msg, String jndi, @Cause Throwable t);

   // XAManagedConnectionFactory

   /**
    * Error creating XA connection
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30054, value = "Error creating XA connection for: %s")
   public void errorCreatingXAConnection(String msg, @Cause Throwable t);

   /**
    * Undefined URLXASelectStrategy
    * @param msg The message
    */
   @LogMessage(level = ERROR)
   @Message(id = 30055, value = "Unable to load undefined URLXASelectStrategy for: %s")
   public void undefinedURLXASelectStrategy(String msg);

   /**
    * Error URLXASelectStrategy
    * @param msg The message
    * @param jndi The JNDI
    */
   @LogMessage(level = ERROR)
   @Message(id = 30056, value = "Unable to load %s URLXASelectStrategy for: %s")
   public void errorURLXASelectStrategy(String msg, String jndi);

   /**
    * Error URLXASelectStrategy
    * @param msg The message
    * @param jndi The JNDI
    * @param t The throwable
    */
   @LogMessage(level = ERROR)
   @Message(id = 30057, value = "Unable to load %s URLXASelectStrategy for: %s")
   public void errorURLXASelectStrategyExt(String msg, String jndi, @Cause Throwable t);

   // XAManagedConnection

   /**
    * Error checking state
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30060, value = "Error checking state")
   public void errorCheckingState(@Cause Throwable t);


   /**
    * Auto commit reset
    * @param msg The message
    * @param t The throwable
    */
   @LogMessage(level = WARN)
   @Message(id = 30061, value = "Error resetting auto-commit for: %s")
   public void errorResettingAutoCommit(String msg, @Cause Throwable t);

   @LogMessage(level = WARN)
   @Message(id = 30062, value = "Error during prepared statement cache flushing")
   public void errorDuringPreparedStatementCacheFlushing(@Cause Throwable t);
}
