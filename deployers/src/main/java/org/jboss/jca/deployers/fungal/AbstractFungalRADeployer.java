/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.deployers.common.AbstractResourceAdapterDeployer;
import org.jboss.jca.deployers.common.DeployException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

import com.github.fungal.api.util.Injection;
import com.github.fungal.api.util.JarFilter;

/**
 * An abstract resource adapter deployer which contains common functionality
 * for all resource adapter archive based deployers.
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractFungalRADeployer extends AbstractResourceAdapterDeployer
{

   /**
    * Create a new AbstractResourceAdapterDeployer.
    *
    * @param validateClasses boolean to express if this instance will apply validation on classes structure
    * @param log the right log where put messages
    */
   public AbstractFungalRADeployer(boolean validateClasses, Logger log)
   {
      super(validateClasses, log);
   }

   /**
    * Initialize and inject configuration properties
    * @param className The fully qualified class name
    * @param configs The configuration properties
    * @param cl The class loader
    * @return The object
    * @throws DeployException Thrown if the object cant be initialized
    */
   @Override
   public Object initAndInject(String className, List<? extends ConfigProperty> configs, ClassLoader cl)
      throws DeployException
   {
      try
      {
         Class clz = Class.forName(className, true, cl);
         Object o = clz.newInstance();

         if (configs != null)
         {
            Injection injector = new Injection();
            for (ConfigProperty cpmd : configs)
            {
               if (cpmd.isValueSet())
                  injector.inject(cpmd.getConfigPropertyType().getValue(), cpmd.getConfigPropertyName().getValue(),
                     cpmd.getConfigPropertyValue().getValue(), o);
            }
         }

         return o;
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + className + " failed", t);
      }
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   protected URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         if (jars != null)
         {
            for (int j = 0; j < jars.length; j++)
            {
               list.add(jars[j].getCanonicalFile().toURI().toURL());
            }
         }
      }
      return list.toArray(new URL[list.size()]);
   }

   @Override
   public String[] bindConnectionFactory(URL url, String deployment, Object cf) throws Throwable
   {
      JndiStrategy js = ((RAConfiguration) getConfiguration()).getJndiStrategy().clone();

      String[] result = js.bindConnectionFactories(deployment, new Object[]{cf});

      ((RAConfiguration) getConfiguration()).getMetadataRepository().registerJndiMapping(url.toExternalForm(),
         cf.getClass().getName(), result[0]);

      return result;
   }

   @Override
   public String[] bindConnectionFactory(URL url, String deployment, Object cf, String jndi) throws Throwable
   {
      JndiStrategy js = ((RAConfiguration) getConfiguration()).getJndiStrategy().clone();

      String[] result = js.bindConnectionFactories(deployment, new Object[]{cf}, new String[]{jndi});

      ((RAConfiguration) getConfiguration()).getMetadataRepository().registerJndiMapping(url.toExternalForm(),
         cf.getClass().getName(), jndi);

      return result;
   }

   @Override
   protected File getReportDirectory()
   {
      return new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/log/");
   }

   @Override
   protected boolean checkConfigurationIsValid()
   {
      return getConfiguration() != null && getConfiguration() instanceof RAConfiguration;
   }

   @Override
   protected PrintWriter getLogPrintWriter()
   {
      return new PrintWriter(((RAConfiguration) getConfiguration()).getPrintStream());
   }

   @Override
   protected TransactionManager getTransactionManager()
   {
      return ((RAConfiguration) getConfiguration()).getTransactionManager();
   }

   @Override
   protected void registerResourceAdapterToMDR(URL url, File root, Connector cmd, IronJacamar ijmd)
      throws AlreadyExistsException
   {
      ((RAConfiguration) getConfiguration()).getMetadataRepository().
         registerResourceAdapter(url.toExternalForm(), root, cmd, ijmd);
   }
}
