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

import org.jboss.jca.core.spi.statistics.Statistics;
import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a resource adapter instance
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapter
{
   /** The object instance */
   private WeakReference<javax.resource.spi.ResourceAdapter> instance;

   /** The config property's */
   private List<ConfigProperty> configProperties;

   /**
    * Constructor
    * @param ra The resource adapter instance
    */
   public ResourceAdapter(javax.resource.spi.ResourceAdapter ra)
   {
      this.instance = new WeakReference<javax.resource.spi.ResourceAdapter>(ra);
      this.configProperties = null;
   }

   /**
    * Get the resource adapter instance.
    * 
    * Note, that the value may be <code>null</code> if the resource adapter was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public javax.resource.spi.ResourceAdapter getResourceAdapter()
   {
      return instance.get();
   }

   /**
    * Get the list of config property's
    * @return The value
    */
   public List<ConfigProperty> getConfigProperties()
   {
      if (configProperties == null)
         configProperties = new ArrayList<ConfigProperty>(1);

      return configProperties;
   }

   /**
    * Get the statistics
    * @return The value; <code>null</code> if no statistics is available
    */
   public StatisticsPlugin getStatistics()
   {
      if (getResourceAdapter() != null && getResourceAdapter() instanceof Statistics)
      {
         return ((Statistics)getResourceAdapter()).getStatistics();
      }

      return null;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ResourceAdapter@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[instance=").append(getResourceAdapter());
      sb.append(" configProperties=").append(configProperties);
      sb.append(" statistics=").append(getStatistics());
      sb.append("]");

      return sb.toString();
   }
}
