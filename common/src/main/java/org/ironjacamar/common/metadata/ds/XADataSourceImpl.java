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
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.ds.DsSecurity;
import org.ironjacamar.common.api.metadata.ds.DsXaPool;
import org.ironjacamar.common.api.metadata.ds.Statement;
import org.ironjacamar.common.api.metadata.ds.Timeout;
import org.ironjacamar.common.api.metadata.ds.TransactionIsolation;
import org.ironjacamar.common.api.metadata.ds.Validation;
import org.ironjacamar.common.api.metadata.ds.XaDataSource;
import org.ironjacamar.common.api.validator.ValidateException;

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
    * @param timeout timeout
    * @param security security
    * @param statement statement
    * @param validation validation
    * @param urlDelimiter urlDelimiter
    * @param urlProperty urlProperty
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param id id
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
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public XADataSourceImpl(TransactionIsolation transactionIsolation, Timeout timeout, DsSecurity security,
                           Statement statement, Validation validation, String urlDelimiter, String urlProperty,
                           String urlSelectorStrategyClassName, String id,
                           Boolean enabled, String jndiName, Boolean spy, Boolean useCcm, Boolean connectable,
                           Boolean tracking,
                           Map<String, String> xaDataSourceProperty, String xaDataSourceClass, String driver,
                           String newConnectionSql,
                           DsXaPool xaPool, Recovery recovery, Map<String, String> expressions) throws ValidateException
   {
      super(transactionIsolation, timeout, security, statement, validation, urlDelimiter, urlSelectorStrategyClassName,
            id, enabled, jndiName, spy, useCcm, driver, newConnectionSql, connectable, tracking,
            expressions);

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
         throw new ValidateException(bundle.requiredElementMissing(XML.ELEMENT_XA_DATASOURCE_CLASS,
                                                                   this.getClass().getCanonicalName()));

      if (this.xaDataSourceProperty.isEmpty())
         throw new ValidateException(bundle.requiredElementMissing(XML.ELEMENT_XA_DATASOURCE_PROPERTY,
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

      if (xaDataSourceProperty != null && !xaDataSourceProperty.isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = xaDataSourceProperty.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            sb.append("<").append(XML.ELEMENT_XA_DATASOURCE_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(XML.ELEMENT_XA_DATASOURCE_PROPERTY).append(">");
         }
      }

      if (xaDataSourceClass != null)
      {
         sb.append("<").append(XML.ELEMENT_XA_DATASOURCE_CLASS).append(">");
         sb.append(xaDataSourceClass);
         sb.append("</").append(XML.ELEMENT_XA_DATASOURCE_CLASS).append(">");
      }

      if (driver != null)
      {
         sb.append("<").append(XML.ELEMENT_DRIVER).append(">");
         sb.append(driver);
         sb.append("</").append(XML.ELEMENT_DRIVER).append(">");
      }

      if (urlDelimiter != null)
      {
         sb.append("<").append(XML.ELEMENT_URL_DELIMITER).append(">");
         sb.append(urlDelimiter);
         sb.append("</").append(XML.ELEMENT_URL_DELIMITER).append(">");
      }

      if (urlProperty != null)
      {
         sb.append("<").append(XML.ELEMENT_URL_PROPERTY).append(">");
         sb.append(urlProperty);
         sb.append("</").append(XML.ELEMENT_URL_PROPERTY).append(">");
      }

      if (urlSelectorStrategyClassName != null)
      {
         sb.append("<").append(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
         sb.append(urlSelectorStrategyClassName);
         sb.append("</").append(XML.ELEMENT_URL_SELECTOR_STRATEGY_CLASS_NAME).append(">");
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

      if (xaPool != null)
         sb.append(xaPool);

      if (security != null)
         sb.append(security);

      if (validation != null)
         sb.append(validation);

      if (timeout != null)
         sb.append(timeout);

      if (statement != null)
         sb.append(statement);

      if (recovery != null)
         sb.append(recovery);

      sb.append("</xa-datasource>");

      return sb.toString();
   }
}
