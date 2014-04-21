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

import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.transaction.local.LocalXAException;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;
import org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Local XA resource implementation.
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalXAResourceImpl implements LocalXAResource, XAResourceWrapper
{
   /** Connection listener */
   private ConnectionListener cl;

   /**Connection manager*/
   private ConnectionManager connectionManager = null;

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
      this.cl = null;
      this.connectionManager = null;
      this.productName = productName;
      this.productVersion = productVersion;
      this.jndiName = jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectionManager(ConnectionManager connectionManager)
   {
      this.connectionManager = connectionManager;
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectionListener(ConnectionListener cl)
   {
      this.cl = cl;
   }

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
      try
      {
         cl.getManagedConnection().getLocalTransaction().begin();
      }
      catch (ResourceException re)
      {
         throw new LocalXAException("start", XAException.XAER_RMERR, re);
      }
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
      try
      {
         cl.getManagedConnection().getLocalTransaction().commit();
      }
      catch (ResourceException re)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException("commit", XAException.XA_RBROLLBACK, re);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
      throw new LocalXAException("Error", XAException.XAER_RMERR);
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
      throw new LocalXAException("Error", XAException.XAER_RMERR);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      try
      {
         cl.getManagedConnection().getLocalTransaction().rollback();
      }
      catch (ResourceException re)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException("rollback", XAException.XAER_RMERR, re);
      }
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
