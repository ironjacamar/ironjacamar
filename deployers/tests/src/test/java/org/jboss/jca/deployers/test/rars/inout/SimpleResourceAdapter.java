/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.test.rars.inout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Logger;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;

/**
 * SimpleResourceAdapter
 *
 * @version $Revision: $
 */
public class SimpleResourceAdapter implements ResourceAdapter, java.io.Serializable
{

   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger("SimpleResourceAdapter");

   /** The activations by activation spec */
   private Map<SimpleActivationSpec, SimpleActivation> activations;

   /** first */
   private String first = "ra";

   /** second */
   private Boolean second = true;

   /**
    * Default constructor
    */
   public SimpleResourceAdapter()
   {
      this.activations = Collections.synchronizedMap(new HashMap<SimpleActivationSpec, SimpleActivation>());

   }

   /** 
    * Set first
    * @param first The value
    */
   public void setFirst(String first)
   {
      this.first = first;
   }

   /** 
    * Get first
    * @return The value
    */
   public String getFirst()
   {
      return first;
   }

   /** 
    * Set second
    * @param second The value
    */
   public void setSecond(Boolean second)
   {
      this.second = second;
   }

   /** 
    * Get second
    * @return The value
    */
   public Boolean getSecond()
   {
      return second;
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
      SimpleActivation activation = new SimpleActivation(this, endpointFactory, (SimpleActivationSpec)spec);
      activations.put((SimpleActivationSpec)spec, activation);
      activation.start();

      log.finest("endpointActivation()");
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
      SimpleActivation activation = activations.remove(spec);
      if (activation != null)
         activation.stop();

      log.finest("endpointDeactivation()");
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
      log.finest("start()");
   }

   /**
    * This is called when a resource adapter instance is undeployed or
    * during application server shutdown. 
    */
   public void stop()
   {
      log.finest("stop()");
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
      log.finest("getXAResources()");
      return null;
   }

   /** 
    * Returns a hash code value for the object.
    * @return A hash code value for this object.
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      if (first != null)
         result += 31 * result + 7 * first.hashCode();
      else
         result += 31 * result + 7;
      if (second != null)
         result += 31 * result + 7 * second.hashCode();
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
      if (!(other instanceof SimpleResourceAdapter))
         return false;
      SimpleResourceAdapter obj = (SimpleResourceAdapter)other;
      boolean result = true; 
      if (result)
      {
         if (first == null)
            result = obj.getFirst() == null;
         else
            result = first.equals(obj.getFirst());
      }
      if (result)
      {
         if (second == null)
            result = obj.getSecond() == null;
         else
            result = second.equals(obj.getSecond());
      }
      return result;
   }

}
