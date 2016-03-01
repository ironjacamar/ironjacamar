/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.api.deploymentrepository;

import java.util.Map;
import java.util.Set;

import javax.resource.spi.ActivationSpec;

/**
 * A message listener
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface MessageListener
{
   /**
    * Get the message listener
    * @return The value
    */
   public Class<?> getMessageListener();

   /**
    * Get the activation spec
    * @return The value
    */
   public Class<?> getActivationSpec();

   /**
    * Get the config properties (name, type)
    * @return The value
    */
   public Map<String, Class<?>> getConfigProperties();

   /**
    * Get the required config properties (name)
    * @return The value
    */
   public Set<String> getRequiredConfigProperties();

   /**
    * Create an activation spec instance
    * @return The value
    * @exception Exception If creation fails
    */
   public ActivationSpec createActivationSpec() throws Exception;
}
