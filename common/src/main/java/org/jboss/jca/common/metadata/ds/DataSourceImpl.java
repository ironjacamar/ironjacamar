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
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * A DataSourceImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class DataSourceImpl extends DataSourceAbstractImpl implements DataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5214100851560229431L;

   private final String connectionUrl;

   private String driverClass;

   private final String driver;

   private final HashMap<String, String> connectionProperties;

   private final String newConnectionSql;

   private final CommonPool pool;

   /**
    * Create a new DataSourceImpl.
    *
    * @param connectionUrl connectionUrl
    * @param driverClass driverClass
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
    * @param pool pool
    * @throws ValidateException ValidateException
    */
   public DataSourceImpl(String connectionUrl, String driverClass, String driver,
      TransactionIsolation transactionIsolation, Map<String, String> connectionProperties, TimeOut timeOut,
      DsSecurity security, Statement statement, Validation validation, String urlDelimiter,
      String urlSelectorStrategyClassName, String newConnectionSql, boolean useJavaContext, String poolName,
      boolean enabled, String jndiName, boolean spy, boolean useccm, CommonPool pool) throws ValidateException
   {
      super(transactionIsolation, timeOut, security, statement, validation, urlDelimiter,
            urlSelectorStrategyClassName, useJavaContext, poolName, enabled, jndiName, spy, useccm);
      this.connectionUrl = connectionUrl;
      this.driverClass = driverClass;
      this.driver = driver;
      if (connectionProperties != null)
      {
         this.connectionProperties = new HashMap<String, String>(connectionProperties.size());
         this.connectionProperties.putAll(connectionProperties);
      }
      else
      {
         this.connectionProperties = new HashMap<String, String>(0);
      }
      this.newConnectionSql = newConnectionSql;
      this.pool = pool;
      this.validate();
   }

   /**
    * Get the connectionUrl.
    *
    * @return the connectionUrl.
    */
   @Override
   public final String getConnectionUrl()
   {
      return connectionUrl;
   }

   /**
    * Get the driverClass.
    *
    * @return the driverClass.
    */
   @Override
   public final String getDriverClass()
   {
      return driverClass;
   }

   /**
    * Get the driver.
    *
    * @return the driver.
    */
   @Override
   public final String getDriver()
   {
      return driver;
   }

   /**
    * Get the connectionProperties.
    *
    * @return the connectionProperties.
    */
   @Override
   public final Map<String, String> getConnectionProperties()
   {
      return Collections.unmodifiableMap(connectionProperties);
   }

   /**
    * Get the statement.
    *
    * @return the statement.
    */
   @Override
   public final Statement getStatement()
   {
      return statement;
   }

   /**
    * Get the urlDelimiter.
    *
    * @return the urlDelimiter.
    */
   @Override
   public final String getUrlDelimiter()
   {
      return urlDelimiter;
   }

   /**
    * Get the urlSelectorStrategyClassName.
    *
    * @return the urlSelectorStrategyClassName.
    */
   @Override
   public final String getUrlSelectorStrategyClassName()
   {
      return urlSelectorStrategyClassName;
   }

   /**
    * Get the newConnectionSql.
    *
    * @return the newConnectionSql.
    */
   @Override
   public final String getNewConnectionSql()
   {
      return newConnectionSql;
   }

   /**
    * Get the pool.
    *
    * @return the pool.
    */
   @Override
   public final CommonPool getPool()
   {
      return pool;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((connectionProperties == null) ? 0 : connectionProperties.hashCode());
      result = prime * result + ((connectionUrl == null) ? 0 : connectionUrl.hashCode());
      result = prime * result + ((driver == null) ? 0 : driver.hashCode());
      result = prime * result + ((driverClass == null) ? 0 : driverClass.hashCode());
      result = prime * result + ((newConnectionSql == null) ? 0 : newConnectionSql.hashCode());
      result = prime * result + ((pool == null) ? 0 : pool.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof DataSourceImpl))
         return false;
      DataSourceImpl other = (DataSourceImpl) obj;
      if (connectionProperties == null)
      {
         if (other.connectionProperties != null)
            return false;
      }
      else if (!connectionProperties.equals(other.connectionProperties))
         return false;
      if (connectionUrl == null)
      {
         if (other.connectionUrl != null)
            return false;
      }
      else if (!connectionUrl.equals(other.connectionUrl))
         return false;
      if (driver == null)
      {
         if (other.driver != null)
            return false;
      }
      else if (!driver.equals(other.driver))
         return false;
      if (driverClass == null)
      {
         if (other.driverClass != null)
            return false;
      }
      else if (!driverClass.equals(other.driverClass))
         return false;
      if (newConnectionSql == null)
      {
         if (other.newConnectionSql != null)
            return false;
      }
      else if (!newConnectionSql.equals(other.newConnectionSql))
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

   @Override
   public String toString()
   {
      return "DataSourceImpl [connectionUrl=" + connectionUrl + ", driverClass=" + driverClass + ", driver=" +
             driver + ", connectionProperties=" + connectionProperties + ", newConnectionSql=" + newConnectionSql +
             ", pool=" + pool + "]";
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.connectionUrl == null || this.connectionUrl.trim().length() == 0)
         throw new ValidateException("connectionUrl (xml tag " + Tag.CONNECTIONURL + ") is required in " +
                                     this.getClass().getCanonicalName());
      if ((this.driverClass == null || this.driverClass.trim().length() == 0) &&
          (this.driver == null || this.driver.trim().length() == 0))
         throw new ValidateException("driverClass (xml tag " + Tag.DRIVERCLASS + ") is required in " +
                                     this.getClass().getCanonicalName() + "if no driver (xml tag " + Tag.DRIVER +
                                     ") is not specified");

   }

   /**
    * Set the driverClass.
    *
    * @param driverClass The driverClass to set.
    */
   public final void forceDriverClass(String driverClass)
   {
      this.driverClass = driverClass;
   }
}
