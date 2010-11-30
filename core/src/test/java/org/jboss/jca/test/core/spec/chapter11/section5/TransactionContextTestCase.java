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

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.test.core.spec.chapter11.common.TransactionContextWork;

import java.net.URL;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

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
   /*
    * Embedded
    */
   private static Embedded embedded;

   /**
    * Test whether or not work contains  both execution context and implement {@link WorkContextProvider}.
    * @throws Throwable if work contains both execution context and implement {@link WorkContextProvider}
    */
   @Test(expected = WorkRejectedException.class)
   public void testNotBothExecutionContext() throws Throwable
   {
      WorkManager manager = embedded.lookup("WorkManager", WorkManager.class);
      manager.doWork(new TransactionContextWork(), WorkManager.INDEFINITE, new ExecutionContext(), null);

   }

   /**
    * Before class.
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create(false);

      // Startup
      embedded.startup();

      // Deploy Naming, Transaction and WorkManager
      URL naming = TransactionContextTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = TransactionContextTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm = TransactionContextTestCase.class.getClassLoader().getResource("workmanager.xml");

      embedded.deploy(naming);
      embedded.deploy(transaction);
      embedded.deploy(wm);

   }

   /**
    * After class.
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      URL naming = TransactionContextTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = TransactionContextTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm = TransactionContextTestCase.class.getClassLoader().getResource("workmanager.xml");

      embedded.undeploy(wm);
      embedded.undeploy(transaction);
      embedded.undeploy(naming);
      embedded.shutdown();

      embedded = null;
   }
}
