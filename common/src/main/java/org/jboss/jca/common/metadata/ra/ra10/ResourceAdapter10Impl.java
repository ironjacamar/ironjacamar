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
package org.jboss.jca.common.metadata.ra.ra10;

import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.api.metadata.ra.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.validator.ValidateException;

import java.util.List;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ResourceAdapter10Impl implements ResourceAdapter10
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 6841574517370539456L;

   private final XsdString managedConnectionFactoryClass;

   private final XsdString connectionFactoryInterface;

   private final XsdString connectionFactoryImplClass;

   private final XsdString connectionInterface;

   private final XsdString connectionImplClass;

   private final TransactionSupportEnum transactionSupport;

   private final List<AuthenticationMechanism> authenticationMechanism;

   private final List<ConfigProperty> configProperty;

   private final Boolean reauthenticationSupport;

   private final List<SecurityPermission> securityPermission;

   private final String id;

   /**
    *
    * Create a new ResourceAdapter10.
    *
    * @param managedConnectionFactoryClass class name
    * @param connectionFactoryInterface interface name
    * @param connectionFactoryImplClass class name
    * @param connectionInterface interface name
    * @param connectionImplClass class name
    * @param transactionSupport how transactions are supported
    * @param authenticationMechanism how authentication is performed
    * @param configProperty list of configs
    * @param reauthenticationSupport true if reautentication is supported
    * @param securityPermission what security permissions are supported
    * @param id the id attribute in xml file
    */
   public ResourceAdapter10Impl(XsdString managedConnectionFactoryClass, XsdString connectionFactoryInterface,
         XsdString connectionFactoryImplClass, XsdString connectionInterface, XsdString connectionImplClass,
         TransactionSupportEnum transactionSupport, List<AuthenticationMechanism> authenticationMechanism,
         List<ConfigProperty> configProperty, Boolean reauthenticationSupport,
         List<SecurityPermission> securityPermission, String id)
   {
      super();
      this.managedConnectionFactoryClass = managedConnectionFactoryClass;
      this.connectionFactoryInterface = connectionFactoryInterface;
      this.connectionFactoryImplClass = connectionFactoryImplClass;
      this.connectionInterface = connectionInterface;
      this.connectionImplClass = connectionImplClass;
      this.transactionSupport = transactionSupport;
      this.authenticationMechanism = authenticationMechanism;
      this.configProperty = configProperty;
      this.reauthenticationSupport = reauthenticationSupport;
      this.securityPermission = securityPermission;
      this.id = id;
   }

   /**
    * Get the managedConnectionFactoryClass.
    *
    * @return the managedConnectionFactoryClass.
    */
   @Override
   public XsdString getManagedConnectionFactoryClass()
   {
      return managedConnectionFactoryClass;
   }

   /**
    * Get the connectionFactoryInterface.
    *
    * @return the connectionFactoryInterface.
    */
   @Override
   public XsdString getConnectionFactoryInterface()
   {
      return connectionFactoryInterface;
   }

   /**
    * Get the connectionFactoryImplClass.
    *
    * @return the connectionFactoryImplClass.
    */
   @Override
   public XsdString getConnectionFactoryImplClass()
   {
      return connectionFactoryImplClass;
   }

   /**
    * Get the connectionInterface.
    *
    * @return the connectionInterface.
    */
   @Override
   public XsdString getConnectionInterface()
   {
      return connectionInterface;
   }

   /**
    * Get the connectionImplClass.
    *
    * @return the connectionImplClass.
    */
   @Override
   public XsdString getConnectionImplClass()
   {
      return connectionImplClass;
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
    * Get the authenticationMechanism.
    *
    * @return the authenticationMechanism.
    */
   @Override
   public List<AuthenticationMechanism> getAuthenticationMechanisms()
   {
      return authenticationMechanism == null ? null : Collections.unmodifiableList(authenticationMechanism);
   }

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   @Override
   public List<ConfigProperty> getConfigProperties()
   {
      return configProperty;
   }

   /**
    * Get the reauthenticationSupport.
    *
    * @return the reauthenticationSupport.
    */
   @Override
   public Boolean getReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   /**
    * Get the securityPermission.
    *
    * @return the securityPermission.
    */
   @Override
   public List<SecurityPermission> getSecurityPermissions()
   {
      return securityPermission == null ? null : Collections.unmodifiableList(securityPermission);
   }

   /**
    * Get the id.
    *
    * @return the id.
    */
   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((authenticationMechanism == null) ? 0 : authenticationMechanism.hashCode());
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      result = prime * result + ((connectionFactoryImplClass == null) ? 0 : connectionFactoryImplClass.hashCode());
      result = prime * result + ((connectionFactoryInterface == null) ? 0 : connectionFactoryInterface.hashCode());
      result = prime * result + ((connectionImplClass == null) ? 0 : connectionImplClass.hashCode());
      result = prime * result + ((connectionInterface == null) ? 0 : connectionInterface.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result
            + ((managedConnectionFactoryClass == null) ? 0 : managedConnectionFactoryClass.hashCode());
      result = prime * result + ((reauthenticationSupport == null) ? 0 : reauthenticationSupport.hashCode());
      result = prime * result + ((securityPermission == null) ? 0 : securityPermission.hashCode());
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
      if (!(obj instanceof ResourceAdapter10Impl))
         return false;
      ResourceAdapter10Impl other = (ResourceAdapter10Impl) obj;
      if (authenticationMechanism == null)
      {
         if (other.authenticationMechanism != null)
            return false;
      }
      else if (!authenticationMechanism.equals(other.authenticationMechanism))
         return false;
      if (configProperty == null)
      {
         if (other.configProperty != null)
            return false;
      }
      else if (!configProperty.equals(other.configProperty))
         return false;
      if (connectionFactoryImplClass == null)
      {
         if (other.connectionFactoryImplClass != null)
            return false;
      }
      else if (!connectionFactoryImplClass.equals(other.connectionFactoryImplClass))
         return false;
      if (connectionFactoryInterface == null)
      {
         if (other.connectionFactoryInterface != null)
            return false;
      }
      else if (!connectionFactoryInterface.equals(other.connectionFactoryInterface))
         return false;
      if (connectionImplClass == null)
      {
         if (other.connectionImplClass != null)
            return false;
      }
      else if (!connectionImplClass.equals(other.connectionImplClass))
         return false;
      if (connectionInterface == null)
      {
         if (other.connectionInterface != null)
            return false;
      }
      else if (!connectionInterface.equals(other.connectionInterface))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (managedConnectionFactoryClass == null)
      {
         if (other.managedConnectionFactoryClass != null)
            return false;
      }
      else if (!managedConnectionFactoryClass.equals(other.managedConnectionFactoryClass))
         return false;
      if (reauthenticationSupport == null)
      {
         if (other.reauthenticationSupport != null)
            return false;
      }
      else if (!reauthenticationSupport.equals(other.reauthenticationSupport))
         return false;
      if (securityPermission == null)
      {
         if (other.securityPermission != null)
            return false;
      }
      else if (!securityPermission.equals(other.securityPermission))
         return false;
      if (transactionSupport != other.transactionSupport)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "ResourceAdapter10 [managedConnectionFactoryClass=" + managedConnectionFactoryClass
            + ", connectionFactoryInterface=" + connectionFactoryInterface + ", connectionFactoryImplClass="
            + connectionFactoryImplClass + ", connectionInterface=" + connectionInterface + ", connectionImplClass="
            + connectionImplClass + ", transactionSupport=" + transactionSupport + ", authenticationMechanism="
            + authenticationMechanism + ", configProperties=" + configProperty + ", reauthenticationSupport="
            + reauthenticationSupport + ", securityPermission=" + securityPermission + ", id=" + id + "]";
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.getManagedConnectionFactoryClass() == null || this.getManagedConnectionFactoryClass().getValue() == null
            || this.getManagedConnectionFactoryClass().getValue().trim().equals(""))
      {

         throw new ValidateException("ManagedConnectionFactoryClass should be defined");
      }

   }

}
