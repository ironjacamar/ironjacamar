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

import org.ironjacamar.core.workmanager.support.ShortRunningWork;
import org.ironjacamar.core.workmanager.support.SynchronizedReleaseWork;
import org.ironjacamar.core.workmanager.support.SynchronizedRunWork;
import org.ironjacamar.core.workmanager.support.UnsynchronizedWork;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.wm.WorkConnectionFactory;

import javax.annotation.Resource;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.Xid;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * WorkInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */

@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class WorkInterfaceTestCase
{
   /** The WorkConnectionFactory */
   @Resource(mappedName = "java:/eis/WorkConnectionFactory")
   private WorkConnectionFactory wcf;


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
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testCannotDeclaredSynchronizedRunMethodWork() throws Throwable
   {
      SynchronizedRunWork sw = new SynchronizedRunWork();
      wcf.getConnection().doWork(sw);
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
      wcf.getConnection().doWork(sw);
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
      wcf.getConnection().doWork(usw);
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

      wcf.getConnection().doWork(work, WorkManager.INDEFINITE, ec, null);

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
