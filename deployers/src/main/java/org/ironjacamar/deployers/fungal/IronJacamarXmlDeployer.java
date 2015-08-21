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
package org.ironjacamar.deployers.fungal;

import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.metadata.ironjacamar.IronJacamarParser;
import org.ironjacamar.common.metadata.merge.Merger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * Activate the resource adapter based on the META-INF/ironjacamar.xml file if present
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class IronJacamarXmlDeployer extends AbstractFungalRADeployer implements CloneableDeployer
{
   /**
    * Constructor
    */
   public IronJacamarXmlDeployer()
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
      return Constants.DEPLOYER_IRONJACAMAR_XML;
   }

   /**
    * {@inheritDoc}
    */
   public Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      File archive = (File)context.get(Constants.ATTACHMENT_ARCHIVE);

      if (archive == null)
         throw new DeployException("Deployment " + url.toExternalForm() + " not found");

      FileInputStream fis = null;
      try
      {
         File ijXml = new File(archive, "META-INF/ironjacamar.xml");

         if (ijXml.exists())
         {
            IronJacamarParser parser = new IronJacamarParser();

            fis = new FileInputStream(ijXml);
            
            XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(fis);

            Activation a = parser.parse(xsr);

            Connector c = (Connector)context.get(Constants.ATTACHMENT_MERGED_METADATA);
            if (c == null)
               c = (Connector)context.get(Constants.ATTACHMENT_RA_XML_METADATA);

            KernelClassLoader cl = (KernelClassLoader)context.get(Constants.ATTACHMENT_CLASSLOADER);

            Merger merger = new Merger();
            Connector actC = merger.merge(c.copy(), a);
            
            activate(actC, a, cl);
         }

         return null;
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
      IronJacamarXmlDeployer i = new IronJacamarXmlDeployer();
      i.setDeploymentRepository(deploymentRepository);
      i.setMetadataRepository(metadataRepository);
      i.setBootstrapContext(bootstrapContext);
      i.setJndiStrategy(jndiStrategy);
      return i;
   }
}
