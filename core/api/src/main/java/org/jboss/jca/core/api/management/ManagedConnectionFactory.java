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
 * Represents a managed connection factory instance
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a> 
 */
public class ManagedConnectionFactory
{
   /** The object instance */
   private WeakReference<jakarta.resource.spi.ManagedConnectionFactory> instance;

   /** The config property's */
   private List<ConfigProperty> configProperties;
   
   /**
    * Constructor
    * @param mcf The managed connection factory instance
    */
   public ManagedConnectionFactory(jakarta.resource.spi.ManagedConnectionFactory mcf)
   {
      this.instance = new WeakReference<jakarta.resource.spi.ManagedConnectionFactory>(mcf);
      this.configProperties = null;
   }

   /**
    * Get the managed connection factory instance.
    * 
    * Note, that the value may be <code>null</code> if the managed connection factory was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public jakarta.resource.spi.ManagedConnectionFactory getManagedConnectionFactory()
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
      if (getManagedConnectionFactory() != null && getManagedConnectionFactory() instanceof Statistics)
      {
         return ((Statistics)getManagedConnectionFactory()).getStatistics();
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

      sb.append("ManagedConnectionFactory@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[instance=").append(getManagedConnectionFactory());
      sb.append(" configProperties=").append(configProperties);
      sb.append(" statistics=").append(getStatistics());
      sb.append("]");

      return sb.toString();
   }
}
