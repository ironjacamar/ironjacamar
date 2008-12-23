/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.jboss.jca.core.api;

import javax.resource.spi.work.WorkException;

import org.jboss.jca.common.api.ThreadPool;
import org.jboss.tm.JBossXATerminator;

/**
 * The JBoss specific work manager interface
 */
public interface WorkManager extends javax.resource.spi.work.WorkManager
{
   /**
    * Retrieve the thread pool
    * @return the thread pool
    */
   public ThreadPool getThreadPool();

   /**
    * Set the thread pool
    * @param threadPool the thread pool
    */
   public void setThreadPool(ThreadPool threadPool);

   /**
    * Get the XATerminator
    * @return The XA terminator
    */
   public JBossXATerminator getXATerminator();

   /**
    * Set the XATerminator
    * @param xaTerminator The XA terminator
    */
   public void setXATerminator(JBossXATerminator xaTerminator);

   /**
    * Start work
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   public void startWork(WorkWrapper wrapper) throws WorkException;

   /**
    * End work
    * @param wrapper the work wrapper
    */
   public void endWork(WorkWrapper wrapper);

   /**
    * Cancel work
    * @param wrapper the work wrapper
    */
   public void cancelWork(WorkWrapper wrapper);
}
