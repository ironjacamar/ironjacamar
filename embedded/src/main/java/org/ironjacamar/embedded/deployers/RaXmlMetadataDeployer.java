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
import org.ironjacamar.common.metadata.spec.RaParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * Load the META-INF/ra.xml file if present
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class RaXmlMetadataDeployer extends AbstractFungalRADeployer implements CloneableDeployer
{
   /**
    * Constructor
    */
   public RaXmlMetadataDeployer()
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
      return Constants.DEPLOYER_RA_XML_METADATA;
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
         File raXml = new File(archive, "META-INF/ra.xml");

         if (raXml.exists())
         {
            RaParser parser = new RaParser();

            fis = new FileInputStream(raXml);
            
            XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(fis);

            Connector c = parser.parse(xsr);

            context.put(Constants.ATTACHMENT_RA_XML_METADATA, c);
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
      return new RaXmlMetadataDeployer();
   }
}
