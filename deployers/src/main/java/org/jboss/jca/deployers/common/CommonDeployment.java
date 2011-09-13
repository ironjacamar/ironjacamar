/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.common;

import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.DataSource;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;
import org.jboss.jca.deployers.DeployersLogger;

import java.net.URL;
import java.util.Arrays;

import javax.resource.spi.ResourceAdapter;

/**
 *
 * A CommonDeployment.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CommonDeployment
{

   private final URL url;

   private final String deploymentName;

   private final boolean activateDeployment;

   private final ResourceAdapter resourceAdapter;

   private final String resourceAdapterKey;

   private final Object[] cfs;

   private final String[] cfJndiNames;

   private final ConnectionManager[] cfCMs;

   private final Object[] aos;

   private final String[] aoJndiNames;

   private final XAResourceRecovery[] recoveryModules;

   private final Connector connector;

   private final DataSource[] dataSources;

   private final ClassLoader cl;

   private final DeployersLogger log;

   /**
    * Create a new Deployment.
    *
    * @param url url
    * @param deploymentName deploymentName
    * @param activateDeployment activateDeployment
    * @param resourceAdapter resourceAdapter
    * @param resourceAdapterKey resourceAdapter key
    * @param cfs The connection factories
    * @param cfJndiNames The JNDI names for the connection factories
    * @param cfCM The connection managers
    * @param aos The admin objects
    * @param aoJndiNames The JNDI names for the admin objects
    * @param recoveryModules The recovery modules
    * @param connector The management view of a connector
    * @param dataSources The management view of datasources
    * @param cl cl
    * @param log log
    */
   public CommonDeployment(URL url, String deploymentName, boolean activateDeployment,
                           ResourceAdapter resourceAdapter, String resourceAdapterKey,
                           Object[] cfs, String[] cfJndiNames, ConnectionManager[] cfCM,
                           Object[] aos, String[] aoJndiNames,
                           XAResourceRecovery[] recoveryModules,
                           Connector connector, DataSource[] dataSources,
                           ClassLoader cl, DeployersLogger log)
   {
      super();
      this.url = url;
      this.deploymentName = deploymentName;
      this.activateDeployment = activateDeployment;
      this.resourceAdapter = resourceAdapter;
      this.resourceAdapterKey = resourceAdapterKey;
      this.cfs = cfs != null ? Arrays.copyOf(cfs, cfs.length) : null;
      this.cfJndiNames = cfJndiNames != null ? Arrays.copyOf(cfJndiNames, cfJndiNames.length) : null;
      this.cfCMs = cfCM != null ? Arrays.copyOf(cfCM, cfCM.length) : null;
      this.aos = aos != null ? Arrays.copyOf(aos, aos.length) : null;
      this.aoJndiNames = aoJndiNames != null ? Arrays.copyOf(aoJndiNames, aoJndiNames.length) : null;
      this.recoveryModules = recoveryModules;
      this.connector = connector;
      this.dataSources = dataSources;
      this.cl = cl;
      this.log = log;
   }

   /**
    * Get the url.
    *
    * @return the url.
    */
   public final URL getURL()
   {
      return url;
   }

   /**
    * Get the deploymentName.
    *
    * @return the deploymentName.
    */
   public final String getDeploymentName()
   {
      return deploymentName;
   }

   /**
    * Get the activateDeployment.
    *
    * @return the activateDeployment.
    */
   public final boolean isActivateDeployment()
   {
      return activateDeployment;
   }

   /**
    * Get the resourceAdapter.
    *
    * @return the resourceAdapter.
    */
   public final ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * Get the resource adapter key
    * @return The value
    */
   public final String getResourceAdapterKey()
   {
      return resourceAdapterKey;
   }

   /**
    * Get the cfs.
    *
    * @return the cfs.
    */
   public final Object[] getCfs()
   {
      return cfs != null ? Arrays.copyOf(cfs, cfs.length) : null;
   }

   /**
    * Get the connection factory JNDI names.
    *
    * @return the jndiNames.
    */
   public final String[] getCfJndiNames()
   {
      return cfJndiNames != null ? Arrays.copyOf(cfJndiNames, cfJndiNames.length) : null;
   }

   /**
    * Get the connection managers.
    *
    * @return The value
    */
   public final ConnectionManager[] getConnectionManagers()
   {
      return cfCMs != null ? Arrays.copyOf(cfCMs, cfCMs.length) : null;
   }

   /**
    * Get the aos.
    *
    * @return the aos.
    */
   public final Object[] getAos()
   {
      return aos != null ? Arrays.copyOf(aos, aos.length) : null;
   }

   /**
    * Get the admin object JNDI names.
    *
    * @return the jndiNames.
    */
   public final String[] getAoJndiNames()
   {
      return aoJndiNames != null ? Arrays.copyOf(aoJndiNames, aoJndiNames.length) : null;
   }

   /**
    * Get the recovery modules.
    * @return The value
    */
   public final XAResourceRecovery[] getRecovery()
   {
      return recoveryModules;
   }
 
   /**
    * Get the management view of the connector
    * @return The value
    */
   public final Connector getConnector()
   {
      return connector;
   }

   /**
    * Get the management view of the datasources
    * @return The value
    */
   public final DataSource[] getDataSources()
   {
      return dataSources;
   }

   /**
    * Get the cl.
    *
    * @return the cl.
    */
   public final ClassLoader getCl()
   {
      return cl;
   }

   /**
    * Get the log.
    *
    * @return the log.
    */
   public final DeployersLogger getLog()
   {
      return log;
   }
}
