/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.spec;

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.spec.Activationspec;
import org.jboss.jca.common.api.metadata.spec.MessageListener;
import org.jboss.jca.common.api.metadata.spec.XsdString;

/**
 * A MessageListener implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MessageListenerImpl implements MessageListener
{
   private static final long serialVersionUID = 1L;

   private XsdString messagelistenerType;

   private Activationspec activationspec;

   private String id;

   /**
    * Constructor
    * @param messagelistenerType full qualified name of the java type
    * @param activationspec  The activationspecType specifies an activation
    * specification.  The information includes fully qualified
    * Java class name of an activation specification and a set of
    * required configuration property names.
    * @param id XML ID
    */
   public MessageListenerImpl(XsdString messagelistenerType, Activationspec activationspec, String id)
   {
      this.messagelistenerType = messagelistenerType;
      if (!XsdString.isNull(this.messagelistenerType))
         this.messagelistenerType.setTag(XML.MessageListenerTag.MESSAGELISTENER_TYPE.toString());
      this.activationspec = activationspec;
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getMessagelistenerType()
   {
      return messagelistenerType;
   }

   /**
    * {@inheritDoc}
    */
   public Activationspec getActivationspec()
   {
      return activationspec;
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
   public CopyableMetaData copy()
   {
      return new MessageListenerImpl(CopyUtil.clone(messagelistenerType), CopyUtil.clone(activationspec),
                                     CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((activationspec == null) ? 0 : activationspec.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((messagelistenerType == null) ? 0 : messagelistenerType.hashCode());
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
      if (!(obj instanceof MessageListenerImpl))
      {
         return false;
      }
      MessageListenerImpl other = (MessageListenerImpl) obj;
      if (activationspec == null)
      {
         if (other.activationspec != null)
         {
            return false;
         }
      }
      else if (!activationspec.equals(other.activationspec))
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
      if (messagelistenerType == null)
      {
         if (other.messagelistenerType != null)
         {
            return false;
         }
      }
      else if (!messagelistenerType.equals(other.messagelistenerType))
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

      sb.append("<messagelistener");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      sb.append(messagelistenerType);

      sb.append(activationspec);

      sb.append("</messagelistener>");
      
      return sb.toString();
   }
}
