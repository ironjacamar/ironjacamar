/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.spi.mdr;

import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.Connector;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The SPI for the metadata repository
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface MetadataRepository
{
   /**
    * Register a resource adapter template
    * @param uniqueId An unique id that represents the deployment
    * @param root The deployment root
    * @param md The connector metadata
    * @param a The Activation metadata
    * @exception AlreadyExistsException Thrown if the unique id is already registered
    */
   public void registerResourceAdapter(String uniqueId, File root, Connector md, Activation a) 
      throws AlreadyExistsException;

   /**
    * Unregister a resource adapter template
    * @param uniqueId An unique id that represents the deployment
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public void unregisterResourceAdapter(String uniqueId) throws NotFoundException;

   /**
    * Check if there exists a resource adapter for a unique id
    * @param uniqueId An unique id that represents the deployment
    * @return True if there is a resource adapter; otherwise false
    */
   public boolean hasResourceAdapter(String uniqueId);

   /**
    * Get the metadata for a resource adapter
    * @param uniqueId An unique id that represents the deployment
    * @return The metadata
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public Connector getResourceAdapter(String uniqueId) throws NotFoundException;

   /**
    * Get the resource adapters unique ids registered
    * @return The unique ids
    */
   public Set<String> getResourceAdapters();

   /**
    * Get the root for a resource adapter deployment
    * @param uniqueId An unique id that represents the deployment
    * @return The root
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public File getRoot(String uniqueId) throws NotFoundException;

   /**
    * Get the Activation metadata for a resource adapter deployment
    * @param uniqueId An unique id that represents the deployment
    * @return The metadata
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public Activation getActivation(String uniqueId) throws NotFoundException;

   /**
    * Register a JNDI mapping for a unique id
    * @param uniqueId An unique id that represents the deployment
    * @param clz The fully qualified class name
    * @param jndi The JNDI name
    */
   public void registerJndiMapping(String uniqueId, String clz, String jndi);

   /**
    * Unregister a JNDI mapping for a unique id
    * @param uniqueId An unique id that represents the deployment
    * @param clz The fully qualified class name
    * @param jndi The JNDI name
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public void unregisterJndiMapping(String uniqueId, String clz, String jndi) throws NotFoundException;

   /**
    * Check if there exists JNDI mappings for a unique id
    * @param uniqueId An unique id that represents the deployment
    * @return True if there are mappings; otherwise false
    */
   public boolean hasJndiMappings(String uniqueId);

   /**
    * Get the JNDI mappings for a unique id
    * @param uniqueId An unique id that represents the deployment
    * @return The mappings
    * @exception NotFoundException Thrown if the unique id isn't registered
    */
   public Map<String, List<String>> getJndiMappings(String uniqueId) throws NotFoundException;
}
