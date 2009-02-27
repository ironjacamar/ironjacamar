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
 * WorkListenerInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.3/3.4
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkListenerInterfaceTestCase
{

   /**
    * Test for paragraph 1 Section 3.3.1
    * 
    */
   @Ignore
   public void testWorkSubmitStatus() throws Throwable
   {
   }   

   /**
    * Test for paragraph 1 Section 3.3.2
    */
   @Ignore
   public void testWorkAcceptedStatus() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.3
    */
   @Ignore
   public void testWorkRejectedStatus() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.4
    */
   @Ignore
   public void testWorkStartedStatus() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.5
    */
   @Ignore
   public void testWorkCompletedStatus() throws Throwable
   {
   }
   
   /**
    * Test for bullet 1 paragraph 2 Section 3.4
    * The WorkEvent instance provides The event type.
    */
   @Test
   public void testWorkEventInheritEventObject() throws Throwable
   {
      javax.resource.spi.work.WorkEvent workEvent = 
         new javax.resource.spi.work.WorkEvent(new Object(), 0, null, null);
      assertTrue(workEvent instanceof java.util.EventObject);
   }   
   
   /**
    * Test for bullet 2 paragraph 2 Section 3.4
    * The source object, that is, the Work instance, on which the event initially occurred.
    */
   @Ignore
   public void testSourceObjectIsInitial() throws Throwable
   {
   }   
   
   /**
    * Test for bullet 3 paragraph 2 Section 3.4
    * A handle to the associated Work instance.
    */
   @Ignore
   public void testHandleAssociatedWork() throws Throwable
   {
   }   
   
   /**
    * Test for bullet 4 paragraph 2 Section 3.4
    * An optional start delay duration in millisecond.
    */
   @Ignore
   public void testStartDelayDuration() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 4 Section 3.4
    * The WorkAdapter class is provided as a convenience for easily creating
    */
   @Test
   public void testWorkAdapter() throws Throwable
   {
      javax.resource.spi.work.WorkAdapter workAdapter = 
         new javax.resource.spi.work.WorkAdapter();
      assertTrue(workAdapter instanceof javax.resource.spi.work.WorkListener);
   }
   
   /**
    * Test for bullet 1 Section 3.4.1
    * The WorkListener instance must not make any thread assumptions and must be thread-safe
    */
   @Ignore
   public void testWorkListenerThreadSafe() throws Throwable
   {
   }   
   
   /**
    * Test for bullet 3 Section 3.4.1
    * The WorkListener implementation must not make any assumptions on the ordering of notifications.
    */
   @Ignore
   public void testNotificationWithoutOrder() throws Throwable
   {
   }
}