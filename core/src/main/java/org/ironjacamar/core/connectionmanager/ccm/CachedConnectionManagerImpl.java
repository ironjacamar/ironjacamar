/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.connectionmanager.ccm;

import org.ironjacamar.core.CoreBundle;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;
import org.ironjacamar.core.spi.transaction.TxUtils;
import org.ironjacamar.core.tracer.Tracer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * CacheConnectionManager.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class CachedConnectionManagerImpl implements CachedConnectionManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
                                                           CachedConnectionManager.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** Synchronization key */
   private static final String CLOSE_CONNECTION_SYNCHRONIZATION = "CLOSE_CONNECTION_SYNCHRONIZATION";

   /** Debug */
   private boolean debug;

   /** Error */
   private boolean error;

   /** Ignore unknown connections */
   private boolean ignoreConnections;

   /** Transaction integration */
   private TransactionIntegration transactionIntegration;

   /** Thread contexts - stack based */
   private ThreadLocal<LinkedList<Context>> threadContexts = new ThreadLocal<LinkedList<Context>>();

   /** Connection stack traces */
   private Map<Object, Throwable> connectionStackTraces = new WeakHashMap<Object, Throwable>();

   /**
    * Constructor
    * @param transactionIntegration The transaction integration
    */
   public CachedConnectionManagerImpl(TransactionIntegration transactionIntegration)
   {
      this.debug = false;
      this.error = false;
      this.ignoreConnections = false;
      this.transactionIntegration = transactionIntegration;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDebug()
   {
      return debug;
   }

   /**
    * {@inheritDoc}
    */
   public void setDebug(boolean v)
   {
      debug = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isError()
   {
      return error;
   }

   /**
    * {@inheritDoc}
    */
   public void setError(boolean v)
   {
      error = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isIgnoreUnknownConnections()
   {
      return ignoreConnections;
   }

   /**
    * {@inheritDoc}
    */
   public void setIgnoreUnknownConnections(boolean v)
   {
      ignoreConnections = v;
   }

   /**
    * {@inheritDoc}
    */
   public void userTransactionStarted() throws SystemException
   {
      Context context = currentContext();

      log.tracef("user tx started, context: %s", context);

      if (context != null)
      {
         for (org.ironjacamar.core.connectionmanager.ConnectionManager cm : context.getConnectionManagers())
         {
            if (cm.getTransactionSupport() != TransactionSupportLevel.NoTransaction)
            {
               List<org.ironjacamar.core.connectionmanager.listener.ConnectionListener> cls =
                  context.getConnectionListeners(cm);

               if (!cls.isEmpty())
               {
                  Map<Credential, org.ironjacamar.core.connectionmanager.listener.ConnectionListener> enlistmentMap =
                     new HashMap<>();

                  List<org.ironjacamar.core.connectionmanager.listener.ConnectionListener> cleanup =
                     new ArrayList<>();
                  
                  try
                  {
                     for (org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl : cls)
                     {
                        if (enlistmentMap.get(cl.getCredential()) == null)
                        {
                           enlistmentMap.put(cl.getCredential(), cl);
                        }
                        else
                        {
                           // Merge
                           org.ironjacamar.core.connectionmanager.listener.ConnectionListener existing =
                              enlistmentMap.get(cl.getCredential());

                           for (Object c : cl.getConnections())
                           {
                              existing.getManagedConnection().associateConnection(c);
                              existing.addConnection(c);

                              context.switchConnectionListener(c, cl, existing);
                           }

                           cl.clearConnections();

                           cleanup.add(cl);
                        }
                     }

                     // Enlist ConnectionListener's
                     for (org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl :
                             enlistmentMap.values())
                     {
                        if (Tracer.isEnabled())
                        {
                           for (Object c : cl.getConnections())
                           {
                              Tracer.ccmUserTransaction(cl.getManagedConnectionPool().getPool()
                                                        .getConfiguration().getId(),
                                                        cl.getManagedConnectionPool(),
                                                        cl, c, context.toString());
                           }
                        }

                        cm.transactionStarted(cl);
                     }

                     // Do cleanup
                     for (org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl : cleanup)
                     {
                        context.removeConnectionListener(cm, cl);
                        cm.returnConnectionListener(cl, false);
                     }
                  }
                  catch (Exception e)
                  {
                     SystemException se = new SystemException();
                     se.initCause(e);
                     throw se;
                  }
               }
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void pushContext(Object contextKey, Set unsharableResources) throws ResourceException
   {
      LinkedList<Context> stack = threadContexts.get();
      Context context = new Context(contextKey);

      if (stack == null)
      {
         log.tracef("push: new stack for context: %s", context);

         stack = new LinkedList<Context>();
         threadContexts.set(stack);
      }
      else if (stack.isEmpty())
      {
         log.tracef("push: new stack for context: %s", context);
      }
      else
      {
         log.tracef("push: old stack for context: %s", stack.getLast());
         log.tracef("push: new stack for context: %s", context);
      }

      if (Tracer.isEnabled())
         Tracer.pushCCMContext(context.toString(), new Throwable("CALLSTACK"));

      stack.addLast(context);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void popContext(Set unsharableResources) throws ResourceException
   {
      LinkedList<Context> stack = threadContexts.get();

      if (stack == null || stack.isEmpty())
         return;

      Context context = stack.removeLast();

      if (log.isTraceEnabled())
      {
         if (!stack.isEmpty())
         {
            log.tracef("pop: old stack for context: %s", context);
            log.tracef("pop: new stack for context: %s", stack.getLast());
         }
         else
         {
            log.tracef("pop: old stack for context: %s", context);
         }
      }

      if (Tracer.isEnabled())
         Tracer.popCCMContext(context.toString(), new Throwable("CALLSTACK"));

      if (debug && closeAll(context) && error)
      {
         throw new ResourceException(bundle.someConnectionsWereNotClosed());
      }

      context.clear();
   }

   /**
    * Look at the current context
    * @return The value
    */
   private Context currentContext()
   {
      LinkedList<Context> stack = threadContexts.get();

      if (stack != null && !stack.isEmpty())
      {
         return stack.getLast();
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void registerConnection(org.ironjacamar.core.api.connectionmanager.ConnectionManager cm,
                                  org.ironjacamar.core.api.connectionmanager.listener.ConnectionListener cl,
                                  Object connection)
   {
      if (debug)
      {
         synchronized (connectionStackTraces)
         {
            connectionStackTraces.put(connection, new Throwable("STACKTRACE"));
         }
      }

      Context context = currentContext();

      log.tracef("registering connection from connection manager: %s, connection : %s, context: %s",
                 cm, connection, context);

      if (context != null)
      {
         // Use internal API
         org.ironjacamar.core.connectionmanager.ConnectionManager iCm =
            (org.ironjacamar.core.connectionmanager.ConnectionManager)cm;

         org.ironjacamar.core.connectionmanager.listener.ConnectionListener iCl =
            (org.ironjacamar.core.connectionmanager.listener.ConnectionListener)cl;

         if (Tracer.isEnabled())
         {
            Tracer.registerCCMConnection(iCl.getManagedConnectionPool().getPool()
                                         .getConfiguration().getId(),
                                         iCl.getManagedConnectionPool(),
                                         iCl, connection, context.toString());
         }

         context.registerConnection(iCm, iCl, connection);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void unregisterConnection(org.ironjacamar.core.api.connectionmanager.ConnectionManager cm,
                                    org.ironjacamar.core.api.connectionmanager.listener.ConnectionListener cl,
                                    Object connection)
   {
      if (debug)
      {
         CloseConnectionSynchronization ccs = getCloseConnectionSynchronization(false);
         if (ccs != null)
         {
            ccs.remove(connection);
         }

         synchronized (connectionStackTraces)
         {
            connectionStackTraces.remove(connection);
         }
      }

      Context context = currentContext();

      log.tracef("unregistering connection from connection manager: %s, connection: %s, context: %s",
                 cm, connection, context);

      if (context == null)
         return;

      // Use internal API
      org.ironjacamar.core.connectionmanager.ConnectionManager iCm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)cm;

      org.ironjacamar.core.connectionmanager.listener.ConnectionListener iCl =
         (org.ironjacamar.core.connectionmanager.listener.ConnectionListener)cl;

      if (context.unregisterConnection(iCm, iCl, connection))
      {
         if (Tracer.isEnabled())
         {
            Tracer.unregisterCCMConnection(iCl.getManagedConnectionPool().getPool()
                                           .getConfiguration().getId(),
                                           iCl.getManagedConnectionPool(),
                                           iCl, connection, context.toString());
         }
      }
      else
      {
         if (Tracer.isEnabled())
         {
            Tracer.unknownCCMConnection(iCl.getManagedConnectionPool().getPool()
                                        .getConfiguration().getId(),
                                        iCl.getManagedConnectionPool(),
                                        iCl, connection, context.toString());
         }

         if (!ignoreConnections)
            throw new IllegalStateException(); //bundle.tryingToReturnUnknownConnection(connection.toString()));
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getNumberOfConnections()
   {
      if (!debug)
         return 0;

      synchronized (connectionStackTraces)
      {
         return connectionStackTraces.size();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, String> listConnections()
   {
      if (!debug)
         return Collections.unmodifiableMap(Collections.EMPTY_MAP);

      synchronized (connectionStackTraces)
      {
         Map<String, String> result = new HashMap<String, String>();

         for (Map.Entry<Object, Throwable> entry : connectionStackTraces.entrySet())
         {
            Object key = entry.getKey();
            Throwable stackTrace = entry.getValue();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true);
            stackTrace.printStackTrace(ps);

            result.put(key.toString(), baos.toString());
         }

         return Collections.unmodifiableMap(result);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void start()
   {
      if (transactionIntegration != null && transactionIntegration.getUserTransactionRegistry() != null)
         transactionIntegration.getUserTransactionRegistry().addListener(this);

      log.debugf("start: %s", this.toString());
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
      log.debugf("stop: %s", this.toString());

      if (transactionIntegration != null && transactionIntegration.getUserTransactionRegistry() != null)
         transactionIntegration.getUserTransactionRegistry().removeListener(this);
   }

   /**
    * Close all connections for a context
    * @param context The context
    * @return True if connections were closed, otherwise false
    */
   private boolean closeAll(Context context)
   {
      boolean unclosed = false;
      CloseConnectionSynchronization ccs = getCloseConnectionSynchronization(true);

      for (org.ironjacamar.core.connectionmanager.ConnectionManager cm : context.getConnectionManagers())
      {
         for (org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl :
                 context.getConnectionListeners(cm))
         {
            for (Object c : context.getConnections(cl))
            {
               if (ccs == null)
               {
                  unclosed = true;

                  if (Tracer.isEnabled())
                  {
                     Tracer.closeCCMConnection(cl.getManagedConnectionPool().getPool()
                                               .getConfiguration().getId(),
                                               cl.getManagedConnectionPool(),
                                               cl, c, context.toString());
                  }

                  closeConnection(c);
               }
               else
               {
                  ccs.add(c);
               }
            }
         }
      }

      return unclosed;
   }

   /**
    * Get the CloseConnectionSynchronization instance
    * @param createIfNotFound Create if not found
    * @return The value
    */
   private CloseConnectionSynchronization getCloseConnectionSynchronization(boolean createIfNotFound)
   {
      try
      {
         Transaction tx = null;
         if (transactionIntegration != null)
            tx = transactionIntegration.getTransactionManager().getTransaction();

         if (tx != null && TxUtils.isActive(tx))
         {
            CloseConnectionSynchronization ccs = (CloseConnectionSynchronization)
               transactionIntegration.getTransactionSynchronizationRegistry().
               getResource(CLOSE_CONNECTION_SYNCHRONIZATION);
            
            if (ccs == null && createIfNotFound)
            {
               ccs = new CloseConnectionSynchronization();

               transactionIntegration.getTransactionSynchronizationRegistry().
                  putResource(CLOSE_CONNECTION_SYNCHRONIZATION, ccs);

               transactionIntegration.getTransactionSynchronizationRegistry().registerInterposedSynchronization(ccs);
            }

            return ccs;
         }
      }
      catch (Throwable t)
      {
         log.debug("Unable to synchronize with transaction", t);
      }

      return null;
   }

   /**
    * Close connection handle
    * @param connectionHandle Connection handle
    */
   private void closeConnection(Object connectionHandle)
   {
      try
      {
         Throwable exception = null;

         synchronized (connectionStackTraces)
         {
            exception = connectionStackTraces.remove(connectionHandle);
         }

         Method m = SecurityActions.getMethod(connectionHandle.getClass(), "close", new Class[]{});

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
    * Close unclosed connections in beforeCompletion
    */
   private class CloseConnectionSynchronization implements Synchronization
   {
      /** Connection handles */
      private List<Object> connections;

      /** Closing flag */
      private AtomicBoolean closing;

      /**
       * Constructor
       */
      public CloseConnectionSynchronization()
      {
         this.connections = new ArrayList<Object>();
         this.closing = new AtomicBoolean(false);
      }

      /**
       * Add a connection handle
       * @param c Connection handle
       */
      public void add(Object c)
      {
         if (!closing.get())
            connections.add(c);
      }

      /**
       * Remove a connection handle
       * @param c Connection handle
       */
      public void remove(Object c)
      {
         if (!closing.get())
            connections.remove(c);
      }

      /**
       * {@inheritDoc}
       */
      public void beforeCompletion()
      {
         closeAll();
      }

      /**
       * {@inheritDoc}
       */
      public void afterCompletion(int status)
      {
         // Rollback scenario
         closeAll();
      }

      private void closeAll()
      {
         closing.set(true);

         if (!connections.isEmpty())
         {
            for (Object c : connections)
            {
               closeConnection(c);
            }

            connections.clear();
         }
      }
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
      sb.append(" ignoreConnections=").append(ignoreConnections);
      sb.append(" transactionIntegration=").append(transactionIntegration);
      sb.append(" threadContexts=").append(threadContexts.get());
      sb.append(" connectionStackTraces=").append(connectionStackTraces);
      sb.append("]");

      return sb.toString();
   }
}
