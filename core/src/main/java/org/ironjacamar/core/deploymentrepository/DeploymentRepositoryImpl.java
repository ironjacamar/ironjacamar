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

import org.ironjacamar.core.api.deploymentrepository.AdminObject;
import org.ironjacamar.core.api.deploymentrepository.ConnectionFactory;
import org.ironjacamar.core.api.deploymentrepository.Deployment;
import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The deployment repository implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DeploymentRepositoryImpl implements DeploymentRepository
{
   /** The deployments */
   private Collection<Deployment> deployments;

   /**
    * Constructor
    */
   public DeploymentRepositoryImpl()
   {
      this.deployments = Collections.synchronizedCollection(new ArrayList<Deployment>());
   }
   
   /**
    * {@inheritDoc}
    */
   public Collection<Deployment> getDeployments()
   {
      return Collections.unmodifiableCollection(deployments);
   }

   /**
    * {@inheritDoc}
    */
   public Deployment findByJndi(String jndi)
   {
      for (Deployment d : deployments)
      {
         for (ConnectionFactory cf : d.getConnectionFactories())
         {
            if (cf.getJndiName().equals(jndi))
               return d;
         }
         for (AdminObject ao : d.getAdminObjects())
         {
            if (ao.getJndiName().equals(jndi))
               return d;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Deployment findById(String id)
   {
      for (Deployment d : deployments)
      {
         if (d.getIdentifier() != null && d.getIdentifier().equals(id))
            return d;
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Deployment> findByName(String name)
   {
      Collection<Deployment> result = new ArrayList<Deployment>();
      for (Deployment d : deployments)
      {
         if (d.getName() != null && d.getName().equals(name))
            result.add(d);
      }
      return Collections.unmodifiableCollection(result);
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Deployment> findByMessageListener(String messageListener)
   {
      Collection<Deployment> result = new ArrayList<Deployment>();
      for (Deployment d : deployments)
      {
         if (d.getResourceAdapter() != null && d.getResourceAdapter().isMessageListenerSupported(messageListener))
            result.add(d);
      }
      return Collections.unmodifiableCollection(result);
   }

   /**
    * {@inheritDoc}
    */
   public boolean registerDeployment(Deployment deployment)
   {
      if (deployments.contains(deployment))
         return false;

      return deployments.add(deployment);
   }

   /**
    * {@inheritDoc}
    */
   public boolean unregisterDeployment(Deployment deployment)
   {
      if (!deployments.contains(deployment))
         return false;

      return deployments.remove(deployment);
   }

   /**
    *{@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("DeploymentRepository@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[");
      sb.append("deployments=").append(deployments);
      sb.append("]");
      
      return sb.toString();
   }
}
