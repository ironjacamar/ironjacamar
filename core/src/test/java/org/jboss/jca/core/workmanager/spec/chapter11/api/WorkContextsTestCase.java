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

import org.jboss.jca.core.workmanager.spec.chapter11.common.HintsContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.SecurityContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.SecurityContextCustom2;
import org.jboss.jca.core.workmanager.spec.chapter11.common.TransactionContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.UniversalProviderWork;
import org.jboss.jca.core.workmanager.spec.chapter11.common.UnsupportedContext;
import org.jboss.jca.embedded.arquillian.Inject;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.threads.QueueExecutor;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * WorkContextsTestCase.
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class WorkContextsTestCase
{
   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager manager;

   /**
    * Injecting thread pool
    */
   @Inject(name = "LongRunningThreadPool")
   QueueExecutor executor;

   /**
    * Injecting default bootstrap context
    */
   @Inject(name = "DefaultBootstrapContext")
   BootstrapContext bootstrapContext;

   /**
    * Test api for {@link WorkContextProvider#getWorkContexts()}
    */
   @Test
   public void testGetWorkContextsNumber()
   {
      UniversalProviderWork work = new UniversalProviderWork();
      work.addContext(new TransactionContext());
      assertNotNull(work.getWorkContexts());
      assertEquals(1, work.getWorkContexts().size());
   }

   /**
    * Test whether or not work contains  both execution context and implement {@link WorkContextProvider}.
    * @throws Throwable if work contains both execution context and implement {@link WorkContextProvider}
    */
   @Test(expected = WorkRejectedException.class)
   public void testNotBothExecutionContext() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      work.addContext(new TransactionContext());
      manager.doWork(work, WorkManager.INDEFINITE, new ExecutionContext(), null);
   }

   /**
    * If the resource adapter returns a null or an empty List when the WorkManager
    * makes a call to the getWorkContexts method, the WorkManager must treat it as if no
    * additional execution contexts are associated with that Work instance and must
    * continue with the Work processing.
    * @throws Throwable - work exception
    */
   @Test
   public void testNoWorkContext() throws Throwable 
   {
      UniversalProviderWork work = new UniversalProviderWork();
      manager.doWork(work);
      assertTrue(work.isReleased());
      work.addContext(null);
      work.setReleased(false);
      manager.doWork(work);
      assertTrue(work.isReleased());
   }

   /**
    * Test unsupported context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testUnsupportedType() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      work.addContext(new UnsupportedContext());
      manager.doWork(work);
   }

   /**
    * Test duplicate transaction context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testTransactionContextDuplicate() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      work.addContext(new TransactionContext());
      work.addContext(new TransactionContextCustom());
      manager.doWork(work);
   }

   /**
    * Test duplicate security context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testSecurityContextDuplicate() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      work.addContext(new SecurityContextCustom());
      work.addContext(new SecurityContextCustom2());
      manager.doWork(work);
   }

   /**
    * Test duplicate hint context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testHintContextDuplicate() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      work.addContext(new HintsContext());
      work.addContext(new HintsContextCustom());
      manager.doWork(work);
   }

   /**
    * The application server must support the establishment of TransactionContext,
    * SecurityContext, and HintsContext contexts.
    * @throws Throwable - work exception
    */
   @Test
   public void testAllSupportedWorkContexts()  throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      work.addContext(new TransactionContextCustom());
      work.addContext(new SecurityContextCustom());
      work.addContext(new HintsContextCustom());
      manager.doWork(work);
      assertTrue(work.isReleased());
   }
   /**
    * Test hints context with long running work support.
    * @throws Throwable if duplicate exist
    */
   @Test
   public void testHintsContextLongRunningWork() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      HintsContext hc = new HintsContextCustom();
      hc.setHint(HintsContext.LONGRUNNING_HINT, true);
      work.addContext(hc);
      manager.doWork(work);
      assertEquals(1, executor.getCurrentThreadCount());
      assertTrue(work.isReleased());
   }
   
   /**
    * Supported context should be checked by equals method, so implementations
    * won't pass
    */
   @Test
   public void testContextSupport()
   {
      assertTrue(bootstrapContext.isContextSupported(TransactionContext.class));
      assertTrue(bootstrapContext.isContextSupported(HintsContext.class));
      assertTrue(bootstrapContext.isContextSupported(SecurityContext.class));
      assertFalse(bootstrapContext.isContextSupported(TransactionContextCustom.class));
      assertFalse(bootstrapContext.isContextSupported(HintsContextCustom.class));
      assertFalse(bootstrapContext.isContextSupported(SecurityContextCustom.class));
      assertFalse(bootstrapContext.isContextSupported(UnsupportedContext.class));
   }
}
