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

import java.util.concurrent.CountDownLatch;

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
 * Test cases for deploying a lazy association resource adapter archive
 * using multiple threads (CCM)
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class LazyCCMNoTransactionTestCase
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyCCMNoTransactionTestCase.class);

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
      return ResourceAdapterFactory.createLazyDeployment(TransactionSupportLevel.NoTransaction);
   }

   @Resource(mappedName = "java:/eis/LazyConnectionFactory")
   private LazyConnectionFactory connectionFactory;

   @Resource(mappedName = "java:/UserTransaction")
   private UserTransaction userTransaction;

   /**
    * CCM
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testCCM() throws Throwable
   {
      assertNotNull(connectionFactory);

      CountDownLatch step1 = new CountDownLatch(1);
      CountDownLatch step2 = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(2);

      Worker primary = new Worker(connectionFactory, true, userTransaction, step1, step2, done);
      Thread t1 = new Thread(primary);
      t1.start();

      Worker secondary = new Worker(connectionFactory, false, userTransaction, step1, step2, done);
      Thread t2 = new Thread(secondary);
      t2.start();

      done.await();

      assertNull(primary.getThrowable());
      assertNull(secondary.getThrowable());
   }

   /**
    * Worker
    */
   private static class Worker implements Runnable
   {
      /** Throwable */
      private Throwable throwable;

      /** CF */
      private LazyConnectionFactory connectionFactory;

      /** Primary */
      private boolean primary;

      /** UserTransaction */
      private UserTransaction ut;

      /** Step1 */
      private CountDownLatch step1;

      /** Step2 */
      private CountDownLatch step2;

      /** Done */
      private CountDownLatch done;

      /**
       * Constructor
       * @param connectionFactory The CF
       * @param primary Primary worker
       * @param ut The user transaction
       * @param step1 Step 1 trigger
       * @param step2 Step 2 trigger
       * @param done Done trigger
       */
      public Worker(LazyConnectionFactory connectionFactory, boolean primary,
                    UserTransaction ut,
                    CountDownLatch step1, CountDownLatch step2, CountDownLatch done)
      {
         this.throwable = null;
         this.connectionFactory = connectionFactory;
         this.primary = primary;
         this.ut = ut;
         this.step1 = step1;
         this.step2 = step2;
         this.done = done;
      }

      /**
       * Get throwable
       * @return The value
       */
      public Throwable getThrowable()
      {
         return throwable;
      }

      /**
       * {@inheritDoc}
       */
      public void run()
      {
         LazyConnection lc = null;
         try
         {
            if (!primary)
            {
               step1.await();
               log.info("Step2");
            }
            else
            {
               log.info("Step1");
            }

            lc = connectionFactory.getConnection();
            
            ut.begin();

            assertTrue(lc.isManagedConnectionSet());

            if (primary)
            {
               step1.countDown();
               step2.await();
               log.info("Step3");
            }
            else
            {
               assertTrue(lc.closeManagedConnection());
               step2.countDown();
            }

            assertFalse(lc.isManagedConnectionSet());

            if (primary)
            {
               assertTrue(lc.associate());
               assertTrue(lc.isManagedConnectionSet());
            }
         }
         catch (Throwable t)
         {
            t.printStackTrace();
            throwable = t;
         }
         finally
         {
            if (lc != null)
               lc.close();

            try
            {
               ut.commit();
            }
            catch (Throwable t)
            {
               throwable = t;
            }
            
            done.countDown();
         }
      }
   }
}
