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

import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DsPool;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.validator.ValidateException;

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
    * @param timeOut timeOut
    * @param security security
    * @param statement statement
    * @param validation validation
    * @param urlDelimiter urlDelimiter
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param newConnectionSql newConnectionSql
    * @param useJavaContext useJavaContext
    * @param poolName poolName
    * @param enabled enabled
    * @param jndiName jndiName
    * @param spy spy
    * @param useccm useccm
    * @param jta jta
    * @param connectable connectable
    * @param tracking tracking
    * @param mcp mcp
    * @param enlistmentTrace enlistmentTrace
    * @param pool pool
    * @throws ValidateException ValidateException
    */
   public DataSourceImpl(String connectionUrl, String driverClass, String dataSourceClass, String driver,
                         TransactionIsolation transactionIsolation, Map<String, String> connectionProperties, 
                         TimeOut timeOut, DsSecurity security, Statement statement, Validation validation, 
                         String urlDelimiter, String urlSelectorStrategyClassName, String newConnectionSql, 
                         Boolean useJavaContext, String poolName, Boolean enabled, String jndiName, 
                         Boolean spy, Boolean useccm, Boolean jta, Boolean connectable, Boolean tracking, String mcp,
                         Boolean enlistmentTrace,
                         DsPool pool)
      throws ValidateException
   {
      super(transactionIsolation, timeOut, security, statement, validation, urlDelimiter, urlSelectorStrategyClassName,
            useJavaContext, poolName, enabled, jndiName, spy, useccm, driver, newConnectionSql, connectable, tracking,
            mcp, enlistmentTrace);

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
         throw new ValidateException(bundle.requiredElementMissing(Tag.CONNECTION_URL.getLocalName(), 
                                                                   this.getClass().getCanonicalName()));
      
      if ((this.driverClass == null || this.driverClass.trim().length() == 0) &&
          (this.dataSourceClass == null || this.dataSourceClass.trim().length() == 0) &&
          (this.driver == null || this.driver.trim().length() == 0))
         throw new ValidateException(bundle.requiredElementMissing(Tag.DRIVER_CLASS.getLocalName(), 
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

      if (jndiName != null)
         sb.append(" ").append(DataSource.Attribute.JNDI_NAME).append("=\"").append(jndiName).append("\"");

      if (poolName != null)
         sb.append(" ").append(DataSource.Attribute.POOL_NAME).append("=\"").append(poolName).append("\"");

      if (enabled != null)
         sb.append(" ").append(DataSource.Attribute.ENABLED).append("=\"").append(enabled).append("\"");

      if (useJavaContext != null)
      {
         sb.append(" ").append(DataSource.Attribute.USE_JAVA_CONTEXT);
         sb.append("=\"").append(useJavaContext).append("\"");
      }

      if (spy != null)
         sb.append(" ").append(DataSource.Attribute.SPY).append("=\"").append(spy).append("\"");

      if (useCcm != null)
         sb.append(" ").append(DataSource.Attribute.USE_CCM).append("=\"").append(useCcm).append("\"");

      if (jta != null)
         sb.append(" ").append(DataSource.Attribute.JTA).append("=\"").append(jta).append("\"");

      if (connectable != null)
         sb.append(" ").append(DataSource.Attribute.CONNECTABLE).append("=\"").append(connectable).append("\"");

      if (tracking != null)
         sb.append(" ").append(DataSource.Attribute.TRACKING).append("=\"").append(tracking).append("\"");

      if (mcp != null)
         sb.append(" ").append(DataSource.Attribute.MCP).append("=\"").append(mcp).append("\"");

      if (enlistmentTrace != null)
         sb.append(" ").append(DataSource.Attribute.ENLISTMENT_TRACE).append("=\"").append(enlistmentTrace)
               .append("\"");

      sb.append(">");

      if (connectionUrl != null)
      {
         sb.append("<").append(DataSource.Tag.CONNECTION_URL).append(">");
         sb.append(connectionUrl);
         sb.append("</").append(DataSource.Tag.CONNECTION_URL).append(">");
      }

      if (driverClass != null)
      {
         sb.append("<").append(DataSource.Tag.DRIVER_CLASS).append(">");
         sb.append(driverClass);
         sb.append("</").append(DataSource.Tag.DRIVER_CLASS).append(">");
      }

      if (dataSourceClass != null)
      {
         sb.append("<").append(DataSource.Tag.DATASOURCE_CLASS).append(">");
         sb.append(dataSourceClass);
         sb.append("</").append(DataSource.Tag.DATASOURCE_CLASS).append(">");
      }

      if (driver != null)
      {
         sb.append("<").append(DataSource.Tag.DRIVER).append(">");
         sb.append(driver);
         sb.append("</").append(DataSource.Tag.DRIVER).append(">");
      }

      if (connectionProperties != null && connectionProperties.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = connectionProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            sb.append("<").append(DataSource.Tag.CONNECTION_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(DataSource.Tag.CONNECTION_PROPERTY).append(">");
         }
      }

      if (newConnectionSql != null)
      {
         sb.append("<").append(DataSource.Tag.NEW_CONNECTION_SQL).append(">");
         sb.append(newConnectionSql);
         sb.append("</").append(DataSource.Tag.NEW_CONNECTION_SQL).append(">");
      }

      if (transactionIsolation != null)
      {
         sb.append("<").append(DataSource.Tag.TRANSACTION_ISOLATION).append(">");
         sb.append(transactionIsolation);
         sb.append("</").append(DataSource.Tag.TRANSACTION_ISOLATION).append(">");
      }

      if (urlDelimiter != null)
      {
         sb.append("<").append(DataSource.Tag.URL_DELIMITER).append(">");
         sb.append(urlDelimiter);
         sb.append("</").append(DataSource.Tag.URL_DELIMITER).append(">");
      }

      if (urlSelectorStrategyClassName != null)
      {
         sb.append("<").append(DataSource.Tag.URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
         sb.append(urlSelectorStrategyClassName);
         sb.append("</").append(DataSource.Tag.URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
      }

      if (pool != null)
         sb.append(pool);

      if (security != null)
         sb.append(security);

      if (validation != null)
         sb.append(validation);

      if (timeOut != null)
         sb.append(timeOut);

      if (statement != null)
         sb.append(statement);

      sb.append("</datasource>");

      return sb.toString();
   }
}
