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
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.api.metadata.CopyUtil;
import org.ironjacamar.common.api.metadata.MergeUtil;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.MergeableMetadata;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class ConnectionDefinitionImpl extends AbstractMetadata implements ConnectionDefinition
{
   private static final long serialVersionUID = 197264648585424765L;

   private XsdString managedconnectionfactoryClass;

   private List<ConfigProperty> configProperties;

   private XsdString connectionfactoryInterface;

   private XsdString connectionfactoryImplClass;

   private XsdString connectionInterface;

   private XsdString connectionImplClass;

   private String id;

   /**
    * Constructor
    * @param managedconnectionfactoryClass full qualified name of the class
    * @param configProperty List of configproperties
    * @param connectionfactoryInterface full qualified name of the factory interface
    * @param connectionfactoryImplClass full qualified name of the factory implementation class
    * @param connectionInterface full qualified name of the connection interface
    * @param connectionImplClass full qualified name of the connection implementation class
    * @param id XML ID
    */
   public ConnectionDefinitionImpl(XsdString managedconnectionfactoryClass,
                                   List<ConfigProperty> configProperty, XsdString connectionfactoryInterface,
                                   XsdString connectionfactoryImplClass, XsdString connectionInterface,
                                   XsdString connectionImplClass, String id)
   {
      super(null);
      if (configProperty != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(configProperty);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
      this.managedconnectionfactoryClass = managedconnectionfactoryClass;
      if (!XsdString.isNull(this.managedconnectionfactoryClass))
         this.managedconnectionfactoryClass.
            setTag(XML.ELEMENT_MANAGEDCONNECTIONFACTORY_CLASS);
      this.connectionfactoryInterface = connectionfactoryInterface;
      if (!XsdString.isNull(this.connectionfactoryInterface))
         this.connectionfactoryInterface.setTag(XML.ELEMENT_CONNECTIONFACTORY_INTERFACE);
      this.connectionfactoryImplClass = connectionfactoryImplClass;
      if (!XsdString.isNull(this.connectionfactoryImplClass))
         this.connectionfactoryImplClass.setTag(XML.ELEMENT_CONNECTIONFACTORY_IMPL_CLASS);
      this.connectionInterface = connectionInterface;
      if (!XsdString.isNull(this.connectionInterface))
         this.connectionInterface.setTag(XML.ELEMENT_CONNECTION_INTERFACE);
      this.connectionImplClass = connectionImplClass;
      if (!XsdString.isNull(this.connectionImplClass))
         this.connectionImplClass.setTag(XML.ELEMENT_CONNECTION_IMPL_CLASS);
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getManagedConnectionFactoryClass()
   {
      return managedconnectionfactoryClass;
   }

   /**
    * {@inheritDoc}
    */
   public List<ConfigProperty> getConfigProperties()
   {
      return Collections.unmodifiableList(configProperties);
   }

   /**
    * Force configProperties with new content.
    * This method is thread safe
    *
    * @param newContents the list of new properties
    */
   public synchronized void forceConfigProperties(List<ConfigProperty> newContents)
   {
      if (newContents != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(newContents);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getConnectionFactoryInterface()
   {
      return connectionfactoryInterface;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getConnectionFactoryImplClass()
   {
      return connectionfactoryImplClass;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getConnectionInterface()
   {
      return connectionInterface;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getConnectionImplClass()
   {
      return connectionImplClass;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionDefinition merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof ConnectionDefinitionImpl)
      {
         ConnectionDefinitionImpl input = (ConnectionDefinitionImpl) jmd;

         List<ConfigProperty> newConfigProperty = MergeUtil.mergeConfigList(this.configProperties,
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

   /**
    * {@inheritDoc}
    */
   public ConnectionDefinition copy()
   {
      return new ConnectionDefinitionImpl(CopyUtil.clone(managedconnectionfactoryClass),
                                          CopyUtil.cloneList(configProperties),
                                          CopyUtil.clone(connectionfactoryInterface),
                                          CopyUtil.clone(connectionfactoryImplClass),
                                          CopyUtil.clone(connectionInterface),
                                          CopyUtil.clone(connectionImplClass), CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
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
    */
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
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);
      sb.append("<connection-definition");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
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
}
