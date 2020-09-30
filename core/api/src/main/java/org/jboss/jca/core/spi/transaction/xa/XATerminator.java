/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.spi.transaction.xa;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.transaction.xa.Xid;

/**
 * Extends XATerminator to include registration calls
 *
 * @author <a href="abrock@redhat.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface XATerminator extends javax.resource.spi.XATerminator
{
   /**
    * Invoked for transaction inflow of work
    * 
    * @param work The work starting
    * @param xid The xid of the work
    * @param timeout The transaction timeout
    * @throws WorkCompletedException with error code WorkException.TX_CONCURRENT_WORK_DISALLOWED
    *         when work is already present for the xid or whose completion is in progress, only
    *         the global part of the xid must be used for this check.
    */
   public void registerWork(Work work, Xid xid, long timeout) throws WorkCompletedException;
   
   /**
    * Invoked for transaction inflow of work
    * 
    * @param work The work starting
    * @param xid The xid of the work
    * @throws WorkCompletedException With error code WorkException.TX_RECREATE_FAILED if it is unable 
    *         to recreate the transaction context
    */
   public void startWork(Work work, Xid xid) throws WorkCompletedException;

   /**
    * Invoked when transaction inflow work ends
    * 
    * @param work The work ending
    * @param xid The xid of the work
    */
   public void endWork(Work work, Xid xid);

   /**
    * Invoked when the work fails
    * 
    * @param work The work ending
    * @param xid The xid of the work
    */
   public void cancelWork(Work work, Xid xid);
}
