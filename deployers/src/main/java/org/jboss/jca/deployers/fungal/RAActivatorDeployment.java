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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

/**
 * A resource adapter activator deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RAActivatorDeployment extends AbstractFungalDeployment
{
   /**
    * Constructor
    * @param deployment The deployment
    * @param deploymentName The deployment name
    * @param ra The resource adapter instance if present
    * @param jndiStrategy The JNDI strategy
    * @param mdr The metadata repository
    * @param cfs The connection factories
    * @param cfJndis The JNDI names for the connection factories
    * @param aos The admin objects
    * @param aoJndis The JNDI names for the admin objects
    * @param managementRepository The management repository
    * @param connector The management connector instance
    * @param server The MBeanServer
    * @param objectNames The ObjectNames
    * @param cl The classloader for the deployment
    * @param log The logger
    */
   public RAActivatorDeployment(URL deployment, String deploymentName, ResourceAdapter ra, JndiStrategy jndiStrategy,
                                MetadataRepository mdr, 
                                Object[] cfs, String[] cfJndis, 
                                Object[] aos, String[] aoJndis, 
                                ManagementRepository managementRepository, Connector connector,
                                MBeanServer server, List<ObjectName> objectNames,
                                ClassLoader cl, Logger log)
   {
      super(deployment, deploymentName, true, ra, jndiStrategy, mdr, 
            cfs, cfJndis, aos, aoJndis, managementRepository, connector, server, objectNames, cl, log);
   }
}
