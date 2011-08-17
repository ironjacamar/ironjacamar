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
import org.jboss.jca.common.api.metadata.ra.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.Messageadapter;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class InboundResourceAdapterImpl implements InboundResourceAdapter
{
   /**
    */
   private static final long serialVersionUID = -2854927981408307535L;

   private final Messageadapter messageadapter;

   private final String id;

   /**
    * @param messageadapter The messageadapterType specifies information about the
    * messaging capabilities of the resource adapter. This
    * contains information specific to the implementation of the
    * resource adapter library as specified through the
    * messagelistener element.
    * @param id XML ID
    */
   public InboundResourceAdapterImpl(Messageadapter messageadapter, String id)
   {
      super();
      this.messageadapter = messageadapter;
      this.id = id;
   }

   /**
    * @return messageadapter
    */
   @Override
   public Messageadapter getMessageadapter()
   {
      return messageadapter;
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
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((messageadapter == null) ? 0 : messageadapter.hashCode());
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
      if (!(obj instanceof InboundResourceAdapterImpl))
      {
         return false;
      }
      InboundResourceAdapterImpl other = (InboundResourceAdapterImpl) obj;
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
      if (messageadapter == null)
      {
         if (other.messageadapter != null)
         {
            return false;
         }
      }
      else if (!messageadapter.equals(other.messageadapter))
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

      sb.append("<").append("inbound-resourceadapter");
      if (id != null)
         sb.append(" ").append(InboundResourceAdapter.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      if (messageadapter != null)
      {
         sb.append(messageadapter);
      }

      sb.append("</inbound-resourceadapter>");

      return sb.toString();
   }

   /**
    *
    * A validate method. Don't extending for the moment  ValidatableMetadata
    *
    * @return true if Ra is valid, flase in the other cases
    */
   @Override
   public boolean validationAsBoolean()
   {
      if (this.getMessageadapter() == null || this.getMessageadapter().getMessagelisteners() == null
            || this.getMessageadapter().getMessagelisteners().size() == 0)
         return false;
      MessageListener mlmd = this.getMessageadapter().getMessagelisteners().get(0);
      if (mlmd.getMessagelistenerType() == null || mlmd.getActivationspec() == null
            || mlmd.getActivationspec().getActivationspecClass() == null)
         return false;
      return true;
   }

   @Override
   public InboundResourceAdapter merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof InboundResourceAdapterImpl)
      {
         InboundResourceAdapterImpl input = (InboundResourceAdapterImpl) jmd;
         String newId = this.id == null ? input.id : this.id;

         Messageadapter newMessageadapter = this.messageadapter == null ? input.messageadapter : this.messageadapter
               .merge(input.messageadapter);
         return new InboundResourceAdapterImpl(newMessageadapter, newId);
      }
      else
      {
         return this;
      }
   }

   @Override
   public CopyableMetaData copy()
   {
      return new InboundResourceAdapterImpl(CopyUtil.clone(messageadapter), CopyUtil.cloneString(id));
   }

}
