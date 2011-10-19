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
package org.jboss.jca.as.converters;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Logger;

/**
 * Parser for legacy ds.xml
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class LegacyDsParser extends AbstractParser
{
   private static final String DEFAULT_SECURITY_DOMAIN = "other";
   private static Logger log = Logger.getLogger(LegacyDsParser.class);
   
   /**
    * parse xml string to datasources
    * @param xmlInputStream xml file input stream
    * @return DataSources
    * @throws Exception exception
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

   private void skipParse(XMLStreamReader reader) throws Exception
   {
      int level = 1;
      while (reader.hasNext() && level > 0)
      {
         switch (reader.next()) 
         {
            case END_ELEMENT : 
            {
               level--;
               break;
            }
            case START_ELEMENT : 
            {
               level++;
               break;
            }
            default :
               continue;
         }
      }
      log.info("Skip parse " + reader.getLocalName());
      //System.out.println("Skip parse " + reader.getLocalName());
   }
   

   private void notSupport(XMLStreamReader reader) throws Exception
   {
      log.info("So far not support " + reader.getLocalName());
   }

   private DataSources parse(XMLStreamReader reader) throws Exception
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
               case CONNECTION_FACTORIES : {
                  //throw new NotSupportedException();
                  notSupport(reader);
                  return null;
               }
               default :
                  throw new UnknownTagException(reader.getLocalName());
            }
            break;
         }
         default :
            throw new IllegalStateException();
      }

      return dataSources;

   }

   private DataSources parseDataSources(XMLStreamReader reader) throws Exception
   {
      ArrayList<NoTxDataSource> noTxDatasource = new ArrayList<NoTxDataSource>();
      ArrayList<LocalTxDataSource> localTxDatasource = new ArrayList<LocalTxDataSource>();
      ArrayList<XaDataSource> xaDataSource = new ArrayList<XaDataSource>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.DATASOURCES)
               {
                  return new DatasourcesImpl(noTxDatasource, localTxDatasource, xaDataSource);
               }
               else
               {
                  if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.UNKNOWN)
                  {
                     throw new UnknownTagException(reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (DataSources.Tag.forName(reader.getLocalName()))
               {
                  case NO_TX_DATASOURCE : {
                     noTxDatasource.add(parseLocalTxDataSource(reader));
                     break;
                  }
                  case LOCAL_TX_DATASOURCE : {
                     localTxDatasource.add(parseLocalTxDataSource(reader));
                     break;
                  }
                  case XA_DATASOURCE : {
                     xaDataSource.add(parseXADataSource(reader));
                     break;
                  }
                  default :
                     skipParse(reader);
               }
               break;
            }
         }
      }
      throw new ParserException(reader.getLocalName());
   }

   private XaDataSource parseXADataSource(XMLStreamReader reader) throws Exception
   {
      Map<String, String> xaDataSourceProperty = new HashMap<String, String>();

      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;
      
      //String driverClass = null;
      String driver = null;
      
      String xaDataSourceClass = null;

      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String poolName = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      Boolean spy = Defaults.SPY;
      Boolean useCcm = Defaults.USE_CCM;
      
      Boolean jta = Defaults.JTA;
      String userName = null;
      String password = null;
      
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      Boolean prefill = Defaults.PREFILL;
      
      Long blockingTimeoutMillis = null;
      Long idleTimeoutMinutes = null;
      Boolean setTxQueryTimeout = Defaults.SET_TX_QUERY_TIMEOUT;
      Long queryTimeout = null;
      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Long useTryLock = null;
      Integer xaResourceTimeout = null;
      
      Long preparedStatementsCacheSize = null;
      Boolean sharePreparedStatements = Defaults.SHARE_PREPARED_STATEMENTS;
      TrackStatementsEnum trackStatements = TrackStatementsEnum.FALSE;
      
      TransactionIsolation transactionIsolation = TransactionIsolation.TRANSACTION_NONE;
      String securityDomain = DEFAULT_SECURITY_DOMAIN;
      Extension reauthPlugin = null;
      
      Boolean backgroundValidation = Defaults.BACKGROUND_VALIDATION;
      Long backgroundValidationMillis = null;
      Boolean useFastFail = Defaults.USE_FAST_FAIL;
      Extension validConnectionChecker = null;
      String checkValidConnectionSql = null;
      Boolean validateOnMatch = Defaults.VALIDATE_ON_MATCH;
      Extension staleConnectionChecker = null;
      Extension exceptionSorter = null;
      
      Boolean useStrictMin = Defaults.USE_STRICT_MIN;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      
      Boolean isSameRmOverride = Defaults.IS_SAME_RM_OVERRIDE;
      Boolean interleaving = Defaults.INTERLEAVING;
      Boolean padXid = Defaults.PAD_XID;
      Boolean wrapXaDataSource = Defaults.WRAP_XA_RESOURCE;
      Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;
      
      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.XA_DATASOURCE)
               {
                  LegacyXaDataSourceImp xaDsImpl = new LegacyXaDataSourceImp(xaDataSourceClass,
                        driver, transactionIsolation, xaDataSourceProperty);
                  xaDsImpl.buildTimeOut(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry, 
                        allocationRetryWaitMillis, xaResourceTimeout, setTxQueryTimeout, queryTimeout, useTryLock);
                  xaDsImpl.buildDsSecurity(userName, password, securityDomain, reauthPlugin);
                  xaDsImpl.buildStatement(sharePreparedStatements, preparedStatementsCacheSize, trackStatements);
                  xaDsImpl.buildValidation(backgroundValidation, backgroundValidationMillis, useFastFail, 
                        validConnectionChecker, checkValidConnectionSql, validateOnMatch, staleConnectionChecker, 
                        exceptionSorter);
                  xaDsImpl.buildCommonPool(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy,
                        isSameRmOverride, interleaving, padXid, wrapXaDataSource, noTxSeparatePool);
                  xaDsImpl.buildOther(urlDelimiter, urlSelectorStrategyClassName, newConnectionSql, useJavaContext, 
                        poolName, enabled, jndiName, spy, useCcm, jta);
                  xaDsImpl.buildXaDataSourceImpl();
                  return xaDsImpl;
               }
               else
               {
                  if (XaDataSource.Tag.forName(reader.getLocalName()) == XaDataSource.Tag.UNKNOWN)
                  {
                     throw new UnknownTagException(reader.getLocalName());
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
                  
                  case VALID_CONNECTION_CHECKER : {
                     String classname = elementAsString(reader);
                     validConnectionChecker = new Extension(classname, null);
                     break;
                  }
                  case EXCEPTION_SORTER : {
                     String classname = elementAsString(reader);
                     exceptionSorter = new Extension(classname, null);
                     break;
                  }
                  case STALE_CONNECTION_CHECKER : {
                     String classname = elementAsString(reader);
                     staleConnectionChecker = new Extension(classname, null);
                     break;
                  }

                  case JNDI_NAME : {
                     poolName = elementAsString(reader);
                     jndiName = "java:jboss/datasources/" + poolName;
                     break;
                  }
                  case USE_JAVA_CONTEXT : {
                     useJavaContext = elementAsBoolean(reader);
                     break;
                  }
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case USER_NAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
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
                     setTxQueryTimeout = elementAsBoolean(reader);
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
                  case PREPARED_STATEMENT_CACHE_SIZE : {
                     preparedStatementsCacheSize = elementAsLong(reader);
                     break;
                  }
                  case TRACK_STATEMENTS : {
                     trackStatements = TrackStatementsEnum.TRUE;
                     break;
                  }
                  case SHARE_PREPARED_STATEMENTS : {
                     sharePreparedStatements = elementAsBoolean(reader);
                     break;
                  }
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
                  case USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  case VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     skipParse(reader);

               }
               break;
            }
            case CHARACTERS : {
               break;
            }
         }
      }
      throw new ParserException();
   }

   private LocalTxDataSource parseLocalTxDataSource(XMLStreamReader reader) throws Exception
   {
      String connectionUrl = null;
      String driverClass = null;
      String dataSourceClass = null;
      String driver = null;
      Map<String, String> connectionProperties = new HashMap<String, String>();

      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;

      //attributes reading
      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String poolName = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      Boolean spy = Defaults.SPY;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean jta = Defaults.JTA;
      String userName = null;
      String password = null;
      
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      Boolean prefill = Defaults.PREFILL;
      
      Long blockingTimeoutMillis = null;
      Long idleTimeoutMinutes = null;
      Long queryTimeout = null;
      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Long useTryLock = null;
      Integer xaResourceTimeout = null;
      
      Long preparedStatementsCacheSize = null;
      Boolean sharePreparedStatements = Defaults.SHARE_PREPARED_STATEMENTS;
      TrackStatementsEnum trackStatements = TrackStatementsEnum.FALSE;
      Boolean setTxQueryTimeout = Defaults.SET_TX_QUERY_TIMEOUT;
      
      TransactionIsolation transactionIsolation = TransactionIsolation.TRANSACTION_NONE;
      String securityDomain = DEFAULT_SECURITY_DOMAIN;
      Extension reauthPlugin = null;
      
      Boolean backgroundValidation = Defaults.BACKGROUND_VALIDATION;
      Long backgroundValidationMillis = null;
      Boolean useFastFail = Defaults.USE_FAST_FAIL;
      Extension validConnectionChecker = null;
      String checkValidConnectionSql = null;
      Boolean validateOnMatch = Defaults.VALIDATE_ON_MATCH;
      Extension staleConnectionChecker = null;
      Extension exceptionSorter = null;
      
      Boolean useStrictMin = Defaults.USE_STRICT_MIN;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      
      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.LOCAL_TX_DATASOURCE)
               {
                  LegacyTxDataSourceImpl txDsImpl = new LegacyTxDataSourceImpl(connectionUrl, 
                        driverClass, dataSourceClass, driver, transactionIsolation, connectionProperties);
                  txDsImpl.buildTimeOut(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry, 
                        allocationRetryWaitMillis, xaResourceTimeout, setTxQueryTimeout, queryTimeout, useTryLock);
                  txDsImpl.buildDsSecurity(userName, password, securityDomain, reauthPlugin);
                  txDsImpl.buildStatement(sharePreparedStatements, preparedStatementsCacheSize, trackStatements);
                  txDsImpl.buildValidation(backgroundValidation, backgroundValidationMillis, useFastFail, 
                        validConnectionChecker, checkValidConnectionSql, validateOnMatch, staleConnectionChecker, 
                        exceptionSorter);
                  txDsImpl.buildCommonPool(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy);
                  txDsImpl.buildOther(urlDelimiter, urlSelectorStrategyClassName, newConnectionSql, useJavaContext, 
                        poolName, enabled, jndiName, spy, useCcm, jta);
                  txDsImpl.buildDataSourceImpl();
                  return txDsImpl;
               }
               else
               {
                  if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.UNKNOWN)
                  {
                     throw new UnknownTagException(reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (LocalTxDataSource.Tag.forName(reader.getLocalName()))
               {
                  case VALID_CONNECTION_CHECKER : {
                     String classname = elementAsString(reader);
                     validConnectionChecker = new Extension(classname, null);
                     break;
                  }
                  case EXCEPTION_SORTER : {
                     String classname = elementAsString(reader);
                     exceptionSorter = new Extension(classname, null);
                     break;
                  }
                  case STALE_CONNECTION_CHECKER : {
                     String classname = elementAsString(reader);
                     staleConnectionChecker = new Extension(classname, null);
                     break;
                  }
                  
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
                  case JNDI_NAME : {
                     poolName = elementAsString(reader);
                     jndiName = "java:jboss/datasources/" + poolName;
                     break;
                  }
                  case USE_JAVA_CONTEXT : {
                     useJavaContext = elementAsBoolean(reader);
                     break;
                  }
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case USER_NAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
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
                     setTxQueryTimeout = elementAsBoolean(reader);
                     break;
                  }
                  case USE_TRY_LOCK : {
                     useTryLock = elementAsLong(reader);
                     break;
                  }
                  case PREPARED_STATEMENT_CACHE_SIZE : {
                     preparedStatementsCacheSize = elementAsLong(reader);
                     break;
                  }
                  case TRACK_STATEMENTS : {
                     trackStatements = TrackStatementsEnum.TRUE;
                     break;
                  }
                  case SHARE_PREPARED_STATEMENTS : {
                     sharePreparedStatements = elementAsBoolean(reader);
                     break;
                  }
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
                  case USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  case VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     skipParse(reader);
               }
               break;
            }
         }
      }
      throw new ParserException();
   }


   /**
   *
   * A Tag.
   */
   public enum Tag 
   {
      /**
       * always first
       */
      UNKNOWN(null),

      /**
       * datasources tag
       */
      DATASOURCES("datasources"),

      /**
       * datasources tag
       */
      CONNECTION_FACTORIES("connection-factories");

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
