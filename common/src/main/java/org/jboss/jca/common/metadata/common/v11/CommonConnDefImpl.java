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
package org.jboss.jca.common.metadata.common.v11;

import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.v11.CommonConnDef;

import java.util.Iterator;
import java.util.Map;

/**
 * A ConnectionDefinition.
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CommonConnDefImpl extends org.jboss.jca.common.metadata.common.v10.CommonConnDefImpl
   implements CommonConnDef
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7109775624169563102L;

   private final Boolean sharable;

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
    * @param sharable sharable
    * @param pool pool
    * @param timeOut timeOut
    * @param validation validation
    * @param security security
    * @param recovery recovery
    */
   public CommonConnDefImpl(Map<String, String> configProperties, String className, String jndiName,
                            String poolName, Boolean enabled, Boolean useJavaContext, Boolean useCcm, Boolean sharable,
                            CommonPool pool, CommonTimeOut timeOut,
                            CommonValidation validation, CommonSecurity security, Recovery recovery)
   {
      super(configProperties, className, jndiName, poolName, enabled, useJavaContext, useCcm,
            pool, timeOut, validation, security, recovery);

      this.sharable = sharable;
   }

   /**
    * Get the sharable
    *
    * @return the sharable
    */
   @Override
   public final Boolean isSharable()
   {
      return sharable;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((sharable == null) ? 0 : sharable.hashCode());
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
      if (!super.equals(obj))
         return false;

      CommonConnDefImpl other = (CommonConnDefImpl) obj;
      if (sharable == null)
      {
         if (other.sharable != null)
            return false;
      }
      else if (!sharable.equals(other.sharable))
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

      if (sharable != null)
         sb.append(" ").append(CommonConnDef.Attribute.SHARABLE).append("=\"").append(sharable).append("\"");

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
