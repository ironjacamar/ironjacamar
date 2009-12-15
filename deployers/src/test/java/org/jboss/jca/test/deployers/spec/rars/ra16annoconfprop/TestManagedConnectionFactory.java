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
package org.jboss.jca.test.deployers.spec.rars.ra16annoconfprop;

import org.jboss.jca.test.deployers.spec.rars.BaseManagedConnectionFactory;

import javax.resource.spi.ConfigProperty;

/**
 * TestManagedConnectionFactory
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class TestManagedConnectionFactory extends BaseManagedConnectionFactory
{
   private static final long serialVersionUID = 1L;
   
   @ConfigProperty(type = String.class, defaultValue = "JCA")
   private String myStringProperty;

   @ConfigProperty
   private String myOtherStringProperty;

   private String myMethodStringProperty;

   /**
    * Constructor
    */
   public TestManagedConnectionFactory()
   {
   }

   /**
    * Set the MyStringProperty value
    * @param myStringProperty The value
    */
   public void setMyStringProperty(String myStringProperty)
   {
      this.myStringProperty = myStringProperty;
   }

   /**
    * Get the MyStringProperty value
    * @return The value
    */
   public String getMyStringProperty()
   {
      return myStringProperty;
   }

   /**
    * Set the MyOtherStringProperty value
    * @param value The value
    */
   public void setMyOtherStringProperty(String value)
   {
      this.myOtherStringProperty = value;
   }

   /**
    * Get the MyOtherStringProperty value
    * @return The value
    */
   public String getMyOtherStringProperty()
   {
      return myOtherStringProperty;
   }

   /**
    * Set the MyMethodStringProperty value
    * @param value The value
    */
   @ConfigProperty
   public void setMyMethodStringProperty(String value)
   {
      this.myMethodStringProperty = value;
   }

   /**
    * Get the MyMethodStringProperty value
    * @return The value
    */
   public String getMyMethodStringProperty()
   {
      return myMethodStringProperty;
   }
}
