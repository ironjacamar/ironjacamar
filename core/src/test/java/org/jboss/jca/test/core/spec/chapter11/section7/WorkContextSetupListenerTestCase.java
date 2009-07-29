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

package org.jboss.jca.test.core.spec.chapter11.section7;

import org.jboss.jca.test.core.spec.chapter11.common.DuplicateTransactionContextWork;
import org.jboss.jca.test.core.spec.chapter11.common.TransactionContextCustom;
import org.jboss.jca.test.core.spec.chapter11.common.TransactionContextWork;
import org.jboss.jca.test.core.spec.chapter11.section4.subsection3.WorkContextHandlingAssignmentTestCase;

import javax.resource.spi.work.WorkContextErrorCodes;
import javax.resource.spi.work.WorkManager;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * WorkContextSetupListenerTest
 * 
 * @version $Rev$ $Date$
 * @author gurkanerdogdu
 */
public class WorkContextSetupListenerTestCase
{
   /** Embedded bootstrap */
   private static EmbeddedTestMcBootstrap bootstrap = null;

   /**
    * Test {@link WorkContextLifecycleListener} for transaction context.
    * 
    * @throws Throwable throws any error
    */
   @Test
   public void testTransactionContextCustomListener() throws Throwable
   {
      WorkManager manager = bootstrap.lookup("WorkManager", WorkManager.class);
      manager.doWork(new TransactionContextWork(), WorkManager.INDEFINITE, null, null);
      
      String errorCode = TransactionContextCustom.getContextSetupFailedErrorCode();
      boolean complete = TransactionContextCustom.isContextSetupComplete();
      
      Assert.assertEquals("", errorCode);
      Assert.assertTrue(complete);
      
   }
   
   /**
    * Test {@link WorkContextLifecycleListener} for transaction context.
    * 
    * @throws Throwable throws any error
    */
   @Test
   public void testTransactionContextFailedListener() throws Throwable
   {
      WorkManager manager = bootstrap.lookup("WorkManager", WorkManager.class);
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
      
      Assert.assertEquals(WorkContextErrorCodes.DUPLICATE_CONTEXTS, errorCode);
      Assert.assertFalse(complete);
      
   }
   

   /**
    * Before class.
    */
   @BeforeClass
   public static void beforeClass()
   {
      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();

      // Deploy Naming, Transaction and WorkManager
      bootstrap.deploy(WorkContextHandlingAssignmentTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkContextHandlingAssignmentTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.deploy(WorkContextHandlingAssignmentTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");

   }

   /**
    * After class.
    */
   @AfterClass
   public static void afterClass()
   {
      bootstrap.undeploy(WorkContextHandlingAssignmentTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
      bootstrap.undeploy(WorkContextHandlingAssignmentTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkContextHandlingAssignmentTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.shutdown();

      bootstrap = null;
   }

}
