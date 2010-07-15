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
package org.jboss.jca.common.metadata.jbossra.jbossra20;

import org.jboss.jca.common.metadata.JCAMetadata;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 */
public class JbossRa implements JCAMetadata
{

   /**
    */
   private static final long serialVersionUID = -1494921311038998843L;

   private final List<RaConfigProperty<?>> raConfigProperties;

   private final String bootstrapContext;

   private final List<BeanValidationGroups> beanValidationGroups;

   /**
    * @param raConfigProperties List of properties for configuration
    * @param bootstrapContext String representing the bootstrap context name
    * @param beanValidationGroups for validations
    */
   public JbossRa(List<RaConfigProperty<?>> raConfigProperties, String bootstrapContext,
         List<BeanValidationGroups> beanValidationGroups)
   {
      super();
      this.raConfigProperties = raConfigProperties;
      this.bootstrapContext = bootstrapContext;
      this.beanValidationGroups = beanValidationGroups;
   }

   /**
    * @return raConfigProperties
    */
   public List<RaConfigProperty<?>> getRaConfigProperties()
   {
      return Collections.unmodifiableList(raConfigProperties);
   }

   /**
    * @return bootstrapContext
    */
   public String getBootstrapContext()
   {
      return bootstrapContext;
   }

   /**
    * @return beanValidationGroups
    */
   public List<BeanValidationGroups> getBeanValidationGroups()
   {
      return Collections.unmodifiableList(beanValidationGroups);
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
      result = prime * result + ((beanValidationGroups == null) ? 0 : beanValidationGroups.hashCode());
      result = prime * result + ((bootstrapContext == null) ? 0 : bootstrapContext.hashCode());
      result = prime * result + ((raConfigProperties == null) ? 0 : raConfigProperties.hashCode());
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
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof JbossRa))
         return false;
      JbossRa other = (JbossRa) obj;
      if (beanValidationGroups == null)
      {
         if (other.beanValidationGroups != null)
            return false;
      }
      else if (!beanValidationGroups.equals(other.beanValidationGroups))
         return false;
      if (bootstrapContext == null)
      {
         if (other.bootstrapContext != null)
            return false;
      }
      else if (!bootstrapContext.equals(other.bootstrapContext))
         return false;
      if (raConfigProperties == null)
      {
         if (other.raConfigProperties != null)
            return false;
      }
      else if (!raConfigProperties.equals(other.raConfigProperties))
         return false;
      return true;
   }

}
