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

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * The adapters bundle.
 *
 * Message ids ranging from 030000 to 039999 inclusively.
 */
@MessageBundle(projectCode = "IJ")
public interface AdaptersBundle
{
   // BaseWrapperManagedConnectionFactory

   /**
    * Non-managed environment
    * @return The value
    */
   @Message(id = 31000, value = "Resource Adapter does not support running in a non-managed environment")
   public String nonManagedEnvironment();

   /**
    * Reauth plugin error
    * @return The value
    */
   @Message(id = 31001, value = "Error during loading reauth plugin")
   public String errorDuringLoadingReauthPlugin();

   /**
    * Connection listener plugin error
    * @return The value
    */
   @Message(id = 31002, value = "Error during loading connection listener plugin")
   public String errorDuringLoadingConnectionListenerPlugin();

   /**
    * Wrong CRI
    * @param clz The class
    * @return The value
    */
   @Message(id = 31003, value = "Wrong kind of ConnectionRequestInfo: %s")
   public String wrongConnectionRequestInfo(String clz);

   /**
    * No credentials in Subject
    * @return The value
    */
   @Message(id = 31004, value = "No matching credentials in Subject")
   public String noMatchingCredentials();

   // BaseWrapperManagedConnection

   /**
    * Wrong handle
    * @param clz The class
    * @return The value
    */
   @Message(id = 31010, value = "Wrong connection handle to associate: %s")
   public String wrongConnectionHandle(String clz);

   /**
    * Active locks
    * @return The value
    */
   @Message(id = 31011, value = "Still active locks")
   public String activeLocks();

   /**
    * Unable to obtain lock
    * @param seconds The seconds
    * @param o The object
    * @return The value
    */
   @Message(id = 31012, value = "Unable to obtain lock in %d seconds: %s")
   public String unableToObtainLock(int seconds, Object o);

   /**
    * Interrupted while lock
    * @param o The object
    * @return The value
    */
   @Message(id = 31013, value = "Interrupted attempting lock: %s")
   public String interruptedWhileLock(Object o);

   /**
    * Connection destroyed
    * @return The value
    */
   @Message(id = 31014, value = "Connection has been destroyed")
   public String connectionDestroyed();

   /**
    * Error during reauthentication
    * @return The value
    */
   @Message(id = 31015, value = "Error during reauthentication")
   public String errorDuringReauthentication();

   /**
    * Wrong credentials
    * @return The value
    */
   @Message(id = 31016, value = "Wrong credentials passed to getConnection")
   public String wrongCredentials();

   /**
    * Autocommit managed transaction
    * @return The value
    */
   @Message(id = 31017, value = "You cannot set autocommit during a managed transaction")
   public String autocommitManagedTransaction();

   /**
    * Readonly managed transaction
    * @return The value
    */
   @Message(id = 31018, value = "You cannot set read only during a managed transaction")
   public String readonlyManagedTransaction();

   /**
    * Commit managed transaction
    * @return The value
    */
   @Message(id = 31019, value = "You cannot commit during a managed transaction")
   public String commitManagedTransaction();

   /**
    * Commit autocommit
    * @return The value
    */
   @Message(id = 31020, value = "You cannot commit with autocommit set")
   public String commitAutocommit();

   /**
    * Rollback managed transaction
    * @return The value
    */
   @Message(id = 31021, value = "You cannot rollback during a managed transaction")
   public String rollbackManagedTransaction();

   /**
    * Rollback autocommit
    * @return The value
    */
   @Message(id = 31022, value = "You cannot rollback with autocommit set")
   public String rollbackAutocommit();

   // JBossWrapper

   /**
    * Not a wrapper for
    * @param clz The class
    * @return The value
    */
   @Message(id = 31030, value = "Not a wrapper for: %s")
   public String notWrapperFor(String clz);

   // WrappedConnection

   /**
    * Not associated
    * @param o The object
    * @return The value
    */
   @Message(id = 31040, value = "Connection is not associated with a managed connection: %s")
   public String connectionNotAssociated(Object o);

   /**
    * Connection closed
    * @return The value
    */
   @Message(id = 31041, value = "Connection handle has been closed and is unusable")
   public String connectionClosed();

   /**
    * Method not implemented
    * @return The value
    */
   @Message(id = 31042, value = "Method is not implemented by JDBC driver")
   public String methodNotImplemented();

   // WrappedResultSet

   /**
    * ResultSet closed
    * @return The value
    */
   @Message(id = 31050, value = "The result set is closed")
   public String resultSetClosed();

   // WrappedStatement

   /**
    * Statement closed
    * @return The value
    */
   @Message(id = 31060, value = "The statement is closed")
   public String statementClosed();

   // WrapperDataSource

   /**
    * Transaction cannot proceed
    * @param s The status
    * @return The value
    */
   @Message(id = 31070, value = "Transaction cannot proceed: %s")
   public String transactionCannotProceed(String s);

   // LocalManagedConnectionFactory

   /**
    * DriverClass null
    * @return The value
    */
   @Message(id = 31080, value = "DriverClass is undefined")
   public String driverClassNull();

   /**
    * ConnectionURL null
    * @return The value
    */
   @Message(id = 31081, value = "ConnectionURL is undefined")
   public String connectionURLNull();

   /**
    * Unable to create connection from datasource
    * @return The value
    */
   @Message(id = 31082, value = "Unable to create connection from datasource")
   public String unableToCreateConnectionFromDataSource();

   /**
    * Wrong driver class
    * @param clz The class
    * @param url The URL
    * @return The value
    */
   @Message(id = 31083, value = "Wrong driver class [%s] for this connection URL [%s]")
   public String wrongDriverClass(String clz, String url);

   /**
    * Unable to create connection
    * @return The value
    */
   @Message(id = 31084, value = "Unable to create connection")
   public String unableToCreateConnection();

   /**
    * Unable to create connection from URL
    * @param url The url
    * @return The value
    */
   @Message(id = 31085, value = "Unable to create connection from URL: %s")
   public String unableToCreateConnectionFromURL(String url);

   /**
    * No driver for url 
    * @param url The url
    * @return The value
    */
   @Message(id = 31086, value = "No DriverClass specified for URL: %s")
   public String noDriverClassForURL(String url);

   /**
    * Failed to register driver
    * @param url The url
    * @return The value
    */
   @Message(id = 31087, value = "Failed to register DriverClass for: %s")
   public String failedToRegisterDriverClass(String url);

   /**
    * DataSourceClass null
    * @return The value
    */
   @Message(id = 31088, value = "DataSourceClass is undefined")
   public String datasourceClassNull();

   /**
    * Failed to load datasource
    * @param clz The clz
    * @return The value
    */
   @Message(id = 31089, value = "Failed to load datasource: %s")
   public String failedToLoadDataSource(String clz);

   // LocalManagedConnection

   /**
    * LocalTransaction only
    * @return The value
    */
   @Message(id = 31090, value = "LocalTransaction only")
   public String localTransactionOnly();

   /**
    * LocalTransaction nested
    * @return The value
    */
   @Message(id = 31091, value = "Trying to begin a nested LocalTransaction")
   public String localTransactionNested();

   // XAManagedConnectionFactory

   /**
    * Unable to load connection properties
    * @return The value
    */
   @Message(id = 31100, value = "Could not load connection properties")
   public String unableToLoadConnectionProperties();

   /**
    * XADataSourceClass null
    * @return The value
    */
   @Message(id = 31101, value = "XADataSourceClass is undefined")
   public String xaDatasourceClassNull();

   /**
    * Failed to load xa datasource
    * @param clz The clz
    * @return The value
    */
   @Message(id = 31102, value = "Failed to load XA datasource: %s")
   public String failedToLoadXADataSource(String clz);
}

