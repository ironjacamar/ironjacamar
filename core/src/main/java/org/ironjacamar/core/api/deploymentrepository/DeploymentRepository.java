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

package org.ironjacamar.core.api.deploymentrepository;

import java.util.Collection;

/**
 * The deployment repository
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface DeploymentRepository
{
   /**
    * Get the deployments
    * @return An unmodifiable collection of deployments
    */
   public Collection<Deployment> getDeployments();

   /**
    * Find a deployment
    * @param jndi The JNDI name activated in the deployment
    * @return The deployment, or <code>null</code>
    */
   public Deployment findByJndi(String jndi);

   /**
    * Find a deployment
    * @param id The identifier of the deployment
    * @return The deployment, or <code>null</code>
    */
   public Deployment findById(String id);

   /**
    * Find deployments
    * @param name The name of the deployment
    * @return The active deployments
    */
   public Collection<Deployment> findByName(String name);

   /**
    * Find deployments
    * @param messageListener The class name of the message listener
    * @return The deployments
    */
   public Collection<Deployment> findByMessageListener(String messageListener);

   /**
    * Register a deployment
    * @param deployment The deployment
    * @return <code>true</code> if registered, otherwise <code>false</code>
    */
   public boolean registerDeployment(Deployment deployment);

   /**
    * Unregister a deployment
    * @param deployment The deployment
    * @return <code>true</code> if unregistered, otherwise <code>false</code>
    */
   public boolean unregisterDeployment(Deployment deployment);
}

