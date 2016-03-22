/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.api.connectionmanager.pool;

import org.ironjacamar.common.api.metadata.common.FlushStrategy;

/**
 * The pool configuration. 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolConfiguration
{
   /** The id */
   private String id;

   /** Minumum size of the pool */
   private int minSize;

   /** Initial size of the pool */
   private Integer initialSize;

   /** Maximum size of the pool */
   private int maxSize;

   /** Blocking timeout. In milliseconds */
   private long blockingTimeout;
   
   /** Idle timeout period. Default 30 mins */
   private int idleTimeoutMinutes;

   /** Validate on match validation */
   private boolean validateOnMatch;
   
   /** Background validation */
   private boolean backgroundValidation;
   
   /** Background validation - millis */
   private long backgroundValidationMillis;
   
   /** Prefill pool*/
   private boolean prefill;
   
   /** Use fast fail */
   private boolean useFastFail;

   /** The Flush Strategy */
   private FlushStrategy flushStrategy;


   /**
    * Constructor
    */
   public PoolConfiguration()
   {
      id = null;
      minSize = 0;
      initialSize = null;
      maxSize = 20;
      blockingTimeout = 30000;
      idleTimeoutMinutes = 30;
      validateOnMatch = false;
      backgroundValidation = false;
      backgroundValidationMillis = 60000;
      prefill = false;
      useFastFail = false;
      flushStrategy = null;
   }

   /**
    * Get id
    * @return The value
    */
   public String getId()
   {
      return id;
   }

   /**
    * Set id
    * @param v The value
    */
   public void setId(String v)
   {
      id = v;
   }

   /**
    * Get min-pool-size
    * @return The value
    */
   public int getMinSize()
   {
      if (minSize > maxSize)
         return maxSize;

      return minSize;
   }

   /**
    * Set min-pool-size
    * @param v The value
    */
   public void setMinSize(int v)
   {
      if (v >= 0)
         minSize = v;
   }

   /**
    * Get initial-pool-size
    * @return The value
    */
   public int getInitialSize()
   {
      if (initialSize == null)
         return getMinSize();

      if (initialSize.intValue() > maxSize)
         return maxSize;

      return initialSize.intValue();
   }

   /**
    * Set initial-pool-size
    * @param v The value
    */
   public void setInitialSize(int v)
   {
      if (v >= 0)
         initialSize = Integer.valueOf(v);
   }

   /**
    * Get max-pool-size
    * @return The value
    */
   public int getMaxSize()
   {
      if (maxSize < minSize)
         return minSize;

      return maxSize;
   }

   /**
    * Set max-pool-size
    * @param v The value
    */
   public void setMaxSize(int v)
   {
      if (v >= 0)
         maxSize = v;
   }

   /**
    * Get blocking-timeout
    * @return The value
    */
   public long getBlockingTimeout()
   {
      return blockingTimeout;
   }

   /**
    * Set blocking-timeout
    * @param v The value
    */
   public void setBlockingTimeout(long v)
   {
      if (v >= 0)
         blockingTimeout = v;
   }

   /**
    * Get idle-timeout-minutes
    * @return The value
    */
   public int getIdleTimeoutMinutes()
   {
      return idleTimeoutMinutes;
   }

   /**
    * Set idle-timeout-minutes
    * @param v The value
    */
   public void setIdleTimeoutMinutes(int v)
   {
      if (v >= 0)
         idleTimeoutMinutes = v;
   }

   /**
    * Get validate-on-match
    * @return The value
    */
   public boolean isValidateOnMatch()
   {
      return validateOnMatch;
   }

   /**
    * Set validate-on-match
    * @param v The value
    */
   public void setValidateOnMatch(boolean v)
   {
      validateOnMatch = v;
   }

   /**
    * Get background-validation
    * @return The value
    */
   public boolean isBackgroundValidation()
   {
      if (isValidateOnMatch())
         return false;

      return backgroundValidation;
   }

   /**
    * Set background-validation
    * @param v The value
    */
   public void setBackgroundValidation(boolean v)
   {
      backgroundValidation = v;
   }

   /**
    * Get background-validation-millis
    * @return The value
    */
   public long getBackgroundValidationMillis()
   {
      return backgroundValidationMillis;
   }

   /**
    * Set background-validation-millis
    * @param v The value
    */
   public void setBackgroundValidationMillis(long v)
   {
      backgroundValidationMillis = v;
   }

   /**
    * Get prefill
    * @return The value
    */
   public boolean isPrefill()
   {
      return prefill || (initialSize != null && initialSize > 0);
   }

   /**
    * Set prefill
    * @param v The value
    */
   public void setPrefill(boolean v)
   {
      prefill = v;
   }

   /**
    * Get use-fast-fai;
    * @return The value
    */
   public boolean isUseFastFail()
   {
      return useFastFail;
   }

   /**
    * Set use-fast-fail
    * @param v The value
    */
   public void setUseFastFail(boolean v)
   {
      useFastFail = v;
   }

   /**
    * Set flush-strategy
    * @param f The value
    */
   public void setFlushStrategy(FlushStrategy f)
   {
      flushStrategy = f;
   }

   /**
    * Get flush-strategy;
    * @return The value
    */
   public FlushStrategy getFlushStrategy()
   {
      return flushStrategy;
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
      sb.append("[minSize=").append(minSize);
      sb.append(" initialSize=").append(initialSize != null ? initialSize.intValue() : "null");
      sb.append(" maxSize=").append(maxSize);
      sb.append(" blockingTimeout=").append(blockingTimeout);
      sb.append(" idleTimeoutMinutes=").append(idleTimeoutMinutes);
      sb.append(" validateOnMatch=").append(validateOnMatch);
      sb.append(" backgroundValidation=").append(backgroundValidation);
      sb.append(" backgroundValidationMillis=").append(backgroundValidationMillis);
      sb.append(" prefill=").append(prefill);
      sb.append(" useFastFail=").append(useFastFail);
      sb.append(" flushStrategy=").append(flushStrategy);
      sb.append("]");

      return sb.toString();
   }
}
