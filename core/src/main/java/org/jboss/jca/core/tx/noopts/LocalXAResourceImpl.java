/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.transaction.local.LocalXAException;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;
import org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Messages;

/**
 * Local XA resource implementation.
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class LocalXAResourceImpl implements LocalXAResource, XAResourceWrapper
{
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Product name */
   private String productName;

   /** Product version */
   private String productVersion;
   
   /** Product version */
   private String jndiName;
   
   /**
    * Creates a new instance.
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    */
   public LocalXAResourceImpl(String productName, String productVersion,
                              String jndiName)
   {
      this.productName = productName;
      this.productVersion = productVersion;
      this.jndiName = jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectionManager(ConnectionManager connectionManager)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectionListener(ConnectionListener cl)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
      throw new LocalXAException(bundle.forgetNotSupportedInLocalTx(), XAException.XAER_RMERR);
   }
   
   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws XAException
   {
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSameRM(XAResource xaResource) throws XAException
   {
      return xaResource == this;
   }

   /**
    * {@inheritDoc}
    */
   public int prepare(Xid xid) throws XAException
   {
      return XAResource.XA_OK;
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int flag) throws XAException
   {
      throw new LocalXAException(bundle.noRecoverWithLocalTxResourceManagers(), XAException.XAER_RMERR);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public XAResource getResource()
   {
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public String getProductName()
   {
      return productName;
   }

   /**
    * {@inheritDoc}
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
