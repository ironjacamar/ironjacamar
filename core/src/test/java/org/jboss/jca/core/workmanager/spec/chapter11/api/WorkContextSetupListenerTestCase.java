/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.spec.chapter11.api;

import org.jboss.jca.core.workmanager.spec.chapter11.common.ContextWorkAdapter;
import org.jboss.jca.core.workmanager.spec.chapter11.common.TransactionContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.UniversalProviderWork;
import org.jboss.jca.embedded.arquillian.Inject;

import javax.resource.spi.work.WorkContextErrorCodes;
import javax.resource.spi.work.WorkManager;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * WorkContextSetupListenerTest
 * 
 * The WorkManager must make the notifications related to Work accepted and started
 * events prior to calling the WorkContext setup related notifications. The WorkManager
 * must make the notifications related to the Work completed events after the WorkContext
 * setup related notifications.
 *
 * @version $Rev$ $Date$
 * @author gurkanerdogdu
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(Arquillian.class)
public class WorkContextSetupListenerTestCase
{
   private static final Logger LOG = Logger.getLogger(WorkContextSetupListenerTestCase.class);

   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager manager;

   /**
    * Test WorkContextLifecycleListener for transaction context.
    *
    * @throws Throwable throws any error
    */
   @Test
   public void testTransactionContextCustomListener() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      TransactionContextCustom listener = new TransactionContextCustom();
      work.addContext(listener);
      ContextWorkAdapter wa = new ContextWorkAdapter();
      manager.doWork(work, WorkManager.INDEFINITE, null, wa);

      assertEquals("", listener.getContextSetupFailedErrorCode());
      assertTrue(listener.isContextSetupComplete());

      LOG.info("1Test//accepted:" + wa.getTimeAccepted() + "//started:" + wa.getTimeStarted() + "//context:"
            + listener.getTimeStamp() + "//completed:" + wa.getTimeCompleted());

      assertTrue(wa.getTimeAccepted() > 0);
      assertTrue(wa.getTimeStarted() > 0);
      assertTrue(listener.getTimeStamp() > 0);
      assertTrue(wa.getTimeCompleted() > 0);

      assertTrue(wa.getTimeAccepted() <= wa.getTimeStarted());
      assertTrue(wa.getTimeStarted() <= listener.getTimeStamp());
      assertTrue(listener.getTimeStamp() <= wa.getTimeCompleted());
   }

   /**
    * Test WorkContextLifecycleListener for transaction context.
    *
    * @throws Throwable throws any error
    */
   @Test
   public void testTransactionContextFailedListener() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      TransactionContextCustom listener = new TransactionContextCustom();
      work.addContext(listener);
      work.addContext(listener); //to be sure, that listener will be fired 

      ContextWorkAdapter wa = new ContextWorkAdapter();

      try
      {
         manager.doWork(work, WorkManager.INDEFINITE, null, wa);
         fail("Exception expected");
      }
      catch (Throwable e)
      {
         //Expected
      }

      assertEquals(WorkContextErrorCodes.DUPLICATE_CONTEXTS, listener.getContextSetupFailedErrorCode());
      assertFalse(listener.isContextSetupComplete());

      LOG.info("2Test//accepted:" + wa.getTimeAccepted() + "//started:" + wa.getTimeStarted() + "//context:"
            + listener.getTimeStamp() + "//completed:" + wa.getTimeCompleted());

      assertTrue(wa.getTimeAccepted() > 0);
      assertTrue(wa.getTimeStarted() > 0);
      assertTrue(listener.getTimeStamp() > 0);
      assertTrue(wa.getTimeCompleted() > 0);

      assertTrue(wa.getTimeAccepted() <= wa.getTimeStarted());
      assertTrue(wa.getTimeStarted() <= listener.getTimeStamp());
      assertTrue(listener.getTimeStamp() <= wa.getTimeCompleted());
   }
}
