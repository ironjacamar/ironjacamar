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

package org.jboss.jca.test.core.spec.chapter11.section5;

import org.jboss.jca.test.core.spec.chapter11.common.TransactionContextWork;
import org.jboss.jca.test.core.spec.chapter11.section4.subsection3.WorkContextHandlingAssignmentTestCase;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TransactionContextTestCase.
 * @version $Rev$ $Date$
 *
 */
public class TransactionContextTestCase
{
   private static EmbeddedTestMcBootstrap bootstrap = null;

   /**
    * Test whether or not work contains  both execution context and implement {@link WorkContextProvider}.
    * @throws Throwable if work contains both execution context and implement {@link WorkContextProvider}
    */
   @Test(expected = WorkRejectedException.class)
   public void testNotBothExecutionContext() throws Throwable
   {
      WorkManager manager = bootstrap.lookup("WorkManager", WorkManager.class);
      manager.doWork(new TransactionContextWork(), WorkManager.INDEFINITE, new ExecutionContext(), null);

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
