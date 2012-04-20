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
package org.jboss.jca.test.deployers.spec.rars.testcases;

import static org.junit.Assert.*;

import org.jboss.jca.common.metadata.ra.ra16.Connector16Impl;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.rar.Endpoint;
import org.jboss.jca.core.spi.rar.MessageListener;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.embedded.arquillian.Inject;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.Test1AdminObjectInterface;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.Test2ActivationSpec;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.Test2Connection;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.Test2ConnectionFactory;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.Test2ManagedConnectionFactory;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.Test2MessageListener;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.TestActivationSpec;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.TestAdminObjectInterface;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.TestConnection;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.TestConnectionFactory;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.TestManagedConnectionFactory;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.TestMessageListener;
import org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno.TestResourceAdapter;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.resource.spi.ActivationSpec;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Ra16inoutijmultiannoTestCase
 * 
 * @author Vladimir Rastseluev
 *
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class Ra16inoutijmultiannoTestCase
{
   private static Logger log = Logger.getLogger("Ra16inoutijmultiannoTestCase");

   private static String deploymentName = "Ra16inoutijmultiannoTestCase";

   /**
    * Define the deployment
    *
    * @return The deployment archive
    */
   @Deployment
   public static ResourceAdapterArchive createDeployment()
   {
      String archiveName = "ra16inoutijmultianno.rar";
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, deploymentName + ".rar");
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(TestResourceAdapter.class.getPackage());
      raa.addAsLibrary(ja);

      raa.addAsManifestResource(archiveName + "/META-INF/ironjacamar.xml", "ironjacamar.xml");

      return raa;
   }

   /** Resource */
   @Resource(mappedName = "java:/eis/TestConnectionFactory")
   private TestConnectionFactory connectionFactory1;

   /** Resource */
   @Resource(mappedName = "java:/eis/Test2ConnectionFactory")
   private Test2ConnectionFactory connectionFactory2;

   /** Resource */
   @Resource(mappedName = "java:/eis/ao/TestAdminObjectInterface")
   private TestAdminObjectInterface adminObject;

   /** Resource */
   @Resource(mappedName = "java:/eis/ao/Test1AdminObjectInterface")
   private Test1AdminObjectInterface adminObject1;

   @Inject(name = "ResourceAdapterRepository")
   private ResourceAdapterRepository raRepository;

   @Inject(name = "MDR")
   private MetadataRepository mdr;

   /**
    * Test getConnection
    *
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testGetConnection1() throws Throwable
   {
      assertNotNull(connectionFactory1);
      TestConnection connection1 = connectionFactory1.getConnection();
      assertNotNull(connection1);
      TestManagedConnectionFactory mcf = connection1.getMCF();
      assertEquals(new Byte((byte) 2), mcf.getByteProperty());
      connection1.close();
   }

   /**
    * Test getConnection
    *
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testGetConnection2() throws Throwable
   {
      assertNotNull(connectionFactory2);
      Test2Connection connection2 = connectionFactory2.getConnection();
      assertNotNull(connection2);
      Test2ManagedConnectionFactory mcf = connection2.getMCF();
      assertEquals(new Double(3.14), mcf.getDoubleProperty());
      connection2.close();
   }

   /**
    * Test admin objects
   *
   * @exception Throwable Thrown if case of an error
   */
   @Test
   public void testAdminOjbects() throws Throwable
   {
      assertNotNull(adminObject);
      assertEquals(new Float(1.1f), adminObject.getFloatProperty());
      assertNotNull(adminObject1);
      assertEquals(true, adminObject1.getBooleanProperty());
   }

   /**
    * test activation 1
    * 
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testActivation1() throws Throwable
   {
      testActivation(TestMessageListener.class);
   }

   /**
    * test activation 2
    * 
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testActivation2() throws Throwable
   {
      testActivation(Test2MessageListener.class);
   }

   /**
    * Test activation
    * 
    * @param clazz class name
    * 
    * @exception Throwable Thrown if case of an error
    */
   public void testActivation(Class clazz) throws Throwable
   {
      Set<String> ids = raRepository.getResourceAdapters(clazz);

      assertNotNull(ids);
      assertEquals(1, ids.size());

      String piId = ids.iterator().next();
      assertNotNull(piId);

      Endpoint endpoint = raRepository.getEndpoint(piId);
      assertNotNull(endpoint);

      List<MessageListener> listeners = raRepository.getMessageListeners(piId);
      assertNotNull(listeners);
      assertEquals(2, listeners.size());

      MessageListener listener = listeners.get(0);
      MessageListener listener1 = listeners.get(1);

      ActivationSpec as = listener.getActivation().createInstance();
      ActivationSpec as1 = listener1.getActivation().createInstance();
      assertNotNull(as);
      assertNotNull(as.getResourceAdapter());
      assertNotNull(as1);
      assertNotNull(as1.getResourceAdapter());

      TestActivationSpec tas;
      Test2ActivationSpec tas1;

      if (as instanceof TestActivationSpec)
      {
         tas = (TestActivationSpec) as;
         tas1 = (Test2ActivationSpec) as1;
      }
      else
      {
         tas = (TestActivationSpec) as1;
         tas1 = (Test2ActivationSpec) as;
      }
      assertNotNull(tas.getCharProperty());
      assertEquals(new Character('a'), tas.getCharProperty());
      assertTrue(tas.getResourceAdapter() instanceof TestResourceAdapter);
      TestResourceAdapter tra = (TestResourceAdapter) tas.getResourceAdapter();
      assertEquals("string", tra.getStringProperty());

      assertNotNull(tas1.getShortProperty());
      assertEquals((short) 3, tas1.getShortProperty());
      assertTrue(tas1.getResourceAdapter() instanceof TestResourceAdapter);
      tra = (TestResourceAdapter) tas1.getResourceAdapter();
      assertEquals(new Integer(1), tra.getIntProperty());
   }

   /**
    * Test metadata
    *     
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testMetaData() throws Throwable
   {
      assertNotNull(mdr);
      Set<String> ids = mdr.getResourceAdapters();

      assertNotNull(ids);
      assertEquals(1, ids.size());

      String piId = ids.iterator().next();
      assertNotNull(piId);
      assertNotNull(mdr.getResourceAdapter(piId));
      assertTrue(mdr.getResourceAdapter(piId) instanceof Connector16Impl);
   }

}
