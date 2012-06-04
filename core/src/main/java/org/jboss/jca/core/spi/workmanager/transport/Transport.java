/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.spi.workmanager.transport;

import org.jboss.jca.core.api.workmanager.DistributedWorkManager;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

/**
 * The transport interface defines the methods for the physical transport
 * of the work instances for a distributed work manager
 */
public interface Transport
{
   /**
    * Set the distributed work manager
    * @param dwm The value
    */
   public void setDistributedWorkManager(DistributedWorkManager dwm);

   /**
    * Ping time to a distributed work manager
    * @param dwm The id
    * @return The ping time in milliseconds
    */
   public long ping(String dwm);

   /**
    * doWork
    * @param id The work manager id
    * @param work The work
    * @exception WorkException Thrown if an error occurs
    */
   public void doWork(String id, DistributableWork work) throws WorkException;

   /**
    * scheduleWork
    * @param id The work manager id
    * @param work The work
    * @exception WorkException Thrown if an error occurs
    */
   public void scheduleWork(String id, DistributableWork work) throws WorkException;

   /**
    * startWork
    * @param id The work manager id
    * @param work The work
    * @return The delay
    * @exception WorkException Thrown if an error occurs
    */
   public long startWork(String id, DistributableWork work) throws WorkException;
}
