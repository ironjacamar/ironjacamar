/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.api.management;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a connector instance
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a> 
 */
public class Connector
{
   /** The unique id */
   private String uniqueId;

   /** The resource adapter instance */
   private ResourceAdapter resourceAdapter;

   /** The connection factories */
   private List<ConnectionFactory> connectionFactories;

   /** The admin objects */
   private List<AdminObject> adminObjects;

   /** The connection managers */
   private List<ConnectionManager> connectionManagers;


   /**
    * Constructor
    * @param uniqueId The unique id
    */
   public Connector(String uniqueId)
   {
      this.uniqueId = uniqueId;
      this.resourceAdapter = null;
      this.connectionFactories = null;
      this.adminObjects = null;
      this.connectionManagers = null;
   }

   /**
    * Get the unique id
    * @return The value
    */
   public String getUniqueId()
   {
      return uniqueId;
   }

   /**
    * Get the resource adapter
    * @return The value
    */
   public ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * Set the resource adapter
    * @param ra The value
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      this.resourceAdapter = ra;
   }

   /**
    * Get the list of connection factories
    * @return The value
    */
   public List<ConnectionFactory> getConnectionFactories()
   {
      if (connectionFactories == null)
         connectionFactories = new ArrayList<ConnectionFactory>(1);

      return connectionFactories;
   }

   /**
    * Get the list of admin objects
    * @return The value
    */
   public List<AdminObject> getAdminObjects()
   {
      if (adminObjects == null)
         adminObjects = new ArrayList<AdminObject>(1);

      return adminObjects;
   }

   /**
    * Get the list of connection managers
    * @return The value
    */
   public List<ConnectionManager> getConnectionManagers()
   {
      if (connectionManagers == null)
         connectionManagers = new ArrayList<ConnectionManager>(1);

      return connectionManagers;
   }


   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("Connector@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[uniqueId=").append(uniqueId);
      sb.append(" resourceAdapter=").append(resourceAdapter);
      sb.append(" connectionFactories=").append(connectionFactories);
      sb.append(" adminObjects=").append(adminObjects);
      sb.append(" connectionManagers=").append(connectionManagers);
      sb.append("]");

      return sb.toString();
   }
}
