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

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.transaction.local.LocalResourceException;
import org.jboss.jca.core.spi.transaction.local.LocalXAException;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * Local XA resource implementation.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalXAResourceImpl implements LocalXAResource,
                                            org.jboss.jca.core.spi.transaction.LastResource,
                                            org.jboss.tm.LastResource,
                                            org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper,
                                            org.jboss.tm.XAResourceWrapper
{
   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, LocalXAResourceImpl.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Connection listener */
   private ConnectionListener cl;

   /**Connection manager*/
   private ConnectionManager connectionManager = null;

   /**
    * <code>warned</code> is set after one warning about a local participant in
    * a multi-branch jta transaction is logged.
    */
   private boolean warned = false;

   /** Current transction branch id */
   private Xid currentXid;

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
      log.tracef("start(%s, %s)", xid, flags);  
      
      if (currentXid != null && flags == XAResource.TMNOFLAGS)
      {
         throw new LocalXAException(bundle.tryingStartNewTxWhenOldNotComplete(
               currentXid, xid, flags), XAException.XAER_PROTO);
      }
      
      if (currentXid == null && flags != XAResource.TMNOFLAGS)
      {
         throw new LocalXAException(bundle.tryingStartNewTxWithWrongFlags(xid, flags), XAException.XAER_PROTO);
      }

      if (currentXid == null)
      {
         try
         {
            cl.getManagedConnection().getLocalTransaction().begin();
         }
         catch (ResourceException re)
         {
            throw new LocalXAException(bundle.errorTryingStartLocalTx(), XAException.XAER_RMERR, re);
         }
         catch (Throwable t)
         {
            throw new LocalXAException(bundle.throwableTryingStartLocalTx(), XAException.XAER_RMERR, t);
         }

         currentXid = xid;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
      log.tracef("end(%s,%s)", xid, flags);  
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      if (!xid.equals(currentXid))
      {
         throw new LocalXAException(bundle.wrongXidInCommit(currentXid, xid), XAException.XAER_PROTO);
         
      }
      
      currentXid = null;

      try
      {
         cl.getManagedConnection().getLocalTransaction().commit();
      }
      catch (LocalResourceException lre)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException(bundle.couldNotCommitLocalTx(), XAException.XAER_RMFAIL, lre);
      }
      catch (ResourceException re)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException(bundle.couldNotCommitLocalTx(), XAException.XA_RBROLLBACK, re);
      }
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
      if (!warned)
      {
         log.prepareCalledOnLocaltx();  
      }
      warned = true;
      
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
      if (!xid.equals(currentXid))
      {
         throw new LocalXAException(bundle.wrongXidInRollback(currentXid, xid), XAException.XAER_PROTO);  
      }
      currentXid = null;
      try
      {
         cl.getManagedConnection().getLocalTransaction().rollback();
      }
      catch (LocalResourceException lre)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException(bundle.couldNotRollbackLocalTx(), XAException.XAER_RMFAIL, lre);
      }
      catch (ResourceException re)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException(bundle.couldNotRollbackLocalTx(), XAException.XAER_RMERR, re);
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

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("LocalXAResourceImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[connectionListener=").append(cl != null ? Integer.toHexString(System.identityHashCode(cl)) : "null");
      sb.append(" connectionManager=").append(connectionManager != null ?
                                              Integer.toHexString(System.identityHashCode(connectionManager)) : "null");
      sb.append(" warned=").append(warned);
      sb.append(" currentXid=").append(currentXid);
      sb.append(" productName=").append(productName);
      sb.append(" productVersion=").append(productVersion);
      sb.append(" jndiName=").append(jndiName);
      sb.append("]");

      return sb.toString();
   }
}
