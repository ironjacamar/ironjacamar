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


import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class MessageAdapter implements IdDecoratedMetadata
{
   /**
    */
   private static final long serialVersionUID = 1731250340667920811L;

   private final List<MessageListener> messagelistener;

   private final String id;

   /**
    * @param messagelistener The messagelistener specifies information about a
    *  specific message listener supported by the messaging
    *  resource adapter. It contains information on the Java type
    *  of the message listener interface and an activation
    *  specification.
    * @param id XML ID
    */
   public MessageAdapter(List<MessageListener> messagelistener, String id)
   {
      super();
      this.messagelistener = messagelistener;
      this.id = id;
   }

   /**
    * @return messagelistener
    */
   public List<MessageListener> getMessagelistener()
   {
      return Collections.unmodifiableList(messagelistener);
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
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((messagelistener == null) ? 0 : messagelistener.hashCode());
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
      if (!(obj instanceof MessageAdapter))
      {
         return false;
      }
      MessageAdapter other = (MessageAdapter) obj;
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
      if (messagelistener == null)
      {
         if (other.messagelistener != null)
         {
            return false;
         }
      }
      else if (!messagelistener.equals(other.messagelistener))
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
      return "MessageAdapter [messagelistener=" + messagelistener + ", id=" + id + "]";
   }
}
