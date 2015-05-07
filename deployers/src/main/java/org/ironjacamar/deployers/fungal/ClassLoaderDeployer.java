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
import java.net.URL;

import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * ClassLoader deployer
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ClassLoaderDeployer extends AbstractFungalRADeployer implements CloneableDeployer
{
   /** Parent first */
   private boolean parentFirst;

   /**
    * Constructor
    */
   public ClassLoaderDeployer()
   {
      this.parentFirst = true;
   }

   /**
    * Copy constructor
    * @param pf Parent first class loading
    */
   ClassLoaderDeployer(boolean pf)
   {
      this.parentFirst = pf;
   }

   /**
    * Set parent first class loader type
    * @param v The value
    */
   public void setParentFirst(boolean v)
   {
      parentFirst = v;
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
      return Constants.DEPLOYER_CLASSLOADER;
   }

   /**
    * {@inheritDoc}
    */
   public Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      File archive = (File)context.get(Constants.ATTACHMENT_ARCHIVE);

      if (archive == null)
         throw new DeployException("Deployment " + url.toExternalForm() + " not found");
      
      try
      {
         URL[] urls = getUrls(archive);
         KernelClassLoader cl = null;
         if (parentFirst)
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_FIRST, urls, parent);
         }
         else
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_LAST, urls, parent);
         }
         
         context.put(Constants.ATTACHMENT_CLASSLOADER, cl);

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
      return new ClassLoaderDeployer(parentFirst);
   }
}
