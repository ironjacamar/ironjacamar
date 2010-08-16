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
package org.jboss.jca.common.metadata.resourceadpater;

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.NoTxConnectionFactory;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

   private final ArrayList<NoTxConnectionFactory> connectionFactories;

   private final ArrayList<AdminObject> adminObjects;

   /**
    * Create a new ResourceAdapterImpl.
    *
    * @param archive archive
    * @param transactionSupport transactionSupport
    * @param connectionFactories connectionFactories
    * @param adminObjects adminObjects
    */
   public ResourceAdapterImpl(String archive, TransactionSupportEnum transactionSupport,
         List<? extends NoTxConnectionFactory> connectionFactories, List<AdminObject> adminObjects)
   {
      super();
      this.archive = archive;
      this.transactionSupport = transactionSupport;
      if (connectionFactories != null)
      {
         this.connectionFactories = new ArrayList<NoTxConnectionFactory>(connectionFactories.size());
         this.connectionFactories.addAll(connectionFactories);
      }
      else
      {
         this.connectionFactories = new ArrayList<NoTxConnectionFactory>(0);
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
   public final List<? extends NoTxConnectionFactory> getConnectionFactories()
   {
      return Collections.unmodifiableList(connectionFactories);
   }

   /**
    * Get the adminObjects.
    *
    * @return the adminObjects.
    */
   @Override
   public final List<AdminObject> getAdminobjects()
   {
      return Collections.unmodifiableList(adminObjects);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((adminObjects == null) ? 0 : adminObjects.hashCode());
      result = prime * result + ((archive == null) ? 0 : archive.hashCode());
      result = prime * result + ((connectionFactories == null) ? 0 : connectionFactories.hashCode());
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
      if (!(obj instanceof ResourceAdapterImpl))
         return false;
      ResourceAdapterImpl other = (ResourceAdapterImpl) obj;
      if (adminObjects == null)
      {
         if (other.adminObjects != null)
            return false;
      }
      else if (!adminObjects.equals(other.adminObjects))
         return false;
      if (archive == null)
      {
         if (other.archive != null)
            return false;
      }
      else if (!archive.equals(other.archive))
         return false;
      if (connectionFactories == null)
      {
         if (other.connectionFactories != null)
            return false;
      }
      else if (!connectionFactories.equals(other.connectionFactories))
         return false;
      if (transactionSupport != other.transactionSupport)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "ResourceAdapterImpl [archive=" + archive + ", transactionSupport=" + transactionSupport
            + ", connectionFactories=" + connectionFactories + ", adminObjects=" + adminObjects + "]";
   }
}

