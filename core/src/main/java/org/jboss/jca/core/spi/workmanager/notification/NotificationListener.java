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

package org.jboss.jca.core.spi.workmanager.notification;

/**
 * The notification listener gets notified about changes
 */
public interface NotificationListener
{
   /**
    * A distributed work manager joined
    * @param id The id
    */
   public void join(String id);

   /**
    * A distributed work manager left
    * @param id The id
    */
   public void leave(String id);

   /**
    * Update the short thread pool information for a distributed work manager
    * @param id The work manager id
    * @param free The number of free threads
    */
   public void updateShortRunningFree(String id, long free);

   /**
    * Update the long thread pool information for a distributed work manager
    * @param id The work manager id
    * @param free The number of free threads
    */
   public void updateLongRunningFree(String id, long free);
}
