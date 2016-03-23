/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.core.connectionmanager.pool;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Capacity filler
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class CapacityFiller implements Runnable
{
   /** Singleton instance */
   private static final CapacityFiller INSTANCE = new CapacityFiller();

   /** Managed connection pool list */
   private final LinkedList<CapacityRequest> crs = new LinkedList<CapacityRequest>();

   /** Filler thread */
   private final Thread fillerThread;

   /** Thread name */
   private static final String THREAD_FILLER_NAME = "JCA CapacityFiller";

   /**Thread is configured or not*/
   private AtomicBoolean threadStarted = new AtomicBoolean(false);

   /**
    * Schedule capacity request
    * @param cr The value
    */
   public static void schedule(CapacityRequest cr)
   {
      INSTANCE.internalSchedule(cr);
   }

   /**
    * Constructor
    */
   CapacityFiller()
   {
      fillerThread = new Thread(this, THREAD_FILLER_NAME);
      fillerThread.setDaemon(true);
   }

   /**
    * {@inheritDoc}
    */
   public void run()
   {
      final ClassLoader myClassLoader = SecurityActions.getClassLoader(getClass());
      SecurityActions.setThreadContextClassLoader(myClassLoader);

      while (true)
      {
         boolean empty = false;

         while (!empty)
         {
            CapacityRequest cr = null;

            synchronized (crs)
            {
               empty = crs.isEmpty();
               if (!empty)
                  cr = crs.removeFirst();
            }

            if (!empty)
            {
               cr.getManagedConnectionPool().increaseCapacity();
            }
         }

         try 
         {
            synchronized (crs)
            {
               while (crs.isEmpty())
               {
                  crs.wait();                        
               }
            }
         }
         catch (InterruptedException ie)
         {
            Thread.currentThread().interrupt();
            return;
         }
      }
   }

   /**
    * Internal: Schedule
    * @param cr The value
    */
   private void internalSchedule(CapacityRequest cr)
   {
      if (this.threadStarted.compareAndSet(false, true))         
      {
         this.fillerThread.start();
      }
      
      // Multiple instances of the same ManagedConnectionPool is allowed
      synchronized (crs)
      {
         crs.addLast(cr);
         crs.notifyAll();
      }
   }
}
