/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.common.metadata.ra.ra10.Connector10Impl;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.test.deployers.spec.DeploymentTestBase;
import org.jboss.jca.test.deployers.spec.rars.BaseCciConnectionFactory;
import org.jboss.jca.test.deployers.spec.rars.ra10dtdout.TestManagedConnectionFactory;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Resource;
import javax.resource.cci.ConnectionFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for deploying resource adapter archives (.RAR) using -ra.xml files
 * for activation
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class Ra10dtdoutTestCase extends DeploymentTestBase
{
   /**
    * 
    * create deployment 
    * 
    * @param archiveName 
    * @return archive
    * @throws Exception in case of error
    */
   public static ResourceAdapterArchive createDeployment(String archiveName) throws Exception
   {
      ResourceAdapterArchive raa = buidShrinkwrapRa(archiveName,
         getPackageName(TestManagedConnectionFactory.class));
      raa.addAsManifestResource(archiveName + "/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * MDR
    */
   @Inject(name = "MDR")
   private MetadataRepository mdr;

   /**
    * 
    * first deployment
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive deployment1() throws Exception
   {
      return createDeployment("ra10dtdout.rar");
   }

   /**
    * 
    * second deployment
    * 
    * @return archive
    * @throws Exception in case of error
    */
   @Deployment(name = "d2", order = 2)
   public static ResourceAdapterArchive deployment2() throws Exception
   {
      return createDeployment("ra10dtdoutoverwrite.rar");
   }

   /**
    * connection factories, registered after deployment
    */
   @Resource(mappedName = "java:/eis/ra10dtdout")
   private ConnectionFactory connectionFactory1;

   @Resource(mappedName = "java:/eis/ra10dtdoutoverwrite")
   private ConnectionFactory connectionFactory2;

   /**
    * 
    * Tests connection factory and properties of ManagedConnectionFactory
    * 
    * @param connectionFactory to test
    * @param aaa property value
    * @param bbb property value
    * @throws Exception in case of error
    */
   public void testBasic(ConnectionFactory connectionFactory, String aaa, boolean bbb) throws Exception
   {
      assertNotNull(connectionFactory);
      assertNotNull(connectionFactory.getConnection());
      assertTrue(connectionFactory instanceof BaseCciConnectionFactory);
      BaseCciConnectionFactory cf = (BaseCciConnectionFactory) connectionFactory;
      assertNotNull(cf.getMcf());
      assertTrue(cf.getMcf() instanceof TestManagedConnectionFactory);
      TestManagedConnectionFactory mcf = (TestManagedConnectionFactory) cf.getMcf();
      assertEquals(aaa, mcf.getAaa());
      assertEquals(bbb, mcf.getBbb());
   }

   /**
    * 
    * first connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void first() throws Exception
   {
      testBasic(connectionFactory1, "bbb", true);
   }

   /**
    * 
    * second connection factory test
    * 
    * @throws Exception in case of error
    */
   @Test
   public void second() throws Exception
   {
      testBasic(connectionFactory2, "aaa", false);
   }

   /**
    * Test metadata
    *     
    * @throws Exception in case of error
    */
   @Test
   public void testMetaData() throws Exception
   {
      assertNotNull(mdr);
      Set<String> ids = mdr.getResourceAdapters();

      assertNotNull(ids);
      assertEquals(2, ids.size());

      Iterator<String> it = ids.iterator();
      while (it.hasNext())
         checkMetadata(it.next());
   }

   /**
    * 
    * checks, if metadata is of appropriate type
    * 
    * @param piId - metadata name
    * @throws Exception in case of error
    */
   public void checkMetadata(String piId) throws Exception
   {
      assertNotNull(piId);
      assertNotNull(mdr.getResourceAdapter(piId));
      assertTrue(mdr.getResourceAdapter(piId) instanceof Connector10Impl);

   }
}
