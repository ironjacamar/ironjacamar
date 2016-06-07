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

import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.api.metadatarepository.MetadataRepository;

import org.jboss.logging.Logger;
import org.jboss.threads.QueueExecutor;

/**
 * Verify all container objects
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AllChecks extends Condition
{
   /** The logger */
   private static Logger log = Logger.getLogger(AllChecks.class);

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
         CachedConnectionManager ccm = resolver.lookup("CachedConnectionManager", CachedConnectionManager.class);
         if (ccm == null)
            throw new ConditionException("CachedConnectionManager is null");

         ccm.setDebug(true);

         if (ccm.getNumberOfConnections() != 0)
            throw new ConditionException("CachedConnectionManager contains connections: " + ccm.listConnections());
         
         DeploymentRepository dr = resolver.lookup("DeploymentRepository", DeploymentRepository.class);

         if (dr == null)
            throw new ConditionException("DeploymentRepository is null");

         if (!dr.getDeployments().isEmpty())
            throw new ConditionException("DeploymentRepository contains deployments: " + dr.getDeployments());

         MetadataRepository mr = resolver.lookup("MetadataRepository", MetadataRepository.class);

         if (mr == null)
            throw new ConditionException("MetadataRepository is null");

         if (!mr.getMetadata().isEmpty())
            throw new ConditionException("MetadataRepository contains metadata: " + mr.getMetadata());

         QueueExecutor srThread = resolver.lookup("ShortRunningThreadPool", QueueExecutor.class);
         if (srThread.getQueueSize() > 0)
            throw new ConditionException("ShortRunningThreadPool is not idle");

         QueueExecutor lrThread = resolver.lookup("LongRunningThreadPool", QueueExecutor.class);
         if (lrThread.getQueueSize() > 0)
            throw new ConditionException("LongRunningThreadPool is not idle");


      }
      catch (ConditionException ce)
      {
         log.error(ce.getMessage(), ce);
         throw ce;
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         throw new ConditionException("Error", t);
      }
   }
}
