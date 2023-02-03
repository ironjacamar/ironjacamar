/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.inflow;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.jca.core.inflow.ra.PureInflowResourceAdapter;
import org.jboss.jca.core.inflow.ra.inflow.PureInflowActivationSpec;
import org.jboss.jca.core.spi.rar.Endpoint;
import org.jboss.jca.core.spi.rar.MessageListener;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.naming.InitialContext;
import jakarta.resource.spi.ActivationSpec;

import org.jboss.logging.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for pure inflow
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PureInflowTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(PureInflowTestCase.class);

   public static ResourceAdapterArchive createDeployment()
   {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "pure-inflow.jar");
      jar.addClass(PureInflowResourceAdapter.class);

      ResourceAdapterArchive rar =
              ShrinkWrap.create(ResourceAdapterArchive.class, "pure-inflow.rar");
      rar.addAsLibrary(jar);
      rar.addAsManifestResource("rars/inflow/META-INF/ironjacamar.xml", "ironjacamar.xml");
      rar.addAsManifestResource("rars/inflow/META-INF/ra.xml", "ra.xml");
      return rar;
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Inflow
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testInflow() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(true);
      ResourceAdapterArchive pureInflowRar = createDeployment();

      InitialContext ic = null;

      try
      {
         embedded.startup();

         ResourceAdapterRepository raRepository = 
            embedded.lookup("ResourceAdapterRepository", ResourceAdapterRepository.class);

         assertNotNull(raRepository);

         RAActivator raActivator = 
            embedded.lookup("RAActivator", RAActivator.class);

         assertNotNull(raActivator);
         raActivator.setEnabled(false);

         embedded.deploy(pureInflowRar);

         ic = new InitialContext();

         Set<String> ids = raRepository.getResourceAdapters(jakarta.jms.MessageListener.class);

         assertNotNull(ids);
         assertEquals(1, ids.size());

         String piId = ids.iterator().next();
         assertNotNull(piId);

         Endpoint endpoint = raRepository.getEndpoint(piId);
         assertNotNull(endpoint);

         List<MessageListener> listeners = raRepository.getMessageListeners(piId);
         assertNotNull(listeners);
         assertEquals(1, listeners.size());

         MessageListener listener = listeners.get(0);

         ActivationSpec as = listener.getActivation().createInstance();
         assertNotNull(as);
         assertNotNull(as.getResourceAdapter());

         assertTrue(as instanceof PureInflowActivationSpec);
         PureInflowActivationSpec pias = (PureInflowActivationSpec)as;
         assertNotNull(pias.getDefaultString());
         assertEquals("Default", pias.getDefaultString());
         assertNotNull(pias.getDefaultBoolean());
         assertTrue(pias.getDefaultBoolean());
      }
      finally
      {
         embedded.undeploy(pureInflowRar);
         embedded.shutdown();
      }
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
   }
}
