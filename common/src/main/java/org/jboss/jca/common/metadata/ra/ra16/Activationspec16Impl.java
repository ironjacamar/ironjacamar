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
package org.jboss.jca.common.metadata.ra.ra16;

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra16.Activationspec16;
import org.jboss.jca.common.metadata.ra.ra15.Activationspec15Impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class Activationspec16Impl extends Activationspec15Impl implements Activationspec16
{
   /**
    */
   private static final long serialVersionUID = -6951903183562100136L;

   private final ArrayList<ConfigProperty> configProperties;

   /**
    * @param activationspecClass full qualified name of the class
    * @param requiredConfigProperty a List of required config properties
    * @param configProperties a list of (optional) config property
    * @param id xmlID
    */
   public Activationspec16Impl(XsdString activationspecClass, List<RequiredConfigProperty> requiredConfigProperty,
         List<? extends ConfigProperty> configProperties, String id)
   {
      super(activationspecClass, requiredConfigProperty, id);
      if (configProperties != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(configProperties.size());
         this.configProperties.addAll(configProperties);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
   }

   /**
    * @return configProperty
    */
   @Override
   public List<? extends ConfigProperty> getConfigProperties()
   {
      return configProperties == null ? null : Collections.unmodifiableList(configProperties);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof Activationspec16Impl))
         return false;
      Activationspec16Impl other = (Activationspec16Impl) obj;
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<activationspec");
      if (id != null)
         sb.append(" ").append(Activationspec16.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      sb.append(activationspecClass);

      if (requiredConfigProperty != null)
      {
         for (RequiredConfigProperty rcp : requiredConfigProperty)
         {
            sb.append(rcp);
         }
      }

      if (configProperties != null)
      {
         for (ConfigProperty cp : configProperties)
         {
            sb.append(cp);
         }
      }

      sb.append("</activationspec>");

      return sb.toString();
   }

   @Override
   public CopyableMetaData copy()
   {
      return new Activationspec16Impl(CopyUtil.clone(activationspecClass),
            CopyUtil.cloneList(requiredConfigProperty),
            CopyUtil.cloneList(configProperties),
            CopyUtil.cloneString(id));
   }

}
