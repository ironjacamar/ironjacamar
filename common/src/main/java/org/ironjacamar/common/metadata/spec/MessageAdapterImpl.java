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
import org.ironjacamar.common.api.metadata.spec.MergeableMetadata;
import org.ironjacamar.common.api.metadata.spec.MessageListener;
import org.ironjacamar.common.api.metadata.spec.Messageadapter;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Messageadapter implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MessageAdapterImpl extends AbstractMetadata implements Messageadapter
{
   private static final long serialVersionUID = 1L;

   private List<MessageListener> messagelisteners;

   private String id;

   /**
    * Constructor
    * @param messagelisteners  messagelisteners specifies information about a
    *  specific message listener supported by the messaging
    *  resource adapter. It contains information on the Java type
    *  of the message listener interface and an activation
    *  specification.
    * @param id XML ID
    */
   public MessageAdapterImpl(List<MessageListener> messagelisteners, String id)
   {
      super(null);
      if (messagelisteners != null)
      {
         this.messagelisteners = new ArrayList<MessageListener>(messagelisteners);
      }
      else
      {
         this.messagelisteners = new ArrayList<MessageListener>(0);
      }
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public List<MessageListener> getMessagelisteners()
   {
      return Collections.unmodifiableList(messagelisteners);
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
   public Messageadapter merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof MessageAdapterImpl)
      {
         MessageAdapterImpl input = (MessageAdapterImpl) jmd;

         String newId = this.id == null ? input.id : this.id;

         List<MessageListener> newMessagelistener = MergeUtil.mergeList(this.messagelisteners,
               input.messagelisteners);
         return new MessageAdapterImpl(newMessagelistener, newId);
      }
      else
      {
         return this;
      }
   }

   /**
    * {@inheritDoc}
    */
   public Messageadapter copy()
   {
      return new MessageAdapterImpl(CopyUtil.cloneList(messagelisteners), CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((messagelisteners == null) ? 0 : messagelisteners.hashCode());
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
      if (!(obj instanceof MessageAdapterImpl))
      {
         return false;
      }
      MessageAdapterImpl other = (MessageAdapterImpl) obj;
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
      if (messagelisteners == null)
      {
         if (other.messagelisteners != null)
         {
            return false;
         }
      }
      else if (!messagelisteners.equals(other.messagelisteners))
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

      sb.append("<messageadapter");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      if (messagelisteners != null)
      {
         for (MessageListener ml : messagelisteners)
         {
            sb.append(ml);
         }
      }

      sb.append("</messageadapter>");
      
      return sb.toString();
   }
}
