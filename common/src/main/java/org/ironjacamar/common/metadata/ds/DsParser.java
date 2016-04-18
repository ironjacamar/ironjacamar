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
import org.ironjacamar.common.api.metadata.ds.Credential;
import org.ironjacamar.common.api.metadata.ds.DataSource;
import org.ironjacamar.common.api.metadata.ds.DataSources;
import org.ironjacamar.common.api.metadata.ds.Driver;
import org.ironjacamar.common.api.metadata.ds.DsPool;
import org.ironjacamar.common.api.metadata.ds.DsSecurity;
import org.ironjacamar.common.api.metadata.ds.DsXaPool;
import org.ironjacamar.common.api.metadata.ds.Statement;
import org.ironjacamar.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.ironjacamar.common.api.metadata.ds.Timeout;
import org.ironjacamar.common.api.metadata.ds.TransactionIsolation;
import org.ironjacamar.common.api.metadata.ds.Validation;
import org.ironjacamar.common.api.metadata.ds.XaDataSource;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.MetadataParser;
import org.ironjacamar.common.metadata.ParserException;
import org.ironjacamar.common.metadata.common.AbstractParser;
import org.ironjacamar.common.metadata.common.CommonXML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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
    * Store a -ds.xml file
    * @param metadata The datasource definitions
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   public void store(DataSources metadata, XMLStreamWriter writer) throws Exception
   {
      if (metadata != null && writer != null)
      {
         writer.writeStartElement(XML.ELEMENT_DATASOURCES);

         if (metadata.getDataSource() != null && !metadata.getDataSource().isEmpty())
         {
            for (DataSource ds : metadata.getDataSource())
            {
               storeDataSource(ds, writer);
            }
         }
         if (metadata.getXaDataSource() != null && !metadata.getXaDataSource().isEmpty())
         {
            for (XaDataSource xads : metadata.getXaDataSource())
            {
               storeXaDataSource(xads, writer);
            }
         }
         if (metadata.getDrivers() != null && !metadata.getDrivers().isEmpty())
         {
            writer.writeStartElement(XML.ELEMENT_DRIVERS);
            for (Driver drv : metadata.getDrivers())
            {
               storeDriver(drv, writer);
            }
            writer.writeEndElement();
         }

         writer.writeEndElement();
      }
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
      List<DataSource> datasource = new ArrayList<DataSource>();
      List<XaDataSource> xaDataSource = new ArrayList<XaDataSource>();
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

      Map<String, String> expressions = new HashMap<String, String>();
      
      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_NAME : {
               name = attributeAsString(reader, XML.ATTRIBUTE_NAME, expressions);
               break;
            }
            case XML.ATTRIBUTE_MAJOR_VERSION : {
               majorVersion = attributeAsInt(reader, XML.ATTRIBUTE_MAJOR_VERSION, expressions);
               break;
            }
            case XML.ATTRIBUTE_MINOR_VERSION : {
               minorVersion = attributeAsInt(reader, XML.ATTRIBUTE_MINOR_VERSION, expressions);
               break;
            }
            case XML.ATTRIBUTE_MODULE : {
               module = attributeAsString(reader, XML.ATTRIBUTE_MODULE, expressions);
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
                                        driverClass, dataSourceClass, xaDataSourceClass,
                          !expressions.isEmpty() ? expressions : null);
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
                     dataSourceClass = elementAsString(reader, XML.ELEMENT_DATASOURCE_CLASS, expressions);
                     break;
                  }
                  case XML.ELEMENT_XA_DATASOURCE_CLASS : {
                     xaDataSourceClass = elementAsString(reader, XML.ELEMENT_XA_DATASOURCE_CLASS, expressions);
                     break;
                  }
                  case XML.ELEMENT_DRIVER_CLASS : {
                     driverClass = elementAsString(reader, XML.ELEMENT_DRIVER_CLASS, expressions);
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

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_SECURITY.equals(reader.getLocalName()))
               {
                  return new DsSecurityImpl(userName, password, securityDomain, reauthPlugin,
                          !expressions.isEmpty() ? expressions : null);
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
                     password = elementAsString(reader, XML.ELEMENT_PASSWORD, expressions);
                     break;
                  }
                  case XML.ELEMENT_USER_NAME : {
                     userName = elementAsString(reader, XML.ELEMENT_USER_NAME, expressions);
                     break;
                  }
                  case XML.ELEMENT_SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader, XML.ELEMENT_SECURITY_DOMAIN, expressions);
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

      Map<String, String> expressions = new HashMap<String, String>();
      
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_VALIDATION.equals(reader.getLocalName()))
               {
                  return new ValidationImpl(backgroundValidation, backgroundValidationMillis, useFastFail,
                                            validConnectionChecker, checkValidConnectionSql, validateOnMatch,
                                            staleConnectionChecker, exceptionSorter,
                          !expressions.isEmpty() ? expressions : null);
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
                     backgroundValidation = elementAsBoolean(reader, XML.ELEMENT_BACKGROUND_VALIDATION, expressions);
                     break;
                  }
                  case XML.ELEMENT_BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader, XML.ELEMENT_BACKGROUND_VALIDATION_MILLIS,
                                                                expressions);
                     break;
                  }
                  case XML.ELEMENT_CHECK_VALID_CONNECTION_SQL : {
                     checkValidConnectionSql = elementAsString(reader, XML.ELEMENT_CHECK_VALID_CONNECTION_SQL,
                                                               expressions);
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
                     useFastFail = elementAsBoolean(reader, XML.ELEMENT_USE_FAST_FAIL, expressions);
                     break;
                  }
                  case XML.ELEMENT_VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader, XML.ELEMENT_VALIDATE_ON_MATCH, expressions);
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
   protected Timeout parseTimeoutSettings(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      Long blockingTimeoutMillis = null;
      Integer idleTimeoutMinutes = null;
      Boolean setTxQuertTimeout = Defaults.SET_TX_QUERY_TIMEOUT;
      Long queryTimeout = null;
      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Long useTryLock = null;
      Integer xaResourceTimeout = null;

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_TIMEOUT.equals(reader.getLocalName()))
               {
                  return new TimeoutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                                         allocationRetryWaitMillis, xaResourceTimeout, setTxQuertTimeout,
                                         queryTimeout, useTryLock,
                          !expressions.isEmpty() ? expressions : null);
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
                     allocationRetry = elementAsInteger(reader, XML.ELEMENT_ALLOCATION_RETRY, expressions);
                     break;
                  }
                  case XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader, XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS,
                                                               expressions);
                     break;
                  }
                  case XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader, XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS, expressions);
                     break;
                  }
                  case XML.ELEMENT_IDLE_TIMEOUT_MINUTES : {
                     idleTimeoutMinutes = elementAsInteger(reader, XML.ELEMENT_IDLE_TIMEOUT_MINUTES, expressions);
                     break;
                  }
                  case XML.ELEMENT_QUERY_TIMEOUT : {
                     queryTimeout = elementAsLong(reader, XML.ELEMENT_QUERY_TIMEOUT, expressions);
                     break;
                  }
                  case XML.ELEMENT_SET_TX_QUERY_TIMEOUT : {
                     setTxQuertTimeout = elementAsBoolean(reader, XML.ELEMENT_SET_TX_QUERY_TIMEOUT, expressions);
                     break;
                  }
                  case XML.ELEMENT_USE_TRY_LOCK : {
                     useTryLock = elementAsLong(reader, XML.ELEMENT_USE_TRY_LOCK, expressions);
                     break;
                  }
                  case XML.ELEMENT_XA_RESOURCE_TIMEOUT : {
                     xaResourceTimeout = elementAsInteger(reader, XML.ELEMENT_XA_RESOURCE_TIMEOUT, expressions);
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

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_STATEMENT.equals(reader.getLocalName()))
               {
                  return new StatementImpl(sharePreparedStatements, preparedStatementsCacheSize, trackStatements,
                          !expressions.isEmpty() ? expressions : null);
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
                     preparedStatementsCacheSize = elementAsLong(reader, XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE,
                                                                 expressions);
                     break;
                  }
                  case XML.ELEMENT_TRACK_STATEMENTS : {
                     String elementString = elementAsString(reader, XML.ELEMENT_TRACK_STATEMENTS, expressions);
                     trackStatements = TrackStatementsEnum.valueOf(elementString == null ? "FALSE" : elementString
                        .toUpperCase(Locale.US));
                     break;
                  }
                  case XML.ELEMENT_SHARE_PREPARED_STATEMENTS : {
                     sharePreparedStatements = elementAsBoolean(reader, XML.ELEMENT_SHARE_PREPARED_STATEMENTS,
                                                                expressions);
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
      String type = Defaults.TYPE;
      String janitor = Defaults.JANITOR;
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Capacity capacity = null;
      Extension connectionListener = null;

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_TYPE : {
               type = attributeAsString(reader, XML.ATTRIBUTE_TYPE, expressions);
               break;
            }
            case XML.ATTRIBUTE_JANITOR : {
               janitor = attributeAsString(reader, XML.ATTRIBUTE_JANITOR, expressions);
               break;
            }
            default :
               break;
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_POOL.equals(reader.getLocalName()))
               {
                  return new DsPoolImpl(type, janitor, minPoolSize, initialPoolSize, maxPoolSize, prefill,
                                        flushStrategy, capacity, connectionListener,
                          !expressions.isEmpty() ? expressions : null);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MAX_POOL_SIZE :
                     case XML.ELEMENT_INITIAL_POOL_SIZE :
                     case XML.ELEMENT_MIN_POOL_SIZE :
                     case XML.ELEMENT_PREFILL :
                     case XML.ELEMENT_FLUSH_STRATEGY :
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
                     maxPoolSize = elementAsInteger(reader, XML.ELEMENT_MAX_POOL_SIZE, expressions);
                     break;
                  }
                  case XML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader, XML.ELEMENT_INITIAL_POOL_SIZE, expressions);
                     break;
                  }
                  case XML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader, XML.ELEMENT_MIN_POOL_SIZE, expressions);
                     break;
                  }
                  case XML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader, XML.ELEMENT_PREFILL, expressions);
                     break;
                  }
                  case XML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader, expressions);
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
      String type = Defaults.TYPE;
      String janitor = Defaults.JANITOR;
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Capacity capacity = null;
      Extension connectionListener = null;
      Boolean isSameRmOverride = Defaults.IS_SAME_RM_OVERRIDE;
      Boolean padXid = Defaults.PAD_XID;
      Boolean wrapXaDataSource = Defaults.WRAP_XA_RESOURCE;

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_TYPE : {
               type = attributeAsString(reader, XML.ATTRIBUTE_TYPE, expressions);
               break;
            }
            case XML.ATTRIBUTE_JANITOR : {
               janitor = attributeAsString(reader, XML.ATTRIBUTE_JANITOR, expressions);
               break;
            }
            default :
               break;
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_XA_POOL.equals(reader.getLocalName()))
               {
                  return new DsXaPoolImpl(type, janitor, minPoolSize, initialPoolSize, maxPoolSize, prefill,
                                          flushStrategy, isSameRmOverride, padXid,
                                          wrapXaDataSource, capacity,
                                          connectionListener,
                          !expressions.isEmpty() ? expressions : null);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MAX_POOL_SIZE :
                     case XML.ELEMENT_INITIAL_POOL_SIZE :
                     case XML.ELEMENT_MIN_POOL_SIZE :
                     case XML.ELEMENT_IS_SAME_RM_OVERRIDE :
                     case XML.ELEMENT_PAD_XID :
                     case XML.ELEMENT_WRAP_XA_RESOURCE :
                     case XML.ELEMENT_PREFILL :
                     case XML.ELEMENT_FLUSH_STRATEGY :
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
                     maxPoolSize = elementAsInteger(reader, XML.ELEMENT_MAX_POOL_SIZE, expressions);
                     break;
                  }
                  case XML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader, XML.ELEMENT_INITIAL_POOL_SIZE, expressions);
                     break;
                  }
                  case XML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader, XML.ELEMENT_MIN_POOL_SIZE, expressions);
                     break;
                  }
                  case XML.ELEMENT_IS_SAME_RM_OVERRIDE : {
                     isSameRmOverride = elementAsBoolean(reader, XML.ELEMENT_IS_SAME_RM_OVERRIDE, expressions);
                     break;
                  }
                  case XML.ELEMENT_PAD_XID : {
                     padXid = elementAsBoolean(reader, XML.ELEMENT_PAD_XID, expressions);
                     break;
                  }
                  case XML.ELEMENT_WRAP_XA_RESOURCE : {
                     wrapXaDataSource = elementAsBoolean(reader, XML.ELEMENT_WRAP_XA_RESOURCE, expressions);
                     break;
                  }
                  case XML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader, XML.ELEMENT_PREFILL, expressions);
                     break;
                  }
                  case XML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader, expressions);
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
      Timeout timeoutSettings = null;
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

      String id = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      Boolean spy = Defaults.SPY;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean connectable = Defaults.CONNECTABLE;
      Boolean tracking = Defaults.TRACKING;

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, XML.ATTRIBUTE_ENABLED, Defaults.ENABLED, expressions);
               break;
            }
            case XML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, XML.ATTRIBUTE_JNDI_NAME, expressions);
               break;
            }
            case XML.ATTRIBUTE_ID : {
               id = attributeAsString(reader, XML.ATTRIBUTE_ID, expressions);
               break;
            }
            case XML.ATTRIBUTE_SPY : {
               spy = attributeAsBoolean(reader, XML.ATTRIBUTE_SPY, Defaults.SPY, expressions);
               break;
            }
            case XML.ATTRIBUTE_USE_CCM : {
               useCcm = attributeAsBoolean(reader, XML.ATTRIBUTE_USE_CCM, Defaults.USE_CCM, expressions);
               break;
            }
            case XML.ATTRIBUTE_CONNECTABLE : {
               connectable = attributeAsBoolean(reader, XML.ATTRIBUTE_CONNECTABLE, Defaults.CONNECTABLE, expressions);
               break;
            }
            case XML.ATTRIBUTE_TRACKING : {
               tracking = attributeAsBoolean(reader, XML.ATTRIBUTE_TRACKING, Defaults.TRACKING, expressions);
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
                  return new XADataSourceImpl(transactionIsolation, timeoutSettings, securitySettings,
                                              statementSettings, validationSettings, urlDelimiter, urlProperty,
                                              urlSelectorStrategyClassName, id, enabled,
                                              jndiName, spy, useCcm, connectable, tracking, xaDataSourceProperty,
                                              xaDataSourceClass, driver, newConnectionSql, xaPool, recovery,
                          !expressions.isEmpty() ? expressions : null);
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
                     parseConfigProperty(xaDataSourceProperty, reader, XML.ELEMENT_XA_DATASOURCE_PROPERTY, expressions);
                     break;
                  }
                  case XML.ELEMENT_XA_DATASOURCE_CLASS : {
                     xaDataSourceClass = elementAsString(reader, XML.ELEMENT_XA_DATASOURCE_CLASS, expressions);
                     break;
                  }
                  case XML.ELEMENT_DRIVER : {
                     driver = elementAsString(reader, XML.ELEMENT_DRIVER, expressions);
                     break;
                  }
                  case XML.ELEMENT_XA_POOL : {
                     xaPool = parseXaPool(reader);
                     break;
                  }
                  case XML.ELEMENT_NEW_CONNECTION_SQL : {
                     newConnectionSql = elementAsString(reader, XML.ELEMENT_NEW_CONNECTION_SQL, expressions);
                     break;
                  }
                  case XML.ELEMENT_URL_DELIMITER : {
                     urlDelimiter = elementAsString(reader, XML.ELEMENT_URL_DELIMITER, expressions);
                     break;
                  }
                  case XML.ELEMENT_URL_PROPERTY : {
                     urlProperty = elementAsString(reader, XML.ELEMENT_URL_PROPERTY, expressions);
                     break;
                  }
                  case XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME : {
                     urlSelectorStrategyClassName = elementAsString(reader,
                                                                    XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME,
                                                                    expressions);
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_ISOLATION : {
                     String str = elementAsString(reader, XML.ELEMENT_TRANSACTION_ISOLATION, expressions);
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
                     timeoutSettings = parseTimeoutSettings(reader);
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
   protected DataSource parseDataSource(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      String connectionUrl = null;
      String driverClass = null;
      String dataSourceClass = null;
      String driver = null;
      TransactionIsolation transactionIsolation = null;
      Map<String, String> connectionProperties = new HashMap<String, String>();
      Timeout timeoutSettings = null;
      DsSecurity securitySettings = null;
      Statement statementSettings = null;
      Validation validationSettings = null;
      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;
      DsPool pool = null;

      //attributes reading
      String id = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      Boolean spy = Defaults.SPY;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean jta = Defaults.JTA;
      Boolean connectable = Defaults.CONNECTABLE;
      Boolean tracking = Defaults.TRACKING;

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, XML.ATTRIBUTE_ENABLED, Defaults.ENABLED, expressions);
               break;
            }
            case XML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, XML.ATTRIBUTE_JNDI_NAME, expressions);
               break;
            }
            case XML.ATTRIBUTE_ID : {
               id = attributeAsString(reader, XML.ATTRIBUTE_ID, expressions);
               break;
            }
            case XML.ATTRIBUTE_SPY : {
               spy = attributeAsBoolean(reader, XML.ATTRIBUTE_SPY, Defaults.SPY, expressions);
               break;
            }
            case XML.ATTRIBUTE_USE_CCM : {
               useCcm = attributeAsBoolean(reader, XML.ATTRIBUTE_USE_CCM, Defaults.USE_CCM, expressions);
               break;
            }
            case XML.ATTRIBUTE_JTA : {
               jta = attributeAsBoolean(reader, XML.ATTRIBUTE_JTA, Defaults.JTA, expressions);
               break;
            }
            case XML.ATTRIBUTE_CONNECTABLE : {
               connectable = attributeAsBoolean(reader, XML.ATTRIBUTE_CONNECTABLE, Defaults.CONNECTABLE, expressions);
               break;
            }
            case XML.ATTRIBUTE_TRACKING : {
               tracking = attributeAsBoolean(reader, XML.ATTRIBUTE_TRACKING, Defaults.TRACKING, expressions);
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
                                            connectionProperties, timeoutSettings, securitySettings,
                                            statementSettings, validationSettings, urlDelimiter,
                                            urlSelectorStrategyClassName, newConnectionSql, id,
                                            enabled, jndiName, spy, useCcm, jta, connectable, tracking, pool,
                          !expressions.isEmpty() ? expressions : null);
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
                     parseConfigProperty(connectionProperties, reader, XML.ELEMENT_CONNECTION_PROPERTY, expressions);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_URL : {
                     connectionUrl = elementAsString(reader, XML.ELEMENT_CONNECTION_URL, expressions);
                     break;
                  }
                  case XML.ELEMENT_DRIVER_CLASS : {
                     driverClass = elementAsString(reader, XML.ELEMENT_DRIVER_CLASS, expressions);
                     break;
                  }
                  case XML.ELEMENT_DATASOURCE_CLASS : {
                     dataSourceClass = elementAsString(reader, XML.ELEMENT_DATASOURCE_CLASS, expressions);
                     break;
                  }
                  case XML.ELEMENT_DRIVER : {
                     driver = elementAsString(reader, XML.ELEMENT_DRIVER, expressions);
                     break;
                  }
                  case XML.ELEMENT_POOL : {
                     pool = parsePool(reader);
                     break;
                  }
                  case XML.ELEMENT_NEW_CONNECTION_SQL : {
                     newConnectionSql = elementAsString(reader, XML.ELEMENT_NEW_CONNECTION_SQL, expressions);
                     break;
                  }
                  case XML.ELEMENT_URL_DELIMITER : {
                     urlDelimiter = elementAsString(reader, XML.ELEMENT_URL_DELIMITER, expressions);
                     break;
                  }
                  case XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME : {
                     urlSelectorStrategyClassName = elementAsString(reader,
                                                                    XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME,
                                                                    expressions);
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_ISOLATION : {
                     String str = elementAsString(reader, XML.ELEMENT_TRANSACTION_ISOLATION, expressions);
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
                     timeoutSettings = parseTimeoutSettings(reader);
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

   /**
    *
    * Parse recovery tag
    *
    * @param reader reader
    * @return the parsed recovery object
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    * @throws ValidateException in case of error
    */
   @Override
   protected Recovery parseRecovery(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      Boolean noRecovery = Defaults.NO_RECOVERY;
      Credential security = null;
      Extension plugin = null;

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_NO_RECOVERY : {
               noRecovery = attributeAsBoolean(reader, XML.ATTRIBUTE_NO_RECOVERY, Boolean.FALSE, expressions);
               break;
            }
            default :
               break;
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_RECOVERY :
                     return new RecoveryImpl(security, plugin, noRecovery,
                             !expressions.isEmpty() ? expressions : null);
                  case XML.ELEMENT_RECOVERY_CREDENTIAL :
                  case XML.ELEMENT_RECOVERY_PLUGIN :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_RECOVERY_CREDENTIAL : {
                     security = parseCredential(reader);
                     break;
                  }
                  case XML.ELEMENT_RECOVERY_PLUGIN : {
                     plugin = parseExtension(reader, XML.ELEMENT_RECOVERY_PLUGIN);
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
    * parse credential tag
    *
    * @param reader reader
    * @return the parse Object
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    * @throws ValidateException in case of error
    */
   @Override
   protected Credential parseCredential(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      String userName = null;
      String password = null;
      String securityDomain = null;

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_SECURITY :
                  case XML.ELEMENT_RECOVERY_CREDENTIAL :
                     return new CredentialImpl(userName, password, securityDomain,
                             !expressions.isEmpty() ? expressions : null);
                  case XML.ELEMENT_USER_NAME :
                  case XML.ELEMENT_PASSWORD :
                  case XML.ELEMENT_SECURITY_DOMAIN :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_USER_NAME : {
                     userName = elementAsString(reader, XML.ELEMENT_USER_NAME, expressions);
                     break;
                  }
                  case XML.ELEMENT_PASSWORD : {
                     password = elementAsString(reader, XML.ELEMENT_PASSWORD, expressions);
                     break;
                  }
                  case XML.ELEMENT_SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader, XML.ELEMENT_SECURITY_DOMAIN, expressions);
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
    * Store a datasource
    * @param ds The datasource
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeDataSource(DataSource ds, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_DATASOURCE);

      if (ds.isJTA() != null && (ds.hasExpression(XML.ATTRIBUTE_JTA) ||
                                 !Defaults.JTA.equals(ds.isJTA())))
         writer.writeAttribute(XML.ATTRIBUTE_JTA,
                               ds.getValue(XML.ATTRIBUTE_JTA, ds.isJTA().toString()));

      if (ds.getJndiName() != null)
         writer.writeAttribute(XML.ATTRIBUTE_JNDI_NAME,
                               ds.getValue(XML.ATTRIBUTE_JNDI_NAME, ds.getJndiName()));

      if (ds.getId() != null)
         writer.writeAttribute(XML.ATTRIBUTE_ID,
                               ds.getValue(XML.ATTRIBUTE_ID, ds.getId()));

      if (ds.isEnabled() != null && (ds.hasExpression(XML.ATTRIBUTE_ENABLED) ||
                                     !Defaults.ENABLED.equals(ds.isEnabled())))
         writer.writeAttribute(XML.ATTRIBUTE_ENABLED,
                               ds.getValue(XML.ATTRIBUTE_ENABLED, ds.isEnabled().toString()));

      if (ds.isSpy() != null && (ds.hasExpression(XML.ATTRIBUTE_SPY) ||
                                 !Defaults.SPY.equals(ds.isSpy())))
         writer.writeAttribute(XML.ATTRIBUTE_SPY,
                               ds.getValue(XML.ATTRIBUTE_SPY, ds.isSpy().toString()));

      if (ds.isUseCcm() != null && (ds.hasExpression(XML.ATTRIBUTE_USE_CCM) ||
                                    !Defaults.USE_CCM.equals(ds.isUseCcm())))
         writer.writeAttribute(XML.ATTRIBUTE_USE_CCM,
                               ds.getValue(XML.ATTRIBUTE_USE_CCM, ds.isUseCcm().toString()));

      if (ds.isConnectable() != null && (ds.hasExpression(XML.ATTRIBUTE_CONNECTABLE) ||
                                         !Defaults.CONNECTABLE.equals(ds.isConnectable())))
         writer.writeAttribute(XML.ATTRIBUTE_CONNECTABLE,
                               ds.getValue(XML.ATTRIBUTE_CONNECTABLE, ds.isConnectable().toString()));

      if (ds.isTracking() != null)
         writer.writeAttribute(XML.ATTRIBUTE_TRACKING,
                               ds.getValue(XML.ATTRIBUTE_TRACKING, ds.isTracking().toString()));

      if (ds.getConnectionUrl() != null)
      {
         writer.writeStartElement(XML.ELEMENT_CONNECTION_URL);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_CONNECTION_URL, ds.getConnectionUrl()));
         writer.writeEndElement();
      }

      if (ds.getDriverClass() != null)
      {
         writer.writeStartElement(XML.ELEMENT_DRIVER_CLASS);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_DRIVER_CLASS, ds.getDriverClass()));
         writer.writeEndElement();
      }

      if (ds.getDataSourceClass() != null)
      {
         writer.writeStartElement(XML.ELEMENT_DATASOURCE_CLASS);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_DATASOURCE_CLASS, ds.getDataSourceClass()));
         writer.writeEndElement();
      }

      if (ds.getDriver() != null)
      {
         writer.writeStartElement(XML.ELEMENT_DRIVER);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_DRIVER, ds.getDriver()));
         writer.writeEndElement();
      }

      if (ds.getConnectionProperties() != null && !ds.getConnectionProperties().isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = ds.getConnectionProperties().entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            writer.writeStartElement(XML.ELEMENT_CONNECTION_PROPERTY);
            writer.writeAttribute(XML.ATTRIBUTE_NAME, entry.getKey());
            writer.writeCharacters(ds.getValue(XML.ELEMENT_CONNECTION_PROPERTY, entry.getKey(), entry.getValue()));
            writer.writeEndElement();
         }
      }

      if (ds.getNewConnectionSql() != null)
      {
         writer.writeStartElement(XML.ELEMENT_NEW_CONNECTION_SQL);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_NEW_CONNECTION_SQL, ds.getNewConnectionSql()));
         writer.writeEndElement();
      }

      if (ds.getTransactionIsolation() != null)
      {
         writer.writeStartElement(XML.ELEMENT_TRANSACTION_ISOLATION);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_TRANSACTION_ISOLATION,
                                            ds.getTransactionIsolation().toString()));
         writer.writeEndElement();
      }

      if (ds.getUrlDelimiter() != null)
      {
         writer.writeStartElement(XML.ELEMENT_URL_DELIMITER);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_URL_DELIMITER, ds.getUrlDelimiter()));
         writer.writeEndElement();
      }

      if (ds.getUrlSelectorStrategyClassName() != null)
      {
         writer.writeStartElement(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME);
         writer.writeCharacters(ds.getValue(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME,
                                            ds.getUrlSelectorStrategyClassName()));
         writer.writeEndElement();
      }

      if (ds.getPool() != null)
         storePool(ds.getPool(), writer);

      if (ds.getSecurity() != null)
         storeSecurity(ds.getSecurity(), writer);

      if (ds.getValidation() != null)
         storeValidation(ds.getValidation(), writer);

      if (ds.getTimeout() != null)
         storeTimeout(ds.getTimeout(), writer);

      if (ds.getStatement() != null)
         storeStatement(ds.getStatement(), writer);

      writer.writeEndElement();
   }

   /**
    * Store a XA datasource
    * @param xads The XA datasource
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeXaDataSource(XaDataSource xads, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_XA_DATASOURCE);

      if (xads.getJndiName() != null)
         writer.writeAttribute(XML.ATTRIBUTE_JNDI_NAME,
                               xads.getValue(XML.ATTRIBUTE_JNDI_NAME, xads.getJndiName()));

      if (xads.getId() != null)
         writer.writeAttribute(XML.ATTRIBUTE_ID,
                               xads.getValue(XML.ATTRIBUTE_ID, xads.getId()));

      if (xads.isEnabled() != null && (xads.hasExpression(XML.ATTRIBUTE_ENABLED) ||
                                       !Defaults.ENABLED.equals(xads.isEnabled())))
         writer.writeAttribute(XML.ATTRIBUTE_ENABLED,
                               xads.getValue(XML.ATTRIBUTE_ENABLED, xads.isEnabled().toString()));

      if (xads.isSpy() != null && (xads.hasExpression(XML.ATTRIBUTE_SPY) ||
                                   !Defaults.SPY.equals(xads.isSpy())))
         writer.writeAttribute(XML.ATTRIBUTE_SPY,
                               xads.getValue(XML.ATTRIBUTE_SPY, xads.isSpy().toString()));

      if (xads.isUseCcm() != null && (xads.hasExpression(XML.ATTRIBUTE_USE_CCM) ||
                                      !Defaults.USE_CCM.equals(xads.isUseCcm())))
         writer.writeAttribute(XML.ATTRIBUTE_USE_CCM,
                               xads.getValue(XML.ATTRIBUTE_USE_CCM, xads.isUseCcm().toString()));

      if (xads.isConnectable() != null && (xads.hasExpression(XML.ATTRIBUTE_CONNECTABLE) ||
                                           !Defaults.CONNECTABLE.equals(xads.isConnectable())))
         writer.writeAttribute(XML.ATTRIBUTE_CONNECTABLE,
                               xads.getValue(XML.ATTRIBUTE_CONNECTABLE, xads.isConnectable().toString()));

      if (xads.isTracking() != null)
         writer.writeAttribute(XML.ATTRIBUTE_TRACKING,
                               xads.getValue(XML.ATTRIBUTE_TRACKING, xads.isTracking().toString()));

      if (xads.getXaDataSourceProperty() != null && !xads.getXaDataSourceProperty().isEmpty())
      {
         Iterator<Map.Entry<String, String>> it =
            xads.getXaDataSourceProperty().entrySet().iterator();

         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            writer.writeStartElement(XML.ELEMENT_XA_DATASOURCE_PROPERTY);
            writer.writeAttribute(XML.ATTRIBUTE_NAME, entry.getKey());
            writer.writeCharacters(xads.getValue(XML.ELEMENT_XA_DATASOURCE_PROPERTY, entry.getKey(), entry.getValue()));
            writer.writeEndElement();
         }
      }

      if (xads.getXaDataSourceClass() != null)
      {
         writer.writeStartElement(XML.ELEMENT_XA_DATASOURCE_CLASS);
         writer.writeCharacters(xads.getValue(XML.ELEMENT_XA_DATASOURCE_CLASS, xads.getXaDataSourceClass()));
         writer.writeEndElement();
      }

      if (xads.getDriver() != null)
      {
         writer.writeStartElement(XML.ELEMENT_DRIVER);
         writer.writeCharacters(xads.getValue(XML.ELEMENT_DRIVER, xads.getDriver()));
         writer.writeEndElement();
      }

      if (xads.getUrlDelimiter() != null)
      {
         writer.writeStartElement(XML.ELEMENT_URL_DELIMITER);
         writer.writeCharacters(xads.getValue(XML.ELEMENT_URL_DELIMITER, xads.getUrlDelimiter()));
         writer.writeEndElement();
      }

      if (xads.getUrlProperty() != null)
      {
         writer.writeStartElement(XML.ELEMENT_URL_PROPERTY);
         writer.writeCharacters(xads.getValue(XML.ELEMENT_URL_PROPERTY, xads.getUrlProperty()));
         writer.writeEndElement();
      }

      if (xads.getUrlSelectorStrategyClassName() != null)
      {
         writer.writeStartElement(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME);
         writer.writeCharacters(xads.getValue(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME,
                                              xads.getUrlSelectorStrategyClassName()));
         writer.writeEndElement();
      }

      if (xads.getNewConnectionSql() != null)
      {
         writer.writeStartElement(XML.ELEMENT_NEW_CONNECTION_SQL);
         writer.writeCharacters(xads.getValue(XML.ELEMENT_NEW_CONNECTION_SQL, xads.getNewConnectionSql()));
         writer.writeEndElement();
      }

      if (xads.getTransactionIsolation() != null)
      {
         writer.writeStartElement(XML.ELEMENT_TRANSACTION_ISOLATION);
         writer.writeCharacters(xads.getValue(XML.ELEMENT_TRANSACTION_ISOLATION,
                                              xads.getTransactionIsolation().toString()));
         writer.writeEndElement();
      }

      if (xads.getXaPool() != null)
         storeXaPool(xads.getXaPool(), writer);

      if (xads.getSecurity() != null)
         storeSecurity(xads.getSecurity(), writer);

      if (xads.getValidation() != null)
         storeValidation(xads.getValidation(), writer);

      if (xads.getTimeout() != null)
         storeTimeout(xads.getTimeout(), writer);

      if (xads.getStatement() != null)
         storeStatement(xads.getStatement(), writer);

      if (xads.getRecovery() != null)
         storeRecovery(xads.getRecovery(), writer);

      writer.writeEndElement();
   }

   /**
    * Store a driver
    * @param drv The driver
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeDriver(Driver drv, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_DRIVER);

      if (drv.getName() != null)
         writer.writeAttribute(XML.ATTRIBUTE_NAME,
                               drv.getValue(XML.ATTRIBUTE_NAME, drv.getName()));

      if (drv.getModule() != null)
         writer.writeAttribute(XML.ATTRIBUTE_MODULE,
                               drv.getValue(XML.ATTRIBUTE_MODULE, drv.getModule()));

      if (drv.getMajorVersion() != null)
         writer.writeAttribute(XML.ATTRIBUTE_MAJOR_VERSION,
                               drv.getValue(XML.ATTRIBUTE_MAJOR_VERSION, drv.getMajorVersion().toString()));

      if (drv.getMinorVersion() != null)
         writer.writeAttribute(XML.ATTRIBUTE_MINOR_VERSION,
                               drv.getValue(XML.ATTRIBUTE_MINOR_VERSION, drv.getMinorVersion().toString()));

      if (drv.getDriverClass() != null)
      {
         writer.writeStartElement(XML.ELEMENT_DRIVER_CLASS);
         writer.writeCharacters(drv.getValue(XML.ELEMENT_DRIVER_CLASS, drv.getDriverClass()));
         writer.writeEndElement();
      }

      if (drv.getDataSourceClass() != null)
      {
         writer.writeStartElement(XML.ELEMENT_DATASOURCE_CLASS);
         writer.writeCharacters(drv.getValue(XML.ELEMENT_DATASOURCE_CLASS, drv.getDataSourceClass()));
         writer.writeEndElement();
      }

      if (drv.getXaDataSourceClass() != null)
      {
         writer.writeStartElement(XML.ELEMENT_XA_DATASOURCE_CLASS);
         writer.writeCharacters(drv.getValue(XML.ELEMENT_XA_DATASOURCE_CLASS, drv.getXaDataSourceClass()));
         writer.writeEndElement();
      }

      writer.writeEndElement();
   }

   /**
    * Store a pool
    * @param pool The pool
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storePool(DsPool pool, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_POOL);

      if (pool.getMinPoolSize() != null && (pool.hasExpression(XML.ELEMENT_MIN_POOL_SIZE) ||
                                            !Defaults.MIN_POOL_SIZE.equals(pool.getMinPoolSize())))
      {
         writer.writeStartElement(XML.ELEMENT_MIN_POOL_SIZE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_MIN_POOL_SIZE, pool.getMinPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getInitialPoolSize() != null)
      {
         writer.writeStartElement(XML.ELEMENT_INITIAL_POOL_SIZE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_INITIAL_POOL_SIZE, pool.getInitialPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getMaxPoolSize() != null && (pool.hasExpression(XML.ELEMENT_MAX_POOL_SIZE) ||
                                            !Defaults.MAX_POOL_SIZE.equals(pool.getMaxPoolSize())))
      {
         writer.writeStartElement(XML.ELEMENT_MAX_POOL_SIZE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_MAX_POOL_SIZE, pool.getMaxPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.isPrefill() != null && (pool.hasExpression(XML.ELEMENT_PREFILL) ||
                                       !Defaults.PREFILL.equals(pool.isPrefill())))
      {
         writer.writeStartElement(XML.ELEMENT_PREFILL);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_PREFILL, pool.isPrefill().toString()));
         writer.writeEndElement();
      }

      if (pool.getFlushStrategy() != null && (pool.hasExpression(XML.ELEMENT_FLUSH_STRATEGY) ||
                                              !Defaults.FLUSH_STRATEGY.equals(pool.getFlushStrategy())))
      {
         writer.writeStartElement(XML.ELEMENT_FLUSH_STRATEGY);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_FLUSH_STRATEGY, pool.getFlushStrategy().toString()));
         writer.writeEndElement();
      }

      if (pool.getCapacity() != null)
         storeCapacity(pool.getCapacity(), writer);

      if (pool.getConnectionListener() != null)
      {
         writer.writeStartElement(XML.ELEMENT_CONNECTION_LISTENER);
         writer.writeAttribute(XML.ATTRIBUTE_CLASS_NAME,
                               pool.getConnectionListener().getValue(XML.ATTRIBUTE_CLASS_NAME,
                                                                     pool.getConnectionListener().getClassName()));

         if (!pool.getConnectionListener().getConfigPropertiesMap().isEmpty())
         {
            Iterator<Map.Entry<String, String>> it =
               pool.getConnectionListener().getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               writer.writeStartElement(XML.ELEMENT_CONFIG_PROPERTY);
               writer.writeAttribute(XML.ATTRIBUTE_NAME, entry.getKey());
               writer.writeCharacters(pool.getConnectionListener().getValue(XML.ELEMENT_CONFIG_PROPERTY,
                                                                            entry.getKey(), entry.getValue()));
               writer.writeEndElement();
            }
         }

         writer.writeEndElement();
      }

      writer.writeEndElement();
   }

   /**
    * Store a XA pool
    * @param pool The pool
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeXaPool(DsXaPool pool, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_XA_POOL);

      if (pool.getMinPoolSize() != null && (pool.hasExpression(XML.ELEMENT_MIN_POOL_SIZE) ||
                                            !Defaults.MIN_POOL_SIZE.equals(pool.getMinPoolSize())))
      {
         writer.writeStartElement(XML.ELEMENT_MIN_POOL_SIZE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_MIN_POOL_SIZE, pool.getMinPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getInitialPoolSize() != null)
      {
         writer.writeStartElement(XML.ELEMENT_INITIAL_POOL_SIZE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_INITIAL_POOL_SIZE, pool.getInitialPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getMaxPoolSize() != null && (pool.hasExpression(XML.ELEMENT_MAX_POOL_SIZE) ||
                                            !Defaults.MAX_POOL_SIZE.equals(pool.getMaxPoolSize())))
      {
         writer.writeStartElement(XML.ELEMENT_MAX_POOL_SIZE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_MAX_POOL_SIZE, pool.getMaxPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.isPrefill() != null && (pool.hasExpression(XML.ELEMENT_PREFILL) ||
                                       !Defaults.PREFILL.equals(pool.isPrefill())))
      {
         writer.writeStartElement(XML.ELEMENT_PREFILL);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_PREFILL, pool.isPrefill().toString()));
         writer.writeEndElement();
      }

      if (pool.getFlushStrategy() != null && (pool.hasExpression(XML.ELEMENT_FLUSH_STRATEGY) ||
                                              !Defaults.FLUSH_STRATEGY.equals(pool.getFlushStrategy())))
      {
         writer.writeStartElement(XML.ELEMENT_FLUSH_STRATEGY);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_FLUSH_STRATEGY, pool.getFlushStrategy().toString()));
         writer.writeEndElement();
      }

      if (pool.getCapacity() != null)
         storeCapacity(pool.getCapacity(), writer);

      if (pool.getConnectionListener() != null)
      {
         storeExtension(pool.getConnectionListener(), writer, XML.ELEMENT_CONNECTION_LISTENER);
      }

      if (pool.isIsSameRmOverride() != null)
      {
         writer.writeStartElement(XML.ELEMENT_IS_SAME_RM_OVERRIDE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_IS_SAME_RM_OVERRIDE, pool.isIsSameRmOverride().toString()));
         writer.writeEndElement();
      }

      if (pool.isPadXid() != null && (pool.hasExpression(XML.ELEMENT_PAD_XID) ||
                                      !Defaults.PAD_XID.equals(pool.isPadXid())))
      {
         writer.writeStartElement(XML.ELEMENT_PAD_XID);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_PAD_XID, pool.isPadXid().toString()));
         writer.writeEndElement();
      }

      if (pool.isWrapXaResource() != null && (pool.hasExpression(XML.ELEMENT_WRAP_XA_RESOURCE) ||
                                              !Defaults.WRAP_XA_RESOURCE.equals(pool.isWrapXaResource())))
      {
         writer.writeStartElement(XML.ELEMENT_WRAP_XA_RESOURCE);
         writer.writeCharacters(pool.getValue(XML.ELEMENT_WRAP_XA_RESOURCE, pool.isWrapXaResource().toString()));
         writer.writeEndElement();
      }

      writer.writeEndElement();
   }

   /**
    * Store security
    * @param s The security
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeSecurity(DsSecurity s, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_SECURITY);

      if (s.getUserName() != null)
      {
         writer.writeStartElement(XML.ELEMENT_USER_NAME);
         writer.writeCharacters(s.getValue(XML.ELEMENT_USER_NAME, s.getUserName()));
         writer.writeEndElement();

         writer.writeStartElement(XML.ELEMENT_PASSWORD);
         writer.writeCharacters(s.getValue(XML.ELEMENT_PASSWORD, s.getPassword()));
         writer.writeEndElement();
      }
      else if (s.getSecurityDomain() != null)
      {
         writer.writeStartElement(XML.ELEMENT_SECURITY_DOMAIN);
         writer.writeCharacters(s.getValue(XML.ELEMENT_SECURITY_DOMAIN, s.getSecurityDomain()));
         writer.writeEndElement();
      }

      if (s.getReauthPlugin() != null)
      {
         storeExtension(s.getReauthPlugin(), writer, XML.ELEMENT_REAUTH_PLUGIN);
      }

      writer.writeEndElement();
   }

   /**
    * Store validation
    * @param v The validation
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeValidation(Validation v, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_VALIDATION);

      if (v.getValidConnectionChecker() != null)
      {
         storeExtension(v.getValidConnectionChecker(), writer, XML.ELEMENT_VALID_CONNECTION_CHECKER);
      }

      if (v.getCheckValidConnectionSql() != null)
      {
         writer.writeStartElement(XML.ELEMENT_CHECK_VALID_CONNECTION_SQL);
         writer.writeCharacters(v.getValue(XML.ELEMENT_CHECK_VALID_CONNECTION_SQL, v.getCheckValidConnectionSql()));
         writer.writeEndElement();
      }

      if (v.isValidateOnMatch() != null)
      {
         writer.writeStartElement(XML.ELEMENT_VALIDATE_ON_MATCH);
         writer.writeCharacters(v.getValue(XML.ELEMENT_VALIDATE_ON_MATCH, v.isValidateOnMatch().toString()));
         writer.writeEndElement();
      }

      if (v.isBackgroundValidation() != null)
      {
         writer.writeStartElement(XML.ELEMENT_BACKGROUND_VALIDATION);
         writer.writeCharacters(v.getValue(XML.ELEMENT_BACKGROUND_VALIDATION, v.isBackgroundValidation().toString()));
         writer.writeEndElement();
      }

      if (v.getBackgroundValidationMillis() != null)
      {
         writer.writeStartElement(XML.ELEMENT_BACKGROUND_VALIDATION_MILLIS);
         writer.writeCharacters(v.getValue(XML.ELEMENT_BACKGROUND_VALIDATION_MILLIS,
                                           v.getBackgroundValidationMillis().toString()));
         writer.writeEndElement();
      }

      if (v.isUseFastFail() != null)
      {
         writer.writeStartElement(XML.ELEMENT_USE_FAST_FAIL);
         writer.writeCharacters(v.getValue(XML.ELEMENT_USE_FAST_FAIL, v.isUseFastFail().toString()));
         writer.writeEndElement();
      }

      if (v.getStaleConnectionChecker() != null)
      {
         storeExtension(v.getStaleConnectionChecker(), writer, XML.ELEMENT_STALE_CONNECTION_CHECKER);
      }

      if (v.getExceptionSorter() != null)
      {
         storeExtension(v.getExceptionSorter(), writer, XML.ELEMENT_EXCEPTION_SORTER);
      }

      writer.writeEndElement();
   }

   /**
    * Store timeout
    * @param t The timeout
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeTimeout(Timeout t, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_TIMEOUT);

      if (t.getBlockingTimeoutMillis() != null)
      {
         writer.writeStartElement(XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS);
         writer.writeCharacters(t.getValue(XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS,
                                           t.getBlockingTimeoutMillis().toString()));
         writer.writeEndElement();
      }

      if (t.getIdleTimeoutMinutes() != null)
      {
         writer.writeStartElement(XML.ELEMENT_IDLE_TIMEOUT_MINUTES);
         writer.writeCharacters(t.getValue(XML.ELEMENT_IDLE_TIMEOUT_MINUTES, t.getIdleTimeoutMinutes().toString()));
         writer.writeEndElement();
      }

      if (t.isSetTxQueryTimeout() != null && Boolean.TRUE.equals(t.isSetTxQueryTimeout()))
      {
         writer.writeEmptyElement(XML.ELEMENT_SET_TX_QUERY_TIMEOUT);
      }

      if (t.getQueryTimeout() != null)
      {
         writer.writeStartElement(XML.ELEMENT_QUERY_TIMEOUT);
         writer.writeCharacters(t.getValue(XML.ELEMENT_QUERY_TIMEOUT, t.getQueryTimeout().toString()));
         writer.writeEndElement();
      }

      if (t.getUseTryLock() != null)
      {
         writer.writeStartElement(XML.ELEMENT_USE_TRY_LOCK);
         writer.writeCharacters(t.getValue(XML.ELEMENT_USE_TRY_LOCK, t.getUseTryLock().toString()));
         writer.writeEndElement();
      }

      if (t.getAllocationRetry() != null)
      {
         writer.writeStartElement(XML.ELEMENT_ALLOCATION_RETRY);
         writer.writeCharacters(t.getValue(XML.ELEMENT_ALLOCATION_RETRY, t.getAllocationRetry().toString()));
         writer.writeEndElement();
      }

      if (t.getAllocationRetryWaitMillis() != null)
      {
         writer.writeStartElement(XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS);
         writer.writeCharacters(t.getValue(XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS,
                                           t.getAllocationRetryWaitMillis().toString()));
         writer.writeEndElement();
      }

      if (t.getXaResourceTimeout() != null)
      {
         writer.writeStartElement(XML.ELEMENT_XA_RESOURCE_TIMEOUT);
         writer.writeCharacters(t.getValue(XML.ELEMENT_XA_RESOURCE_TIMEOUT, t.getXaResourceTimeout().toString()));
         writer.writeEndElement();
      }

      writer.writeEndElement();
   }

   /**
    * Store statement
    * @param s The statement
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeStatement(Statement s, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_STATEMENT);

      if (s.getTrackStatements() != null)
      {
         writer.writeStartElement(XML.ELEMENT_TRACK_STATEMENTS);
         writer.writeCharacters(s.getValue(XML.ELEMENT_TRACK_STATEMENTS, s.getTrackStatements().toString()));
         writer.writeEndElement();
      }

      if (s.getPreparedStatementsCacheSize() != null)
      {
         writer.writeStartElement(XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE);
         writer.writeCharacters(s.getValue(XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE,
                                           s.getPreparedStatementsCacheSize().toString()));
         writer.writeEndElement();
      }

      if (s.isSharePreparedStatements() != null && Boolean.TRUE.equals(s.isSharePreparedStatements()))
      {
         writer.writeEmptyElement(XML.ELEMENT_SHARE_PREPARED_STATEMENTS);
      }

      writer.writeEndElement();
   }

   /**
    * Store recovery
    * @param r The recovery
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   @Override
   protected void storeRecovery(Recovery r, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(XML.ELEMENT_RECOVERY);

      if (r.isNoRecovery() != null)
         writer.writeAttribute(XML.ATTRIBUTE_NO_RECOVERY,
                               r.getValue(XML.ATTRIBUTE_NO_RECOVERY, r.isNoRecovery().toString()));

      if (r.getCredential() != null)
      {
         writer.writeStartElement(XML.ELEMENT_RECOVERY_CREDENTIAL);

         Credential c = (Credential)r.getCredential();
         if (c.getUserName() != null)
         {
            writer.writeStartElement(XML.ELEMENT_USER_NAME);
            writer.writeCharacters(c.getValue(XML.ELEMENT_USER_NAME,
                                              c.getUserName()));
            writer.writeEndElement();

            writer.writeStartElement(XML.ELEMENT_PASSWORD);
            writer.writeCharacters(c.getValue(XML.ELEMENT_PASSWORD,
                                              c.getPassword()));
            writer.writeEndElement();
         }
         else
         {
            writer.writeStartElement(XML.ELEMENT_SECURITY_DOMAIN);
            writer.writeCharacters(c.getValue(XML.ELEMENT_SECURITY_DOMAIN,
                                              c.getSecurityDomain()));
            writer.writeEndElement();
         }
         writer.writeEndElement();
      }

      if (r.getPlugin() != null)
      {
         storeExtension(r.getPlugin(), writer, CommonXML.ELEMENT_RECOVERY_PLUGIN);
      }

      writer.writeEndElement();
   }
}
