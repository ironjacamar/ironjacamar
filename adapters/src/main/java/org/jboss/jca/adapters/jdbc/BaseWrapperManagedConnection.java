/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc;

import org.jboss.jca.adapters.AdaptersBundle;
import org.jboss.jca.adapters.AdaptersLogger;
import org.jboss.jca.adapters.jdbc.spi.reauth.ReauthPlugin;
import org.jboss.jca.adapters.jdbc.util.ReentrantLock;
import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.ConnectableResourceListener;
import org.jboss.jca.core.connectionmanager.pool.mcp.NotifyingManagedConnection;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;

import org.jboss.logging.Messages;

/**
 * BaseWrapperManagedConnection
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:wprice@redhat.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class BaseWrapperManagedConnection implements NotifyingManagedConnection, ConnectableResource
{
   private static final WrappedConnectionFactory WRAPPED_CONNECTION_FACTORY;

   /** JDBC 4.2 factory */
   private static final String JDBC42_FACTORY = "org.jboss.jca.adapters.jdbc.jdk8.WrappedConnectionFactoryJDK8";

   /** JDBC 4.1 factory */
   private static final String JDBC41_FACTORY = "org.jboss.jca.adapters.jdbc.jdk7.WrappedConnectionFactoryJDK7";

   /** The bundle */
   protected static AdaptersBundle bundle = Messages.getBundle(AdaptersBundle.class);

   /** The managed connection factory */
   protected final BaseWrapperManagedConnectionFactory mcf;

   /** The connection */
   protected final Connection con;

   /** The properties */
   protected Properties props;

   private final int transactionIsolation;

   private final boolean readOnly;

   private ReentrantLock lock = new ReentrantLock(true);

   private int tryLock;
   
   private final Collection<ConnectionEventListener> cels = new CopyOnWriteArrayList<ConnectionEventListener>();

   private final Set<WrappedConnection> handles = new HashSet<WrappedConnection>();

   private PreparedStatementCache psCache = null;

   /** The state lock */
   protected final Object stateLock = new Object();

   /** Is inside a managed transaction */
   protected volatile boolean inManagedTransaction = false;

   /** Is inside a local transaction */
   protected AtomicBoolean inLocalTransaction = new AtomicBoolean(false);

   /** JDBC auto-commit */
   protected boolean jdbcAutoCommit = true;

   /** Ignore in managed auto commit calls */
   protected static boolean ignoreInManagedAutoCommitCalls = false;

   protected static boolean setAutoCommitOnCleanup = true;

   /** Underlying auto-commit */
   protected volatile boolean underlyingAutoCommit = true;

   // See JBAS-5678
   private boolean shouldRollbackOnDestroy = false;

   /** JDBC read-only */
   protected boolean jdbcReadOnly;

   /** Underlying read-only */
   protected boolean underlyingReadOnly;

   /** JDBC transaction isolation */
   protected int jdbcTransactionIsolation;

   /** Destroyed */
   protected boolean destroyed = false;

   /** Metadata */
   protected ManagedConnectionMetaData metadata;

   /** optional implementations on the driver*/
   private Optional<MethodHandle> requestBegin,requestEnd;

   static
   {
      Class<?> connectionFactory = null;
      try
      {
         connectionFactory = BaseWrapperManagedConnection.class.forName(JDBC42_FACTORY);
      }
      catch (ClassNotFoundException cnfe8)
      {
         try
         {
            connectionFactory = BaseWrapperManagedConnection.class.forName(JDBC41_FACTORY);
         }
         catch (ClassNotFoundException cnfe7)
         {
            throw new RuntimeException("Unabled to load wrapped connection factory", cnfe7);
         }
      }

      try
      {
         WRAPPED_CONNECTION_FACTORY = (WrappedConnectionFactory) connectionFactory.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error initializign connection factory", e);
      }

      String ignAutoCommit = SecurityActions.getSystemProperty("ironjacamar.jdbc.ignoreautocommit");
      if (ignAutoCommit != null)
         ignoreInManagedAutoCommitCalls = Boolean.valueOf(ignAutoCommit);

      //see JBJCA-1431
      String setAutoCommitOnCleanupString = SecurityActions.getSystemProperty("ironjacamar.jdbc.setautocommitoncleanup");
      if (setAutoCommitOnCleanupString != null)
         setAutoCommitOnCleanup = Boolean.valueOf(setAutoCommitOnCleanupString);
   }

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param con The connection
    * @param props The properties
    * @param transactionIsolation The transaction isolation
    * @param psCacheSize The prepared statement cache size
    * @exception SQLException Thrown if an error occurs
    */
   public BaseWrapperManagedConnection(final BaseWrapperManagedConnectionFactory mcf,
                                       final Connection con,
                                       Properties props, 
                                       final int transactionIsolation, 
                                       final int psCacheSize) 
      throws SQLException
   {
      this.mcf = mcf;
      this.con = con;
      this.props = props;
      this.tryLock = mcf.getUseTryLock().intValue();

      if (psCacheSize > 0)
      {
         psCache = new PreparedStatementCache(psCacheSize, mcf.getStatistics());
         mcf.getStatistics().registerPreparedStatementCache(psCache);
      }

      if (transactionIsolation == -1)
         this.transactionIsolation = con.getTransactionIsolation();

      else
      {
         this.transactionIsolation = transactionIsolation;
         con.setTransactionIsolation(transactionIsolation);
      }

      readOnly = con.isReadOnly();

      if (mcf.getNewConnectionSQL() != null)
      {
         Statement s = con.createStatement();
         try
         {
            s.execute(mcf.getNewConnectionSQL());
         }
         finally
         {
            s.close();
         }
      }

      underlyingReadOnly = readOnly;
      jdbcReadOnly = readOnly;
      jdbcTransactionIsolation = this.transactionIsolation;

      metadata = new ManagedConnectionMetaDataImpl(con, props.getProperty("user"));
   }

   /**
    * Add a connection event listener
    * @param cel The listener
    */
   public void addConnectionEventListener(ConnectionEventListener cel)
   {
      cels.add(cel);
   }

   /**
    * Remove a connection event listener
    * @param cel The listener
    */
   public void removeConnectionEventListener(ConnectionEventListener cel)
   {
      cels.remove(cel);
   }

   /**
    * Associate a handle
    * @param handle The handle
    * @exception ResourceException Thrown if an error occurs
    */
   public void associateConnection(Object handle) throws ResourceException
   {
      if (!(handle instanceof WrappedConnection))
         throw new ResourceException(bundle.wrongConnectionHandle(handle.toString()));

      WrappedConnection wc = (WrappedConnection)handle;
      wc.setManagedConnection(this);
      synchronized (handles)
      {
         handles.add(wc);
      }
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return metadata;
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
   public void cleanup() throws ResourceException
   {
      clearThreads();
      resetProperties();
   }

   private void clearThreads() throws ResourceException
   {
      boolean isActive = false;
      shouldRollbackOnDestroy = !underlyingAutoCommit;

      if (lock.hasQueuedThreads())
      {
         Thread currentThread = Thread.currentThread();
         Throwable currentThrowable =
            new Throwable("Detected queued threads during cleanup from: " + currentThread.getName());
         currentThrowable.setStackTrace(SecurityActions.getStackTrace(currentThread));
         mcf.log.queuedThreadName(currentThread.getName(), currentThrowable);

         Collection<Thread> threads = lock.getQueuedThreads();
         for (Thread thread : threads)
         {
            Throwable t = new Throwable("Queued thread: " + thread.getName());
            t.setStackTrace(SecurityActions.getStackTrace(thread));

            mcf.log.queuedThread(thread.getName(), t);
         }

         // Double-check
         if (lock.hasQueuedThreads())
            isActive = true;
      }

      if (lock.isLocked())
      {
         Thread owner = lock.getOwner();
         if (owner != null)
         {
            Throwable t = new Throwable("Lock owned during cleanup: " + owner.getName());
            t.setStackTrace(SecurityActions.getStackTrace(owner));
            mcf.log.lockOwned(owner.getName(), t);
         }
         else
         {
            mcf.log.lockOwnedWithoutOwner();
         }

         // Double-check
         if (lock.isLocked())
            isActive = true;
         if (isActive)
         {
            throw new ResourceException(bundle.activeLocks());
         }
      }

      synchronized (handles)
      {
         List<WrappedConnection> handlesCopy = new ArrayList<>(handles);
         for (WrappedConnection lc : handlesCopy)
         {
            lc.setManagedConnection(null);
            // JBJCA-1396 close handles to this connection, otherwise ds.getConnection() will return this instance
            // again, even though it'll have been already destroyed.
            closeHandle(lc);
         }
         handles.clear();
      }
   }

   private void resetProperties(){
      // Reset all the properties we know about to defaults.
      synchronized (stateLock)
      {
         jdbcAutoCommit = true;
         if (setAutoCommitOnCleanup && (jdbcAutoCommit != underlyingAutoCommit))
         {
            try {
               con.setAutoCommit(jdbcAutoCommit);
               underlyingAutoCommit = jdbcAutoCommit;
            } catch (SQLException e) {
               mcf.log.errorResettingAutoCommit(mcf.getJndiName(), e);
            }
         }
         jdbcReadOnly = readOnly;
         if (jdbcTransactionIsolation != transactionIsolation)
         {
            try
            {
               con.setTransactionIsolation(transactionIsolation);
               jdbcTransactionIsolation = transactionIsolation;
            }
            catch (SQLException e)
            {
               mcf.log.transactionIsolationReset(mcf.getJndiName(), e);
            }
         }
      }
   }

   /**
    * Lock
    */
   protected void lock()
   {
      lock.lock();
   }

   /**
    * Try lock
    * @exception SQLException Thrown if a lock can't be obtained
    */
   protected void tryLock() throws SQLException
   {
      if (tryLock < 0)
         return;

      if (getLog().isTraceEnabled())
         dumpLockInformation(true);

      if (tryLock == 0)
      {
         lock();
         return;
      }
      try
      {
         if (!lock.tryLock(tryLock, TimeUnit.SECONDS))
            throw new SQLException(bundle.unableToObtainLock(tryLock, this));
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         throw new SQLException(bundle.interruptedWhileLock(this));
      }
   }
   
   /**
    * Unlock
    */
   protected void unlock()
   {
      try
      {
         if (getLog().isTraceEnabled())
            dumpLockInformation(false);
      } finally
      {
         if (lock.isHeldByCurrentThread())
            lock.unlock();
      }
   }

   /**
    * Dump lock information
    * @param l Obtaining a lock (<code>true</code>), or releasing (<code>false</code>)
    */
   private void dumpLockInformation(boolean l)
   {
      getLog().tracef("%s: HeldByCurrentThread: %s, Locked: %s, HoldCount: %d, QueueLength: %d",
                      l ? "Lock" : "Unlock",
                      lock.isHeldByCurrentThread() ? "Yes" : "No",
                      lock.isLocked() ? "Yes" : "No",
                      lock.getHoldCount(),
                      lock.getQueueLength());
         
      if (lock.isLocked())
      {
         getLog().tracef("Owner: %s", lock.getOwner().toString());
      }

      if (lock.hasQueuedThreads())
      {
         Collection<Thread> threads = lock.getQueuedThreads();
         for (Thread thread : threads)
         {
            getLog().tracef("Queued: %s", thread.toString());
         }
      }
   }

   /**
    * Get a connection
    * @param subject The subject
    * @param cri The connection request info
    * @return The connection
    * @exception ResourceException Thrown if an error occurs
    */
   public Object getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      if (Boolean.TRUE.equals(mcf.getReauthEnabled()))
         mcf.loadReauthPlugin();

      checkIdentity(subject, cri);
      return getWrappedConnection();
   }

   /**
    * Destroy
    * @exception ResourceException Thrown if an error occurs
    */
   public void destroy() throws ResourceException
   {
      synchronized (stateLock)
      {
         destroyed = true;
      }

      clearThreads();

      try
      {
         // See JBAS-5678
         if (shouldRollbackOnDestroy)
            con.rollback();
      }
      catch (SQLException ignored)
      {
         if (getLog().isTraceEnabled())
            getLog().trace("Ignored error during rollback: ", ignored);
      }

      try
      {
         con.close();
      }
      catch (SQLException ignored)
      {
         if (getLog().isTraceEnabled())
            getLog().trace("Ignored error during close: ", ignored);
      }

      if (psCache != null)
         mcf.getStatistics().deregisterPreparedStatementCache(psCache);
   }

   /**
    * Get the properties
    * @return The value
    */
   public Properties getProperties()
   {
      return this.props;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection() throws Exception
   {
      return getWrappedConnection();
   }
 
   /**
    * {@inheritDoc}
    */
   public void setConnectableResourceListener(ConnectableResourceListener crl)
   {
   }

   /**
    * Error a handle
    * @param handle The handle
    */
   void errorHandle(WrappedConnection handle)
   {
      returnHandle(handle, true);
   }

   /**
    * Close a handle
    * @param handle The handle
    */
   void closeHandle(WrappedConnection handle)
   {
      returnHandle(handle, false);
   }

   /**
    * Return a handle
    * @param handle The handle
    * @param error Is the handle in error
    */
   private void returnHandle(WrappedConnection handle, boolean error)
   {
      synchronized (stateLock)
      {
         if (destroyed)
            return;
      }

      synchronized (handles)
      {
         handles.remove(handle);

         if (handles.size() == 0)
         {
            if (mcf.getConnectionListenerPlugin() != null)
            {
               try
               {
                  mcf.getConnectionListenerPlugin().passivated(con);
               }
               catch (SQLException se)
               {
                  mcf.log.errorDuringConnectionListenerPassivation(mcf.getJndiName(), se);
               }
            }
         }
      }

      ConnectionEvent ce = null;

      if (!error)
      {
         ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      }
      else
      {
         ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                                  new SQLException(bundle.invalidConnection()));
      }

      ce.setConnectionHandle(handle);

      for (ConnectionEventListener cel : cels)
      {

         if (!error)
         {
            cel.connectionClosed(ce);
         }
         else
         {
            cel.connectionErrorOccurred(ce);
         }
      }
   }

   /**
    * Connection error
    * @param t The error
    * @return The error
    */
   Throwable connectionError(Throwable t)
   {
      if (t instanceof SQLException)
      {
         if (mcf.isStaleConnection((SQLException)t))
         {
            t = new StaleConnectionException((SQLException)t);
         
         }
         else
         {
            if (mcf.isExceptionFatal((SQLException)t))
            {
               broadcastConnectionError(t);
            }
         }
      }
      else
      {
         broadcastConnectionError(t);         
      }

      return t;
   }

   /**
    * Broad cast a connection error
    * @param e The error
    */
   protected void broadcastConnectionError(Throwable e)
   {
      synchronized (stateLock)
      {
         if (destroyed)
         {
            if (getLog().isTraceEnabled())
               getLog().trace("Not broadcasting error, already destroyed " + this, e);
            return;
         }
      }

      // We need to unlock() before sending the connection error to the
      // event listeners. Otherwise the lock won't be in sync once
      // cleanup() is called
      unlock();

      Exception ex = null;
      if (e instanceof Exception)
      {
         ex = (Exception) e;
      }
      else
      {
         ex = new ResourceAdapterInternalException("Unexpected error", e);
      }

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, ex);

      for (ConnectionEventListener cel : cels)
      {
         try
         {
            cel.connectionErrorOccurred(ce);
         }
         catch (Throwable t)
         {
            getLog().errorNotifyingConnectionListener(cel.toString(), t);
         }
      }
   }

   /**
    * Get the connection
    * @return The connection
    * @exception SQLException Thrown if there isn't a connection
    */
   Connection getRealConnection() throws SQLException
   {
      if (con == null)
         throw new SQLException(bundle.connectionDestroyed());

      return con;
   }

   /**
    * Get prepared statement
    * @param sql The SQL
    * @param resultSetType The result set type
    * @param resultSetConcurrency The result set concurrency
    * @return The statement
    * @exception SQLException Thrown if an error occurs
    */
   PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      if (psCache != null)
      {
         mcf.getStatistics().deltaPreparedStatementCacheAccessCount();

         PreparedStatementCache.Key key = 
            new PreparedStatementCache.Key(sql,
                                           PreparedStatementCache.Key.PREPARED_STATEMENT, 
                                           resultSetType, 
                                           resultSetConcurrency);

         CachedPreparedStatement cachedps = psCache.get(key);
         if (cachedps != null)
         {
            if (canUse(cachedps))
            {
               mcf.getStatistics().deltaPreparedStatementCacheHitCount();

               cachedps.inUse();
            }
            else
            {
               mcf.getStatistics().deltaPreparedStatementCacheMissCount();

               return doPrepareStatement(sql, resultSetType, resultSetConcurrency);
            }
         }
         else
         {
            PreparedStatement ps = doPrepareStatement(sql, resultSetType, resultSetConcurrency);
            cachedps = WRAPPED_CONNECTION_FACTORY.createCachedPreparedStatement(ps);
            psCache.put(key, cachedps);

            mcf.getStatistics().deltaPreparedStatementCacheAddCount();
         }

         return cachedps;
      }
      else
      {
         return doPrepareStatement(sql, resultSetType, resultSetConcurrency);
      }
   }

   /**
    * Create prepared statement
    * @param sql The SQL
    * @param resultSetType The result set type
    * @param resultSetConcurrency The result set concurrency
    * @return The statement
    * @exception SQLException Thrown if an error occurs
    */
   PreparedStatement doPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
   }

   /**
    * Get callable statement
    * @param sql The SQL
    * @param resultSetType The result set type
    * @param resultSetConcurrency The result set concurrency
    * @return The statement
    * @exception SQLException Thrown if an error occurs
    */
   CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      if (psCache != null)
      {
         mcf.getStatistics().deltaPreparedStatementCacheAccessCount();

         PreparedStatementCache.Key key = 
            new PreparedStatementCache.Key(sql, 
                                           PreparedStatementCache.Key.CALLABLE_STATEMENT, 
                                           resultSetType, 
                                           resultSetConcurrency);

         CachedCallableStatement cachedps = (CachedCallableStatement) psCache.get(key);

         if (cachedps != null)
         {
            if (canUse(cachedps))
            {
               mcf.getStatistics().deltaPreparedStatementCacheHitCount();
               cachedps.inUse();
            }
            else
            {
               mcf.getStatistics().deltaPreparedStatementCacheMissCount();
               return doPrepareCall(sql, resultSetType, resultSetConcurrency);
            }
         }
         else
         {
            CallableStatement cs = doPrepareCall(sql, resultSetType, resultSetConcurrency);
            cachedps = WRAPPED_CONNECTION_FACTORY.createCachedCallableStatement(cs);
            psCache.put(key, cachedps);
            mcf.getStatistics().deltaPreparedStatementCacheAddCount();
         }
         return cachedps;
      }
      else
      {
         return doPrepareCall(sql, resultSetType, resultSetConcurrency);
      }
   }

   /**
    * Create callable statement
    * @param sql The SQL
    * @param resultSetType The result set type
    * @param resultSetConcurrency The result set concurrency
    * @return The statement
    * @exception SQLException Thrown if an error occurs
    */
   CallableStatement doPrepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return con.prepareCall(sql, resultSetType, resultSetConcurrency);
   }
   
   /**
    * Can the cached prepared statement be used
    * @param cachedps The statement
    * @return <code>True</code> if available; otherwise <code>false</code>
    */
   boolean canUse(CachedPreparedStatement cachedps)
   {
      // Nobody is using it so we are ok
      if (!cachedps.isInUse())
         return true;

      // Cannot reuse prepared statements in auto commit mode
      // if will close the previous usage of the PS
      if (underlyingAutoCommit)
         return false;

      // We have been told not to share
      return mcf.sharePS;
   }

   /**
    * Get the logger
    * @return The value
    */
   protected AdaptersLogger getLog()
   {
      return mcf.log;
   }

   private void checkIdentity(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      Properties newProps = mcf.getConnectionProperties(props, subject, cri);

      if (Boolean.TRUE.equals(mcf.getReauthEnabled()))
      {
         // Same credentials
         if (props.equals(newProps))
         {
            return;
         }

         // Check for changes
         if (!props.getProperty("user").equals(newProps.getProperty("user")) ||
             !props.getProperty("password").equals(newProps.getProperty("password")))
         {
            try
            {
               ReauthPlugin plugin = mcf.getReauthPlugin();
               plugin.reauthenticate(con, newProps.getProperty("user"), newProps.getProperty("password"));
               props = newProps;
            }
            catch (SQLException se)
            {
               throw new ResourceException(bundle.errorDuringReauthentication(), se);
            }
         }
      }
      else
      {
         if (!props.equals(newProps))
            throw new ResourceException(bundle.wrongCredentials());
      }
   }

   /**
    * The <code>checkTransaction</code> method makes sure the adapter follows the JCA
    * autocommit contract, namely all statements executed outside a container managed transaction
    * or a component managed transaction should be autocommitted. To avoid continually calling
    * setAutocommit(enable) before and after container managed transactions, we keep track of the state
    * and check it before each transactional method call.
    * @exception SQLException Thrown if an error occurs
    */
   void checkTransaction() throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            return;

         // Check autocommit
         if (jdbcAutoCommit != underlyingAutoCommit)
         {
            con.setAutoCommit(jdbcAutoCommit);
            underlyingAutoCommit = jdbcAutoCommit;
         }
      }

      if (mcf.isJTA().booleanValue())
      {
         if (!jdbcAutoCommit && !inLocalTransaction.getAndSet(true))
         {
            ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);

            for (ConnectionEventListener cel : cels)
            {
               try
               {
                  cel.localTransactionStarted(ce);
               }
               catch (Throwable t)
               {
                  if (getLog().isTraceEnabled())
                     getLog().trace("Error notifying of connection committed for listener: " + cel, t);
               }
            }
         }
      }

      checkState();
   }

   /**
    * Check state
    * @exception SQLException Thrown if an error occurs
    */
   protected void checkState() throws SQLException
   {
      synchronized (stateLock)
      {
         // Check readonly
         if (jdbcReadOnly != underlyingReadOnly)
         {
            con.setReadOnly(jdbcReadOnly);
            underlyingReadOnly = jdbcReadOnly;
         }
      }
   }

   /**
    * Is JDBC auto-commit
    * @return <code>True</code> if auto-commit; otherwise <code>false</code>
    */
   boolean isJdbcAutoCommit()
   {
      return inManagedTransaction ? false : jdbcAutoCommit;
   }

   /**
    * Set JDBC auto-commit
    * @param jdbcAutoCommit The status
    * @exception SQLException Thrown if an error occurs
    */
   void setJdbcAutoCommit(final boolean jdbcAutoCommit) throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
         {
            if (!ignoreInManagedAutoCommitCalls)
            {
               throw new SQLException(bundle.autocommitManagedTransaction());
            }
            else
            {
               return;
            }
         }

         this.jdbcAutoCommit = jdbcAutoCommit;
      }

      if (mcf.isJTA().booleanValue())
      {
         if (jdbcAutoCommit && inLocalTransaction.getAndSet(false))
         {
            ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);

            for (ConnectionEventListener cel : cels)
            {
               try
               {
                  cel.localTransactionCommitted(ce);
               }
               catch (Throwable t)
               {
                  if (getLog().isTraceEnabled())
                     getLog().trace("Error notifying of connection committed for listener: " + cel, t);
               }
            }
         }
      }
   }

   /**
    * Is JDBC read-only
    * @return <code>True</code> if read-only; otherwise <code>false</code>
    */
   boolean isJdbcReadOnly()
   {
      return jdbcReadOnly;
   }

   /**
    * Set JDBC read-only
    * @param readOnly The value
    * @exception SQLException Thrown if an error occurs
    */
   void setJdbcReadOnly(final boolean readOnly) throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException(bundle.readonlyManagedTransaction());

         this.jdbcReadOnly = readOnly;
      }
   }

   /**
    * Get JDBC transaction isolation
    * @return The value
    */
   int getJdbcTransactionIsolation()
   {
      return jdbcTransactionIsolation;
   }

   /**
    * Set JDBC transaction isolation
    * @param isolationLevel The value
    * @exception SQLException Thrown if an error occurs
    */
   void setJdbcTransactionIsolation(final int isolationLevel) throws SQLException
   {
      synchronized (stateLock)
      {
         con.setTransactionIsolation(isolationLevel);
         this.jdbcTransactionIsolation = isolationLevel;
      }
   }

   /**
    * JDBC commit
    * @exception SQLException Thrown if an error occurs
    */
   void jdbcCommit() throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException(bundle.commitManagedTransaction());

         if (jdbcAutoCommit)
            throw new SQLException(bundle.commitAutocommit());
      }
      con.commit();

      if (mcf.isJTA().booleanValue())
      {
         if (inLocalTransaction.getAndSet(false))
         {
            ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);

            for (ConnectionEventListener cel : cels)
            {
               try
               {
                  cel.localTransactionCommitted(ce);
               }
               catch (Throwable t)
               {
                  if (getLog().isTraceEnabled())
                     getLog().trace("Error notifying of connection committed for listener: " + cel, t);
               }
            }
         }
      }
   }

   /**
    * JDBC rollback
    * @exception SQLException Thrown if an error occurs
    */
   void jdbcRollback() throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException(bundle.rollbackManagedTransaction());
         if (jdbcAutoCommit)
            throw new SQLException(bundle.rollbackAutocommit());
      }
      con.rollback();

      if (mcf.isJTA().booleanValue())
      {
         if (inLocalTransaction.getAndSet(false))
         {
            ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);

            for (ConnectionEventListener cel : cels)
            {
               try
               {
                  cel.localTransactionRolledback(ce);
               }
               catch (Throwable t)
               {
                  if (getLog().isTraceEnabled())
                     getLog().trace("Error notifying of connection rollback for listener: " + cel, t);
               }
            }
         }
      }
   }

   /**
    * JDBC rollback
    * @param savepoint A savepoint
    * @exception SQLException Thrown if an error occurs
    */
   void jdbcRollback(Savepoint savepoint) throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException(bundle.rollbackManagedTransaction());

         if (jdbcAutoCommit)
            throw new SQLException(bundle.rollbackAutocommit());
      }
      con.rollback(savepoint);
   }

   /**
    * Get track statements
    * @return The value
    */
   int getTrackStatements()
   {
      return mcf.trackStatements;
   }

   /**
    * Is transaction query timeout
    * @return <code>True</code> if ; otherwise <code>false</code>
    */
   boolean isTransactionQueryTimeout()
   {
      return mcf.isTransactionQueryTimeout;
   }

   /**
    * Get query timeout
    * @return The value
    */
   int getQueryTimeout()
   {
      return mcf.getQueryTimeout();
   }

   /**
    * Check exception
    * @param e The exception
    * @exception ResourceException Thrown if an error occurs
    */
   protected void checkException(SQLException e) throws ResourceException
   {
      connectionError(e);

      throw new ResourceException("SQLException", e);
   }

   /**
    * Get a wrapped connection
    * @return The connection
    * @exception ResourceException Thrown if an error occurs
    */
   private WrappedConnection getWrappedConnection() throws ResourceException
   {
      WrappedConnection lc = WRAPPED_CONNECTION_FACTORY.createWrappedConnection(this,
                                                                                mcf.getSpy().booleanValue(),
                                                                                mcf.getJndiName(),
                                                                                mcf.isDoLocking(),
                                                                                mcf.getClassLoaderPlugin());
      synchronized (handles)
      {
         handles.add(lc);

         if (handles.size() == 1)
         {
            if (mcf.getConnectionListenerPlugin() != null)
            {
               try
               {
                  mcf.getConnectionListenerPlugin().activated(con);
               }
               catch (SQLException se)
               {
                  mcf.log.errorDuringConnectionListenerActivation(mcf.getJndiName(), se);
               }
            }
         }
      }

      return lc;
   }

   protected Optional<MethodHandle> getEndRequestNotify()
   {
      return requestEnd;
   }

   protected void setEndRequestNotify(Optional<MethodHandle> endRequest)
   {
      requestEnd = endRequest;
   }

   protected Optional<MethodHandle> getBeginRequestNotify()
   {
      return requestBegin;
   }

   protected void setBeginRequestNotify(Optional<MethodHandle> beginRequest)
   {
      requestBegin = beginRequest;
   }

   /**
    * Returns true if the underlying connection is handled by an XA resource manager
    * @return The value
    */
   public abstract boolean isXA();

   public void notifyRequestBegin()
   {
      Optional<MethodHandle> mh = getBeginRequestNotify();
      if (mh == null)
      {
         mh = lookupNotifyMethod("beginRequest");
         setBeginRequestNotify(mh);
      }
      if (mh.isPresent())
         invokeNotifyMethod(mh.get(), "beginRequest");
   }

   public void notifyRequestEnd()
   {
      Optional<MethodHandle> mh = getEndRequestNotify();
      if (mh == null)
      {
         mh = lookupNotifyMethod("endRequest");
         setEndRequestNotify(mh);
      }
      if (mh.isPresent())
         invokeNotifyMethod(mh.get(), "endRequest");
      flushPreparedStatementCache();
   }

   private void flushPreparedStatementCache()
   {
      if(psCache != null)
      {
         psCache.flush(cachedPreparedStatement -> {
               try
               {
                  return cachedPreparedStatement.isClosed();
               }
               catch(Exception e)
               {
                  mcf.log.errorDuringPreparedStatementCacheFlushing(e);
                  return false;
               }
            });
      }
   }

   private Optional<MethodHandle> lookupNotifyMethod(String methodName)
   {
      try
      {
         MethodHandle mh = SecurityActions.getMethodHandleInClassHierarchy(con.getClass(), methodName);
         if (mh == null)
            return Optional.empty();
         else
            return Optional.of(mh);
      } catch (Exception e)
      {
         getLog().debugf("Unable to invoke %s#%s: %s", con.getClass(), methodName, e.getMessage());
         return Optional.empty();
      }
   }

   private void invokeNotifyMethod(MethodHandle mh, String methodName)
   {
      try
      {
         mh.invoke(getRealConnection());
         getLog().debugf("java.sql.Connection#%s has been invoked", methodName);
      } catch (Throwable t)
      {
         getLog().debugf("Unable to invoke java.sql.Connection#%s: %s", methodName, t.getMessage());
      }
   }
}
