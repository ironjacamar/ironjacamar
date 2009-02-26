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
 * WorkInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.2
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkInterfaceTestCase
{

   
   /**
    * Test for paragraph 2
    * The WorkManager dispatches a thread that calls the run method to
    *             begin execution of a Work instance.
    */
   @Test
   public void testCallRunMethod() throws Throwable
   {
      
   }

   /**
    * Test for paragraph 2
    * The WorkManager must catch any exception thrown during Work processing,
    *             which includes execution context setup, and wrap it with a 
    *             WorkCompletedException set to an appropriate error code, 
    *             which indicates the nature of the error condition.
    */
   @Ignore
   public void testCatchAllExceptionAroundRun() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 3
    * The WorkManager may call the release method to request the active Work 
    *            instance to complete execution as soon as possible. 
    */
   @Ignore
   public void testReleaseMethod() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 3
    * This would be called on a separate thread than the one currently executing the Work instance.
    */
   @Ignore
   public void testCalledBySeparateThread() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 3
    * Since this method call causes the Work instance to be simultaneously acted upon
    *            by multiple threads, the Work instance implementation must be 
    *            thread-safe, and this method must be re-entrant.
    */
   @Ignore
   public void testWorkInstanceThreadSafeAndReentrant() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            blocks but they must not be declared as synchronized methods.
    */
   @Ignore
   public void testCannotDeclaredSynchronized() throws Throwable
   {
   }
}
