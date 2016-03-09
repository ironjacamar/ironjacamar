/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.connectionmanager.lazy;

import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.lazy.LazyConnection;
import org.ironjacamar.rars.lazy.LazyConnectionFactory;

import javax.annotation.Resource;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for deploying a lazy association resource adapter archive using XATransaction
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class LazyXATransactionTestCase
{
   private static Logger log = Logger.getLogger(LazyXATransactionTestCase.class);

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      return ResourceAdapterFactory.createLazyRar();
   }

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor() throws Exception
   {
      return ResourceAdapterFactory.createLazyDeployment(TransactionSupportLevel.XATransaction);
   }

   @Resource(mappedName = "java:/eis/LazyConnectionFactory")
   private LazyConnectionFactory connectionFactory;

   @Resource(mappedName = "java:/UserTransaction")
   private UserTransaction userTransaction;

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
      }
   }

   /**
    * Two connections - one managed connection - without enlistment
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testTwoConnectionsWithoutEnlistment() throws Throwable
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
      }
   }

   /**
    * Two connections - one managed connection - with enlistment
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testTwoConnectionsWithEnlistment() throws Throwable
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
      }
   }
}
