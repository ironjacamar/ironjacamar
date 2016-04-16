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
import org.ironjacamar.common.api.metadata.spec.InboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.MergeableMetadata;
import org.ironjacamar.common.api.metadata.spec.MessageListener;
import org.ironjacamar.common.api.metadata.spec.Messageadapter;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

/**
 * An InboundResourceAdapter implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class InboundResourceAdapterImpl extends AbstractMetadata implements InboundResourceAdapter
{
   private static final long serialVersionUID = 1L;

   private Messageadapter messageadapter;

   private String id;

   /**
    * Constructor
    * @param messageadapter The messageadapterType specifies information about the
    * messaging capabilities of the resource adapter. This
    * contains information specific to the implementation of the
    * resource adapter library as specified through the
    * messagelistener element.
    * @param id XML ID
    */
   public InboundResourceAdapterImpl(Messageadapter messageadapter, String id)
   {
      super(null);
      this.messageadapter = messageadapter;
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public Messageadapter getMessageadapter()
   {
      return messageadapter;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * A validate method. Don't extending for the moment  ValidatableMetadata
    *
    * @return true if Ra is valid, false in the other cases
    */
   public boolean validationAsBoolean()
   {
      if (this.getMessageadapter() == null || this.getMessageadapter().getMessagelisteners() == null
            || this.getMessageadapter().getMessagelisteners().isEmpty())
         return false;
      MessageListener mlmd = this.getMessageadapter().getMessagelisteners().get(0);
      if (mlmd.getMessagelistenerType() == null || mlmd.getActivationspec() == null
            || mlmd.getActivationspec().getActivationspecClass() == null)
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
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

   /**
    * {@inheritDoc}
    */
   public InboundResourceAdapter copy()
   {
      return new InboundResourceAdapterImpl(CopyUtil.clone(messageadapter), CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
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
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<").append("inbound-resourceadapter");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      if (messageadapter != null)
      {
         sb.append(messageadapter);
      }

      sb.append("</inbound-resourceadapter>");

      return sb.toString();
   }
}
