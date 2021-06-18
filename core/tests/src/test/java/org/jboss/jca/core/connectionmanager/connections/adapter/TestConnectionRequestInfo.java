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
package org.jboss.jca.core.connectionmanager.connections.adapter;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * TestConnectionRequestInfo
 */
public class TestConnectionRequestInfo implements ConnectionRequestInfo
{
   private String failure;

   /**
    * Constructor
    */
   public TestConnectionRequestInfo()
   {
      this.failure = "nowhere";
   }

   /**
    * Constructor
    * @param failure failure
    */
   public TestConnectionRequestInfo(String failure)
   {
      this.failure = failure;
   }

   /**
    * Get failure
    * @return The value
    */
   public String getFailure()
   {
      return failure;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return failure.hashCode();
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object param1)
   {
      if (param1 == this)
         return true;

      if (param1 == null || (!(param1 instanceof TestConnectionRequestInfo)))
         return false;

      TestConnectionRequestInfo other = (TestConnectionRequestInfo)param1;
      return failure.equals(other.failure);
   }
}
