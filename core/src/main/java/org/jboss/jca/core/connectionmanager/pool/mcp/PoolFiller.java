/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.logging.Logger;

/**
 * PoolFiller
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev: $
 */
class PoolFiller implements Runnable
{
   /** Log instance */
   private static Logger log = Logger.getLogger(PoolFiller.class);

   /** Singleton instance */
   private static final PoolFiller FILLER = new PoolFiller();

   /** Pools list */
   private final LinkedList<ManagedConnectionPool> pools = new LinkedList<ManagedConnectionPool>();

   /** Filler thread */
   private final Thread fillerThread;

   /** Thread name */
   private static final String THREAD_FILLER_NAME = "JCA PoolFiller";

   /**Thread is configured or not*/
   private AtomicBoolean threadStarted = new AtomicBoolean(false);

   /**
    * Fill given pool.
    * 
    * @param mcp internal managed connection pool
    */
   static void fillPool(ManagedConnectionPool mcp)
   {
      FILLER.internalFillPool(mcp);
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
      final ClassLoader myClassLoader = getClass().getClassLoader();
      SecurityActions.setThreadContextClassLoader(myClassLoader);

      while (true)
      {
         ManagedConnectionPool mcp = null;

         while (true)
         {
            synchronized (pools)
            {
               mcp = pools.removeFirst();
            }

            if (mcp == null) 
               break;
                        
            mcp.fillToMin();
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
    *  fill pool.
    * @param mcp connection pool
    */
   private void internalFillPool(ManagedConnectionPool mcp)
   {
      if (this.threadStarted.compareAndSet(false, true))         
      {
         this.fillerThread.start();
      }
      
      synchronized (pools)
      {
         pools.addLast(mcp);
         pools.notifyAll();
      }
   }
}
