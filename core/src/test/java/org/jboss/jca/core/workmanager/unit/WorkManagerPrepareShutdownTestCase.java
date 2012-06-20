/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.unit;

import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.api.workmanager.WorkManagerStatistics;
import org.jboss.jca.core.workmanager.spec.chapter10.common.CallbackCount;
import org.jboss.jca.core.workmanager.spec.chapter10.common.MyWorkAdapter;
import org.jboss.jca.core.workmanager.spec.chapter10.common.SimpleWork;
import org.jboss.jca.embedded.arquillian.Inject;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Work manager test case for preparing shutdown
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(Arquillian.class)
public class WorkManagerPrepareShutdownTestCase
{

   private static final Logger LOG = Logger.getLogger(WorkManagerPrepareShutdownTestCase.class);

   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

   /**
    * Test graceful shutdown
    * @throws Throwable throwable exception 
    */
   @Test
   public void testPrepareShutdown() throws Throwable
   {
      WorkManagerStatistics stat = workManager.getStatistics();
      assertNotNull(stat);
      assertFalse(workManager.isShutdown());

      Work work = new SimpleWork();
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);
      
      workManager.prepareShutdown();
      assertTrue(workManager.isShutdown());

      try
      {
         workManager.doWork(work, WorkManager.INDEFINITE, null, wa);
         fail("exception should be thrown");
      }
      catch (WorkRejectedException e)
      {
         //Expected
      }
      try
      {
         workManager.startWork(work, WorkManager.INDEFINITE, null, wa);
         fail("exception should be thrown");
      }
      catch (WorkRejectedException e)
      {
         //Expected
      }
      try
      {
         workManager.scheduleWork(work, WorkManager.INDEFINITE, null, wa);
         fail("exception should be thrown");
      }
      catch (WorkRejectedException e)
      {
         //Expected
      }
      assertEquals("should be same", 3, callbackCount.getRejectedCount());
      assertEquals("should be same", 0, stat.getWorkActive());
      assertEquals("should be same", 0, stat.getDoWorkAccepted());
      assertEquals("should be same", 1, stat.getDoWorkRejected());
      assertEquals("should be same", 1, stat.getScheduleWorkRejected());
      assertEquals("should be same", 0, stat.getScheduleWorkAccepted());
      assertEquals("should be same", 0, stat.getStartWorkAccepted());
      assertEquals("should be same", 1, stat.getStartWorkRejected());
      LOG.info(stat.toString());

   }
}
