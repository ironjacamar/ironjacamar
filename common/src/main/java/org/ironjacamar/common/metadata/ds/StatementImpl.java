/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.metadata.ds;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.ds.Statement;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.Map;

import org.jboss.logging.Messages;

/**
 *
 * A StatementImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class StatementImpl extends AbstractMetadata implements Statement
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3361665706947342366L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private final Boolean sharePreparedStatements;

   private final Long preparedStatementsCacheSize;

   private final TrackStatementsEnum trackStatements;

   /**
    * Create a new StatementImpl.
    *
    * @param sharePreparedStatements sharePreparedStatements
    * @param preparedStatementsCacheSize preparedStatementsCacheSize
    * @param trackStatements trackStatements
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public StatementImpl(Boolean sharePreparedStatements, Long preparedStatementsCacheSize,
                        TrackStatementsEnum trackStatements,
                        Map<String, String> expressions) throws ValidateException
   {
      super(expressions);
      this.sharePreparedStatements = sharePreparedStatements;
      this.preparedStatementsCacheSize = preparedStatementsCacheSize;
      this.trackStatements = trackStatements;
      this.validate();
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
      result = prime * result + ((sharePreparedStatements == null) ? 0 : sharePreparedStatements.hashCode());
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
      if (sharePreparedStatements == null)
      {
         if (other.sharePreparedStatements != null)
            return false;
      }
      else if (!sharePreparedStatements.equals(other.sharePreparedStatements))
         return false;
      if (trackStatements != other.trackStatements)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<statement>");

      if (trackStatements != null)
      {
         sb.append("<").append(XML.ELEMENT_TRACK_STATEMENTS).append(">");
         sb.append(trackStatements);
         sb.append("</").append(XML.ELEMENT_TRACK_STATEMENTS).append(">");
      }

      if (preparedStatementsCacheSize != null)
      {
         sb.append("<").append(XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE).append(">");
         sb.append(preparedStatementsCacheSize);
         sb.append("</").append(XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE).append(">");
      }

      if (sharePreparedStatements != null && Boolean.TRUE.equals(sharePreparedStatements))
      {
         sb.append("<").append(XML.ELEMENT_SHARE_PREPARED_STATEMENTS).append("/>");
      }

      sb.append("</statement>");

      return sb.toString();
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.preparedStatementsCacheSize != null && this.preparedStatementsCacheSize < 0)
         throw new ValidateException(bundle.invalidNegative(XML.ELEMENT_PREPARED_STATEMENT_CACHE_SIZE));
   }
}

