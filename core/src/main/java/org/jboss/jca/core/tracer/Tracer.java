/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
    * Get connection listener
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param pooled Is the connection pooled
    * @param interleaving Interleaving flag
    */
   public static void getConnectionListener(String poolName, Object cl, boolean pooled, boolean interleaving)
   {
      if (!interleaving)
      {
         if (pooled)
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.GET_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.GET_CONNECTION_LISTENER_NEW,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
      }
      else
      {
         if (pooled)
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
      }
   }

   /**
    * Return connection listener
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param kill Kill the listener
    * @param interleaving Interleaving flag
    */
   public static void returnConnectionListener(String poolName, Object cl, boolean kill, boolean interleaving)
   {
      if (!interleaving)
      {
         if (!kill)
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.RETURN_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.RETURN_CONNECTION_LISTENER_WITH_KILL,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
      }
      else
      {
         if (!kill)
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
      }
   }

   /**
    * Clear connection listener
    * @param poolName The name of the pool
    * @param cl The connection listener
    */
   public static void clearConnectionListener(String poolName, Object cl)
   {
      log.tracef("%s", new TraceEvent(poolName, TraceEvent.CLEAR_CONNECTION_LISTENER,
                                      Integer.toHexString(System.identityHashCode(cl))));
   }

   /**
    * Enlist connection listener
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param success Outcome
    * @param interleaving Interleaving flag
    */
   public static void enlistConnectionListener(String poolName, Object cl, boolean success, boolean interleaving)
   {
      if (!interleaving)
      {
         if (success)
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.ENLIST_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.ENLIST_CONNECTION_LISTENER_FAILED,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
      }
      else
      {
         if (success)
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
         else
         {
            log.tracef("%s", new TraceEvent(poolName, TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED,
                                            Integer.toHexString(System.identityHashCode(cl))));
         }
      }
   }

   /**
    * Delist connection listener
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param success Is successful
    * @param rollbacked Is the transaction rollbacked
    * @param interleaving Interleaving flag
    */
   public static void delistConnectionListener(String poolName, Object cl, boolean success, boolean rollbacked,
                                               boolean interleaving)
   {
      if (!rollbacked)
      {
         if (!interleaving)
         {
            if (success)
            {
               log.tracef("%s", new TraceEvent(poolName, TraceEvent.DELIST_CONNECTION_LISTENER,
                                               Integer.toHexString(System.identityHashCode(cl))));
            }
            else
            {
               log.tracef("%s", new TraceEvent(poolName, TraceEvent.DELIST_CONNECTION_LISTENER_FAILED,
                                               Integer.toHexString(System.identityHashCode(cl))));
            }
         }
         else
         {
            if (success)
            {
               log.tracef("%s", new TraceEvent(poolName, TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER,
                                               Integer.toHexString(System.identityHashCode(cl))));
            }
            else
            {
               log.tracef("%s", new TraceEvent(poolName, TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER_FAILED,
                                               Integer.toHexString(System.identityHashCode(cl))));
            }
         }
      }
      else
      {
         log.tracef("%s", new TraceEvent(poolName, TraceEvent.DELIST_ROLLEDBACK_CONNECTION_LISTENER,
                                         Integer.toHexString(System.identityHashCode(cl))));
      }
   }

   /**
    * Get connection
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param connection The connection
    */
   public static void getConnection(String poolName, Object cl, Object connection)
   {
      log.tracef("%s", new TraceEvent(poolName, TraceEvent.GET_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection))));
   }

   /**
    * Return connection
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param connection The connection
    */
   public static void returnConnection(String poolName, Object cl, Object connection)
   {
      log.tracef("%s", new TraceEvent(poolName, TraceEvent.RETURN_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection))));
   }

   /**
    * Clear connection
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param connection The connection
    */
   public static void clearConnection(String poolName, Object cl, Object connection)
   {
      log.tracef("%s", new TraceEvent(poolName, TraceEvent.CLEAR_CONNECTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      Integer.toHexString(System.identityHashCode(connection))));
   }

   /**
    * Exception
    * @param poolName The name of the pool
    * @param cl The connection listener
    * @param exception The exception
    */
   public static void exception(String poolName, Object cl, Throwable exception)
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

      log.tracef("%s", new TraceEvent(poolName, TraceEvent.EXCEPTION,
                                      Integer.toHexString(System.identityHashCode(cl)),
                                      sb.toString()));
   }
}
