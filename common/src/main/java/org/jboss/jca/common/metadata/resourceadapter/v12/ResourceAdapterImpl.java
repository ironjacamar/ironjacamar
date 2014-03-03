/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.resourceadapter.v12;

import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.v11.WorkManager;
import org.jboss.jca.common.api.metadata.common.v12.CommonConnDef;
import org.jboss.jca.common.api.metadata.resourceadapter.v12.ResourceAdapter;
import org.jboss.jca.common.metadata.common.v12.CommonIronJacamarImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A ResourceAdapterImpl.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterImpl extends CommonIronJacamarImpl implements ResourceAdapter
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** archive */
   protected final String archive;

   /** id */
   protected final String id;

   /** workmanager */
   protected final WorkManager workmanager;

   /**
    * Create a new ResourceAdapterImpl.
    *
    * @param id The id
    * @param archive archive
    * @param transactionSupport transactionSupport
    * @param connectionDefinitions connectionDefinitions
    * @param adminObjects adminObjects
    * @param configProperties configProperties
    * @param beanValidationGroups beanValidationGroups
    * @param bootstrapContext bootstrapContext
    * @param workmanager workmanager
    */
   public ResourceAdapterImpl(String id, String archive, TransactionSupportEnum transactionSupport,
                              List<CommonConnDef> connectionDefinitions, List<CommonAdminObject> adminObjects,
                              Map<String, String> configProperties, List<String> beanValidationGroups,
                              String bootstrapContext, WorkManager workmanager)
   {
      super(transactionSupport, configProperties, adminObjects, connectionDefinitions, beanValidationGroups,
            bootstrapContext);

      this.id = id;
      this.archive = archive;
      this.workmanager = workmanager;
   }

   /**
    * Get the id.
    *
    * @return the value.
    */
   @Override
   public final String getId()
   {
      return id;
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

   /**
    * {@inheritDoc}
    */
   public WorkManager getWorkManager()
   {
      return workmanager;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((archive == null) ? 0 : archive.hashCode());
      result = prime * result + ((workmanager == null) ? 0 : workmanager.hashCode());
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
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (archive == null)
      {
         if (other.archive != null)
            return false;
      }
      else if (!archive.equals(other.archive))
         return false;
      if (workmanager == null)
      {
         if (other.workmanager != null)
            return false;
      }
      else if (!workmanager.equals(other.workmanager))
         return false;

      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<resource-adapter");

      if (id != null)
      {
         sb.append(" ").append(ResourceAdapter.Attribute.ID).append("=\"").append(id).append("\"");
      }

      sb.append(">");

      sb.append("<").append(ResourceAdapter.Tag.ARCHIVE).append(">");
      sb.append(archive);
      sb.append("</").append(ResourceAdapter.Tag.ARCHIVE).append(">");

      if (beanValidationGroups != null && beanValidationGroups.size() > 0)
      {
         sb.append("<").append(ResourceAdapter.Tag.BEAN_VALIDATION_GROUPS).append(">");
         for (String bvg : beanValidationGroups)
         {
            sb.append("<").append(ResourceAdapter.Tag.BEAN_VALIDATION_GROUP).append(">");
            sb.append(bvg);
            sb.append("</").append(ResourceAdapter.Tag.BEAN_VALIDATION_GROUP).append(">");
         }
         sb.append("</").append(ResourceAdapter.Tag.BEAN_VALIDATION_GROUPS).append(">");
      }

      if (bootstrapContext != null)
      {
         sb.append("<").append(ResourceAdapter.Tag.BOOTSTRAP_CONTEXT).append(">");
         sb.append(bootstrapContext);
         sb.append("</").append(ResourceAdapter.Tag.BOOTSTRAP_CONTEXT).append(">");
      }

      if (configProperties != null && configProperties.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = configProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            sb.append("<").append(ResourceAdapter.Tag.CONFIG_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(ResourceAdapter.Tag.CONFIG_PROPERTY).append(">");
         }
      }
      
      if (transactionSupport != null)
      {
         sb.append("<").append(ResourceAdapter.Tag.TRANSACTION_SUPPORT).append(">");
         sb.append(transactionSupport);
         sb.append("</").append(ResourceAdapter.Tag.TRANSACTION_SUPPORT).append(">");
      }

      if (workmanager != null)
      {
         sb.append(workmanager);
      }

      if (connectionDefinitions != null && connectionDefinitions.size() > 0)
      {
         sb.append("<").append(ResourceAdapter.Tag.CONNECTION_DEFINITIONS).append(">");
         for (CommonConnDef cd : connectionDefinitions)
         {
            sb.append(cd);
         }
         sb.append("</").append(ResourceAdapter.Tag.CONNECTION_DEFINITIONS).append(">");
      }

      if (adminObjects != null && adminObjects.size() > 0)
      {
         sb.append("<").append(ResourceAdapter.Tag.ADMIN_OBJECTS).append(">");
         for (CommonAdminObject ao : adminObjects)
         {
            sb.append(ao);
         }
         sb.append("</").append(ResourceAdapter.Tag.ADMIN_OBJECTS).append(">");
      }

      sb.append("</resource-adapter>");
      
      return sb.toString();
   }
}
