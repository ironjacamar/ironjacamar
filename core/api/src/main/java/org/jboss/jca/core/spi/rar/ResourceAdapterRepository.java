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

import java.util.List;
import java.util.Set;

import javax.resource.spi.ResourceAdapter;

/**
 * The SPI for the resource adapter repository
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ResourceAdapterRepository
{
   /**
    * Register a resource adapter
    * @param ra The resource adapter instance
    * @return The unique id for the resource adapter
    */
   public String registerResourceAdapter(ResourceAdapter ra);

   /**
    * Unregister a resource adapter
    * @param key The key for the resource adapter instance
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public void unregisterResourceAdapter(String key) throws NotFoundException;

   /**
    * Get the resource adapter instance based on the unique id
    * @param uniqueId An unique id that represents the deployment
    * @return The resource adapter
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public ResourceAdapter getResourceAdapter(String uniqueId) throws NotFoundException;

   /**
    * Get the resource adapters unique ids registered
    * @return The unique ids
    */
   public Set<String> getResourceAdapters();

   /**
    * Get the resource adapters unique ids registered which has the specified
    * message listener type
    * @param messageListenerType The message listener type
    * @return The unique ids
    */
   public Set<String> getResourceAdapters(Class<?> messageListenerType);

   /**
    * Get an endpoint representation for a resource adapter
    * @param uniqueId An unique id that represents the deployment
    * @return The endpoint
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public Endpoint getEndpoint(String uniqueId) throws NotFoundException;

   /**
    * Get a list of message listeners supported for a resource adapter
    * @param uniqueId An unique id that represents the deployment
    * @return The list of message listeners
    * @exception NotFoundException Thrown if the unique id isn't registered
    * @exception InstantiationException Thrown if an object couldn't created
    * @exception IllegalAccessException Thrown if object access is inaccessible
    */
   public List<MessageListener> getMessageListeners(String uniqueId) 
      throws NotFoundException, InstantiationException, IllegalAccessException;

   /**
    * Set the recovery mode for a resource adapter
    * @param uniqueId An unique id that represents the deployment
    * @param isXA Is the resource adapter instance XA capable
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public void setRecoveryForResourceAdapter(String uniqueId, boolean isXA)
      throws NotFoundException;
}
