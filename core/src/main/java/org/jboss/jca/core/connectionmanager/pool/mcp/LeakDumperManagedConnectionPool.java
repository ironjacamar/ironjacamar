/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * A managed connection pool which dumps any leaks at shutdown
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LeakDumperManagedConnectionPool extends SemaphoreArrayListManagedConnectionPool
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           LeakDumperManagedConnectionPool.class.getName());

   /** Dump to special file too */
   private static boolean useFile = false;

   /** Special file name */
   private static String leakFileName = null;

   /** Leak lock */
   private static Object leakLock = new Object();

   /** The tracker map of connection listeners */
   private final ConcurrentMap<ConnectionListener, Throwable> tracker =
      new ConcurrentHashMap<ConnectionListener, Throwable>();

   static
   {
      String f = SecurityActions.getSystemProperty("ironjacamar.leaklog");
      if (f != null && !f.trim().equals(""))
      {
         useFile = true;
         leakFileName = f;
      }
   }

   /**
    * Constructor
    */
   public LeakDumperManagedConnectionPool()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ConnectionListener getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      ConnectionListener cl = super.getConnection(subject, cri);

      tracker.put(cl, new Throwable("ALLOCATION LEAK"));

      return cl;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void returnConnection(ConnectionListener cl, boolean kill, boolean cleanup)
   {
      tracker.remove(cl);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   void doDestroy(ConnectionListener cl)
   {
      super.doDestroy(cl);

      if (tracker.containsKey(cl))
      {
         Throwable t = tracker.get(cl);
         log.connectionLeak(getPoolName(), t);

         if (useFile)
            dump(t);

         tracker.remove(cl);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown()
   {
      super.shutdown();

      if (tracker.size() > 0)
      {
         for (Throwable t : tracker.values())
         {
            log.connectionLeak(getPoolName(), t);

            if (useFile)
               dump(t);
         }
      }
   }

   private void dump(Throwable t)
   {
      synchronized (leakLock)
      {
         OutputStream os = null;
         try
         {
            os = new FileOutputStream(leakFileName, true);

            PrintStream ps = new PrintStream(os, true);

            ps.print("Leak detected in pool: ");
            ps.println(getPoolName());

            t.printStackTrace(ps);

            ps.println();

            ps.flush();
         }
         catch (Exception e)
         {
            log.debug(e.getMessage(), e);
         }
         finally
         {
            if (os != null)
            {
               try
               {
                  os.close();
               }
               catch (IOException ioe)
               {
                  // Ignore
               }
            }
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

      sb.append("LeakDumperManagedConnectionPool@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[super=").append(super.toString());
      sb.append("]");

      return sb.toString();
   }
}
