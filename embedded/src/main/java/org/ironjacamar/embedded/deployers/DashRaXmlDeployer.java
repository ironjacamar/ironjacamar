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

import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.Activations;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.metadata.merge.Merger;
import org.ironjacamar.common.metadata.resourceadapter.ResourceAdapterParser;
import org.ironjacamar.core.api.metadatarepository.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * -ra.xml deployer
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DashRaXmlDeployer extends AbstractFungalRADeployer implements CloneableDeployer
{
   /**
    * Constructor
    */
   public DashRaXmlDeployer()
   {
   }

   /**
    * {@inheritDoc}
    */
   public boolean accepts(URL deployment)
   {
      if (deployment != null && deployment.toExternalForm().endsWith("-ra.xml"))
         return true;

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public int getOrder()
   {
      return Constants.DEPLOYER_DASH_RA_XML;
   }

   /**
    * {@inheritDoc}
    */
   public Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      FileInputStream fis = null;
      try
      {
         File dashRaXml = new File(url.toURI());

         ResourceAdapterParser parser = new ResourceAdapterParser();
         
         fis = new FileInputStream(dashRaXml);
         
         XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(fis);

         Activations activations = parser.parse(xsr);
         Merger merger = new Merger();

         ClassLoaderDeployer classLoaderDeployer =
            context.getKernel().getBean("ClassLoaderDeployer", ClassLoaderDeployer.class);

         List<org.ironjacamar.core.api.deploymentrepository.Deployment> deployments =
            new ArrayList<org.ironjacamar.core.api.deploymentrepository.Deployment>();
         
         for (Activation activation : activations.getActivations())
         {
            Metadata metadata = metadataRepository.findByName(activation.getArchive());

            if (metadata == null)
               throw new DeployException("Deployment " + activation.getArchive() + " not found");

            Connector c = metadata.getMetadata();
            File archive = metadata.getArchive();
            
            Connector actC = merger.merge(c.copy(), activation);
            
            // Create a class loader for the archive
            context.put(Constants.ATTACHMENT_ARCHIVE, archive);
            classLoaderDeployer.clone().deploy(url, context, parent);
            KernelClassLoader cl = (KernelClassLoader)context.get(Constants.ATTACHMENT_CLASSLOADER);
            
            deployments.add(activate(actC, activation, cl));
         }

         return new ActivationDeployment(url, deployments, deploymentRepository, deployments.get(0).getClassLoader());
      }
      catch (DeployException de)
      {
         throw de;
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
      }
      finally
      {
         if (fis != null)
         {
            try
            {
               fis.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public Deployer clone() throws CloneNotSupportedException
   {
      DashRaXmlDeployer d = new DashRaXmlDeployer();
      d.setDeploymentRepository(deploymentRepository);
      d.setMetadataRepository(metadataRepository);
      d.setBootstrapContext(bootstrapContext);
      d.setJndiStrategy(jndiStrategy);
      return d;
   }
}
