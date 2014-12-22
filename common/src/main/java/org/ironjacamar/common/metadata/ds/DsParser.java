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

import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.ds.DataSources;
import org.ironjacamar.common.api.metadata.ds.Driver;
import org.ironjacamar.common.api.metadata.ds.DsPool;
import org.ironjacamar.common.api.metadata.ds.DsSecurity;
import org.ironjacamar.common.api.metadata.ds.DsXaPool;
import org.ironjacamar.common.api.metadata.ds.Statement;
import org.ironjacamar.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.ironjacamar.common.api.metadata.ds.TimeOut;
import org.ironjacamar.common.api.metadata.ds.TransactionIsolation;
import org.ironjacamar.common.api.metadata.ds.Validation;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.MetadataParser;
import org.ironjacamar.common.metadata.ParserException;
import org.ironjacamar.common.metadata.common.AbstractParser;

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
         //found a non tag..go on. Normally non-tag found at beginning are comments or DTD declaration
         iterate = reader.nextTag();
      }
      switch (iterate)
      {
         case END_ELEMENT : {
            // should mean we're done, so ignore it.
            break;
         }
         case START_ELEMENT : {

            switch (reader.getLocalName())
            {
               case XML.ELEMENT_DATASOURCES : {
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
      List<org.ironjacamar.common.api.metadata.ds.DataSource> datasource =
         new ArrayList<org.ironjacamar.common.api.metadata.ds.DataSource>();
      List<org.ironjacamar.common.api.metadata.ds.XaDataSource> xaDataSource =
         new ArrayList<org.ironjacamar.common.api.metadata.ds.XaDataSource>();
      Map<String, Driver> drivers = new HashMap<String, Driver>();
      boolean driversMatched = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_DATASOURCES.equals(reader.getLocalName()))
               {
                  return new DatasourcesImpl(datasource, xaDataSource, drivers);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_DATASOURCE :
                     case XML.ELEMENT_XA_DATASOURCE :
                     case XML.ELEMENT_DRIVERS :
                     case XML.ELEMENT_DRIVER :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_DATASOURCE : {
                     datasource.add(parseDataSource(reader));
                     break;
                  }
                  case XML.ELEMENT_XA_DATASOURCE : {
                     xaDataSource.add(parseXADataSource(reader));
                     break;
                  }
                  case XML.ELEMENT_DRIVERS : {
                     driversMatched = true;
                     break;
                  }
                  case XML.ELEMENT_DRIVER : {
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

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_NAME : {
               name = attributeAsString(reader, XML.ATTRIBUTE_NAME);
               break;
            }
            case XML.ATTRIBUTE_MAJOR_VERSION : {
               majorVersion = attributeAsInt(reader, XML.ATTRIBUTE_MAJOR_VERSION);
               break;
            }
            case XML.ATTRIBUTE_MINOR_VERSION : {
               minorVersion = attributeAsInt(reader, XML.ATTRIBUTE_MINOR_VERSION);
               break;
            }
            case XML.ATTRIBUTE_MODULE : {
               module = attributeAsString(reader, XML.ATTRIBUTE_MODULE);
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
               if (XML.ELEMENT_DRIVER.equals(reader.getLocalName()))
               {
                  return new DriverImpl(name, majorVersion, minorVersion, module,
                                        driverClass, dataSourceClass, xaDataSourceClass);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_DATASOURCE_CLASS :
                     case XML.ELEMENT_XA_DATASOURCE_CLASS :
                     case XML.ELEMENT_DRIVER_CLASS :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_DATASOURCE_CLASS : {
                     dataSourceClass = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_XA_DATASOURCE_CLASS : {
                     xaDataSourceClass = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_DRIVER_CLASS : {
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
               if (XML.ELEMENT_SECURITY.equals(reader.getLocalName()))
               {
                  return new DsSecurityImpl(userName, password, securityDomain, reauthPlugin);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_PASSWORD :
                     case XML.ELEMENT_USER_NAME :
                     case XML.ELEMENT_SECURITY_DOMAIN :
                     case XML.ELEMENT_REAUTH_PLUGIN :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_USER_NAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_REAUTH_PLUGIN : {
                     reauthPlugin = parseExtension(reader, XML.ELEMENT_REAUTH_PLUGIN);
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
               if (XML.ELEMENT_VALIDATION.equals(reader.getLocalName()))
               {
                  return new ValidationImpl(backgroundValidation, backgroundValidationMillis, useFastFail,
                                            validConnectionChecker, checkValidConnectionSql, validateOnMatch,
                                            staleConnectionChecker, exceptionSorter);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_BACKGROUND_VALIDATION :
                     case XML.ELEMENT_BACKGROUND_VALIDATION_MILLIS :
                     case XML.ELEMENT_CHECK_VALID_CONNECTION_SQL :
                     case XML.ELEMENT_EXCEPTION_SORTER :
                     case XML.ELEMENT_STALE_CONNECTION_CHECKER :
                     case XML.ELEMENT_USE_FAST_FAIL :
                     case XML.ELEMENT_VALIDATE_ON_MATCH :
                     case XML.ELEMENT_VALID_CONNECTION_CHECKER :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_BACKGROUND_VALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader);
                     break;
                  }
                  case XML.ELEMENT_CHECK_VALID_CONNECTION_SQL : {
                     checkValidConnectionSql = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_EXCEPTION_SORTER : {
                     exceptionSorter = parseExtension(reader, XML.ELEMENT_EXCEPTION_SORTER);
                     break;
                  }
                  case XML.ELEMENT_STALE_CONNECTION_CHECKER : {
                     staleConnectionChecker = parseExtension(reader, XML.ELEMENT_STALE_CONNECTION_CHECKER);
                     break;
                  }
                  case XML.ELEMENT_USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_VALID_CONNECTION_CHECKER : {
                     validConnectionChecker = parseExtension(reader, XML.ELEMENT_VALID_CONNECTION_CHECKER);
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

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_TIMEOUT.equals(reader.getLocalName()))
               {
                  return new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                                         allocationRetryWaitMillis, xaResourceTimeout, setTxQuertTimeout,
                                         queryTimeout, useTryLock);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_ALLOCATION_RETRY :
                     case XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS :
                     case XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS :
                     case XML.ELEMENT_IDLE_TIMEOUT_MINUTES :
                     case XML.ELEMENT_QUERY_TIMEOUT :
                     case XML.ELEMENT_SET_TX_QUERY_TIMEOUT :
                     case XML.ELEMENT_USE_TRY_LOCK :
                     case XML.ELEMENT_XA_RESOURCE_TIMEOUT :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_ALLOCATION_RETRY : {
                     allocationRetry = elementAsInteger(reader);
                     break;
                  }
                  case XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case XML.ELEMENT_IDLE_TIMEOUT_MINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  case XML.ELEMENT_QUERY_TIMEOUT : {
                     queryTimeout = elementAsLong(reader);
                     break;
                  }
                  case XML.ELEMENT_SET_TX_QUERY_TIMEOUT : {
                     setTxQuertTimeout = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_USE_TRY_LOCK : {
                     useTryLock = elementAsLong(reader);
                     break;
                  }
                  case XML.ELEMENT_XA_RESOURCE_TIMEOUT : {
                     xaResourceTimeout = elementAsInteger(reader);
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
               if (XML.ELEMENT_STATEMENT.equals(reader.getLocalName()))
               {
                  return new StatementImpl(sharePreparedStatements, preparedStatementsCacheSize, trackStatements);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE :
                     case XML.ELEMENT_TRACK_STATEMENTS :
                     case XML.ELEMENT_SHARE_PREPARED_STATEMENTS :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE : {
                     preparedStatementsCacheSize = elementAsLong(reader);
                     break;
                  }
                  case XML.ELEMENT_TRACK_STATEMENTS : {
                     String elementString = elementAsString(reader);
                     trackStatements = TrackStatementsEnum.valueOf(elementString == null ? "FALSE" : elementString
                        .toUpperCase(Locale.US));
                     break;
                  }
                  case XML.ELEMENT_SHARE_PREPARED_STATEMENTS : {
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
      Extension connectionListener = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_POOL.equals(reader.getLocalName()))
               {
                  return new DsPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy,
                                        allowMultipleUsers, capacity, connectionListener);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MAX_POOL_SIZE :
                     case XML.ELEMENT_INITIAL_POOL_SIZE :
                     case XML.ELEMENT_MIN_POOL_SIZE :
                     case XML.ELEMENT_PREFILL :
                     case XML.ELEMENT_USE_STRICT_MIN :
                     case XML.ELEMENT_FLUSH_STRATEGY :
                     case XML.ELEMENT_ALLOW_MULTIPLE_USERS :
                     case XML.ELEMENT_CAPACITY :
                     case XML.ELEMENT_CONNECTION_LISTENER :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case XML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case XML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case XML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case XML.ELEMENT_ALLOW_MULTIPLE_USERS : {
                     allowMultipleUsers = Boolean.TRUE;
                     break;
                  }
                  case XML.ELEMENT_CAPACITY : {
                     capacity = parseCapacity(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_LISTENER : {
                     connectionListener = parseExtension(reader, XML.ELEMENT_CONNECTION_LISTENER);
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
               if (XML.ELEMENT_XA_POOL.equals(reader.getLocalName()))
               {
                  return new DsXaPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin,
                                          flushStrategy, isSameRmOverride, interleaving, padXid,
                                          wrapXaDataSource, noTxSeparatePool, allowMultipleUsers, capacity,
                                          connectionListener);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MAX_POOL_SIZE :
                     case XML.ELEMENT_INITIAL_POOL_SIZE :
                     case XML.ELEMENT_MIN_POOL_SIZE :
                     case XML.ELEMENT_INTERLEAVING :
                     case XML.ELEMENT_IS_SAME_RM_OVERRIDE :
                     case XML.ELEMENT_NO_TX_SEPARATE_POOLS :
                     case XML.ELEMENT_PAD_XID :
                     case XML.ELEMENT_WRAP_XA_RESOURCE :
                     case XML.ELEMENT_PREFILL :
                     case XML.ELEMENT_USE_STRICT_MIN :
                     case XML.ELEMENT_FLUSH_STRATEGY :
                     case XML.ELEMENT_ALLOW_MULTIPLE_USERS :
                     case XML.ELEMENT_CAPACITY :
                     case XML.ELEMENT_CONNECTION_LISTENER :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case XML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case XML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case XML.ELEMENT_INTERLEAVING : {
                     interleaving = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_IS_SAME_RM_OVERRIDE : {
                     isSameRmOverride = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_NO_TX_SEPARATE_POOLS : {
                     noTxSeparatePool = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_PAD_XID : {
                     padXid = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_WRAP_XA_RESOURCE : {
                     wrapXaDataSource = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case XML.ELEMENT_ALLOW_MULTIPLE_USERS : {
                     allowMultipleUsers = Boolean.TRUE;
                     break;
                  }
                  case XML.ELEMENT_CAPACITY : {
                     capacity = parseCapacity(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_LISTENER : {
                     connectionListener = parseExtension(reader, XML.ELEMENT_CONNECTION_LISTENER);
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
   protected org.ironjacamar.common.api.metadata.ds.XaDataSource
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

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, XML.ATTRIBUTE_ENABLED, Defaults.ENABLED);
               break;
            }
            case XML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, XML.ATTRIBUTE_JNDI_NAME);
               break;
            }
            case XML.ATTRIBUTE_POOL_NAME : {
               poolName = attributeAsString(reader, XML.ATTRIBUTE_POOL_NAME);
               break;
            }
            case XML.ATTRIBUTE_USE_JAVA_CONTEXT : {
               useJavaContext = attributeAsBoolean(reader, XML.ATTRIBUTE_USE_JAVA_CONTEXT, Defaults.USE_JAVA_CONTEXT);
               break;
            }
            case XML.ATTRIBUTE_SPY : {
               spy = attributeAsBoolean(reader, XML.ATTRIBUTE_SPY, Defaults.SPY);
               break;
            }
            case XML.ATTRIBUTE_USE_CCM : {
               useCcm = attributeAsBoolean(reader, XML.ATTRIBUTE_USE_CCM, Defaults.USE_CCM);
               break;
            }
            case XML.ATTRIBUTE_CONNECTABLE : {
               connectable = attributeAsBoolean(reader, XML.ATTRIBUTE_CONNECTABLE, Defaults.CONNECTABLE);
               break;
            }
            case XML.ATTRIBUTE_TRACKING : {
               tracking = attributeAsBoolean(reader, XML.ATTRIBUTE_TRACKING, Defaults.TRACKING);
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
               if (XML.ELEMENT_XA_DATASOURCE.equals(reader.getLocalName()))
               {
                  return new XADataSourceImpl(transactionIsolation, timeOutSettings, securitySettings,
                                              statementSettings, validationSettings, urlDelimiter, urlProperty,
                                              urlSelectorStrategyClassName, useJavaContext, poolName, enabled,
                                              jndiName, spy, useCcm, connectable, tracking, xaDataSourceProperty,
                                              xaDataSourceClass, driver, newConnectionSql, xaPool, recovery);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_XA_DATASOURCE_PROPERTY :
                     case XML.ELEMENT_XA_DATASOURCE_CLASS :
                     case XML.ELEMENT_DRIVER :
                     case XML.ELEMENT_XA_POOL :
                     case XML.ELEMENT_NEW_CONNECTION_SQL :
                     case XML.ELEMENT_URL_DELIMITER :
                     case XML.ELEMENT_URL_PROPERTY :
                     case XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME :
                     case XML.ELEMENT_TRANSACTION_ISOLATION :
                     case XML.ELEMENT_SECURITY :
                     case XML.ELEMENT_STATEMENT :
                     case XML.ELEMENT_TIMEOUT :
                     case XML.ELEMENT_VALIDATION :
                     case XML.ELEMENT_RECOVERY :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_XA_DATASOURCE_PROPERTY : {
                     parseConfigProperty(xaDataSourceProperty, reader);
                     break;
                  }
                  case XML.ELEMENT_XA_DATASOURCE_CLASS : {
                     xaDataSourceClass = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_DRIVER : {
                     driver = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_XA_POOL : {
                     xaPool = parseXaPool(reader);
                     break;
                  }
                  case XML.ELEMENT_NEW_CONNECTION_SQL : {
                     newConnectionSql = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_URL_DELIMITER : {
                     urlDelimiter = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_URL_PROPERTY : {
                     urlProperty = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME : {
                     urlSelectorStrategyClassName = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_ISOLATION : {
                     String str = elementAsString(reader);
                     transactionIsolation = TransactionIsolation.forName(str);
                     if (transactionIsolation == null)
                     {
                        transactionIsolation = TransactionIsolation.customLevel(str);
                     }
                     break;
                  }
                  case XML.ELEMENT_SECURITY : {
                     securitySettings = parseDsSecurity(reader);
                     break;
                  }
                  case XML.ELEMENT_STATEMENT : {
                     statementSettings = parseStatementSettings(reader);
                     break;
                  }
                  case XML.ELEMENT_TIMEOUT : {
                     timeOutSettings = parseTimeOutSettings(reader);
                     break;
                  }
                  case XML.ELEMENT_VALIDATION : {
                     validationSettings = parseValidationSetting(reader);
                     break;
                  }
                  case XML.ELEMENT_RECOVERY : {
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
   protected org.ironjacamar.common.api.metadata.ds.DataSource
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

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, XML.ATTRIBUTE_ENABLED, Defaults.ENABLED);
               break;
            }
            case XML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, XML.ATTRIBUTE_JNDI_NAME);
               break;
            }
            case XML.ATTRIBUTE_POOL_NAME : {
               poolName = attributeAsString(reader, XML.ATTRIBUTE_POOL_NAME);
               break;
            }
            case XML.ATTRIBUTE_USE_JAVA_CONTEXT : {
               useJavaContext = attributeAsBoolean(reader, XML.ATTRIBUTE_USE_JAVA_CONTEXT, Defaults.USE_JAVA_CONTEXT);
               break;
            }
            case XML.ATTRIBUTE_SPY : {
               spy = attributeAsBoolean(reader, XML.ATTRIBUTE_SPY, Defaults.SPY);
               break;
            }
            case XML.ATTRIBUTE_USE_CCM : {
               useCcm = attributeAsBoolean(reader, XML.ATTRIBUTE_USE_CCM, Defaults.USE_CCM);
               break;
            }
            case XML.ATTRIBUTE_JTA : {
               jta = attributeAsBoolean(reader, XML.ATTRIBUTE_JTA, Defaults.JTA);
               break;
            }
            case XML.ATTRIBUTE_CONNECTABLE : {
               connectable = attributeAsBoolean(reader, XML.ATTRIBUTE_CONNECTABLE, Defaults.CONNECTABLE);
               break;
            }
            case XML.ATTRIBUTE_TRACKING : {
               tracking = attributeAsBoolean(reader, XML.ATTRIBUTE_TRACKING, Defaults.TRACKING);
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
               if (XML.ELEMENT_DATASOURCE.equals(reader.getLocalName()))
               {
                  return new DataSourceImpl(connectionUrl, driverClass, dataSourceClass, driver, transactionIsolation,
                                            connectionProperties, timeOutSettings, securitySettings,
                                            statementSettings, validationSettings, urlDelimiter,
                                            urlSelectorStrategyClassName, newConnectionSql, useJavaContext, poolName,
                                            enabled, jndiName, spy, useCcm, jta, connectable, tracking, pool);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONNECTION_PROPERTY :
                     case XML.ELEMENT_CONNECTION_URL :
                     case XML.ELEMENT_DRIVER_CLASS :
                     case XML.ELEMENT_DATASOURCE_CLASS :
                     case XML.ELEMENT_DRIVER :
                     case XML.ELEMENT_POOL :
                     case XML.ELEMENT_NEW_CONNECTION_SQL :
                     case XML.ELEMENT_URL_DELIMITER :
                     case XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME :
                     case XML.ELEMENT_TRANSACTION_ISOLATION :
                     case XML.ELEMENT_SECURITY :
                     case XML.ELEMENT_STATEMENT :
                     case XML.ELEMENT_TIMEOUT :
                     case XML.ELEMENT_VALIDATION :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_CONNECTION_PROPERTY : {
                     parseConfigProperty(connectionProperties, reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_URL : {
                     connectionUrl = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_DRIVER_CLASS : {
                     driverClass = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_DATASOURCE_CLASS : {
                     dataSourceClass = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_DRIVER : {
                     driver = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_POOL : {
                     pool = parsePool(reader);
                     break;
                  }
                  case XML.ELEMENT_NEW_CONNECTION_SQL : {
                     newConnectionSql = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_URL_DELIMITER : {
                     urlDelimiter = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME : {
                     urlSelectorStrategyClassName = elementAsString(reader);
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_ISOLATION : {
                     String str = elementAsString(reader);
                     transactionIsolation = TransactionIsolation.forName(str);
                     if (transactionIsolation == null)
                     {
                        transactionIsolation = TransactionIsolation.customLevel(str);
                     }
                     break;
                  }
                  case XML.ELEMENT_SECURITY : {
                     securitySettings = parseDsSecurity(reader);
                     break;
                  }
                  case XML.ELEMENT_STATEMENT : {
                     statementSettings = parseStatementSettings(reader);
                     break;
                  }
                  case XML.ELEMENT_TIMEOUT : {
                     timeOutSettings = parseTimeOutSettings(reader);
                     break;
                  }
                  case XML.ELEMENT_VALIDATION : {
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
}
