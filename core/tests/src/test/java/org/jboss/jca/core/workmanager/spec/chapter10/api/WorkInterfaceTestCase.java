/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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

import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.workmanager.spec.chapter10.common.ShortRunningWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.SynchronizedReleaseWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.SynchronizedRunWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.UnsynchronizedWork;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.Xid;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * WorkInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WorkInterfaceTestCase
{
   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testCannotDeclaredSynchronizedRunMethodWork() throws Throwable
   {
      SynchronizedRunWork sw = new SynchronizedRunWork();
      workManager.doWork(sw);
      fail("Synchronized method not catched");
   }

   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testCannotDeclaredSynchronizedReleaseMethodWork() throws Throwable
   {
      SynchronizedReleaseWork sw = new SynchronizedReleaseWork();
      workManager.doWork(sw);
      fail("Synchronized method not catched");
   }

   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain  
    * synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCanDeclaredSynchronizedBlocksInUnsynchronizedWork() throws Throwable
   {
      UnsynchronizedWork usw = new UnsynchronizedWork();
      workManager.doWork(usw);
      assertTrue(usw.isRan());
      assertTrue(usw.isReleased());
   }
   
   /**
    * Test for bullet 4 Section 3.3.6
    * When the application server is unable to recreate an execution context if it is  
    *                      specified for the submitted Work instance, it must throw a
    *                      WorkCompletedException set to an appropriate error code.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkCompletedException.class)
   public void testThrowWorkCompletedException() throws Throwable
   {
      ExecutionContext ec = new ExecutionContext();
      ShortRunningWork work = new ShortRunningWork();
      ec.setXid(new XidImpl());
      ec.setTransactionTimeout(Long.MAX_VALUE);

      workManager.doWork(work, WorkManager.INDEFINITE, ec, null);

   }
   /**
    * Implementation of Xid for test purpose
    * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
    *
    */
   static class XidImpl implements Xid
   {
      
      /**
       * {@inheritDoc}
       */
      public byte[] getBranchQualifier()
      {
         return null;
      }

      /**
       * {@inheritDoc}
       */
      public int getFormatId()
      {
         return 0;
      }
      /**
       * {@inheritDoc}
       */
      public byte[] getGlobalTransactionId()
      {
         return null;
      }

   }
   
}
