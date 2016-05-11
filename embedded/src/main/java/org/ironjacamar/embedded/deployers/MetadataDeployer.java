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
package org.ironjacamar.embedded.deployers;

import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.core.api.metadatarepository.Metadata;
import org.ironjacamar.deployers.DeployersLogger;

import java.io.File;
import java.net.URL;

import org.jboss.logging.Logger;

import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * Register the metadata with MDR
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MetadataDeployer extends AbstractFungalRADeployer implements CloneableDeployer
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, MetadataDeployer.class.getName());

   /**
    * Constructor
    */
   public MetadataDeployer()
   {
   }

   /**
    * {@inheritDoc}
    */
   public boolean accepts(URL deployment)
   {
      return isRarArchive(deployment);
   }

   /**
    * {@inheritDoc}
    */
   public int getOrder()
   {
      return Constants.DEPLOYER_METADATA;
   }

   /**
    * {@inheritDoc}
    */
   public Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      Connector c = (Connector)context.get(Constants.ATTACHMENT_MERGED_METADATA);
      if (c == null)
         c = (Connector)context.get(Constants.ATTACHMENT_RA_XML_METADATA);

      if (c == null)
         throw new DeployException("No metadata for " + url.toExternalForm() + " found");

      try
      {
         File archive = new File(url.toURI());
         Metadata m = registerMetadata(archive.getName(), c.copy(), archive);
        
         return new MetadataDeployment(url, m, metadataRepository);
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public Deployer clone() throws CloneNotSupportedException
   {
      MetadataDeployer m = new MetadataDeployer();
      m.setMetadataRepository(metadataRepository);
      return m;
   }

   /**
    * {@inheritDoc}
    */
   protected DeployersLogger getLogger()
   {
      return log;
   }
}
