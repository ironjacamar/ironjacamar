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
package org.jboss.jca.common.metadata.ra.common;

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.MergeUtil;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.OutboundResourceAdapter;

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

   private ArrayList<ConnectionDefinition> connectionDefinition;

   private TransactionSupportEnum transactionSupport;

   private final ArrayList<AuthenticationMechanism> authenticationMechanism;

   private final Boolean reauthenticationSupport;

   private final String id;

   /**
    * @param connectionDefinition list of connection definitions
    * @param transactionSupport transaction supported form this RA. Valid ones are defined by an enum
    * @param authenticationMechanism list of authentication mechanism supported
    * @param reauthenticationSupport not mandatary boolean value
    * @param id XML ID
    */
   public OutboundResourceAdapterImpl(List<ConnectionDefinition> connectionDefinition,
      TransactionSupportEnum transactionSupport, List<AuthenticationMechanism> authenticationMechanism,
      Boolean reauthenticationSupport, String id)
   {
      super();
      if (connectionDefinition != null)
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(connectionDefinition.size());
         this.connectionDefinition.addAll(connectionDefinition);
      }
      else
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(0);
      }
      this.transactionSupport = transactionSupport;
      if (authenticationMechanism != null)
      {
         this.authenticationMechanism = new ArrayList<AuthenticationMechanism>(authenticationMechanism.size());
         this.authenticationMechanism.addAll(authenticationMechanism);
      }
      else
      {
         this.authenticationMechanism = new ArrayList<AuthenticationMechanism>(0);
      }
      this.reauthenticationSupport = reauthenticationSupport;
      this.id = id;
   }

   /**
    * @return connectionDefinition
    */
   @Override
   public synchronized List<ConnectionDefinition> getConnectionDefinitions()
   {
      return connectionDefinition == null ? null : Collections.unmodifiableList(connectionDefinition);
   }

   /**
   *
   * force connectionDefinition with new content.
   * This method is thread safe
   *
   * @param newContent the list of new properties
   */
   public synchronized void forceConnectionDefinitionsContent(List<ConnectionDefinition> newContent)
   {
      if (newContent != null)
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(newContent.size());
         this.connectionDefinition.addAll(newContent);
      }
      else
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(0);
      }
   }

   /**
    * @return transactionSupport
    */
   @Override
   public synchronized TransactionSupportEnum getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
   *
   * force transactionSupport to the new value
   * This method is thread safe
   *
   * @param newTransactionSupport the new value
   */
   public synchronized void forceNewTrasactionSupport(TransactionSupportEnum newTransactionSupport)
   {
      this.transactionSupport = newTransactionSupport;
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
      result = prime * result + ((reauthenticationSupport == null) ? 0 : reauthenticationSupport.hashCode());
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
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<").append("outbound-resourceadapter");
      if (id != null)
         sb.append(" ").append(OutboundResourceAdapter.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      if (connectionDefinition != null)
      {
         for (ConnectionDefinition cd : connectionDefinition)
         {
            sb.append(cd);
         }
      }

      if (transactionSupport != null)
      {
         sb.append("<").append(OutboundResourceAdapter.Tag.TRANSACTION_SUPPORT).append(">");
         sb.append(transactionSupport);
         sb.append("</").append(OutboundResourceAdapter.Tag.TRANSACTION_SUPPORT).append(">");
      }

      if (authenticationMechanism != null)
      {
         for (AuthenticationMechanism am : authenticationMechanism)
         {
            sb.append(am);
         }
      }

      if (reauthenticationSupport != null)
      {
         sb.append("<").append(OutboundResourceAdapter.Tag.REAUTHENTICATION_SUPPORT).append(">");
         sb.append(reauthenticationSupport);
         sb.append("</").append(OutboundResourceAdapter.Tag.REAUTHENTICATION_SUPPORT).append(">");
      }

      sb.append("</outbound-resourceadapter>");

      return sb.toString();
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
      if (cdm.getManagedConnectionFactoryClass() == null || cdm.getConnectionFactoryInterface() == null ||
          cdm.getConnectionFactoryImplClass() == null || cdm.getConnectionInterface() == null ||
          cdm.getConnectionImplClass() == null)
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

         List<ConnectionDefinition> newConnDef = new ArrayList<ConnectionDefinition>(
                                                                                     this.connectionDefinition
                                                                                        .size());

         newConnDef.addAll(this.connectionDefinition);
         for (ConnectionDefinition rcd : input.connectionDefinition)
         {
            boolean isNew = true;
            for (ConnectionDefinition lcd : this.connectionDefinition)
            {
               if (lcd.getManagedConnectionFactoryClass() == null ||
                   rcd.getManagedConnectionFactoryClass().equals(lcd.getManagedConnectionFactoryClass()))
               {
                  newConnDef.remove(lcd);
                  newConnDef.add(lcd.merge(rcd));
                  isNew = false;
               }
            }
            if (isNew)
               newConnDef.add(rcd);
         }

         TransactionSupportEnum newTransactionSupport = this.transactionSupport == null
            ? input.transactionSupport
            : this.transactionSupport;

         boolean newReauthenticationSupport = this.reauthenticationSupport || input.reauthenticationSupport;
         List<AuthenticationMechanism> newAuthenticationMechanism = MergeUtil.mergeList(
            this.authenticationMechanism, input.authenticationMechanism);
         return new OutboundResourceAdapterImpl(newConnDef, newTransactionSupport, newAuthenticationMechanism,
                                                newReauthenticationSupport, newId);
      }
      else
      {
         return this;
      }
   }

   @Override
   public CopyableMetaData copy()
   {
      return new OutboundResourceAdapterImpl(CopyUtil.cloneList(connectionDefinition), transactionSupport,
                                             CopyUtil.cloneList(authenticationMechanism), reauthenticationSupport,
                                             CopyUtil.cloneString(id));
   }
}
