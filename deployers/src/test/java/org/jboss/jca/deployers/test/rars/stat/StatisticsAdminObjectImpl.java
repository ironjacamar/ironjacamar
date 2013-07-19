/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.test.rars.stat;

import org.jboss.jca.core.spi.statistics.Statistics;
import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;

import javax.resource.Referenceable;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;

/**
 * StatisticsAdminObjectImpl
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class StatisticsAdminObjectImpl implements StatisticsAdminObject,
                                                  ResourceAdapterAssociation, Referenceable, Serializable,
                                                  Statistics
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** The resource adapter */
   private ResourceAdapter ra;

   /** Reference */
   private Reference reference;

   /** enableStat */
   private Boolean enableStat;

   /** Statistics plugin */
   private StatisticsPlugin statPlugin;

   /**
    * Default constructor
    */
   public StatisticsAdminObjectImpl()
   {
      statPlugin = new StatisticsAdminObjectStatisticsPlugin();
   }

   /** 
    * Set enableStat
    * @param enableStat The value
    */
   public void setEnableStat(Boolean enableStat)
   {
      this.enableStat = enableStat;
   }

   /** 
    * Get enableStat
    * @return The value
    */
   public Boolean getEnableStat()
   {
      return enableStat;
   }

   /**
    * Get the resource adapter
    *
    * @return The handle
    */
   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }

   /**
    * Set the resource adapter
    *
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      this.ra = ra;
   }

   /**
    * Get the Reference instance.
    *
    * @return Reference instance
    * @exception NamingException Thrown if a reference can't be obtained
    */
   @Override
   public Reference getReference() throws NamingException
   {
      return reference;
   }

   /**
    * Set the Reference instance.
    *
    * @param reference A Reference instance
    */
   @Override
   public void setReference(Reference reference)
   {
      this.reference = reference;
   }

   /**
    * Get the statistics plugin
    * @return The value
    */
   public StatisticsPlugin getStatistics()
   {
      return statPlugin;
   }

   /** 
    * Returns a hash code value for the object.
    * @return A hash code value for this object.
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      if (enableStat != null)
         result += 31 * result + 7 * enableStat.hashCode();
      else
         result += 31 * result + 7;
      return result;
   }

   /** 
    * Indicates whether some other object is equal to this one.
    * @param other The reference object with which to compare.
    * @return true if this object is the same as the obj argument, false otherwise.
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;
      if (other == this)
         return true;
      if (!(other instanceof StatisticsAdminObjectImpl))
         return false;
      boolean result = true;
      StatisticsAdminObjectImpl obj = (StatisticsAdminObjectImpl)other;
      if (result)
      {
         if (enableStat == null)
            result = obj.getEnableStat() == null;
         else
            result = enableStat.equals(obj.getEnableStat());
      }
      return result;
   }

}
