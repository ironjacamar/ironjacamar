/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.Driver;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.AbstractParser;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.logging.Messages;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 * A DsParser.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class DsParser extends AbstractParser implements MetadataParser<DataSources>
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   @Override
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

   @Override
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

   private DataSources parseDataSources(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      ArrayList<XaDataSource> xaDataSource = new ArrayList<XaDataSource>();
      ArrayList<DataSource> datasource = new ArrayList<DataSource>();
      HashMap<String, Driver> drivers = new HashMap<String, Driver>();
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

   private Driver parseDriver(XMLStreamReader reader) throws XMLStreamException, ParserException,
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

   private XaDataSource parseXADataSource(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      TransactionIsolation transactionIsolation = null;
      Map<String, String> xaDataSourceProperty = new HashMap<String, String>();
      TimeOut timeOutSettings = null;
      DsSecurity securitySettings = null;
      Statement statementSettings = null;
      Validation validationSettings = null;
      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;
      CommonXaPool xaPool = null;
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

      for (XaDataSource.Attribute attribute : XaDataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), true);
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
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case SPY : {
               spy = attributeAsBoolean(reader, attribute.getLocalName(), false);
               break;
            }
            case USE_CCM : {
               useCcm = attributeAsBoolean(reader, attribute.getLocalName(), true);
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
                                              statementSettings, validationSettings, urlDelimiter,
                                              urlSelectorStrategyClassName, useJavaContext, poolName, enabled,
                                              jndiName, spy, useCcm, xaDataSourceProperty, xaDataSourceClass, driver,
                                              newConnectionSql, xaPool, recovery);
               }
               else
               {
                  if (XaDataSource.Tag.forName(reader.getLocalName()) == XaDataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XaDataSource.Tag.forName(reader.getLocalName()))
               {
                  case XA_DATASOURCE_PROPERTY : {
                     xaDataSourceProperty.put(attributeAsString(reader, "name"), elementAsString(reader));
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
                  case URL_SELECTOR_STRATEGY_CLASS_NAME : {
                     urlSelectorStrategyClassName = elementAsString(reader);
                     break;
                  }
                  case TRANSACTION_ISOLATION : {
                     transactionIsolation = TransactionIsolation.valueOf(elementAsString(reader));
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

   private DsSecurity parseDsSecurity(XMLStreamReader reader) throws XMLStreamException, ParserException,
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
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.SECURITY)
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

   private DataSource parseDataSource(XMLStreamReader reader) throws XMLStreamException, ParserException,
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
      CommonPool pool = null;

      //attributes reading
      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String poolName = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      Boolean spy = Defaults.SPY;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean jta = Defaults.JTA;

      for (DataSource.Attribute attribute : DataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), true);
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
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case SPY : {
               spy = attributeAsBoolean(reader, attribute.getLocalName(), false);
               break;
            }
            case USE_CCM : {
               useCcm = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case JTA : {
               jta = attributeAsBoolean(reader, attribute.getLocalName(), true);
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
                                            enabled, jndiName, spy, useCcm, jta, pool);
               }
               else
               {
                  if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (DataSource.Tag.forName(reader.getLocalName()))
               {
                  case CONNECTION_PROPERTY : {
                     connectionProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
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
                     transactionIsolation = TransactionIsolation.valueOf(elementAsString(reader));
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

   private Validation parseValidationSetting(XMLStreamReader reader) throws XMLStreamException, ParserException,
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
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.VALIDATION)
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

   private TimeOut parseTimeOutSettings(XMLStreamReader reader) throws XMLStreamException, ParserException,
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
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.TIMEOUT)
               {

                  return new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                                         allocationRetryWaitMillis, xaResourceTimeout, setTxQuertTimeout,
                                         queryTimeout, useTryLock);
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
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   private Statement parseStatementSettings(XMLStreamReader reader) throws XMLStreamException, ParserException,
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
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.STATEMENT)
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
   * A Tag.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
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

      private final String name;

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
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }
}
