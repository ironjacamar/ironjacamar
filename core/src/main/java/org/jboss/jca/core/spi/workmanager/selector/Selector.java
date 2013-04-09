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

package org.jboss.jca.core.spi.workmanager.selector;

import org.jboss.jca.core.spi.workmanager.Address;

import javax.resource.spi.work.DistributableWork;

/**
 * The selector interface defines how a node is selected when a
 * work instance should be distributed
 */
public interface Selector
{
   /**
    * Select a distributed work manager
    * @param own The address of the invoking distributed work manager
    * @param work The work instance
    * @return The address of the selected distributed work manager; <code>null</code> if none could be selected
    */
   public Address selectDistributedWorkManager(Address own, DistributableWork work);
}
