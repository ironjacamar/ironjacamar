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

import org.jboss.jca.core.connectionmanager.pool.PoolParams;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <code>PoolParams</code> unit test.
 * 
 * @author gurkanerdogdu
 * @version $Rev$ $Date$
 *
 */
public class PoolParamsTestCase
{
   /**
    * Test {@link PoolParams#getMinSize()}
    */
   @Test
   public void testMinSize()
   {
      PoolParams params = new PoolParams();
      assertEquals(0, params.getMinSize());
      params.setMinSize(10);
      assertEquals(10, params.getMinSize());
   }

   /**
    * Test {@link PoolParams#getMaxSize()}
    */
   @Test
   public void testMaxSize()
   {
      PoolParams params = new PoolParams();
      assertEquals(10, params.getMaxSize());
      params.setMaxSize(20);
      assertEquals(20, params.getMaxSize());      
   }

   /**
    * Test {@link PoolParams#getBlockingTimeout()}
    */
   @Test
   public void testBlockingTimeout()
   {
      PoolParams params = new PoolParams();
      assertEquals(30000, params.getBlockingTimeout());
      params.setBlockingTimeout(60000);
      assertEquals(60000, params.getBlockingTimeout());
   }

   /**
    * Test {@link PoolParams#getIdleTimeout()}
    */
   @Test
   public void testIdleTimeout()
   {
      PoolParams params = new PoolParams();
      assertEquals(1000 * 60 * 30, params.getIdleTimeout());
      params.setIdleTimeout(1000 * 60 * 20);
      assertEquals(1000 * 60 * 20, params.getIdleTimeout());
   }

   /**
    * Test {@link PoolParams#getBackgroundValidationInterval()}
    */
   @Test
   public void testBackgroundValidationInterval()
   {
      PoolParams params = new PoolParams();
      assertEquals(0L, params.getBackgroundValidationInterval());
      params.setBackgroundValidationInterval(50000L);
      assertEquals(50000L, params.getBackgroundValidationInterval());
   }

   /**
    * Test {@link PoolParams#isPrefill()}
    */
   @Test
   public void testIsPrefill()
   {
      PoolParams params = new PoolParams();
      assertFalse("Prefill must be false", params.isPrefill());
      params.setPrefill(true);
      assertTrue("Prefill must be true", params.isPrefill());
   }

   /**
    * Test {@link PoolParams#isStrictMin()}
    */
   @Test
   public void testIsStrictMin()
   {
      PoolParams params = new PoolParams();
      assertFalse("StrictMin must be false", params.isStrictMin());
      params.setStrictMin(true);
      assertTrue("StrictMin must be true", params.isStrictMin());      
   }

   /**
    * Test {@link PoolParams#isUseFastFail()}
    */
   @Test
   public void testIsUseFastFail()
   {
      PoolParams params = new PoolParams();
      assertFalse("UseFastFail must be false", params.isUseFastFail());
      params.setUseFastFail(true);
      assertTrue("UseFastFail must be true", params.isUseFastFail());      
   }

}
