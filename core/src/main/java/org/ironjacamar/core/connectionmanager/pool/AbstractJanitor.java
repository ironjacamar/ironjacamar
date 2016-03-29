/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.logging.Logger;

/**
 * Abstract janitor
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractJanitor implements Janitor
{
   /** The logger */
   private static Logger log = Logger.getLogger(AbstractJanitor.class);

   /** New line */
   private static String newLine = SecurityActions.getSystemProperty("line.separator");

   /** The pool */
   private Pool pool;

   /** The listeners */
   private Map<ConnectionListener, Exception> listeners;
   
   /**
    * Constructor
    */
   public AbstractJanitor()
   {
      this.pool = null;
      this.listeners = new HashMap<>();
   }
   
   /**
    * {@inheritDoc}
    */
   public synchronized String[] dumpQueuedThreads()
   {
      List<String> result = new ArrayList<String>();

      if (pool != null && pool.getPermits().hasQueuedThreads())
      {
         Collection<Thread> queuedThreads = new ArrayList<Thread>(pool.getPermits().getQueuedThreads());
         for (Thread t : queuedThreads)
         {
            result.add(dumpQueuedThread(t));
         }
      }

      return result.toArray(new String[result.size()]);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Map<String, String> getConnectionListeners()
   {
      Map<String, String> result = new TreeMap<>();

      for (Map.Entry<ConnectionListener, Exception> entry : listeners.entrySet())
      {
         String id = Integer.toHexString(System.identityHashCode(entry.getKey()));
         StringBuilder sb = new StringBuilder();

         for (StackTraceElement ste : entry.getValue().getStackTrace())
         {
            sb = sb.append(ste.getClassName());
            sb = sb.append(":");
            sb = sb.append(ste.getMethodName());
            sb = sb.append(":");
            sb = sb.append(ste.getLineNumber());
            sb = sb.append(newLine);
         }

         result.put(id, sb.toString());
      }
      
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized boolean killConnectionListener(String id)
   {
      ConnectionListener cl = null;
      Iterator<ConnectionListener> it = listeners.keySet().iterator();
      while (cl == null && it.hasNext())
      {
         ConnectionListener l = it.next();
         if (Integer.toHexString(System.identityHashCode(l)).equals(id))
            cl = l;
      }

      if (cl != null)
      {
         try
         {
            pool.returnConnectionListener(cl, true);
         }
         catch (Exception e)
         {
            log.tracef(e, "killConnectionListener(%s)", id);
         }

         return true;
      }
      
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public void registerConnectionListener(ConnectionListener cl)
   {
      synchronized (listeners)
      {
         listeners.put(cl, new Exception());
      }
   }

   /**
    * {@inheritDoc}
    */
   public void unregisterConnectionListener(ConnectionListener cl)
   {
      synchronized (listeners)
      {
         listeners.remove(cl);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setPool(Pool pool)
   {
      this.pool = pool;
   }

   /**
    * Dump a thread
    * @param t The thread
    * @return The stack trace
    */
   private String dumpQueuedThread(Thread t)
   {
      StringBuilder sb = new StringBuilder();

      // Header
      sb = sb.append("Queued thread: ");
      sb = sb.append(t.getName());
      sb = sb.append(newLine);

      // Body
      StackTraceElement[] stes = SecurityActions.getStackTrace(t);
      if (stes != null)
      {
         for (StackTraceElement ste : stes)
         {
            sb = sb.append("  ");
            sb = sb.append(ste.getClassName());
            sb = sb.append(":");
            sb = sb.append(ste.getMethodName());
            sb = sb.append(":");
            sb = sb.append(ste.getLineNumber());
            sb = sb.append(newLine);
         }
      }

      return sb.toString();
   }
}
