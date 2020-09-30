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
 * Represents an admin object instance
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AdminObject
{
   /** The object instance */
   private WeakReference<Object> instance;

   /** The config property's */
   private List<ConfigProperty> configProperties;
   
   /** jndi name */
   private String jndiName;

   /**
    * Constructor
    * @param ao The admin object instance
    */
   public AdminObject(Object ao)
   {
      this.instance = new WeakReference<Object>(ao);
      this.configProperties = null;
   }

   /**
    * Get the admin object instance.
    * 
    * Note, that the value may be <code>null</code> if the admin object was
    * undeployed and this object wasn't cleared up correctly.
    * @return The instance
    */
   public Object getAdminObject()
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
    * Get the jndiName.
    * 
    * @return the jndiName.
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Set the jndiName.
    * 
    * @param jndiName The jndiName to set.
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   /**
    * Get the statistics
    * @return The value; <code>null</code> if no statistics is available
    */
   public StatisticsPlugin getStatistics()
   {
      if (getAdminObject() != null && getAdminObject() instanceof Statistics)
      {
         return ((Statistics)getAdminObject()).getStatistics();
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

      sb.append("AdminObject@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[instance=").append(getAdminObject());
      sb.append(" configProperties=").append(configProperties);
      sb.append(" statistics=").append(getStatistics());
      sb.append("]");

      return sb.toString();
   }
}
