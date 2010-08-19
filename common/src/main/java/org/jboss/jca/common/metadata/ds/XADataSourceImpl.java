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

import org.jboss.jca.common.api.metadata.ds.Recovery;
import org.jboss.jca.common.api.metadata.ds.Security;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
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

   private final boolean isSameRmOverride;

   private final boolean interleaving;

   private final Recovery recovery;

   private final String newConnectionSql;

   private final boolean padXid;

   private final boolean wrapXaDataSource;

   private final boolean noTxSeparatePool;

   private final boolean trackConnectionByTx;

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
    * @param isSameRmOverride isSameRmOverride
    * @param interleaving interleaving
    * @param recovery recovery
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
    * @param padXid padXid
    * @param wrapXaDataSource wrapXaDataSource
    * @param noTxSeparatePool noTxSeparatePool
    * @param trackConnectionByTx trackConnectionByTx
    */
   public XADataSourceImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, String userName, String password,
         Map<String, String> xaDataSourceProperty, String xaDataSourceClass,
         TransactionIsolation transactionIsolation, boolean isSameRmOverride, boolean interleaving,
         Recovery recovery, TimeOut timeOut, Security security,
         Statement statement, Validation validation, String urlDelimiter,
         String urlSelectorStrategyClassName, String newConnectionSql, boolean useJavaContext, String poolName,
         boolean enabled, String jndiName, boolean padXid, boolean wrapXaDataSource, boolean noTxSeparatePool,
         boolean trackConnectionByTx)
   {
      super(minPoolSize, maxPoolSize, prefill, transactionIsolation, timeOut, security,
            statement, validation, urlDelimiter, urlSelectorStrategyClassName, useJavaContext,
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
      this.isSameRmOverride = isSameRmOverride;
      this.interleaving = interleaving;
      this.recovery = recovery;
      this.newConnectionSql = newConnectionSql;
      this.padXid = padXid;
      this.wrapXaDataSource = wrapXaDataSource;
      this.noTxSeparatePool = noTxSeparatePool;
      this.trackConnectionByTx = trackConnectionByTx;
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
    * Get the isSameRmOverride.
    *
    * @return the isSameRmOverride.
    */
   @Override
   public final boolean isSameRmOverride()
   {
      return isSameRmOverride;
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
    * Get the recovery.
    *
    * @return the recovery.
    */
   @Override
   public final Recovery getRecovery()
   {
      return recovery;
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
      result = prime * result + (interleaving ? 1231 : 1237);
      result = prime * result + (isSameRmOverride ? 1231 : 1237);
      result = prime * result + ((newConnectionSql == null) ? 0 : newConnectionSql.hashCode());
      result = prime * result + (noTxSeparatePool ? 1231 : 1237);
      result = prime * result + (padXid ? 1231 : 1237);
      result = prime * result + ((password == null) ? 0 : password.hashCode());
      result = prime * result + ((recovery == null) ? 0 : recovery.hashCode());
      result = prime * result + (trackConnectionByTx ? 1231 : 1237);
      result = prime * result + ((userName == null) ? 0 : userName.hashCode());
      result = prime * result + (wrapXaDataSource ? 1231 : 1237);
      result = prime * result + ((xaDataSourceClass == null) ? 0 : xaDataSourceClass.hashCode());
      result = prime * result + ((xaDataSourceProperty == null) ? 0 : xaDataSourceProperty.hashCode());
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
      if (interleaving != other.interleaving)
         return false;
      if (isSameRmOverride != other.isSameRmOverride)
         return false;
      if (newConnectionSql == null)
      {
         if (other.newConnectionSql != null)
            return false;
      }
      else if (!newConnectionSql.equals(other.newConnectionSql))
         return false;
      if (noTxSeparatePool != other.noTxSeparatePool)
         return false;
      if (padXid != other.padXid)
         return false;
      if (password == null)
      {
         if (other.password != null)
            return false;
      }
      else if (!password.equals(other.password))
         return false;
      if (recovery == null)
      {
         if (other.recovery != null)
            return false;
      }
      else if (!recovery.equals(other.recovery))
         return false;
      if (trackConnectionByTx != other.trackConnectionByTx)
         return false;
      if (userName == null)
      {
         if (other.userName != null)
            return false;
      }
      else if (!userName.equals(other.userName))
         return false;
      if (wrapXaDataSource != other.wrapXaDataSource)
         return false;
      if (xaDataSourceClass == null)
      {
         if (other.xaDataSourceClass != null)
            return false;
      }
      else if (!xaDataSourceClass.equals(other.xaDataSourceClass))
         return false;
      if (xaDataSourceProperty == null)
      {
         if (other.xaDataSourceProperty != null)
            return false;
      }
      else if (!xaDataSourceProperty.equals(other.xaDataSourceProperty))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "XADataSourceImpl [userName=" + userName + ", password=" + password + ", xaDataSourceProperty="
            + xaDataSourceProperty + ", xaDataSourceClass=" + xaDataSourceClass + ", isSameRmOverride="
            + isSameRmOverride + ", interleaving=" + interleaving + ", recovery=" + recovery
            + ", newConnectionSql=" + newConnectionSql + ", padXid=" + padXid + ", wrapXaDataSource="
            + wrapXaDataSource + ", noTxSeparatePool=" + noTxSeparatePool + ", trackConnectionByTx="
            + trackConnectionByTx + "]";
   }

   /**
    * Get the xaDataSourceProperty.
    *
    * @return the xaDataSourceProperty.
    */
   @Override
   public final Map<String, String> getXaDataSourceProperty()
   {
      return Collections.unmodifiableMap(xaDataSourceProperty);
   }

   /**
    * Get the padXid.
    *
    * @return the padXid.
    */
   @Override
   public final boolean isPadXid()
   {
      return padXid;
   }

   /**
    * Get the wrapXaDataSource.
    *
    * @return the wrapXaDataSource.
    */
   @Override
   public final boolean isWrapXaDataSource()
   {
      return wrapXaDataSource;
   }

   /**
    * Get the noTxSeparatePool.
    *
    * @return the noTxSeparatePool.
    */
   @Override
   public final boolean isNoTxSeparatePool()
   {
      return noTxSeparatePool;
   }

   /**
    * Get the trackConnectionByTx.
    *
    * @return the trackConnectionByTx.
    */
   @Override
   public final boolean isTrackConnectionByTx()
   {
      return trackConnectionByTx;
   }
}
