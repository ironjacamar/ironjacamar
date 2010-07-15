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
package org.jboss.jca.common.metadata.specs;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class OutboundResourceAdapter implements IdDecoratedMetadata
{
   /**
    */
   private static final long serialVersionUID = 421345307326415666L;

   private final List<ConnectionDefinition> connectionDefinition;

   private final TransactionSupportEnum transactionSupport;

   private final List<AuthenticationMechanism> authenticationMechanism;

   private final Boolean reauthenticationSupport;

   private final String id;

   /**
    * @param connectionDefinition list of connection definitions
    * @param transactionSupport transaction supported form this RA. Valid ones are defined by an enum
    * @param authenticationMechanism list of authentication mechanism supported
    * @param reauthenticationSupport not mandatary boolean value
    * @param id XML ID
    */
   public OutboundResourceAdapter(List<ConnectionDefinition> connectionDefinition,
         TransactionSupportEnum transactionSupport, List<AuthenticationMechanism> authenticationMechanism,
         Boolean reauthenticationSupport, String id)
   {
      super();
      this.connectionDefinition = connectionDefinition;
      this.transactionSupport = transactionSupport;
      this.authenticationMechanism = authenticationMechanism;
      this.reauthenticationSupport = reauthenticationSupport;
      this.id = id;
   }

   /**
    * @return connectionDefinition
    */
   public List<ConnectionDefinition> getConnectionDefinition()
   {
      return Collections.unmodifiableList(connectionDefinition);
   }

   /**
    * @return transactionSupport
    */
   public TransactionSupportEnum getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * @return authenticationMechanism
    */
   public List<AuthenticationMechanism> getAuthenticationMechanism()
   {
      return Collections.unmodifiableList(authenticationMechanism);
   }

   /**
    * @return reauthenticationSupport
    */
   public Boolean getReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   /**
    * {@inheritDoc}
    *
    * @see IdDecoratedMetadata#getId()
    */
   @Override
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((authenticationMechanism == null) ? 0 : authenticationMechanism.hashCode());
      result = prime * result + ((connectionDefinition == null) ? 0 : connectionDefinition.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((reauthenticationSupport == null) ? 0 : reauthenticationSupport.hashCode());
      result = prime * result + ((transactionSupport == null) ? 0 : transactionSupport.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof OutboundResourceAdapter))
      {
         return false;
      }
      OutboundResourceAdapter other = (OutboundResourceAdapter) obj;
      if (authenticationMechanism == null)
      {
         if (other.authenticationMechanism != null)
         {
            return false;
         }
      }
      else if (!authenticationMechanism.equals(other.authenticationMechanism))
      {
         return false;
      }
      if (connectionDefinition == null)
      {
         if (other.connectionDefinition != null)
         {
            return false;
         }
      }
      else if (!connectionDefinition.equals(other.connectionDefinition))
      {
         return false;
      }
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if (!id.equals(other.id))
      {
         return false;
      }
      if (reauthenticationSupport == null)
      {
         if (other.reauthenticationSupport != null)
         {
            return false;
         }
      }
      else if (!reauthenticationSupport.equals(other.reauthenticationSupport))
      {
         return false;
      }
      if (transactionSupport != other.transactionSupport)
      {
         return false;
      }
      return true;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "OutboundResourceAdapter [connectionDefinition=" + connectionDefinition + ", transactionSupport="
            + transactionSupport + ", authenticationMechanism=" + authenticationMechanism
            + ", reauthenticationSupport=" + reauthenticationSupport + ", id=" + id + "]";
   }
}
