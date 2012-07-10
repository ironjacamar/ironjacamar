/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.core.spec.chapter10.section3;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.test.core.spec.chapter10.common.LongRunningWork;
import org.jboss.jca.test.core.spec.chapter10.common.ShortRunningWork;
import org.jboss.jca.test.core.spec.chapter10.common.SynchronizedWork;
import org.jboss.jca.test.core.spec.chapter10.common.TestWorkException;
import org.jboss.jca.test.core.spec.chapter10.common.UnsynchronizedWork;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.2
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkInterfaceTestCase
{
   /*
    * Embedded
    */
   private static Embedded embedded;
   
   /**
    * Test for paragraph 2
    * The WorkManager dispatches a thread that calls the run method to
    *             begin execution of a Work instance.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCallRunMethod() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);
      ShortRunningWork work = new ShortRunningWork();
      
      assertFalse(work.hasCallRun());
      workManager.doWork(work);
      assertTrue(work.hasCallRun());
   }

   /**
    * Test for paragraph 2
    * The WorkManager must catch any exception thrown during Work processing,
    *             which includes execution context setup, and wrap it with a 
    *             WorkCompletedException set to an appropriate error code, 
    *             which indicates the nature of the error condition.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCatchAllExceptionAroundRun() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      work.setThrowWorkException(true);
      
      try
      {
         workManager.doWork(work);
         fail("should throw WorkAException");
      } 
      catch (WorkException e)
      {
         assertNotNull(e);
         assertTrue(e instanceof WorkCompletedException);
         assertTrue(e.getCause() instanceof TestWorkException);
      }
   }
   
   /**
    * Test for paragraph 3
    * The WorkManager may call the release method to request the active Work 
    *            instance to complete execution as soon as possible. 
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCallReleaseMethod() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);
      
      ShortRunningWork shortWork = new ShortRunningWork();
      assertFalse(shortWork.getWasReleased());
      workManager.doWork(shortWork);
      assertTrue(shortWork.getWasReleased());
   }
   
   /**
    * Test for paragraph 3
    * This would be called on a separate thread than the one currently executing the Work instance.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCallReleaseWithOtherThread() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork longWork = new LongRunningWork(start, done);
      workManager.startWork(longWork);
      long currentThread = Thread.currentThread().getId();
      //TODO we should impl call release()
      //assertNotSame(currentThread, longWork.getReleaseThread())
   }
   
   /**
    * Test for paragraph 3
    * Since this method call causes the Work instance to be simultaneously acted upon
    *            by multiple threads, the Work instance implementation must be 
    *            thread-safe, and this method must be re-entrant.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testWorkInstanceThreadSafeAndReentrant() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCannotDeclaredSynchronizedSynchronizedWork() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);

      try
      {
         SynchronizedWork sw = new SynchronizedWork();
         workManager.doWork(sw);
         fail("Synchronized methods not catched");
      }
      catch (WorkException we)
      {
         // Expected
      }
   }
   
   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCannotDeclaredSynchronizedUnsynchronizedWork() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);

      UnsynchronizedWork usw = new UnsynchronizedWork();
      workManager.doWork(usw);
   }
   
   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create(false);

      // Startup
      embedded.startup();

      // Deploy Naming, Transaction and WorkManager
      URL naming =
         WorkInterfaceTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         WorkInterfaceTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         WorkInterfaceTestCase.class.getClassLoader().getResource("workmanager.xml");

      embedded.deploy(naming);
      embedded.deploy(transaction);
      embedded.deploy(wm);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy WorkManager, Transaction and Naming
      URL naming =
         WorkInterfaceTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         WorkInterfaceTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         WorkInterfaceTestCase.class.getClassLoader().getResource("workmanager.xml");

      embedded.undeploy(wm);
      embedded.undeploy(transaction);
      embedded.undeploy(naming);

      // Shutdown MC
      embedded.shutdown();

      // Set Embedded to null
      embedded = null;
   }
}

