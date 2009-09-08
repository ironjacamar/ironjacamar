/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.xa;

import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.exception.JBossLocalXAException;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.tm.LastResource;

/**
 * Local XA resource implementation.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$ $Date$
 */
public class LocalXAResource implements XAResource, LastResource
{
   /** Log instance */
   private static Logger log = Logger.getLogger(LocalXAResource.class);

   /** Connection listener */
   private ConnectionListener cl;

   /** Log trace */
   private boolean trace;
   
   /**Connection manager*/
   private AbstractConnectionManager connectionManager = null;

   /**
    * <code>warned</code> is set after one warning about a local participant in
    * a multi-branch jta transaction is logged.
    */
   private boolean warned = false;

   /** Current transction branch id */
   private Xid currentXid;

   /**
    * Creates a new instance.
    * @param connectionManager connection manager
    */
   public LocalXAResource(AbstractConnectionManager connectionManager)
   {
      this.trace = log.isTraceEnabled();
      this.connectionManager = connectionManager;
   }

   /**
    * Sets connection listener.
    * 
    * @param cl connection listener
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
      if (this.trace)
      {
         log.trace("start, xid: " + xid + ", flags: " + flags);  
      }
      
      if (this.currentXid != null && flags == XAResource.TMNOFLAGS)
      {
         throw new JBossLocalXAException("Trying to start a new tx when old is not complete! old: " +
               currentXid + ", new " + xid + ", flags " + flags, XAException.XAER_PROTO);
      }
      
      if (this.currentXid == null && flags != XAResource.TMNOFLAGS)
      {
         throw new JBossLocalXAException("Trying to start a new tx with wrong flags!  new " + xid +
               ", flags " + flags, XAException.XAER_PROTO);
      }

      if (this.currentXid == null)
      {
         try
         {
            this.cl.getManagedConnection().getLocalTransaction().begin();
         }
         catch (ResourceException re)
         {
            throw new JBossLocalXAException("Error trying to start local tx: ", XAException.XAER_RMERR, re);
         }
         catch (Throwable t)
         {
            throw new JBossLocalXAException("Throwable trying to start local transaction!", XAException.XAER_RMERR, t);
         }

         this.currentXid = xid;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
      if (this.trace)
      {
         log.trace("end on xid: " + xid + " called with flags " + flags);  
      }
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      if (!xid.equals(this.currentXid))
      {
         throw new JBossLocalXAException("wrong xid in commit: expected: " + this.currentXid +
               ", got: " + xid, XAException.XAER_PROTO);
         
      }
      
      this.currentXid = null;

      try
      {
         this.cl.getManagedConnection().getLocalTransaction().commit();
      }
      catch (ResourceException re)
      {
         this.connectionManager.returnManagedConnection(this.cl, true);
         
         if (this.trace)
         {
            log.trace("commit problem: ", re);  
         }
         
         throw new JBossLocalXAException("could not commit local tx", XAException.XA_RBROLLBACK, re);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
      throw new JBossLocalXAException("forget not supported in local tx", XAException.XAER_RMERR);
   }
   
   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws XAException
   {
      // TODO implement this javax.transaction.xa.XAResource method
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
      if (!this.warned)
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
      throw new JBossLocalXAException("no recover with local-tx only resource managers", XAException.XAER_RMERR);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      if (!xid.equals(this.currentXid))
      {
         throw new JBossLocalXAException("wrong xid in rollback: expected: " +
               this.currentXid + ", got: " + xid, XAException.XAER_PROTO);  
      }
      this.currentXid = null;
      try
      {
         this.cl.getManagedConnection().getLocalTransaction().rollback();
      }
      catch (ResourceException re)
      {
         this.connectionManager.returnManagedConnection(this.cl, true);
        
         if (this.trace)
         {
            log.trace("rollback problem: ", re);  
         }
         
         throw new JBossLocalXAException("could not rollback local tx", XAException.XAER_RMERR, re);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      // TODO implement this javax.transaction.xa.XAResource method
      return false;
   }
}
