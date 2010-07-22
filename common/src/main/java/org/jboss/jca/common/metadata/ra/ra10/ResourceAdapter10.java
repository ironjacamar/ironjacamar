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

import org.jboss.jca.common.metadata.ra.common.AuthenticationMechanism;
import org.jboss.jca.common.metadata.ra.common.ConfigProperty;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter;
import org.jboss.jca.common.metadata.ra.common.SecurityPermission;
import org.jboss.jca.common.metadata.ra.common.TransactionSupportEnum;
import org.jboss.jca.common.metadata.ra.common.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ResourceAdapter10 implements ResourceAdapter
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 6841574517370539456L;

   private final XsdString managedConnectionFactoryClass;

   private final XsdString connectionFactoryInterface;

   private final XsdString connectionFactoryImplClass;

   private final XsdString connectionInterface;

   private final XsdString connectionImplClass;

   private final TransactionSupportEnum transactionSupport;

   private final ArrayList<AuthenticationMechanism> authenticationMechanism;

   private final ArrayList<ConfigProperty> configProperties;

   private final Boolean reauthenticationSupport;

   private final ArrayList<SecurityPermission> securityPermission;

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
    * @param configProperties list of configs
    * @param reauthenticationSupport true if reautentication is supported
    * @param securityPermission what security permissions are supported
    * @param id the id attribute in xml file
    */
   public ResourceAdapter10(XsdString managedConnectionFactoryClass, XsdString connectionFactoryInterface,
         XsdString connectionFactoryImplClass, XsdString connectionInterface, XsdString connectionImplClass,
         TransactionSupportEnum transactionSupport, ArrayList<AuthenticationMechanism> authenticationMechanism,
         ArrayList<ConfigProperty> configProperties, Boolean reauthenticationSupport,
         ArrayList<SecurityPermission> securityPermission, String id)
   {
      super();
      this.managedConnectionFactoryClass = managedConnectionFactoryClass;
      this.connectionFactoryInterface = connectionFactoryInterface;
      this.connectionFactoryImplClass = connectionFactoryImplClass;
      this.connectionInterface = connectionInterface;
      this.connectionImplClass = connectionImplClass;
      this.transactionSupport = transactionSupport;
      this.authenticationMechanism = authenticationMechanism;
      this.configProperties = configProperties;
      this.reauthenticationSupport = reauthenticationSupport;
      this.securityPermission = securityPermission;
      this.id = id;
   }

   /**
    * Get the managedConnectionFactoryClass.
    *
    * @return the managedConnectionFactoryClass.
    */
   public XsdString getManagedConnectionFactoryClass()
   {
      return managedConnectionFactoryClass;
   }

   /**
    * Get the connectionFactoryInterface.
    *
    * @return the connectionFactoryInterface.
    */
   public XsdString getConnectionFactoryInterface()
   {
      return connectionFactoryInterface;
   }

   /**
    * Get the connectionFactoryImplClass.
    *
    * @return the connectionFactoryImplClass.
    */
   public XsdString getConnectionFactoryImplClass()
   {
      return connectionFactoryImplClass;
   }

   /**
    * Get the connectionInterface.
    *
    * @return the connectionInterface.
    */
   public XsdString getConnectionInterface()
   {
      return connectionInterface;
   }

   /**
    * Get the connectionImplClass.
    *
    * @return the connectionImplClass.
    */
   public XsdString getConnectionImplClass()
   {
      return connectionImplClass;
   }

   /**
    * Get the transactionSupport.
    *
    * @return the transactionSupport.
    */
   public TransactionSupportEnum getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * Get the authenticationMechanism.
    *
    * @return the authenticationMechanism.
    */
   public List<AuthenticationMechanism> getAuthenticationMechanism()
   {
      return Collections.unmodifiableList(authenticationMechanism);
   }

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   public List<ConfigProperty> getConfigProperties()
   {
      return Collections.unmodifiableList(configProperties);
   }

   /**
    * Get the reauthenticationSupport.
    *
    * @return the reauthenticationSupport.
    */
   public Boolean getReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   /**
    * Get the securityPermission.
    *
    * @return the securityPermission.
    */
   public List<SecurityPermission> getSecurityPermission()
   {
      return Collections.unmodifiableList(securityPermission);
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
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
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
      if (!(obj instanceof ResourceAdapter10))
         return false;
      ResourceAdapter10 other = (ResourceAdapter10) obj;
      if (authenticationMechanism == null)
      {
         if (other.authenticationMechanism != null)
            return false;
      }
      else if (!authenticationMechanism.equals(other.authenticationMechanism))
         return false;
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
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
            + authenticationMechanism + ", configProperties=" + configProperties + ", reauthenticationSupport="
            + reauthenticationSupport + ", securityPermission=" + securityPermission + ", id=" + id + "]";
   }

   /**
   *
   * A Tag.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * managedconnectionfactory-class TAG
       */
      MANAGED_CONNECTIONFACTORY_CLASS("managedconnectionfactory-class"),

      /**
       * config-property TAG
       */
      CONFIG_PROPERTY("config-property"),

      /**
       * connectionfactory-interface TAG
       */
      CONNECTIONFACTORY_INTERFACE("connectionfactory-interface"),

      /**
       * connectionfactory-impl-class TAG
       */
      CONNECTIONFACTORY_IMPL_CLASS("connectionfactory-impl-class"),

      /**
       * connection-interface TAG
       */
      CONNECTION_INTERFACE("connection-interface"),

      /**
       * connection-impl-class TAG
       */
      CONNECTION_IMPL_CLASS("connection-impl-class"),

      /**
       * transaction-support TAG
       */
      TRANSACTION_SUPPORT("transaction-support"),

      /**
       * authentication-mechanism TAG
       */
      AUTHENTICATION_MECHANISM("authentication-mechanism"),
      /**
       * security-permission TAG
       */
      SECURITY_PERMISSION("security-permission"),
      /**
       * reauthentication-support TAG
       */
      REAUTHENTICATION_SUPPORT("reauthentication-support");


      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }

   /**
    *
    * A Attribute.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public enum Attribute
   {

      /** id attribute
       *
       */
      ID("id");

      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Attribute(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

   }
}
