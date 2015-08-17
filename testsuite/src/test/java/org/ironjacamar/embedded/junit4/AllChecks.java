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
package org.ironjacamar.embedded.junit4;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.api.metadatarepository.MetadataRepository;

/**
 * Verify all container objects
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AllChecks extends Condition
{
   /**
    * Constructor
    */
   public AllChecks()
   {
      super();
   }
   
   /**
    * {@inheritDoc}
    */
   public void verify(Resolver resolver) throws ConditionException
   {
      try
      {
         DeploymentRepository dr = resolver.lookup("DeploymentRepository", DeploymentRepository.class);

         if (dr == null)
            throw new ConditionException("DeploymentRepository is null");

         if (dr.getDeployments().size() > 0)
            throw new ConditionException("DeploymentRepository contains deployments: " + dr.getDeployments());

         MetadataRepository mr = resolver.lookup("MetadataRepository", MetadataRepository.class);

         if (mr == null)
            throw new ConditionException("MetadataRepository is null");

         if (mr.getMetadata().size() > 0)
            throw new ConditionException("MetadataRepository contains metadata: " + mr.getMetadata());
      }
      catch (ConditionException ce)
      {
         throw ce;
      }
      catch (Throwable t)
      {
         throw new ConditionException("Error", t);
      }
   }
}
