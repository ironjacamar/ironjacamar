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
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
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
import java.util.Locale;
import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.TransactionManager;

import org.jboss.security.SubjectFactory;

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
    */
   public AbstractFungalRADeployer(boolean validateClasses)
   {
      super(validateClasses);
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
                  {
                     try
                     {
                        injector.inject(o,
                                        cpmd.getConfigPropertyName().getValue(),
                                        cpmd.getConfigPropertyValue().getValue(),
                                        cpmd.getConfigPropertyType().getValue());
                     }
                     catch (NoSuchMethodException nsme)
                     {
                        String newPropertyType = convertType(cpmd.getConfigPropertyType().getValue());

                        if (newPropertyType != null)
                        {
                           injector.inject(o,
                                           cpmd.getConfigPropertyName().getValue(),
                                           cpmd.getConfigPropertyValue().getValue(),
                                           newPropertyType);

                        }
                        else
                        {
                           throw nsme;
                        }
                     }
                  }
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
    * Convert type if possible
    * @param old The old type
    * @return The new type; otherwise <code>null</code>
    */
   private String convertType(String old)
   {
      if (Boolean.class.getName().equals(old))
      {
         return boolean.class.getName();
      }
      else if (boolean.class.getName().equals(old))
      {
         return Boolean.class.getName();
      }
      else if (Byte.class.getName().equals(old))
      {
         return byte.class.getName();
      }
      else if (byte.class.getName().equals(old))
      {
         return Byte.class.getName();
      }
      else if (Short.class.getName().equals(old))
      {
         return short.class.getName();
      }
      else if (short.class.getName().equals(old))
      {
         return Short.class.getName();
      }
      else if (Integer.class.getName().equals(old))
      {
         return int.class.getName();
      }
      else if (int.class.getName().equals(old))
      {
         return Integer.class.getName();
      }
      else if (Long.class.getName().equals(old))
      {
         return long.class.getName();
      }
      else if (long.class.getName().equals(old))
      {
         return Long.class.getName();
      }
      else if (Float.class.getName().equals(old))
      {
         return float.class.getName();
      }
      else if (float.class.getName().equals(old))
      {
         return Float.class.getName();
      }
      else if (Double.class.getName().equals(old))
      {
         return double.class.getName();
      }
      else if (double.class.getName().equals(old))
      {
         return Double.class.getName();
      }
      else if (Character.class.getName().equals(old))
      {
         return char.class.getName();
      }
      else if (char.class.getName().equals(old))
      {
         return Character.class.getName();
      }

      return null;
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

      log.infof("Bound connection factory under: %s", result[0]);

      return result;
   }

   @Override
   public String[] bindConnectionFactory(URL url, String deployment, Object cf, String jndi) throws Throwable
   {
      JndiStrategy js = ((RAConfiguration) getConfiguration()).getJndiStrategy().clone();

      String[] result = js.bindConnectionFactories(deployment, new Object[]{cf}, new String[]{jndi});

      ((RAConfiguration) getConfiguration()).getMetadataRepository().registerJndiMapping(url.toExternalForm(),
         cf.getClass().getName(), jndi);

      log.infof("Bound connection factory under: %s", jndi);

      return result;
   }

   @Override
   public String[] bindAdminObject(URL url, String deployment, Object ao) throws Throwable
   {
      JndiStrategy js = ((RAConfiguration) getConfiguration()).getJndiStrategy().clone();

      String[] result = js.bindAdminObjects(deployment, new Object[]{ao});

      ((RAConfiguration) getConfiguration()).getMetadataRepository().registerJndiMapping(url.toExternalForm(),
         ao.getClass().getName(), result[0]);

      log.infof("Bound admin object under: %s", result[0]);

      return result;
   }

   @Override
   public String[] bindAdminObject(URL url, String deployment, Object ao, String jndi) throws Throwable
   {
      JndiStrategy js = ((RAConfiguration) getConfiguration()).getJndiStrategy().clone();

      String[] result = js.bindAdminObjects(deployment, new Object[]{ao}, new String[]{jndi});

      ((RAConfiguration) getConfiguration()).getMetadataRepository().registerJndiMapping(url.toExternalForm(),
         ao.getClass().getName(), jndi);

      log.infof("Bound admin object under: %s", jndi);

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
      return ((RAConfiguration) getConfiguration()).getTransactionIntegration().getTransactionManager();
   }

   @Override
   protected TransactionIntegration getTransactionIntegration()
   {
      return ((RAConfiguration) getConfiguration()).getTransactionIntegration();
   }

   @Override
   protected void registerResourceAdapterToMDR(URL url, File root, Connector cmd, IronJacamar ijmd)
      throws org.jboss.jca.core.spi.mdr.AlreadyExistsException
   {
      ((RAConfiguration) getConfiguration()).getMetadataRepository().
         registerResourceAdapter(url.toExternalForm(), root, cmd, ijmd);
   }

   @Override
   protected String registerResourceAdapterToResourceAdapterRepository(ResourceAdapter instance)
   {
      return ((RAConfiguration) getConfiguration()).getResourceAdapterRepository().
         registerResourceAdapter(instance);
   }

   /**
    * Register management view of a connector in JMX
    * @param mgtConnector The management view of the connector
    * @param server The MBeanServer instance
    * @param domain The management domain
    * @return The ObjectName's generated for this connector
    * @exception JMException Thrown in case of an error
    */
   protected List<ObjectName> registerManagementView(org.jboss.jca.core.api.management.Connector mgtConnector,
                                                     MBeanServer server, String domain)
      throws JMException
   {
      List<ObjectName> ons = null;

      if (mgtConnector != null)
      {
         if (server != null)
         {
            ons = new ArrayList<ObjectName>();
            String baseName = domain + ":deployment=" + mgtConnector.getUniqueId();

            if (mgtConnector.getResourceAdapter() != null)
            {
               org.jboss.jca.core.api.management.ResourceAdapter mgtRa = mgtConnector.getResourceAdapter();

               if (mgtRa.getResourceAdapter() != null)
               {
                  Set<String> writeable = new HashSet<String>();
                  Set<String> excludeAttributes = new HashSet<String>();

                  for (org.jboss.jca.core.api.management.ConfigProperty mgtCp : mgtRa.getConfigProperties())
                  {
                     String mgtCpName = mgtCp.getName().substring(0, 1).toUpperCase(Locale.US);
                     if (mgtCp.getName().length() > 1)
                        mgtCpName += mgtCp.getName().substring(1);

                     if (mgtCp.isDynamic())
                        writeable.add(mgtCpName);

                     if (mgtCp.isConfidential())
                        excludeAttributes.add(mgtCpName);
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

                  if (mgtRa.getStatistics() != null)
                  {
                     String raSName = baseName + ",type=ResourceAdapterStatistics,class=" +
                        getClassName(mgtRa.getResourceAdapter().getClass().getName());

                     Set<String> writeStatAttributes = new HashSet<String>();
                     writeStatAttributes.add("Enabled");
                     Set<String> excludeStatAttributes = new HashSet<String>();
                     excludeStatAttributes.add("Names");
                     Set<String> excludeStatOperations = new HashSet<String>();
                     excludeStatOperations.add("delta(.)*");
                     
                     DynamicMBean raSDMB = JMX.createMBean(mgtRa.getStatistics(),
                                                           "Resource adapter statistics",
                                                           writeStatAttributes, null,
                                                           excludeStatAttributes, excludeStatOperations);
                     ObjectName raSON = new ObjectName(raSName);
                     
                     server.registerMBean(raSDMB, raSON);
                     
                     ons.add(raSON);
                  }
               }
            }

            for (org.jboss.jca.core.api.management.ConnectionFactory mgtCf :
                    mgtConnector.getConnectionFactories())
            {
               String jndiName = cleanJndiName(mgtCf.getJndiName());
               
               if (mgtCf.getManagedConnectionFactory() != null)
               {
                  org.jboss.jca.core.api.management.ManagedConnectionFactory mgtMcf = 
                     mgtCf.getManagedConnectionFactory();
                  Set<String> writeable = new HashSet<String>();
                  Set<String> excludeAttributes = new HashSet<String>();

                  for (org.jboss.jca.core.api.management.ConfigProperty mgtCp : mgtMcf.getConfigProperties())
                  {
                     String mgtCpName = mgtCp.getName().substring(0, 1).toUpperCase(Locale.US);
                     if (mgtCp.getName().length() > 1)
                        mgtCpName += mgtCp.getName().substring(1);

                     if (mgtCp.isDynamic())
                        writeable.add(mgtCpName);

                     if (mgtCp.isConfidential())
                        excludeAttributes.add(mgtCpName);
                  }

                  String mcfName = baseName + ",jndi=" + jndiName +
                     ",type=ManagedConnectionFactory,class=" +
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

                  if (mgtMcf.getStatistics() != null)
                  {
                     String mcfSName = baseName + ",jndi=" + jndiName +
                        ",type=ManagedConnectionFactoryStatistics,class=" +
                        getClassName(mgtMcf.getManagedConnectionFactory().getClass().getName());

                     Set<String> writeStatAttributes = new HashSet<String>();
                     writeStatAttributes.add("Enabled");
                     Set<String> excludeStatAttributes = new HashSet<String>();
                     excludeStatAttributes.add("Names");
                     Set<String> excludeStatOperations = new HashSet<String>();
                     excludeStatOperations.add("delta(.)*");
                     
                     DynamicMBean mcfSDMB = JMX.createMBean(mgtMcf.getStatistics(),
                                                            "Managed connection factory statistics",
                                                            writeStatAttributes, null,
                                                            excludeStatAttributes, excludeStatOperations);
                     ObjectName mcfSON = new ObjectName(mcfSName);
                     
                     server.registerMBean(mcfSDMB, mcfSON);

                     ons.add(mcfSON);
                  }
               }

               if (mgtCf.getPoolConfiguration() != null)
               {
                  String mcfPCName = baseName + ",jndi=" + jndiName +
                     ",type=ConnectionFactory,class=" +
                     getClassName(mgtCf.getConnectionFactory().getClass().getName()) +
                     ",subcategory=PoolConfiguration";
                  
                  DynamicMBean mcfPCDMB = JMX.createMBean(mgtCf.getPoolConfiguration(), "Pool configuration");
                  ObjectName mcfPCON = new ObjectName(mcfPCName);
                  
                  server.registerMBean(mcfPCDMB, mcfPCON);
                  
                  ons.add(mcfPCON);
               }

               if (mgtCf.getPool() != null)
               {
                  String cfPName = baseName + ",jndi=" + jndiName + ",type=ConnectionFactory,class=" +
                     getClassName(mgtCf.getConnectionFactory().getClass().getName()) + ",subcategory=Pool";
                  
                  DynamicMBean cfPDMB = JMX.createMBean(mgtCf.getPool(), "Pool");
                  ObjectName cfPON = new ObjectName(cfPName);
                  
                  server.registerMBean(cfPDMB, cfPON);
                  
                  ons.add(cfPON);
                  
                  if (mgtCf.getPool().getStatistics() != null)
                  {
                     String cfPSName = baseName + ",jndi=" + jndiName + ",type=ConnectionFactory,class=" +
                        getClassName(mgtCf.getConnectionFactory().getClass().getName()) + ",subcategory=PoolStatistics";

                     Set<String> writeStatAttributes = new HashSet<String>();
                     writeStatAttributes.add("Enabled");
                     Set<String> excludeStatAttributes = new HashSet<String>();
                     excludeStatAttributes.add("Names");
                     Set<String> excludeStatOperations = new HashSet<String>();
                     excludeStatOperations.add("delta(.)*");

                     DynamicMBean cfPSDMB = JMX.createMBean(mgtCf.getPool().getStatistics(), "Pool statistics",
                                                            writeStatAttributes, null,
                                                            excludeStatAttributes, excludeStatOperations);
                     ObjectName cfPSON = new ObjectName(cfPSName);
                  
                     server.registerMBean(cfPSDMB, cfPSON);
                     
                     ons.add(cfPSON);
                  }
               }
            }

            for (org.jboss.jca.core.api.management.AdminObject mgtAo : mgtConnector.getAdminObjects())
            {
               if (mgtAo.getAdminObject() != null)
               {
                  Set<String> writeable = new HashSet<String>();
                  Set<String> excludeAttributes = new HashSet<String>();

                  String jndiName = cleanJndiName(mgtAo.getJndiName());

                  for (org.jboss.jca.core.api.management.ConfigProperty mgtCp : mgtAo.getConfigProperties())
                  {
                     String mgtCpName = mgtCp.getName().substring(0, 1).toUpperCase(Locale.US);
                     if (mgtCp.getName().length() > 1)
                        mgtCpName += mgtCp.getName().substring(1);

                     if (mgtCp.isDynamic())
                        writeable.add(mgtCpName);

                     if (mgtCp.isConfidential())
                        excludeAttributes.add(mgtCpName);
                  }

                  String aoName = baseName + ",jndi=" + jndiName +
                     ",type=AdminObject,class=" + getClassName(mgtAo.getAdminObject().getClass().getName());

                  DynamicMBean aoDMB = JMX.createMBean(mgtAo.getAdminObject(),
                                                       "Admin object",
                                                       writeable,
                                                       null,
                                                       excludeAttributes,
                                                       null);
                  ObjectName aoON = new ObjectName(aoName);

                  server.registerMBean(aoDMB, aoON);
                  
                  ons.add(aoON);
                  
                  if (mgtAo.getStatistics() != null)
                  {
                     String aoSName = baseName +  ",jndi=" + jndiName +
                        ",type=AdminObjectStatistics,class=" +
                        getClassName(mgtAo.getAdminObject().getClass().getName());

                     Set<String> writeStatAttributes = new HashSet<String>();
                     writeStatAttributes.add("Enabled");
                     Set<String> excludeStatAttributes = new HashSet<String>();
                     excludeStatAttributes.add("Names");
                     Set<String> excludeStatOperations = new HashSet<String>();
                     excludeStatOperations.add("delta(.)*");

                     DynamicMBean aoSDMB = JMX.createMBean(mgtAo.getStatistics(),
                                                           "Admin object statistics",
                                                           writeStatAttributes, null,
                                                           excludeStatAttributes, excludeStatOperations);
                     ObjectName aoSON = new ObjectName(aoSName);
                     
                     server.registerMBean(aoSDMB, aoSON);
                     
                     ons.add(aoSON);
                  }
               }
            }
         }

         log.debugf("Adding management connector: %s", mgtConnector);
         
         ((RAConfiguration)getConfiguration()).getManagementRepository().getConnectors().add(mgtConnector);
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

   /**
    * Clean JNDI name for management
    * @param jndi The JNDI name
    * @return The value
    */
   private String cleanJndiName(String jndi)
   {
      jndi = jndi.replace(':', '!');

      return jndi;
   }

   @Override
   protected SubjectFactory getSubjectFactory(String securityDomain) throws DeployException
   {
      log.tracef("getSubjectFactory(%s)", securityDomain);

      if (securityDomain == null || securityDomain.trim().equals(""))
         return null;

      try
      {
         return kernel.getBean(securityDomain, SubjectFactory.class);
      }
      catch (Throwable t)
      {
         throw new DeployException("Error during loookup of security domain: " + securityDomain, t);
      }
   }

   @Override
   protected CachedConnectionManager getCachedConnectionManager()
   {
      return ((RAConfiguration) getConfiguration()).getCachedConnectionManager();
   }
}
