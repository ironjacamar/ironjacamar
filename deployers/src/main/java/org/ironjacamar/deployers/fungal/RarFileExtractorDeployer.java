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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * RAR file extractor deployer
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class RarFileExtractorDeployer extends AbstractFungalRADeployer implements CloneableDeployer
{
   /**
    * Constructor
    */
   public RarFileExtractorDeployer()
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
      return Constants.DEPLOYER_RAR_FILE_EXTRACTOR;
   }

   /**
    * {@inheritDoc}
    */
   public Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      boolean extracted = false;
      File destination = null;
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            throw new IOException("Archive " + url.toExternalForm() + " doesnt exists");

         File root = null;
         if (f.isFile())
         {
            FileUtil fileUtil = new FileUtil();
            destination = new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/tmp/");
            root = fileUtil.extract(f, destination);
            extracted = true;
         }
         else
         {
            root = f;
         }

         context.put(Constants.ATTACHMENT_ARCHIVE, root);

         if (extracted)
         {
            return new TempDirectoryDeployment(url, root);
         }
         else
         {
            return null;
         }
      }
      catch (Throwable t)
      {
         if (destination != null)
         {
            try
            {
               FileUtil fileUtil = new FileUtil();
               fileUtil.delete(destination);
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
         
         throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public Deployer clone() throws CloneNotSupportedException
   {
      return new RarFileExtractorDeployer();
   }
}
