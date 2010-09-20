/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.ds.Statement;

/**
 *
 * A StatementImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class StatementImpl implements Statement
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 3361665706947342366L;

   private final Boolean sharePreparedStatements;

   private final Long preparedStatementsCacheSize;

   private final TrackStatementsEnum trackStatements;

   /**
    * Create a new StatementImpl.
    *
    * @param sharePreparedStatements sharePreparedStatements
    * @param preparedStatementsCacheSize preparedStatementsCacheSize
    * @param trackStatements trackStatements
    */
   public StatementImpl(Boolean sharePreparedStatements, Long preparedStatementsCacheSize,
         TrackStatementsEnum trackStatements)
   {
      super();
      this.sharePreparedStatements = sharePreparedStatements;
      this.preparedStatementsCacheSize = preparedStatementsCacheSize;
      this.trackStatements = trackStatements;
   }

   /**
    * Get the sharePreparedStatements.
    *
    * @return the sharePreparedStatements.
    */
   @Override
   public final Boolean isSharePreparedStatements()
   {
      return sharePreparedStatements;
   }

   /**
    * Get the preparedStatementsCacheSize.
    *
    * @return the preparedStatementsCacheSize.
    */
   @Override
   public final Long getPreparedStatementsCacheSize()
   {
      return preparedStatementsCacheSize;
   }

   /**
    * Get the trackStatements.
    *
    * @return the trackStatements.
    */
   @Override
   public final TrackStatementsEnum getTrackStatements()
   {
      return trackStatements;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((preparedStatementsCacheSize == null) ? 0 : preparedStatementsCacheSize.hashCode());
      result = prime * result + (sharePreparedStatements ? 1231 : 1237);
      result = prime * result + ((trackStatements == null) ? 0 : trackStatements.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof StatementImpl))
         return false;
      StatementImpl other = (StatementImpl) obj;
      if (preparedStatementsCacheSize == null)
      {
         if (other.preparedStatementsCacheSize != null)
            return false;
      }
      else if (!preparedStatementsCacheSize.equals(other.preparedStatementsCacheSize))
         return false;
      if (sharePreparedStatements != other.sharePreparedStatements)
         return false;
      if (trackStatements != other.trackStatements)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "StatementImpl [sharePreparedStatements=" + sharePreparedStatements +
            ", preparedStatementsCacheSize=" + preparedStatementsCacheSize +
            ", trackStatements=" + trackStatements + "]";
   }
}

