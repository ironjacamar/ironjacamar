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

package org.ironjacamar.common.metadata.ds;

import org.ironjacamar.common.metadata.common.CommonXML;

/**
 * XML
 */
interface XML extends CommonXML
{
   /**
    * jta
    */
   public static final String ATTRIBUTE_JTA = "jta";

   /**
    * major-version
    */
   public static final String ATTRIBUTE_MAJOR_VERSION = "major-version";

   /**
    * minor-version
    */
   public static final String ATTRIBUTE_MINOR_VERSION = "minor-version";

   /**
    * module
    */
   public static final String ATTRIBUTE_MODULE = "module";

   /**
    * spy
    */
   public static final String ATTRIBUTE_SPY = "spy";

   /**
    * check-valid-connection-sql
    */
   public static final String ELEMENT_CHECK_VALID_CONNECTION_SQL = "check-valid-connection-sql";

   /**
    * connection-listener
    */
   public static final String ELEMENT_CONNECTION_LISTENER = "connection-listener";

   /**
    * connection-property
    */
   public static final String ELEMENT_CONNECTION_PROPERTY = "connection-property";

   /**
    * connection-url
    */
   public static final String ELEMENT_CONNECTION_URL = "connection-url";

   /**
    * datasource
    */
   public static final String ELEMENT_DATASOURCE = "datasource";

   /**
    * datasources
    */
   public static final String ELEMENT_DATASOURCES = "datasources";

   /**
    * datasource-class
    */
   public static final String ELEMENT_DATASOURCE_CLASS = "datasource-class";

   /**
    * driver
    */
   public static final String ELEMENT_DRIVER = "driver";

   /**
    * drivers
    */
   public static final String ELEMENT_DRIVERS = "drivers";

   /**
    * driver-class
    */
   public static final String ELEMENT_DRIVER_CLASS = "driver-class";

   /**
    * exception-sorter
    */
   public static final String ELEMENT_EXCEPTION_SORTER = "exception-sorter";

   /**
    * new-connection-sql
    */
   public static final String ELEMENT_NEW_CONNECTION_SQL = "new-connection-sql";

   /**
    * password
    */
   public static final String ELEMENT_PASSWORD = "password";

   /**
    * prepared-statement-cache-size
    */
   public static final String ELEMENT_PREPARED_STATEMENT_CACHE_SIZE = "prepared-statement-cache-size";

   /**
    * query-timeout
    */
   public static final String ELEMENT_QUERY_TIMEOUT = "query-timeout";

   /**
    * reauth-plugin
    */
   public static final String ELEMENT_REAUTH_PLUGIN = "reauth-plugin";

   /**
    * set-tx-query-timeout
    */
   public static final String ELEMENT_SET_TX_QUERY_TIMEOUT = "set-tx-query-timeout";

   /**
    * share-prepared-statements
    */
   public static final String ELEMENT_SHARE_PREPARED_STATEMENTS = "share-prepared-statements";

   /**
    * stale-connection-checker
    */
   public static final String ELEMENT_STALE_CONNECTION_CHECKER = "stale-connection-checker";

   /**
    * statement
    */
   public static final String ELEMENT_STATEMENT = "statement";

   /**
    * track-statements
    */
   public static final String ELEMENT_TRACK_STATEMENTS = "track-statements";

   /**
    * transaction-isolation
    */
   public static final String ELEMENT_TRANSACTION_ISOLATION = "transaction-isolation";

   /**
    * url-delimiter
    */
   public static final String ELEMENT_URL_DELIMITER = "url-delimiter";

   /**
    * url-property
    */
   public static final String ELEMENT_URL_PROPERTY = "url-property";

   /**
    * url-selector-strategy-class-name
    */
   public static final String ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME = "url-selector-strategy-class-name";

   /**
    * use-try-lock
    */
   public static final String ELEMENT_USE_TRY_LOCK = "use-try-lock";

   /**
    * user-name
    */
   public static final String ELEMENT_USER_NAME = "user-name";

   /**
    * valid-connection-checker
    */
   public static final String ELEMENT_VALID_CONNECTION_CHECKER = "valid-connection-checker";

   /**
    * xa-datasource
    */
   public static final String ELEMENT_XA_DATASOURCE = "xa-datasource";

   /**
    * xa-datasource-class
    */
   public static final String ELEMENT_XA_DATASOURCE_CLASS = "xa-datasource-class";

   /**
    * xa-datasource-property
    */
   public static final String ELEMENT_XA_DATASOURCE_PROPERTY = "xa-datasource-property";
}
