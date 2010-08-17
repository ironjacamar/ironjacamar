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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * A AdminObjectImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class AdminObjectImpl implements AdminObject
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8137442556861441967L;

   private final HashMap<String, String> configProperties;

   private final String className;

   private final String jndiName;

   private final boolean enabled;

   private final boolean useJavaContext;

   /**
    * Create a new AdminObjectImpl.
    *
    * @param configProperties configProperties
    * @param className className
    * @param jndiName jndiName
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    */
   public AdminObjectImpl(Map<String, String> configProperties, String className, String jndiName, boolean enabled,
         boolean useJavaContext)
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
      this.enabled = enabled;
      this.useJavaContext = useJavaContext;
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
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + (enabled ? 1231 : 1237);
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + (useJavaContext ? 1231 : 1237);
      return result;
   }

   @Override
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
      if (enabled != other.enabled)
         return false;
      if (jndiName == null)
      {
         if (other.jndiName != null)
            return false;
      }
      else if (!jndiName.equals(other.jndiName))
         return false;
      if (useJavaContext != other.useJavaContext)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "AdminObjectImpl [configProperties=" + configProperties + ", className=" + className + ", jndiName="
            + jndiName + ", enabled=" + enabled + ", useJavaContext=" + useJavaContext + "]";
   }
}

