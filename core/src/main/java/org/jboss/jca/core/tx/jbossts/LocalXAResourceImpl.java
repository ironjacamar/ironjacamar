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

import org.jboss.jca.core.api.connectionmanager.ConnectionManager;
import org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.transaction.local.LocalXAException;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * Local XA resource implementation.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class LocalXAResourceImpl implements LocalXAResource, org.jboss.tm.LastResource
{
   /** Log instance */
   private static Logger log = Logger.getLogger(LocalXAResourceImpl.class);

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

   /**
    * Creates a new instance.
    */
   public LocalXAResourceImpl()
   {
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
         throw new LocalXAException("Trying to start a new tx when old is not complete! old: " +
               currentXid + ", new " + xid + ", flags " + flags, XAException.XAER_PROTO);
      }
      
      if (currentXid == null && flags != XAResource.TMNOFLAGS)
      {
         throw new LocalXAException("Trying to start a new tx with wrong flags!  new " + xid +
               ", flags " + flags, XAException.XAER_PROTO);
      }

      if (currentXid == null)
      {
         try
         {
            cl.getManagedConnection().getLocalTransaction().begin();
         }
         catch (ResourceException re)
         {
            throw new LocalXAException("Error trying to start local tx: ", XAException.XAER_RMERR, re);
         }
         catch (Throwable t)
         {
            throw new LocalXAException("Throwable trying to start local transaction!", XAException.XAER_RMERR, t);
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
         throw new LocalXAException("Wrong xid in commit: expected: " + currentXid +
               ", got: " + xid, XAException.XAER_PROTO);
         
      }
      
      currentXid = null;

      try
      {
         cl.getManagedConnection().getLocalTransaction().commit();
      }
      catch (ResourceException re)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException("Could not commit local tx", XAException.XA_RBROLLBACK, re);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
      throw new LocalXAException("Forget not supported in local tx", XAException.XAER_RMERR);
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
         log.warn("Prepare called on a local tx. Use of local transactions on a jta transaction with more " +
               "than one branch may result in inconsistent data in some cases of failure.");  
      }
      warned = true;
      
      return XAResource.XA_OK;
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int flag) throws XAException
   {
      throw new LocalXAException("No recover with local-tx only resource managers", XAException.XAER_RMERR);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      if (!xid.equals(currentXid))
      {
         throw new LocalXAException("Wrong xid in rollback: expected: " +
               currentXid + ", got: " + xid, XAException.XAER_PROTO);  
      }
      currentXid = null;
      try
      {
         cl.getManagedConnection().getLocalTransaction().rollback();
      }
      catch (ResourceException re)
      {
         connectionManager.returnManagedConnection(cl, true);
         throw new LocalXAException("Could not rollback local tx", XAException.XAER_RMERR, re);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      return false;
   }
}
