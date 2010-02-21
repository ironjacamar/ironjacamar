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

import org.jboss.jca.common.api.ConnectionFactoryBuilder;
import org.jboss.jca.common.util.LocalConnectionFactoryBuilder;
import org.jboss.jca.core.api.CloneableBootstrapContext;
import org.jboss.jca.core.connectionmanager.notx.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.PoolParams;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;
import org.jboss.jca.deployers.common.Annotations;
import org.jboss.jca.deployers.common.Metadata;
import org.jboss.jca.deployers.common.validator.Failure;
import org.jboss.jca.deployers.common.validator.FailureHelper;
import org.jboss.jca.deployers.common.validator.Key;
import org.jboss.jca.deployers.common.validator.Severity;
import org.jboss.jca.deployers.common.validator.ValidateObject;
import org.jboss.jca.deployers.common.validator.Validator;
import org.jboss.jca.fungal.deployers.CloneableDeployer;
import org.jboss.jca.fungal.deployers.DeployException;
import org.jboss.jca.fungal.deployers.Deployer;
import org.jboss.jca.fungal.deployers.Deployment;
import org.jboss.jca.fungal.util.FileUtil;
import org.jboss.jca.fungal.util.Injection;
import org.jboss.jca.fungal.util.JarFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.Referenceable;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;

import org.jboss.logging.Logger;

import org.jboss.metadata.rar.jboss.BvGroupMetaData;
import org.jboss.metadata.rar.jboss.JBossRA20Base;
import org.jboss.metadata.rar.jboss.JBossRAMetaData;
import org.jboss.metadata.rar.spec.AdminObjectMetaData;
import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;
import org.jboss.metadata.rar.spec.ConnectionDefinitionMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.MessageListenerMetaData;
import org.jboss.util.naming.Util;

/**
 * The RA deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 */
public final class RADeployer implements CloneableDeployer
{
   private static Logger log = Logger.getLogger(RADeployer.class);
   private static boolean trace = log.isTraceEnabled();

   /** JNDI prefix */
   private static final String JNDI_PREFIX = "java:/eis/";
   
   /** Preform bean validation */
   private static AtomicBoolean beanValidation = new AtomicBoolean(true);

   /** Preform archive validation */
   private static AtomicBoolean archiveValidation = new AtomicBoolean(true);

   /** Archive validation: Fail on Warn */
   private static AtomicBoolean archiveValidationFailOnWarn = new AtomicBoolean(false);

   /** Archive validation: Fail on Error */
   private static AtomicBoolean archiveValidationFailOnError = new AtomicBoolean(true);

   /** Print stream */
   private static PrintStream printStream = null;

   /** Default bootstrap context */
   private static CloneableBootstrapContext defaultBootstrapContext = null;

   /**
    * Constructor
    */
   public RADeployer()
   {
   }

   /**
    * Set if bean validation should be performed
    * @param value The value
    */
   public void setBeanValidation(boolean value)
   {
      beanValidation.set(value);
   }
   
   /**
    * Should bean validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getBeanValidation()
   {
      return beanValidation.get();
   }
   
   /**
    * Set if archive validation should be performed
    * @param value The value
    */
   public void setArchiveValidation(boolean value)
   {
      archiveValidation.set(value);
   }
   
   /**
    * Should archive validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getArchiveValidation()
   {
      return archiveValidation.get();
   }
   
   /**
    * Set if a failed warning archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnWarn(boolean value)
   {
      archiveValidationFailOnWarn.set(value);
   }
   
   /**
    * Does a failed archive validation warning report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnWarn()
   {
      return archiveValidationFailOnWarn.get();
   }
   
   /**
    * Set if a failed error archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnError(boolean value)
   {
      archiveValidationFailOnError.set(value);
   }
   
   /**
    * Does a failed archive validation error report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnError()
   {
      return archiveValidationFailOnError.get();
   }
   
   /**
    * Set the print stream
    * @param value The value
    */
   public synchronized void setPrintStream(PrintStream value)
   {
      printStream = value;
   }
   
   /**
    * Get the print stream
    * @return The handle
    */
   public synchronized PrintStream getPrintStream()
   {
      return printStream;
   }
   
   /**
    * Set the default bootstrap context
    * @param value The value
    */
   public synchronized void setDefaultBootstrapContext(CloneableBootstrapContext value)
   {
      defaultBootstrapContext = value;
   }
   
   /**
    * Get the default bootstrap context
    * @return The handle
    */
   public synchronized CloneableBootstrapContext getDefaultBootstrapContext()
   {
      return defaultBootstrapContext;
   }
   
   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   public Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      if (url == null || !(url.toExternalForm().endsWith(".rar") || url.toExternalForm().endsWith(".rar/")))
         return null;

      log.debug("Deploying: " + url.toExternalForm());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         File f = new File(url.toURI());
      
         if (!f.exists())
            throw new IOException("Archive " + url.toExternalForm() + " doesnt exists");

         File root = null;

         if (f.isFile())
         {
            FileUtil fileUtil = new FileUtil();
            File destination = new File(SecurityActions.getSystemProperty("jboss.jca.home"), "/tmp/");
            root = fileUtil.extract(f, destination);
         }
         else
         {
            root = f;
         }
      
         // Create classloader
         URL[] urls = getUrls(root);
         URLClassLoader cl = SecurityActions.createURLCLassLoader(urls, parent);
         SecurityActions.setThreadContextClassLoader(cl);

         // Parse metadata
         Metadata metadataHandler = new Metadata();
         ConnectorMetaData cmd = metadataHandler.getStandardMetaData(root);
         JBossRAMetaData jrmd = metadataHandler.getJBossMetaData(root);

         // Annotation scanning
         Annotations annotator = new Annotations();
         cmd = annotator.scan(cmd, cl.getURLs(), cl);
         
         // Validate metadata
         metadataHandler.validate(cmd);
         
         // Merge metadata
         cmd = metadataHandler.merge(cmd, jrmd);

         // Notify regarding license terms
         if (cmd != null && cmd.getLicense() != null && cmd.getLicense().getRequired())
            log.info("Required license terms for " + url.toExternalForm());

         ResourceAdapter resourceAdapter = null;
         List<ValidateObject> archiveValidationObjects = new ArrayList<ValidateObject>();
         List<Object> beanValidationObjects = new ArrayList<Object>();

         List<String> jndiNames = null;

         // Create objects and inject values
         if (cmd != null)
         {
            // ResourceAdapter
            if (cmd.getRa() != null && cmd.getRa().getRaClass() != null)
            {
               resourceAdapter =
                  (ResourceAdapter)initAndInject(cmd.getRa().getRaClass(), cmd.getRa().getConfigProperty(), cl);

               if (trace)
               {
                  log.trace("ResourceAdapter: " + resourceAdapter.getClass().getName());
                  log.trace("ResourceAdapter defined in classloader: " + resourceAdapter.getClass().getClassLoader());
               }

               archiveValidationObjects.add(new ValidateObject(Key.RESOURCE_ADAPTER, 
                                                               resourceAdapter, 
                                                               cmd.getRa().getConfigProperty()));
               beanValidationObjects.add(resourceAdapter);
            }
            
            // ManagedConnectionFactory
            if (cmd.getRa() != null &&
                cmd.getRa().getOutboundRa() != null && 
                cmd.getRa().getOutboundRa().getConDefs() != null)
            {
               List<ConnectionDefinitionMetaData> cdMetas = cmd.getRa().getOutboundRa().getConDefs();
               if (cdMetas.size() > 0)
               {
                  for (ConnectionDefinitionMetaData cdMeta : cdMetas)
                  {
                     if (cdMeta.getManagedConnectionFactoryClass() != null)
                     {
                        ManagedConnectionFactory mcf =
                           (ManagedConnectionFactory)initAndInject(cdMeta.getManagedConnectionFactoryClass(), 
                                                                   cdMeta.getConfigProps(), cl);

                        if (trace)
                        {
                           log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                           log.trace("ManagedConnectionFactory defined in classloader: " + 
                                     mcf.getClass().getClassLoader());
                        }

                        mcf.setLogWriter(new PrintWriter(printStream));

                        archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY,
                                                                        mcf,
                                                                        cdMeta.getConfigProps()));
                        beanValidationObjects.add(mcf);
                        associateResourceAdapter(resourceAdapter, mcf);

                        // TODO: add proper configuration and use it (support TxConnectionManager as well)
                        NoTxConnectionManager noTxCm = new NoTxConnectionManager();
                        PoolParams poolParams = new PoolParams();
                        OnePool onePool = new OnePool(mcf, poolParams, true);
                        onePool.setConnectionListenerFactory(noTxCm);
                        noTxCm.setPoolingStrategy(onePool);
                        
                        // ConnectionFactory
                        Object cf = mcf.createConnectionFactory(noTxCm);

                        if (trace)
                        {
                           log.trace("ConnectionFactory: " + cf.getClass().getName());
                           log.trace("ConnectionFactory defined in classloader: " + 
                                     cf.getClass().getClassLoader());
                        }

                        archiveValidationObjects.add(new ValidateObject(Key.CONNECTION_FACTORY, cf));

                        if (cf instanceof Serializable && cf instanceof Referenceable)
                        {
                           if (cdMetas.size() == 1)
                           {
                              if (jndiNames == null)
                                 jndiNames = new ArrayList<String>(1);

                              String jndiName = f.getName().substring(0, f.getName().indexOf(".rar"));

                              bindConnectionFactory(jndiName, (Serializable)cf, mcf);
                              jndiNames.add(JNDI_PREFIX + jndiName);
                           }
                           else
                           {
                              log.warn("NYI: There are multiple connection factories for: " + f.getName());
                           }
                        }
                     }
                  }
               }
            }

            // ActivationSpec
            if (cmd.getRa() != null &&
                cmd.getRa().getInboundRa() != null &&
                cmd.getRa().getInboundRa().getMessageAdapter() != null &&
                cmd.getRa().getInboundRa().getMessageAdapter().getMessageListeners() != null)
            {
               List<MessageListenerMetaData> mlMetas = cmd.getRa().getInboundRa().
                  getMessageAdapter().getMessageListeners();
               if (mlMetas.size() > 0)
               {
                  for (MessageListenerMetaData mlMeta : mlMetas)
                  {
                     if (mlMeta.getActivationSpecType() != null && mlMeta.getActivationSpecType().getAsClass() != null)
                     {
                        List<ConfigPropertyMetaData> cpm = mlMeta.getActivationSpecType().getConfigProps();

                        Object o = initAndInject(mlMeta.getActivationSpecType().getAsClass(), cpm, cl);

                        if (trace)
                        {
                           log.trace("ActivationSpec: " + o.getClass().getName());
                           log.trace("ActivationSpec defined in classloader: " + 
                                     o.getClass().getClassLoader());
                        }

                        archiveValidationObjects.add(new ValidateObject(Key.ACTIVATION_SPEC, o, cpm));
                        beanValidationObjects.add(o);
                        associateResourceAdapter(resourceAdapter, o);
                     }
                  }
               }
            }

            // AdminObject
            if (cmd.getRa() != null &&
                cmd.getRa().getAdminObjects() != null)
            {
               List<AdminObjectMetaData> aoMetas = cmd.getRa().getAdminObjects();
               if (aoMetas.size() > 0)
               {
                  for (AdminObjectMetaData aoMeta : aoMetas)
                  {
                     if (aoMeta.getAdminObjectImplementationClass() != null)
                     {
                        Object o = initAndInject(aoMeta.getAdminObjectImplementationClass(), 
                                                 aoMeta.getConfigProps(), cl);

                        if (trace)
                        {
                           log.trace("AdminObject: " + o.getClass().getName());
                           log.trace("AdminObject defined in classloader: " + 
                                     o.getClass().getClassLoader());
                        }

                        archiveValidationObjects.add(
                           new ValidateObject(Key.ADMIN_OBJECT, o, aoMeta.getConfigProps()));
                        beanValidationObjects.add(o);
                     }
                  }
               }
            }
         }

         // Archive validation
         if (getArchiveValidation())
         {
            Validator validator = new Validator();
            List<Failure> failures = validator.validate(archiveValidationObjects.toArray(
               new ValidateObject[archiveValidationObjects.size()]));

            if (failures != null && failures.size() > 0)
            {
               FailureHelper fh = new FailureHelper(failures);
               File reportDirectory = new File(SecurityActions.getSystemProperty("jboss.jca.home"), "/log/");

               boolean failureWarn = false;
               boolean failureError = false;

               for (Failure failure : failures)
               {
                  if (failure.getSeverity() == Severity.WARNING)
                  {
                     failureWarn = true;
                  }
                  else
                  {
                     failureError = true;
                  }
               }

               String errorText = "";
               if (reportDirectory.exists())
               {
                  String reportName = url.getFile();
                  int lastIndex = reportName.lastIndexOf(File.separator);
                  if (lastIndex != -1)
                     reportName = reportName.substring(lastIndex + 1);
                  reportName += ".log";

                  File report = new File(reportDirectory, reportName);
                  FileWriter fw = null;
                  try
                  {
                     fw = new FileWriter(report);
                     BufferedWriter bw = new BufferedWriter(fw, 8192);
                     bw.write(fh.asText(validator.getResourceBundle()));
                     bw.flush();

                     errorText = "Validation failures - see: " + report.getAbsolutePath();
                  }
                  catch (IOException ioe)
                  {
                     log.warn(ioe.getMessage(), ioe);
                  }
                  finally
                  {
                     if (fw != null)
                     {
                        try
                        {
                           fw.close();
                        }
                        catch (IOException ignore)
                        {
                           // Ignore
                        }
                     }
                  }
               }
               else
               {
                  errorText = fh.asText(validator.getResourceBundle());
               }

               if ((getArchiveValidationFailOnWarn() && failureWarn) ||
                   (getArchiveValidationFailOnError() && failureError))
                  throw new DeployException(errorText);
            }
         }

         // Bean validation
         if (getBeanValidation())
         {
            JBossRA20Base jrmd20 = null;
            List<Class> groupsClasses = null;
            if (jrmd instanceof JBossRA20Base)
            {
               jrmd20 = (JBossRA20Base)jrmd;
            }
            if (jrmd20 != null && jrmd20.getBvGroupsList() != null && jrmd20.getBvGroupsList().size() > 0)
            {
               BvGroupMetaData bvGroups = jrmd20.getBvGroupsList().get(0);
               groupsClasses = new ArrayList<Class>();
               for (String group : bvGroups.getBvGroups())
               {
                  groupsClasses.add(Class.forName(group, true, cl));
               }
            }
            
            if (beanValidationObjects.size() > 0)
            {
               BeanValidation beanValidator = new BeanValidation();
               for (Object o : beanValidationObjects)
               {
                  beanValidator.validate(o, groupsClasses);
               }
            }
         }
         
         // Activate deployment
         if (resourceAdapter != null)
            startContext(resourceAdapter);

         log.info("Deployed: " + url.toExternalForm());

         return new RADeployment(url, jndiNames, cl);
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
         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }

   private ConnectionFactoryBuilder getConnectionFactoryBuilder()
   {
      return new LocalConnectionFactoryBuilder();
   }

   /**
    * Start the resource adapter
    * @param resourceAdapter The resource adapter
    * @throws DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked") 
   private void startContext(ResourceAdapter resourceAdapter) throws DeployException
   {
      try 
      {
         Class clz = resourceAdapter.getClass();
         Method start = clz.getMethod("start", new Class[] {BootstrapContext.class});

         CloneableBootstrapContext cbc = defaultBootstrapContext.clone();

         start.invoke(resourceAdapter, new Object[] {cbc});
      }
      catch (InvocationTargetException ite)
      {
         throw new DeployException("Unable to start " + 
                                   resourceAdapter.getClass().getName(), ite.getTargetException());
      }
      catch (Throwable t)
      {
         throw new DeployException("Unable to start " + resourceAdapter.getClass().getName(), t);
      }
   }

   /**
    * Associate resource adapter with ojects if they implement ResourceAdapterAssociation
    * @param resourceAdapter The resource adapter
    * @param object The of possible association object
    * @throws DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked") 
   private void associateResourceAdapter(ResourceAdapter resourceAdapter, Object object)
      throws DeployException
   {
      if (resourceAdapter != null && object != null)
      {
         if (object instanceof ResourceAdapterAssociation)
         {
            try 
            {
               Class clz = object.getClass();

               Method setResourceAdapter = clz.getMethod("setResourceAdapter",
                                                         new Class[] {ResourceAdapter.class});

               setResourceAdapter.invoke(object, new Object[] {resourceAdapter});
            }
            catch (Throwable t)
            {
               throw new DeployException("Unable to associate " + object.getClass().getName(), t);
            }
         }
      }
   }

   /**
    * Initialize and inject configuration properties
    * @param className The fully qualified class name
    * @param configs The configuration properties
    * @param cl The class loader
    * @return The object
    * @throws DeployException Thrown if the object cant be initialized
    */
   private Object initAndInject(String className, 
                                List<ConfigPropertyMetaData> configs,
                                ClassLoader cl)
      throws DeployException
   {
      try 
      {
         Class clz = Class.forName(className, true, cl);
         Object o = clz.newInstance();
         
         if (configs != null)
         {
            Injection injector = new Injection();
            for (ConfigPropertyMetaData cpmd : configs)
            {
               injector.inject(cpmd.getType(), cpmd.getName(), cpmd.getValue(), o);
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
   private URL[] getUrls(File directory) throws MalformedURLException, IOException
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

   /**
    * Bind connection factory into JNDI
    * @param name The JNDI name
    * @param cf The connection factory
    * @param mcf The managed connection factory
    * @exception Exception thrown if an error occurs
    */
   private void bindConnectionFactory(String name, Serializable cf, ManagedConnectionFactory mcf) throws Exception
   {
      ConnectionFactoryBuilder cfb = getConnectionFactoryBuilder();
      cfb.setManagedConnectionFactory(mcf).setConnectionFactory(cf).setName(name);
      Context context = new InitialContext();
      try
      {

         Referenceable referenceable = (Referenceable)cf;
         referenceable.setReference(cfb.build());

         Util.bind(context, JNDI_PREFIX + name, cf);

      }
      finally
      {
         context.close();     // release connection
      }
   }


   /**
    * Clone
    * @return The copy of the object
    * @exception CloneNotSupportedException Thrown if a copy can't be created
    */
   public Deployer clone() throws CloneNotSupportedException
   {
      return new RADeployer();
   }
}
