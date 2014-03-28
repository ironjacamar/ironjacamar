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

import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.v12.DsXaPool;
import org.jboss.jca.common.api.metadata.ds.v13.XaDataSource;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Iterator;
import java.util.Map;

/**
 * An XA datasource implementation
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XADataSourceImpl extends org.jboss.jca.common.metadata.ds.v12.XADataSourceImpl implements XaDataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** Connectable */
   protected Boolean connectable;

   /** Tracking */
   protected Boolean tracking;

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
                           Boolean tracking,
                           Map<String, String> xaDataSourceProperty, String xaDataSourceClass, String driver,
                           String newConnectionSql,
                           DsXaPool xaPool, Recovery recovery) throws ValidateException
   {
      super(transactionIsolation, timeOut, security, statement, validation, urlDelimiter, urlProperty,
            urlSelectorStrategyClassName, useJavaContext, poolName, enabled, jndiName, spy, useCcm,
            xaDataSourceProperty, xaDataSourceClass, driver, newConnectionSql, xaPool, recovery);

      this.connectable = connectable;
      this.tracking = tracking;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Boolean isConnectable()
   {
      return connectable;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Boolean isTracking()
   {
      return tracking;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((connectable == null) ? 0 : connectable.hashCode());
      result = prime * result + ((tracking == null) ? 0 : tracking.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof XADataSourceImpl))
         return false;
      XADataSourceImpl other = (XADataSourceImpl) obj;
      if (connectable == null)
      {
         if (other.connectable != null)
            return false;
      }
      else if (!connectable.equals(other.connectable))
         return false;
      if (tracking == null)
      {
         if (other.tracking != null)
            return false;
      }
      else if (!tracking.equals(other.tracking))
         return false;
      return true;
   }

   @Override
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
