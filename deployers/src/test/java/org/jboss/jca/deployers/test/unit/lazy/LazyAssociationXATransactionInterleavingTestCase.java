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

package org.jboss.jca.deployers.test.unit.lazy;

import org.jboss.jca.deployers.test.rars.lazy.LazyConnection;
import org.jboss.jca.deployers.test.rars.lazy.LazyConnectionFactory;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for deploying a lazy association resource adapter archive using XATransaction and
 * interleaving
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LazyAssociationXATransactionInterleavingTestCase extends LazyTestBase
{
   //-------------------------------------------------------------------------------------||
   //---------------------- GIVEN --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor() throws Exception
   {
      return createDescriptor("lazy.rar/lazy-xatx-interleaving-ra.xml");
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

         assertFalse(lc.isEnlisted());
         assertTrue(lc.enlist());
         assertTrue(lc.isEnlisted());
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

      userTransaction.begin();

      LazyConnection lc1 = null;
      LazyConnection lc2 = null;
      try
      {
         lc1 = connectionFactory.getConnection();

         assertTrue(lc1.isManagedConnectionSet());

         lc2 = connectionFactory.getConnection();

         fail("Got two connections");
      }
      catch (Throwable t)
      {
         // Ok
      }
      finally
      {
         if (lc1 != null)
            lc1.close();

         if (lc2 != null)
            lc2.close();

         userTransaction.rollback();
      }
   }
}
