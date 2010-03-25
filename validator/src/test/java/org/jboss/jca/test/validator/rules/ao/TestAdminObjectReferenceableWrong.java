/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.validator.rules.ao;

import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;

/**
 * An AdminObject with wrong property
 * @author Stefano Maestri mailto:stefano.maestri@javalinux.it
 *
 */
public class TestAdminObjectReferenceableWrong implements ResourceAdapterAssociation, Serializable
{
   /**
    */
   private static final long serialVersionUID = 5554958948788007459L;

   @ConfigProperty(type = String.class, defaultValue = "InAO")
   private String myStringProperty;


   /**
    *
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
    * @see javax.resource.spi.ResourceAdapterAssociation#getResourceAdapter()
    */
   @Override
   public ResourceAdapter getResourceAdapter()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    *
    * @see javax.resource.spi.ResourceAdapterAssociation#setResourceAdapter(javax.resource.spi.ResourceAdapter)
    */
   @Override
   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
   }
}
