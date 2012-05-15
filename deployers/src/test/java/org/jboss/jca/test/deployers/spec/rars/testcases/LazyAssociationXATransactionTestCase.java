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

import org.jboss.jca.embedded.dsl.InputStreamDescriptor;
import org.jboss.jca.test.deployers.spec.ArquillianJCATestUtils;
import org.jboss.jca.test.deployers.spec.rars.lazy.LazyConnection;
import org.jboss.jca.test.deployers.spec.rars.lazy.LazyConnectionFactory;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for deploying a lazy association resource adapter archive using XATransaction
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
public class LazyAssociationXATransactionTestCase
{
   //-------------------------------------------------------------------------------------||
   //---------------------- GIVEN --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      String archiveName = "lazy.rar";
      String packageName = "org.jboss.jca.test.deployers.spec.rars.lazy";
      ResourceAdapterArchive raa = ArquillianJCATestUtils.buidShrinkwrapRa(archiveName, packageName);
      raa.addAsManifestResource(archiveName + "/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("lazy-xatx-ra.xml", 
                                                            cl.getResourceAsStream("lazy-xatx-ra.xml"));
      return isd;
   }

   //-------------------------------------------------------------------------------------||
   //---------------------- WHEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   //
   @Resource(mappedName = "java:/eis/LazyConnectionFactory")
   private LazyConnectionFactory connectionFactory;

   @Resource(mappedName = "java:/UserTransaction")
   private UserTransaction userTransaction;

   //-------------------------------------------------------------------------------------||
   //---------------------- THEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      assertNotNull(connectionFactory);
      assertNotNull(userTransaction);

      boolean status = true;
      userTransaction.begin();

      LazyConnection lc = null;
      try
      {
         lc = connectionFactory.getConnection();

         assertTrue(lc.isManagedConnectionSet());

         lc.closeManagedConnection();

         assertFalse(lc.isManagedConnectionSet());

         lc.associate();

         assertTrue(lc.isManagedConnectionSet());
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         status = false;
         fail("Throwable:" + t.getMessage());
      }
      finally
      {
         if (lc != null)
            lc.close();

         if (status)
         {
            userTransaction.commit();
         }
         else
         {
            userTransaction.rollback();
         }
      }
   }

   /**
    * Two connections - one managed connection
    * @exception Throwable Thrown if case of an error
    */
   @Ignore
   public void testTwoConnections() throws Throwable
   {
      assertNotNull(connectionFactory);
      assertNotNull(userTransaction);

      boolean status = true;
      userTransaction.begin();

      LazyConnection lc1 = null;
      LazyConnection lc2 = null;
      try
      {
         lc1 = connectionFactory.getConnection();

         assertTrue(lc1.isManagedConnectionSet());

         lc2 = connectionFactory.getConnection();

         assertTrue(lc2.isManagedConnectionSet());
         assertFalse(lc1.isManagedConnectionSet());

         assertTrue(lc2.closeManagedConnection());

         assertFalse(lc1.isManagedConnectionSet());
         assertFalse(lc2.isManagedConnectionSet());

         assertTrue(lc1.associate());

         assertTrue(lc1.isManagedConnectionSet());
         assertFalse(lc2.isManagedConnectionSet());
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         status = false;
         fail("Throwable:" + t.getMessage());
      }
      finally
      {
         if (lc1 != null)
            lc1.close();

         if (lc2 != null)
            lc2.close();

         if (status)
         {
            userTransaction.commit();
         }
         else
         {
            userTransaction.rollback();
         }
      }
   }
}
