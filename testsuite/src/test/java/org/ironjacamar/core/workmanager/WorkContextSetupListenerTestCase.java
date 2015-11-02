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
import org.ironjacamar.core.workmanager.support.UniversalProviderWork;
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

import javax.annotation.Resource;
import javax.resource.spi.work.WorkContextErrorCodes;
import javax.resource.spi.work.WorkManager;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * WorkContextSetupListenerTest
 * 
 * The WorkManager must make the notifications related to Work accepted and started
 * events prior to calling the WorkContext setup related notifications. The WorkManager
 * must make the notifications related to the Work completed events after the WorkContext
 * setup related notifications.
 *
 * @author gurkanerdogdu
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class WorkContextSetupListenerTestCase
{
   private static final Logger LOG = Logger.getLogger(WorkContextSetupListenerTestCase.class);

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
    * Test WorkContextLifecycleListener for transaction context.
    *
    * @throws Throwable throws any error
    */
   @Test
   public void testTransactionContextCustomListener() throws Throwable
   {
      UniversalProviderWork work = new UniversalProviderWork();
      WorkContextSetupListenerTransactionContext listener = new WorkContextSetupListenerTransactionContext();
      work.addContext(listener);
      ContextWorkAdapter wa = new ContextWorkAdapter();
      wcf.getConnection().doWork(work, WorkManager.INDEFINITE, null, wa);

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
      WorkContextSetupListenerTransactionContext listener = new WorkContextSetupListenerTransactionContext();
      work.addContext(new WorkContextSetupListenerTransactionContext());
      work.addContext(listener);
     
      ContextWorkAdapter wa = new ContextWorkAdapter();

      try
      {
         wcf.getConnection().doWork(work, WorkManager.INDEFINITE, null, wa);
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
