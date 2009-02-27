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
 * WorkManagerInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.3
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkManagerInterfaceTestCase
{

   
   /**
    * Test for paragraph 1
    * WorkManager instance can be obtained by calling the getWorkManager method of the BootstrapContext instance.
    */
   @Test
   public void testGetWorkManagerFromBootstrapConext() throws Throwable
   {
      
   }

   /**
    * Test for paragraph 3
    * doWork method: This call blocks until the Work instance completes execution.
    */
   @Ignore
   public void testDoWorkMethod() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 3
    * doWork method: this provides a first in, first out (FIFO) execution start 
    *      ordering and last in, first out (LIFO) execution completion ordering guarantee.
    */
   @Ignore
   public void testFifoStartLifoFinish() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 4
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion.
    */
   @Ignore
   public void testStartWorkMethod() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 4
    * startWork method: This returns the time elapsed in milliseconds from Work acceptance until the start of execution.
    */
   @Ignore
   public void testReturnTimeBeforeStart() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 4
    * startWork method: A value of -1 (WorkManager.UNKNOWN) must be returned, if the actual start delay duration is unknown.
    */
   @Ignore
   public void testUnknownReturnedIfDonotKnowDelay() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 4
    * startWork method: this provides a FIFO execution start ordering guarantee, 
    *                 but no execution completion ordering guarantee.
    */
   @Ignore
   public void testFifoStart() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 5
    * scheduleWork method: This call does not block and returns immediately once a
    *                Work instance has been accepted for processing.
    */
   @Ignore
   public void testScheduleWorkMethod() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 6
    * The optional startTimeout parameter specifies a time duration in milliseconds within which 
    *      the execution of the Work instance must start. Otherwise, the Work instance 
    *      is rejected with a WorkRejectedException set to an appropriate error code (WorkException.START_TIMED_OUT).
    */
   @Ignore
   public void testStartTimeoutThrowWorkRejectedException() throws Throwable
   {
   }
   
   /**
    * Test for bullet 1 Section 3.3.6
    * The application server must implement the WorkManager interface
    */
   @Ignore
   public void testAsImplementWorkManagerInterface() throws Throwable
   {
   }   
   
   /**
    * Test for bullet 2 Section 3.3.6
    * The application server must allow nested Work submissions.
    */
   @Ignore
   public void testAllowNestedWork() throws Throwable
   {
   }
   
   /**
    * Test for bullet 4 Section 3.3.6
    * When the application server is unable to recreate an execution context if it is  
    *                      specified for the submitted Work instance, it must throw a
    *                      WorkCompletedException set to an appropriate error code.
    */
   @Ignore
   public void testThrowWorkCompletedException() throws Throwable
   {
   }
}

