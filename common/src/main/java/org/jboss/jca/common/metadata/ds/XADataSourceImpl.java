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

import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
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
public class XADataSourceImpl extends DataSourceAbstractImpl implements XaDataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1401087499308709724L;

   private final HashMap<String, String> xaDataSourceProperty;

   private final String xaDataSourceClass;

   private final String module;

   private final String newConnectionSql;

   private final CommonXaPool xaPool;

   /**
    * Create a new XADataSourceImpl.
    *
    * @param transactionIsolation transactionIsolation
    * @param timeOut timeOut
    * @param security security
    * @param statement statement
    * @param validation validation
    * @param urlDelimiter urlDelimiter
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param useJavaContext useJavaContext
    * @param poolName poolName
    * @param enabled enabled
    * @param jndiName jndiName
    * @param xaDataSourceProperty xaDataSourceProperty
    * @param xaDataSourceClass xaDataSourceClass
    * @param module module
    * @param newConnectionSql newConnectionSql
    * @param xaPool xaPool
    * @throws ValidateException ValidateException
    */
   public XADataSourceImpl(TransactionIsolation transactionIsolation, TimeOut timeOut, CommonSecurity security,
      Statement statement, Validation validation, String urlDelimiter, String urlSelectorStrategyClassName,
      boolean useJavaContext, String poolName, boolean enabled, String jndiName,
      Map<String, String> xaDataSourceProperty, String xaDataSourceClass, String module, String newConnectionSql,
      CommonXaPool xaPool) throws ValidateException
   {
      super(transactionIsolation, timeOut, security, statement, validation, urlDelimiter,
            urlSelectorStrategyClassName, useJavaContext, poolName, enabled, jndiName);
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
      this.module = module;
      this.newConnectionSql = newConnectionSql;
      this.xaPool = xaPool;
      this.validate();
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
    * Get the module.
    *
    * @return the module.
    */
   @Override
   public final String getModule()
   {
      return module;
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
      result = prime * result + ((newConnectionSql == null) ? 0 : newConnectionSql.hashCode());
      result = prime * result + ((xaDataSourceClass == null) ? 0 : xaDataSourceClass.hashCode());
      result = prime * result + ((module == null) ? 0 : module.hashCode());
      result = prime * result + ((xaDataSourceProperty == null) ? 0 : xaDataSourceProperty.hashCode());
      result = prime * result + ((xaPool == null) ? 0 : xaPool.hashCode());
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
      if (newConnectionSql == null)
      {
         if (other.newConnectionSql != null)
            return false;
      }
      else if (!newConnectionSql.equals(other.newConnectionSql))
         return false;
      if (xaDataSourceClass == null)
      {
         if (other.xaDataSourceClass != null)
            return false;
      }
      else if (!xaDataSourceClass.equals(other.xaDataSourceClass))
         return false;
      if (module == null)
      {
         if (other.module != null)
            return false;
      }
      else if (!module.equals(other.module))
         return false;
      if (xaDataSourceProperty == null)
      {
         if (other.xaDataSourceProperty != null)
            return false;
      }
      else if (!xaDataSourceProperty.equals(other.xaDataSourceProperty))
         return false;
      if (xaPool == null)
      {
         if (other.xaPool != null)
            return false;
      }
      else if (!xaPool.equals(other.xaPool))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "XADataSourceImpl [xaDataSourceProperty=" + xaDataSourceProperty + ", xaDataSourceClass=" +
             xaDataSourceClass + ", module=" + module + ", newConnectionSql=" + newConnectionSql + ", xaPool=" +
             xaPool + ", transactionIsolation=" + transactionIsolation + ", timeOut=" + timeOut + ", security=" +
             security + ", statement=" + statement + ", validation=" + validation + ", urlDelimiter=" + urlDelimiter +
             ", urlSelectorStrategyClassName=" + urlSelectorStrategyClassName + ", useJavaContext=" + useJavaContext +
             ", poolName=" + poolName + ", enabled=" + enabled + ", jndiName=" + jndiName + "]";
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
    * Get the xaPool.
    *
    * @return the xaPool.
    */
   @Override
   public final CommonXaPool getXaPool()
   {
      return xaPool;
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.xaDataSourceClass == null || this.xaDataSourceClass.trim().length() == 0)
         throw new ValidateException("xaDataSourceClass is required in " + this.getClass().getCanonicalName());
      if (this.xaDataSourceProperty.isEmpty())
         throw new ValidateException("at least one xaDataSourceProperty is required in " +
                                     this.getClass().getCanonicalName());

   }
}
