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

package org.jboss.jca.test.core.connectionmanager.unit;

import org.jboss.jca.core.connectionmanager.ConnectionCounter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <code>ConnectionCounter</code> unit tests.
 * 
 * @author gurkanerdogdu
 * @version $Rev$ $Date$
 *
 */
public class ConnectionCounterTestCase
{
   /**
    * Test {@link ConnectionCounter#getGuaranteedCount()}.
    */
   @Test
   public void testGetGuaranteedCount()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0, counter.getCreatedCount());      
      counter.inc();
      assertEquals(1, counter.getGuaranteedCount());
      counter.inc();
      counter.inc();
      assertEquals(3, counter.getGuaranteedCount());
      counter.dec();
      assertEquals(2, counter.getGuaranteedCount());
   }

   /**
    * Test {@link ConnectionCounter#getGuaranteedCount()}.
    */
   @Test
   public void testGetCount()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0, counter.getCreatedCount());      
      counter.inc();
      assertEquals(1, counter.getCount());
      counter.inc();
      counter.inc();
      assertEquals(3, counter.getCount());
      counter.dec();
      assertEquals(2, counter.getCount());
      
   }

   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testGetCreatedCount()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0, counter.getCreatedCount());      
      counter.inc();
      assertEquals(1, counter.getCreatedCount());
      counter.inc();
      assertEquals(2, counter.getCreatedCount());
   }

   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testGetDestroyedCount()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0, counter.getDestroyedCount());      
      counter.inc();
      assertEquals(1, counter.getCreatedCount());
      counter.dec();
      assertEquals(1, counter.getDestroyedCount());
      
   }

   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testInc()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0, counter.getCreatedCount());
      
      for (int i = 0; i < 5; i++)
      {
         counter.inc();  
      }      
      
      assertEquals(5, counter.getCreatedCount());
   }

   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testUpdateBlockTime()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0L, counter.getTotalBlockTime());
      counter.updateBlockTime(1234567L);      
      long lo = counter.getTotalBlockTime();      
      assertEquals(1234567L, lo);
   }
   
   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testGetTotalBlockTime()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0L, counter.getTotalBlockTime());
      counter.updateBlockTime(1L);
      counter.updateBlockTime(5L);      
      assertEquals(6L, counter.getTotalBlockTime());
   }

   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testGetTimedOut()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0L, counter.getTimedOutCount());
      
      counter.incTimedOutCount();
      counter.incTimedOutCount();
      assertEquals(2L, counter.getTimedOutCount());           
   }

   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testIncTimedOut()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0L, counter.getTimedOutCount());
      
      counter.incTimedOutCount();
      counter.incTimedOutCount();
      assertEquals(2L, counter.getTimedOutCount());                 
   }

   /**
    * Test {@link ConnectionCounter#GetGuaranteedCount()}.
    */
   @Test
   public void testGetMaxWaitTime()
   {
      ConnectionCounter counter = new ConnectionCounter();
      assertEquals(0L, counter.getMaxWaitTime());
      counter.updateBlockTime(10L);      
      assertEquals(10L, counter.getMaxWaitTime());
      counter.updateBlockTime(20L);
      assertEquals(20L, counter.getMaxWaitTime());
      counter.updateBlockTime(10L);
      assertEquals(20L, counter.getMaxWaitTime());
   }


}
