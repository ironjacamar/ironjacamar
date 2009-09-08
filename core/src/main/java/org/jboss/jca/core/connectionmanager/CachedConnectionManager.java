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
package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.transaction.TransactionSynchronizer;
import org.jboss.jca.spi.ComponentStack;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;


import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

import org.jboss.tm.TxUtils;
import org.jboss.tm.usertx.UserTransactionListener;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction.UserTransactionStartedListener;
import org.jboss.util.Strings;

/**
 * CacheConnectionManager.
 * @version $Rev$ $Date$
 *
 */
public class CachedConnectionManager implements
   UserTransactionStartedListener,
   UserTransactionListener,
   ComponentStack
{
   /**Log instance*/
   private static Logger log = Logger.getLogger(CachedConnectionManager.class);
   
   /**Log trace*/
   private boolean trace;
   
   /**Debug flag*/
   private boolean debug;
   
   /**Error flag*/
   private boolean error;
   
   /**Transaction Manager instance*/
   private TransactionManager transactionManager;
   
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
   private final Map<KeyConnectionAssociation, Map<ConnectionCacheListener, Collection<ConnectionRecord>>> 
   objectToConnectionManagerMap = new HashMap<KeyConnectionAssociation, 
                                              Map<ConnectionCacheListener, Collection<ConnectionRecord>>>();

   /**
    * Connection stacktraces
    */
   private Map<Object, Throwable> connectionStackTraces = new WeakHashMap<Object, Throwable>();

   /**
    * Creates a new instance.
    */
   public CachedConnectionManager()
   {
      this.trace = log.isTraceEnabled();
   }
   
   /**
    * Gets transaction manager.
    * @return transaction manager
    */
   public TransactionManager getTransactionManager()
   {
      return this.transactionManager;
   }

   /**
    * Sets transaction manager.
    * @param transactionManager transaction manager
    */
   public void setTransactionManager(TransactionManager transactionManager)
   {
      this.transactionManager = transactionManager;
      
      // FIXME we should be injecting onto the synchronizer directly
      if (this.transactionManager != null)
      {
         TransactionSynchronizer.setTransactionManager(transactionManager);
      }
         
   }
   
   
   /**
    * {@inheritDoc}
    */
   public void userTransactionStarted() throws SystemException
   {
      KeyConnectionAssociation key = peekMetaAwareObject();
      if (trace)
      {
         log.trace("user tx started, key: " + key);  
      }
      if (key == null)
      {
         return; //not participating properly in this management scheme.  
      }

      Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap = key.getCMToConnectionsMap();
      Iterator<Entry<ConnectionCacheListener, Collection<ConnectionRecord>>> cmToConnectionsMapIterator = 
         cmToConnectionsMap.entrySet().iterator();
      
      while (cmToConnectionsMapIterator.hasNext())
      {
         Entry<ConnectionCacheListener, Collection<ConnectionRecord>> entry = cmToConnectionsMapIterator.next();
         ConnectionCacheListener cm = (ConnectionCacheListener) entry.getKey();
         Collection<ConnectionRecord> conns =  entry.getValue();
         
         cm.transactionStarted(conns);
      }
   }
   
   /**
    * 
    * @return stack last meta-aware object
    */
   KeyConnectionAssociation peekMetaAwareObject()
   {
      LinkedList<Object> stack = currentObjects.get();
      if (stack == null)
      {
         return null;  
      }
      
      if (!stack.isEmpty())
      {
         return (KeyConnectionAssociation) stack.getLast();  
      }
      
      else
      {
         return null;  
      }
   }
   
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void popMetaAwareObject(Set unsharableResources) throws ResourceException
   {
      LinkedList<Object> stack = this.currentObjects.get();
      KeyConnectionAssociation oldKey = (KeyConnectionAssociation) stack.removeLast();
      if (this.trace)
      {
         log.trace("popped object: " + Strings.defaultToString(oldKey));  
      }

      if (!stack.contains(oldKey))
      {
         disconnect(oldKey, unsharableResources);
      } // end of if ()
       
      if (this.debug)
      {
         if (closeAll(oldKey.getCMToConnectionsMap()) && this.error)
         {
            throw new ResourceException("Some connections were not closed, " +
                  "see the log for the allocation stacktraces");  
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
   public void registerConnection(ConnectionCacheListener cm, ConnectionListener cl, 
         Object connection, ConnectionRequestInfo cri)
   {
      if (this.debug)
      {
         synchronized (this.connectionStackTraces)
         {
            this.connectionStackTraces.put(connection, new Throwable("STACKTRACE"));
         }
      }

      KeyConnectionAssociation key = peekMetaAwareObject();
      
      if (this.trace)
      {
         log.trace("registering connection from connection manager " + cm +
               ", connection : " + connection + ", key: " + key);  
      }
      
      if (key == null)
      {
         return; //not participating properly in this management scheme.  
      }

      ConnectionRecord cr = new ConnectionRecord(cl, connection, cri);
      Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap = key.getCMToConnectionsMap();
      Collection<ConnectionRecord> conns = cmToConnectionsMap.get(cm);
      if (conns == null)
      {
         conns = new ArrayList<ConnectionRecord>();
         cmToConnectionsMap.put(cm, conns);
      }
      
      conns.add(cr);
   }

   /**
    * Unregister connection.
    * @param cm connection manager
    * @param connection connection handle
    */
   public void unregisterConnection(ConnectionCacheListener cm, Object connection)
   {
      if (this.debug)
      {
         CloseConnectionSynchronization cas = getCloseConnectionSynchronization(false);
         if (cas != null)
         {
            cas.remove(connection);  
         }
         
         synchronized (this.connectionStackTraces)
         {
            this.connectionStackTraces.remove(connection);
         }
      }

      KeyConnectionAssociation key = peekMetaAwareObject();
      
      if (this.trace)
      {
         log.trace("unregistering connection from connection manager " + cm +
               ", object: " + connection + ", key: " + key);  
      }
      
      if (key == null)
      {
         return; //not participating properly in this management scheme.  
      }

      Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap = key.getCMToConnectionsMap();
      Collection<ConnectionRecord> conns = cmToConnectionsMap.get(cm);
      if (conns == null)
      {
         return; // Can happen if connections are "passed" between contexts  
      }
      
      for (Iterator<ConnectionRecord> i = conns.iterator(); i.hasNext();)
      {
         if ((i.next()).getConnection() == connection)
         {
            i.remove();
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
      LinkedList<Object> stack = this.currentObjects.get();
      if (stack == null)
      {
         if (this.trace)
         {
            log.trace("new stack for key: " + Strings.defaultToString(rawKey));  
         }
         
         stack = new LinkedList<Object>();         
         currentObjects.set(stack);         
      }
      else
      {
         if (trace)
         {
            log.trace("old stack for key: " + Strings.defaultToString(rawKey));  
         }
      }
      
      KeyConnectionAssociation key = new KeyConnectionAssociation(rawKey);
      if (!stack.contains(key))
      {
         reconnect(key, unsharableResources);
      }
      
      stack.addLast(key);      
   }
   
   /**
    * The <code>reconnect</code> method gets the cmToConnectionsMap
    * from objectToConnectionManagerMap, copies it to the key, and
    * reconnects all the connections in it.
    *
    * @param key a <code>KeyConnectionAssociation</code> value
    * @param unsharableResources a <code>Set</code> value
    * @exception ResourceException if an error occurs
    */
   @SuppressWarnings("unchecked")
   private void reconnect(KeyConnectionAssociation key, Set unsharableResources) throws ResourceException
   {
      Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap = null;
      synchronized (this.objectToConnectionManagerMap)
      {
         cmToConnectionsMap = this.objectToConnectionManagerMap.get(key);
         if (cmToConnectionsMap == null)
         {
            return;  
         }
      }
      key.setCMToConnectionsMap(cmToConnectionsMap);
      Iterator<Entry<ConnectionCacheListener, Collection<ConnectionRecord>>> cmToConnectionsMapIterator = 
         cmToConnectionsMap.entrySet().iterator();
      
      while (cmToConnectionsMapIterator.hasNext())
      {
         Entry<ConnectionCacheListener, Collection<ConnectionRecord>> entry = cmToConnectionsMapIterator.next();
         ConnectionCacheListener cm = (ConnectionCacheListener) entry.getKey();
         Collection<ConnectionRecord> conns =  entry.getValue();
         
         cm.reconnect(conns, unsharableResources);
      }
   }
   
   /**
    * Disconnect connections.
    * @param key key
    * @param unsharableResources resource
    * @throws ResourceException exception
    */
   @SuppressWarnings("unchecked")
   private void disconnect(KeyConnectionAssociation key, Set unsharableResources) throws ResourceException
   {
      Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap = null;
      cmToConnectionsMap = key.getCMToConnectionsMap();
      if (!cmToConnectionsMap.isEmpty())
      {
         synchronized (objectToConnectionManagerMap)
         {
            objectToConnectionManagerMap.put(key, cmToConnectionsMap);
         }
         Iterator<Entry<ConnectionCacheListener, Collection<ConnectionRecord>>> cmToConnectionsMapIterator = 
            cmToConnectionsMap.entrySet().iterator();
         
         while (cmToConnectionsMapIterator.hasNext())
         {
            Entry<ConnectionCacheListener, Collection<ConnectionRecord>> entry = cmToConnectionsMapIterator.next();
            ConnectionCacheListener cm = (ConnectionCacheListener) entry.getKey();
            Collection<ConnectionRecord> conns =  entry.getValue();
            
            cm.disconnect(conns, unsharableResources);
         }
      }
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
      {
         log.trace("unregisterConnectionCacheListener: " + cm);  
      }
      
      synchronized (objectToConnectionManagerMap)
      {
         Iterator<Map<ConnectionCacheListener, Collection<ConnectionRecord>>> it = 
               this.objectToConnectionManagerMap.values().iterator();
         
         while (it.hasNext())
         {
            Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap = it.next();
            if (cmToConnectionsMap != null)
            {
               cmToConnectionsMap.remove(cm);  
            }
         }
      }
   }
   
   
   /**
    * Close all connections.
    * @param cmToConnectionsMap connection manager to connections
    * @return true if close
    */
   private boolean closeAll(Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap)
   {
      if (!debug)
      {
         return false;  
      }

      boolean unclosed = false;

      Collection<Collection<ConnectionRecord>> connections = cmToConnectionsMap.values();
      if (connections.size() != 0)
      {
         for (Iterator<Collection<ConnectionRecord>> i = connections.iterator(); i.hasNext();)
         {
            Collection<ConnectionRecord> conns = i.next();
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
         if (this.transactionManager != null)
         {
            tx = this.transactionManager.getTransaction();  
         }
        
         if (tx != null)
         {
            TransactionSynchronizer.lock(tx);
            try
            {
               CloseConnectionSynchronization cas = (CloseConnectionSynchronization) 
                                                   TransactionSynchronizer.getCCMSynchronization(tx);
               
               if (cas == null && createIfNotFound && TxUtils.isActive(tx))
               {
                  cas = new CloseConnectionSynchronization();
                  TransactionSynchronizer.registerCCMSynchronization(tx, cas);
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
            exception = (Throwable) connectionStackTraces.remove(connectionHandle);
         }
         
         Method m = connectionHandle.getClass().getMethod("close", new Class[]{});
         
         try
         {
            if (exception != null)
            {
               log.info("Closing a connection for you.  Please close them yourself: " + connectionHandle, exception);  
            }
            else
            {
               log.info("Closing a connection for you.  Please close them yourself: " + connectionHandle);  
            }
            
            m.invoke(connectionHandle, new Object[]{});
         }
         catch (Throwable t)
         {
            log.info("Throwable trying to close a connection for you, please close it yourself", t);
         }
      }
      catch (NoSuchMethodException nsme)
      {
         log.info("Could not find a close method on alleged connection objects.  Please close your own connections.");
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
         if (closing.get())
         {
            return;  
         }
         connections.add(c);
      }

      /**
       * Removes connection handle.
       * @param c connection handle
       */
      public  void remove(Object c)
      {
         if (closing.get())
         {
            return;  
         }
         
         connections.remove(c);
      }

      /**
       * {@inheritDoc}
       */
      public void beforeCompletion()
      {
         //No-action
      }

      /**
       * {@inheritDoc}
       */
      public void afterCompletion(int status)
      {
         this.closing.set(true);
         
         for (Iterator<Object> i = connections.iterator(); i.hasNext();)
         {
            closeConnection(i.next());  
         }
        
         connections.clear();
      }
   }
   
}
