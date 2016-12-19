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

import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.DsXaPool;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An XA datasource implementation
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XADataSourceImpl extends DataSourceAbstractImpl implements XaDataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The properties */
   protected HashMap<String, String> xaDataSourceProperty;

   /** The class */
   protected String xaDataSourceClass;

   /** The XA pool */
   protected DsXaPool xaPool;

   /** The recovery */
   protected Recovery recovery;

   /** The url property */
   protected String urlProperty;

   /**
    * Create a new XADataSourceImpl.
    *
    * @param transactionIsolation transactionIsolation
    * @param timeOut timeOut
    * @param security security
    * @param statement statement
    * @param validation validation
    * @param urlDelimiter urlDelimiter
    * @param urlProperty urlProperty
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param useJavaContext useJavaContext
    * @param poolName poolName
    * @param enabled enabled
    * @param jndiName jndiName
    * @param spy spy
    * @param useCcm useCcm
    * @param connectable connectable
    * @param tracking tracking
    * @param mcp mcp
    * @param enlistmentTrace enlistmentTrace
    * @param xaDataSourceProperty xaDataSourceProperty
    * @param xaDataSourceClass xaDataSourceClass
    * @param driver driver
    * @param newConnectionSql newConnectionSql
    * @param xaPool xaPool
    * @param recovery recovery
    * @throws ValidateException ValidateException
    */
   public XADataSourceImpl(TransactionIsolation transactionIsolation, TimeOut timeOut, DsSecurity security,
                           Statement statement, Validation validation, String urlDelimiter, String urlProperty,
                           String urlSelectorStrategyClassName, Boolean useJavaContext, String poolName,
                           Boolean enabled, String jndiName, Boolean spy, Boolean useCcm, Boolean connectable,
                           Boolean tracking, String mcp, Boolean enlistmentTrace,
                           Map<String, String> xaDataSourceProperty, String xaDataSourceClass, String driver,
                           String newConnectionSql,
                           DsXaPool xaPool, Recovery recovery) throws ValidateException
   {
      super(transactionIsolation, timeOut, security, statement, validation, urlDelimiter, urlSelectorStrategyClassName,
            useJavaContext, poolName, enabled, jndiName, spy, useCcm, driver, newConnectionSql, connectable, tracking,
            mcp, enlistmentTrace);

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
      this.xaPool = xaPool;
      this.recovery = recovery;

      this.urlProperty = urlProperty;

      this.validate();
   }

   /**
    * {@inheritDoc}
    */
   public String getXaDataSourceClass()
   {
      return xaDataSourceClass;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, String> getXaDataSourceProperty()
   {
      return Collections.unmodifiableMap(xaDataSourceProperty);
   }

   /**
    * {@inheritDoc}
    */
   public DsXaPool getXaPool()
   {
      return xaPool;
   }

   /**
    * {@inheritDoc}
    */
   public String getUrlProperty()
   {
      return urlProperty;
   }

   /**
    * {@inheritDoc}
    */
   public Recovery getRecovery()
   {
      return recovery;
   }

   /**
    * Set the xaDataSourceClass.
    *
    * @param xaDataSourceClass The xaDataSourceClass to set.
    */
   public void forceXaDataSourceClass(String xaDataSourceClass)
   {
      this.xaDataSourceClass = xaDataSourceClass;
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException
   {
      if ((this.xaDataSourceClass == null || this.xaDataSourceClass.trim().length() == 0) &&
          (this.driver == null || this.driver.trim().length() == 0))
         throw new ValidateException(bundle.requiredElementMissing(Tag.XA_DATASOURCE_CLASS.getLocalName(),
                                                                   this.getClass().getCanonicalName()));

      if (this.xaDataSourceProperty.isEmpty())
         throw new ValidateException(bundle.requiredElementMissing(Tag.XA_DATASOURCE_PROPERTY.getLocalName(),
                                                                   this.getClass().getCanonicalName()));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((xaDataSourceClass == null) ? 0 : xaDataSourceClass.hashCode());
      result = prime * result + ((xaDataSourceProperty == null) ? 0 : xaDataSourceProperty.hashCode());
      result = prime * result + ((xaPool == null) ? 0 : xaPool.hashCode());
      result = prime * result + ((urlProperty == null) ? 0 : urlProperty.hashCode());
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
      if (!(obj instanceof XADataSourceImpl))
         return false;
      XADataSourceImpl other = (XADataSourceImpl) obj;

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
      if (xaPool == null)
      {
         if (other.xaPool != null)
            return false;
      }
      else if (!xaPool.equals(other.xaPool))
         return false;

      if (urlProperty == null)
      {
         if (other.urlProperty != null)
            return false;
      }
      else if (!urlProperty.equals(other.urlProperty))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<xa-datasource");

      if (jndiName != null)
         sb.append(" ").append(XaDataSource.Attribute.JNDI_NAME).append("=\"").append(jndiName).append("\"");

      if (poolName != null)
         sb.append(" ").append(XaDataSource.Attribute.POOL_NAME).append("=\"").append(poolName).append("\"");

      if (enabled != null)
         sb.append(" ").append(XaDataSource.Attribute.ENABLED).append("=\"").append(enabled).append("\"");

      if (useJavaContext != null)
      {
         sb.append(" ").append(XaDataSource.Attribute.USE_JAVA_CONTEXT);
         sb.append("=\"").append(useJavaContext).append("\"");
      }

      if (spy != null)
         sb.append(" ").append(XaDataSource.Attribute.SPY).append("=\"").append(spy).append("\"");

      if (useCcm != null)
         sb.append(" ").append(XaDataSource.Attribute.USE_CCM).append("=\"").append(useCcm).append("\"");

      if (connectable != null)
         sb.append(" ").append(XaDataSource.Attribute.CONNECTABLE).append("=\"").append(connectable).append("\"");

      if (tracking != null)
         sb.append(" ").append(XaDataSource.Attribute.TRACKING).append("=\"").append(tracking).append("\"");

      if (mcp != null)
         sb.append(" ").append(XaDataSource.Attribute.MCP).append("=\"").append(mcp).append("\"");

      if (enlistmentTrace != null)
         sb.append(" ").append(XaDataSource.Attribute.ENLISTMENT_TRACE).append("=\"").append(enlistmentTrace)
               .append("\"");

      sb.append(">");

      if (xaDataSourceProperty != null && xaDataSourceProperty.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = xaDataSourceProperty.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            sb.append("<").append(XaDataSource.Tag.XA_DATASOURCE_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(XaDataSource.Tag.XA_DATASOURCE_PROPERTY).append(">");
         }
      }

      if (xaDataSourceClass != null)
      {
         sb.append("<").append(XaDataSource.Tag.XA_DATASOURCE_CLASS).append(">");
         sb.append(xaDataSourceClass);
         sb.append("</").append(XaDataSource.Tag.XA_DATASOURCE_CLASS).append(">");
      }

      if (driver != null)
      {
         sb.append("<").append(XaDataSource.Tag.DRIVER).append(">");
         sb.append(driver);
         sb.append("</").append(XaDataSource.Tag.DRIVER).append(">");
      }

      if (urlDelimiter != null)
      {
         sb.append("<").append(XaDataSource.Tag.URL_DELIMITER).append(">");
         sb.append(urlDelimiter);
         sb.append("</").append(XaDataSource.Tag.URL_DELIMITER).append(">");
      }

      if (urlProperty != null)
      {
         sb.append("<").append(XaDataSource.Tag.URL_PROPERTY).append(">");
         sb.append(urlProperty);
         sb.append("</").append(XaDataSource.Tag.URL_PROPERTY).append(">");
      }

      if (urlSelectorStrategyClassName != null)
      {
         sb.append("<").append(XaDataSource.Tag.URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
         sb.append(urlSelectorStrategyClassName);
         sb.append("</").append(XaDataSource.Tag.URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
      }

      if (newConnectionSql != null)
      {
         sb.append("<").append(XaDataSource.Tag.NEW_CONNECTION_SQL).append(">");
         sb.append(newConnectionSql);
         sb.append("</").append(XaDataSource.Tag.NEW_CONNECTION_SQL).append(">");
      }

      if (transactionIsolation != null)
      {
         sb.append("<").append(XaDataSource.Tag.TRANSACTION_ISOLATION).append(">");
         sb.append(transactionIsolation);
         sb.append("</").append(XaDataSource.Tag.TRANSACTION_ISOLATION).append(">");
      }

      if (xaPool != null)
         sb.append(xaPool);

      if (security != null)
         sb.append(security);

      if (validation != null)
         sb.append(validation);

      if (timeOut != null)
         sb.append(timeOut);

      if (statement != null)
         sb.append(statement);

      if (recovery != null)
         sb.append(recovery);

      sb.append("</xa-datasource>");

      return sb.toString();
   }
}
