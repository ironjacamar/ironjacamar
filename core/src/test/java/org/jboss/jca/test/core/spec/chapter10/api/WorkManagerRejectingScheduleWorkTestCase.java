/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.core.spec.chapter10.api;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.test.core.spec.chapter10.common.MyWorkAdapter;
import org.jboss.jca.test.core.spec.chapter10.common.ShortRunningWork;

import java.net.URL;

import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkManagerRejectingScheduleWorkTestCase.
 * 
 * Tests for rejecting work instance to the WorkManager scheduleWork() methods
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class WorkManagerRejectingScheduleWorkTestCase
{
   /*
    * Embedded
    */
   private static Embedded embedded;
   
   /**
    * scheduleWork method
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testScheduleWork() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();

      workManager.scheduleWork(work);
   }
   
   /**
    * scheduleWork method (full signature)
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testScheduleWorkFullSignature() throws Throwable
   {
      WorkManager workManager = embedded.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      MyWorkAdapter wa = new MyWorkAdapter();
      
      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, wa);
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
         WorkManagerRejectingScheduleWorkTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         WorkManagerRejectingScheduleWorkTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         WorkManagerRejectingScheduleWorkTestCase.class.getClassLoader().
         getResource("rejecting-workmanager.xml");

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
         WorkManagerRejectingScheduleWorkTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         WorkManagerRejectingScheduleWorkTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         WorkManagerRejectingScheduleWorkTestCase.class.getClassLoader().
         getResource("rejecting-workmanager.xml");

      embedded.undeploy(wm);
      embedded.undeploy(transaction);
      embedded.undeploy(naming);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
