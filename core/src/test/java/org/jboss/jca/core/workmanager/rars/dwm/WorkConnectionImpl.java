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
 * WorkConnectionImpl
 *
 * @version $Revision: $
 */
public class WorkConnectionImpl implements WorkConnection
{
   /** ManagedConnection */
   private WorkManagedConnection mc;

   /** ManagedConnectionFactory */
   private WorkManagedConnectionFactory mcf;

   /**
    * Default constructor
    * @param mc WorkManagedConnection
    * @param mcf WorkManagedConnectionFactory
    */
   public WorkConnectionImpl(WorkManagedConnection mc, WorkManagedConnectionFactory mcf)
   {
      this.mc = mc;
      this.mcf = mcf;
   }

   /**
    * {@inheritDoc}
    */
   public void doWork(Work work) throws WorkException
   {
      mc.doWork(work);
   }

   /**
    * {@inheritDoc}
    */
   public void scheduleWork(Work work) throws WorkException
   {
      mc.scheduleWork(work);
   }

   /**
    * {@inheritDoc}
    */
   public long startWork(Work work) throws WorkException
   {
      return mc.startWork(work);
   }

   /**
    * Close
    */
   public void close()
   {
      mc.closeHandle(this);
   }
}
