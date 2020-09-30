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
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for deploying a lazy association resource adapter archive using LocalTransaction
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LazyAssociationLocalTransactionTestCase extends LazyTestBase
{
   private static Logger log = Logger.getLogger(LazyAssociationLocalTransactionTestCase.class);

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
      return createDescriptor("lazy.rar/lazy-localtx-ra.xml");
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
      log.infof("testBasic: Start");

      assertNotNull(connectionFactory);
      assertNotNull(userTransaction);

      boolean status = true;
      userTransaction.begin();

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
         assertTrue(lc.isEnlisted());
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
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

         log.infof("testBasic: End");
      }
   }

   /**
    * Enlistment without transaction.
    *
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testEnlistmentWithoutTransaction() throws Throwable
   {
      log.infof("testEnlistmentWithoutTransaction: Start");

      assertNotNull(connectionFactory);
      assertNotNull(userTransaction);

      boolean status = true;


      LazyConnection lc1 = null;
      try
      {
         lc1 = connectionFactory.getConnection();

         assertTrue(lc1.isManagedConnectionSet());

         assertTrue(lc1.closeManagedConnection());

         assertFalse(lc1.isManagedConnectionSet());

         assertTrue(lc1.associate());

         assertTrue(lc1.isManagedConnectionSet());

         assertFalse(lc1.isEnlisted());
         assertTrue(lc1.enlist());
         assertFalse(lc1.isEnlisted());

         userTransaction.begin();
         assertTrue(lc1.enlist());
         assertTrue(lc1.isEnlisted());

      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         status = false;
         fail("Throwable:" + t.getMessage());
      }
      finally
      {
         if (status)
         {
            userTransaction.commit();
         }
         else
         {
            if (lc1 != null)
               lc1.close();
            userTransaction.rollback();
         }
      }

      // attempt to enlist after transaction commit
      boolean fail = true;
      try
      {
         lc1.enlist();
      }
      catch (RuntimeException expected)
      {
         fail = false;
      }
      finally
      {
         lc1.close();
      }
      assertFalse("Enlist should had thrown some exception, as we are not interleaving, and the transaction is closed",
            fail);

      // a new connection from the same cf should work just fine
      LazyConnection lc2 = null;
      try
      {
         lc2 = connectionFactory.getConnection();

         assertTrue(lc2.isManagedConnectionSet());

         assertFalse(lc2.isEnlisted());
         assertTrue(lc2.enlist());
         assertFalse(lc2.isEnlisted());
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         status = false;
         fail("Throwable:" + t.getMessage());
      }
      finally
      {
         if (lc2 != null)
            lc2.close();

         log.infof("testEnlistmentWithoutTransaction: End");
      }
   }

   /**
    * Two connections - one managed connection - without enlistment
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testTwoConnectionsWithoutEnlistment() throws Throwable
   {
      log.infof("testTwoConnectionsWithoutEnlistment: Start");

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

         log.infof("testTwoConnectionsWithoutEnlistment: Before 2nd getConnection");

         lc2 = connectionFactory.getConnection();

         assertTrue(lc2.isManagedConnectionSet());
         assertFalse(lc1.isManagedConnectionSet());

         log.infof("testTwoConnectionsWithoutEnlistment: Before closeManagedConnection");

         assertTrue(lc2.closeManagedConnection());

         assertFalse(lc1.isManagedConnectionSet());
         assertFalse(lc2.isManagedConnectionSet());

         log.infof("testTwoConnectionsWithoutEnlistment: Before associate");

         assertTrue(lc1.associate());

         assertTrue(lc1.isManagedConnectionSet());
         assertFalse(lc2.isManagedConnectionSet());

         log.infof("testTwoConnectionsWithoutEnlistment: After associate");
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
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

         log.infof("testTwoConnectionsWithoutEnlistment: End");
      }
   }

   /**
    * Two connections - one managed connection - with enlistment
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testTwoConnectionsWithEnlistment() throws Throwable
   {
      log.infof("testTwoConnectionsWithEnlistment: Start");

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
         assertFalse(lc1.isEnlisted());
         assertTrue(lc1.enlist());
         assertTrue(lc1.isEnlisted());

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

         log.infof("testTwoConnectionsWithEnlistment: End");
      }
   }
}
