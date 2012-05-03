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
package org.jboss.jca.core.workmanager.rars.dwm;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

/**
 * WorkConnection
 */
public interface WorkConnection
{
   /**
    * doWork
    * @param work The instance
    * @throws WorkException WorkException
    */
   public void doWork(Work work) throws WorkException;

   /**
    * scheduleWork
    * @param work The instance
    * @throws WorkException WorkException
    */
   public void scheduleWork(Work work) throws WorkException;

   /**
    * startWork
    * @param work The instance
    * @return Start delay
    * @throws WorkException WorkException
    */
   public long startWork(Work work) throws WorkException;

   /**
    * Close
    */
   public void close();
}
