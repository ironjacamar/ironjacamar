/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2006, Red Hat Inc, and individual contributors
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

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PoolFiller
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:sstark@redhat.com">Scott Stark</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class PoolFiller implements Runnable
{

   /** Singleton instance */
   private static final PoolFiller FILLER = new PoolFiller();

   /** Pools list */
   private final LinkedList<FillRequest> pools = new LinkedList<FillRequest>();

   /** Filler thread */
   private final Thread fillerThread;

   /** Thread name */
   private static final String THREAD_FILLER_NAME = "JCA PoolFiller";

   /**Thread is configured or not*/
   private AtomicBoolean threadStarted = new AtomicBoolean(false);

   /**
    * Fill given pool
    * @param fr The fill request
    */
   static void fillPool(FillRequest fr)
   {
      FILLER.internalFillPool(fr);
   }

   /**
    * Creates a new pool filler instance.
    */
   PoolFiller()
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
            FillRequest fr = null;

            synchronized (pools)
            {
               empty = pools.isEmpty();
               if (!empty)
                  fr = pools.removeFirst();
            }

            if (!empty)
            {
               fr.getManagedConnectionPool().fillTo(fr.getFillSize());
            }
         }

         try 
         {
            synchronized (pools)
            {
               while (pools.isEmpty())
               {
                  pools.wait();                        
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
    * Fill pool
    * @param fr The fill request
    */
   private void internalFillPool(FillRequest fr)
   {
      if (this.threadStarted.compareAndSet(false, true))         
      {
         this.fillerThread.start();
      }
      
      synchronized (pools)
      {
         if (!pools.contains(fr))
         {
            pools.addLast(fr);
            pools.notifyAll();
         }
      }
   }
}
