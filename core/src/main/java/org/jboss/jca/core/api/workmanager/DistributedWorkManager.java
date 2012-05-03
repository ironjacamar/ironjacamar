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

package org.jboss.jca.core.api.workmanager;

import org.jboss.jca.core.spi.workmanager.policy.Policy;
import org.jboss.jca.core.spi.workmanager.selector.Selector;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

/**
 * The JBoss specific distributed work manager interface
 */
public interface DistributedWorkManager extends javax.resource.spi.work.DistributableWorkManager, WorkManager
{
   /**
    * Get the policy
    * @return The value
    */
   public Policy getPolicy();

   /**
    * Set the policy
    * @param v The value
    */
   public void setPolicy(Policy v);

   /**
    * Get the selector
    * @return The value
    */
   public Selector getSelector();

   /**
    * Set the selector
    * @param v The value
    */
   public void setSelector(Selector v);

   /**
    * Get the transport
    * @return The value
    */
   public Transport getTransport();

   /**
    * Set the transport
    * @param v The value
    */
   public void setTransport(Transport v);
}
