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
package org.jboss.jca.common.metadata.ra.common;


import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ConnectionDefinition implements IdDecoratedMetadata
{
   /**
    */
   private static final long serialVersionUID = 197264648585424765L;

   private final String managedconnectionfactoryClass;

   private final List<? extends ConfigProperty> configProperty;

   private final String connectionfactoryInterface;

   private final String connectionfactoryImplClass;

   private final String connectionInterface;

   private final String connectionImplClass;

   private final String id;

   /**
    * @param managedconnectionfactoryClass full qualified name of the class
    * @param configProperty List of configproperties
    * @param connectionfactoryInterface full qualified name of the factory interface
    * @param connectionfactoryImplClass full qualified name of the factory implementation class
    * @param connectionInterface full qualified name of the connection interface
    * @param connectionImplClass full qualified name of the connection implementation class
    * @param id XML ID
    */
   public ConnectionDefinition(String managedconnectionfactoryClass, List<? extends ConfigProperty> configProperty,
         String connectionfactoryInterface, String connectionfactoryImplClass, String connectionInterface,
         String connectionImplClass, String id)
   {
      super();
      this.managedconnectionfactoryClass = managedconnectionfactoryClass;
      this.configProperty = configProperty;
      this.connectionfactoryInterface = connectionfactoryInterface;
      this.connectionfactoryImplClass = connectionfactoryImplClass;
      this.connectionInterface = connectionInterface;
      this.connectionImplClass = connectionImplClass;
      this.id = id;
   }

   /**
    * @return managedconnectionfactoryClass
    */
   public String getManagedconnectionfactoryClass()
   {
      return managedconnectionfactoryClass;
   }

   /**
    * @return configProperty
    */
   public List<? extends ConfigProperty> getConfigProperty()
   {
      return Collections.unmodifiableList(configProperty);
   }

   /**
    * @return connectionfactoryInterface
    */
   public String getConnectionfactoryInterface()
   {
      return connectionfactoryInterface;
   }

   /**
    * @return connectionfactoryImplClass
    */
   public String getConnectionfactoryImplClass()
   {
      return connectionfactoryImplClass;
   }

   /**
    * @return connectionInterface
    */
   public String getConnectionInterface()
   {
      return connectionInterface;
   }

   /**
    * @return connectionImplClass
    */
   public String getConnectionImplClass()
   {
      return connectionImplClass;
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
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      result = prime * result + ((connectionImplClass == null) ? 0 : connectionImplClass.hashCode());
      result = prime * result + ((connectionInterface == null) ? 0 : connectionInterface.hashCode());
      result = prime * result + ((connectionfactoryImplClass == null) ? 0 : connectionfactoryImplClass.hashCode());
      result = prime * result + ((connectionfactoryInterface == null) ? 0 : connectionfactoryInterface.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result
            + ((managedconnectionfactoryClass == null) ? 0 : managedconnectionfactoryClass.hashCode());
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
      if (!(obj instanceof ConnectionDefinition))
      {
         return false;
      }
      ConnectionDefinition other = (ConnectionDefinition) obj;
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
      if (connectionImplClass == null)
      {
         if (other.connectionImplClass != null)
         {
            return false;
         }
      }
      else if (!connectionImplClass.equals(other.connectionImplClass))
      {
         return false;
      }
      if (connectionInterface == null)
      {
         if (other.connectionInterface != null)
         {
            return false;
         }
      }
      else if (!connectionInterface.equals(other.connectionInterface))
      {
         return false;
      }
      if (connectionfactoryImplClass == null)
      {
         if (other.connectionfactoryImplClass != null)
         {
            return false;
         }
      }
      else if (!connectionfactoryImplClass.equals(other.connectionfactoryImplClass))
      {
         return false;
      }
      if (connectionfactoryInterface == null)
      {
         if (other.connectionfactoryInterface != null)
         {
            return false;
         }
      }
      else if (!connectionfactoryInterface.equals(other.connectionfactoryInterface))
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
      if (managedconnectionfactoryClass == null)
      {
         if (other.managedconnectionfactoryClass != null)
         {
            return false;
         }
      }
      else if (!managedconnectionfactoryClass.equals(other.managedconnectionfactoryClass))
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
      return "ConnectionDefinition [managedconnectionfactoryClass=" + managedconnectionfactoryClass
            + ", configProperty=" + configProperty + ", connectionfactoryInterface=" + connectionfactoryInterface
            + ", connectionfactoryImplClass=" + connectionfactoryImplClass + ", connectionInterface="
            + connectionInterface + ", connectionImplClass=" + connectionImplClass + ", id=" + id + "]";
   }
}
