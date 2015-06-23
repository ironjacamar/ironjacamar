/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tracer;

import org.jboss.jca.Version;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.jboss.logging.Logger;

/**
 * The tracer class
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Tracer
{
   /** Tracer logger */
   private static Logger log = Logger.getLogger(Tracer.class);
   
   /** Is the tracer enabled */
   private static boolean enabled = log.isTraceEnabled();

   /** Are callstacks recorded */
   private static boolean recordCallstacks = false;

   /** Is the tracer confidential */
   private static boolean confidential = false;

   static
   {
      log.tracef("%s", new TraceEvent(Version.VERSION, "NONE", TraceEvent.VERSION, "NONE"));

      String value = SecurityActions.getSystemProperty("ironjacamar.tracer.callstacks");
      if (value != null && !value.trim().equals(""))
      {
         try
         {
            recordCallstacks = Boolean.valueOf(value);
         }
         catch (Throwable t)
         {
            // Assume record callstacks
            recordCallstacks = true;
         }
      }

      value = SecurityActions.getSystemProperty("ironjacamar.tracer.confidential");
      if (value != null && !value.trim().equals(""))
      {
         try
         {
            confidential = Boolean.valueOf(value);
         }
         catch (Throwable t)
         {
            // Assume confidential
            confidential = true;
         }
      }
   }
   
   /**
    * Is enabled
    * @return The value
    */
   public static boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Set enabled
    * @param v The value
    */
   public static void setEnabled(boolean v)
   {
      enabled = v;
   }

   /**
    * Should callstacks be recorded
    * @return The value
    */
   public static boolean isRecordCallstacks()
   {
      return recordCallstacks && !confidential;
   }

   /**
    * Is confidential
    * @return The value
    */
   public static boolean isConfidential()
   {
      return confidential;
   }

   /**
    * Get connection listener
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param pooled Is the connection pooled
    * @param interleaving Interleaving flag
    * @param callstack The call stack
    */
   public static synchronized void getConnectionListener(String poolName, Object mcp, Object cl,
                                                         boolean pooled, boolean interleaving,
                                                         Throwable callstack)
   {
      if (!interleaving)
      {
         if (pooled)
         {
            log.tracef("%s", new TraceEvent(poolName, Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.GET_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.GET_CONNECTION_LISTENER_NEW,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
      }
      else
      {
         if (pooled)
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
      }
   }

   /**
    * Return connection listener
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param kill Kill the listener
    * @param interleaving Interleaving flag
    * @param callstack The call stack
    */
   public static synchronized void returnConnectionListener(String poolName, Object mcp,
                                                            Object cl, boolean kill, boolean interleaving,
                                                            Throwable callstack)
   {
      if (!interleaving)
      {
         if (!kill)
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.RETURN_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.RETURN_CONNECTION_LISTENER_WITH_KILL,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
      }
      else
      {
         if (!kill)
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            !confidential && callstack != null ? toString(callstack) : ""));
         }
      }
   }

   /**
    * Clear connection listener
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    */
   public static synchronized void clearConnectionListener(String poolName, Object mcp, Object cl)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.CLEAR_CONNECTION_LISTENER,
                                      Integer.toHexString(System.identityHashCode(cl))));
   }

   /**
    * Enlist connection listener
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param tx The transaction
    * @param success Outcome
    * @param interleaving Interleaving flag
    */
   public static synchronized void enlistConnectionListener(String poolName, Object mcp, Object cl,
                                                            String tx,
                                                            boolean success, boolean interleaving)
   {
      if (!interleaving)
      {
         if (success)
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.ENLIST_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            tx.replace('-', '_')));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.ENLIST_CONNECTION_LISTENER_FAILED,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            tx.replace('-', '_')));
         }
      }
      else
      {
         if (success)
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            tx.replace('-', '_')));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            tx.replace('-', '_')));
         }
      }
   }

   /**
    * Delist connection listener
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param tx The transaction
    * @param success Is successful
    * @param rollbacked Is the transaction rollbacked
    * @param interleaving Interleaving flag
    */
   public static synchronized void delistConnectionListener(String poolName, Object mcp, Object cl, String tx,
                                                            boolean success, boolean rollbacked, boolean interleaving)
   {
      if (!rollbacked)
      {
         if (!interleaving)
         {
            if (success)
            {
               log.tracef("%s", new TraceEvent(poolName,
                                               Integer.toHexString(System.identityHashCode(mcp)),
                                               TraceEvent.DELIST_CONNECTION_LISTENER,
                                               Integer.toHexString(System.identityHashCode(cl)),
                                               tx.replace('-', '_')));
            }
            else
            {
               log.tracef("%s", new TraceEvent(poolName,
                                               Integer.toHexString(System.identityHashCode(mcp)),
                                               TraceEvent.DELIST_CONNECTION_LISTENER_FAILED,
                                               Integer.toHexString(System.identityHashCode(cl)),
                                               tx.replace('-', '_')));
            }
         }
         else
         {
            if (success)
            {
               log.tracef("%s", new TraceEvent(poolName,
                                               Integer.toHexString(System.identityHashCode(mcp)),
                                               TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER,
                                               Integer.toHexString(System.identityHashCode(cl)),
                                               tx.replace('-', '_')));
            }
            else
            {
               log.tracef("%s", new TraceEvent(poolName,
                                               Integer.toHexString(System.identityHashCode(mcp)),
                                               TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER_FAILED,
                                               Integer.toHexString(System.identityHashCode(cl)),
                                               tx.replace('-', '_')));
            }
         }
      }
      else
      {
         if (success)
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.DELIST_ROLLEDBACK_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            tx.replace('-', '_')));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName,
                                            Integer.toHexString(System.identityHashCode(mcp)),
                                            TraceEvent.DELIST_ROLLEDBACK_CONNECTION_LISTENER_FAILED,
                                            Integer.toHexString(System.identityHashCode(cl)),
                                            tx.replace('-', '_')));
         }
      }
   }

   /**
    * Get connection
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    */
   public static synchronized void getConnection(String poolName, Object mcp, Object cl, Object connection)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.GET_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection))));
   }

   /**
    * Return connection
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    */
   public static synchronized void returnConnection(String poolName, Object mcp, Object cl, Object connection)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.RETURN_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection))));
   }

   /**
    * Clear connection
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    */
   public static synchronized void clearConnection(String poolName, Object mcp, Object cl, Object connection)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.CLEAR_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection))));
   }

   /**
    * Exception
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param exception The exception
    */
   public static synchronized void exception(String poolName, Object mcp, Object cl, Throwable exception)
   {
      if (!confidential)
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.EXCEPTION,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         toString(exception)));
   }

   /**
    * Create connection listener
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param mc The managed connection
    * @param get A GET operation
    * @param prefill A PREFILL operation
    * @param incrementer An INCREMENTER operation
    * @param callstack The call stack
    */
   public static synchronized void createConnectionListener(String poolName, Object mcp, Object cl, Object mc,
                                                            boolean get, boolean prefill, boolean incrementer,
                                                            Throwable callstack)
   {
      if (get)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.CREATE_CONNECTION_LISTENER_GET,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         Integer.toHexString(System.identityHashCode(mc)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (prefill)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.CREATE_CONNECTION_LISTENER_PREFILL,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         Integer.toHexString(System.identityHashCode(mc)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (incrementer)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.CREATE_CONNECTION_LISTENER_INCREMENTER,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         Integer.toHexString(System.identityHashCode(mc)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
   }

   /**
    * Destroy connection listener
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param ret A RETURN operation
    * @param idle An IDLE operation
    * @param invalid An INVALID operation
    * @param flush A FLUSH operation
    * @param error An ERROR operation
    * @param prefill A PREFILL operation
    * @param incrementer An INCREMENTER operation
    * @param callstack The call stack
    */
   public static synchronized void destroyConnectionListener(String poolName, Object mcp, Object cl,
                                                             boolean ret, boolean idle, boolean invalid,
                                                             boolean flush, boolean error,
                                                             boolean prefill, boolean incrementer,
                                                             Throwable callstack)
   {
      if (ret)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.DESTROY_CONNECTION_LISTENER_RETURN,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (idle)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.DESTROY_CONNECTION_LISTENER_IDLE,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (invalid)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.DESTROY_CONNECTION_LISTENER_INVALID,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (flush)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.DESTROY_CONNECTION_LISTENER_FLUSH,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (error)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.DESTROY_CONNECTION_LISTENER_ERROR,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (prefill)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.DESTROY_CONNECTION_LISTENER_PREFILL,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
      else if (incrementer)
      {
         log.tracef("%s", new TraceEvent(poolName,
                                         Integer.toHexString(System.identityHashCode(mcp)),
                                         TraceEvent.DESTROY_CONNECTION_LISTENER_INCREMENTER,
                                         Integer.toHexString(System.identityHashCode(cl)),
                                         !confidential && callstack != null ? toString(callstack) : ""));
      }
   }

   /**
    * Create managed connection pool
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    */
   public static synchronized void createManagedConnectionPool(String poolName, Object mcp)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.MANAGED_CONNECTION_POOL_CREATE,
                                      "NONE"));
   }

   /**
    * Destroy managed connection pool
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    */
   public static synchronized void destroyManagedConnectionPool(String poolName, Object mcp)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.MANAGED_CONNECTION_POOL_DESTROY,
                                      "NONE"));
   }

   /**
    * Push CCM context
    * @param key The frame key
    * @param callstack The call stack
    */
   public static synchronized void pushCCMContext(String key, Throwable callstack)
   {
      log.tracef("%s", new TraceEvent("CachedConnectionManager", "NONE", TraceEvent.PUSH_CCM_CONTEXT,
                                      "NONE", key, callstack != null ? toString(callstack) : ""));
   }

   /**
    * Pop CCM context
    * @param key The frame key
    * @param callstack The call stack
    */
   public static synchronized void popCCMContext(String key, Throwable callstack)
   {
      log.tracef("%s", new TraceEvent("CachedConnectionManager", "NONE", TraceEvent.POP_CCM_CONTEXT,
                                      "NONE", key, callstack != null ? toString(callstack) : ""));
   }

   /**
    * Register CCM connection
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    * @param key The frame key
    */
   public static synchronized void registerCCMConnection(String poolName, Object mcp, Object cl,
                                                         Object connection, String key)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.REGISTER_CCM_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection)),
                                      key));
   }

   /**
    * Unregister CCM connection
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    * @param key The frame key
    */
   public static synchronized void unregisterCCMConnection(String poolName, Object mcp, Object cl,
                                                           Object connection, String key)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.UNREGISTER_CCM_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection)),
                                      key));
   }

   /**
    * Unknown CCM connection
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    * @param key The frame key
    */
   public static synchronized void unknownCCMConnection(String poolName, Object mcp, Object cl,
                                                        Object connection, String key)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.UNKNOWN_CCM_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection)),
                                      key));
   }

   /**
    * Close CCM connection
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    * @param key The frame key
    */
   public static synchronized void closeCCMConnection(String poolName, Object mcp, Object cl,
                                                      Object connection, String key)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.CLOSE_CCM_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection)),
                                      key));
   }

   /**
    * CCM user transaction
    * @param poolName The name of the pool
    * @param mcp The managed connection pool
    * @param cl The connection listener
    * @param connection The connection
    * @param key The frame key
    */
   public static synchronized void ccmUserTransaction(String poolName, Object mcp, Object cl,
                                                      Object connection, String key)
   {
      log.tracef("%s", new TraceEvent(poolName,
                                      Integer.toHexString(System.identityHashCode(mcp)),
                                      TraceEvent.CCM_USER_TRANSACTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection)),
                                      key));
   }

   /**
    * Throwable to string
    * @param exception The exception
    * @return The string representation
    */
   private static synchronized String toString(Throwable exception)
   {
      CharArrayWriter caw = new CharArrayWriter();
      PrintWriter pw = new PrintWriter(caw, true);
      exception.printStackTrace(pw);
      pw.flush();

      char[] data = caw.toCharArray();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < data.length; i++)
      {
         char c = data[i];
         if (c == '\n')
         {
            sb = sb.append('|');
         }
         else if (c == '\r')
         {
            sb = sb.append('/');
         }
         else if (c == '\t')
         {
            sb = sb.append('\\');
         }
         else if (c == ' ')
         {
            sb = sb.append('_');
         }
         else
         {
            sb = sb.append(c);
         }
      }

      return sb.toString();
   }
}
