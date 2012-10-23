/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.test;

import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.deployers.test.rars.inout.SimpleAdminObject;
import org.jboss.jca.deployers.test.rars.inout.SimpleAdminObjectImpl;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnection;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnection1;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory1;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactoryImpl;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactoryImpl1;
import org.jboss.jca.deployers.test.rars.inout.SimpleManagedConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleManagedConnectionFactory1;
import org.jboss.jca.deployers.test.rars.inout.SimpleResourceAdapter;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import static org.junit.Assert.*;

/**
 * DeploymentTestBase - base class for tests with deployments
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public abstract class DeploymentTestBase
{
   /** The logger */
   protected static Logger log = Logger.getLogger("DeploymentTestBase");

   /**
    * MDR
    */
   @Inject(name = "MDR")
   protected MetadataRepository mdr;

   /**
    * Build a shrinkwrap rar adding all necessary classes
    *
    * @param archiveName the archhive name
    * @param packageName the package name
    * @return the shrinkwrapped rar
    * @throws Exception in case of error creating the archive
    */
   public static ResourceAdapterArchive buidShrinkwrapRa(String archiveName, String packageName) throws Exception
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, archiveName);

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");

      ja.addPackage(packageName);

      raa.addAsLibrary(ja);

      return raa;
   }

   /**
    * Build a shrinkwrap rar adding all necessary classes
    *
    * @param archiveName the archhive name
    * @param packageName the package name
    * @return the shrinkwrapped rar
    * @throws Exception in case of error creating the archive
    */
   public static ResourceAdapterArchive buidShrinkwrapRa(String archiveName, Package packageName) throws Exception
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, archiveName);

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");

      ja.addPackage(packageName);

      raa.addAsLibrary(ja);

      return raa;
   }

   /**
    * Build a shrinkwrap rar adding all necessary classes
    *
    * @param archiveName the archhive name
    * @param classes to add
    * @return the shrinkwrapped rar
    * @throws Exception in case of error creating the archive
    */
   public static ResourceAdapterArchive buidShrinkwrapRa(String archiveName, Class... classes) throws Exception
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, archiveName);

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");

      ja.addClasses(classes);

      raa.addAsLibrary(ja);

      return raa;
   }

   /**
    * 
    * Create .ear, containing .rar
    * 
    * @param rar archive
    * @return ear archive
    * @throws Exception in case of error
    */
   public static EnterpriseArchive buildEarWithRar(ResourceAdapterArchive rar) throws Exception
   {
      EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, rar.getName().replaceAll("rar", "ear"));
      ear.addAsModule(rar);
      return ear;
   }

   /**
    * 
    * Create .ear, containing some .rar archives
    * 
    * @param name of created archive
    * @param rars archives
    * @return ear archive
    * @throws Exception in case of error
    */
   public static EnterpriseArchive buildEarWithRar(String name, ResourceAdapterArchive... rars) throws Exception
   {
      EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, name);
      for (ResourceAdapterArchive rar : rars)
         ear.addAsModule(rar);
      return ear;
   }

   /**
    * 
    * Adds resource file to the archive
    * 
    * @param name of file
    * @param raa archive
    */
   public static void addResourceFile(String name, ResourceAdapterArchive raa)
   {
      raa.addAsManifestResource(raa.getName() + "/META-INF/" + name, name);
   }

   /**
    * 
    * Adds ironjacamar descriptor to the archive
    * 
    * @param raa archive
    */
   public static void addIJXml(ResourceAdapterArchive raa)
   {
      addResourceFile("ironjacamar.xml", raa);
   }

   /**
    * 
    * Adds ra descriptor to the archive
    * 
    * @param raa archive
    */
   public static void addRaXml(ResourceAdapterArchive raa)
   {
      addResourceFile("ra.xml", raa);
   }

   /**
    * Define the deployment of file
    * @param fileName to deploy
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   public static Descriptor createDescriptor(String fileName) throws Exception
   {
      String descriptorName = (fileName.lastIndexOf(".xml") >= 0
         ? fileName.substring(0, fileName.lastIndexOf(".xml"))
         : fileName).replace('/', '-').replace('\\', '-').replace('.', '-') + ".xml";
      log.info("Descriptor created:" + descriptorName);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor(descriptorName, cl.getResourceAsStream(fileName));
      return isd;
   }

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
      ResourceAdapterArchive raa = buidShrinkwrapRa(archiveName, SimpleManagedConnectionFactory.class.getPackage());
      addRaXml(raa);

      return raa;
   }

   /**
    * 
    * Creates deployment with ironjacamar.xml activation
    * 
    * @param dirName name of directory, where resources are placed
    * @param rarName name of created archive
    * @return archive 
    * @throws Exception in case of error
    */
   public static ResourceAdapterArchive createIJDeployment(String dirName, String rarName) throws Exception
   {
      ResourceAdapterArchive rar = createDeployment(dirName);
      addIJXml(rar);

      return ShrinkWrap.create(ResourceAdapterArchive.class, rarName).merge(rar);
   }

   /**
    * Test metadata
    * 
    * 
    * @param size of metadata expected    
    * @throws Exception in case of error
    */
   public void testMetaData(int size) throws Exception
   {
      assertNotNull(mdr);
      Set<String> ids = mdr.getResourceAdapters();

      assertNotNull(ids);
      assertEquals(size, ids.size());

      Iterator<String> it = ids.iterator();
      while (it.hasNext())
      {
         String piId = it.next();
         assertNotNull(piId);
         assertNotNull(mdr.getResourceAdapter(piId));

         checkMetadata(piId);
      }
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
      //to override
   }

   /**
    * 
    * Tests connection factory and properties of ManagedConnectionFactory
    * 
    * @param connectionFactory to test
    * @param first property value
    * @param second property value
    * @return ManagedConnectionFactory
    * @throws Exception in case of error
    */
   public SimpleManagedConnectionFactory testSimpleCF(SimpleConnectionFactory connectionFactory, String first,
      char second) throws Exception
   {
      SimpleManagedConnectionFactory mcf = null;
      assertNotNull(connectionFactory);
      SimpleConnection con = null;
      try
      {
         con = connectionFactory.getConnection();
         assertNotNull(con);
         assertTrue(connectionFactory instanceof SimpleConnectionFactoryImpl);
         SimpleConnectionFactoryImpl cf = (SimpleConnectionFactoryImpl) connectionFactory;
         assertNotNull(cf.getMCF());
         assertTrue(cf.getMCF() instanceof SimpleManagedConnectionFactory);
         mcf = (SimpleManagedConnectionFactory) cf.getMCF();
         assertEquals(first, mcf.getFirst());
         assertEquals(second, (char) mcf.getSecond());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail(e.getMessage());
      }
      finally
      {
         if (con != null)
            con.close();
      }
      return mcf;
   }

   /**
    * 
    * Tests connection factory and properties of ManagedConnectionFactory
    * 
    * @param connectionFactory to test
    * @param first property value
    * @param second property value
    * @return ManagedConnectionFactory
    * @throws Exception in case of error
    */
   public SimpleManagedConnectionFactory1 testSimpleCF1(SimpleConnectionFactory1 connectionFactory, String first,
      byte second) throws Exception
   {
      SimpleManagedConnectionFactory1 mcf = null;
      assertNotNull(connectionFactory);
      SimpleConnection1 con = null;
      try
      {
         con = connectionFactory.getConnection();
         assertNotNull(con);
         assertTrue(connectionFactory instanceof SimpleConnectionFactoryImpl1);
         SimpleConnectionFactoryImpl1 cf = (SimpleConnectionFactoryImpl1) connectionFactory;
         assertNotNull(cf.getMCF());
         assertTrue(cf.getMCF() instanceof SimpleManagedConnectionFactory1);
         mcf = (SimpleManagedConnectionFactory1) cf.getMCF();
         assertEquals(first, mcf.getFirst());
         assertEquals(second, (byte) mcf.getSecond());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail(e.getMessage());
      }
      finally
      {
         if (con != null)
            con.close();
      }
      return mcf;
   }

   /**
    * 
    * tests SimpleAdminObjectImpl properties
    * 
    * @param ao SimpleAdminObject 
    * @param first property should be equal
    * @param second property should be equal
    */
   public void testSimpleAO(SimpleAdminObject ao, String first, double second)
   {
      assertNotNull(ao);
      assertTrue(ao instanceof SimpleAdminObjectImpl);
      SimpleAdminObjectImpl aoi = (SimpleAdminObjectImpl) ao;
      assertEquals(first, aoi.getFirst());
      assertEquals(second, (double) aoi.getSecond(), 0);
   }

   /**
    * 
    * tests SimpleResourceAdapter properites
    * 
    * @param ra SimpleResourceAdapter
    * @param first property should be equal
    * @param second property should be equal
    */
   public void testSimpleRA(SimpleResourceAdapter ra, String first, boolean second)
   {
      assertNotNull(ra);
      assertEquals(first, ra.getFirst());
      assertEquals(second, ra.getSecond());
   }

}
