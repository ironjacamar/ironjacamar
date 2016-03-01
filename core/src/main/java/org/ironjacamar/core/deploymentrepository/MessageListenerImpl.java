/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.deploymentrepository;

import org.ironjacamar.core.api.deploymentrepository.MessageListener;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.ActivationSpec;

/**
 * A message listener implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MessageListenerImpl implements MessageListener
{
   /** The message listener class */
   private Class<?> messageListenerClass;
   
   /** The activation spec definition */
   private ActivationSpecImpl as;
   
   /** The activation spec class */
   private Class<? extends ActivationSpec> activationSpecClass;
   
   /**
    * Constructor
    * @param messageListenerClass The message listener class
    * @param as The activation spec
    * @param cl The class loader
    * @exception Exception If the activation spec can't be resolved
    */
   public MessageListenerImpl(Class<?> messageListenerClass, ActivationSpecImpl as, ClassLoader cl) throws Exception
   {
      this.messageListenerClass = messageListenerClass;
      this.as = as;
      this.activationSpecClass = (Class<? extends ActivationSpec>)Class.forName(as.getClassName(), true, cl);
   }
   
   /**
    * {@inheritDoc}
    */
   public Class<?> getMessageListener()
   {
      return messageListenerClass;
   }

   /**
    * {@inheritDoc}
    */
   public Class<?> getActivationSpec()
   {
      return activationSpecClass;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, Class<?>> getConfigProperties()
   {
      return Collections.unmodifiableMap(as.getConfigProperties());
   }

   /**
    * {@inheritDoc}
    */
   public Set<String> getRequiredConfigProperties()
   {
      return Collections.unmodifiableSet(as.getRequiredConfigProperties());
   }

   /**
    * {@inheritDoc}
    */
   public ActivationSpec createActivationSpec() throws Exception
   {
      return activationSpecClass.newInstance();
   }
}
