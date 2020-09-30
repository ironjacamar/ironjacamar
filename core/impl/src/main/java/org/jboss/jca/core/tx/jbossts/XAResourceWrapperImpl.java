/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.transaction.xa.XidWrapper;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * A XAResourceWrapper.
 * 
 * @author <a href="wprice@redhat.com">Weston Price</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XAResourceWrapperImpl implements org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper,
                                              org.jboss.tm.XAResourceWrapper
{
   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, XAResourceWrapperImpl.class.getName());
   
   /** The XA resource */
   private final XAResource xaResource;
   
   /** Pad */
   private final boolean pad;

   /** Override Rm Value */
   private final Boolean overrideRmValue;

   /** Product name */
   private final String productName;

   /** Product version */
   private final String productVersion;
   
   /** Product version */
   private final String jndiName;
   
   /** Cached hashCode() */
   private transient int cachedHashCode;

   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param productName product name
    * @param productVersion product version
    */   
   public XAResourceWrapperImpl(XAResource resource,
                                String productName, String productVersion)
   {
      this(resource, false, null, productName, productVersion, null);
   }

   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param pad pad
    * @param override override
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    */   
   public XAResourceWrapperImpl(XAResource resource, boolean pad, Boolean override, 
                                String productName, String productVersion,
                                String jndiName)
   {
      this.overrideRmValue = override;
      this.pad = pad;
      this.xaResource = resource;
      this.productName = productName;
      this.productVersion = productVersion;
      this.jndiName = jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      if (pad)
         xid = convertXid(xid);

      xaResource.commit(xid, onePhase);
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
      if (pad)
         xid = convertXid(xid);

      xaResource.end(xid, flags);
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
      if (pad)
         xid = convertXid(xid);

      xaResource.forget(xid);
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws XAException
   {
      return xaResource.getTransactionTimeout();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSameRM(XAResource resource) throws XAException
   {
      if (overrideRmValue != null)
      {
         if (log.isTraceEnabled())
         {
            log.trace("Executing isSameRM with override value" + overrideRmValue + " for XAResourceWrapper" + this);
         }
         return overrideRmValue.booleanValue();
      }
      else
      {
         if (resource instanceof org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper)
         {
            org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper other =
               (org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper)resource;
            return xaResource.isSameRM(other.getResource());
         }
         else
         {
            return xaResource.isSameRM(resource);
         }
         
      }
   }

   /**
    * {@inheritDoc}
    */
   public int prepare(Xid xid) throws XAException
   {
      if (pad)
         xid = convertXid(xid);

      return xaResource.prepare(xid);
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int flag) throws XAException
   {
      return xaResource.recover(flag);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      if (pad)
         xid = convertXid(xid);

      xaResource.rollback(xid);
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int flag) throws XAException
   {
      return xaResource.setTransactionTimeout(flag);
   }

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
      if (pad)
         xid = convertXid(xid);

      xaResource.start(xid, flags);
   }

   /**
    * Get the XAResource that is being wrapped
    * @return The XAResource
    */
   public XAResource getResource()
   {
      return xaResource;
   }

   /**
    * Get product name
    * @return Product name of the instance if defined; otherwise <code>null</code>
    */
   public String getProductName()
   {
      return productName;
   }

   /**
    * Get product version
    * @return Product version of the instance if defined; otherwise <code>null</code>
    */
   public String getProductVersion()
   {
      return productVersion;
   }

   /**
    * {@inheritDoc}
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Return wrapper for given xid.
    * @param xid xid
    * @return return wrapper
    */
   private Xid convertXid(Xid xid)
   {
      if (xid instanceof XidWrapper)
         return xid;
      else
         return new XidWrapperImpl(xid, pad, jndiName);
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean equals(Object object)
   {
      if (object == this)
         return true;

      if (object == null || !(object instanceof XAResourceWrapperImpl))
         return false;  

      XAResourceWrapperImpl other = (XAResourceWrapperImpl)object;
      if (xaResource != null)
      {
         if (!xaResource.equals(other.xaResource))
            return false;
      }
      else
      {
         if (other.xaResource != null)
            return false;
      }

      if (pad != other.pad)
         return false;

      if (overrideRmValue != null)
      {
         if (!overrideRmValue.equals(other.overrideRmValue))
            return false;
      }
      else
      {
         if (other.overrideRmValue != null)
            return false;
      }

      if (productName != null)
      {
         if (!productName.equals(other.productName))
            return false;
      }
      else
      {
         if (other.productName != null)
            return false;
      }

      if (productVersion != null)
      {
         if (!productVersion.equals(other.productVersion))
            return false;
      }
      else
      {
         if (other.productVersion != null)
            return false;
      }

      if (jndiName != null)
      {
         if (!jndiName.equals(other.jndiName))
            return false;
      }
      else
      {
         if (other.jndiName != null)
            return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      if (cachedHashCode == 0)
      {
         int result = 31;
         result += xaResource != null ? 7 * xaResource.hashCode() : 7;
         result += pad ? 7 : 1;
         result += overrideRmValue != null ? 7 * overrideRmValue.hashCode() : 7;
         result += productName != null ? 7 * productName.hashCode() : 7;
         result += productVersion != null ? 7 * productVersion.hashCode() : 7;
         result += jndiName != null ? 7 * jndiName.hashCode() : 7;

         cachedHashCode = result;
      }

      return cachedHashCode;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("XAResourceWrapperImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[xaResource=").append(xaResource);
      sb.append(" pad=").append(pad);
      sb.append(" overrideRmValue=").append(overrideRmValue);
      sb.append(" productName=").append(productName);
      sb.append(" productVersion=").append(productVersion);
      sb.append(" jndiName=").append(jndiName);
      sb.append("]");
      return sb.toString();
   }
}
