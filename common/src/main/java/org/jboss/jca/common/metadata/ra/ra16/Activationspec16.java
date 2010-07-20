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

import org.jboss.jca.common.metadata.ra.ra15.Activationspec15;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class Activationspec16 extends Activationspec15
{
   /**
    */
   private static final long serialVersionUID = -6951903183562100136L;

   private final List<ConfigProperty16> configProperty;

   /**
    * @param activationspecClass full qualified name of the class
    * @param requiredConfigProperty a List of required config properties
    * @param configProperty a list of (optional) config property
    * @param id xmlID
    */
   public Activationspec16(String activationspecClass, List<RequiredConfigProperty> requiredConfigProperty,
         List<ConfigProperty16> configProperty, String id)
   {
      super(activationspecClass, requiredConfigProperty, id);
      this.configProperty = configProperty;
   }

   /**
    * @return configProperty
    */
   public List<ConfigProperty16> getConfigProperty()
   {
      return Collections.unmodifiableList(configProperty);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof Activationspec16))
         return false;
      Activationspec16 other = (Activationspec16) obj;
      if (configProperty == null)
      {
         if (other.configProperty != null)
            return false;
      }
      else if (!configProperty.equals(other.configProperty))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "Activationspec [configProperty=" + configProperty + ", activationspecClass=" + activationspecClass
            + ", requiredConfigProperty=" + requiredConfigProperty + ", id=" + id + "]";
   }

}
