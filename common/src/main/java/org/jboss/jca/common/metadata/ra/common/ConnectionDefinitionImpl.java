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

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.MergeUtil;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition.Tag;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ConnectionDefinitionImpl implements ConnectionDefinition
{
   /**
    */
   private static final long serialVersionUID = 197264648585424765L;

   private final XsdString managedconnectionfactoryClass;

   private ArrayList<ConfigProperty> configProperties;

   private final XsdString connectionfactoryInterface;

   private final XsdString connectionfactoryImplClass;

   private final XsdString connectionInterface;

   private final XsdString connectionImplClass;

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
   public ConnectionDefinitionImpl(XsdString managedconnectionfactoryClass,
      List<? extends ConfigProperty> configProperty, XsdString connectionfactoryInterface,
      XsdString connectionfactoryImplClass, XsdString connectionInterface, XsdString connectionImplClass, String id)
   {
      super();
      if (configProperty != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(configProperty.size());
         this.configProperties.addAll(configProperty);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
      this.managedconnectionfactoryClass = managedconnectionfactoryClass;
      if (!XsdString.isNull(this.managedconnectionfactoryClass))
         this.managedconnectionfactoryClass.setTag(Tag.MANAGEDCONNECTIONFACTORY_CLASS.toString());
      this.connectionfactoryInterface = connectionfactoryInterface;
      if (!XsdString.isNull(this.connectionfactoryInterface))
         this.connectionfactoryInterface.setTag(Tag.CONNECTIONFACTORY_INTERFACE.toString());
      this.connectionfactoryImplClass = connectionfactoryImplClass;
      if (!XsdString.isNull(this.connectionfactoryImplClass))
         this.connectionfactoryImplClass.setTag(Tag.CONNECTIONFACTORY_IMPL_CLASS.toString());
      this.connectionInterface = connectionInterface;
      if (!XsdString.isNull(this.connectionInterface))
         this.connectionInterface.setTag(Tag.CONNECTION_INTERFACE.toString());
      this.connectionImplClass = connectionImplClass;
      if (!XsdString.isNull(this.connectionImplClass))
         this.connectionImplClass.setTag(Tag.CONNECTION_IMPL_CLASS.toString());
      this.id = id;
   }

   /**
    * @return managedconnectionfactoryClass
    */
   @Override
   public XsdString getManagedConnectionFactoryClass()
   {
      return managedconnectionfactoryClass;
   }

   /**
    * @return configProperty
    */
   @Override
   public synchronized List<? extends ConfigProperty> getConfigProperties()
   {
      return configProperties == null ? null : Collections.unmodifiableList(configProperties);
   }

   /**
   *
   * force configProperties with new content.
   * This method is thread safe
   *
   * @param newContents the list of new properties
   */
   public synchronized void forceNewConfigPropertiesContent(List<? extends ConfigProperty> newContents)
   {
      if (newContents != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(newContents.size());
         this.configProperties.addAll(newContents);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
   }

   /**
    * @return connectionfactoryInterface
    */
   @Override
   public XsdString getConnectionFactoryInterface()
   {
      return connectionfactoryInterface;
   }

   /**
    * @return connectionfactoryImplClass
    */
   @Override
   public XsdString getConnectionFactoryImplClass()
   {
      return connectionfactoryImplClass;
   }

   /**
    * @return connectionInterface
    */
   @Override
   public XsdString getConnectionInterface()
   {
      return connectionInterface;
   }

   /**
    * @return connectionImplClass
    */
   @Override
   public XsdString getConnectionImplClass()
   {
      return connectionImplClass;
   }

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
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((connectionImplClass == null) ? 0 : connectionImplClass.hashCode());
      result = prime * result + ((connectionInterface == null) ? 0 : connectionInterface.hashCode());
      result = prime * result + ((connectionfactoryImplClass == null) ? 0 : connectionfactoryImplClass.hashCode());
      result = prime * result + ((connectionfactoryInterface == null) ? 0 : connectionfactoryInterface.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result +
               ((managedconnectionfactoryClass == null) ? 0 : managedconnectionfactoryClass.hashCode());
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
      if (!(obj instanceof ConnectionDefinitionImpl))
      {
         return false;
      }
      ConnectionDefinitionImpl other = (ConnectionDefinitionImpl) obj;
      if (configProperties == null)
      {
         if (other.configProperties != null)
         {
            return false;
         }
      }
      else if (!configProperties.equals(other.configProperties))
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
      StringBuilder sb = new StringBuilder(1024);
      sb.append("<connection-definition");
      if (id != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      sb.append(managedconnectionfactoryClass);

      if (configProperties != null)
      {
         for (ConfigProperty cp : configProperties)
         {
            sb.append(cp);
         }
      }

      sb.append(connectionfactoryInterface);

      sb.append(connectionfactoryImplClass);

      sb.append(connectionInterface);

      sb.append(connectionImplClass);

      sb.append("</connection-definition>");

      return sb.toString();
   }

   @Override
   public ConnectionDefinition merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof ConnectionDefinitionImpl)
      {
         ConnectionDefinitionImpl input = (ConnectionDefinitionImpl) jmd;

         List<? extends ConfigProperty> newConfigProperty = MergeUtil.mergeConfigList(this.configProperties,
            input.configProperties);
         XsdString newManagedconnectionfactoryClass = this.managedconnectionfactoryClass == null
            ? input.managedconnectionfactoryClass
            : this.managedconnectionfactoryClass;
         XsdString newConnectionInterface = this.connectionInterface == null
            ? input.connectionInterface
            : this.connectionInterface;
         XsdString newConnectionfactoryImplClass = this.connectionfactoryImplClass == null
            ? input.connectionfactoryImplClass
            : this.connectionfactoryImplClass;
         XsdString newConnectionfactoryInterface = this.connectionfactoryInterface == null
            ? input.connectionfactoryInterface
            : this.connectionfactoryInterface;
         String newId = this.id == null ? input.id : this.id;
         XsdString newConnectionImplClass = this.connectionImplClass == null
            ? input.connectionImplClass
            : this.connectionImplClass;
         return new ConnectionDefinitionImpl(newManagedconnectionfactoryClass, newConfigProperty,
                                             newConnectionfactoryInterface, newConnectionfactoryImplClass,
                                             newConnectionInterface, newConnectionImplClass, newId);
      }
      else
      {
         return this;
      }
   }

   @Override
   public CopyableMetaData copy()
   {
      return new ConnectionDefinitionImpl(CopyUtil.clone(managedconnectionfactoryClass),
                                          CopyUtil.cloneList(configProperties),
                                          CopyUtil.clone(connectionfactoryInterface),
                                          CopyUtil.clone(connectionfactoryImplClass),
                                          CopyUtil.clone(connectionInterface),
                                          CopyUtil.clone(connectionImplClass), CopyUtil.cloneString(id));
   }
}
