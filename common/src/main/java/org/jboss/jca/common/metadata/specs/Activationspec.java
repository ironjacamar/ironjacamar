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
package org.jboss.jca.common.metadata.specs;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class Activationspec implements IdDecoratedMetadata
{
   /**
    */
   private static final long serialVersionUID = -6951903183562100136L;

   private final String activationspecClass;

   private final List<RequiredConfigProperty> requiredConfigProperty;

   private final List<ConfigProperty> configProperty;

   private final String id;

   /**
    * @param activationspecClass full qualified name of the class
    * @param requiredConfigProperty a List of required config properties
    * @param configProperty a list of (optional) config property
    * @param id xmlID
    */
   public Activationspec(String activationspecClass, List<RequiredConfigProperty> requiredConfigProperty,
         List<ConfigProperty> configProperty, String id)
   {
      super();
      this.activationspecClass = activationspecClass;
      this.requiredConfigProperty = requiredConfigProperty;
      this.configProperty = configProperty;
      this.id = id;
   }

   /**
    * @return activationspecClass
    */
   public String getActivationspecClass()
   {
      return activationspecClass;
   }

   /**
    * @return requiredConfigProperty
    */
   public List<RequiredConfigProperty> getRequiredConfigProperty()
   {
      return Collections.unmodifiableList(requiredConfigProperty);
   }

   /**
    * @return configProperty
    */
   public List<ConfigProperty> getConfigProperty()
   {
      return Collections.unmodifiableList(configProperty);
   }

   /**
    * {@inheritDoc}
    *
    * @see IdDecoratedMetadata#getId()
    */
   @Override
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((activationspecClass == null) ? 0 : activationspecClass.hashCode());
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((requiredConfigProperty == null) ? 0 : requiredConfigProperty.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof Activationspec))
      {
         return false;
      }
      Activationspec other = (Activationspec) obj;
      if (activationspecClass == null)
      {
         if (other.activationspecClass != null)
         {
            return false;
         }
      }
      else if (!activationspecClass.equals(other.activationspecClass))
      {
         return false;
      }
      if (configProperty == null)
      {
         if (other.configProperty != null)
         {
            return false;
         }
      }
      else if (!configProperty.equals(other.configProperty))
      {
         return false;
      }
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if (!id.equals(other.id))
      {
         return false;
      }
      if (requiredConfigProperty == null)
      {
         if (other.requiredConfigProperty != null)
         {
            return false;
         }
      }
      else if (!requiredConfigProperty.equals(other.requiredConfigProperty))
      {
         return false;
      }
      return true;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Activationspec [activationspecClass=" + activationspecClass + ", requiredConfigProperty="
            + requiredConfigProperty + ", configProperty=" + configProperty + ", id=" + id + "]";
   }

}
