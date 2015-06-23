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

/**
 * A trace event
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TraceEvent
{
   /** Get connection listener */
   public static final int GET_CONNECTION_LISTENER = 0;

   /** Get connection listener (New) */
   public static final int GET_CONNECTION_LISTENER_NEW = 1;

   /** Get interleaving connection listener */
   public static final int GET_INTERLEAVING_CONNECTION_LISTENER = 2;

   /** Get connection listener interleaving (New) */
   public static final int GET_INTERLEAVING_CONNECTION_LISTENER_NEW = 3;

   /** Return connection listener */
   public static final int RETURN_CONNECTION_LISTENER = 10;

   /** Return connection listener with kill */
   public static final int RETURN_CONNECTION_LISTENER_WITH_KILL = 11;

   /** Return interleaving connection listener */
   public static final int RETURN_INTERLEAVING_CONNECTION_LISTENER = 12;

   /** Return interleaving connection listener with kill */
   public static final int RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL = 13;

   /** Clear connection listener */
   public static final int CLEAR_CONNECTION_LISTENER = 14;

   /** Enlist connection listener */
   public static final int ENLIST_CONNECTION_LISTENER = 20;

   /** Enlist connection listener (Failed) */
   public static final int ENLIST_CONNECTION_LISTENER_FAILED = 21;

   /** Enlist interleaving connection listener */
   public static final int ENLIST_INTERLEAVING_CONNECTION_LISTENER = 22;

   /** Enlist interleaving connection listener (Failed) */
   public static final int ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED = 23;

   /** Delist connection listener */
   public static final int DELIST_CONNECTION_LISTENER = 30;

   /** Delist connection listener (Failed) */
   public static final int DELIST_CONNECTION_LISTENER_FAILED = 31;

   /** Delist interleaving connection listener */
   public static final int DELIST_INTERLEAVING_CONNECTION_LISTENER = 32;

   /** Delist interleaving connection listener (Failed) */
   public static final int DELIST_INTERLEAVING_CONNECTION_LISTENER_FAILED = 33;

   /** Delist rollbacked connection listener */
   public static final int DELIST_ROLLEDBACK_CONNECTION_LISTENER = 34;

   /** Delist rollbacked connection listener (Failed) */
   public static final int DELIST_ROLLEDBACK_CONNECTION_LISTENER_FAILED = 35;

   /** Get connection */
   public static final int GET_CONNECTION = 40;

   /** Return connection */
   public static final int RETURN_CONNECTION = 41;

   /** Clear connection */
   public static final int CLEAR_CONNECTION = 42;

   /** Exception */
   public static final int EXCEPTION = 50;

   /** Create connection listner (GET) */
   public static final int CREATE_CONNECTION_LISTENER_GET = 60;

   /** Create connection listner (PREFILL) */
   public static final int CREATE_CONNECTION_LISTENER_PREFILL = 61;

   /** Create connection listner (INCREMENTER) */
   public static final int CREATE_CONNECTION_LISTENER_INCREMENTER = 62;

   /** Destroy connection listner (RETURN) */
   public static final int DESTROY_CONNECTION_LISTENER_RETURN = 70;

   /** Destroy connection listner (IDLE) */
   public static final int DESTROY_CONNECTION_LISTENER_IDLE = 71;

   /** Destroy connection listner (INVALID) */
   public static final int DESTROY_CONNECTION_LISTENER_INVALID = 72;

   /** Destroy connection listner (FLUSH) */
   public static final int DESTROY_CONNECTION_LISTENER_FLUSH = 73;

   /** Destroy connection listner (ERROR) */
   public static final int DESTROY_CONNECTION_LISTENER_ERROR = 74;

   /** Destroy connection listner (PREFILL) */
   public static final int DESTROY_CONNECTION_LISTENER_PREFILL = 75;

   /** Destroy connection listner (INCREMENTER) */
   public static final int DESTROY_CONNECTION_LISTENER_INCREMENTER = 76;

   /** Managed connection pool create */
   public static final int MANAGED_CONNECTION_POOL_CREATE = 80;

   /** Managed connection pool destroy */
   public static final int MANAGED_CONNECTION_POOL_DESTROY = 81;

   /** Push CCM context */
   public static final int PUSH_CCM_CONTEXT = 90;

   /** Pop CCM context */
   public static final int POP_CCM_CONTEXT = 91;

   /** Register CCM connection */
   public static final int REGISTER_CCM_CONNECTION = 92;

   /** Unregister CCM connection */
   public static final int UNREGISTER_CCM_CONNECTION = 93;

   /** CCM user transaction */
   public static final int CCM_USER_TRANSACTION = 94;

   /** Unknown CCM connection */
   public static final int UNKNOWN_CCM_CONNECTION = 95;

   /** Close CCM connection */
   public static final int CLOSE_CCM_CONNECTION = 96;

   /** Version */
   public static final int VERSION = 100;

   /** The pool */
   private String pool;

   /** The managed connection pool */
   private String mcp;

   /** The thread id */
   private long threadId;

   /** The type */
   private int type;

   /** The time stamp */
   private long timestamp;

   /** The connection listener */
   private String cl;

   /** The first payload */
   private String payload1;

   /** The second payload */
   private String payload2;

   /**
    * Constructor
    * @param pool The pool
    * @param mcp The MCP
    * @param type The event type
    * @param cl The connection listener
    */
   TraceEvent(String pool, String mcp, int type, String cl)
   {
      this(pool, mcp, Thread.currentThread().getId(), type, System.nanoTime(), cl, "", "");
   }

   /**
    * Constructor
    * @param pool The pool
    * @param mcp The MCP
    * @param type The event type
    * @param cl The connection listener
    * @param payload1 The first payload
    */
   TraceEvent(String pool, String mcp, int type, String cl, String payload1)
   {
      this(pool, mcp, Thread.currentThread().getId(), type, System.nanoTime(), cl, payload1, "");
   }

   /**
    * Constructor
    * @param pool The pool
    * @param mcp The MCP
    * @param type The event type
    * @param cl The connection listener
    * @param payload1 The first payload
    * @param payload2 The second payload
    */
   TraceEvent(String pool, String mcp, int type, String cl, String payload1, String payload2)
   {
      this(pool, mcp, Thread.currentThread().getId(), type, System.nanoTime(), cl, payload1, payload2);
   }

   /**
    * Parse constructor
    * @param pool The pool
    * @param mcp The MCP
    * @param threadId The thread id
    * @param type The event type
    * @param timestamp The timestamp
    * @param cl The connection listener
    * @param payload1 The first payload
    * @param payload2 The second payload
    */
   private TraceEvent(String pool, String mcp, long threadId, int type, long timestamp, String cl,
                      String payload1, String payload2)
   {
      this.pool = pool != null ? pool.replace('-', '_') : "Empty"; 
      this.mcp = mcp;
      this.threadId = threadId;
      this.type = type;
      this.timestamp = timestamp;
      this.cl = cl;
      this.payload1 = payload1;
      this.payload2 = payload2;
   }

   /**
    * Get the pool
    * @return The value
    */
   public String getPool()
   {
      return pool;
   }

   /**
    * Get the managed connection pool
    * @return The value
    */
   public String getManagedConnectionPool()
   {
      return mcp;
   }

   /**
    * Get the thread id
    * @return The value
    */
   public long getThreadId()
   {
      return threadId;
   }

   /**
    * Get the type
    * @return The value
    */
   public int getType()
   {
      return type;
   }

   /**
    * Get the timestamp
    * @return The value
    */
   public long getTimestamp()
   {
      return timestamp;
   }

   /**
    * Get the connection listener
    * @return The value
    */
   public String getConnectionListener()
   {
      return cl;
   }

   /**
    * Get the first payload
    * @return The value
    */
   public String getPayload1()
   {
      return payload1;
   }

   /**
    * Get the second payload
    * @return The value
    */
   public String getPayload2()
   {
      return payload2;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("IJTRACER");
      sb.append("-");
      sb.append(pool);
      sb.append("-");
      sb.append(mcp);
      sb.append("-");
      sb.append(Long.toString(threadId));
      sb.append("-");
      sb.append(Integer.toString(type));
      sb.append("-");
      sb.append(Long.toString(timestamp));
      sb.append("-");
      sb.append(cl);
      sb.append("-");
      sb.append(payload1);
      sb.append("-");
      sb.append(payload2);

      return sb.toString();
   }

   /**
    * As text
    * @param event The event
    * @return The text
    */
   public static String asText(TraceEvent event)
   {
      switch (event.getType())
      {
         case GET_CONNECTION_LISTENER:
            return "getConnectionListener()";
         case GET_CONNECTION_LISTENER_NEW:
            return "getConnectionListener(true)";
         case GET_INTERLEAVING_CONNECTION_LISTENER:
            return "getConnectionListener() (I)";
         case GET_INTERLEAVING_CONNECTION_LISTENER_NEW:
            return "getConnectionListener(true) (I)";
         case RETURN_CONNECTION_LISTENER:
            return "returnConnectionListener()";
         case RETURN_CONNECTION_LISTENER_WITH_KILL:
            return "returnConnectionListener(true)";
         case RETURN_INTERLEAVING_CONNECTION_LISTENER:
            return "returnConnectionListener() (I)";
         case RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL:
            return "returnConnectionListener(true) (I)";
         case CLEAR_CONNECTION_LISTENER:
            return "clearConnectionListener()";
         case ENLIST_CONNECTION_LISTENER:
            return "enlistResource()";
         case ENLIST_CONNECTION_LISTENER_FAILED:
            return "enlistResource(false)";
         case ENLIST_INTERLEAVING_CONNECTION_LISTENER:
            return "enlistResource() (I)";
         case ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:
            return "enlistResource(false) (I)";
         case DELIST_CONNECTION_LISTENER:
            return "delistResource()";
         case DELIST_CONNECTION_LISTENER_FAILED:
            return "delistResource(false)";
         case DELIST_INTERLEAVING_CONNECTION_LISTENER:
            return "delistResource() (I)";
         case DELIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:
            return "delistResource(false) (I)";
         case DELIST_ROLLEDBACK_CONNECTION_LISTENER:
            return "delistResource() (R)";
         case DELIST_ROLLEDBACK_CONNECTION_LISTENER_FAILED:
            return "delistResource(false) (R)";
         case GET_CONNECTION:
            return "getConnection(" + event.getPayload1() + ")";
         case RETURN_CONNECTION:
            return "returnConnection(" + event.getPayload1() + ")";
         case CLEAR_CONNECTION:
            return "clearConnection(" + event.getPayload1() + ")";
         case EXCEPTION:
            return "exception";
         case CREATE_CONNECTION_LISTENER_GET:
            return "createConnectionListener(GET)";
         case CREATE_CONNECTION_LISTENER_PREFILL:
            return "createConnectionListener(PREFILL)";
         case CREATE_CONNECTION_LISTENER_INCREMENTER:
            return "createConnectionListener(INCREMENTER)";
         case DESTROY_CONNECTION_LISTENER_RETURN:
            return "destroyConnectionListener(RETURN)";
         case DESTROY_CONNECTION_LISTENER_IDLE:
            return "destroyConnectionListener(IDLE)";
         case DESTROY_CONNECTION_LISTENER_INVALID:
            return "destroyConnectionListener(INVALID)";
         case DESTROY_CONNECTION_LISTENER_FLUSH:
            return "destroyConnectionListener(FLUSH)";
         case DESTROY_CONNECTION_LISTENER_ERROR:
            return "destroyConnectionListener(ERROR)";
         case DESTROY_CONNECTION_LISTENER_PREFILL:
            return "destroyConnectionListener(PREFILL)";
         case DESTROY_CONNECTION_LISTENER_INCREMENTER:
            return "destroyConnectionListener(INCREMENTER)";
         case MANAGED_CONNECTION_POOL_CREATE:
            return "createManagedConnectionPool()";
         case MANAGED_CONNECTION_POOL_DESTROY:
            return "destroyManagedConnectionPool()";
         case PUSH_CCM_CONTEXT:
            return "pushContext()";
         case POP_CCM_CONTEXT:
            return "popContext()";
         case REGISTER_CCM_CONNECTION:
            return "registerConnection()";
         case UNREGISTER_CCM_CONNECTION:
            return "unregisterConnection()";
         case CCM_USER_TRANSACTION:
            return "userTransaction()";
         case UNKNOWN_CCM_CONNECTION:
            return "unknownConnection()";
         case CLOSE_CCM_CONNECTION:
            return "closeConnection()";
         case VERSION:
            return "version()";
         default:
      }

      return "";
   }

   /**
    * Parse a trace event
    * @param data The data string
    * @return The event
    */
   public static TraceEvent parse(String data)
   {
      String[] raw = data.split("-");

      String header = raw[0];
      String p = raw[1];
      String m = raw[2];
      long tid = Long.parseLong(raw[3]);
      int t = Integer.parseInt(raw[4]);
      long ts = Long.parseLong(raw[5]);
      String c = raw[6];
      String pyl = "";
      String py2 = "";

      if (raw.length >= 8)
         pyl = raw[7];
      if (raw.length >= 9)
         py2 = raw[8];

      return new TraceEvent(p, m, tid, t, ts, c, pyl, py2);
   }
}
