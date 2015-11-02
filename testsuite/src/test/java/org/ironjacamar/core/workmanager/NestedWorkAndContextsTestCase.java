/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.workmanager;

import org.ironjacamar.core.workmanager.support.ContextWorkAdapter;
import org.ironjacamar.core.workmanager.support.NestProviderWork;
import org.ironjacamar.core.workmanager.support.NestedWorkSecurityContext;
import org.ironjacamar.core.workmanager.support.UnsupportedContext;
import org.ironjacamar.core.workmanager.support.WorkContextSetupListenerTransactionContext;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.wm.WorkConnectionFactory;

import java.util.concurrent.CyclicBarrier;

import javax.annotation.Resource;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkManager;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * NestedWorkContextsTestCase.
 * Because nested Work submissions are allowed in the Connector WorkManager, the
 * Connector WorkManager must support nested contexts unless the WorkContext
 * type prohibits them. 
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class NestedWorkAndContextsTestCase
{
   /** The user transaction */
   @Resource(mappedName = "java:/eis/WorkConnectionFactory")
   private static WorkConnectionFactory wcf;


   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createWorkRar();
   }

   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createWorkDeployment(null);
   }

   /**
    * Test for paragraph 3
    * doWork method: this provides a first in, first out (FIFO) execution start 
    *      ordering and last in, first out (LIFO) execution completion ordering guarantee.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testFifoStartLifoFinish() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new TransactionContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new WorkContextSetupListenerTransactionContext());

      workA.setNestDo(true);
      workA.setWorkManager(wcf.getConnection().getWorkManager());
      workA.setWork(workB);
      wcf.getConnection().doWork(workA, WorkManager.INDEFINITE, null, wa);

      assertEquals(wa.getStart(), "AB");
      assertEquals(wa.getDone(), "BA");
   }

   /**
    * Test for paragraph 4
    * startWork method: this provides a FIFO execution start ordering guarantee, 
    *                 but no execution completion ordering guarantee.
    * @throws Throwable throwable exception 
    */
   //@Test
   public void testFifoStart() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new NestedWorkSecurityContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new HintsContext());

      workA.setNestDo(false);
      workA.setWorkManager(wcf.getConnection().getWorkManager());
      workA.setWork(workB);
      
      CyclicBarrier barrier = new CyclicBarrier(3);
      workA.setBarrier(barrier);
      workB.setBarrier(barrier);

      wcf.getConnection().startWork(workA, WorkManager.INDEFINITE, null, wa);
      barrier.await();
      assertEquals(wa.getStart(), "AB");
   }

   /**
    * Test for paragraph 4
    * scheduletWork method: this provides a FIFO execution start ordering guarantee, 
    *                 but no execution completion ordering guarantee.
    * @throws Throwable throwable exception 
    */
   //@Test
   public void testFifoSchedule() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new NestedWorkSecurityContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new HintsContext());

      workA.setNestDo(false);
      workA.setWorkManager(wcf.getConnection().getWorkManager());
      workA.setWork(workB);
      
      CyclicBarrier barrier = new CyclicBarrier(3);
      workA.setBarrier(barrier);
      workB.setBarrier(barrier);

      wcf.getConnection().scheduleWork(workA, WorkManager.INDEFINITE, null, wa);
      barrier.await();
      assertEquals(wa.getStart(), "AB");
   }

   /**
    * Test unsupported context nested doWork. 
    * @throws Throwable throwable exception 
    */
   //@Test(expected = Throwable.class)
   public void testDoWorkUnsupportedContext() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new TransactionContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new UnsupportedContext());

      workA.setNestDo(true);
      workA.setWorkManager(wcf.getConnection().getWorkManager());
      workA.setWork(workB);
      wcf.getConnection().doWork(workA, WorkManager.INDEFINITE, null, wa);
   }

   /**
    * Test unsupported context nested startWork
    * @throws Throwable throwable exception 
    */
   //@Test
   public void testStartWorkUnsupportedContext() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new HintsContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new UnsupportedContext());

      workA.setNestDo(false);
      workA.setWorkManager(wcf.getConnection().getWorkManager());
      workA.setWork(workB);
      
      CyclicBarrier barrier = new CyclicBarrier(2);
      workA.setBarrier(barrier);
      workB.setBarrier(barrier);

      wcf.getConnection().startWork(workA, WorkManager.INDEFINITE, null, wa);
      barrier.await();
      assertNotNull(wa.getException());
   }

   /**
    * Test unsupported context nested scheduleWork
    * @throws Throwable throwable exception 
    */
   //@Test
   public void testScheduleWorkUnsupportedContext() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new HintsContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new UnsupportedContext());

      workA.setNestDo(false);
      workA.setWorkManager(wcf.getConnection().getWorkManager());
      workA.setWork(workB);
      
      CyclicBarrier barrier = new CyclicBarrier(2);
      workA.setBarrier(barrier);
      workB.setBarrier(barrier);

      wcf.getConnection().getWorkManager().scheduleWork(workA, WorkManager.INDEFINITE, null, wa);
      barrier.await();
      assertNotNull(wa.getException());
   }
}
