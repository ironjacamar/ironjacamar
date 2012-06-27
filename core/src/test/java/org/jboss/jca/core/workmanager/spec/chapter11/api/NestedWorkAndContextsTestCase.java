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
import org.jboss.jca.core.workmanager.spec.chapter11.common.NestProviderWork;
import org.jboss.jca.core.workmanager.spec.chapter11.common.SecurityContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.TransactionContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.UnsupportedContext;
import org.jboss.jca.embedded.arquillian.Inject;

import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkManager;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * NestedWorkContextsTestCase.
 * Because nested Work submissions are allowed in the Connector WorkManager, the
 * Connector WorkManager must support nested contexts unless the WorkContext
 * type prohibits them. 
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class NestedWorkAndContextsTestCase
{
   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

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
      workB.addContext(new TransactionContextCustom());

      workA.setNestDo(true);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      workManager.doWork(workA, WorkManager.INDEFINITE, null, wa);

      assertEquals(wa.getStart(), "AB");
      assertEquals(wa.getDone(), "BA");
   }

   /**
    * Test for paragraph 4
    * startWork method: this provides a FIFO execution start ordering guarantee, 
    *                 but no execution completion ordering guarantee.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testFifoStart() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new SecurityContextCustom());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new HintsContext());

      workA.setNestDo(false);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      workManager.startWork(workA, WorkManager.INDEFINITE, null, wa);
      while (wa.getStart().length() < 2);
      assertEquals(wa.getStart(), "AB");
   }

   /**
    * Test unsupported context nested doWork. 
    * @throws Throwable throwable exception 
    */
   @Test(expected = Throwable.class)
   public void testDoWorkUnsupportedContext() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new TransactionContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new UnsupportedContext());

      workA.setNestDo(true);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      workManager.doWork(workA, WorkManager.INDEFINITE, null, wa);
   }

   /**
    * Test unsupported context nested startWork
    * @throws Throwable throwable exception 
    */
   @Test(expected = Throwable.class)
   @Ignore
   public void testStartWorkUnsupportedContext() throws Throwable
   {
      ContextWorkAdapter wa = new ContextWorkAdapter();
      NestProviderWork workA = new NestProviderWork("A", wa);
      workA.addContext(new HintsContext());

      NestProviderWork workB = new NestProviderWork("B", null);
      workB.addContext(new UnsupportedContext());

      workA.setNestDo(false);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      workManager.startWork(workA, WorkManager.INDEFINITE, null, wa);
   }
}
