/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.spi.transaction;

import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

/**
 * XAResource statistics
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface XAResourceStatistics extends StatisticsPlugin
{
   /**
    * Get the commit count
    * @return The value
    */
   public long getCommitCount();

   /**
    * Get the commit total time (milliseconds)
    * @return The value
    */
   public long getCommitTotalTime();

   /**
    * Get the commit average time (milliseconds)
    * @return The value
    */
   public long getCommitAverageTime();

   /**
    * Get the commit max time (milliseconds)
    * @return The value
    */
   public long getCommitMaxTime();

   /**
    * Delta commit
    * @param time The milliseconds
    */
   public void deltaCommit(long time);

   /**
    * Get the end count
    * @return The value
    */
   public long getEndCount();

   /**
    * Get the end total time (milliseconds)
    * @return The value
    */
   public long getEndTotalTime();

   /**
    * Get the end average time (milliseconds)
    * @return The value
    */
   public long getEndAverageTime();

   /**
    * Get the end max time (milliseconds)
    * @return The value
    */
   public long getEndMaxTime();

   /**
    * Delta end
    * @param time The milliseconds
    */
   public void deltaEnd(long time);

   /**
    * Get the forget count
    * @return The value
    */
   public long getForgetCount();

   /**
    * Get the forget total time (milliseconds)
    * @return The value
    */
   public long getForgetTotalTime();

   /**
    * Get the forget average time (milliseconds)
    * @return The value
    */
   public long getForgetAverageTime();

   /**
    * Get the forget max time (milliseconds)
    * @return The value
    */
   public long getForgetMaxTime();

   /**
    * Delta forget
    * @param time The milliseconds
    */
   public void deltaForget(long time);

   /**
    * Get the prepare count
    * @return The value
    */
   public long getPrepareCount();

   /**
    * Get the prepare total time (milliseconds)
    * @return The value
    */
   public long getPrepareTotalTime();

   /**
    * Get the prepare average time (milliseconds)
    * @return The value
    */
   public long getPrepareAverageTime();

   /**
    * Get the prepare max time (milliseconds)
    * @return The value
    */
   public long getPrepareMaxTime();

   /**
    * Delta prepare
    * @param time The milliseconds
    */
   public void deltaPrepare(long time);

   /**
    * Get the recover count
    * @return The value
    */
   public long getRecoverCount();

   /**
    * Get the recover total time (milliseconds)
    * @return The value
    */
   public long getRecoverTotalTime();

   /**
    * Get the recover average time (milliseconds)
    * @return The value
    */
   public long getRecoverAverageTime();

   /**
    * Get the recover max time (milliseconds)
    * @return The value
    */
   public long getRecoverMaxTime();

   /**
    * Delta recover
    * @param time The milliseconds
    */
   public void deltaRecover(long time);

   /**
    * Get the rollback count
    * @return The value
    */
   public long getRollbackCount();

   /**
    * Get the rollback total time (milliseconds)
    * @return The value
    */
   public long getRollbackTotalTime();

   /**
    * Get the rollback average time (milliseconds)
    * @return The value
    */
   public long getRollbackAverageTime();

   /**
    * Get the rollback max time (milliseconds)
    * @return The value
    */
   public long getRollbackMaxTime();

   /**
    * Delta rollback
    * @param time The milliseconds
    */
   public void deltaRollback(long time);

   /**
    * Get the start count
    * @return The value
    */
   public long getStartCount();

   /**
    * Get the start total time (milliseconds)
    * @return The value
    */
   public long getStartTotalTime();

   /**
    * Get the start average time (milliseconds)
    * @return The value
    */
   public long getStartAverageTime();

   /**
    * Get the start max time (milliseconds)
    * @return The value
    */
   public long getStartMaxTime();

   /**
    * Delta start
    * @param time The milliseconds
    */
   public void deltaStart(long time);
}
