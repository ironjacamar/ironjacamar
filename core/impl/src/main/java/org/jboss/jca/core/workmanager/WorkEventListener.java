/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.workmanager;

import org.jboss.jca.core.api.workmanager.StatisticsExecutor;
import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

import javax.resource.spi.work.WorkAdapter;
import javax.resource.spi.work.WorkEvent;


/**
 * Work event listener
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkEventListener extends WorkAdapter
{
   /** Short or long running work */
   private boolean isLong;

   /** The short thread pool */
   private StatisticsExecutor shortThreadPool;

   /** The long thread pool */
   private StatisticsExecutor longThreadPool;

   /** The address */
   private Address address;

   /** The transport */
   private Transport transport;

   /**
    * Constructor
    * @param isLong Is long running work instance
    * @param shortThreadPool The short running thread pool
    * @param longThreadPool The long running thread pool
    * @param address The address
    * @param transport The transport
    */
   public WorkEventListener(boolean isLong,
                            StatisticsExecutor shortThreadPool,
                            StatisticsExecutor longThreadPool,
                            Address address,
                            Transport transport)
   {
      this.isLong = isLong;
      this.shortThreadPool = shortThreadPool;
      this.longThreadPool = longThreadPool;
      this.address = address;
      this.transport = transport;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void workCompleted(WorkEvent e)
   {
      done();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void workRejected(WorkEvent e)
   {
      done();
   }

   /**
    * Send the done signal to other nodes
    * We are adding 1 to the result, since the thread officially has been released yet, but will be shortly
    */
   private void done()
   {
      if (longThreadPool != null && isLong)
      {
         transport.updateLongRunningFree(address,
                                         longThreadPool.getNumberOfFreeThreads() + 1);
      }
      else
      {
         transport.updateShortRunningFree(address,
                                          shortThreadPool.getNumberOfFreeThreads() + 1);
      }
   }
}
