/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.ccm;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionRecord;
import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.connectionmanager.transaction.TransactionSynchronizer;
import org.jboss.jca.core.spi.transaction.TxUtils;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * CacheConnectionManager.
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CachedConnectionManagerImpl implements CachedConnectionManager
{
   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
                                                           CachedConnectionManager.class.getName());
   /** Trace */
   private static boolean trace = log.isTraceEnabled();

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Debugging flag */
   private boolean debug = false;

   /** Enabled error handling for debugging */
   private boolean error = false;

   /** Transaction Manager instance */
   private TransactionManager transactionManager;

   private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

   /** User transaction registry */
   private UserTransactionRegistry userTransactionRegistry;

   /**
    * ThreadLocal that holds current calling meta-programming aware
    * object, used in case someone is idiotic enough to cache a
    * connection between invocations.and want the spec required
    * behavior of it getting hooked up to an appropriate
    * ManagedConnection on each method invocation.
    */
   private final ThreadLocal<LinkedList<Object>> currentObjects = new ThreadLocal<LinkedList<Object>>();

   /**
    * The variable <code>objectToConnectionManagerMap</code> holds the
    * map of meta-aware object to set of connections it holds, used by
    * the idiot spec compliant behavior.
    */
   private final ConcurrentMap<KeyConnectionAssociation,
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>>
   objectToConnectionManagerMap = new ConcurrentHashMap<KeyConnectionAssociation,
      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>>();

   /**
    * Connection stacktraces
    */
   private final Map<Object, Throwable> connectionStackTraces = new WeakHashMap<Object, Throwable>();

   /**
    * Creates a new instance.
    * @param transactionManager The transaction manager
    * @param transactionSynchronizationRegistry the transaction synchronization registry
    * @param userTransactionRegistry The user transaction registry
    */
   public CachedConnectionManagerImpl(TransactionManager transactionManager,
                                      TransactionSynchronizationRegistry transactionSynchronizationRegistry,
                                      UserTransactionRegistry userTransactionRegistry)
   {
      this.transactionManager = transactionManager;
      this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
      this.userTransactionRegistry = userTransactionRegistry;
   }

   /**
    * Gets transaction manager.
    * @return transaction manager
    */
   public TransactionManager getTransactionManager()
   {
      return transactionManager;
   }

   /**
    * Gets transaction synchronization registry
    * @return TransactionSynchronizationRegistry
    */
   public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      return transactionSynchronizationRegistry;
   }

   /**
    * Set debug flag
    * @param v The value
    */
   public void setDebug(boolean v)
   {
      debug = v;
   }

   /**
    * Set error flag
    * @param v The value
    */
   public void setError(boolean v)
   {
      error = v;
   }

   /**
    * Start
    */
   public void start()
   {
      if (userTransactionRegistry != null)
         userTransactionRegistry.addListener(this);

      log.debugf("start: %s", this.toString());
   }

   /**
    * Stop
    */
   public void stop()
   {
      log.debugf("stop: %s", this.toString());

      if (userTransactionRegistry != null)
         userTransactionRegistry.removeListener(this);
   }

   /**
    * {@inheritDoc}
    */
   public void userTransactionStarted() throws SystemException
   {
      KeyConnectionAssociation key = peekMetaAwareObject();

      if (trace)
         log.tracef("user tx started, key: %s", key);

      if (key != null)
      {
         ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
            key.getCMToConnectionsMap();

         Iterator<Entry<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>> cmToConnectionsMapIterator =
            cmToConnectionsMap.entrySet().iterator();

         while (cmToConnectionsMapIterator.hasNext())
         {
            Entry<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> entry =
               cmToConnectionsMapIterator.next();

            ConnectionCacheListener cm = entry.getKey();
            CopyOnWriteArrayList<ConnectionRecord> conns = entry.getValue();

            cm.transactionStarted(conns);
         }
      }
   }

   /**
    *
    * @return stack last meta-aware object
    */
   private KeyConnectionAssociation peekMetaAwareObject()
   {
      LinkedList<Object> stack = currentObjects.get();

      if (stack != null && !stack.isEmpty())
      {
         return (KeyConnectionAssociation)stack.getLast();
      }

      return null;
   }


   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void popMetaAwareObject(Set unsharableResources) throws ResourceException
   {
      LinkedList<Object> stack = currentObjects.get();
      KeyConnectionAssociation oldKey = (KeyConnectionAssociation) stack.removeLast();

      if (trace)
         log.tracef("popped object: %s", oldKey);

      if (debug)
      {
         if (closeAll(oldKey.getCMToConnectionsMap()) && error)
         {
            throw new ResourceException(bundle.someConnectionsWereNotClosed());
         }
      }
   }

   /**
    * Register connection.
    * @param cm connection manager
    * @param cl connection listener
    * @param connection connection handle
    * @param cri connection request info.
    */
   public void registerConnection(org.jboss.jca.core.api.connectionmanager.listener.ConnectionCacheListener cm,
                                  org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener cl,
                                  Object connection, ConnectionRequestInfo cri)
   {
      if (debug)
      {
         synchronized (connectionStackTraces)
         {
            connectionStackTraces.put(connection, new Throwable("STACKTRACE"));
         }
      }

      KeyConnectionAssociation key = peekMetaAwareObject();

      if (trace)
         log.tracef("registering connection from connection manager: %s, connection : %s, key: %s",
                    cm, connection, key);

      if (key != null)
      {
         ConnectionRecord cr = new ConnectionRecord(cl, connection, cri);
         ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
            key.getCMToConnectionsMap();

         CopyOnWriteArrayList<ConnectionRecord> conns = cmToConnectionsMap.get(cm);
         if (conns == null)
         {
            conns = new CopyOnWriteArrayList<ConnectionRecord>();
            cmToConnectionsMap.put((ConnectionCacheListener)cm, conns);
         }

         conns.add(cr);
      }
   }

   /**
    * Unregister connection.
    * @param cm connection manager
    * @param connection connection handle
    */
   public void unregisterConnection(org.jboss.jca.core.api.connectionmanager.listener.ConnectionCacheListener cm,
                                    Object connection)
   {
      if (debug)
      {
         CloseConnectionSynchronization cas = getCloseConnectionSynchronization(false);
         if (cas != null)
         {
            cas.remove(connection);
         }

         synchronized (connectionStackTraces)
         {
            connectionStackTraces.remove(connection);
         }
      }

      KeyConnectionAssociation key = peekMetaAwareObject();

      if (trace)
         log.tracef("unregistering connection from connection manager: %s, connection: %s, key: %s",
                    cm, connection, key);

      if (key == null)
         return;

      ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap =
         key.getCMToConnectionsMap();

      CopyOnWriteArrayList<ConnectionRecord> conns = cmToConnectionsMap.get(cm);

      // Can happen if connections are "passed" between contexts
      if (conns == null)
         return; 

      // Note iterator of CopyOnWriteArrayList does not support remove method
      // We use here remove on CopyOnWriteArrayList directly
      for (ConnectionRecord connectionRecord : conns)
      {
         if (connectionRecord.getConnection() == connection)
         {
            conns.remove(connectionRecord);
            return;
         }
      }

      throw new IllegalStateException("Trying to return an unknown connection2! " + connection);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void pushMetaAwareObject(final Object rawKey, Set unsharableResources) throws ResourceException
   {
      LinkedList<Object> stack = currentObjects.get();
      if (stack == null)
      {
         if (trace)
            log.tracef("new stack for key: %s", rawKey);

         stack = new LinkedList<Object>();
         currentObjects.set(stack);
      }
      else
      {
         if (trace)
            log.tracef("old stack for key: %s", rawKey);
      }

      KeyConnectionAssociation key = new KeyConnectionAssociation(rawKey);
      stack.addLast(key);
   }

   /**
    * Describe <code>unregisterConnectionCacheListener</code> method here.
    * This is a shutdown method called by a connection manager.  It will remove all reference
    * to that connection manager from the cache, so cached connections from that manager
    * will never be recoverable.
    * Possibly this method should not exist.
    *
    * @param cm a <code>ConnectionCacheListener</code> value
    */
   public void unregisterConnectionCacheListener(ConnectionCacheListener cm)
   {
      if (trace)
         log.tracef("unregisterConnectionCacheListener: %s", cm);

      Iterator<ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>>> it =
         objectToConnectionManagerMap.values().iterator();

      while (it.hasNext())
      {
         ConcurrentMap<ConnectionCacheListener, CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap = it.next();

         if (cmToConnectionsMap != null)
            cmToConnectionsMap.remove(cm);
      }
   }


   /**
    * Close all connections.
    * @param cmToConnectionsMap connection manager to connections
    * @return true if close
    */
   private boolean closeAll(ConcurrentMap<ConnectionCacheListener,
                            CopyOnWriteArrayList<ConnectionRecord>> cmToConnectionsMap)
   {
      boolean unclosed = false;

      Collection<CopyOnWriteArrayList<ConnectionRecord>> connections = cmToConnectionsMap.values();
      if (connections.size() != 0)
      {
         for (Iterator<CopyOnWriteArrayList<ConnectionRecord>> i = connections.iterator(); i.hasNext();)
         {
            CopyOnWriteArrayList<ConnectionRecord> conns = i.next();
            for (Iterator<ConnectionRecord> j = conns.iterator(); j.hasNext();)
            {
               Object c = (j.next()).getConnection();
               CloseConnectionSynchronization cas = getCloseConnectionSynchronization(true);
               if (cas == null)
               {
                  unclosed = true;
                  closeConnection(c);
               }
               else
               {
                  cas.add(c);
               }
            }
         }
      }

      return unclosed;
   }

   /**
    * Gets close sync. instance.
    * @param createIfNotFound create if not found
    * @return sync. instance
    */
   private CloseConnectionSynchronization getCloseConnectionSynchronization(boolean createIfNotFound)
   {
      try
      {
         Transaction tx = null;
         if (transactionManager != null)
         {
            tx = transactionManager.getTransaction();
         }

         if (tx != null)
         {
            TransactionSynchronizer.lock(tx);
            try
            {
               CloseConnectionSynchronization cas = 
                  (CloseConnectionSynchronization)TransactionSynchronizer.getCCMSynchronization(tx);

               if (cas == null && createIfNotFound && TxUtils.isActive(tx))
               {
                  cas = new CloseConnectionSynchronization();
                  TransactionSynchronizer.registerCCMSynchronization(tx, cas, transactionSynchronizationRegistry);
               }

               return cas;
            }
            finally
            {
               TransactionSynchronizer.unlock(tx);
            }
         }
      }
      catch (Throwable t)
      {
         log.debug("Unable to synchronize with transaction", t);
      }

      return null;
   }


   /**
    * Close connection handle.
    * @param connectionHandle connection handle
    */
   private void closeConnection(Object connectionHandle)
   {
      try
      {
         Throwable exception;

         synchronized (connectionStackTraces)
         {
            exception = connectionStackTraces.remove(connectionHandle);
         }

         Method m = connectionHandle.getClass().getMethod("close", new Class[]{});

         try
         {
            if (exception != null)
            {
               log.closingConnection(connectionHandle, exception);
            }
            else
            {
               log.closingConnection(connectionHandle);
            }

            m.invoke(connectionHandle, new Object[]{});
         }
         catch (Throwable t)
         {
            log.closingConnectionThrowable(t);
         }
      }
      catch (NoSuchMethodException nsme)
      {
         log.closingConnectionNoClose(connectionHandle.getClass().getName());
      }
   }


   /**
    * Close synch. class.
    */
   private class CloseConnectionSynchronization implements Synchronization
   {
      /**Connection handles*/
      CopyOnWriteArraySet<Object> connections = new CopyOnWriteArraySet<Object>();

      /**Closing flag*/
      AtomicBoolean closing = new AtomicBoolean(false);

      /**
       * Creates a new instance.
       */
      public CloseConnectionSynchronization()
      {
      }

      /**
       * Add new connection handle.
       * @param c connection handle
       */
      public  void add(Object c)
      {
         if (!closing.get())
            connections.add(c);
      }

      /**
       * Removes connection handle.
       * @param c connection handle
       */
      public  void remove(Object c)
      {
         if (!closing.get())
            connections.remove(c);
      }

      /**
       * {@inheritDoc}
       */
      public void beforeCompletion()
      {
         // No-action
      }

      /**
       * {@inheritDoc}
       */
      public void afterCompletion(int status)
      {
         closing.set(true);

         for (Iterator<Object> i = connections.iterator(); i.hasNext();)
         {
            closeConnection(i.next());
         }

         connections.clear();
      }
   }

   /**
    * Get the currentObjects.
    * This method is package protected beacause it is intended only for test case use.
    * Please don't use it in your production code.
    *
    * @return the currentObjects.
    */
   final ThreadLocal<LinkedList<Object>> getCurrentObjects()
   {
      return currentObjects;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("CachedConnectionManagerImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[debug=").append(debug);
      sb.append(" error=").append(error);
      sb.append(" transactionManager=").append(transactionManager);
      sb.append(" transactionSynchronizationRegistry=").append(transactionSynchronizationRegistry);
      sb.append(" userTransactionRegistry=").append(userTransactionRegistry);
      sb.append(" currentObjects=").append(currentObjects.get());
      sb.append(" objectToConnectionManagerMap=").append(objectToConnectionManagerMap);
      sb.append(" connectionStackTraces=").append(connectionStackTraces);
      sb.append("]");

      return sb.toString();
   }
}
