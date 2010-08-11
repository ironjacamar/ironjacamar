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

import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.RecoverySettings;
import org.jboss.jca.common.api.metadata.ds.SecuritySettings;
import org.jboss.jca.common.api.metadata.ds.SecuritySettings.SecurityManager;
import org.jboss.jca.common.api.metadata.ds.StatementSettings;
import org.jboss.jca.common.api.metadata.ds.StatementSettings.TrackStatementsEnum;
import org.jboss.jca.common.api.metadata.ds.TimeOutSettings;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.ValidationSettings;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.ds.XaDataSource.Attribute;
import org.jboss.jca.common.metadata.AbstractParser;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

   @Override
   public DataSources parse(InputStream xmlInputStream) throws Exception
   {

      XMLStreamReader reader = null;
      DataSources dataSources = null;

      try
      {
         XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         reader = inputFactory.createXMLStreamReader(xmlInputStream);

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
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }

               break;
            }
            default :
               throw new IllegalStateException();
         }
      }
      finally
      {
         if (reader != null)
            reader.close();
      }
      return dataSources;

   }

   private DataSources parseDataSources(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<XaDataSource> xaDataSource = new ArrayList<XaDataSource>();
      ArrayList<DataSource> datasource = new ArrayList<DataSource>();
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.DATASOURCES)
               {

                  return new DatasourcesImpl(datasource, xaDataSource);
               }
               else
               {
                  if (DataSources.Tag.forName(reader.getLocalName()) == DataSources.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
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
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private XaDataSource parseXADataSource(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      boolean prefill = false;
      String userName = null;
      String password = null;
      TransactionIsolation transactionIsolation = null;
      Map<String, String> xaDataSourceProperty = new HashMap<String, String>();
      TimeOutSettings timeOutSettings = null;
      SecuritySettings securitySettings = null;
      StatementSettings statementSettings = null;
      ValidationSettings validationSettings = null;
      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;

      boolean interleaving = false;
      boolean isSameRmOverrideValue = false;
      String xaDataSourceClass = null;
      RecoverySettings recoverySettings = null;

      //attributes reading

      boolean useJavaContext = false;
      String poolName = null;
      boolean enabled = true;
      String jndiName = null;

      for (Attribute attribute : XaDataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            case JNDINAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName());
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

                  return new XADataSourceImpl(minPoolSize, maxPoolSize, prefill, userName, password,
                        xaDataSourceProperty, xaDataSourceClass, transactionIsolation, isSameRmOverrideValue,
                        interleaving, recoverySettings, timeOutSettings, securitySettings, statementSettings,
                        validationSettings, urlDelimiter, urlSelectorStrategyClassName, newConnectionSql,
                        useJavaContext, poolName, enabled, jndiName);
               }
               else
               {
                  if (XaDataSource.Tag.forName(reader.getLocalName()) == XaDataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XaDataSource.Tag.forName(reader.getLocalName()))
               {
                  case XADATASOURCEPROPERTY : {
                     xaDataSourceProperty.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case XADATASOURCECLASS : {
                     xaDataSourceClass = elementAsString(reader);
                     break;
                  }
                  case MAXPOOLSIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MINPOOLSIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case NEWCONNECTIONSQL : {
                     newConnectionSql = elementAsString(reader);
                     break;
                  }
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case USERNAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case URLDELIMITER : {
                     urlDelimiter = elementAsString(reader);
                     break;
                  }
                  case URLSELECTORSTRATEGYCLASSNAME : {
                     urlSelectorStrategyClassName = elementAsString(reader);
                     break;
                  }
                  case TRANSACTIONISOLATION : {
                     transactionIsolation = TransactionIsolation.valueOf(elementAsString(reader));
                     break;
                  }
                  case SECURITYSETTINGS : {
                     securitySettings = parseSecuritySettings(reader);
                     break;
                  }
                  case STATEMENTSETTINGS : {
                     statementSettings = parseStatementSettings(reader);
                     break;
                  }
                  case TIMEOUTSETTINGS : {
                     timeOutSettings = parseTimeOutSettings(reader);
                     break;
                  }
                  case VALIDATIONSETTINGS : {
                     validationSettings = parseValidationSetting(reader);
                     break;
                  }
                  case INTERLEAVING : {
                     interleaving = elementAsBoolean(reader);
                     break;
                  }
                  case ISSAMERMOVERRIDEVALUE : {
                     isSameRmOverrideValue = elementAsBoolean(reader);
                     break;
                  }
                  case RECOVERYSETTINGS : {
                     recoverySettings = parseRecoverySettings(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private RecoverySettings parseRecoverySettings(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String recoverUserName = null;
      String recoverPassword = null;
      String recoverSecurityDomain = null;
      boolean noRecover = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XaDataSource.Tag.forName(reader.getLocalName()) == XaDataSource.Tag.RECOVERYSETTINGS)
               {


                  return new RecoverySettingsImpl(noRecover, recoverUserName, recoverPassword, recoverSecurityDomain);
               }
               else
               {
                  if (RecoverySettings.Tag.forName(reader.getLocalName()) == RecoverySettings.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (RecoverySettings.Tag.forName(reader.getLocalName()))
               {
                  case NORECOVER : {
                     noRecover = elementAsBoolean(reader);
                     break;
                  }
                  case RECOVERPASSWORD : {
                     recoverPassword = elementAsString(reader);
                     break;
                  }
                  case RECOVERUSERNAME : {
                     recoverUserName = elementAsString(reader);
                     break;
                  }
                  case RECOVERSECURITYDOMAIN : {
                     recoverSecurityDomain = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private DataSource parseDataSource(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      boolean prefill = false;
      String userName = null;
      String password = null;
      String connectionUrl = null;
      String driverClass = null;
      TransactionIsolation transactionIsolation = null;
      Map<String, String> connectionProperties = new HashMap<String, String>();
      TimeOutSettings timeOutSettings = null;
      SecuritySettings securitySettings = null;
      StatementSettings statementSettings = null;
      ValidationSettings validationSettings = null;
      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;

      //attributes reading
      boolean useJavaContext = false;
      String poolName = null;
      boolean enabled = true;
      String jndiName = null;

      for (Attribute attribute : XaDataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            case JNDINAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName());
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

                  return new DataSourceImpl(minPoolSize, maxPoolSize, prefill, userName, password, connectionUrl,
                        driverClass, transactionIsolation, connectionProperties, timeOutSettings, securitySettings,
                        statementSettings, validationSettings, urlDelimiter, urlSelectorStrategyClassName,
                        newConnectionSql, useJavaContext, poolName, enabled, jndiName);
               }
               else
               {
                  if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (DataSource.Tag.forName(reader.getLocalName()))
               {
                  case CONNECTIONPROPERTY : {
                     connectionProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case CONNECTIONURL : {
                     connectionUrl = elementAsString(reader);
                     break;
                  }
                  case DRIVERCLASS : {
                     driverClass = elementAsString(reader);
                     break;
                  }
                  case MAXPOOLSIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case NEWCONNECTIONSQL : {
                     newConnectionSql = elementAsString(reader);
                     break;
                  }
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case USERNAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case URLDELIMITER : {
                     urlDelimiter = elementAsString(reader);
                     break;
                  }
                  case URLSELECTORSTRATEGYCLASSNAME : {
                     urlSelectorStrategyClassName = elementAsString(reader);
                     break;
                  }
                  case TRANSACTIONISOLATION : {
                     transactionIsolation = TransactionIsolation.valueOf(elementAsString(reader));
                     break;
                  }
                  case SECURITYSETTINGS : {
                     securitySettings = parseSecuritySettings(reader);
                     break;
                  }
                  case STATEMENTSETTINGS : {
                     statementSettings = parseStatementSettings(reader);
                     break;
                  }
                  case TIMEOUTSETTINGS : {
                     timeOutSettings = parseTimeOutSettings(reader);
                     break;
                  }
                  case VALIDATIONSETTINGS : {
                     validationSettings = parseValidationSetting(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private ValidationSettings parseValidationSetting(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      boolean validateOnMatch = false;
      boolean useFastFail = false;
      Long backgroundValidationMinutes = null;
      String staleConnectionCheckerClassName = null;
      boolean backgroundValidation = false;
      String checkValidConnectionSql = null;
      String validConnectionCheckerClassName = null;
      String exceptionSorterClassName = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.VALIDATIONSETTINGS)
               {

                  return new ValidationSettingsImpl(validConnectionCheckerClassName, checkValidConnectionSql,
                        validateOnMatch, backgroundValidation, backgroundValidationMinutes, useFastFail,
                        staleConnectionCheckerClassName, exceptionSorterClassName);

               }
               else
               {
                  if (ValidationSettings.Tag.forName(reader.getLocalName()) == ValidationSettings.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ValidationSettings.Tag.forName(reader.getLocalName()))
               {
                  case BACKGROUNDVALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case BACKGROUNDVALIDATIONMINUTES : {
                     backgroundValidationMinutes = elementAsLong(reader);
                     break;
                  }
                  case CHECKVALIDCONNECTIONSQL : {
                     checkValidConnectionSql = elementAsString(reader);
                     break;
                  }
                  case EXCEPTIONSORTERCLASSNAME : {
                     exceptionSorterClassName = elementAsString(reader);
                     break;
                  }
                  case STALECONNECTIONCHECKERCLASSNAME : {
                     staleConnectionCheckerClassName = elementAsString(reader);
                     break;
                  }
                  case USEFASTFAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  case VALIDATEONMATCH : {
                     validateOnMatch = elementAsBoolean(reader);
                     break;
                  }
                  case VALIDCONNECTIONCHECKERCLASSNAME : {
                     validConnectionCheckerClassName = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private TimeOutSettings parseTimeOutSettings(XMLStreamReader reader) throws XMLStreamException, ParserException
   {

      Long blockingTimeoutMillis = null;
      Long idleTimeoutMinutes = null;
      boolean setTxQuertTimeout = false;
      Long queryTimeout = null;
      Long allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Long useTryLock = null;
      Long xaResourceTimeout = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.TIMEOUTSETTINGS)
               {

                  return new TimeOutSettingsImpl(blockingTimeoutMillis, idleTimeoutMinutes, setTxQuertTimeout,
                        queryTimeout, useTryLock, allocationRetry, allocationRetryWaitMillis, xaResourceTimeout);
               }
               else
               {
                  if (TimeOutSettings.Tag.forName(reader.getLocalName()) == TimeOutSettings.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (TimeOutSettings.Tag.forName(reader.getLocalName()))
               {
                  case ALLOCATIONRETRY : {
                     allocationRetry = elementAsLong(reader);
                     break;
                  }
                  case ALLOCATIONRETRYWAITMILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case BLOCKINGTIMEOUTMILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case IDLETIMEOUTMINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  case QUERYTIMEOUT : {
                     queryTimeout = elementAsLong(reader);
                     break;
                  }
                  case SETTXQUERYTIMEOUT : {
                     setTxQuertTimeout = elementAsBoolean(reader);
                     break;
                  }
                  case USETRYLOCK : {
                     useTryLock = elementAsLong(reader);
                     break;
                  }
                  case XARESOURCETIMEOUT : {
                     xaResourceTimeout = elementAsLong(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private StatementSettings parseStatementSettings(XMLStreamReader reader) throws XMLStreamException, ParserException
   {

      Long preparedStatementsCacheSize = null;
      boolean sharePreparedStatements = false;
      TrackStatementsEnum trackStatements = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.STATEMENTSETTINGS)
               {

                  return new StatementSettingsImpl(sharePreparedStatements, preparedStatementsCacheSize,
                        trackStatements);
               }
               else
               {
                  if (StatementSettings.Tag.forName(reader.getLocalName()) == StatementSettings.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (StatementSettings.Tag.forName(reader.getLocalName()))
               {
                  case PREPAREDSTATEMENTCACHESIZE : {
                     preparedStatementsCacheSize = elementAsLong(reader);
                     break;
                  }
                  case TRACKSTATEMENTS : {
                     trackStatements = TrackStatementsEnum.valueOf(elementAsString(reader));
                     break;
                  }
                  case SHAREPREPAREDSTATEMENTS : {
                     sharePreparedStatements = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private SecuritySettings parseSecuritySettings(XMLStreamReader reader) throws XMLStreamException, ParserException
   {

      SecurityManager securityManager = null;
      String securityDomain = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.SECURITYSETTINGS)
               {

                  return new SecuritySettingsImpl(securityManager, securityDomain);
               }
               else
               {
                  if (SecuritySettings.Tag.forName(reader.getLocalName()) == SecuritySettings.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (SecuritySettings.Tag.forName(reader.getLocalName()))
               {
                  case SECURITYDOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  case SECURITYMANAGER : {
                     securityManager = SecurityManager.valueOf(elementAsString(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
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
