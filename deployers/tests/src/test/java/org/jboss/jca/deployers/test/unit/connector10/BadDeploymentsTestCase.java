/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

import org.jboss.jca.deployers.test.DeploymentTestBase;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnection;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactoryImpl;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionImpl;
import org.jboss.jca.deployers.test.rars.inout.SimpleManagedConnectionFactory;
import org.jboss.jca.embedded.Embedded;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for deploying bad resource adapter archives
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class BadDeploymentsTestCase extends DeploymentTestBase
{

   /**
    * Embedded
    */
   @ArquillianResource
   private Embedded embedded;

   /**
    * 
    * Creates archive with strict defined classes
    * 
    * @param archiveName to build
    * @return archive
    * @throws Exception in case of error
    */
   public ResourceAdapterArchive createStrictDeployment(String archiveName) throws Exception
   {
      ResourceAdapterArchive rar = buidShrinkwrapRa(archiveName, SimpleConnection.class, SimpleConnectionImpl.class,
         SimpleConnectionFactory.class, SimpleConnectionFactoryImpl.class, SimpleManagedConnectionFactory.class);
      addRaXml(rar);
      return rar;
   }

   /**
    * Deploys .rar archive
    * 
    * @param raa - archive
    * @param d - activation descriptor
    * @throws Exception in case of error
    */
   public void testDeployment(ResourceAdapterArchive raa, Descriptor d) throws Exception
   {
      log.info("///////BeforeDeployment");

      try
      {

         embedded.deploy(raa);
         if (d != null)
            embedded.deploy(d);
         log.info("///////AfterDeployment");

      }
      catch (Throwable t)
      {
         t.printStackTrace();
         throw new Exception(t);
      }
      finally
      {
         if (d != null)
            try
            {
               embedded.undeploy(d);
            }
            catch (Throwable t1)
            {
               // Ignore
            }

         try
         {
            embedded.undeploy(raa);
         }
         catch (Throwable t2)
         {
            // Ignore
         }
      }

   }

   /**
    * 
    * test archive
    * 
    * @param name of archive
    * @throws Exception in case of error
    */
   public void testArchive(String name) throws Exception
   {
      testArchiveWithIjAndRa(name, null, null);
   }

   /**
    * 
    * test strict archive
    * 
    * @param name of archive
    * @throws Exception in case of error
    */
   public void testStrictArchive(String name) throws Exception
   {
      testDeployment(createStrictDeployment(name + ".rar"), null);
   }

   /**
    * 
    * test archive with ironjacamar and -ra.xml descriptor activation
    * 
    * @param name of archive
    * @param ijPath path to ironjacamar file
    * @param raPath path to -ra.xml file
    * @throws Exception in case of error
    */
   public void testArchiveWithIjAndRa(String name, String ijPath, String raPath) throws Exception
   {
      ResourceAdapterArchive raa = createDeployment(name + ".rar");
      Descriptor d = null;
      if (ijPath != null)
         raa.addAsManifestResource(ijPath, "ironjacamar.xml");
      if (raPath != null)
         d = createDescriptor(raPath);
      testDeployment(raa, d);
   }

   /**
    * 
    * testRaWithWrongProperty
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaWithWrongProperty() throws Exception
   {
      testArchive("ra10dtdoutwrongproperty");
   }

   /**
    * 
    * testRaWithWrongPropertyType
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaWithWrongPropertyType() throws Exception
   {
      testArchive("ra10dtdoutwrongpropertytype");
   }

   /**
    * 
    * testRaWithWrongPropertyValue
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaWithWrongPropertyValue() throws Exception
   {
      testArchive("ra10dtdoutwrongpropertyvalue");
   }

   /**
    * 
    * testCFWithoutDefConstructor
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testCFWithoutDefConstructor() throws Exception
   {
      testArchive("ra10dtdoutwrongmcf1");
   }

   /**
    * 
    * testMCFWithoutEqualsMethod
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testMCFWithoutEqualsMethod() throws Exception
   {
      testArchive("ra10dtdoutwrongmcf2");
   }

   /**
    * 
    * testMCFWithoutHashCodeMethod
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testMCFWithoutHashCodeMethod() throws Exception
   {
      testArchive("ra10dtdoutwrongmcf3");
   }

   /**
    * 
    * testRaWithWrongConnectionFactoryType
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaWithWrongConnectionFactoryType() throws Exception
   {
      testArchive("ra10dtdoutwrongconnectionfactorytype");
   }

   /**
    * 
    * testRaWithWrongConnectionFactoryImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaWithWrongConnectionFactoryImpl() throws Exception
   {
      testArchive("ra10dtdoutwrongconnectionfactoryimpl");
   }

   /**
    * 
    * testRaWithWrongConnectionType
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaWithWrongConnectionType() throws Exception
   {
      testArchive("ra10dtdoutwrongconnectiontype");
   }

   /**
    * 
    * testRaWithWrongConnectionImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaWithWrongConnectionImpl() throws Exception
   {
      testArchive("ra10dtdoutwrongconnectionimpl");
   }

   /**
    * 
    * testRaLackMcf
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaLackMcf() throws Exception
   {
      testArchive("ra10dtdoutlackmcf");
   }

   /**
    * 
    * testRaLackConnectionFactoryType
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaLackConnectionFactoryType() throws Exception
   {
      testArchive("ra10dtdoutlackconnectionfactorytype");
   }

   /**
    * 
    * testRaLackConnectionFactoryImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaLackConnectionFactoryImpl() throws Exception
   {
      testArchive("ra10dtdoutlackconnectionfactoryimpl");
   }

   /**
    * 
    * testRaLackConnectionType
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaLackConnectionType() throws Exception
   {
      testArchive("ra10dtdoutlackconnectiontype");
   }

   /**
    * 
    * testRaLackConnectionImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testRaLackConnectionImpl() throws Exception
   {
      testArchive("ra10dtdoutlackconnectionimpl");
   }

   /**
    * 
    * testRaWithWrongConnectionFactoryTypeAndImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   @Ignore
   public void testRaWithWrongConnectionFactoryTypeAndImpl() throws Exception
   {
      testArchive("ra10dtdoutwrongconnectionfactorytypeandimpl");
   }

   /**
    * 
    * testRaWithWrongConnectionTypeAndImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   @Ignore
   public void testRaWithWrongConnectionTypeAndImpl() throws Exception
   {
      testArchive("ra10dtdoutwrongconnectiontypeandimpl");
   }

   /**
    * 
    * testStrictRaWrongMCF
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   @Ignore
   public void testStrictRaWrongMCF() throws Exception
   {
      testStrictArchive("ra10dtdoutwrongmcf");
   }

   /**
    * 
    * testStrictRaAbsolutelyWrong
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   @Ignore
   public void testStrictRaAbsolutelyWrong() throws Exception
   {
      testStrictArchive("ra10dtdoutoverwrite");
   }

   /**
    * 
    * testStrictRaWithWrongConnectionFactoryType
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testStrictRaWithWrongConnectionFactoryType() throws Exception
   {
      testStrictArchive("ra10dtdoutwrongconnectionfactorytype");
   }

   /**
    * 
    * testStrictRaWithWrongConnectionFactoryImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testStrictRaWithWrongConnectionFactoryImpl() throws Exception
   {
      testStrictArchive("ra10dtdoutwrongconnectionfactoryimpl");
   }

   /**
    * 
    * testStrictRaWithWrongConnectionType
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testStrictRaWithWrongConnectionType() throws Exception
   {
      testStrictArchive("ra10dtdoutwrongconnectiontype");
   }

   /**
    * 
    * testStrictRaWithWrongConnectionImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testStrictRaWithWrongConnectionImpl() throws Exception
   {
      testStrictArchive("ra10dtdoutwrongconnectionimpl");
   }

   /**
    * 
    * testStrictRaWithWrongConnectionFactoryTypeAndImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   @Ignore
   public void testStrictRaWithWrongConnectionFactoryTypeAndImpl() throws Exception
   {
      testStrictArchive("ra10dtdoutwrongconnectionfactorytypeandimpl");
   }

   /**
    * 
    * testStrictRaWithWrongConnectionTypeAndImpl
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   @Ignore
   public void testStrictRaWithWrongConnectionTypeAndImpl() throws Exception
   {
      testStrictArchive("ra10dtdoutwrongconnectiontypeandimpl");
   }

   /**
    * 
    * testIjWithWrongProperty
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testIjWithWrongProperty() throws Exception
   {
      testArchiveWithIjAndRa("ra10dtdout", "ra10dtdoutwrongproperty.rar/META-INF/ironjacamar.xml", null);
   }

   /**
    * 
    * testIjWithWrongPropertyValue
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testIjWithWrongPropertyValue() throws Exception
   {
      testArchiveWithIjAndRa("ra10dtdoutoverwrite", "ra10dtdoutwrongpropertyvalue.rar/META-INF/ironjacamar.xml", null);
   }

   /**
    * 
    * testActivationWithWrongProperty
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testActivationWithWrongProperty() throws Exception
   {
      testArchiveWithIjAndRa("ra10dtdout", null, "ra10dtdoutwrongproperty.rar/rar-ra.xml");
   }

   /**
    * 
    * testActivationWithWrongPropertyValue
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testActivationWithWrongPropertyValue() throws Exception
   {
      testArchiveWithIjAndRa("ra10dtdoutoverwrite", null, "ra10dtdoutwrongpropertyvalue.rar/rar-ra.xml");
   }

   /**
    * 
    * testActivationIJWithWrongProperty
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testActivationIJWithWrongProperty() throws Exception
   {
      testArchiveWithIjAndRa("ra10dtdout", "ra10dtdout.rar/META-INF/ironjacamar.xml",
         "ra10dtdoutwrongproperty.rar/rar-ra.xml");
   }

   /**
    * 
    * testActivationIJWithWrongPropertyValue
    * 
    * @throws Exception in case of error
    */
   @Test(expected = Exception.class)
   public void testActivationIJWithWrongPropertyValue() throws Exception
   {
      testArchiveWithIjAndRa("ra10dtdoutoverwrite", "ra10dtdoutoverwrite.rar/META-INF/ironjacamar.xml",
         "ra10dtdoutwrongpropertyvalue.rar/rar-ra.xml");
   }
}
