/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.rars.test;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;

import javax.resource.Referenceable;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;

/**
 * TestAdminObjectImpl
 */
public class TestAdminObjectImpl implements TestAdminObject,
                                            ResourceAdapterAssociation,
                                            Referenceable,
                                            Serializable
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** The resource adapter */
   private ResourceAdapter ra;

   /** Reference */
   private Reference reference;

   /**
    * Default constructor
    */
   public TestAdminObjectImpl()
   {

   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }

   /**
    * {@inheritDoc}
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      this.ra = ra;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Reference getReference() throws NamingException
   {
      return reference;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setReference(Reference reference)
   {
      this.reference = reference;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;
      if (other == this)
         return true;
      if (!(other instanceof TestAdminObjectImpl))
         return false;
      boolean result = true;
      return result;
   }
}
