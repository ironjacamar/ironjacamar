/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.logging.Logger;

import org.jboss.metadata.rar.jboss.BvGroupMetaData;
import org.jboss.metadata.rar.jboss.JBossRA20Base;
import org.jboss.metadata.rar.jboss.JBossRAMetaData;
import org.jboss.metadata.rar.spec.AdminObjectMetaData;
import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;
import org.jboss.metadata.rar.spec.ConnectionDefinitionMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.JCA16DTDMetaData;
import org.jboss.metadata.rar.spec.JCA16DefaultNSMetaData;
import org.jboss.metadata.rar.spec.JCA16MetaData;
import org.jboss.metadata.rar.spec.MessageListenerMetaData;
import org.jboss.papaki.AnnotationRepository;
import org.jboss.papaki.AnnotationScanner;
import org.jboss.papaki.AnnotationScannerFactory;

/**
 * The RA deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 */
public final class RADeployer implements CloneableDeployer
{
   private static Logger log = Logger.getLogger(RADeployer.class);
   private static boolean trace = log.isTraceEnabled();
   
   /** Preform bean validation */
   private static AtomicBoolean beanValidation = new AtomicBoolean(true);

   /** Preform archive validation */
   private static AtomicBoolean archiveValidation = new AtomicBoolean(true);

   /** Archive validation: Fail on Warn */
   private static AtomicBoolean archiveValidationFailOnWarn = new AtomicBoolean(false);

   /** Archive validation: Fail on Error */
   private static AtomicBoolean archiveValidationFailOnError = new AtomicBoolean(true);

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
         boolean isMetadataComplete = true;

         // Process annotations
         if (cmd == null || cmd.is16())
         {
            AnnotationScanner annotationScanner = 
               AnnotationScannerFactory.getStrategy(AnnotationScannerFactory.JAVASSIST_INPUT_STREAM);
            AnnotationRepository annotationRepository = annotationScanner.scan(cl.getURLs(), cl);

            isMetadataComplete = false;
            if (cmd != null)
            {
               if (cmd instanceof JCA16MetaData)
               {
                  JCA16MetaData jmd = (JCA16MetaData)cmd;
                  isMetadataComplete = jmd.isMetadataComplete();
               }
               else if (cmd instanceof JCA16DefaultNSMetaData)
               {
                  JCA16DefaultNSMetaData jmd = (JCA16DefaultNSMetaData)cmd;
                  isMetadataComplete = jmd.isMetadataComplete();
               }
               else if (cmd instanceof JCA16DTDMetaData)
               {
                  JCA16DTDMetaData jmd = (JCA16DTDMetaData)cmd;
                  isMetadataComplete = jmd.isMetadataComplete();
               }
            }
            
            if (cmd == null || !isMetadataComplete)
            {
               Annotations annotator = new Annotations();
               cmd = annotator.process(cmd, annotationRepository);
            }
         }
         
         // Validate metadata
         metadataHandler.validate(cmd);
         
         // Merge metadata
         cmd = metadataHandler.merge(cmd, jrmd);


         List<ValidateObject> archiveValidationObjects = new ArrayList<ValidateObject>();
         List<Object> beanValidationObjects = new ArrayList<Object>();

         // Create objects and inject values
         if (cmd != null)
         {
            // ResourceAdapter
            if (cmd.getRa() != null && cmd.getRa().getRaClass() != null)
            {
               Object o = initAndInject(cmd.getRa().getRaClass(), cmd.getRa().getConfigProperty(), cl);
               archiveValidationObjects.add(new ValidateObject(Key.RESOURCE_ADAPTER, o));
               beanValidationObjects.add(o);
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
                        Object o = initAndInject(cdMeta.getManagedConnectionFactoryClass(), 
                                                 cdMeta.getConfigProps(), cl);
                        archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY, o));
                        beanValidationObjects.add(o);
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
                        Object o = initAndInject(mlMeta.getActivationSpecType().getAsClass(), 
                                                 mlMeta.getActivationSpecType().getConfigProps(), cl);
                        archiveValidationObjects.add(new ValidateObject(Key.ACTIVATION_SPEC, o));
                        beanValidationObjects.add(o);
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
                        archiveValidationObjects.add(new ValidateObject(Key.ADMIN_OBJECT, o));
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

         log.info("Deployed: " + url.toExternalForm());

         return new RADeployment(url, cl);
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
    * @param directrory The directory
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
    * Clone
    * @return The copy of the object
    * @exception CloneNotSupportedException Thrown if a copy can't be created
    */
   public Deployer clone() throws CloneNotSupportedException
   {
      return new RADeployer();
   }
}
