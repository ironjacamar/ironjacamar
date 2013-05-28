/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.rar;

import org.jboss.jca.core.spi.rar.Activation;
import org.jboss.jca.core.spi.rar.MessageListener;

/**
 * A message listener implementation
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MessageListenerImpl implements MessageListener
{
   /** The message listener type */
   private Class<?> type;

   /** The activation */
   private Activation activation;

   /**
    * Constructor
    * @param type The type
    * @param activation The activation
    */
   MessageListenerImpl(Class<?> type, Activation activation)
   {
      this.type = type;
      this.activation = activation;
   }

   /**
    * {@inheritDoc}
    */
   public Class<?> getType()
   {
      return type;
   }

   /**
    * {@inheritDoc}
    */
   public Activation getActivation()
   {
      return activation;
   }
}
