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


import org.jboss.jca.common.api.metadata.MergeUtil;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.Messageadapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class MessageAdapterImpl implements Messageadapter
{
   /**
    */
   private static final long serialVersionUID = 1731250340667920811L;

   private final ArrayList<MessageListener> messagelisteners;

   private final String id;

   /**
    * @param messagelisteners  messagelisteners specifies information about a
    *  specific message listener supported by the messaging
    *  resource adapter. It contains information on the Java type
    *  of the message listener interface and an activation
    *  specification.
    * @param id XML ID
    */
   public MessageAdapterImpl(List<MessageListener> messagelisteners, String id)
   {
      super();
      if (messagelisteners != null)
      {
         this.messagelisteners = new ArrayList<MessageListener>(messagelisteners.size());
         this.messagelisteners.addAll(messagelisteners);
      }
      else
      {
         this.messagelisteners = new ArrayList<MessageListener>(0);
      }
      this.id = id;
   }

   /**
    * @return messagelistener
    */
   @Override
   public List<MessageListener> getMessagelisteners()
   {
      return messagelisteners == null ? null : Collections.unmodifiableList(messagelisteners);
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
      result = prime * result + ((messagelisteners == null) ? 0 : messagelisteners.hashCode());
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
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "MessageAdapter [messagelisteners=" + messagelisteners + ", id=" + id + "]";
   }

   @Override
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

}
