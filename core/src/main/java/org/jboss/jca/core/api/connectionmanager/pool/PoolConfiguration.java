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

package org.jboss.jca.core.api.connectionmanager.pool;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The pool configuration. 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class PoolConfiguration
{
   /** Minumum size of the pool */
   private AtomicInteger minSize;

   /** Maximum size of the pool */
   private AtomicInteger maxSize;

   /** Blocking timeout. In milliseconds */
   private AtomicLong blockingTimeout;
   
   /** Idle timeout period. Default 30 mins */
   private AtomicInteger idleTimeoutMinutes;

   /** Background validation */
   private AtomicBoolean backgroundValidation;
   
   /** Background validation - millis */
   private AtomicLong backgroundValidationMillis;
   
   /** Prefill pool*/
   private AtomicBoolean prefill;
   
   /** Strict minumum, default false */
   private AtomicBoolean strictMin;

   /** 
    * Do we want to immeadiately break when a connection cannot be matched and
    * not evaluate the rest of the pool? 
    */
   private AtomicBoolean useFastFail;

   /**
    * Constructor
    */
   public PoolConfiguration()
   {
      minSize = new AtomicInteger(0);
      maxSize = new AtomicInteger(20);
      blockingTimeout = new AtomicLong(30000);
      idleTimeoutMinutes = new AtomicInteger(30);
      backgroundValidation = new AtomicBoolean(false);
      backgroundValidationMillis = new AtomicLong(0);
      prefill = new AtomicBoolean(false);
      strictMin = new AtomicBoolean(false);
      useFastFail = new AtomicBoolean(false);
   }

   /**
    * @return the minSize
    */
   public int getMinSize()
   {
      if (minSize.get() > maxSize.get())
         return maxSize.get();

      return minSize.get();
   }

   /**
    * @param minSize the minSize to set
    */
   public void setMinSize(int minSize)
   {
      this.minSize.set(minSize);
   }

   /**
    * @return the maxSize
    */
   public int getMaxSize()
   {
      if (maxSize.get() < minSize.get())
         return minSize.get();

      return maxSize.get();
   }

   /**
    * @param maxSize the maxSize to set
    */
   public void setMaxSize(int maxSize)
   {
      this.maxSize.set(maxSize);
   }

   /**
    * @return the blockingTimeout
    */
   public long getBlockingTimeout()
   {
      return blockingTimeout.get();
   }

   /**
    * @param blockingTimeout the blockingTimeout to set
    */
   public void setBlockingTimeout(long blockingTimeout)
   {
      this.blockingTimeout.set(blockingTimeout);
   }

   /**
    * @return the idleTimeout in milliseconds
    */
   public long getIdleTimeout()
   {
      return idleTimeoutMinutes.get() * 1000 * 60;
   }

   /**
    * @return the idleTimeout
    */
   public int getIdleTimeoutMinutes()
   {
      return idleTimeoutMinutes.get();
   }

   /**
    * @param idleTimeout the idleTimeout to set
    */
   public void setIdleTimeoutMinutes(int idleTimeout)
   {
      this.idleTimeoutMinutes.set(idleTimeout);
   }

   /**
    * @return Should background validation be performed
    */
   public boolean isBackgroundValidation()
   {
      return backgroundValidation.get();
   }

   /**
    * @param v Should background validation be performed 
    */
   public void setBackgroundValidation(boolean v)
   {
      this.backgroundValidation.set(v);
   }

   /**
    * Get the background validation millis setting
    * @return The value
    */
   public long getBackgroundValidationMillis()
   {
      return backgroundValidationMillis.get();
   }

   /**
    * Set the background validation millis setting
    * @param v The value
    */
   public void setBackgroundValidationMillis(long v)
   {
      this.backgroundValidationMillis.set(v);
   }

   /**
    * @return the prefill
    */
   public boolean isPrefill()
   {
      return prefill.get();
   }

   /**
    * @param prefill the prefill to set
    */
   public void setPrefill(boolean prefill)
   {
      this.prefill.set(prefill);
   }

   /**
    * @return the strictMin
    */
   public boolean isStrictMin()
   {
      return strictMin.get();
   }

   /**
    * @param strictMin the strictMin to set
    */
   public void setStrictMin(boolean strictMin)
   {
      this.strictMin.set(strictMin);
   }

   /**
    * @return the useFastFail
    */
   public boolean isUseFastFail()
   {
      return useFastFail.get();
   }

   /**
    * @param useFastFail the useFastFail to set
    */
   public void setUseFastFail(boolean useFastFail)
   {
      this.useFastFail.set(useFastFail);
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("PoolConfiguration@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[minSize=").append(minSize.get());
      sb.append(" maxSize=").append(maxSize.get());
      sb.append(" blockingTimeout=").append(blockingTimeout.get());
      sb.append(" idleTimeoutMinutes=").append(idleTimeoutMinutes.get());
      sb.append(" backgroundValidation=").append(backgroundValidation.get());
      sb.append(" backgroundValidationMillis=").append(backgroundValidationMillis.get());
      sb.append(" prefill=").append(prefill.get());
      sb.append(" strictMin=").append(strictMin.get());
      sb.append(" useFastFail=").append(useFastFail.get());
      sb.append("]");

      return sb.toString();
   }
}
