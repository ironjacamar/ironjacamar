/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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
package org.ironjacamar.core.inflow;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.util.Injection;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.test.inflow.TestMessageListener;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Inflow
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class InflowTestCase
{
   /** The deployment repository */
   @Inject
   private static DeploymentRepository dr;
   
   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createTestRar();
   }
   
   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTestDeployment(0, null, 0);
   }
   
   /**
    * onMessage
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testOnMessage() throws Throwable
   {
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      Collection<org.ironjacamar.core.api.deploymentrepository.Deployment> deployments =
         dr.findByMessageListener(TestMessageListener.class.getName());

      assertNotNull(deployments);
      assertEquals(1, deployments.size());

      org.ironjacamar.core.api.deploymentrepository.Deployment d = deployments.iterator().next();

      // Build ActivationSpec instance -- verify config properties / required config properties
      org.ironjacamar.core.api.deploymentrepository.ResourceAdapter r = d.getResourceAdapter();

      assertNotNull(r);
      assertTrue(r.isMessageListenerSupported(TestMessageListener.class.getName()));

      org.ironjacamar.core.api.deploymentrepository.MessageListener ml =
         r.createMessageListener(TestMessageListener.class.getName());

      assertNotNull(ml);
      assertEquals(1, ml.getConfigProperties().size());
      assertTrue(ml.getConfigProperties().containsKey("Name"));
      assertEquals(1, ml.getRequiredConfigProperties().size());
      assertTrue(ml.getRequiredConfigProperties().contains("Name"));
      
      ActivationSpec as = ml.createActivationSpec();
      assertNotNull(as);

      Injection injection = new Injection();
      injection.inject(as, "Name", "IronJacamar", String.class.getName());
      
      // Activate message endpoint
      TestMessageEndpointFactory tmef = new TestMessageEndpointFactory("Test");

      r.endpointActivation(tmef, as);
      
      assertEquals("IronJacamar", tmef.getMessage());
      assertTrue(tmef.isBeforeDelivery());
      assertTrue(tmef.isAfterDelivery());
      assertTrue(tmef.isRelease());
      
      // Deactivate message endpoint
      r.endpointDeactivation(tmef, as);
   }

   /**
    * TestMessageEndpointFactory
    */
   static class TestMessageEndpointFactory implements MessageEndpointFactory
   {
      private String activationName;
      private String message;
      private boolean beforeDelivery;
      private boolean afterDelivery;
      private boolean release;
      
      /**
       * Constructor
       * @param activationName The activation name
       */
      TestMessageEndpointFactory(String activationName)
      {
         this.activationName = activationName;
         this.message = "";
         this.beforeDelivery = false;
         this.afterDelivery = false;
         this.release = false;
      }
      
      /**
       * {@inheritDoc}
       */
      public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException
      {
         return new TestMessageEndpoint(this);
      }

      /**
       * {@inheritDoc}
       */
      public MessageEndpoint createEndpoint(XAResource xaResource, long timeout) throws UnavailableException
      {
         return new TestMessageEndpoint(this);
      }

      /**
       * {@inheritDoc}
       */
      public String getActivationName()
      {
         return activationName;
      }

      /**
       * {@inheritDoc}
       */
      public Class<?> getEndpointClass()
      {
         return TestMessageEndpoint.class;
      }

      /**
       * {@inheritDoc}
       */
      public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException
      {
         return false;
      }

      /**
       * Get the message
       * @return The value
       */
      public String getMessage()
      {
         return message;
      }

      /**
       * Set the message
       * @param v The value
       */
      public void setMessage(String v)
      {
         message = v;
      }

      /**
       * Is beforeDelivery
       * @return The value
       */
      public boolean isBeforeDelivery()
      {
         return beforeDelivery;
      }

      /**
       * Set beforeDelivery
       * @param v The value
       */
      public void setBeforeDelivery(boolean v)
      {
         beforeDelivery = v;
      }

      /**
       * Is afterDelivery
       * @return The value
       */
      public boolean isAfterDelivery()
      {
         return afterDelivery;
      }

      /**
       * Set afterDelivery
       * @param v The value
       */
      public void setAfterDelivery(boolean v)
      {
         afterDelivery = v;
      }

      /**
       * Is release
       * @return The value
       */
      public boolean isRelease()
      {
         return release;
      }

      /**
       * Set release
       * @param v The value
       */
      public void setRelease(boolean v)
      {
         release = v;
      }
   }

   /**
    * TestMessageEndpoint
    */
   static class TestMessageEndpoint implements MessageEndpoint, TestMessageListener
   {
      private TestMessageEndpointFactory factory;

      /**
       * Constructor
       * @param factory The factory
       */
      TestMessageEndpoint(TestMessageEndpointFactory factory)
      {
         this.factory = factory;
      }
      
      /**
       * {@inheritDoc}
       */
      public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException
      {
         factory.setBeforeDelivery(true);
      }

      /**
       * {@inheritDoc}
       */
      public void afterDelivery() throws ResourceException
      {
         factory.setAfterDelivery(true);
      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
         factory.setRelease(true);
      }
      
      /**
       * {@inheritDoc}
       */
      public void onMessage(String msg)
      {
         factory.setMessage(msg);
      }
   }
}
