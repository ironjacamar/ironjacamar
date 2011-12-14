/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;

import java.util.Collection;

import javax.resource.spi.ManagedConnectionFactory;

/**
 * Managed connection pool utility class
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class ManagedConnectionPoolUtility
{
   private static String newLine = SecurityActions.getSystemProperty("line.separator");

   /**
    * Get the full details of a managed connection pool state
    * @param method The method identifier
    * @param poolName The pool name
    * @param mcps The statistics
    * @return The state
    */
   static String details(String method, String poolName, ManagedConnectionPoolStatistics mcps)
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append(poolName).append(": ");
      sb.append(method).append(" ");
      sb.append("[");
      sb.append(mcps.getActiveCount());
      sb.append("/");
      sb.append(mcps.getAvailableCount());
      sb.append("]");

      return sb.toString();
   }

   /**
    * Get the full details of a managed connection pool state
    * @param identifier The managed connection pool identifier
    * @param method The method identifier
    * @param mcf The managed connection factory
    * @param clf The connection listener factory
    * @param pool The pool
    * @param pc The pool configuration
    * @param available The available connection listeners
    * @param inUse The in-use connection listeners
    * @param mcps The statistics
    * @return The state
    */
   static String fullDetails(int identifier, String method, ManagedConnectionFactory mcf,
                             ConnectionListenerFactory clf, Pool pool, PoolConfiguration pc,
                             Collection<ConnectionListener> available, Collection<ConnectionListener> inUse,
                             ManagedConnectionPoolStatistics mcps)
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("ManagedConnectionPool: ").append(Integer.toHexString(identifier)).append(newLine);
      sb.append("Method: ").append(method).append(newLine);
      sb.append("ManagedConnectionFactory:").append(newLine);
      sb.append("  Class: ").append(mcf.getClass().getName()).append(newLine);
      sb.append("  Object: ").append(Integer.toHexString(System.identityHashCode(mcf))).append(newLine);
      sb.append("ConnectionListenerFactory:").append(newLine);
      sb.append("  Class: ").append(clf.getClass().getName()).append(newLine);
      sb.append("  Object: ").append(Integer.toHexString(System.identityHashCode(clf))).append(newLine);
      sb.append("Pool:").append(newLine);
      sb.append("  Name: ").append(pool.getName()).append(newLine);
      sb.append("PoolConfiguration:").append(newLine);
      sb.append("  MinSize: ").append(pc.getMinSize()).append(newLine);
      sb.append("  MaxSize: ").append(pc.getMaxSize()).append(newLine);
      sb.append("  BlockingTimeout: ").append(pc.getBlockingTimeout()).append(newLine);
      sb.append("  IdleTimeoutMinutes: ").append(pc.getIdleTimeoutMinutes()).append(newLine);
      sb.append("  BackgroundValidation: ").append(pc.isBackgroundValidation()).append(newLine);
      sb.append("  BackgroundValidationMillis: ").append(pc.getBackgroundValidationMillis()).append(newLine);
      sb.append("  StrictMin: ").append(pc.isStrictMin()).append(newLine);
      sb.append("  UseFastFail: ").append(pc.isUseFastFail()).append(newLine);

      sb.append("Available:").append(newLine);
      if (available != null)
      {
         for (ConnectionListener cl : available)
         {
            sb.append("  ").append(Integer.toHexString(System.identityHashCode(cl)));
            sb.append(" (").append(cl.getState()).append(")").append(newLine);
         }
      }

      sb.append("InUse:").append(newLine);
      if (inUse != null)
      {
         for (ConnectionListener cl : inUse)
         {
            sb.append("  ").append(Integer.toHexString(System.identityHashCode(cl)));
            sb.append(" (").append(cl.getState()).append(")").append(newLine);
         }
      }

      sb.append("Statistics:").append(newLine);
      sb.append("  ActiveCount: ").append(mcps.getActiveCount()).append(newLine);
      sb.append("  AvailableCount: ").append(mcps.getAvailableCount()).append(newLine);
      sb.append("  AverageBlockingTime: ").append(mcps.getAverageBlockingTime()).append(newLine);
      sb.append("  AverageCreationTime: ").append(mcps.getAverageCreationTime()).append(newLine);
      sb.append("  CreatedCount: ").append(mcps.getCreatedCount()).append(newLine);
      sb.append("  DestroyedCount: ").append(mcps.getDestroyedCount()).append(newLine);
      sb.append("  MaxCreationTime: ").append(mcps.getMaxCreationTime()).append(newLine);
      sb.append("  MaxUsedCount: ").append(mcps.getMaxUsedCount()).append(newLine);
      sb.append("  MaxWaitCount: ").append(mcps.getMaxWaitCount()).append(newLine);
      sb.append("  MaxWaitTime: ").append(mcps.getMaxWaitTime()).append(newLine);
      sb.append("  TimedOut: ").append(mcps.getTimedOut()).append(newLine);
      sb.append("  TotalBlockingTime: ").append(mcps.getTotalBlockingTime()).append(newLine);
      sb.append("  TotalCreationTime: ").append(mcps.getTotalCreationTime());

      return sb.toString();
   }
}
