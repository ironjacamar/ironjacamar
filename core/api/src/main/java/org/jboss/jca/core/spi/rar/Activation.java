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

package org.jboss.jca.core.spi.rar;

import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;

/**
 * An activation representation
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Activation
{
   /**
    * Get the config-property map with name and type pair
    * @return The value
    */
   public Map<String, Class<?>> getConfigProperties();

   /**
    * Get the required config-property set with names
    * @return The value
    */
   public Set<String> getRequiredConfigProperties();

   /**
    * Create an instance of the associated activation spec.
    * @return The value
    * @exception NotFoundException Thrown if the class is no longer available
    * @exception InstantiationException Thrown if an object couldn't created
    * @exception IllegalAccessException Thrown if object access is inaccessible
    * @exception ResourceException Thrown if the activation spec can't be associated with the resource adapter
    */
   public ActivationSpec createInstance()
      throws NotFoundException, InstantiationException, IllegalAccessException, ResourceException;
}
