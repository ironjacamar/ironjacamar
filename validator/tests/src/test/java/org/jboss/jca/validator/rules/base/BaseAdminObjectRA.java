/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.validator.rules.base;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;

/**
 * A bAse correct AdminObject
 * @author Stefano Maestri mailto:stefano.maestri@javalinux.it
 *
 */

public class BaseAdminObjectRA implements ResourceAdapterAssociation, Referenceable, Serializable
{
   /**
    */
   private static final long serialVersionUID = 1L;
   @ConfigProperty(type = String.class, defaultValue = "InAO")
   private String myStringProperty;

   /**
    * @param myStringProperty the myStringProperty to set
    */
   public void setMyStringProperty(String myStringProperty)
   {
      this.myStringProperty = myStringProperty;
   }

   /**
    * @return the myStringProperty
    */
   public String getMyStringProperty()
   {
      return myStringProperty;
   }

   /**
    * {@inheritDoc}
    *
    * @see jakarta.resource.spi.ResourceAdapterAssociation#getResourceAdapter()
    */
   @Override
   public ResourceAdapter getResourceAdapter()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    *
    * @see jakarta.resource.spi.ResourceAdapterAssociation#setResourceAdapter(jakarta.resource.spi.ResourceAdapter)
    */
   @Override
   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    *
    * @see jakarta.resource.Referenceable#setReference(javax.naming.Reference)
    */
   @Override
   public void setReference(Reference reference)
   {
   }

   /**
    * {@inheritDoc}
    *
    * @see javax.naming.Referenceable#getReference()
    */
   @Override
   public Reference getReference() throws NamingException
   {
      return null;
   }
}
