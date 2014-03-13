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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for deploying a lazy association resource adapter archive
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LazyAssociationTestCase extends LazyTestBase
{
   private static Logger log = Logger.getLogger(LazyAssociationTestCase.class);

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor() throws Exception
   {
      return createDescriptor("lazy.rar/lazy-notx-ra.xml");
   }

   //-------------------------------------------------------------------------------------||
   //---------------------- WHEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   //
   @Resource(mappedName = "java:/eis/LazyConnectionFactory")
   private LazyConnectionFactory connectionFactory;

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

      LazyConnection lc = null;
      try
      {
         lc = connectionFactory.getConnection();

         assertTrue(lc.isManagedConnectionSet());

         assertTrue(lc.closeManagedConnection());

         assertFalse(lc.isManagedConnectionSet());

         assertTrue(lc.associate());

         assertTrue(lc.isManagedConnectionSet());

         assertFalse(lc.isEnlisted());
         assertTrue(lc.enlist());
         assertFalse(lc.isEnlisted());
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);

         fail("Throwable:" + t.getMessage());
      }
      finally
      {
         if (lc != null)
            lc.close();
      }
   }

   /**
    * Two connections - one managed connection
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testTwoConnections() throws Throwable
   {
      assertNotNull(connectionFactory);

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
         log.error(t.getMessage(), t);

         fail("Throwable:" + t.getMessage());
      }
      finally
      {
         if (lc1 != null)
            lc1.close();

         if (lc2 != null)
            lc2.close();
      }
   }
}
