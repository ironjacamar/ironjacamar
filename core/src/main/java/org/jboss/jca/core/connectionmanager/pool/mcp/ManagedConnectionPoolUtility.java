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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.PoolStatisticsImpl;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;

import java.util.Collection;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Managed connection pool utility class
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class ManagedConnectionPoolUtility
{
   private static String newLine = SecurityActions.getSystemProperty("line.separator");

   /**
    * Get the full details of a managed connection pool state
    * @param method The method identifier
    * @param poolName The pool name
    * @param inUse The in use count
    * @param max The max
    * @return The state
    */
   static String details(String method, String poolName, int inUse, int max)
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append(poolName).append(": ");
      sb.append(method).append(" ");
      sb.append("[");
      sb.append(Integer.toString(inUse));
      sb.append("/");
      sb.append(Integer.toString(max));
      sb.append("]");

      return sb.toString();
   }

   /**
    * Get the full details of a managed connection pool state
    * @param mcp The managed connection pool
    * @param method The method identifier
    * @param mcf The managed connection factory
    * @param cm The connection manager
    * @param pool The pool
    * @param pc The pool configuration
    * @param available The available connection listeners
    * @param inUse The in-use connection listeners
    * @param ps The statistics
    * @param subject The subject
    * @param cri The ConnectionRequestInfo
    * @return The state
    */
   static String fullDetails(ManagedConnectionPool mcp, String method, ManagedConnectionFactory mcf,
                             ConnectionManager cm, Pool pool, PoolConfiguration pc,
                             Collection<ConnectionListener> available, Collection<ConnectionListener> inUse,
                             PoolStatisticsImpl ps, Subject subject, ConnectionRequestInfo cri)
   {
      StringBuilder sb = new StringBuilder(1024);
      long now = System.currentTimeMillis();

      sb.append(method).append(newLine);
      sb.append("Method: ").append(method).append(newLine);
      sb.append("  Subject: ").append(subject == null ? "null" :
                                      Integer.toHexString(System.identityHashCode(subject))).append(newLine);
      sb.append("  CRI: ").append(cri == null ? "null" :
                                  Integer.toHexString(System.identityHashCode(cri))).append(newLine);
      sb.append("ManagedConnectionPool:").append(newLine);
      sb.append("  Class: ").append(mcp.getClass().getName()).append(newLine);
      sb.append("  Object: ").append(Integer.toHexString(System.identityHashCode(mcp))).append(newLine);
      sb.append("ManagedConnectionFactory:").append(newLine);
      sb.append("  Class: ").append(mcf.getClass().getName()).append(newLine);
      sb.append("  Object: ").append(Integer.toHexString(System.identityHashCode(mcf))).append(newLine);
      sb.append("ConnectionManager:").append(newLine);
      sb.append("  Class: ").append(cm.getClass().getName()).append(newLine);
      sb.append("  Object: ").append(Integer.toHexString(System.identityHashCode(cm))).append(newLine);
      sb.append("Pool:").append(newLine);
      sb.append("  Name: ").append(pool.getName()).append(newLine);
      sb.append("  Class: ").append(pool.getClass().getName()).append(newLine);
      sb.append("  Object: ").append(Integer.toHexString(System.identityHashCode(pool))).append(newLine);
      sb.append("  FIFO: ").append(pool.isFIFO()).append(newLine);
      sb.append("PoolConfiguration:").append(newLine);
      sb.append("  MinSize: ").append(pc.getMinSize()).append(newLine);
      sb.append("  InitialSize: ").append(pc.getInitialSize()).append(newLine);
      sb.append("  MaxSize: ").append(pc.getMaxSize()).append(newLine);
      sb.append("  BlockingTimeout: ").append(pc.getBlockingTimeout()).append(newLine);
      sb.append("  IdleTimeoutMinutes: ").append(pc.getIdleTimeoutMinutes()).append(newLine);
      sb.append("  ValidateOnMatch: ").append(pc.isValidateOnMatch()).append(newLine);
      sb.append("  BackgroundValidation: ").append(pc.isBackgroundValidation()).append(newLine);
      sb.append("  BackgroundValidationMillis: ").append(pc.getBackgroundValidationMillis()).append(newLine);
      sb.append("  StrictMin: ").append(pc.isStrictMin()).append(newLine);
      sb.append("  UseFastFail: ").append(pc.isUseFastFail()).append(newLine);
      if (pool.getCapacity() != null)
      {
         if (pool.getCapacity().getIncrementer() != null)
            sb.append("  Incrementer: ").append(pool.getCapacity().getIncrementer()).append(newLine);

         if (pool.getCapacity().getDecrementer() != null)
            sb.append("  Decrementer: ").append(pool.getCapacity().getDecrementer()).append(newLine);
      }
      
      int availableSize = (available != null ? available.size() : 0);
      sb.append("Available (").append(availableSize).append("):").append(newLine);
      if (available != null)
      {
         for (ConnectionListener cl : available)
         {
            sb.append("  ").append(Integer.toHexString(System.identityHashCode(cl)));
            sb.append(" (").append(cl.getState()).append(")");
            sb.append(" (Returned: ").append(cl.getLastReturnedTime()).append(")");
            sb.append(" (Validated: ").append(cl.getLastValidatedTime()).append(")");
            sb.append(" (Pool: ").append(now - cl.getLastReturnedTime()).append(")").append(newLine);
         }
      }

      int inUseSize = (inUse != null ? inUse.size() : 0);
      sb.append("InUse (").append(inUseSize).append("):").append(newLine);
      if (inUse != null)
      {
         for (ConnectionListener cl : inUse)
         {
            sb.append("  ").append(Integer.toHexString(System.identityHashCode(cl)));
            sb.append(" (").append(cl.getState()).append(")");
            sb.append(" (CheckedOut: ").append(cl.getLastCheckedOutTime()).append(")");
            sb.append(" (Validated: ").append(cl.getLastValidatedTime()).append(")");
            sb.append(" (Usage: ").append(now - cl.getLastCheckedOutTime()).append(")").append(newLine);
         }
      }

      sb.append("Statistics:").append(newLine);
      sb.append("  ActiveCount: ").append(ps.getActiveCount()).append(newLine);
      sb.append("  AvailableCount: ").append(ps.getAvailableCount()).append(newLine);
      sb.append("  AverageBlockingTime: ").append(ps.getAverageBlockingTime()).append(newLine);
      sb.append("  AverageCreationTime: ").append(ps.getAverageCreationTime()).append(newLine);
      sb.append("  AverageGetTime: ").append(ps.getAverageGetTime()).append(newLine);
      sb.append("  AveragePoolTime: ").append(ps.getAveragePoolTime()).append(newLine);
      sb.append("  AverageUsageTime: ").append(ps.getAverageUsageTime()).append(newLine);
      sb.append("  BlockingFailureCount: ").append(ps.getBlockingFailureCount()).append(newLine);
      sb.append("  CreatedCount: ").append(ps.getCreatedCount()).append(newLine);
      sb.append("  DestroyedCount: ").append(ps.getDestroyedCount()).append(newLine);
      sb.append("  IdleCount: ").append(ps.getIdleCount()).append(newLine);
      sb.append("  InUseCount: ").append(ps.getInUseCount()).append(newLine);
      sb.append("  MaxCreationTime: ").append(ps.getMaxCreationTime()).append(newLine);
      sb.append("  MaxGetTime: ").append(ps.getMaxGetTime()).append(newLine);
      sb.append("  MaxPoolTime: ").append(ps.getMaxPoolTime()).append(newLine);
      sb.append("  MaxUsageTime: ").append(ps.getMaxUsageTime()).append(newLine);
      sb.append("  MaxUsedCount: ").append(ps.getMaxUsedCount()).append(newLine);
      sb.append("  MaxWaitTime: ").append(ps.getMaxWaitTime()).append(newLine);
      sb.append("  TimedOut: ").append(ps.getTimedOut()).append(newLine);
      sb.append("  TotalBlockingTime: ").append(ps.getTotalBlockingTime()).append(newLine);
      sb.append("  TotalCreationTime: ").append(ps.getTotalCreationTime()).append(newLine);
      sb.append("  TotalGetTime: ").append(ps.getTotalGetTime()).append(newLine);
      sb.append("  TotalPoolTime: ").append(ps.getTotalPoolTime()).append(newLine);
      sb.append("  TotalUsageTime: ").append(ps.getTotalUsageTime()).append(newLine);
      sb.append("  WaitCount: ").append(ps.getWaitCount()).append(newLine);

      sb.append("XAResource:").append(newLine);
      sb.append("  CommitCount: ").append(ps.getCommitCount()).append(newLine);
      sb.append("  CommitTotalTime: ").append(ps.getCommitTotalTime()).append(newLine);
      sb.append("  CommitAverageTime: ").append(ps.getCommitAverageTime()).append(newLine);
      sb.append("  CommitMaxTime: ").append(ps.getCommitMaxTime()).append(newLine);
      sb.append("  EndCount: ").append(ps.getEndCount()).append(newLine);
      sb.append("  EndTotalTime: ").append(ps.getEndTotalTime()).append(newLine);
      sb.append("  EndAverageTime: ").append(ps.getEndAverageTime()).append(newLine);
      sb.append("  EndMaxTime: ").append(ps.getEndMaxTime()).append(newLine);
      sb.append("  ForgetCount: ").append(ps.getForgetCount()).append(newLine);
      sb.append("  ForgetTotalTime: ").append(ps.getForgetTotalTime()).append(newLine);
      sb.append("  ForgetAverageTime: ").append(ps.getForgetAverageTime()).append(newLine);
      sb.append("  ForgetMaxTime: ").append(ps.getForgetMaxTime()).append(newLine);
      sb.append("  PrepareCount: ").append(ps.getPrepareCount()).append(newLine);
      sb.append("  PrepareTotalTime: ").append(ps.getPrepareTotalTime()).append(newLine);
      sb.append("  PrepareAverageTime: ").append(ps.getPrepareAverageTime()).append(newLine);
      sb.append("  PrepareMaxTime: ").append(ps.getPrepareMaxTime()).append(newLine);
      sb.append("  RecoverCount: ").append(ps.getRecoverCount()).append(newLine);
      sb.append("  RecoverTotalTime: ").append(ps.getRecoverTotalTime()).append(newLine);
      sb.append("  RecoverAverageTime: ").append(ps.getRecoverAverageTime()).append(newLine);
      sb.append("  RecoverMaxTime: ").append(ps.getRecoverMaxTime()).append(newLine);
      sb.append("  RollbackCount: ").append(ps.getRollbackCount()).append(newLine);
      sb.append("  RollbackTotalTime: ").append(ps.getRollbackTotalTime()).append(newLine);
      sb.append("  RollbackAverageTime: ").append(ps.getRollbackAverageTime()).append(newLine);
      sb.append("  RollbackMaxTime: ").append(ps.getRollbackMaxTime()).append(newLine);
      sb.append("  StartCount: ").append(ps.getStartCount()).append(newLine);
      sb.append("  StartTotalTime: ").append(ps.getStartTotalTime()).append(newLine);
      sb.append("  StartAverageTime: ").append(ps.getStartAverageTime()).append(newLine);
      sb.append("  StartMaxTime: ").append(ps.getStartMaxTime());

      return sb.toString();
   }
}
