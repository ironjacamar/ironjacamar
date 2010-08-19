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
import org.jboss.jca.common.api.metadata.ds.Security;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;

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

   private final String userName;

   private final String password;

   private final String connectionUrl;

   private final String driverClass;

   private final HashMap<String, String> connectionProperties;

   private final String newConnectionSql;

   /**
    * Create a new DataSourceImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param userName userName
    * @param password password
    * @param connectionUrl connectionUrl
    * @param driverClass driverClass
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
    */
   public DataSourceImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, String userName, String password,
         String connectionUrl, String driverClass, TransactionIsolation transactionIsolation,
         Map<String, String> connectionProperties, TimeOut timeOut,
         Security security, Statement statement, Validation validation,
         String urlDelimiter, String urlSelectorStrategyClassName, String newConnectionSql, boolean useJavaContext,
         String poolName, boolean enabled, String jndiName)
   {
      super(minPoolSize, maxPoolSize, prefill, transactionIsolation, timeOut, security,
            statement, validation, urlDelimiter, urlSelectorStrategyClassName, useJavaContext,
            poolName, enabled, jndiName);
      this.userName = userName;
      this.password = password;
      this.connectionUrl = connectionUrl;
      this.driverClass = driverClass;
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

   }

   /**
    * Get the userName.
    *
    * @return the userName.
    */
   @Override
   public final String getUserName()
   {
      return userName;
   }

   /**
    * Get the password.
    *
    * @return the password.
    */
   @Override
   public final String getPassword()
   {
      return password;
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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((connectionProperties == null) ? 0 : connectionProperties.hashCode());
      result = prime * result + ((connectionUrl == null) ? 0 : connectionUrl.hashCode());
      result = prime * result + ((driverClass == null) ? 0 : driverClass.hashCode());
      result = prime * result + (enabled ? 1231 : 1237);
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((maxPoolSize == null) ? 0 : maxPoolSize.hashCode());
      result = prime * result + ((minPoolSize == null) ? 0 : minPoolSize.hashCode());
      result = prime * result + ((newConnectionSql == null) ? 0 : newConnectionSql.hashCode());
      result = prime * result + ((password == null) ? 0 : password.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + (prefill ? 1231 : 1237);
      result = prime * result + ((security == null) ? 0 : security.hashCode());
      result = prime * result + ((statement == null) ? 0 : statement.hashCode());
      result = prime * result + ((timeOut == null) ? 0 : timeOut.hashCode());
      result = prime * result + ((transactionIsolation == null) ? 0 : transactionIsolation.hashCode());
      result = prime * result + ((urlDelimiter == null) ? 0 : urlDelimiter.hashCode());
      result = prime * result + ((urlSelectorStrategyClassName == null) ? 0 : urlSelectorStrategyClassName.hashCode());
      result = prime * result + (useJavaContext ? 1231 : 1237);
      result = prime * result + ((userName == null) ? 0 : userName.hashCode());
      result = prime * result + ((validation == null) ? 0 : validation.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
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
      if (driverClass == null)
      {
         if (other.driverClass != null)
            return false;
      }
      else if (!driverClass.equals(other.driverClass))
         return false;
      if (enabled != other.enabled)
         return false;
      if (jndiName == null)
      {
         if (other.jndiName != null)
            return false;
      }
      else if (!jndiName.equals(other.jndiName))
         return false;
      if (maxPoolSize == null)
      {
         if (other.maxPoolSize != null)
            return false;
      }
      else if (!maxPoolSize.equals(other.maxPoolSize))
         return false;
      if (minPoolSize == null)
      {
         if (other.minPoolSize != null)
            return false;
      }
      else if (!minPoolSize.equals(other.minPoolSize))
         return false;
      if (newConnectionSql == null)
      {
         if (other.newConnectionSql != null)
            return false;
      }
      else if (!newConnectionSql.equals(other.newConnectionSql))
         return false;
      if (password == null)
      {
         if (other.password != null)
            return false;
      }
      else if (!password.equals(other.password))
         return false;
      if (poolName == null)
      {
         if (other.poolName != null)
            return false;
      }
      else if (!poolName.equals(other.poolName))
         return false;
      if (prefill != other.prefill)
         return false;
      if (security == null)
      {
         if (other.security != null)
            return false;
      }
      else if (!security.equals(other.security))
         return false;
      if (statement == null)
      {
         if (other.statement != null)
            return false;
      }
      else if (!statement.equals(other.statement))
         return false;
      if (timeOut == null)
      {
         if (other.timeOut != null)
            return false;
      }
      else if (!timeOut.equals(other.timeOut))
         return false;
      if (transactionIsolation != other.transactionIsolation)
         return false;
      if (urlDelimiter == null)
      {
         if (other.urlDelimiter != null)
            return false;
      }
      else if (!urlDelimiter.equals(other.urlDelimiter))
         return false;
      if (urlSelectorStrategyClassName == null)
      {
         if (other.urlSelectorStrategyClassName != null)
            return false;
      }
      else if (!urlSelectorStrategyClassName.equals(other.urlSelectorStrategyClassName))
         return false;
      if (useJavaContext != other.useJavaContext)
         return false;
      if (userName == null)
      {
         if (other.userName != null)
            return false;
      }
      else if (!userName.equals(other.userName))
         return false;
      if (validation == null)
      {
         if (other.validation != null)
            return false;
      }
      else if (!validation.equals(other.validation))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "DataSourceImpl [minPoolSize=" + minPoolSize + ", maxPoolSize=" + maxPoolSize + ", prefill=" + prefill
            + ", userName=" + userName + ", password=" + password + ", connectionUrl=" + connectionUrl
            + ", driverClass=" + driverClass + ", transactionIsolation=" + transactionIsolation
            + ", connectionProperties=" + connectionProperties + ", timeOut=" + timeOut
            + ", security=" + security + ", statement=" + statement
            + ", validation=" + validation + ", urlDelimiter=" + urlDelimiter
            + ", urlSelectorStrategyClassName=" + urlSelectorStrategyClassName + ", newConnectionSql="
            + newConnectionSql + ", useJavaContext=" + useJavaContext + ", poolName=" + poolName + ", enabled="
            + enabled + ", jndiName=" + jndiName + "]";
   }

   @Override
   public DataSource merge(MergeableMetadata<?> jmd) throws Exception
   {
      return this;
   }
}
