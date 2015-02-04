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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.ConnectableResourceListener;
import org.jboss.jca.core.spi.transaction.local.LocalResourceException;
import org.jboss.jca.core.spi.transaction.local.LocalXAException;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Local connectable XA resource implementation.
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalConnectableXAResourceImpl extends LocalXAResourceImpl 
   implements ConnectableResource, org.jboss.tm.ConnectableResource
{
   /** Connectable resource */
   private ConnectableResource cr;
   
   /** The connectable resource listener */
   private ConnectableResourceListener crl;

   /**
    * Creates a new instance.
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    * @param cr connectable resource
    */
   public LocalConnectableXAResourceImpl(String productName, String productVersion,
                                         String jndiName, ConnectableResource cr)
   {
      super(productName, productVersion, jndiName);
      this.cr = cr;
      this.crl = null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
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
   @Override
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
   public Object getConnection() throws Exception
   {
      Object result = cr.getConnection();

      if (crl != null)
         crl.handleCreated(result);

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectableResourceListener(ConnectableResourceListener crl)
   {
      this.crl = crl;
   }
}
