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
package org.jboss.jca.common.metadata.ds.v13;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.v12.DsPool;
import org.jboss.jca.common.api.metadata.ds.v12.DsXaPool;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * A datasource parser
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DsParser extends org.jboss.jca.common.metadata.ds.v12.DsParser implements MetadataParser<DataSources>
{

   @Override
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

      for (org.jboss.jca.common.api.metadata.ds.v13.XaDataSource.Attribute attribute :
              org.jboss.jca.common.api.metadata.ds.v13.XaDataSource.Attribute.values())
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
                                              jndiName, spy, useCcm, connectable, tracking, xaDataSourceProperty,
                                              xaDataSourceClass, driver, newConnectionSql, xaPool, recovery);
               }
               else
               {
                  if (org.jboss.jca.common.api.metadata.ds.v13.XaDataSource.Tag.forName(reader.getLocalName()) ==
                      org.jboss.jca.common.api.metadata.ds.v13.XaDataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (org.jboss.jca.common.api.metadata.ds.v13.XaDataSource.Tag.forName(reader.getLocalName()))
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

   @Override
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

      for (org.jboss.jca.common.api.metadata.ds.v13.DataSource.Attribute attribute :
              org.jboss.jca.common.api.metadata.ds.v13.DataSource.Attribute.values())
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
                                            enabled, jndiName, spy, useCcm, jta, connectable, tracking, pool);
               }
               else
               {
                  if (org.jboss.jca.common.api.metadata.ds.v13.DataSource.Tag.forName(reader.getLocalName()) ==
                      org.jboss.jca.common.api.metadata.ds.v13.DataSource.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (org.jboss.jca.common.api.metadata.ds.v13.DataSource.Tag.forName(reader.getLocalName()))
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
