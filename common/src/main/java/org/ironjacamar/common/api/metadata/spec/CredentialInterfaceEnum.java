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
package org.ironjacamar.common.api.metadata.spec;

import org.ironjacamar.common.CommonBundle;

import org.jboss.logging.Messages;

/**
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *The credential-interfaceType specifies the
 *        interface that the resource adapter implementation
 *        supports for the representation of the
 *        credentials. This element(s) that use this type,
 *        i.e. credential-interface,  should be used by
 *        application server to find out the Credential
 *        interface it should use as part of the security
 *        contract.
 *        The possible values are:
 *
 *        javax.resource.spi.security.PasswordCredential
 *        org.ietf.jgss.GSSCredential
 *        javax.resource.spi.security.GenericCredential
 */
public enum CredentialInterfaceEnum
{
   /**
    * enum instance representing javax.resource.spi.security.PasswordCredential
    */
   PasswordCredential("javax.resource.spi.security.PasswordCredential"),
   /**
    * enum instance representing org.ietf.jgss.GSSCredentia
    */
   GSSCredential("org.ietf.jgss.GSSCredential"),
   /**
    * enum instance representing javax.resource.spi.security.GenericCredential
    */
   GenericCredential("javax.resource.spi.security.GenericCredential");

   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private final String fullQualifiedName;

   private CredentialInterfaceEnum(String name)
   {
      this.fullQualifiedName = name;
   }

   /**
    *
    * Conveninet method to get enum instance giving full qualified name
    *
    * @param fullQualifiedName the full qualified name of supported Credential interface
    * @return the corresponding enum instance
    * @throws IllegalArgumentException in case the fullQualifiedName isn't one of the supported interfaces:
    *        javax.resource.spi.security.PasswordCredential
    *        org.ietf.jgss.GSSCredential
    *        javax.resource.spi.security.GenericCredential
    */
   public static CredentialInterfaceEnum forName(String fullQualifiedName) throws IllegalArgumentException
   {
      if ("javax.resource.spi.security.PasswordCredential".equals(fullQualifiedName))
      {
         return PasswordCredential;
      }
      else if ("org.ietf.jgss.GSSCredential".equals(fullQualifiedName))
      {
         return GSSCredential;
      }
      else if ("javax.resource.spi.security.GenericCredential".equals(fullQualifiedName))
      {
         return GenericCredential;
      }
      else
      {
         throw new IllegalArgumentException(bundle.fullQualifiedName());
      }
   }

   /**
    * @return fullQualifiedName
    */
   public String getFullQualifiedName()
   {
      return fullQualifiedName;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return fullQualifiedName;
   }
}
