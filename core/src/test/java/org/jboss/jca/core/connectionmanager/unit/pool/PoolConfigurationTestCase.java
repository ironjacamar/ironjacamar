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

package org.jboss.jca.core.connectionmanager.unit.pool;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <code>PoolConfiguration</code> unit test.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a> 
 */
public class PoolConfigurationTestCase
{
   /**
    * Test {@link PoolConfiguration#getMinSize()}
    */
   @Test
   public void testMinSize()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertEquals(0, params.getMinSize());
      params.setMinSize(10);
      assertEquals(10, params.getMinSize());
   }

   /**
    * Test {@link PoolConfiguration#getMaxSize()}
    */
   @Test
   public void testMaxSize()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertEquals(20, params.getMaxSize());
      params.setMaxSize(30);
      assertEquals(30, params.getMaxSize());      
   }

   /**
    * Test {@link PoolConfiguration#getBlockingTimeout()}
    */
   @Test
   public void testBlockingTimeout()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertEquals(30000, params.getBlockingTimeout());
      params.setBlockingTimeout(60000);
      assertEquals(60000, params.getBlockingTimeout());
   }

   /**
    * Test {@link PoolConfiguration#getIdleTimeout()}
    */
   @Test
   public void testIdleTimeout()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertEquals(30, params.getIdleTimeoutMinutes());
      assertEquals(30 * 1000 * 60, params.getIdleTimeout());
      params.setIdleTimeoutMinutes(20);
      assertEquals(20, params.getIdleTimeoutMinutes());
      assertEquals(20 * 1000 * 60, params.getIdleTimeout());
   }

   /**
    * Test {@link PoolConfiguration#isBackgroundValidation()}
    */
   @Test
   public void testBackgroundValidation()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertFalse(params.isBackgroundValidation());
      assertEquals(0L, params.getBackgroundValidationMillis());

      params.setBackgroundValidation(true);
      params.setBackgroundValidationMillis(5000);

      assertTrue(params.isBackgroundValidation());
      assertEquals(5000, params.getBackgroundValidationMillis());
   }

   /**
    * Test {@link PoolConfiguration#isPrefill()}
    */
   @Test
   public void testIsPrefill()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertFalse("Prefill must be false", params.isPrefill());
      params.setPrefill(true);
      assertTrue("Prefill must be true", params.isPrefill());
   }

   /**
    * Test {@link PoolConfiguration#isStrictMin()}
    */
   @Test
   public void testIsStrictMin()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertFalse("StrictMin must be false", params.isStrictMin());
      params.setStrictMin(true);
      assertTrue("StrictMin must be true", params.isStrictMin());      
   }

   /**
    * Test {@link PoolConfiguration#isUseFastFail()}
    */
   @Test
   public void testIsUseFastFail()
   {
      PoolConfiguration params = new PoolConfiguration();
      assertFalse("UseFastFail must be false", params.isUseFastFail());
      params.setUseFastFail(true);
      assertTrue("UseFastFail must be true", params.isUseFastFail());      
   }

}
