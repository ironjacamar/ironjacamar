/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.Driver;
import org.jboss.jca.common.api.metadata.ds.DsPool;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.DsXaPool;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;
import org.jboss.jca.common.metadata.common.AbstractParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * A datasource parser
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DsParser extends AbstractParser implements MetadataParser<DataSources>
{
   /**
    * Parse a -ds.xml file
    * @param xmlInputStream the input stream
    * @return The datasource definitions
    * @exception Exception Thrown if an error occurs
    */
   public DataSources parse(InputStream xmlInputStream) throws Exception
   {
      XMLStreamReader reader = null;

      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      reader = inputFactory.createXMLStreamReader(xmlInputStream);
      try
      {
         return parse(reader);
      }
      finally
      {
         if (reader != null)
            reader.close();
      }
   }

   /**
    * Parse a -ds.xml file
    * @param reader The reader
    * @return The datasource definitions
    * @exception Exception Thrown if an error occurs
    */
   public DataSources parse(XMLStreamReader reader) throws Exception
   {
      DataSources dataSources = null;

      //iterate over tags
      int iterate;
      try
      {
         iterate = reader.nextTag();
      }
      catch (XMLStreamException e)
      {
         //founding a non tag..go on. Normally non-tag found at beginning are comments or DTD declaration
         iterate = reader.nextTag();
      }
      switch (iterate)
      {
         case END_ELEMENT : {
            // should mean we're done, so ignore it.
            break;
         }
         case START_ELEMENT : {

            switch (Tag.forName(reader.getLocalName()))
            {
               case DATASOURCES : {
                  dataSources = parseDataSources(reader);
                  break;
               }
               default :
                  throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
            }

            break;
         }
         default :
            throw new IllegalStateException();
      }

      return dataSources;

   }

   /**
    * Parse datasource
    * @param reader The reader
    * @return The result
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected DataSources parseDataSources(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      List<org.jboss.jca.common.api.metadata.ds.DataSource> datasource =
         new ArrayList<org.jboss.jca.common.api.metadata.ds.DataSource>();
      List<org.jboss.jca.common.api.metadata.ds.XaDataSource> xaDataSource =
         new ArrayList<org.jboss.jca.common.api.metadata.ds.XaDataSource>();
      Map<String, Driver> drivers = new HashMap<String, Driver>();
      boolean driversMatched = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.DATASOURCES)
               {
                  return new DatasourcesImpl(datasource, xaDataSource, drivers);
               }
               else
               {
                  if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (DataSources.Tag.forName(reader.getLocalName()))
               {
                  case DATASOURCE : {
                     datasource.add(parseDataSource(reader));
                     break;
                  }
                  case XA_DATASOURCE : {
                     xaDataSource.add(parseXADataSource(reader));
                     break;
                  }
                  case DRIVERS : {
                     driversMatched = true;
                     break;
                  }
                  case DRIVER : {
                     Driver driver = parseDriver(reader);
                     drivers.put(driver.getName(), driver);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * Parse driver
    * @param reader The reader
    * @return The result
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected Driver parseDriver(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      String driverClass = null;
      String dataSourceClass = null;
      String xaDataSourceClass = null;

      //attributes reading

      String name = null;
      Integer majorVersion = null;
      Integer minorVersion = null;
      String module = null;

      for (org.jboss.jca.common.api.metadata.ds.Driver.Attribute attribute : Driver.Attribute.values())
      {
         switch (attribute)
         {

            case NAME : {
               name = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case MAJOR_VERSION : {
               majorVersion = attributeAsInt(reader, attribute.getLocalName());
               break;
            }
            case MINOR_VERSION : {
               minorVersion = attributeAsInt(reader, attribute.getLocalName());
               break;
            }
            case MODULE : {
               module = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            default :
               break;
         }
      }

      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.DRIVER)
               {

                  return new DriverImpl(name, majorVersion, minorVersion, module,
                                        driverClass, dataSourceClass, xaDataSourceClass);
               }
               else
               {
                  if (Driver.Tag.forName(reader.getLocalName()) == Driver.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Driver.Tag.forName(reader.getLocalName()))
               {
                  case DATASOURCE_CLASS : {
                     dataSourceClass = elementAsString(reader);
                     break;
                  }
                  case XA_DATASOURCE_CLASS : {
                     xaDataSourceClass = elementAsString(reader);
                     break;
                  }
                  case DRIVER_CLASS : {
                     driverClass = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }


   /**
    * Parse security
    * @param reader The reader
    * @return The result
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected DsSecurity parseDsSecurity(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      String userName = null;
      String password = null;
      String securityDomain = null;
      Extension reauthPlugin = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (org.jboss.jca.common.api.metadata.ds.DataSource.Tag.forName(reader.getLocalName()) ==
                   org.jboss.jca.common.api.metadata.ds.DataSource.Tag.SECURITY)
               {

                  return new DsSecurityImpl(userName, password, securityDomain, reauthPlugin);
               }
               else
               {
                  if (DsSecurity.Tag.forName(reader.getLocalName()) == DsSecurity.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               DsSecurity.Tag tag = DsSecurity.Tag.forName(reader.getLocalName());
               switch (tag)
               {
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case USER_NAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  case REAUTH_PLUGIN : {
                     reauthPlugin = parseExtension(reader, tag.getLocalName());
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }


   /**
    * Parse validation
    * @param reader The reader
    * @return The result
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected Validation parseValidationSetting(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      Boolean validateOnMatch = Defaults.VALIDATE_ON_MATCH;
      Boolean useFastFail = Defaults.USE_CCM;
      Long backgroundValidationMillis = null;
      Extension staleConnectionChecker = null;
      Boolean backgroundValidation = Defaults.BACKGROUND_VALIDATION;
      String checkValidConnectionSql = null;
      Extension validConnectionChecker = null;
      Extension exceptionSorter = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (org.jboss.jca.common.api.metadata.ds.DataSource.Tag.forName(reader.getLocalName()) ==
                   org.jboss.jca.common.api.metadata.ds.DataSource.Tag.VALIDATION)
               {

                  return new ValidationImpl(backgroundValidation, backgroundValidationMillis, useFastFail,
                                            validConnectionChecker, checkValidConnectionSql, validateOnMatch,
                                            staleConnectionChecker, exceptionSorter);

               }
               else
               {
                  if (Validation.Tag.forName(reader.getLocalName()) == Validation.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               Validation.Tag currTag = Validation.Tag.forName(reader.getLocalName());
               switch (currTag)
               {
                  case BACKGROUND_VALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader);
                     break;
                  }
                  case CHECK_VALID_CONNECTION_SQL : {
                     checkValidConnectionSql = elementAsString(reader);
                     break;
                  }
                  case EXCEPTION_SORTER : {
                     exceptionSorter = parseExtension(reader, currTag.getLocalName());
                     break;
                  }
                  case STALE_CONNECTION_CHECKER : {
                     staleConnectionChecker = parseExtension(reader, currTag.getLocalName());
                     break;
                  }
                  case USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  case VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader);
                     break;
                  }
                  case VALID_CONNECTION_CHECKER : {
                     validConnectionChecker = parseExtension(reader, currTag.getLocalName());
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * Parse timeout
    * @param reader The reader
    * @return The result
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected TimeOut parseTimeOutSettings(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      Long blockingTimeoutMillis = null;
      Long idleTimeoutMinutes = null;
      Boolean setTxQuertTimeout = Defaults.SET_TX_QUERY_TIMEOUT;
      Long queryTimeout = null;
      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Long useTryLock = null;
      Integer xaResourceTimeout = null;
      Long validationQueryTimeout = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (org.jboss.jca.common.api.metadata.ds.DataSource.Tag.forName(reader.getLocalName()) ==
                   org.jboss.jca.common.api.metadata.ds.DataSource.Tag.TIMEOUT)
               {

                  return new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                                         allocationRetryWaitMillis, xaResourceTimeout, setTxQuertTimeout,
                                         queryTimeout, useTryLock, validationQueryTimeout);
               }
               else
               {
                  if (TimeOut.Tag.forName(reader.getLocalName()) == TimeOut.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (TimeOut.Tag.forName(reader.getLocalName()))
               {
                  case ALLOCATION_RETRY : {
                     allocationRetry = elementAsInteger(reader);
                     break;
                  }
                  case ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case BLOCKING_TIMEOUT_MILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case IDLE_TIMEOUT_MINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  case QUERY_TIMEOUT : {
                     queryTimeout = elementAsLong(reader);
                     break;
                  }
                  case SET_TX_QUERY_TIMEOUT : {
                     setTxQuertTimeout = elementAsBoolean(reader);
                     break;
                  }
                  case USE_TRY_LOCK : {
                     useTryLock = elementAsLong(reader);
                     break;
                  }
                  case XA_RESOURCE_TIMEOUT : {
                     xaResourceTimeout = elementAsInteger(reader);
                     break;
                  }
                  case VALIDATION_QUERY_TIMEOUT: {
                     validationQueryTimeout = elementAsLong(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * Parse statement
    * @param reader The reader
    * @return The result
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected Statement parseStatementSettings(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      Long preparedStatementsCacheSize = null;
      Boolean sharePreparedStatements = Defaults.SHARE_PREPARED_STATEMENTS;
      TrackStatementsEnum trackStatements = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (org.jboss.jca.common.api.metadata.ds.DataSource.Tag.forName(reader.getLocalName()) ==
                   org.jboss.jca.common.api.metadata.ds.DataSource.Tag.STATEMENT)
               {

                  return new StatementImpl(sharePreparedStatements, preparedStatementsCacheSize, trackStatements);
               }
               else
               {
                  if (Statement.Tag.forName(reader.getLocalName()) == Statement.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Statement.Tag.forName(reader.getLocalName()))
               {
                  case PREPARED_STATEMENT_CACHE_SIZE : {
                     preparedStatementsCacheSize = elementAsLong(reader);
                     break;
                  }
                  case TRACK_STATEMENTS : {
                     String elementString = elementAsString(reader);
                     trackStatements = TrackStatementsEnum.valueOf(elementString == null ? "FALSE" : elementString
                        .toUpperCase(Locale.US));
                     break;
                  }
                  case SHARE_PREPARED_STATEMENTS : {
                     sharePreparedStatements = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    *
    * parse a {@link CommonPool} object
    *
    * @param reader reader
    * @return the parsed {@link CommonPool} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    * @throws ValidateException ValidateException
    */
   protected DsPool parsePool(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      Boolean useStrictMin = Defaults.USE_STRICT_MIN;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Boolean allowMultipleUsers = Defaults.ALLOW_MULTIPLE_USERS;
      Capacity capacity = null;
      Boolean fair = Defaults.FAIR;
      Extension connectionListener = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (org.jboss.jca.common.api.metadata.ds.DataSource.Tag.forName(reader.getLocalName()) ==
                   org.jboss.jca.common.api.metadata.ds.DataSource.Tag.POOL)
               {
                  return new DsPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy,
                                        allowMultipleUsers, capacity, fair, connectionListener);
               }
               else
               {
                  if (DsPool.Tag.forName(reader.getLocalName()) == DsPool.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               DsPool.Tag tag = DsPool.Tag.forName(reader.getLocalName());
               switch (tag)
               {
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case FAIR : {
                     fair = elementAsBoolean(reader);
                     break;
                  }
                  case USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case ALLOW_MULTIPLE_USERS : {
                     allowMultipleUsers = Boolean.TRUE;
                     break;
                  }
                  case CAPACITY : {
                     capacity = parseCapacity(reader);
                     break;
                  }
                  case CONNECTION_LISTENER : {
                     connectionListener = parseExtension(reader, tag.getLocalName());
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
   *
   * parse a {@link CommonXaPool} object
   *
   * @param reader reader
   * @return the parsed {@link CommonXaPool} object
   * @throws XMLStreamException XMLStreamException
   * @throws ParserException ParserException
    * @throws ValidateException ValidateException
   */
   protected DsXaPool parseXaPool(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Boolean allowMultipleUsers = Defaults.ALLOW_MULTIPLE_USERS;
      Capacity capacity = null;
      Boolean fair = Defaults.FAIR;
      Extension connectionListener = null;
      Boolean interleaving = Defaults.INTERLEAVING;
      Boolean isSameRmOverride = Defaults.IS_SAME_RM_OVERRIDE;
      Boolean padXid = Defaults.PAD_XID;
      Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;
      Boolean wrapXaDataSource = Defaults.WRAP_XA_RESOURCE;
      Boolean useStrictMin = Defaults.USE_STRICT_MIN;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (org.jboss.jca.common.api.metadata.ds.XaDataSource.Tag.forName(reader.getLocalName()) ==
                   org.jboss.jca.common.api.metadata.ds.XaDataSource.Tag.XA_POOL)
               {
                  return new DsXaPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin,
                                          flushStrategy, isSameRmOverride, interleaving, padXid,
                                          wrapXaDataSource, noTxSeparatePool, allowMultipleUsers, capacity,
                                          fair, connectionListener);
               }
               else
               {
                  if (DsXaPool.Tag.forName(reader.getLocalName()) == DsXaPool.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               DsXaPool.Tag tag = DsXaPool.Tag.forName(reader.getLocalName());
               switch (tag)
               {
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case INTERLEAVING : {
                     interleaving = elementAsBoolean(reader);
                     break;
                  }
                  case IS_SAME_RM_OVERRIDE : {
                     isSameRmOverride = elementAsBoolean(reader);
                     break;
                  }
                  case NO_TX_SEPARATE_POOLS : {
                     noTxSeparatePool = elementAsBoolean(reader);
                     break;
                  }
                  case PAD_XID : {
                     padXid = elementAsBoolean(reader);
                     break;
                  }
                  case WRAP_XA_RESOURCE : {
                     wrapXaDataSource = elementAsBoolean(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case FAIR : {
                     fair = elementAsBoolean(reader);
                     break;
                  }
                  case USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case ALLOW_MULTIPLE_USERS : {
                     allowMultipleUsers = Boolean.TRUE;
                     break;
                  }
                  case CAPACITY : {
                     capacity = parseCapacity(reader);
                     break;
                  }
                  case CONNECTION_LISTENER : {
                     connectionListener = parseExtension(reader, tag.getLocalName());
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * Parse a XA datasource
    * @param reader The reader
    * @return The XA datasource
    * @exception XMLStreamException Thrown if a stream error occurs
    * @exception ParserException Thrown if a parser error occurs
    * @exception ValidateException Thrown if a validation error occurs
    */
   protected org.jboss.jca.common.api.metadata.ds.XaDataSource
   parseXADataSource(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      TransactionIsolation transactionIsolation = null;
      Map<String, String> xaDataSourceProperty = new HashMap<String, String>();
      TimeOut timeOutSettings = null;
      DsSecurity securitySettings = null;
      Statement statementSettings = null;
      Validation validationSettings = null;
      String urlDelimiter = null;
      String urlProperty = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;
      DsXaPool xaPool = null;
      Recovery recovery = null;

      String xaDataSourceClass = null;
      String driver = null;

      //attributes reading

      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String poolName = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      Boolean spy = Defaults.SPY;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean connectable = Defaults.CONNECTABLE;
      Boolean tracking = Defaults.TRACKING;
      String mcp = Defaults.MCP;
      Boolean enlistmentTrace = Defaults.ENLISTMENT_TRACE;

      for (org.jboss.jca.common.api.metadata.ds.XaDataSource.Attribute attribute :
              org.jboss.jca.common.api.metadata.ds.XaDataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.ENABLED);
               break;
            }
            case JNDI_NAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USE_JAVA_CONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.USE_JAVA_CONTEXT);
               break;
            }
            case SPY : {
               spy = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.SPY);
               break;
            }
            case USE_CCM : {
               useCcm = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.USE_CCM);
               break;
            }
            case CONNECTABLE : {
               connectable = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.CONNECTABLE);
               break;
            }
            case TRACKING : {
               tracking = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.TRACKING);
               break;
            }
            case MCP : {
               mcp = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case ENLISTMENT_TRACE : {
               enlistmentTrace = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.ENLISTMENT_TRACE);
               break;
            }
            default :
               break;
         }
      }

      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.XA_DATASOURCE)
               {

                  return new XADataSourceImpl(transactionIsolation, timeOutSettings, securitySettings,
                                              statementSettings, validationSettings, urlDelimiter, urlProperty,
                                              urlSelectorStrategyClassName, useJavaContext, poolName, enabled,
                                              jndiName, spy, useCcm, connectable, tracking, mcp, enlistmentTrace,
                                              xaDataSourceProperty,
                                              xaDataSourceClass, driver, newConnectionSql, xaPool, recovery);
               }
               else
               {
                  if (org.jboss.jca.common.api.metadata.ds.XaDataSource.Tag.forName(reader.getLocalName()) ==
                      org.jboss.jca.common.api.metadata.ds.XaDataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (org.jboss.jca.common.api.metadata.ds.XaDataSource.Tag.forName(reader.getLocalName()))
               {
                  case XA_DATASOURCE_PROPERTY : {
                     parseConfigProperty(xaDataSourceProperty, reader);
                     break;
                  }
                  case XA_DATASOURCE_CLASS : {
                     xaDataSourceClass = elementAsString(reader);
                     break;
                  }
                  case DRIVER : {
                     driver = elementAsString(reader);
                     break;
                  }
                  case XA_POOL : {
                     xaPool = parseXaPool(reader);
                     break;
                  }
                  case NEW_CONNECTION_SQL : {
                     newConnectionSql = elementAsString(reader);
                     break;
                  }
                  case URL_DELIMITER : {
                     urlDelimiter = elementAsString(reader);
                     break;
                  }
                  case URL_PROPERTY : {
                     urlProperty = elementAsString(reader);
                     break;
                  }
                  case URL_SELECTOR_STRATEGY_CLASS_NAME : {
                     urlSelectorStrategyClassName = elementAsString(reader);
                     break;
                  }
                  case TRANSACTION_ISOLATION : {
                     String str = elementAsString(reader);
                     transactionIsolation = TransactionIsolation.forName(str);
                     if (transactionIsolation == null)
                     {
                        transactionIsolation = TransactionIsolation.customLevel(str);
                     }
                     break;
                  }
                  case SECURITY : {
                     securitySettings = parseDsSecurity(reader);
                     break;
                  }
                  case STATEMENT : {
                     statementSettings = parseStatementSettings(reader);
                     break;
                  }
                  case TIMEOUT : {
                     timeOutSettings = parseTimeOutSettings(reader);
                     break;
                  }
                  case VALIDATION : {
                     validationSettings = parseValidationSetting(reader);
                     break;
                  }
                  case RECOVERY : {
                     recovery = parseRecovery(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * Parse a datasource
    * @param reader The reader
    * @return The datasource
    * @exception XMLStreamException Thrown if a stream error occurs
    * @exception ParserException Thrown if a parser error occurs
    * @exception ValidateException Thrown if a validation error occurs
    */
   protected org.jboss.jca.common.api.metadata.ds.DataSource
   parseDataSource(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      String connectionUrl = null;
      String driverClass = null;
      String dataSourceClass = null;
      String driver = null;
      TransactionIsolation transactionIsolation = null;
      Map<String, String> connectionProperties = new HashMap<String, String>();
      TimeOut timeOutSettings = null;
      DsSecurity securitySettings = null;
      Statement statementSettings = null;
      Validation validationSettings = null;
      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;
      DsPool pool = null;

      //attributes reading
      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String poolName = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      Boolean spy = Defaults.SPY;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean jta = Defaults.JTA;
      Boolean connectable = Defaults.CONNECTABLE;
      Boolean tracking = Defaults.TRACKING;
      String mcp = Defaults.MCP;
      Boolean enlistmentTrace = Defaults.ENLISTMENT_TRACE;

      for (org.jboss.jca.common.api.metadata.ds.DataSource.Attribute attribute :
              org.jboss.jca.common.api.metadata.ds.DataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.ENABLED);
               break;
            }
            case JNDI_NAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USE_JAVA_CONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.USE_JAVA_CONTEXT);
               break;
            }
            case SPY : {
               spy = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.SPY);
               break;
            }
            case USE_CCM : {
               useCcm = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.USE_CCM);
               break;
            }
            case JTA : {
               jta = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.JTA);
               break;
            }
            case CONNECTABLE : {
               connectable = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.CONNECTABLE);
               break;
            }
            case TRACKING : {
               tracking = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.TRACKING);
               break;
            }
            case MCP : {
               mcp = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case ENLISTMENT_TRACE : {
               enlistmentTrace = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.ENLISTMENT_TRACE);
               break;
            }
            default :
               break;
         }
      }

      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.DATASOURCE)
               {
                  return new DataSourceImpl(connectionUrl, driverClass, dataSourceClass, driver, transactionIsolation,
                                            connectionProperties, timeOutSettings, securitySettings,
                                            statementSettings, validationSettings, urlDelimiter,
                                            urlSelectorStrategyClassName, newConnectionSql, useJavaContext, poolName,
                                            enabled, jndiName, spy, useCcm, jta, connectable, tracking, mcp,
                                            enlistmentTrace, pool);
               }
               else
               {
                  if (org.jboss.jca.common.api.metadata.ds.DataSource.Tag.forName(reader.getLocalName()) ==
                      org.jboss.jca.common.api.metadata.ds.DataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (org.jboss.jca.common.api.metadata.ds.DataSource.Tag.forName(reader.getLocalName()))
               {
                  case CONNECTION_PROPERTY : {
                     parseConfigProperty(connectionProperties, reader);
                     break;
                  }
                  case CONNECTION_URL : {
                     connectionUrl = elementAsString(reader);
                     break;
                  }
                  case DRIVER_CLASS : {
                     driverClass = elementAsString(reader);
                     break;
                  }
                  case DATASOURCE_CLASS : {
                     dataSourceClass = elementAsString(reader);
                     break;
                  }
                  case DRIVER : {
                     driver = elementAsString(reader);
                     break;
                  }
                  case POOL : {
                     pool = parsePool(reader);
                     break;
                  }
                  case NEW_CONNECTION_SQL : {
                     newConnectionSql = elementAsString(reader);
                     break;
                  }
                  case URL_DELIMITER : {
                     urlDelimiter = elementAsString(reader);
                     break;
                  }
                  case URL_SELECTOR_STRATEGY_CLASS_NAME : {
                     urlSelectorStrategyClassName = elementAsString(reader);
                     break;
                  }
                  case TRANSACTION_ISOLATION : {
                     String str = elementAsString(reader);
                     transactionIsolation = TransactionIsolation.forName(str);
                     if (transactionIsolation == null)
                     {
                        transactionIsolation = TransactionIsolation.customLevel(str);
                     }
                     break;
                  }
                  case SECURITY : {
                     securitySettings = parseDsSecurity(reader);
                     break;
                  }
                  case STATEMENT : {
                     statementSettings = parseStatementSettings(reader);
                     break;
                  }
                  case TIMEOUT : {
                     timeOutSettings = parseTimeOutSettings(reader);
                     break;
                  }
                  case VALIDATION : {
                     validationSettings = parseValidationSetting(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * A Tag.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /** jboss-ra tag name
       *
       */
      DATASOURCES("datasources");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
       * Set the value
       * @param v The name
       * @return The value
       */
      Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }
}
