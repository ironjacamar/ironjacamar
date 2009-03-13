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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * PeriodicExecutionTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.7
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class PeriodicExecutionTestCase
{

   
   /**
    * Test for paragraph 1
    * A resource adapter may need to periodically execute Work instances.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testPeriodicExecution() throws Throwable
   {
      
   }

   /**
    * Test for paragraph 3
    * The application server must throw an UnavailableException if a Timer instance is unavailable; the resource
    *        adapter may retry later.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testThrowUnavailableException() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 3
    *  The application server must throw an java.lang.UnsupportedOperationException, if it does not 
    *  support the Timer service.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testThrowUnsupportedOperationException() throws Throwable
   {
   }
}
