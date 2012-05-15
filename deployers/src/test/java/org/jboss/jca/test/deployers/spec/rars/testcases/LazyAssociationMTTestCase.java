/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test cases for deploying a lazy association resource adapter archive
 * using multiple threads
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
public class LazyAssociationMTTestCase
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyAssociationMTTestCase.class);

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
      InputStreamDescriptor isd = new InputStreamDescriptor("lazy-notx-ra.xml", 
                                                            cl.getResourceAsStream("lazy-notx-ra.xml"));
      return isd;
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
    * Two connections - one managed connection
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testTwoConnections() throws Throwable
   {
      assertNotNull(connectionFactory);

      CountDownLatch step1 = new CountDownLatch(1);
      CountDownLatch step2 = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(2);

      Worker primary = new Worker(connectionFactory, true, step1, step2, done);
      Thread t1 = new Thread(primary);
      t1.start();

      Worker secondary = new Worker(connectionFactory, false, step1, step2, done);
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
       * @param step1 Step 1 trigger
       * @param step2 Step 2 trigger
       * @param done Done trigger
       */
      public Worker(LazyConnectionFactory connectionFactory, boolean primary,
                    CountDownLatch step1, CountDownLatch step2, CountDownLatch done)
      {
         this.throwable = null;
         this.connectionFactory = connectionFactory;
         this.primary = primary;
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

            done.countDown();
         }
      }
   }
}
