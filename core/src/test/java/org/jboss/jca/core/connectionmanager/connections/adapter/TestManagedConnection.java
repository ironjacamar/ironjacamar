/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.connections.adapter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * TestManagedConnection
 */
public class TestManagedConnection  implements ManagedConnection, XAResource, LocalTransaction
{
   private static final String STARTED = "STARTED";
   private static final String SUSPENDED = "SUSPENDED";
   private static final String ENDED = "ENDED";
   private static final String PREPARED = "PREPARED";

   private static final String LOCAL_NONE = "LOCAL_NONE";
   private static final String LOCAL_TRANSACTION = "LOCAL_TRANSACTION";
   private static final String LOCAL_COMMITTED = "LOCAL_COMMITTED";
   private static final String LOCAL_ROLLEDBACK = "LOCAL_ROLLEDBACK";

   private int id;

   private Logger log = Logger.getLogger(getClass());
   private TestManagedConnectionFactory mcf;
   private HashSet<TestConnection> handles = new HashSet<TestConnection>();
   private HashSet<ConnectionEventListener> listeners = new HashSet<ConnectionEventListener>();

   private GlobalXID currentXid;

   private AtomicBoolean destroyed = new AtomicBoolean(false);

   private boolean failInPrepare = false;
   private boolean failInCommit = false;

   private static boolean failInStart = false;
   private static boolean failInEnd = false;
   private static int xaCode;

   private String localState = LOCAL_NONE;

   /**
    * Constructor
    * @param mcf The MCF
    * @param id The id
    */
   public TestManagedConnection(TestManagedConnectionFactory mcf, int id)
   {
      this.mcf = mcf;
      this.id = id;
   }

   /**
    * Fail in start
    * @param fis value
    * @param xa the code
    */
   public static void setFailInStart(boolean fis, int xa)
   {
      failInStart = fis;
      xaCode = xa;
   }

   /**
    * Fail in end
    * @param fie value
    * @param xa the code
    */
   public static void setFailInEnd(boolean fie, int xa)
   {
      failInEnd = fie;
      xaCode = xa;
   }

   /**
    * Fail in prepare
    * @param fail value
    * @param xaCode the code
    */
   void setFailInPrepare(boolean fail, int xaCode)
   {
      this.failInPrepare = fail;
      this.xaCode = xaCode;
   }

   /**
    * Fail in commit
    * @param fail value
    * @param xaCode the code
    */
   void setFailInCommit(boolean fail, int xaCode)
   {
      this.failInCommit = fail;
      this.xaCode = xaCode;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void destroy() throws ResourceException
   {
      if (destroyed.get())
         return;

      cleanup();

      destroyed.set(true);
      currentXid = null;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void cleanup() throws ResourceException
   {
      checkDestroyedResourceException();

      for (TestConnection c : handles)
      {
         c.setMc(null);
      }

      handles.clear();
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Object getConnection(Subject param1, ConnectionRequestInfo param2) throws ResourceException
   {
      checkDestroyedResourceException();

      if (param2 != null && ((TestConnectionRequestInfo) param2).getFailure().equals("getConnectionResource"))
         throw new ResourceException(this.toString());

      if (param2 != null && ((TestConnectionRequestInfo) param2).getFailure().equals("getConnectionRuntime"))
         throw new RuntimeException(this.toString());

      TestConnection c =  new TestConnection(this);
      handles.add(c);

      return c;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void associateConnection(Object p) throws ResourceException
   {
      checkDestroyedResourceException();

      if (p instanceof TestConnection)
      {
         TestConnection tc = (TestConnection)p;
         tc.setMc(this);
         handles.add(tc);
      }
      else
      {
         throw new ResourceException("Wrong kind of Connection " + p);
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void addConnectionEventListener(ConnectionEventListener cel)
   {
      listeners.add(cel);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void removeConnectionEventListener(ConnectionEventListener cel)
   {
      listeners.remove(cel);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized XAResource getXAResource() throws ResourceException
   {
      checkDestroyedResourceException();
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   /**
    * Get listeners
    * @return The value
    */
   public List<ConnectionEventListener> getListeners()
   {
      List<ConnectionEventListener> result = null;

      synchronized (listeners)
      {
         result = new ArrayList<ConnectionEventListener>(listeners);
      }

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
      long sleepInStart = mcf.getSleepInStart();
      if (flags == TMNOFLAGS && sleepInStart != 0)
         doSleep(sleepInStart);

      synchronized (this)
      {
         if (failInStart)
         {
            XAException xaex = new XAException(xaCode + " for " + this);
            xaex.errorCode = xaCode;
            broadcastConnectionError(xaex);
            throw xaex;
         }

         GlobalXID gid = new GlobalXID(xid);
         String flagString = getXAResourceFlagsAsString(flags);
         checkDestroyedXAException();
         Map<GlobalXID, String> xids = getXids();
         synchronized (xids)
         {
            String state = xids.get(gid);
            if (state == null && flags != TMNOFLAGS)
            {
               XAException xaex = new XAException("Invalid start state=" + state + " xid=" + gid +
                                                   " flags=" + flagString + " for " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }
            if (state != null && state != SUSPENDED && state != ENDED
                && (state != STARTED || ((flags & TMJOIN) == 0))
                && (state != STARTED || ((flags & TMRESUME) == 0)))
            {
               XAException xaex = new XAException("Invalid start state=" + state + " xid=" + gid +
                                                  " flags=" + flagString + " for " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }

            if ((flags & TMJOIN) != 0 && mcf.getFailJoin())
            {
               XAException xaex = new XAException("Join is not allowed " + state + " xid=" + gid +
                                                  " flags=" + flagString + " for " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }
            xids.put(gid, STARTED);
         }

         this.currentXid = gid;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void end(final Xid xid, final int flags) throws XAException
   {
      if (failInEnd)
      {
         XAException xaex = new XAException(xaCode + " for " + this);
         xaex.errorCode = xaCode;
         broadcastConnectionError(xaex);
         throw xaex;
      }

      long sleepInEnd = mcf.getSleepInEnd();
      if (flags != TMSUCCESS && sleepInEnd != 0)
         doSleep(sleepInEnd);

      synchronized (this)
      {
         GlobalXID gid = new GlobalXID(xid);
         String flagString = getXAResourceFlagsAsString(flags);
         Map<GlobalXID, String> xids = getXids();
         synchronized (xids)
         {
            String state = xids.get(gid);
            if (state != STARTED && state != SUSPENDED && state != ENDED)
            {
               XAException xaex = new XAException("Invalid end state=" + state + " xid=" + gid + " " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }
            if ((flags & TMSUSPEND) == 0)
            {
               xids.put(gid, ENDED);
            }
            else
            {
               xids.put(gid, SUSPENDED);
            }
         }

         this.currentXid = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void commit(Xid xid, boolean onePhase) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      log.info("commit with xid=" + gid + " onePhase=" + onePhase + " for " + this);
      checkDestroyedXAException();

      if (failInCommit)
      {
         XAException xaex = new XAException(xaCode + " for " + this);
         xaex.errorCode = xaCode;
         throw xaex;
      }

      Map<GlobalXID, String> xids = getXids();
      synchronized (xids)
      {
         String state = xids.get(gid);
         if (onePhase)
         {
            if (state != SUSPENDED && state != ENDED)
            {
               XAException xaex = new XAException("Invalid one phase commit state=" + state +
                                                  " xid=" + gid + " " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }
         }
         else
         {
            if (state != PREPARED)
            {
               XAException xaex = new XAException("Invalid two phase commit state=" + state +
                                                  " xid=" + gid + " " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }
         }
         xids.remove(gid);
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void rollback(Xid xid) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      checkDestroyedXAException();
      Map<GlobalXID, String> xids = getXids();
      synchronized (xids)
      {
         String state = xids.get(gid);
         if (state != SUSPENDED && state != ENDED && state != PREPARED)
         {
            XAException xaex = new XAException("Invalid rollback state=" + state + " xid=" + gid + " " + this);
            xaex.errorCode = XAException.XAER_PROTO;
            throw xaex;
         }
         xids.remove(gid);
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized int prepare(Xid xid) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      checkDestroyedXAException();
      Map<GlobalXID, String> xids = getXids();
      synchronized (xids)
      {
         String state = xids.get(gid);
         if (state != SUSPENDED && state != ENDED)
         {
            XAException xaex = new XAException("Invalid prepare state=" + state + " xid=" + gid + " " + this);
            xaex.errorCode = XAException.XAER_PROTO;
            throw xaex;
         }
         if (failInPrepare)
         {
            XAException xae = new XAException(xaCode + " for " + this);
            xae.errorCode = xaCode;
            throw xae;
         }
         xids.put(gid, PREPARED);
         return XA_OK;
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void forget(Xid xid) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      checkDestroyedXAException();
      Map<GlobalXID, String> xids = getXids();
      synchronized (xids)
      {
         xids.remove(gid);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int param1) throws XAException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSameRM(XAResource xar) throws XAException
   {
      if (xar == null || (!(xar instanceof TestManagedConnection)))
         return false;

      TestManagedConnection other = (TestManagedConnection) xar;
      return (mcf == other.mcf);
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
   public boolean setTransactionTimeout(int param1) throws XAException
   {
      return false;
   }

   /**
    * Get the local state
    * @return The value
    */
   public String getLocalState()
   {
      return localState;
   }

   /**
    * {@inheritDoc}
    */
   public void begin() throws ResourceException
   {
      localState = LOCAL_TRANSACTION;
   }

   /**
    * Send begin
    * @exception ResourceException If error
    */
   public void sendBegin() throws ResourceException
   {
      begin();
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
      Collection<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.localTransactionStarted(event);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void commit() throws ResourceException
   {
      localState = LOCAL_COMMITTED;
   }

   /**
    * Send commit
    * @exception ResourceException If error
    */
   public void sendCommit() throws ResourceException
   {
      commit();

      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
      Collection<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.localTransactionCommitted(event);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws ResourceException
   {
      localState = LOCAL_ROLLEDBACK;
   }

   /**
    * Send rollback
    * @exception ResourceException If error
    */
   public void sendRollback() throws ResourceException
   {
      rollback();

      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
      Collection<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.localTransactionRolledback(event);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
   }

   /**
    * Is in tx
    * @return The result
    */
   synchronized boolean isInTx()
   {
      return currentXid != null;
   }

   /**
    * Get Xids
    * @return The value
    */
   Map<GlobalXID, String> getXids()
   {
      return mcf.getXids();
   }

   /**
    * Connection closed
    * @param handle The handle
    */
   void connectionClosed(TestConnection handle)
   {
      if (destroyed.get())
         return;

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(handle);

      Collection<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.connectionClosed(ce);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
      synchronized (this)
      {
         handles.remove(handle);
      }
   }

   /**
    * Broadcast connection error
    * @param e The error
    */
   protected void broadcastConnectionError(Throwable e)
   {
      if (destroyed.get())
         return;

      Exception ex = null;
      if (e instanceof Exception)
         ex = (Exception) e;
      else
         ex = new ResourceAdapterInternalException("Unexpected error", e);

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, ex);
      Collection<ConnectionEventListener> copy = null;
      synchronized (listeners)
      {
         copy = new ArrayList<ConnectionEventListener>(listeners);
      }

      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.connectionErrorOccurred(ce);
         }
         catch (Throwable t)
         {
            // Ignore
         }
      }
   }

   /**
    * Connection error
    * @param handle The handle
    * @param e The error
    */
   void connectionError(TestConnection handle, Exception e)
   {
      if (destroyed.get())
         return;

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
      ce.setConnectionHandle(handle);

      Collection<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.connectionErrorOccurred(ce);
         }
         catch (Throwable t)
         {
            // Ignore
         }
      }
   }

   /**
    * Check destroyed
    * @exception ResourceException If error
    */
   void checkDestroyedResourceException() throws ResourceException
   {
      if (destroyed.get())
         throw new ResourceException("Already destroyed " + this);
   }

   /**
    * Check destroyed
    * @exception XAException If error
    */
   void checkDestroyedXAException() throws XAException
   {
      if (destroyed.get())
      {
         XAException xaex = new XAException("Already destroyed " + this);
         xaex.errorCode = XAException.XAER_PROTO;
         throw xaex;
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("TestManagedConnection#").append(id);
      buffer.append("{");
      buffer.append("xid=").append(currentXid);
      buffer.append(" destroyed=").append(destroyed.get());
      buffer.append("}");
      return buffer.toString();
   }

   /**
    * Sleep
    * @param sleep Millis
    */
   public void doSleep(long sleep)
   {
      boolean interrupted = false;
      try
      {
         Thread.sleep(sleep);
      }
      catch (InterruptedException e)
      {
         interrupted = true;
      }

      if (interrupted)
         Thread.currentThread().interrupt();
   }

   /**
    * Get XAResource
    * @param flags flags
    * @return The status
    */
   private static String getXAResourceFlagsAsString(int flags)
   {
      if (flags == XAResource.TMNOFLAGS)
      {
         return "|TMNOFLAGS";
      }
      else
      {
         StringBuffer sbuf = new StringBuffer(64);
         
         if ((flags & XAResource.TMONEPHASE) != 0)
         {
            sbuf.append("|TMONEPHASE");
         }
         if ((flags & XAResource.TMJOIN) != 0)
         {
            sbuf.append("|TMJOIN");
         }
         if ((flags & XAResource.TMRESUME) != 0)
         {
            sbuf.append("|TMRESUME");
         }
         if ((flags & XAResource.TMSUCCESS) != 0)
         {
            sbuf.append("|TMSUCCESS");
         }
         if ((flags & XAResource.TMFAIL) != 0)
         {
            sbuf.append("|TMFAIL");
         }
         if ((flags & XAResource.TMSUSPEND) != 0)
         {
            sbuf.append("|TMSUSPEND");
         }
         if ((flags & XAResource.TMSTARTRSCAN) != 0)
         {
            sbuf.append("|TMSTARTRSCAN");
         }
         if ((flags & XAResource.TMENDRSCAN) != 0)
         {
            sbuf.append("|TMENDRSCAN");
         }
         return sbuf.toString();
      }
   }
}
