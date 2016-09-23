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
package org.ironjacamar.common.metadata.common;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.logging.Messages;

/**
 * An extension
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ExtensionImpl extends AbstractMetadata implements Extension
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6275984008991105644L;

   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private String className;

   private Map<String, String> configPropertiesMap;

   private String moduleName;

   private String moduleSlot;

   /**
    * Constructor
    * @param className the className
    * @param moduleName module Name
    * @param moduleSlot module slot
    * @param configPropertiesMap configPropertiesMap
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public ExtensionImpl(String className, String moduleName, String moduleSlot, Map<String, String> configPropertiesMap,
                        Map<String, String> expressions) throws ValidateException
   {
      super(expressions);
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
      this.moduleName = moduleName;
      this.moduleSlot = moduleSlot;
      this.validate();
   }

   /**
    * Get the className.
    *
    * @return the className.
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Get the configPropertiesMap.
    *
    * @return the configPropertiesMap.
    */
   public Map<String, String> getConfigPropertiesMap()
   {
      return Collections.unmodifiableMap(configPropertiesMap);
   }

   /**
    * Get the module name.
    * @return the module name.
    */
   public String getModuleName()
   {
      return moduleName;
   }

   /**
    * Get the module slot.
    * @return the module slot.
    */
   public  String getModuleSlot()
   {
      return moduleSlot;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configPropertiesMap == null) ? 0 : configPropertiesMap.hashCode());
      result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
      result = prime * result + ((moduleSlot == null) ? 0 : moduleSlot.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ExtensionImpl))
         return false;
      ExtensionImpl other = (ExtensionImpl) obj;
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
      if (moduleName == null)
      {
         if (other.moduleName != null)
            return false;
      }
      else if (!moduleName.equals(other.moduleName))
         return false;
      if (moduleSlot == null)
      {
         if (other.moduleSlot != null)
            return false;
      }
      else if (!moduleSlot.equals(other.moduleSlot))
         return false;

      return true;
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.className == null || className.trim().length() == 0)
         throw new ValidateException(bundle.missingClassName(this.getClass().getCanonicalName()));
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);
      sb.append(" ").append("class-name").append("=\"");
      sb.append(this.getClassName()).append("\"");
      if (this.getModuleName() != null)
      {
         sb.append(" ").append("module-name").append("=\"");
         sb.append(this.getModuleName()).append("\"");
      }
      if (this.getModuleSlot() != null)
      {
         sb.append(" ").append("module-slot").append("=\"");
         sb.append(this.getModuleSlot()).append("\"");
      }
      sb.append(">");

      if (!this.getConfigPropertiesMap().isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = this.getConfigPropertiesMap().entrySet().iterator();

         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            sb.append("<").append("config-property");
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append("config-property").append(">");
         }
      }
      return sb.toString();
   }
}

