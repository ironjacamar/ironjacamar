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

import org.jboss.jca.common.api.metadata.common.AdminObject;
import org.jboss.jca.common.api.metadata.common.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;
import org.jboss.jca.common.metadata.common.CommonIronJacamarImpl;

import java.util.List;
import java.util.Map;

/**
 *
 * A ResourceAdapterImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ResourceAdapterImpl extends CommonIronJacamarImpl implements ResourceAdapter
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 7607776873201143875L;
   private final String archive;
   /**
    *
    * Create a new ResourceAdapterImpl.
    *
    * @param archive archive
    * @param transactionSupport transactionSupport
    * @param connectionDefinitions connectionDefinitions
    * @param adminObjects adminObjects
    * @param configProperties configProperties
    * @param beanValidationGroups beanValidationGroups
    * @param bootstrapContext bootstrapContext
    */
   public ResourceAdapterImpl(String archive, TransactionSupportEnum transactionSupport,
      List<ConnectionDefinition> connectionDefinitions, List<AdminObject> adminObjects,
      Map<String, String> configProperties, List<String> beanValidationGroups, String bootstrapContext)
   {
      super(transactionSupport, configProperties, adminObjects, connectionDefinitions, beanValidationGroups,
            bootstrapContext);
      this.archive = archive;
   }

   /**
    * Get the archive.
    *
    * @return the archive.
    */
   @Override
   public final String getArchive()
   {
      return archive;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((archive == null) ? 0 : archive.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof ResourceAdapterImpl))
         return false;
      ResourceAdapterImpl other = (ResourceAdapterImpl) obj;
      if (archive == null)
      {
         if (other.archive != null)
            return false;
      }
      else if (!archive.equals(other.archive))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "ResourceAdapterImpl [archive=" + archive + ", transactionSupport=" + transactionSupport +
             ", configProperties=" + configProperties + ", adminObjects=" + adminObjects +
             ", connectionDefinitions=" + connectionDefinitions + ", beanValidationGroups=" +
             beanValidationGroups + ", bootstrapContext=" + bootstrapContext + "]";
   }
}

