/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * TestResourceAdapter
 *
 * @version $Revision: $
 */
@Connector(reauthenticationSupport = false, 
transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction)
public class TestResourceAdapter implements ResourceAdapter
{

   /** The logger */
   private static Logger log = Logger.getLogger("TestResourceAdapter");

   /** The activations by activation spec */
   private ConcurrentHashMap<TestActivationSpec, TestActivation> activations;

   /** The activations by activation spec */
   private ConcurrentHashMap<Test2ActivationSpec, Test2Activation> activations2;

   /** stringProperty */
   @ConfigProperty(defaultValue = "string")
   private String stringProperty;

   /** intProperty */
   @ConfigProperty(defaultValue = "1")
   private Integer intProperty;

   /**
    * Default constructor
    */
   public TestResourceAdapter()
   {
      this.activations = new ConcurrentHashMap<TestActivationSpec, TestActivation>();

   }

   /** 
    * Set stringProperty
    * @param stringProperty The value
    */
   public void setStringProperty(String stringProperty)
   {
      this.stringProperty = stringProperty;
   }

   /** 
    * Get stringProperty
    * @return The value
    */
   public String getStringProperty()
   {
      return stringProperty;
   }

   /** 
    * Set intProperty
    * @param intProperty The value
    */
   public void setIntProperty(Integer intProperty)
   {
      this.intProperty = intProperty;
   }

   /** 
    * Get intProperty
    * @return The value
    */
   public Integer getIntProperty()
   {
      return intProperty;
   }

   /**
    * This is called during the activation of a message endpoint.
    *
    * @param endpointFactory A message endpoint factory instance.
    * @param spec An activation spec JavaBean instance.
    * @throws ResourceException generic exception 
    */
   public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException
   {
      if (endpointFactory == null || spec == null)
         return;

      if (spec instanceof TestActivation)
      {
         TestActivation activation = new TestActivation(this, endpointFactory, (TestActivationSpec) spec);
         activations.put((TestActivationSpec) spec, activation);
         activation.start();
      }
      else if (spec instanceof Test2Activation)
      {
         Test2Activation activation2 = new Test2Activation(this, endpointFactory, (Test2ActivationSpec) spec);
         activations2.put((Test2ActivationSpec) spec, activation2);
         activation2.start();
      }

      log.finest("endpointActivation()");
   }

   /**
    * This is called when a message endpoint is deactivated. 
    *
    * @param endpointFactory A message endpoint factory instance.
    * @param spec An activation spec JavaBean instance.
    */
   public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
   {
      if (endpointFactory == null || spec == null)
         return;

      if (spec instanceof TestActivation)
      {
         TestActivation activation = activations.remove(spec);
         if (activation != null)
            activation.stop();
      }
      else if (spec instanceof Test2Activation)
      {
         Test2Activation activation2 = activations2.remove(spec);
         if (activation2 != null)
            activation2.stop();
      }

      log.finest("endpointDeactivation()");
   }

   /**
    * This is called when a resource adapter instance is bootstrapped.
    *
    * @param ctx A bootstrap context containing references 
    * @throws ResourceAdapterInternalException indicates bootstrap failure.
    */
   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
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
   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
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
      if (stringProperty != null)
         result += 31 * result + 7 * stringProperty.hashCode();
      else
         result += 31 * result + 7;
      if (intProperty != null)
         result += 31 * result + 7 * intProperty.hashCode();
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
      if (!(other instanceof TestResourceAdapter))
         return false;
      TestResourceAdapter obj = (TestResourceAdapter) other;
      boolean result = true;
      if (result)
      {
         if (stringProperty == null)
            result = obj.getStringProperty() == null;
         else
            result = stringProperty.equals(obj.getStringProperty());
      }
      if (result)
      {
         if (intProperty == null)
            result = obj.getIntProperty() == null;
         else
            result = intProperty.equals(obj.getIntProperty());
      }
      return result;
   }

}
