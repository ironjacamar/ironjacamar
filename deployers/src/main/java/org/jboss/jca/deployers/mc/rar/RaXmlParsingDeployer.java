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

package org.jboss.jca.deployers.mc.rar;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;

/**
 * RaXmlParsingDeployer parses the Java Connector Architecture ra.xml
 * file
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 */
public class RaXmlParsingDeployer extends AbstractParsingDeployer
{
   /** The logger */
   private static Logger log = Logger.getLogger(RaXmlParsingDeployer.class);

   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   public RaXmlParsingDeployer()
   {
      log.info("Constructing RaXmlParsingDeployer");
   }

   /**
    * Deploy
    * @param unit The deployment unit
    * @exception DeploymentException Thrown if the deployment fails
    */
   public void deploy(DeploymentUnit unit) throws DeploymentException 
   {
      if (unit instanceof VFSDeploymentUnit)
      {
         deploy((VFSDeploymentUnit)unit);
      }
   }

   /**
    * Deploy
    * @param unit The deployment unit
    * @exception DeploymentException Thrown if the deployment fails
    */
   public void deploy(VFSDeploymentUnit unit) throws DeploymentException 
   {
      throw new DeploymentException("NYI");
   }
}
