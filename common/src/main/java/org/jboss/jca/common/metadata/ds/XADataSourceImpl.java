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

import org.jboss.jca.common.api.metadata.ds.RecoverySettings;
import org.jboss.jca.common.api.metadata.ds.SecuritySettings;
import org.jboss.jca.common.api.metadata.ds.StatementSettings;
import org.jboss.jca.common.api.metadata.ds.TimeOutSettings;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.ValidationSettings;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;

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
public class XADataSourceImpl extends DataSourceAbstractImpl implements XaDataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1401087499308709724L;

   private final String userName;

   private final String password;

   private final HashMap<String, String> xaDataSourceProperty;

   private final String xaDataSourceClass;

   private final boolean isSameRmOverrideValue;

   private final boolean interleaving;

   private final RecoverySettings recoverySettings;

   private final String newConnectionSql;

   /**
    * Create a new XADataSourceImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param userName userName
    * @param password password
    * @param xaDataSourceProperty xaDataSoourceProperty
    * @param xaDataSourceClass xaDataSourceClass
    * @param transactionIsolation transactionIsolation
    * @param isSameRmOverrideValue isSameRmOverrideValue
    * @param interleaving interleaving
    * @param recoverySettings recoverySettings
    * @param timeOutSettings timeOutSettings
    * @param securitySettings securitySettings
    * @param statementSettings statementSettings
    * @param validationSettings validationSettings
    * @param urlDelimiter urlDelimiter
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param newConnectionSql newConnectionSql
    * @param useJavaContext useJavaContext
    * @param poolName poolName
    * @param enabled enabled
    * @param jndiName jndiName
    */
   public XADataSourceImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, String userName, String password,
         Map<String, String> xaDataSourceProperty, String xaDataSourceClass,
         TransactionIsolation transactionIsolation, boolean isSameRmOverrideValue, boolean interleaving,
         RecoverySettings recoverySettings, TimeOutSettings timeOutSettings, SecuritySettings securitySettings,
         StatementSettings statementSettings, ValidationSettings validationSettings, String urlDelimiter,
         String urlSelectorStrategyClassName, String newConnectionSql, boolean useJavaContext, String poolName,
         boolean enabled, String jndiName)
   {
      super(minPoolSize, maxPoolSize, prefill, transactionIsolation, timeOutSettings, securitySettings,
            statementSettings, validationSettings, urlDelimiter, urlSelectorStrategyClassName, useJavaContext,
            poolName, enabled, jndiName);
      this.userName = userName;
      this.password = password;
      if (xaDataSourceProperty != null)
      {
         this.xaDataSourceProperty = new HashMap<String, String>(xaDataSourceProperty.size());
         this.xaDataSourceProperty.putAll(xaDataSourceProperty);
      }
      else
      {
         this.xaDataSourceProperty = new HashMap<String, String>(0);
      }
      this.xaDataSourceClass = xaDataSourceClass;
      this.isSameRmOverrideValue = isSameRmOverrideValue;
      this.interleaving = interleaving;
      this.recoverySettings = recoverySettings;
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
    * Get the xaDataSoourceProperty.
    *
    * @return the xaDataSoourceProperty.
    */
   @Override
   public final Map<String, String> getXaDataSoourceProperty()
   {
      return Collections.unmodifiableMap(xaDataSourceProperty);
   }

   /**
    * Get the xaDataSourceClass.
    *
    * @return the xaDataSourceClass.
    */
   @Override
   public final String getXaDataSourceClass()
   {
      return xaDataSourceClass;
   }

   /**
    * Get the isSameRmOverrideValue.
    *
    * @return the isSameRmOverrideValue.
    */
   @Override
   public final boolean isSameRmOverrideValue()
   {
      return isSameRmOverrideValue;
   }

   /**
    * Get the interleaving.
    *
    * @return the interleaving.
    */
   @Override
   public final boolean isInterleaving()
   {
      return interleaving;
   }

   /**
    * Get the recoverySettings.
    *
    * @return the recoverySettings.
    */
   @Override
   public final RecoverySettings getRecoverySettings()
   {
      return recoverySettings;
   }

   /**
    * Get the statementSettings.
    *
    * @return the statementSettings.
    */
   @Override
   public final StatementSettings getStatementSettings()
   {
      return statementSettings;
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
      result = prime * result + (enabled ? 1231 : 1237);
      result = prime * result + (interleaving ? 1231 : 1237);
      result = prime * result + (isSameRmOverrideValue ? 1231 : 1237);
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((maxPoolSize == null) ? 0 : maxPoolSize.hashCode());
      result = prime * result + ((minPoolSize == null) ? 0 : minPoolSize.hashCode());
      result = prime * result + ((newConnectionSql == null) ? 0 : newConnectionSql.hashCode());
      result = prime * result + ((password == null) ? 0 : password.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + (prefill ? 1231 : 1237);
      result = prime * result + ((recoverySettings == null) ? 0 : recoverySettings.hashCode());
      result = prime * result + ((securitySettings == null) ? 0 : securitySettings.hashCode());
      result = prime * result + ((statementSettings == null) ? 0 : statementSettings.hashCode());
      result = prime * result + ((timeOutSettings == null) ? 0 : timeOutSettings.hashCode());
      result = prime * result + ((transactionIsolation == null) ? 0 : transactionIsolation.hashCode());
      result = prime * result + ((urlDelimiter == null) ? 0 : urlDelimiter.hashCode());
      result = prime * result + ((urlSelectorStrategyClassName == null) ? 0 : urlSelectorStrategyClassName.hashCode());
      result = prime * result + (useJavaContext ? 1231 : 1237);
      result = prime * result + ((userName == null) ? 0 : userName.hashCode());
      result = prime * result + ((validationSettings == null) ? 0 : validationSettings.hashCode());
      result = prime * result + ((xaDataSourceProperty == null) ? 0 : xaDataSourceProperty.hashCode());
      result = prime * result + ((xaDataSourceClass == null) ? 0 : xaDataSourceClass.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof XADataSourceImpl))
         return false;
      XADataSourceImpl other = (XADataSourceImpl) obj;
      if (enabled != other.enabled)
         return false;
      if (interleaving != other.interleaving)
         return false;
      if (isSameRmOverrideValue != other.isSameRmOverrideValue)
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
      if (recoverySettings == null)
      {
         if (other.recoverySettings != null)
            return false;
      }
      else if (!recoverySettings.equals(other.recoverySettings))
         return false;
      if (securitySettings == null)
      {
         if (other.securitySettings != null)
            return false;
      }
      else if (!securitySettings.equals(other.securitySettings))
         return false;
      if (statementSettings == null)
      {
         if (other.statementSettings != null)
            return false;
      }
      else if (!statementSettings.equals(other.statementSettings))
         return false;
      if (timeOutSettings == null)
      {
         if (other.timeOutSettings != null)
            return false;
      }
      else if (!timeOutSettings.equals(other.timeOutSettings))
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
      if (validationSettings == null)
      {
         if (other.validationSettings != null)
            return false;
      }
      else if (!validationSettings.equals(other.validationSettings))
         return false;
      if (xaDataSourceProperty == null)
      {
         if (other.xaDataSourceProperty != null)
            return false;
      }
      else if (!xaDataSourceProperty.equals(other.xaDataSourceProperty))
         return false;
      if (xaDataSourceClass == null)
      {
         if (other.xaDataSourceClass != null)
            return false;
      }
      else if (!xaDataSourceClass.equals(other.xaDataSourceClass))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "XADataSourceImpl [minPoolSize=" + minPoolSize + ", maxPoolSize=" + maxPoolSize + ", prefill=" + prefill
            + ", userName=" + userName + ", password=" + password + ", xaDataSoourceProperty=" + xaDataSourceProperty
            + ", xaDataSourceClass=" + xaDataSourceClass + ", transactionIsolation=" + transactionIsolation
            + ", isSameRmOverrideValue=" + isSameRmOverrideValue + ", interleaving=" + interleaving
            + ", recoverySettings=" + recoverySettings + ", timeOutSettings=" + timeOutSettings + ", securitySettings="
            + securitySettings + ", statementSettings=" + statementSettings + ", validationSettings="
            + validationSettings + ", urlDelimiter=" + urlDelimiter + ", urlSelectorStrategyClassName="
            + urlSelectorStrategyClassName + ", newConnectionSql=" + newConnectionSql + ", useJavaContext="
            + useJavaContext + ", poolName=" + poolName + ", enabled=" + enabled + ", jndiName=" + jndiName + "]";
   }
}
