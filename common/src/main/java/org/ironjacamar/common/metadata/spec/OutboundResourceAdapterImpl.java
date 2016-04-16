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
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.api.metadata.CopyUtil;
import org.ironjacamar.common.api.metadata.MergeUtil;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.spec.AuthenticationMechanism;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.MergeableMetadata;
import org.ironjacamar.common.api.metadata.spec.OutboundResourceAdapter;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An OutboundResourceAdapter implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class OutboundResourceAdapterImpl extends AbstractMetadata implements OutboundResourceAdapter
{
   private static final long serialVersionUID = 1L;

   private List<ConnectionDefinition> connectionDefinition;

   private TransactionSupportEnum transactionSupport;

   private String transactionSupportId;

   private List<AuthenticationMechanism> authenticationMechanism;

   private boolean reauthenticationSupport;

   private String reauthenticationSupportId;

   private String id;

   /**
    * Constructor
    * @param connectionDefinition list of connection definitions
    * @param transactionSupport transaction supported form this RA. Valid ones are defined by an enum
    * @param authenticationMechanism list of authentication mechanism supported
    * @param reauthenticationSupport not mandatary boolean value
    * @param transactionSupportId transaction support element ID
    * @param reauthenticationSupportId reauthentication support element ID
    * @param id XML ID
    */
   public OutboundResourceAdapterImpl(List<ConnectionDefinition> connectionDefinition,
                                      TransactionSupportEnum transactionSupport,
                                      List<AuthenticationMechanism> authenticationMechanism,
                                      boolean reauthenticationSupport, String id, String transactionSupportId,
                                      String reauthenticationSupportId)
   {
      super(null);
      if (connectionDefinition != null)
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(connectionDefinition);
      }
      else
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(0);
      }
      this.transactionSupport = transactionSupport;
      if (authenticationMechanism != null)
      {
         this.authenticationMechanism = new ArrayList<AuthenticationMechanism>(authenticationMechanism);
      }
      else
      {
         this.authenticationMechanism = new ArrayList<AuthenticationMechanism>(0);
      }
      this.reauthenticationSupport = reauthenticationSupport;
      this.id = id;
      this.reauthenticationSupportId = reauthenticationSupportId;
      this.transactionSupportId = transactionSupportId;
   }

   /**
    * {@inheritDoc}
    */
   public List<ConnectionDefinition> getConnectionDefinitions()
   {
      return Collections.unmodifiableList(connectionDefinition);
   }

   /**
    * Force connectionDefinition with new content.
    * This method is thread safe
    *
    * @param newContent the list of new properties
    */
   public synchronized void forceConnectionDefinitions(List<ConnectionDefinition> newContent)
   {
      if (newContent != null)
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(newContent);
      }
      else
      {
         this.connectionDefinition = new ArrayList<ConnectionDefinition>(0);
      }
   }

   /**
    * {@inheritDoc}
    */
   public TransactionSupportEnum getTransactionSupport()
   {
      if (transactionSupport == null)
         return TransactionSupportEnum.NoTransaction;

      return transactionSupport;
   }

   /**
    * Force transactionSupport to the new value
    * This method is thread safe
    *
    * @param newTransactionSupport the new value
    */
   public synchronized void forceTransactionSupport(TransactionSupportEnum newTransactionSupport)
   {
      this.transactionSupport = newTransactionSupport;
   }

   /**
    * {@inheritDoc}
    */
   public List<AuthenticationMechanism> getAuthenticationMechanisms()
   {
      return Collections.unmodifiableList(authenticationMechanism);
   }

   /**
    * {@inheritDoc}
    */
   public boolean getReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public String getTransactionSupportId()
   {
      return transactionSupportId;
   }

   /**
    * {@inheritDoc}
    */
   public String getReauthenticationSupportId()
   {
      return reauthenticationSupportId;
   }

   /**
    * A validate method. Don't extending for the moment ValidatableMetadata
    *
    * @return true if Ra is valid, flase in the other cases
    */
   public boolean validationAsBoolean()
   {
      if (this.getConnectionDefinitions() == null || this.getConnectionDefinitions().isEmpty())
         return false;

      ConnectionDefinition cdm = this.getConnectionDefinitions().get(0);
      if (cdm.getManagedConnectionFactoryClass() == null || cdm.getConnectionFactoryInterface() == null ||
          cdm.getConnectionFactoryImplClass() == null || cdm.getConnectionInterface() == null ||
          cdm.getConnectionImplClass() == null)
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public OutboundResourceAdapter merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof OutboundResourceAdapterImpl)
      {
         OutboundResourceAdapterImpl input = (OutboundResourceAdapterImpl) jmd;

         String newId = this.id == null ? input.id : this.id;
         String newTransactionSupportId = this.transactionSupportId == null ?
            input.transactionSupportId : this.transactionSupportId;
         String newReauthenticationSupportId = this.reauthenticationSupportId == null ?
            input.reauthenticationSupportId : this.reauthenticationSupportId;

         List<ConnectionDefinition> newConnDef = new ArrayList<ConnectionDefinition>(this.connectionDefinition.size());

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
         List<AuthenticationMechanism> newAuthenticationMechanism = MergeUtil.mergeList(this.authenticationMechanism,
            input.authenticationMechanism);
         return new OutboundResourceAdapterImpl(newConnDef, newTransactionSupport, newAuthenticationMechanism,
                                                newReauthenticationSupport, newId, newTransactionSupportId,
                                                newReauthenticationSupportId);
      }
      else
      {
         return this;
      }
   }

   /**
    * {@inheritDoc}
    */
   public OutboundResourceAdapter copy()
   {
      return new OutboundResourceAdapterImpl(CopyUtil.cloneList(connectionDefinition), transactionSupport,
                                             CopyUtil.cloneList(authenticationMechanism), reauthenticationSupport,
                                             CopyUtil.cloneString(id), CopyUtil.cloneString(transactionSupportId),
                                             CopyUtil.cloneString(reauthenticationSupportId));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((authenticationMechanism == null) ? 0 : authenticationMechanism.hashCode());
      result = prime * result + ((connectionDefinition == null) ? 0 : connectionDefinition.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((transactionSupport == null) ? 0 : transactionSupport.hashCode());
      result = prime * result + (reauthenticationSupport ? 1 : 0);
      result = prime * result + ((transactionSupportId == null) ? 0 : transactionSupportId.hashCode());
      result = prime * result + ((reauthenticationSupportId == null) ? 0 : reauthenticationSupportId.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
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
      if (transactionSupportId == null)
      {
         if (other.transactionSupportId != null)
         {
            return false;
         }
      }
      else if (!transactionSupportId.equals(other.transactionSupportId))
      {
         return false;
      }
      if (reauthenticationSupportId == null)
      {
         if (other.reauthenticationSupportId != null)
         {
            return false;
         }
      }
      else if (!reauthenticationSupportId.equals(other.reauthenticationSupportId))
      {
         return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<").append("outbound-resourceadapter");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
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
         sb.append("<").append(XML.ELEMENT_TRANSACTION_SUPPORT)
            .append(transactionSupportId == null ? "" : " id=\"" + transactionSupportId + "\"").append(">");
         sb.append(transactionSupport);
         sb.append("</").append(XML.ELEMENT_TRANSACTION_SUPPORT).append(">");
      }

      if (authenticationMechanism != null)
      {
         for (AuthenticationMechanism am : authenticationMechanism)
         {
            sb.append(am);
         }
      }

      sb.append("<").append(XML.ELEMENT_REAUTHENTICATION_SUPPORT)
         .append(reauthenticationSupportId == null ? "" : " id=\"" + reauthenticationSupportId + "\"").append(">");
      sb.append(reauthenticationSupport);
      sb.append("</").append(XML.ELEMENT_REAUTHENTICATION_SUPPORT).append(">");

      sb.append("</outbound-resourceadapter>");

      return sb.toString();
   }
}
