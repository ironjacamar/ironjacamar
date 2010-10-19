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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.util.Injection;
import com.github.fungal.api.util.JMX;
import com.github.fungal.api.util.JarFilter;

/**
 * An abstract resource adapter deployer which contains common functionality
 * for all resource adapter archive based deployers.
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractFungalRADeployer extends AbstractResourceAdapterDeployer
{
   /** The kernel */
   protected Kernel kernel;

   /**
    * Create a new AbstractResourceAdapterDeployer.
    *
    * @param validateClasses boolean to express if this instance will apply validation on classes structure
    * @param log the right log where put messages
    */
   public AbstractFungalRADeployer(boolean validateClasses, Logger log)
   {
      super(validateClasses, log);
      kernel = null;
   }

   /**
    * Get the kernel
    * @return The kernel
    */
   public Kernel getKernel()
   {
      return kernel;
   }

   /**
    * Set the kernel
    * @param kernel The kernel
    */
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   /**
    * Start
    */
   @Override
   public void start()
   {
      super.start();

      if (kernel == null)
         throw new IllegalStateException("Kernel not defined");
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
               {
                  boolean setValue = true;

                  if (cpmd instanceof org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16)
                  {
                     org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16 cpmd16 =
                        (org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16)cpmd;

                     if (cpmd16.getConfigPropertyIgnore() != null && cpmd16.getConfigPropertyIgnore().booleanValue())
                        setValue = false;
                  }

                  if (setValue)
                     injector.inject(cpmd.getConfigPropertyType().getValue(), cpmd.getConfigPropertyName().getValue(),
                                     cpmd.getConfigPropertyValue().getValue(), o);
               }
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
   public String[] bindAdminObject(URL url, String deployment, Object ao) throws Throwable
   {
      JndiStrategy js = ((RAConfiguration) getConfiguration()).getJndiStrategy().clone();

      String[] result = js.bindAdminObjects(deployment, new Object[]{ao});

      ((RAConfiguration) getConfiguration()).getMetadataRepository().registerJndiMapping(url.toExternalForm(),
         ao.getClass().getName(), result[0]);

      return result;
   }

   @Override
   public String[] bindAdminObject(URL url, String deployment, Object ao, String jndi) throws Throwable
   {
      JndiStrategy js = ((RAConfiguration) getConfiguration()).getJndiStrategy().clone();

      String[] result = js.bindAdminObjects(deployment, new Object[]{ao}, new String[]{jndi});

      ((RAConfiguration) getConfiguration()).getMetadataRepository().registerJndiMapping(url.toExternalForm(),
         ao.getClass().getName(), jndi);

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

   /**
    * Register management view of a connector in JMX
    * @param mgtConnector The management view of the connector
    * @param server The MBeanServer instance
    * @return The ObjectName's generated for this connector
    * @exception JMException Thrown in case of an error
    */
   protected List<ObjectName> registerManagementView(org.jboss.jca.core.management.Connector mgtConnector,
                                                     MBeanServer server)
      throws JMException
   {
      if (server == null)
         throw new IllegalArgumentException("MBeanServer is null");

      List<ObjectName> ons = new ArrayList<ObjectName>();

      if (mgtConnector != null)
      {
         String baseName = server.getDefaultDomain() + ":deployment=" + mgtConnector.getUniqueId();

         if (mgtConnector.getResourceAdapter() != null)
         {
            org.jboss.jca.core.management.ResourceAdapter mgtRa = mgtConnector.getResourceAdapter();

            if (mgtRa.getResourceAdapter() != null)
            {
               Set<String> writeable = new HashSet<String>();
               Set<String> excludeAttributes = new HashSet<String>();

               for (org.jboss.jca.core.management.ConfigProperty mgtCp : mgtRa.getConfigProperties())
               {
                  if (mgtCp.isDynamic())
                     writeable.add(mgtCp.getName());

                  if (mgtCp.isConfidential())
                     excludeAttributes.add(mgtCp.getName());
               }

               String raName = baseName + ",type=ResourceAdapter,class=" + 
                  getClassName(mgtRa.getResourceAdapter().getClass().getName());

               DynamicMBean raDMB = JMX.createMBean(mgtRa.getResourceAdapter(), 
                                                    "Resource adapter",
                                                    writeable,
                                                    null,
                                                    excludeAttributes,
                                                    null);
               ObjectName raON = new ObjectName(raName);

               server.registerMBean(raDMB, raON);

               ons.add(raON);
            }
         }

         for (org.jboss.jca.core.management.ManagedConnectionFactory mgtMcf : 
                 mgtConnector.getManagedConnectionFactories())
         {
            if (mgtMcf.getManagedConnectionFactory() != null)
            {
               Set<String> writeable = new HashSet<String>();
               Set<String> excludeAttributes = new HashSet<String>();

               for (org.jboss.jca.core.management.ConfigProperty mgtCp : mgtMcf.getConfigProperties())
               {
                  if (mgtCp.isDynamic())
                     writeable.add(mgtCp.getName());

                  if (mgtCp.isConfidential())
                     excludeAttributes.add(mgtCp.getName());
               }

               String mcfName = baseName + ",type=ManagedConnectionFactory,class=" + 
                  getClassName(mgtMcf.getManagedConnectionFactory().getClass().getName());

               DynamicMBean mcfDMB = JMX.createMBean(mgtMcf.getManagedConnectionFactory(), 
                                                     "Managed connection factory",
                                                     writeable,
                                                     null,
                                                     excludeAttributes,
                                                     null);
               ObjectName mcfON = new ObjectName(mcfName);

               server.registerMBean(mcfDMB, mcfON);
         
               ons.add(mcfON);
            }

            if (mgtMcf.getPoolConfiguration() != null)
            {
               String mcfPCName = baseName + ",type=ManagedConnectionFactory,class=" + 
                  getClassName(mgtMcf.getManagedConnectionFactory().getClass().getName()) +
                  ",subcategory=PoolConfiguration";

               DynamicMBean mcfPCDMB = JMX.createMBean(mgtMcf.getPoolConfiguration(), "Pool configuration");
               ObjectName mcfPCON = new ObjectName(mcfPCName);
            
               server.registerMBean(mcfPCDMB, mcfPCON);
         
               ons.add(mcfPCON);
            }

            if (mgtMcf.getPool() != null)
            {
               String mcfPName = baseName + ",type=ManagedConnectionFactory,class=" + 
                  getClassName(mgtMcf.getManagedConnectionFactory().getClass().getName()) + ",subcategory=Pool";
               
               DynamicMBean mcfPDMB = JMX.createMBean(mgtMcf.getPool(), "Pool");
               ObjectName mcfPON = new ObjectName(mcfPName);
               
               server.registerMBean(mcfPDMB, mcfPON);
               
               ons.add(mcfPON);
            }
         }

         for (org.jboss.jca.core.management.AdminObject mgtAo : mgtConnector.getAdminObjects())
         {
            if (mgtAo.getAdminObject() != null)
            {
               Set<String> writeable = new HashSet<String>();
               Set<String> excludeAttributes = new HashSet<String>();

               for (org.jboss.jca.core.management.ConfigProperty mgtCp : mgtAo.getConfigProperties())
               {
                  if (mgtCp.isDynamic())
                     writeable.add(mgtCp.getName());

                  if (mgtCp.isConfidential())
                     excludeAttributes.add(mgtCp.getName());
               }
               
               String aoName = baseName + ",type=AdminObject,class=" + 
                  getClassName(mgtAo.getAdminObject().getClass().getName());
               
               DynamicMBean aoDMB = JMX.createMBean(mgtAo.getAdminObject(), 
                                                    "Admin object",
                                                    writeable,
                                                    null,
                                                    excludeAttributes,
                                                    null);
               ObjectName aoON = new ObjectName(aoName);
               
               server.registerMBean(aoDMB, aoON);
               
               ons.add(aoON);
            }
         }
      }

      return ons;
   }

   /**
    * Get the class name without package name
    * @param clz The fully qualified class name
    * @return The class name
    */
   private String getClassName(String clz)
   {
      if (clz.indexOf(".") != -1)
      {
         int lastIndex = clz.lastIndexOf(".");
         return clz.substring(lastIndex + 1);
      }

      return clz;
   }
}
