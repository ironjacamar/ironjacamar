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
import org.ironjacamar.common.api.metadata.ds.DataSource;
import org.ironjacamar.common.api.metadata.ds.DsPool;
import org.ironjacamar.common.api.metadata.ds.DsSecurity;
import org.ironjacamar.common.api.metadata.ds.Statement;
import org.ironjacamar.common.api.metadata.ds.Timeout;
import org.ironjacamar.common.api.metadata.ds.TransactionIsolation;
import org.ironjacamar.common.api.metadata.ds.Validation;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A datasource implementation
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DataSourceImpl extends DataSourceAbstractImpl implements DataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** JTA */
   private Boolean jta;

   /** Connection URL */
   private String connectionUrl;

   /** Driver class */
   private String driverClass;

   /** DataSource class */
   private String dataSourceClass;

   /** Connection properties */
   private Map<String, String> connectionProperties;

   /** Pool */
   private DsPool pool;

   /**
    * Constructor
    *
    * @param connectionUrl connectionUrl
    * @param driverClass driverClass
    * @param dataSourceClass dataSourceClass
    * @param driver driver
    * @param transactionIsolation transactionIsolation
    * @param connectionProperties connectionProperties
    * @param timeout timeout
    * @param security security
    * @param statement statement
    * @param validation validation
    * @param urlDelimiter urlDelimiter
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param newConnectionSql newConnectionSql
    * @param id id
    * @param enabled enabled
    * @param jndiName jndiName
    * @param spy spy
    * @param useccm useccm
    * @param jta jta
    * @param connectable connectable
    * @param tracking tracking
    * @param pool pool
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public DataSourceImpl(String connectionUrl, String driverClass, String dataSourceClass, String driver,
                         TransactionIsolation transactionIsolation, Map<String, String> connectionProperties, 
                         Timeout timeout, DsSecurity security, Statement statement, Validation validation, 
                         String urlDelimiter, String urlSelectorStrategyClassName, String newConnectionSql, 
                         String id, Boolean enabled, String jndiName, 
                         Boolean spy, Boolean useccm, Boolean jta, Boolean connectable, Boolean tracking, DsPool pool,
                         Map<String, String> expressions)
      throws ValidateException
   {
      super(transactionIsolation, timeout, security, statement, validation, urlDelimiter, urlSelectorStrategyClassName,
            id, enabled, jndiName, spy, useccm, driver, newConnectionSql, connectable, tracking,
            expressions);

      this.jta = jta;
      this.connectionUrl = connectionUrl;
      this.driverClass = driverClass;
      this.dataSourceClass = dataSourceClass;
      if (connectionProperties != null)
      {
         this.connectionProperties = new HashMap<String, String>(connectionProperties.size());
         this.connectionProperties.putAll(connectionProperties);
      }
      else
      {
         this.connectionProperties = new HashMap<String, String>(0);
      }
      this.pool = pool;

      this.validate();
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isJTA()
   {
      return jta;
   }

   /**
    * {@inheritDoc}
    */
   public String getConnectionUrl()
   {
      return connectionUrl;
   }

   /**
    * {@inheritDoc}
    */
   public String getDriverClass()
   {
      return driverClass;
   }

   /**
    * {@inheritDoc}
    */
   public String getDataSourceClass()
   {
      return dataSourceClass;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, String> getConnectionProperties()
   {
      return Collections.unmodifiableMap(connectionProperties);
   }

   /**
    * {@inheritDoc}
    */
   public DsPool getPool()
   {
      return pool;
   }

   /**
    * Set the driverClass.
    *
    * @param driverClass The driverClass to set.
    */
   public void forceDriverClass(String driverClass)
   {
      this.driverClass = driverClass;
   }

   /**
    * Set the dataSourceClass.
    *
    * @param dataSourceClass The dataSourceClass to set.
    */
   public void forceDataSourceClass(String dataSourceClass)
   {
      this.dataSourceClass = dataSourceClass;
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException
   {
      if (this.driverClass != null && (this.connectionUrl == null || this.connectionUrl.trim().length() == 0))
         throw new ValidateException(bundle.requiredElementMissing(XML.ELEMENT_CONNECTION_URL, 
                                                                   this.getClass().getCanonicalName()));
      
      if ((this.driverClass == null || this.driverClass.trim().length() == 0) &&
          (this.dataSourceClass == null || this.dataSourceClass.trim().length() == 0) &&
          (this.driver == null || this.driver.trim().length() == 0))
         throw new ValidateException(bundle.requiredElementMissing(XML.ELEMENT_DRIVER_CLASS, 
                                                                   this.getClass().getCanonicalName()));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((jta == null) ? 0 : jta.hashCode());
      result = prime * result + ((connectionUrl == null) ? 0 : connectionUrl.hashCode());
      result = prime * result + ((driverClass == null) ? 0 : driverClass.hashCode());
      result = prime * result + ((dataSourceClass == null) ? 0 : dataSourceClass.hashCode());
      result = prime * result + ((connectionProperties == null) ? 0 : connectionProperties.hashCode());
      result = prime * result + ((pool == null) ? 0 : pool.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof DataSourceImpl))
         return false;
      DataSourceImpl other = (DataSourceImpl) obj;
      if (jta == null)
      {
         if (other.jta != null)
            return false;
      }
      else if (!jta.equals(other.jta))
         return false;
      if (connectionUrl == null)
      {
         if (other.connectionUrl != null)
            return false;
      }
      else if (!connectionUrl.equals(other.connectionUrl))
         return false;
      if (driverClass == null)
      {
         if (other.driverClass != null)
            return false;
      }
      else if (!driverClass.equals(other.driverClass))
         return false;
      if (dataSourceClass == null)
      {
         if (other.dataSourceClass != null)
            return false;
      }
      else if (!dataSourceClass.equals(other.dataSourceClass))
         return false;
      if (connectionProperties == null)
      {
         if (other.connectionProperties != null)
            return false;
      }
      else if (!connectionProperties.equals(other.connectionProperties))
         return false;
      if (pool == null)
      {
         if (other.pool != null)
            return false;
      }
      else if (!pool.equals(other.pool))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<datasource");

      if (jta != null && !Defaults.JTA.equals(jta))
         sb.append(" ").append(XML.ATTRIBUTE_JTA).append("=\"").append(jta).append("\"");

      if (jndiName != null)
         sb.append(" ").append(XML.ATTRIBUTE_JNDI_NAME).append("=\"").append(jndiName).append("\"");

      if (id != null)
         sb.append(" ").append(XML.ATTRIBUTE_ID).append("=\"").append(id).append("\"");

      if (enabled != null && !Defaults.ENABLED.equals(enabled))
         sb.append(" ").append(XML.ATTRIBUTE_ENABLED).append("=\"").append(enabled).append("\"");

      if (spy != null && !Defaults.SPY.equals(spy))
         sb.append(" ").append(XML.ATTRIBUTE_SPY).append("=\"").append(spy).append("\"");

      if (useCcm != null && !Defaults.USE_CCM.equals(useCcm))
         sb.append(" ").append(XML.ATTRIBUTE_USE_CCM).append("=\"").append(useCcm).append("\"");

      if (connectable != null && !Defaults.CONNECTABLE.equals(connectable))
         sb.append(" ").append(XML.ATTRIBUTE_CONNECTABLE).append("=\"").append(connectable).append("\"");

      if (tracking != null)
         sb.append(" ").append(XML.ATTRIBUTE_TRACKING).append("=\"").append(tracking).append("\"");

      sb.append(">");

      if (connectionUrl != null)
      {
         sb.append("<").append(XML.ELEMENT_CONNECTION_URL).append(">");
         sb.append(connectionUrl);
         sb.append("</").append(XML.ELEMENT_CONNECTION_URL).append(">");
      }

      if (driverClass != null)
      {
         sb.append("<").append(XML.ELEMENT_DRIVER_CLASS).append(">");
         sb.append(driverClass);
         sb.append("</").append(XML.ELEMENT_DRIVER_CLASS).append(">");
      }

      if (dataSourceClass != null)
      {
         sb.append("<").append(XML.ELEMENT_DATASOURCE_CLASS).append(">");
         sb.append(dataSourceClass);
         sb.append("</").append(XML.ELEMENT_DATASOURCE_CLASS).append(">");
      }

      if (driver != null)
      {
         sb.append("<").append(XML.ELEMENT_DRIVER).append(">");
         sb.append(driver);
         sb.append("</").append(XML.ELEMENT_DRIVER).append(">");
      }

      if (connectionProperties != null && !connectionProperties.isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = connectionProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            sb.append("<").append(XML.ELEMENT_CONNECTION_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(XML.ELEMENT_CONNECTION_PROPERTY).append(">");
         }
      }

      if (newConnectionSql != null)
      {
         sb.append("<").append(XML.ELEMENT_NEW_CONNECTION_SQL).append(">");
         sb.append(newConnectionSql);
         sb.append("</").append(XML.ELEMENT_NEW_CONNECTION_SQL).append(">");
      }

      if (transactionIsolation != null)
      {
         sb.append("<").append(XML.ELEMENT_TRANSACTION_ISOLATION).append(">");
         sb.append(transactionIsolation);
         sb.append("</").append(XML.ELEMENT_TRANSACTION_ISOLATION).append(">");
      }

      if (urlDelimiter != null)
      {
         sb.append("<").append(XML.ELEMENT_URL_DELIMITER).append(">");
         sb.append(urlDelimiter);
         sb.append("</").append(XML.ELEMENT_URL_DELIMITER).append(">");
      }

      if (urlSelectorStrategyClassName != null)
      {
         sb.append("<").append(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
         sb.append(urlSelectorStrategyClassName);
         sb.append("</").append(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
      }

      if (pool != null)
         sb.append(pool);

      if (security != null)
         sb.append(security);

      if (validation != null)
         sb.append(validation);

      if (timeout != null)
         sb.append(timeout);

      if (statement != null)
         sb.append(statement);

      sb.append("</datasource>");

      return sb.toString();
   }
}
