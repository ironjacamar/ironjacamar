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

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.ds.CommonDataSource;
import org.ironjacamar.common.api.metadata.ds.DsSecurity;
import org.ironjacamar.common.api.metadata.ds.Statement;
import org.ironjacamar.common.api.metadata.ds.Timeout;
import org.ironjacamar.common.api.metadata.ds.TransactionIsolation;
import org.ironjacamar.common.api.metadata.ds.Validation;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.Map;

import org.jboss.logging.Messages;

/**
 *
 * A DataSourceAbstractImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public abstract class DataSourceAbstractImpl extends AbstractMetadata implements CommonDataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5612846950298960159L;

   /** The bundle */
   protected static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
   * transactionIsolation
   */
   protected TransactionIsolation transactionIsolation;

   /**
   * timeout
   */
   protected Timeout timeout;

   /**
   * security
   */
   protected DsSecurity security;

   /**
   * statement
   */
   protected Statement statement;

   /**
   * validation
   */
   protected Validation validation;

   /**
   * urlDelimiter
   */
   protected String urlDelimiter;

   /**
   * urlSelectorStrategyClassName
   */
   protected String urlSelectorStrategyClassName;

   /**
   * id
   */
   protected String id;

   /**
   * enabled
   */
   protected Boolean enabled;

   /**
   * jndiName
   */
   protected String jndiName;

   /**
   * spy
   */
   protected Boolean spy;

   /**
   * use-ccm
   */
   protected Boolean useCcm;

   /** Driver */
   protected String driver;

   /** New connection SQL */
   protected String newConnectionSql;

   /** Connectable */
   protected Boolean connectable;

   /** Tracking */
   protected Boolean tracking;


   /**
    * Create a new DataSourceAbstractImpl.
    *
    * @param transactionIsolation transactionIsolation
    * @param timeout timeout
    * @param security security
    * @param statement statement
    * @param validation validation
    * @param urlDelimiter urlDelimiter
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param id id
    * @param enabled enabled
    * @param jndiName jndiName
    * @param spy spy
    * @param useCcm useCcm
    * @param driver driver
    * @param newConnectionSql newConnectionSql
    * @param connectable connectable
    * @param tracking tracking
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   protected DataSourceAbstractImpl(TransactionIsolation transactionIsolation, Timeout timeout,
                                    DsSecurity security, Statement statement, Validation validation,
                                    String urlDelimiter, String urlSelectorStrategyClassName,
                                    String id, Boolean enabled, String jndiName,
                                    Boolean spy, Boolean useCcm, String driver, String newConnectionSql,
                                    Boolean connectable, Boolean tracking, Map<String, String> expressions)
      throws ValidateException
   {
      super(expressions);
      this.transactionIsolation = transactionIsolation;
      this.timeout = timeout;
      this.security = security;
      this.statement = statement;
      this.validation = validation;
      this.urlDelimiter = urlDelimiter;
      this.urlSelectorStrategyClassName = urlSelectorStrategyClassName;
      this.id = id;
      this.enabled = enabled;
      this.jndiName = jndiName;
      this.spy = spy;
      this.useCcm = useCcm;
      this.driver = driver;
      this.newConnectionSql = newConnectionSql;
      this.connectable = connectable;
      this.tracking = tracking;
      partialCommonValidation();
   }

   /**
    * {@inheritDoc}
    */
   public TransactionIsolation getTransactionIsolation()
   {
      return transactionIsolation;
   }

   /**
    * {@inheritDoc}
    */
   public Timeout getTimeout()
   {
      return timeout;
   }

   /**
    * {@inheritDoc}
    */
   public DsSecurity getSecurity()
   {
      return security;
   }

   /**
    * {@inheritDoc}
    */
   public Validation getValidation()
   {
      return validation;
   }

   /**
    * {@inheritDoc}
    */
   public Statement getStatement()
   {
      return statement;
   }

   /**
    * {@inheritDoc}
    */
   public String getUrlDelimiter()
   {
      return urlDelimiter;
   }

   /**
    * {@inheritDoc}
    */
   public String getUrlSelectorStrategyClassName()
   {
      return urlSelectorStrategyClassName;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isEnabled()
   {
      return enabled;
   }

   /**
    * {@inheritDoc}
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isSpy()
   {
      return spy;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isUseCcm()
   {
      return useCcm;
   }

   /**
    * {@inheritDoc}
    */
   public String getDriver()
   {
      return driver;
   }

   /**
    * {@inheritDoc}
    */
   public String getNewConnectionSql()
   {
      return newConnectionSql;
   }


   /**
    * {@inheritDoc}
    */
   public Boolean isConnectable()
   {
      return connectable;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isTracking()
   {
      return tracking;
   }

   /**
    * {@inheritDoc}
    */
   public void setEnabled(Boolean enabled)
   {
      this.enabled = enabled;
   }

   /**
    *
    * Partial validation for common fields defined in this abstract class
    *
    * @throws ValidateException ValidateException
    */
   protected void partialCommonValidation() throws ValidateException
   {
      if (this.jndiName == null)
         throw new ValidateException(bundle.requiredAttributeMissing(XML.ATTRIBUTE_JNDI_NAME,
                                                                     this.getClass().getCanonicalName()));

      if (this.timeout != null)
         this.timeout.validate();
      if (this.security != null)
         this.security.validate();
      if (this.statement != null)
         this.statement.validate();
      if (this.validation != null)
         this.validation.validate();
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((security == null) ? 0 : security.hashCode());
      result = prime * result + ((statement == null) ? 0 : statement.hashCode());
      result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
      result = prime * result + ((transactionIsolation == null) ? 0 : transactionIsolation.hashCode());
      result = prime * result + ((urlDelimiter == null) ? 0 : urlDelimiter.hashCode());
      result = prime * result +
               ((urlSelectorStrategyClassName == null) ? 0 : urlSelectorStrategyClassName.hashCode());
      result = prime * result + ((validation == null) ? 0 : validation.hashCode());
      result = prime * result + ((driver == null) ? 0 : driver.hashCode());
      result = prime * result + ((newConnectionSql == null) ? 0 : newConnectionSql.hashCode());
      result = prime * result + ((connectable == null) ? 0 : connectable.hashCode());
      result = prime * result + ((tracking == null) ? 0 : tracking.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof DataSourceAbstractImpl))
         return false;
      DataSourceAbstractImpl other = (DataSourceAbstractImpl) obj;
      if (enabled == null)
      {
         if (other.enabled != null)
            return false;
      }
      else if (!enabled.equals(other.enabled))
         return false;
      if (jndiName == null)
      {
         if (other.jndiName != null)
            return false;
      }
      else if (!jndiName.equals(other.jndiName))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
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
      if (timeout == null)
      {
         if (other.timeout != null)
            return false;
      }
      else if (!timeout.equals(other.timeout))
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
      if (validation == null)
      {
         if (other.validation != null)
            return false;
      }
      else if (!validation.equals(other.validation))
         return false;
      if (driver == null)
      {
         if (other.driver != null)
            return false;
      }
      else if (!driver.equals(other.driver))
         return false;
      if (newConnectionSql == null)
      {
         if (other.newConnectionSql != null)
            return false;
      }
      else if (!newConnectionSql.equals(other.newConnectionSql))
         return false;

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

   /**
    * {@inheritDoc}
    */
   public abstract String toString();
}
