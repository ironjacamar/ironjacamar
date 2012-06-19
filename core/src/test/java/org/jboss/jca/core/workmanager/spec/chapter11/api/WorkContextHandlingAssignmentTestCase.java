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

import org.jboss.jca.core.workmanager.spec.chapter11.common.DuplicateHintContextWork;
import org.jboss.jca.core.workmanager.spec.chapter11.common.DuplicateSecurityContextWork;
import org.jboss.jca.core.workmanager.spec.chapter11.common.DuplicateTransactionContextWork;
import org.jboss.jca.core.workmanager.spec.chapter11.common.UnsupportedWork;
import org.jboss.jca.embedded.arquillian.Inject;

import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkManager;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WorkContextHandlingAssignmentTestCase.
 * @version $Rev$ $Date$
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class WorkContextHandlingAssignmentTestCase
{
   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager manager;

   /**
    * Test unsupported context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testUnsupportedType() throws Throwable
   {
      manager.doWork(new UnsupportedWork());
   }

   /**
    * Test duplicate transaction context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testTransactionContextDuplicate() throws Throwable
   {
      manager.doWork(new DuplicateTransactionContextWork());
   }

   /**
    * Test duplicate security context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testSecurityContextDuplicate() throws Throwable
   {
      manager.doWork(new DuplicateSecurityContextWork());
   }

   /**
    * Test duplicate hint context.
    * @throws Throwable if duplicate exist
    */
   @Test(expected = WorkCompletedException.class)
   public void testHintContextDuplicate() throws Throwable
   {
      manager.doWork(new DuplicateHintContextWork());
   }

}
