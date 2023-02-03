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

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * StatisticsResourceAdapter
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class StatisticsResourceAdapter implements ResourceAdapter, java.io.Serializable, Statistics
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger(StatisticsResourceAdapter.class.getName());

   /** enableStat */
   private Boolean enableStat;

   /** Statistics plugin */
   private StatisticsPlugin statPlugin;

   /**
    * Default constructor
    */
   public StatisticsResourceAdapter()
   {
      statPlugin = new StatisticsResourceAdapterStatisticsPlugin();
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
    * This is called during the activation of a message endpoint.
    *
    * @param endpointFactory A message endpoint factory instance.
    * @param spec An activation spec JavaBean instance.
    * @throws ResourceException generic exception 
    */
   public void endpointActivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec) throws ResourceException
   {
      log.tracef("endpointActivation(%s, %s)", endpointFactory, spec);

   }

   /**
    * This is called when a message endpoint is deactivated. 
    *
    * @param endpointFactory A message endpoint factory instance.
    * @param spec An activation spec JavaBean instance.
    */
   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec)
   {
      log.tracef("endpointDeactivation(%s)", endpointFactory);

   }

   /**
    * This is called when a resource adapter instance is bootstrapped.
    *
    * @param ctx A bootstrap context containing references 
    * @throws ResourceAdapterInternalException indicates bootstrap failure.
    */
   public void start(BootstrapContext ctx)
      throws ResourceAdapterInternalException
   {
      log.tracef("start(%s)", ctx);

   }

   /**
    * This is called when a resource adapter instance is undeployed or
    * during application server shutdown. 
    */
   public void stop()
   {
      log.trace("stop()");
   }

   /**
    * This method is called by the application server during crash recovery.
    *
    * @param specs An array of ActivationSpec JavaBeans 
    * @throws ResourceException generic exception 
    * @return An array of XAResource objects
    */
   public XAResource[] getXAResources(ActivationSpec[] specs)
      throws ResourceException
   {
      log.tracef("getXAResources(%s)", specs.toString());
      return null;
   }

   /**
    * Get statistics
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
      if (!(other instanceof StatisticsResourceAdapter))
         return false;
      boolean result = true;
      StatisticsResourceAdapter obj = (StatisticsResourceAdapter)other;
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
