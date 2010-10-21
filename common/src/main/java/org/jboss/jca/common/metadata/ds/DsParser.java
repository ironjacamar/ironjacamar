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

import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.ds.XaDataSource.Attribute;
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

      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      reader = inputFactory.createXMLStreamReader(xmlInputStream);
      return parse(reader);
   }

   @Override
   public DataSources parse(XMLStreamReader reader) throws Exception
   {

      DataSources dataSources = null;

      try
      {

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
      TransactionIsolation transactionIsolation = null;
      Map<String, String> xaDataSourceProperty = new HashMap<String, String>();
      TimeOut timeOutSettings = null;
      CommonSecurity securitySettings = null;
      Statement statementSettings = null;
      Validation validationSettings = null;
      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;
      CommonXaPool xaPool = null;

      String xaDataSourceClass = null;
      String module = null;

      //attributes reading

      boolean useJavaContext = true;
      String poolName = null;
      boolean enabled = true;
      String jndiName = null;

      for (Attribute attribute : XaDataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), true);
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
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), true);
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
                                              jndiName, xaDataSourceProperty, xaDataSourceClass, module, 
                                              newConnectionSql, xaPool);
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
                  case MODULE : {
                     module = elementAsString(reader);
                     break;
                  }
                  case XA_POOL : {
                     xaPool = parseXaPool(reader);
                     break;
                  }
                  case NEWCONNECTIONSQL : {
                     newConnectionSql = elementAsString(reader);
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
                  case SECURITY : {
                     securitySettings = parseSecuritySettings(reader);
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
      String connectionUrl = null;
      String driverClass = null;
      String module = null;
      TransactionIsolation transactionIsolation = null;
      Map<String, String> connectionProperties = new HashMap<String, String>();
      TimeOut timeOutSettings = null;
      CommonSecurity securitySettings = null;
      Statement statementSettings = null;
      Validation validationSettings = null;
      String urlDelimiter = null;
      String urlSelectorStrategyClassName = null;
      String newConnectionSql = null;
      CommonPool pool = null;

      //attributes reading
      boolean useJavaContext = true;
      String poolName = null;
      boolean enabled = true;
      String jndiName = null;

      for (Attribute attribute : XaDataSource.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), true);
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
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), true);
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

                  return new DataSourceImpl(connectionUrl, driverClass, module, transactionIsolation, 
                                            connectionProperties, timeOutSettings, securitySettings, 
                                            statementSettings, validationSettings,
                                            urlDelimiter, urlSelectorStrategyClassName, newConnectionSql,
                                            useJavaContext, poolName, enabled, jndiName, pool);
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
                  case MODULE : {
                     module = elementAsString(reader);
                     break;
                  }
                  case POOL : {
                     pool = parsePool(reader);
                     break;
                  }
                  case NEWCONNECTIONSQL : {
                     newConnectionSql = elementAsString(reader);
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
                  case SECURITY : {
                     securitySettings = parseSecuritySettings(reader);
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
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Validation parseValidationSetting(XMLStreamReader reader) throws XMLStreamException, ParserException
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
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.VALIDATION)
               {

                  return new ValidationImpl(backgroundValidation, backgroundValidationMinutes, useFastFail,
                                            validConnectionCheckerClassName, checkValidConnectionSql,
                                            validateOnMatch, staleConnectionCheckerClassName,
                                            exceptionSorterClassName);

               }
               else
               {
                  if (Validation.Tag.forName(reader.getLocalName()) == Validation.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Validation.Tag.forName(reader.getLocalName()))
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

   private TimeOut parseTimeOutSettings(XMLStreamReader reader) throws XMLStreamException, ParserException
   {

      Long blockingTimeoutMillis = null;
      Long idleTimeoutMinutes = null;
      boolean setTxQuertTimeout = false;
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
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (TimeOut.Tag.forName(reader.getLocalName()))
               {
                  case ALLOCATIONRETRY : {
                     allocationRetry = elementAsInteger(reader);
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
                     xaResourceTimeout = elementAsInteger(reader);
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

   private Statement parseStatementSettings(XMLStreamReader reader) throws XMLStreamException, ParserException
   {

      Long preparedStatementsCacheSize = null;
      boolean sharePreparedStatements = false;
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
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Statement.Tag.forName(reader.getLocalName()))
               {
                  case PREPAREDSTATEMENTCACHESIZE : {
                     preparedStatementsCacheSize = elementAsLong(reader);
                     break;
                  }
                  case TRACKSTATEMENTS : {
                     String elementString = elementAsString(reader);
                     trackStatements = TrackStatementsEnum.valueOf(elementString == null ? "FALSE" : elementString
                        .toUpperCase(Locale.US));
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
