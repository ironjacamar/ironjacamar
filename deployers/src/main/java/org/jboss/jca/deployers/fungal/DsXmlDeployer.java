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

import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.metadata.ds.DsParser;
import org.jboss.jca.core.naming.ExplicitJndiStrategy;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.deployers.common.AbstractDsDeployer;
import org.jboss.jca.deployers.common.CommonDeployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.util.Injection;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The -ds.xml deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class DsXmlDeployer extends AbstractDsDeployer implements Deployer
{
   /** jdbcLocal **/
   private String jdbcLocal;

   /** jdbcXA **/
   private String jdbcXA;

   /** The kernel */
   private Kernel kernel;

   /**
    * Constructor
    */
   public DsXmlDeployer()
   {
      super(Logger.getLogger(DsXmlDeployer.class));

      this.kernel = null;
      this.jdbcLocal = null;
      this.jdbcXA = null;
   }

   /**
    * Set the name for the JDBC Local resource adapter
    * @param value The value
    */
   public void setJDBCLocal(String value)
   {
      jdbcLocal = value;
   }

   /**
    * Get the name for the JDBC Local resource adapter
    * @return The value
    */
   public String getJDBCLocal()
   {
      return jdbcLocal;
   }

   /**
    * Set the name for the JDBC XA resource adapter
    * @param value The value
    */
   public void setJDBCXA(String value)
   {
      jdbcXA = value;
   }

   /**
    * Get the name for the JDBC Xa resource adapter
    * @return The value
    */
   public String getJDBCXA()
   {
      return jdbcXA;
   }

   /**
    * Set the kernel
    * @param value The value
    */
   public void setKernel(Kernel value)
   {
      kernel = value;
   }

   /**
    * Get the kernel
    * @return The handle
    */
   public Kernel getKernel()
   {
      return kernel;
   }

   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   @Override
   public synchronized Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      if (url == null || !(url.toExternalForm().endsWith("-ds.xml")))
         return null;

      log.debug("Deploying: " + url.toExternalForm());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      InputStream is = null;
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            throw new IOException("Deployment " + url.toExternalForm() + " doesnt exists");

         is = new FileInputStream(f);
         DsParser dsParser = new DsParser();

         DataSources dataSources = dsParser.parse(is);

         String deploymentName = f.getName();

         Set<String> raDeployments = mdr.getResourceAdapters();
         String uniqueJdbcLocalId = null;
         String uniqueJdbcXAId = null;

         for (String s : raDeployments)
         {
            if (s.endsWith(jdbcLocal))
            {
               URL urlJdbcLocal = new URL(s);
               uniqueJdbcLocalId = urlJdbcLocal.toExternalForm();
            }
            else if (s.endsWith(jdbcXA))
            {
               URL urlJdbcXA = new URL(s);
               uniqueJdbcXAId = urlJdbcXA.toExternalForm();
            }
         }

         CommonDeployment c = createObjectsAndInjectValue(url, deploymentName,
                                                          uniqueJdbcLocalId, uniqueJdbcXAId,
                                                          dataSources, parent);

         return new DsXmlDeployment(c.getURL(), c.getDeploymentName(), c.getCfs(), c.getCfJndiNames(), c.getCl());
      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }

         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }

   /**
    * Initialize and inject configuration properties
    * @param className The fully qualified class name
    * @param configs The configuration properties
    * @param cl The class loader
    * @return The object
    * @throws org.jboss.jca.deployers.common.DeployException Thrown if the object cant be initialized
    */
   @Override
   public Object initAndInject(String className, List<? extends ConfigProperty> configs, ClassLoader cl)
      throws org.jboss.jca.deployers.common.DeployException
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
         throw new org.jboss.jca.deployers.common.DeployException("Deployment " + className + " failed", t);
      }
   }

   /**
    * Bind connection factory into JNDI
    * @param deployment The deployment name
    * @param jndi The JNDI name
    * @param cf The connection factory
    * @return The JNDI names bound
    * @throws Throwable thrown if an error occurs
    */
   @Override
   protected String[] bindConnectionFactory(String deployment, String jndi, Object cf) throws Throwable
   {
      JndiStrategy js = new ExplicitJndiStrategy();

      String[] result = js.bindConnectionFactories(deployment, new Object[]{cf}, new String[]{jndi});
      
      log.infof("Bound data source at: %s", jndi);

      return result;
   }

   /**
    * Start
    */
   public void start()
   {
      if (transactionManager == null)
         throw new IllegalStateException("TransactionManager not defined");

      if (mdr == null)
         throw new IllegalStateException("MetadataRepository not defined");

      if (kernel == null)
         throw new IllegalStateException("Kernel not defined");

      if (jdbcLocal == null)
         throw new IllegalStateException("JDBCLocal not defined");

      if (jdbcXA == null)
         throw new IllegalStateException("JDBCXA not defined");
   }

   @Override
   protected ClassLoader getDeploymentClassLoader(String uniqueId)
   {
      try
      {
         URL urlJdbc = new URL(uniqueId);
         return kernel.getDeployment(urlJdbc).getClassLoader();
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t.getMessage(), t);
      }
   }
}
