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
package org.jboss.jca.deployers.test.rars.lazy;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * LazyResourceAdapter
 */
public class LazyResourceAdapter implements ResourceAdapter
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyResourceAdapter.class);

   /** Enable */
   private Boolean enable;

   /** Local transaction */
   private Boolean localTransaction;

   /** XA transaction */
   private Boolean xaTransaction;

   /**
    * Default constructor
    */
   public LazyResourceAdapter()
   {
      enable = Boolean.TRUE;
      localTransaction = Boolean.FALSE;
      xaTransaction = Boolean.FALSE;
   }

   /** 
    * Set Enable
    * @param value The value
    */
   public void setEnable(Boolean value)
   {
      this.enable = value;
   }

   /** 
    * Get Enable
    * @return The value
    */
   public Boolean getEnable()
   {
      return enable;
   }

   /** 
    * Set LocalTransaction
    * @param value The value
    */
   public void setLocalTransaction(Boolean value)
   {
      this.localTransaction = value;
   }

   /** 
    * Get LocalTransaction
    * @return The value
    */
   public Boolean getLocalTransaction()
   {
      return localTransaction;
   }

   /** 
    * Set XATransaction
    * @param value The value
    */
   public void setXATransaction(Boolean value)
   {
      this.xaTransaction = value;
   }

   /** 
    * Get XATransaction
    * @return The value
    */
   public Boolean getXATransaction()
   {
      return xaTransaction;
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
      log.trace("endpointActivation()");
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
      log.trace("endpointDeactivation()");
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
      log.trace("start()");
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
      log.trace("getXAResources()");
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
      if (enable != null)
         result += 31 * result + 7 * enable.hashCode();
      else
         result += 31 * result + 7;
      if (localTransaction != null)
         result += 31 * result + 7 * localTransaction.hashCode();
      else
         result += 31 * result + 7;
      if (xaTransaction != null)
         result += 31 * result + 7 * xaTransaction.hashCode();
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
      if (!(other instanceof LazyResourceAdapter))
         return false;
      LazyResourceAdapter obj = (LazyResourceAdapter)other;
      boolean result = true; 
      if (result)
      {
         if (enable == null)
            result = obj.getEnable() == null;
         else
            result = enable.equals(obj.getEnable());
      }
      if (result)
      {
         if (localTransaction == null)
            result = obj.getLocalTransaction() == null;
         else
            result = localTransaction.equals(obj.getLocalTransaction());
      }
      if (result)
      {
         if (xaTransaction == null)
            result = obj.getXATransaction() == null;
         else
            result = xaTransaction.equals(obj.getXATransaction());
      }
      return result;
   }
}
