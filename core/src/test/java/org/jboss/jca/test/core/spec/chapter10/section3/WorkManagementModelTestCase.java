/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.core.spec.chapter10.section3;

import org.junit.AfterClass;
import static org.junit.Assert.* ;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * WorkManagementModelTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkManagementModelTestCase
{

   
   /**
    * Test for paragraph 1
    * A resource adapter obtains a WorkManager instance from the BootstrapContext
    *            instance provided by the application server during its deployment.
    */
   @Test
   public void testGetWorkManagerFromBootstrapConext() throws Throwable
   {
      
   }

   /**
    * Test for paragraph 2
    * When a Work instance is submitted, one of the free threads picks up the
    *            Work instance, sets up an appropriate execution context and 
    *            calls the run method on the Work instance. 
    */
   @Ignore
   public void testOneThreadPickWorkInstance() throws Throwable
   {
   }
   
   /**
    * Test for paragraph2
    * There is no restriction on the NUMBER of Work instances submitted by a 
    *            resource adapter or when Work instances may be submitted.
    */
   @Ignore
   public void testManyWorkInstancesSubmitted() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 2
    * There is no restriction on the number of Work instances submitted by a 
    *            resource adapter or WHEN Work instances may be submitted.
    */
   @Ignore
   public void testAnytimeWorkInstanceSubmitted() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 2
    * When the run method on the Work instance completes, the application 
    *            server reuses the thread.
    */
   @Ignore
   public void testThreadBackPoolWhenWorkDone() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 3
    * The application server may decide to reclaim active threads based on load conditions. 
    */
   @Ignore
   public void testAsActiveThreadOnLoadCondition() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 3
    * he resource adapter should periodically monitor such hints and do the 
    *            necessary internal cleanup to avoid any inconsistencies. 
    */
   @Ignore
   public void testRaPeriodicalReleaseWorkResource() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 4
    * the application server must use threads of the same thread priority level to
    *            process Work instances submitted by a specific resource adapter. 
    */
   @Ignore
   public void testAsUseThreadSamePriorityLevel() throws Throwable
   {
   }   

}
