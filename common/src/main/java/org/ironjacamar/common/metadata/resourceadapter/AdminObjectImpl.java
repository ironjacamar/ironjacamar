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
package org.ironjacamar.common.metadata.resourceadapter;

import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * A AdminObjectImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class AdminObjectImpl extends AbstractMetadata implements AdminObject
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Map<String, String> configProperties;

   private String className;

   private String jndiName;

   private String poolName;

   private Boolean enabled;

   private Boolean useJavaContext;

   /**
    * Constructor
    *
    * @param configProperties configProperties
    * @param className className
    * @param jndiName jndiName
    * @param poolName poolName
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    * @param expressions expressions
    */
   public AdminObjectImpl(Map<String, String> configProperties, String className, String jndiName,
                          String poolName, Boolean enabled, Boolean useJavaContext,
                          Map<String, String> expressions)
   {
      super(expressions);
      if (configProperties != null)
      {
         this.configProperties = new TreeMap<String, String>();
         this.configProperties.putAll(configProperties);
      }
      else
      {
         this.configProperties = new TreeMap<String, String>();
      }
      this.className = className;
      this.jndiName = jndiName;
      this.poolName = poolName;
      this.enabled = enabled;
      this.useJavaContext = useJavaContext;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, String> getConfigProperties()
   {
      return Collections.unmodifiableMap(configProperties);
   }

   /**
    * {@inheritDoc}
    */
   public String getClassName()
   {
      return className;
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
   public Boolean isEnabled()
   {
      return enabled;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isUseJavaContext()
   {
      return useJavaContext;
   }

   /**
    * {@inheritDoc}
    */
   public String getPoolName()
   {
      return poolName;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + ((useJavaContext == null) ? 0 : useJavaContext.hashCode());
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
      if (!(obj instanceof AdminObjectImpl))
         return false;
      AdminObjectImpl other = (AdminObjectImpl) obj;
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
      if (poolName == null)
      {
         if (other.poolName != null)
            return false;
      }
      else if (!poolName.equals(other.poolName))
         return false;
      if (useJavaContext == null)
      {
         if (other.useJavaContext != null)
            return false;
      }
      else if (!useJavaContext.equals(other.useJavaContext))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<admin-object");

      if (className != null)
         sb.append(" ").append(XML.ATTRIBUTE_CLASS_NAME).append("=\"").append(className).append("\"");

      if (jndiName != null)
         sb.append(" ").append(XML.ATTRIBUTE_JNDI_NAME).append("=\"").append(jndiName).append("\"");

      if (enabled != null && !Defaults.ENABLED.equals(enabled))
         sb.append(" ").append(XML.ATTRIBUTE_ENABLED).append("=\"").append(enabled).append("\"");

      if (useJavaContext != null && !Defaults.USE_JAVA_CONTEXT.equals(useJavaContext))
      {
         sb.append(" ").append(XML.ATTRIBUTE_USE_JAVA_CONTEXT);
         sb.append("=\"").append(useJavaContext).append("\"");
      }

      if (poolName != null)
         sb.append(" ").append(XML.ATTRIBUTE_POOL_NAME).append("=\"").append(poolName).append("\"");

      sb.append(">");

      if (configProperties != null && configProperties.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = configProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            sb.append("<").append(XML.ELEMENT_CONFIG_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(XML.ELEMENT_CONFIG_PROPERTY).append(">");
         }
      }

      sb.append("</admin-object>");
      
      return sb.toString();
   }
}
