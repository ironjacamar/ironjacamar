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
package org.jboss.jca.common.metadata.resourceadpater;

import org.jboss.jca.common.api.metadata.resourceadapter.NoTxConnectionFactory;
import org.jboss.jca.common.api.metadata.resourceadapter.Security;
import org.jboss.jca.common.api.metadata.resourceadapter.TimeOut;
import org.jboss.jca.common.api.metadata.resourceadapter.Validation;

import java.util.HashMap;

/**
 *
 * A NoTxConnectionFactoryImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class NoTxConnectionFactoryImpl implements NoTxConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2475537312421610650L;

   /** minPoolSize field **/
   protected final Integer minPoolSize;

   /** maxPoolSize field **/
   protected final Integer maxPoolSize;

   /** prefill field **/
   protected final boolean prefill;

   /** userName field **/
   protected final String userName;

   /** password field **/
   protected final String password;

   /** connectionDefinition field **/
   protected final String connectionDefinition;

   /** configProperty field **/
   protected final HashMap<String, String> configProperty;

   /** security field **/
   protected final Security security;

   /** timeOut field **/
   protected final TimeOut timeOut;

   /** validation field **/
   protected final Validation validation;

   /** poolName field **/
   protected final String poolName;

   /** className field **/
   protected final String className;

   /** jndiName field **/
   protected final String jndiName;

   /** enabled field **/
   protected final boolean enabled;

   /** useJavaContext field **/
   protected final boolean useJavaContext;

   /**
    * Create a new NoTxConnectionFactoryImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param userName userName
    * @param password password
    * @param connectionDefinition connectionDefinition
    * @param configProperty configProperty
    * @param security security
    * @param timeOut timeOut
    * @param validation validation
    * @param poolName poolName
    * @param className className
    * @param jndiName jndiName
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    */
   public NoTxConnectionFactoryImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, String userName,
         String password, String connectionDefinition, HashMap<String, String> configProperty, Security security,
         TimeOut timeOut, Validation validation, String poolName, String className, String jndiName, boolean enabled,
         boolean useJavaContext)
   {
      super();
      this.minPoolSize = minPoolSize;
      this.maxPoolSize = maxPoolSize;
      this.prefill = prefill;
      this.userName = userName;
      this.password = password;
      this.connectionDefinition = connectionDefinition;
      this.configProperty = configProperty;
      this.security = security;
      this.timeOut = timeOut;
      this.validation = validation;
      this.poolName = poolName;
      this.className = className;
      this.jndiName = jndiName;
      this.enabled = enabled;
      this.useJavaContext = useJavaContext;
   }

   /**
    * Get the minPoolSize.
    *
    * @return the minPoolSize.
    */
   @Override
   public final Integer getMinPoolSize()
   {
      return minPoolSize;
   }

   /**
    * Get the maxPoolSize.
    *
    * @return the maxPoolSize.
    */
   @Override
   public final Integer getMaxPoolSize()
   {
      return maxPoolSize;
   }

   /**
    * Get the prefill.
    *
    * @return the prefill.
    */
   @Override
   public final boolean isPrefill()
   {
      return prefill;
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
    * Get the connectionDefinition.
    *
    * @return the connectionDefinition.
    */
   @Override
   public final String getConnectionDefinition()
   {
      return connectionDefinition;
   }

   /**
    * Get the configProperty.
    *
    * @return the configProperty.
    */
   @Override
   public final HashMap<String, String> getConfigProperty()
   {
      return configProperty;
   }

   /**
    * Get the security.
    *
    * @return the security.
    */
   @Override
   public final Security getSecurity()
   {
      return security;
   }

   /**
    * Get the timeOut.
    *
    * @return the timeOut.
    */
   @Override
   public final TimeOut getTimeOut()
   {
      return timeOut;
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
    * Get the className.
    *
    * @return the className.
    */
   @Override
   public final String getClassName()
   {
      return className;
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
    * Get the enabled.
    *
    * @return the enabled.
    */
   @Override
   public final boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Get the useJavaContext.
    *
    * @return the useJavaContext.
    */
   @Override
   public final boolean isUseJavaContext()
   {
      return useJavaContext;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      result = prime * result + ((connectionDefinition == null) ? 0 : connectionDefinition.hashCode());
      result = prime * result + (enabled ? 1231 : 1237);
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((maxPoolSize == null) ? 0 : maxPoolSize.hashCode());
      result = prime * result + ((minPoolSize == null) ? 0 : minPoolSize.hashCode());
      result = prime * result + ((password == null) ? 0 : password.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + (prefill ? 1231 : 1237);
      result = prime * result + ((security == null) ? 0 : security.hashCode());
      result = prime * result + ((timeOut == null) ? 0 : timeOut.hashCode());
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
      if (!(obj instanceof NoTxConnectionFactoryImpl))
         return false;
      NoTxConnectionFactoryImpl other = (NoTxConnectionFactoryImpl) obj;
      if (className == null)
      {
         if (other.className != null)
            return false;
      }
      else if (!className.equals(other.className))
         return false;
      if (configProperty == null)
      {
         if (other.configProperty != null)
            return false;
      }
      else if (!configProperty.equals(other.configProperty))
         return false;
      if (connectionDefinition == null)
      {
         if (other.connectionDefinition != null)
            return false;
      }
      else if (!connectionDefinition.equals(other.connectionDefinition))
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
      if (timeOut == null)
      {
         if (other.timeOut != null)
            return false;
      }
      else if (!timeOut.equals(other.timeOut))
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
      return "NoTxConnectionFactoryImpl [minPoolSize=" + minPoolSize + ", maxPoolSize=" + maxPoolSize + ", prefill="
            + prefill + ", userName=" + userName + ", password=" + password + ", connectionDefinition="
            + connectionDefinition + ", configProperty=" + configProperty + ", security=" + security + ", timeOut="
            + timeOut + ", validation=" + validation + ", poolName=" + poolName + ", className=" + className
            + ", jndiName=" + jndiName + ", enabled=" + enabled + ", useJavaContext=" + useJavaContext + "]";
   }
}
