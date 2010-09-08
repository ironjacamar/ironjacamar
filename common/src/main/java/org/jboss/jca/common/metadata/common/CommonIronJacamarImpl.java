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
package org.jboss.jca.common.metadata.common;

import org.jboss.jca.common.api.metadata.common.AdminObject;
import org.jboss.jca.common.api.metadata.common.CommonIronJacamar;
import org.jboss.jca.common.api.metadata.common.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A CommonIronJacamarImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class CommonIronJacamarImpl implements CommonIronJacamar
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -7422976850056232511L;

   /** transactionSupport **/
   protected final TransactionSupportEnum transactionSupport;

   /**
    *  configProperties
    */
   protected final HashMap<String, String> configProperties;

   /**
    *  adminObjects
    */
   protected final ArrayList<AdminObject> adminObjects;

   /**
    *  connectionDefinitions
    */
   protected final ArrayList<ConnectionDefinition> connectionDefinitions;

   /**
    *  beanValidationGroups
    */
   protected final ArrayList<String> beanValidationGroups;

   /** bootstrapContext **/
   protected final String bootstrapContext;

   /**
    * Create a new CommonIronJacamarImpl.
    *
    * @param transactionSupport transactionSupport
    * @param configProperties configProperties
    * @param adminObjects adminObjects
    * @param connectionDefinitions connectionDefinitions
    * @param beanValidationGroups beanValidationGroups
    * @param bootstrapContext bootstrapContext
    */
   protected CommonIronJacamarImpl(TransactionSupportEnum transactionSupport,
      Map<String, String> configProperties, List<AdminObject> adminObjects,
      List<ConnectionDefinition> connectionDefinitions, List<String> beanValidationGroups,
      String bootstrapContext)
   {
      super();
      this.transactionSupport = transactionSupport;
      if (connectionDefinitions != null)
      {
         this.connectionDefinitions = new ArrayList<ConnectionDefinition>(connectionDefinitions.size());
         this.connectionDefinitions.addAll(connectionDefinitions);
      }
      else
      {
         this.connectionDefinitions = new ArrayList<ConnectionDefinition>(0);
      }

      if (adminObjects != null)
      {
         this.adminObjects = new ArrayList<AdminObject>(adminObjects.size());
         this.adminObjects.addAll(adminObjects);
      }
      else
      {
         this.adminObjects = new ArrayList<AdminObject>(0);
      }
      if (configProperties != null)
      {
         this.configProperties = new HashMap<String, String>(configProperties.size());
         this.configProperties.putAll(configProperties);
      }
      else
      {
         this.configProperties = new HashMap<String, String>(0);
      }
      if (beanValidationGroups != null)
      {
         this.beanValidationGroups = new ArrayList<String>(beanValidationGroups.size());
         this.beanValidationGroups.addAll(beanValidationGroups);
      }
      else
      {
         this.beanValidationGroups = new ArrayList<String>(0);
      }
      this.bootstrapContext = bootstrapContext;

   }

   /**
    * Get the transactionSupport.
    *
    * @return the transactionSupport.
    */
   @Override
   public final TransactionSupportEnum getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * Get the connectionFactories.
    *
    * @return the connectionFactories.
    */
   @Override
   public final List<ConnectionDefinition> getConnectionDefinitions()
   {
      return Collections.unmodifiableList(connectionDefinitions);
   }

   /**
    * Get the adminObjects.
    *
    * @return the adminObjects.
    */
   @Override
   public final List<AdminObject> getAdminObjects()
   {
      return Collections.unmodifiableList(adminObjects);
   }

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   @Override
   public Map<String, String> getConfigProperties()
   {
      return Collections.unmodifiableMap(configProperties);
   }

   /**
    * Get the beanValidationGroups.
    *
    * @return the beanValidationGroups.
    */
   @Override
   public final List<String> getBeanValidationGroups()
   {
      return Collections.unmodifiableList(beanValidationGroups);
   }

   /**
    * Get the bootstrapContext.
    *
    * @return the bootstrapContext.
    */
   @Override
   public final String getBootstrapContext()
   {
      return bootstrapContext;
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
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof CommonIronJacamarImpl))
         return false;
      CommonIronJacamarImpl other = (CommonIronJacamarImpl) obj;
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
      return true;
   }

   @Override
   public String toString()
   {
      return "CommonIronJacamarImpl [transactionSupport=" + transactionSupport + ", configProperties=" +
             configProperties + ", adminObjects=" + adminObjects + ", connectionDefinitions=" +
             connectionDefinitions + ", beanValidationGroups=" + beanValidationGroups + ", bootstrapContext=" +
             bootstrapContext + "]";
   }

}
