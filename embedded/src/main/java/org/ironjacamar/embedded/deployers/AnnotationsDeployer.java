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

import org.ironjacamar.common.annotations.Annotations;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.Connector.Version;
import org.ironjacamar.common.spi.annotations.repository.AnnotationRepository;
import org.ironjacamar.common.spi.annotations.repository.AnnotationScanner;
import org.ironjacamar.common.spi.annotations.repository.AnnotationScannerFactory;
import org.ironjacamar.deployers.DeployersLogger;

import java.io.File;
import java.net.URL;

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * Process the annotations if present
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AnnotationsDeployer extends AbstractFungalRADeployer implements CloneableDeployer
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, AnnotationsDeployer.class.getName());

   /**
    * Constructor
    */
   public AnnotationsDeployer()
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
      return Constants.DEPLOYER_ANNOTATIONS;
   }

   /**
    * {@inheritDoc}
    */
   public Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      File archive = (File)context.get(Constants.ATTACHMENT_ARCHIVE);

      if (archive == null)
         throw new DeployException("Deployment " + url.toExternalForm() + " not found");

      Connector c = (Connector)context.get(Constants.ATTACHMENT_RA_XML_METADATA);

      // JCA 1.0 / JCA 1.5
      if (c != null && (Version.V_10.equals(c.getVersion()) || Version.V_15.equals(c.getVersion())))
         return null;
      
      // JCA 1.6 / JCA 1.7 metadata complete
      if (c != null && c.isMetadataComplete())
         return null;
      
      try
      {
         KernelClassLoader cl = (KernelClassLoader)context.get(Constants.ATTACHMENT_CLASSLOADER);

         Annotations processor = new Annotations();
         AnnotationScanner scanner = AnnotationScannerFactory.getAnnotationScanner();
         AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);

         Connector merged = processor.merge(c, repository, cl);

         context.put(Constants.ATTACHMENT_MERGED_METADATA, merged);

         return null;
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
      return new AnnotationsDeployer();
   }

   /**
    * {@inheritDoc}
    */
   protected DeployersLogger getLogger()
   {
      return log;
   }
}
