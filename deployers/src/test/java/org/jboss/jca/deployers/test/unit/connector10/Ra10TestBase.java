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

package org.jboss.jca.deployers.test.unit.connector10;

import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.common.metadata.ra.ra10.Connector10Impl;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.deployers.test.DeploymentTestBase;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnection;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnection1;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory1;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactoryImpl;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactoryImpl1;
import org.jboss.jca.deployers.test.rars.inout.SimpleManagedConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleManagedConnectionFactory1;

import java.util.Iterator;
import java.util.Set;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.*;

/**
 * Parent of test cases in this package
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */

public abstract class Ra10TestBase extends DeploymentTestBase
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
    * MDR
    */
   @Inject(name = "MDR")
   private MetadataRepository mdr;

   /**
    * 
    * Tests connection factory and properties of ManagedConnectionFactory
    * 
    * @param connectionFactory to test
    * @param first property value
    * @param second property value
    * @throws Exception in case of error
    */
   public void testBasic(SimpleConnectionFactory connectionFactory, String first, char second) throws Exception
   {
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
         SimpleManagedConnectionFactory mcf = (SimpleManagedConnectionFactory) cf.getMCF();
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

   }

   /**
    * 
    * Tests connection factory and properties of ManagedConnectionFactory
    * 
    * @param connectionFactory to test
    * @param first property value
    * @param second property value
    * @throws Exception in case of error
    */
   public void testBasic1(SimpleConnectionFactory1 connectionFactory, String first, byte second) throws Exception
   {
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
         SimpleManagedConnectionFactory1 mcf = (SimpleManagedConnectionFactory1) cf.getMCF();
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
