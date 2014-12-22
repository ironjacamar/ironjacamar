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
package org.ironjacamar.common.api.metadata.common;

import org.ironjacamar.common.api.metadata.JCAMetadata;
import org.ironjacamar.common.api.metadata.ValidatableMetadata;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * A JdbcAdapterExtension.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public final class Extension implements JCAMetadata, ValidatableMetadata
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6275984008991105644L;

   private final String className;

   private final Map<String, String> configPropertiesMap;

   /**
    * Create a new JdbcAdapterExtension.
    *
    * @param className the className
    * @param configPropertiesMap configPropertiesMap
    * @throws ValidateException ValidateException
    */
   public Extension(String className, Map<String, String> configPropertiesMap) throws ValidateException
   {
      super();
      this.className = className;
      if (configPropertiesMap != null)
      {
         this.configPropertiesMap = new HashMap<String, String>(configPropertiesMap.size());
         this.configPropertiesMap.putAll(configPropertiesMap);
      }
      else
      {
         this.configPropertiesMap = Collections.emptyMap();
      }
      this.validate();
   }

   /**
    * Get the className.
    *
    * @return the className.
    */
   public final String getClassName()
   {
      return className;
   }

   /**
    * Get the configPropertiesMap.
    *
    * @return the configPropertiesMap.
    */
   public final Map<String, String> getConfigPropertiesMap()
   {
      return Collections.unmodifiableMap(configPropertiesMap);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configPropertiesMap == null) ? 0 : configPropertiesMap.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof Extension))
         return false;
      Extension other = (Extension) obj;
      if (className == null)
      {
         if (other.className != null)
            return false;
      }
      else if (!className.equals(other.className))
         return false;
      if (configPropertiesMap == null)
      {
         if (other.configPropertiesMap != null)
            return false;
      }
      else if (!configPropertiesMap.equals(other.configPropertiesMap))
         return false;
      return true;
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.className == null || className.trim().length() == 0)
         throw new ValidateException("class-name is required in " + this.getClass().getCanonicalName());
   }
}

