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
package org.ironjacamar.common.metadata.resourceadapter;

import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.resourceadapter.WorkManager;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A resource adapter activation
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ActivationImpl extends AbstractMetadata implements Activation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** transactionSupport **/
   protected TransactionSupportEnum transactionSupport;

   /**
    *  configProperties
    */
   protected Map<String, String> configProperties;

   /**
    *  adminObjects
    */
   protected List<AdminObject> adminObjects;

   /**
    *  connectionDefinitions
    */
   protected List<ConnectionDefinition> connectionDefinitions;

   /**
    *  beanValidationGroups
    */
   protected List<String> beanValidationGroups;

   /** bootstrapContext **/
   protected String bootstrapContext;

   /** archive */
   protected String archive;

   /** id */
   protected String id;

   /** workmanager */
   protected WorkManager workmanager;

   /**
    * Constructor
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
    * @param expressions expressions
    */
   public ActivationImpl(String id, String archive, TransactionSupportEnum transactionSupport,
                         List<ConnectionDefinition> connectionDefinitions, List<AdminObject> adminObjects,
                         Map<String, String> configProperties, List<String> beanValidationGroups,
                         String bootstrapContext, WorkManager workmanager,
                         Map<String, String> expressions)
   {
      super(expressions);
      this.id = id;
      this.archive = archive;
      this.transactionSupport = transactionSupport;

      if (connectionDefinitions != null)
      {
         this.connectionDefinitions = new ArrayList<ConnectionDefinition>(connectionDefinitions.size());
         this.connectionDefinitions.addAll(connectionDefinitions);
      }
      else
      {
         this.connectionDefinitions = null;
      }

      if (adminObjects != null)
      {
         this.adminObjects = new ArrayList<AdminObject>(adminObjects.size());
         this.adminObjects.addAll(adminObjects);
      }
      else
      {
         this.adminObjects = null;
      }

      if (configProperties != null)
      {
         this.configProperties = new TreeMap<String, String>();
         this.configProperties.putAll(configProperties);
      }
      else
      {
         this.configProperties = null;
      }

      if (beanValidationGroups != null)
      {
         this.beanValidationGroups = new ArrayList<String>(beanValidationGroups.size());
         this.beanValidationGroups.addAll(beanValidationGroups);
      }
      else
      {
         this.beanValidationGroups = null;
      }

      this.bootstrapContext = bootstrapContext;
      this.workmanager = workmanager;
   }

   /**
    * Get the transactionSupport.
    *
    * @return the transactionSupport.
    */
   @Override
   public TransactionSupportEnum getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * Get the connectionFactories.
    *
    * @return the connectionFactories.
    */
   @Override
   public List<ConnectionDefinition> getConnectionDefinitions()
   {
      if (connectionDefinitions == null)
         return null;

      return Collections.unmodifiableList(connectionDefinitions);
   }

   /**
    * Get the adminObjects.
    *
    * @return the adminObjects.
    */
   @Override
   public List<AdminObject> getAdminObjects()
   {
      return adminObjects == null ? null : Collections.unmodifiableList(adminObjects);
   }

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   @Override
   public Map<String, String> getConfigProperties()
   {
      return configProperties == null ? null : Collections.unmodifiableMap(configProperties);
   }

   /**
    * Get the beanValidationGroups.
    *
    * @return the beanValidationGroups.
    */
   @Override
   public List<String> getBeanValidationGroups()
   {
      return beanValidationGroups == null ? null : Collections.unmodifiableList(beanValidationGroups);
   }

   /**
    * Get the bootstrapContext.
    *
    * @return the bootstrapContext.
    */
   @Override
   public String getBootstrapContext()
   {
      return bootstrapContext;
   }

   /**
    * Get the id.
    *
    * @return the value.
    */
   @Override
   public String getId()
   {
      return id;
   }

   /**
    * Get the archive.
    *
    * @return the archive.
    */
   @Override
   public String getArchive()
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
      int result = 1;
      result = prime * result + ((adminObjects == null) ? 0 : adminObjects.hashCode());
      result = prime * result + ((beanValidationGroups == null) ? 0 : beanValidationGroups.hashCode());
      result = prime * result + ((bootstrapContext == null) ? 0 : bootstrapContext.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((connectionDefinitions == null) ? 0 : connectionDefinitions.hashCode());
      result = prime * result + ((transactionSupport == null) ? 0 : transactionSupport.hashCode());
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
      if (obj == null)
         return false;
      if (!(obj instanceof ActivationImpl))
         return false;
      ActivationImpl other = (ActivationImpl) obj;
      if (adminObjects == null)
      {
         if (other.adminObjects != null)
            return false;
      }
      else if (!adminObjects.equals(other.adminObjects))
         return false;
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
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
         return false;
      if (connectionDefinitions == null)
      {
         if (other.connectionDefinitions != null)
            return false;
      }
      else if (!connectionDefinitions.equals(other.connectionDefinitions))
         return false;
      if (transactionSupport != other.transactionSupport)
         return false;
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

      if (archive != null)
      {
         sb.append("<resource-adapter");
      }
      else
      {
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         sb.append("<ironjacamar");
      }

      if (id != null)
      {
         sb.append(" ").append(XML.ATTRIBUTE_ID).append("=\"").append(id).append("\"");
      }

      sb.append(">");

      if (archive != null)
      {
         sb.append("<").append(XML.ELEMENT_ARCHIVE).append(">");
         sb.append(archive);
         sb.append("</").append(XML.ELEMENT_ARCHIVE).append(">");
      }

      if (beanValidationGroups != null && !beanValidationGroups.isEmpty())
      {
         sb.append("<").append(XML.ELEMENT_BEAN_VALIDATION_GROUPS).append(">");
         for (String bvg : beanValidationGroups)
         {
            sb.append("<").append(XML.ELEMENT_BEAN_VALIDATION_GROUP).append(">");
            sb.append(bvg);
            sb.append("</").append(XML.ELEMENT_BEAN_VALIDATION_GROUP).append(">");
         }
         sb.append("</").append(XML.ELEMENT_BEAN_VALIDATION_GROUPS).append(">");
      }

      if (bootstrapContext != null)
      {
         sb.append("<").append(XML.ELEMENT_BOOTSTRAP_CONTEXT).append(">");
         sb.append(bootstrapContext);
         sb.append("</").append(XML.ELEMENT_BOOTSTRAP_CONTEXT).append(">");
      }

      if (configProperties != null && !configProperties.isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = configProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            sb.append("<").append(XML.ELEMENT_CONFIG_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(XML.ELEMENT_CONFIG_PROPERTY).append(">");
         }
      }
      
      if (transactionSupport != null)
      {
         sb.append("<").append(XML.ELEMENT_TRANSACTION_SUPPORT).append(">");
         sb.append(transactionSupport);
         sb.append("</").append(XML.ELEMENT_TRANSACTION_SUPPORT).append(">");
      }

      if (workmanager != null)
      {
         sb.append(workmanager);
      }

      if (connectionDefinitions != null && !connectionDefinitions.isEmpty())
      {
         sb.append("<").append(XML.ELEMENT_CONNECTION_DEFINITIONS).append(">");
         for (ConnectionDefinition cd : connectionDefinitions)
         {
            sb.append(cd);
         }
         sb.append("</").append(XML.ELEMENT_CONNECTION_DEFINITIONS).append(">");
      }

      if (adminObjects != null && !adminObjects.isEmpty())
      {
         sb.append("<").append(XML.ELEMENT_ADMIN_OBJECTS).append(">");
         for (AdminObject ao : adminObjects)
         {
            sb.append(ao);
         }
         sb.append("</").append(XML.ELEMENT_ADMIN_OBJECTS).append(">");
      }

      if (archive != null)
      {
         sb.append("</resource-adapter>");
      }
      else
      {
         sb.append("</ironjacamar>");
      }
      
      return sb.toString();
   }
}
