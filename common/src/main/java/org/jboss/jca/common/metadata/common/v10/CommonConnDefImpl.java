/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.common.v10;

import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.v10.CommonConnDef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * A ConnectionDefinition.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class CommonConnDefImpl implements CommonConnDef
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7109775624169563102L;

   /** config-property */
   protected final Map<String, String> configProperties;

   /** class-name */
   protected final String className;

   /** jndi-name */
   protected final String jndiName;

   /** pool-name */
   protected final String poolName;

   /** enable */
   protected final Boolean enabled;

   /** use-java-context */
   protected final Boolean useJavaContext;

   /** use-ccm */
   protected final Boolean useCcm;

   /** pool */
   protected final CommonPool pool;

   /** timeout */
   protected final CommonTimeOut timeOut;

   /** validation */
   protected final CommonValidation validation;

   /** security */
   protected final CommonSecurity security;

   /** recovery */
   protected final Recovery recovery;

   /** isXA */
   protected final Boolean isXA;

   /**
    * Create a new ConnectionDefinition.
    *
    * @param configProperties configProperties
    * @param className className
    * @param jndiName jndiName
    * @param poolName poolName
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    * @param useCcm useCcm
    * @param pool pool
    * @param timeOut timeOut
    * @param validation validation
    * @param security security
    * @param recovery recovery
    * @param isXA isXA
    */
   public CommonConnDefImpl(Map<String, String> configProperties, String className, String jndiName,
                            String poolName, Boolean enabled, Boolean useJavaContext, Boolean useCcm,
                            CommonPool pool, CommonTimeOut timeOut,
                            CommonValidation validation, CommonSecurity security, Recovery recovery, Boolean isXA)
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
      this.useCcm = useCcm;
      this.pool = pool;
      this.timeOut = timeOut;
      this.validation = validation;
      this.security = security;
      this.recovery = recovery;
      this.isXA = isXA;
   }

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   @Override
   public Map<String, String> getConfigProperties()
   {
      return Collections.unmodifiableMap(configProperties);
   }

   /**
    * Get the className.
    *
    * @return the className.
    */
   @Override
   public String getClassName()
   {
      return className;
   }

   /**
    * Get the jndiName.
    *
    * @return the jndiName.
    */
   @Override
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Get the poolName.
    *
    * @return the poolName.
    */
   @Override
   public String getPoolName()
   {
      return poolName;
   }

   /**
    * Get the enabled.
    *
    * @return the enabled.
    */
   @Override
   public Boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Get the useJavaContext.
    *
    * @return the useJavaContext.
    */
   @Override
   public Boolean isUseJavaContext()
   {
      return useJavaContext;
   }

   /**
    * Get the useCcm.
    *
    * @return the useCcm.
    */
   @Override
   public Boolean isUseCcm()
   {
      return useCcm;
   }

   /**
    * Get the pool.
    *
    * @return the pool.
    */
   @Override
   public CommonPool getPool()
   {
      return pool;
   }

   /**
    * Get the timeOut.
    *
    * @return the timeOut.
    */
   @Override
   public CommonTimeOut getTimeOut()
   {
      return timeOut;
   }

   /**
    * Get the validation.
    *
    * @return the validation.
    */
   @Override
   public CommonValidation getValidation()
   {
      return validation;
   }

   /**
    * Get the security.
    *
    * @return the security.
    */
   @Override
   public CommonSecurity getSecurity()
   {
      return security;
   }

   /**
    * Get the recovery.
    *
    * @return the recovery.
    */
   @Override
   public Recovery getRecovery()
   {
      return recovery;
   }

   @Override
   public Boolean isXa()
   {
      return (pool instanceof CommonXaPool) || isXA != null ? isXA : Boolean.FALSE;
   }

   @Override
   public int hashCode()
   {
      int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((pool == null) ? 0 : pool.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + ((recovery == null) ? 0 : recovery.hashCode());
      result = prime * result + ((security == null) ? 0 : security.hashCode());
      result = prime * result + ((timeOut == null) ? 0 : timeOut.hashCode());
      result = prime * result + ((useJavaContext == null) ? 0 : useJavaContext.hashCode());
      result = prime * result + ((useCcm == null) ? 0 : useCcm.hashCode());
      result = prime * result + ((validation == null) ? 0 : validation.hashCode());
      result = prime * result + ((isXA == null) ? 0 : isXA.hashCode());
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
      if (recovery == null)
      {
         if (other.recovery != null)
            return false;
      }
      else if (!recovery.equals(other.recovery))
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
      if (useJavaContext == null)
      {
         if (other.useJavaContext != null)
            return false;
      }
      else if (!useJavaContext.equals(other.useJavaContext))
         return false;
      if (useCcm == null)
      {
         if (other.useCcm != null)
            return false;
      }
      else if (!useCcm.equals(other.useCcm))
         return false;
      if (validation == null)
      {
         if (other.validation != null)
            return false;
      }
      else if (!validation.equals(other.validation))
         return false;
      if (isXA == null)
      {
         if (other.isXA != null)
            return false;
      }
      else if (!isXA.equals(other.isXA))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<connection-definition");

      if (className != null)
         sb.append(" ").append(CommonConnDef.Attribute.CLASS_NAME).append("=\"").append(className).append("\"");

      if (jndiName != null)
         sb.append(" ").append(CommonConnDef.Attribute.JNDI_NAME).append("=\"").append(jndiName).append("\"");

      if (enabled != null)
         sb.append(" ").append(CommonConnDef.Attribute.ENABLED).append("=\"").append(enabled).append("\"");

      if (useJavaContext != null)
      {
         sb.append(" ").append(CommonConnDef.Attribute.USE_JAVA_CONTEXT);
         sb.append("=\"").append(useJavaContext).append("\"");
      }

      if (poolName != null)
         sb.append(" ").append(CommonConnDef.Attribute.POOL_NAME).append("=\"").append(poolName).append("\"");

      if (useCcm != null)
         sb.append(" ").append(CommonConnDef.Attribute.USE_CCM).append("=\"").append(useCcm).append("\"");

      sb.append(">");

      if (configProperties != null && configProperties.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = configProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            sb.append("<").append(CommonConnDef.Tag.CONFIG_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(CommonConnDef.Tag.CONFIG_PROPERTY).append(">");
         }
      }

      if (pool != null)
         sb.append(pool);


      if (security != null)
         sb.append(security);

      if (timeOut != null)
         sb.append(timeOut);

      if (validation != null)
         sb.append(validation);

      if (recovery != null)
         sb.append(recovery);

      sb.append("</connection-definition>");
      
      return sb.toString();
   }
}
