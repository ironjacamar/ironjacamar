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
package org.jboss.jca.core.workmanager.spec.chapter10.api;

import org.jboss.jca.core.workmanager.spec.chapter10.common.SimpleWork;
import org.jboss.jca.embedded.arquillian.Inject;

import java.util.Timer;
import java.util.TimerTask;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * PeriodicExecutionTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.7
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class PeriodicExecutionTestCase
{
   /**
    * Injecting default bootstrap context
    */
   @Inject(name = "DefaultBootstrapContext")
   BootstrapContext bootstrapContext;

   /**
    * Test for paragraph 1
    * A resource adapter may need to periodically execute Work instances.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testPeriodicExecution() throws Throwable
   {
      assertNotNull(bootstrapContext);
      assertNotNull(bootstrapContext.getWorkManager());
      assertTrue(bootstrapContext.getWorkManager() instanceof WorkManager);
      try
      {
         Timer timer = bootstrapContext.createTimer();
         assertNotNull(timer);
         assertFalse(timer == bootstrapContext.createTimer());

         final WorkManager workManager = bootstrapContext.getWorkManager();
         final SimpleWork work = new SimpleWork();

         timer.schedule(new TimerTask()
         {
            public void run()
            {
               try
               {
                  workManager.scheduleWork(work);
               }
               catch (WorkException we)
               {
                  //Expected
               }
            }
         }, 0, 500);
         Thread.sleep(2000);
         work.setThrowWorkAException(true);
         assertTrue("work should start periodically, runs counted:" + work.getCounter(), work.getCounter() > 1);
      }
      catch (UnavailableException e)
      {
         //That's OK
      }
      catch (UnsupportedOperationException e)
      {
         //That's OK
      }
      catch (Throwable t)
      {
         fail(t.getMessage());
      }
   }
}
