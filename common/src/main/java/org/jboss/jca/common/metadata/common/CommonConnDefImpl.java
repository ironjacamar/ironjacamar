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
package org.jboss.jca.common.metadata.common;

import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * A ConnectionDefinition.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CommonConnDefImpl implements CommonConnDef
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7109775624169563102L;

   private final HashMap<String, String> configProperties;

   private final String className;

   private final String jndiName;

   private final String poolName;

   private final Boolean enabled;

   private final Boolean useJavaContext;

   private final CommonPool pool;

   private final CommonTimeOut timeOut;

   private final CommonValidation validation;

   private final CommonSecurity security;

   /**
    * Create a new ConnectionDefinition.
    *
    * @param configProperties configProperties
    * @param className className
    * @param jndiName jndiName
    * @param poolName poolName
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    * @param pool pool
    * @param timeOut timeOut
    * @param validation validation
    * @param security security
    */
   public CommonConnDefImpl(Map<String, String> configProperties, String className, String jndiName,
      String poolName, Boolean enabled, Boolean useJavaContext, CommonPool pool, CommonTimeOut timeOut,
      CommonValidation validation, CommonSecurity security)
   {
      super();
      if (configProperties != null)
      {
         this.configProperties = new HashMap<String, String>(configProperties.size());
         this.configProperties.putAll(configProperties);
      }
      else
      {
         this.configProperties = new HashMap<String, String>(0);
      }
      this.className = className;
      this.jndiName = jndiName;
      this.poolName = poolName;
      this.enabled = enabled;
      this.useJavaContext = useJavaContext;
      this.pool = pool;
      this.timeOut = timeOut;
      this.validation = validation;
      this.security = security;

   }

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   @Override
   public final Map<String, String> getConfigProperties()
   {
      return Collections.unmodifiableMap(configProperties);
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
    * Get the pool.
    *
    * @return the pool.
    */
   @Override
   public final CommonPool getPool()
   {
      return pool;
   }

   /**
    * Get the timeOut.
    *
    * @return the timeOut.
    */
   @Override
   public final CommonTimeOut getTimeOut()
   {
      return timeOut;
   }

   /**
    * Get the validation.
    *
    * @return the validation.
    */
   @Override
   public final CommonValidation getValidation()
   {
      return validation;
   }

   /**
    * Get the security.
    *
    * @return the security.
    */
   @Override
   public final CommonSecurity getSecurity()
   {
      return security;
   }

   @Override
   public final Boolean isXa()
   {
      return (pool instanceof CommonXaPool);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + (enabled ? 1231 : 1237);
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((pool == null) ? 0 : pool.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + ((security == null) ? 0 : security.hashCode());
      result = prime * result + ((timeOut == null) ? 0 : timeOut.hashCode());
      result = prime * result + (useJavaContext ? 1231 : 1237);
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
      if (!(obj instanceof CommonConnDefImpl))
         return false;
      CommonConnDefImpl other = (CommonConnDefImpl) obj;
      if (className == null)
      {
         if (other.className != null)
            return false;
      }
      else if (!className.equals(other.className))
         return false;
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
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
      if (pool == null)
      {
         if (other.pool != null)
            return false;
      }
      else if (!pool.equals(other.pool))
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
      if (timeOut == null)
      {
         if (other.timeOut != null)
            return false;
      }
      else if (!timeOut.equals(other.timeOut))
         return false;
      if (useJavaContext != other.useJavaContext)
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
      return "ConnectionDefinitionImpl [configProperties=" + configProperties + ", className=" + className +
             ", jndiName=" + jndiName + ", poolName=" + poolName + ", enabled=" + enabled + ", useJavaContext=" +
             useJavaContext + ", pool=" + pool + ", timeOut=" + timeOut + ", validation=" + validation +
             ", security=" + security + "]";
   }

}
