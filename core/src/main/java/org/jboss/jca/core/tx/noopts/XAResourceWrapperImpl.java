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
package org.jboss.jca.core.tx.noopts;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * A XAResourceWrapper.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XAResourceWrapperImpl implements XAResourceWrapper
{
   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, XAResourceWrapperImpl.class.getName());
   
   /** The XA resource */
   private XAResource xaResource;
   
   /** Override Rm Value */
   private Boolean overrideRmValue;

   /** Product name */
   private String productName;

   /** Product version */
   private String productVersion;
   
   /** Product version */
   private String jndiName;
   
   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param override override
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    */   
   public XAResourceWrapperImpl(XAResource resource, Boolean override, 
                                String productName, String productVersion,
                                String jndiName)
   {
      this.xaResource = resource;
      this.overrideRmValue = override;
      this.productName = productName;
      this.productVersion = productVersion;
      this.jndiName = jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      xaResource.commit(xid, onePhase);
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
      xaResource.end(xid, flags);
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
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
}
