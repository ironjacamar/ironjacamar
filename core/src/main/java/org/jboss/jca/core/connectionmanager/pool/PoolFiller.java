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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.common.util.SecurityActions;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;

/**
 * PoolFiller
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$ $Date$
 */
public class PoolFiller implements Runnable
{
   /** Log instance */
   private static Logger log = Logger.getLogger(PoolFiller.class);

   /** Singleton instance */
   private static final PoolFiller FILLER = new PoolFiller();

   /** Pools list */
   private final LinkedBlockingQueue<InternalManagedConnectionPool> pools = 
      new LinkedBlockingQueue<InternalManagedConnectionPool>();

   /** Filler thread */
   private final Thread fillerThread;

   /** Thread name */
   private static final String THREAD_FILLER_NAME = "JCA PoolFiller";

   /** Lock instance */
   private ReentrantLock lock = new ReentrantLock();

   /** Lock condition */
   private Condition condition = this.lock.newCondition();
   
   /**Thread is configured or not*/
   private AtomicBoolean threadStarted = new AtomicBoolean(false);

   /**
    * Fill given pool.
    * 
    * @param mcp internal managed connection pool
    */
   public static void fillPool(InternalManagedConnectionPool mcp)
   {
      FILLER.internalFillPool(mcp);
   }

   /**
    * Creates a new pool filler instance.
    */
   public PoolFiller()
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
      SecurityActions.setTCL(myClassLoader);

      // keep going unless interrupted
      while (true)
      {
         InternalManagedConnectionPool mcp = null;
         try
         {
            // keep iterating through pools till empty, exception escapes.
            while (true)
            {
               mcp = pools.remove();

               if (mcp == null)
               {
                  break;
               }

               mcp.fillToMin();
            }
         }
         catch (Exception e)
         {
            log.warn("Exception is occured while filling pool : " + mcp);
         }

         try
         {
            this.lock.lock();

            while (pools.isEmpty())
            {
               condition.await();
            }
         }
         catch (InterruptedException ie)
         {
            return;

         }
         finally
         {
            this.lock.unlock();
         }
      }
   }

   /**
    * Internal fill pool.
    * @param mcp connection pool
    */
   private void internalFillPool(InternalManagedConnectionPool mcp)
   {
      if (this.threadStarted.compareAndSet(false, true))         
      {
         this.fillerThread.start();
      }
      
      this.pools.add(mcp);
      this.condition.signal();
   }
}
