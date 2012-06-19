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

import org.jboss.jca.core.workmanager.spec.chapter11.common.DuplicateTransactionContextWork;
import org.jboss.jca.core.workmanager.spec.chapter11.common.TransactionContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.TransactionContextWork;
import org.jboss.jca.embedded.arquillian.Inject;

import javax.resource.spi.work.WorkContextErrorCodes;
import javax.resource.spi.work.WorkManager;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * WorkContextSetupListenerTest
 *
 * @version $Rev$ $Date$
 * @author gurkanerdogdu
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(Arquillian.class)
public class WorkContextSetupListenerTestCase
{
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
      manager.doWork(new TransactionContextWork(), WorkManager.INDEFINITE, null, null);

      String errorCode = TransactionContextCustom.getContextSetupFailedErrorCode();
      boolean complete = TransactionContextCustom.isContextSetupComplete();

      assertEquals("", errorCode);
      assertTrue(complete);
   }

   /**
    * Test WorkContextLifecycleListener for transaction context.
    *
    * @throws Throwable throws any error
    */
   @Test
   public void testTransactionContextFailedListener() throws Throwable
   {
      try
      {
         manager.doWork(new DuplicateTransactionContextWork(), WorkManager.INDEFINITE, null, null);
      }
      catch (Throwable e)
      {
         //Swallow
      }

      String errorCode = TransactionContextCustom.getContextSetupFailedErrorCode();
      boolean complete = TransactionContextCustom.isContextSetupComplete();

      assertEquals(WorkContextErrorCodes.DUPLICATE_CONTEXTS, errorCode);
      assertFalse(complete);
   }
}
