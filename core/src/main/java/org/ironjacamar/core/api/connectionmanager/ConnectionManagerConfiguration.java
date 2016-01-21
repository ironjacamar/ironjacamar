/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.api.connectionmanager;

/**
 * The connection manager configuration
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnectionManagerConfiguration
{
   /** Security domain */
   private String securityDomain;

   /** Number of retry to allocate connection */
   private int allocationRetry;

   /** Interval between retries */
   private long allocationRetryWaitMillis;

   /** Jndi name */
   private String jndiName;

   /** Sharable */
   private boolean sharable;

   /** Enlistment */
   private boolean enlistment;

   /** Connectable */
   private boolean connectable;

   /** Tracking */
   private Boolean tracking;

   /** XAResource time out */
   private int xaResourceTimeout;

   /** Pad the Xid */
   private boolean padXid;
   
   /** Wrap the XAResource */
   private boolean wrapXAResource;

   /** IsSameRM override */
   private Boolean isSameRMOverride;

   /**
    * Constructor
    */
   public ConnectionManagerConfiguration()
   {
      securityDomain = null;
      allocationRetry = 0;
      allocationRetryWaitMillis = 5000;
      jndiName = null;
      sharable = true;
      enlistment = true;
      connectable = true;
      tracking = null;
      xaResourceTimeout = 0;
      padXid = false;
      wrapXAResource = true;
      isSameRMOverride = null;
   }

   /**
    * Get security domain
    * @return The value
    */
   public String getSecurityDomain()
   {
      return securityDomain;
   }

   /**
    * Set security domain
    * @param v The value
    */
   public void setSecurityDomain(String v)
   {
      securityDomain = v;
   }

   /**
    * Get the number of allocation retries
    * @return The value
    */
   public int getAllocationRetry()
   {
      return allocationRetry;
   }

   /**
    * Set the number of allocation retries
    * @param v The value
    */
   public void setAllocationRetry(int v)
   {
      if (v >= 0)
         allocationRetry = v;
   }

   /**
    * Get the wait time in milliseconds between each allocation retry
    * @return The value
    */
   public long getAllocationRetryWaitMillis()
   {
      return allocationRetryWaitMillis;
   }

   /**
    * Set the wait time in milliseconds between each allocation retry
    * @param v The value
    */
   public void setAllocationRetryWaitMillis(long v)
   {
      if (v > 0)
         allocationRetryWaitMillis = v;
   }

   /**
    * Get jndi name
    * @return The value
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Set jndi name
    * @param v The value
    */
   public void setJndiName(String v)
   {
      jndiName = v;
   }

   /**
    * Is sharable
    * @return The value
    */
   public boolean isSharable()
   {
      return sharable;
   }

   /**
    * Set the sharable flag
    * @param v The value
    */
   public void setSharable(boolean v)
   {
      this.sharable = v;
   }

   /**
    * Is enlistment
    * @return The value
    */
   public boolean isEnlistment()
   {
      return enlistment;
   }

   /**
    * Set the enlistment flag
    * @param v The value
    */
   public void setEnlistment(boolean v)
   {
      enlistment = v;
   }

   /**
    * Is connectable
    * @return The value
    */
   public boolean isConnectable()
   {
      return connectable;
   }

   /**
    * Set the connectable flag
    * @param v The value
    */
   public void setConnectable(boolean v)
   {
      connectable = v;
   }

   /**
    * Is tracking
    * @return The value
    */
   public Boolean isTracking()
   {
      return tracking;
   }

   /**
    * Set the tracking flag
    * @param v The value
    */
   public void setTracking(Boolean v)
   {
      tracking = v;
   }

   /**
    * Get the XAResource timeout
    * @return The value
    */
   public int getXAResourceTimeout()
   {
      return xaResourceTimeout;
   }

   /**
    * Set the XAResource timeout
    * @param v The value
    */
   public void setXAResourceTimeout(int v)
   {
      if (v >= 0)
         xaResourceTimeout = v;
   }

   /**
    * Get PadXid status
    * @return The value
    */
   public boolean isPadXid()
   {
      return padXid;
   }
   
   /**
    * Set if the Xid should be padded
    * @param v The value
    */
   public void setPadXid(boolean v)
   {
      padXid = v;
   }

   /**
    * Should the XAResource be wrapped
    * @return The value
    */
   public boolean isWrapXAResource()
   {      
      return wrapXAResource;      
   }
   
   /**
    * Set if the XAResource should be wrapped
    * @param v The value
    */
   public void setWrapXAResource(boolean v)
   {
      wrapXAResource = v;
   }
   
   /**
    * Get the IsSameRMOverride value
    * @return The value
    */
   public Boolean isIsSameRMOverride()
   {
      return isSameRMOverride;
   }
   
   /**
    * Set the IsSameRMOverride value.
    * @param v The value
    */
   public void setIsSameRMOverride(Boolean v)
   {
      isSameRMOverride = v;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ConnectionManagerConfiguration@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[securityDomain=").append(securityDomain);
      sb.append(" allocationRetry=").append(allocationRetry);
      sb.append(" allocationRetryWaitMillis=").append(allocationRetryWaitMillis);
      sb.append(" jndiName=").append(jndiName);
      sb.append(" sharable=").append(sharable);
      sb.append(" enlistment=").append(enlistment);
      sb.append(" connectable=").append(connectable);
      sb.append(" tracking=").append(tracking);
      sb.append(" xaResourceTimeout=").append(xaResourceTimeout);
      sb.append(" padXid=").append(padXid);
      sb.append(" wrapXAResource=").append(wrapXAResource);
      sb.append(" isSameRMOverride=").append(isSameRMOverride);
      sb.append("]");

      return sb.toString();
   }
}
