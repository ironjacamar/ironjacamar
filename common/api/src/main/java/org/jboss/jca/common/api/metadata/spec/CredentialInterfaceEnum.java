/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.api.metadata.spec;

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
 *        jakarta.resource.spi.security.PasswordCredential
 *        org.ietf.jgss.GSSCredential
 *        jakarta.resource.spi.security.GenericCredential
 */
public enum CredentialInterfaceEnum
{
   /**
    * enum instance representing jakarta.resource.spi.security.PasswordCredential
    */
   PasswordCredential("jakarta.resource.spi.security.PasswordCredential"),
   /**
    * enum instance representing org.ietf.jgss.GSSCredentia
    */
   GSSCredential("org.ietf.jgss.GSSCredential"),
   /**
    * enum instance representing jakarta.resource.spi.security.GenericCredential
    */
   GenericCredential("jakarta.resource.spi.security.GenericCredential");

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
    *        jakarta.resource.spi.security.PasswordCredential
    *        org.ietf.jgss.GSSCredential
    *        jakarta.resource.spi.security.GenericCredential
    */
   public static CredentialInterfaceEnum forName(String fullQualifiedName) throws IllegalArgumentException
   {
      if ("jakarta.resource.spi.security.PasswordCredential".equals(fullQualifiedName))
      {
         return PasswordCredential;
      }
      else if ("org.ietf.jgss.GSSCredential".equals(fullQualifiedName))
      {
         return GSSCredential;
      }
      else if ("jakarta.resource.spi.security.GenericCredential".equals(fullQualifiedName))
      {
         return GenericCredential;
      }
      else
      {
         throw new IllegalArgumentException("fullQualifiedName should be one of "
               + "jakarta.resource.spi.security.PasswordCredential;" + " org.ietf.jgss.GSSCredential;"
               + "jakarta.resource.spi.security.GenericCredential");
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
