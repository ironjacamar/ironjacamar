/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.api;

import org.jboss.metadata.rar.jboss.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.metadata.rar.spec.ConnectorMetaData;

/**
 * MetaDataRepository API
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public interface MetaDataRepository
{
   /**
    * Add connector metadata
    * @param name The name
    * @param cmd The connector metadata
    * @return The previous value
    */
   public ConnectorMetaData addConnectorMetaData(String name, ConnectorMetaData cmd);

   /**
    * Get connector metadata
    * @param name The name
    * @return The connector metadata
    */
   public ConnectorMetaData getConnectorMetaData(String name);

   /**
    * Remove connector metadata
    * @param name The name
    * @return True if the metadata was removed; otherwise
    */
   public boolean removeConnectorMetaData(String name);

   /**
    * Add managed connecton metadata
    * @param name The name
    * @param group The managed connection metadata
    * @return The previous value
    */
   public ManagedConnectionFactoryDeploymentGroup 
   addManagedConnectionFactoryDeploymentGroup(String name, ManagedConnectionFactoryDeploymentGroup group);

   /**
    * Get managed connection metadata
    * @param name The name
    * @return The managed connection metadata
    */
   public ManagedConnectionFactoryDeploymentGroup getManagedConnectionFactoryDeploymentGroup(String name);

   /**
    * Remove managed connection metadata
    * @param name The name
    * @return True if the metadata was removed; otherwise
    */
   public boolean removeManagedConnectionFactoryDeploymentGroup(String name);
}
