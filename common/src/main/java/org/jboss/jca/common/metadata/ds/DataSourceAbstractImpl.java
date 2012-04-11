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

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.ds.CommonDataSource;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.v10.DataSource;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 *
 * A DataSourceAbstractImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class DataSourceAbstractImpl implements CommonDataSource
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -5612846950298960159L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
   * transactionIsolation
   */
   protected final TransactionIsolation transactionIsolation;

   /**
   * timeOut
   */
   protected final TimeOut timeOut;

   /**
   * security
   */
   protected final DsSecurity security;

   /**
   * statement
   */
   protected final Statement statement;

   /**
   * validation
   */
   protected final Validation validation;

   /**
   * urlDelimiter
   */
   protected final String urlDelimiter;

   /**
   * urlSelectorStrategyClassName
   */
   protected final String urlSelectorStrategyClassName;

   /**
   * useJavaContext
   */
   protected final Boolean useJavaContext;

   /**
   * poolName
   */
   protected final String poolName;

   /**
   * enabled
   */
   protected Boolean enabled;

   /**
   * jndiName
   */
   protected final String jndiName;

   /**
   * spy
   */
   protected final Boolean spy;

   /**
   * use-ccm
   */
   protected final Boolean useCcm;

   /**
    * Create a new DataSourceAbstractImpl.
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
    * @param spy spy
    * @param useCcm useCcm
    * @throws ValidateException ValidateException
    */
   protected DataSourceAbstractImpl(TransactionIsolation transactionIsolation, TimeOut timeOut,
      DsSecurity security, Statement statement, Validation validation, String urlDelimiter,
      String urlSelectorStrategyClassName, Boolean useJavaContext, String poolName, Boolean enabled, String jndiName,
      Boolean spy, Boolean useCcm)
      throws ValidateException
   {
      super();
      this.transactionIsolation = transactionIsolation;
      this.timeOut = timeOut;
      this.security = security;
      this.statement = statement;
      this.validation = validation;
      this.urlDelimiter = urlDelimiter;
      this.urlSelectorStrategyClassName = urlSelectorStrategyClassName;
      this.useJavaContext = useJavaContext;
      this.poolName = poolName;
      this.enabled = enabled;
      this.jndiName = jndiName;
      this.spy = spy;
      this.useCcm = useCcm;
      partialCommonValidation();
   }

   /**
    * Get the transactionIsolation.
    *
    * @return the transactionIsolation.
    */

   @Override
   public final TransactionIsolation getTransactionIsolation()
   {
      return transactionIsolation;
   }

   /**
    * Get the timeOut
    *
    * @return the timeOut.
    */

   @Override
   public final TimeOut getTimeOut()
   {
      return timeOut;
   }

   /**
    * Get the security.
    *
    * @return the security.
    */

   @Override
   public final DsSecurity getSecurity()
   {
      return security;
   }

   /**
    * Get the validation.
    *
    * @return the validation.
    */

   @Override
   public final Validation getValidation()
   {
      return validation;
   }

   /**
    * Get the useJavaContext.
    *
    * @return the useJavaContext.
    */

   @Override
   public final Boolean isUseJavaContext()
   {
      return useJavaContext;
   }

   /**
    * Get the poolName.
    *
    * @return the poolName.
    */

   @Override
   public final String getPoolName()
   {
      return poolName;
   }

   /**
    * Get the enabled.
    *
    * @return the enabled.
    */

   @Override
   public final Boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Get the jndiName.
    *
    * @return the jndiName.
    */

   @Override
   public final String getJndiName()
   {
      return jndiName;
   }

   /**
    * Get the spy
    *
    * @return the spy.
    */

   @Override
   public final Boolean isSpy()
   {
      return spy;
   }

   /**
    * Get the use ccm
    *
    * @return the use ccm.
    */

   @Override
   public final Boolean isUseCcm()
   {
      return useCcm;
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
         throw new ValidateException(bundle.requiredAttributeMissing(DataSource.Attribute.JNDI_NAME.getLocalName(),
                                                                     this.getClass().getCanonicalName()));
      if (this.poolName == null)
         throw new ValidateException(bundle.requiredAttributeMissing(DataSource.Attribute.POOL_NAME.getLocalName(),
                                                                     this.getClass().getCanonicalName()));

      if (this.timeOut != null)
         this.timeOut.validate();
      if (this.security != null)
         this.security.validate();
      if (this.statement != null)
         this.statement.validate();
      if (this.validation != null)
         this.validation.validate();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + ((security == null) ? 0 : security.hashCode());
      result = prime * result + ((statement == null) ? 0 : statement.hashCode());
      result = prime * result + ((timeOut == null) ? 0 : timeOut.hashCode());
      result = prime * result + ((transactionIsolation == null) ? 0 : transactionIsolation.hashCode());
      result = prime * result + ((urlDelimiter == null) ? 0 : urlDelimiter.hashCode());
      result = prime * result +
               ((urlSelectorStrategyClassName == null) ? 0 : urlSelectorStrategyClassName.hashCode());
      result = prime * result + ((useJavaContext == null) ? 0 : useJavaContext.hashCode());
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
      if (poolName == null)
      {
         if (other.poolName != null)
            return false;
      }
      else if (!poolName.equals(other.poolName))
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
      if (useJavaContext == null)
      {
         if (other.useJavaContext != null)
            return false;
      }
      else if (!useJavaContext.equals(other.useJavaContext))
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
   public final void setEnabled(Boolean enabled)
   {
      this.enabled = enabled;
   }

   /**
    * {@inheritDoc}
    */
   public abstract String toString();
}
