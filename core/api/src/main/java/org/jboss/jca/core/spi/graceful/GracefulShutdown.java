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

package org.jboss.jca.core.spi.graceful;

/**
 * The SPI for graceful shutdown
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface GracefulShutdown
{
   /**
    * Cancel shutdown
    * @return True if the shutdown was canceled; false otherwise
    */
   public boolean cancelShutdown();

   /**
    * Signal the component to prepare for shutdown
    */
   public void prepareShutdown();

   /**
    * Signal the component to prepare for shutdown
    * @param cb The callback handle
    */
   public void prepareShutdown(GracefulCallback cb);

   /**
    * Signal the component to prepare for shutdown
    * @param shutdown The number of seconds after which shutdown is forced
    */
   public void prepareShutdown(int shutdown);

   /**
    * Signal the component to prepare for shutdown
    * @param shutdown The number of seconds after which shutdown is forced
    * @param cb The callback handle
    */
   public void prepareShutdown(int shutdown, GracefulCallback cb);

   /**
    * Shutdown the component
    */
   public void shutdown();

   /**
    * Is the component shutdown
    * @return True if shutdown; false if active
    */
   public boolean isShutdown();

   /**
    * Get the delay until shutdown occurs
    * @return The number of seconds, <code>Integer.MAX_VALUE</code> for active,
    *         or <code>Integer.MIN_VALUE</code> for inactive
    */
   public int getDelay();
}
