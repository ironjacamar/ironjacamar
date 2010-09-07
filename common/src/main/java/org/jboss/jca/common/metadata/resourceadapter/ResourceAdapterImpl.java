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

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A ResourceAdapterImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ResourceAdapterImpl implements ResourceAdapter
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7422976850056232511L;
   private final String archive;
   private final TransactionSupportEnum transactionSupport;

   private final HashMap<String, String> configProperties;

   private final ArrayList<AdminObject> adminObjects;

   private final ArrayList<ConnectionDefinition> connectionDefinitions;

   /**
    *
    * Create a new ResourceAdapterImpl.
    *
    * @param archive archive
    * @param transactionSupport transactionSupport
    * @param connectionDefinitions connectionDefinitions
    * @param adminObjects adminObjects
    * @param configProperties configProperties
    */
   public ResourceAdapterImpl(String archive, TransactionSupportEnum transactionSupport,
      List<ConnectionDefinition> connectionDefinitions, List<AdminObject> adminObjects,
      Map<String, String> configProperties)
   {
      super();
      this.archive = archive;
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
}

