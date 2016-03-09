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
 * using multiple threads (LocalTransaction)
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class LazyLocalTransactionMTTestCase
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyLocalTransactionMTTestCase.class);

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
      return ResourceAdapterFactory.createLazyDeployment(TransactionSupportLevel.LocalTransaction);
   }

   @Resource(mappedName = "java:/eis/LazyConnectionFactory")
   private LazyConnectionFactory connectionFactory;

   @Resource(mappedName = "java:/UserTransaction")
   private UserTransaction userTransaction;

   /**
    * Two connections - one managed connection - without enlistment
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testTwoConnectionsWithoutEnlistment() throws Throwable
   {
      assertNotNull(connectionFactory);
      assertNotNull(userTransaction);

      CountDownLatch step1 = new CountDownLatch(1);
      CountDownLatch step2 = new CountDownLatch(1);
      CountDownLatch step3 = new CountDownLatch(1);
      CountDownLatch step4 = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(2);

      Worker primary = new Worker(connectionFactory, userTransaction, false, true, step1, step2, step3, step4, done);
      Thread t1 = new Thread(primary);
      t1.start();

      Worker secondary = new Worker(connectionFactory, userTransaction, false, false, step1, step2, step3, step4, done);
      Thread t2 = new Thread(secondary);
      t2.start();

      done.await();

      assertNull(primary.getThrowable());
      assertNull(secondary.getThrowable());
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

      CountDownLatch step1 = new CountDownLatch(1);
      CountDownLatch step2 = new CountDownLatch(1);
      CountDownLatch step3 = new CountDownLatch(1);
      CountDownLatch step4 = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(2);

      Worker primary = new Worker(connectionFactory, userTransaction, true, true, step1, step2, step3, step4, done);
      Thread t1 = new Thread(primary);
      t1.start();

      Worker secondary = new Worker(connectionFactory, userTransaction, true, false, step1, step2, step3, step4, done);
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

      /** UT */
      private UserTransaction userTransaction;

      /** Enlist */
      private boolean enlist;

      /** Primary */
      private boolean primary;

      /** Step1 */
      private CountDownLatch step1;

      /** Step2 */
      private CountDownLatch step2;

      /** Step3 */
      private CountDownLatch step3;

      /** Step4 */
      private CountDownLatch step4;

      /** Done */
      private CountDownLatch done;

      /**
       * Constructor
       * @param connectionFactory The CF
       * @param userTransaction The UT
       * @param enlist Enlistment
       * @param primary Primary worker
       * @param step1 Step 1 trigger
       * @param step2 Step 2 trigger
       * @param step3 Step 3 trigger
       * @param step4 Step 4 trigger
       * @param done Done trigger
       */
      public Worker(LazyConnectionFactory connectionFactory, UserTransaction userTransaction,
                    boolean enlist, boolean primary,
                    CountDownLatch step1, CountDownLatch step2, CountDownLatch step3, CountDownLatch step4,
                    CountDownLatch done)
      {
         this.throwable = null;
         this.connectionFactory = connectionFactory;
         this.userTransaction = userTransaction;
         this.enlist = enlist;
         this.primary = primary;
         this.step1 = step1;
         this.step2 = step2;
         this.step3 = step3;
         this.step4 = step4;
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
         boolean status = true;
         try
         {
            userTransaction.begin();

            if (!primary)
            {
               step1.await();
               log.info("Step2");
            }
            else
            {
               log.info("Step1");
            }

            if (primary)
            {
               lc = connectionFactory.getConnection();
               assertTrue(lc.isManagedConnectionSet());

               step1.countDown();
               step2.await();
               log.info("Step3");
            }
            else
            {
               try
               {
                  lc = connectionFactory.getConnection();
                  fail("Got a connection");
               }
               catch (Exception e)
               {
                  step2.countDown();
               }
            }

            if (primary)
            {
               if (enlist)
               {
                  assertFalse(lc.isEnlisted());
                  assertTrue(lc.enlist());
                  assertTrue(lc.isEnlisted());
               }

               step3.countDown();
               step4.await();
            }
            else
            {
               step3.await();
               log.info("Step4");
               assertNull(lc);
               step4.countDown();
            }
         }
         catch (Exception e)
         {
            log.error(e.getMessage(), e);
            throwable = e;
            status = false;
         }
         finally
         {
            if (lc != null)
               lc.close();

            try
            {
               if (status)
               {
                  userTransaction.commit();
               }
               else
               {
                  userTransaction.rollback();
               }
            }
            catch (Exception e)
            {
               log.error(e.getMessage(), e);
               throwable = e;
            }

            done.countDown();
         }
      }
   }
}
