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
package org.jboss.jca.common.metadataimpl.ra.common;

import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.OutboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.TransactionSupportEnum;
import org.jboss.jca.common.metadataimpl.MergeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class OutboundResourceAdapterImpl implements OutboundResourceAdapter
{
   /**
    */
   private static final long serialVersionUID = 421345307326415666L;

   private final ArrayList<ConnectionDefinition> connectionDefinition;

   private final TransactionSupportEnum transactionSupport;

   private final ArrayList<AuthenticationMechanism> authenticationMechanism;

   private final boolean reauthenticationSupport;

   private final String id;

   /**
    * @param connectionDefinition list of connection definitions
    * @param transactionSupport transaction supported form this RA. Valid ones are defined by an enum
    * @param authenticationMechanism list of authentication mechanism supported
    * @param reauthenticationSupport not mandatary boolean value
    * @param id XML ID
    */
   public OutboundResourceAdapterImpl(ArrayList<ConnectionDefinition> connectionDefinition,
         TransactionSupportEnum transactionSupport, ArrayList<AuthenticationMechanism> authenticationMechanism,
         boolean reauthenticationSupport, String id)
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
   @Override
   public List<ConnectionDefinition> getConnectionDefinitions()
   {
      return connectionDefinition == null ? null : Collections.unmodifiableList(connectionDefinition);
   }

   /**
    * @return transactionSupport
    */
   @Override
   public TransactionSupportEnum getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * @return authenticationMechanism
    */
   @Override
   public List<AuthenticationMechanism> getAuthenticationMechanisms()
   {
      return authenticationMechanism == null ? null : Collections.unmodifiableList(authenticationMechanism);
   }

   /**
    * @return reauthenticationSupport
    */
   @Override
   public boolean getReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

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
      if (!(obj instanceof OutboundResourceAdapterImpl))
      {
         return false;
      }
      OutboundResourceAdapterImpl other = (OutboundResourceAdapterImpl) obj;
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
      if (reauthenticationSupport != other.reauthenticationSupport)
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

   /**
   *
   * A validate method. Don't extending for the moment ValidatableMetadata
   *
   * @return true if Ra is valid, flase in the other cases
   */
   @Override
   public boolean validationAsBoolean()
   {
      if (this.getConnectionDefinitions() == null || this.getConnectionDefinitions().size() == 0)
         return false;
      ConnectionDefinition cdm = this.getConnectionDefinitions().get(0);
      if (cdm.getManagedconnectionfactoryClass() == null || cdm.getConnectionfactoryInterface() == null
            || cdm.getConnectionfactoryImplClass() == null || cdm.getConnectionInterface() == null
            || cdm.getConnectionImplClass() == null)
         return false;

      return true;
   }

   @Override
   public OutboundResourceAdapter merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof OutboundResourceAdapterImpl)
      {
         OutboundResourceAdapterImpl input = (OutboundResourceAdapterImpl) jmd;

         String newId = this.id == null ? input.id : this.id;

         ArrayList<ConnectionDefinition> newConnectionDefinition = new ArrayList<ConnectionDefinition>(
               this.connectionDefinition.size());

         newConnectionDefinition.addAll(this.connectionDefinition);
         for (ConnectionDefinition rcd : input.connectionDefinition)
         {
            boolean isNew = true;
            for (ConnectionDefinition lcd : this.connectionDefinition)
            {
               if (rcd.getManagedconnectionfactoryClass().equals(lcd.getManagedconnectionfactoryClass()))
               {
                  newConnectionDefinition.remove(lcd);
                  newConnectionDefinition.add(lcd.merge(rcd));
                  isNew = false;
               }
            }
            if (isNew) newConnectionDefinition.add(rcd);
         }

         TransactionSupportEnum newTransactionSupport = this.transactionSupport;

         boolean newReauthenticationSupport = this.reauthenticationSupport || input.reauthenticationSupport;
         ArrayList<AuthenticationMechanism> newAuthenticationMechanism = MergeUtil.mergeArrayList(
               this.authenticationMechanism,
               input.authenticationMechanism);
         return new OutboundResourceAdapterImpl(newConnectionDefinition, newTransactionSupport,
               newAuthenticationMechanism, newReauthenticationSupport, newId);
      }
      else
      {
         return this;
      }
   }

}
