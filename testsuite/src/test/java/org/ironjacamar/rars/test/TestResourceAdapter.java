/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.rars.test;

import org.ironjacamar.rars.test.inflow.TestActivation;
import org.ironjacamar.rars.test.inflow.TestActivationSpec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;

/**
 * TestResourceAdapter
 *
 * @version $Revision: $
 */
public class TestResourceAdapter implements ResourceAdapter, java.io.Serializable
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The activations by activation spec */
   private Map<TestActivationSpec, TestActivation> activations;

   /**
    * Default constructor
    */
   public TestResourceAdapter()
   {
      this.activations = Collections.synchronizedMap(new HashMap<TestActivationSpec, TestActivation>());
   }

   /**
    * {@inheritDoc}
    */
   public void endpointActivation(MessageEndpointFactory endpointFactory,
                                  ActivationSpec spec) throws ResourceException
   {
      TestActivation activation = new TestActivation(this, endpointFactory, (TestActivationSpec)spec);
      activations.put((TestActivationSpec)spec, activation);
      activation.start();
   }

   /**
    * {@inheritDoc}
    */
   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                    ActivationSpec spec)
   {
      TestActivation activation = activations.remove(spec);
      if (activation != null)
         activation.stop();
   }

   /**
    * {@inheritDoc}
    */
   public void start(BootstrapContext ctx)
      throws ResourceAdapterInternalException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
   }

   /**
    * {@inheritDoc}
    */
   public XAResource[] getXAResources(ActivationSpec[] specs)
      throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      return result;
   }

   /**
    * {@inheritDoc}
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
      boolean result = true;
      return result;
   }
}
