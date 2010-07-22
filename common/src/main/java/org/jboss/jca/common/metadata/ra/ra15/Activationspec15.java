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
package org.jboss.jca.common.metadata.ra.ra15;

import org.jboss.jca.common.metadata.ra.common.IdDecoratedMetadata;
import org.jboss.jca.common.metadata.ra.common.XsdString;
import org.jboss.jca.common.metadata.ra.ra16.RequiredConfigProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * A Activationspec15.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class Activationspec15 implements IdDecoratedMetadata
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -342663654303158977L;

   /**
    * activationspec class name
    */
   protected final XsdString activationspecClass;

   /**
    * list of required properties
    */
   protected final ArrayList<RequiredConfigProperty> requiredConfigProperty;

   /**
    * id attribute in xml file
    */
   protected final String id;

   /**
    *
    * Create a new Activationspec15.
    *
    * @param activationspecClass activation spec class name
    * @param requiredConfigProperty list of required property
    * @param id id attribute of xml file
    */
   public Activationspec15(XsdString activationspecClass, ArrayList<RequiredConfigProperty> requiredConfigProperty,
         String id)
   {
      super();
      this.activationspecClass = activationspecClass;
      this.requiredConfigProperty = requiredConfigProperty;
      this.id = id;
   }

   /**
    * @return activationspecClass
    */
   public XsdString getActivationspecClass()
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
    * {@inheritDoc}
    *
    * @see IdDecoratedMetadata#getId()
    */
   @Override
   public String getId()
   {
      return id;
   }


   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((activationspecClass == null) ? 0 : activationspecClass.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((requiredConfigProperty == null) ? 0 : requiredConfigProperty.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof Activationspec15))
         return false;
      Activationspec15 other = (Activationspec15) obj;
      if (activationspecClass == null)
      {
         if (other.activationspecClass != null)
            return false;
      }
      else if (!activationspecClass.equals(other.activationspecClass))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (requiredConfigProperty == null)
      {
         if (other.requiredConfigProperty != null)
            return false;
      }
      else if (!requiredConfigProperty.equals(other.requiredConfigProperty))
         return false;
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
            + requiredConfigProperty + ", id=" + id + "]";
   }

}
