/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.common.api.metadata.ra.Connector;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The SPI for the metadata repository
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface MetadataRepository
{
   /**
    * Register a resource adapter template
    * @param deployment The deployment
    * @param root The deployment root
    * @param md The metadata
    * @exception AlreadyExistsException Thrown if the deployment is already registered
    */
   public void registerResourceAdapter(URL deployment, File root, Connector md) throws AlreadyExistsException;

   /**
    * Unregister a resource adapter template
    * @param deployment The deployment
    * @exception NotFoundException Thrown if the deployment isn't registered
    */
   public void unregisterResourceAdapter(URL deployment) throws NotFoundException;

   /**
    * Get the metadata for a resource adapter
    * @param deployment The deployment
    * @return The metadata
    * @exception NotFoundException Thrown if the deployment isn't registered
    */
   public Connector getResourceAdapter(URL deployment) throws NotFoundException;

   /**
    * Get the resource adapters deployments registered
    * @return The names of the deployments
    */
   public Set<URL> getResourceAdapters();

   /**
    * Get the root for a resource adapter deployment
    * @param deployment The deployment
    * @return The root
    * @exception NotFoundException Thrown if the deployment isn't registered
    */
   public File getRoot(URL deployment) throws NotFoundException;

   /**
    * Register a JNDI mapping for a deployment
    * @param deployment The deployment
    * @param clz The fully qualified class name
    * @param jndi The JNDI name
    */
   public void registerJndiMapping(URL deployment, String clz, String jndi);

   /**
    * Unregister a JNDI mapping for a deployment
    * @param deployment The deployment
    * @param clz The fully qualified class name
    * @param jndi The JNDI name
    */
   public void unregisterJndiMapping(URL deployment, String clz, String jndi);

   /**
    * Get the JNDI mappings for a deployment
    * @param deployment The deployment
    * @return The mappings
    */
   public Map<String, List<String>> getJndiMappings(URL deployment);
}
