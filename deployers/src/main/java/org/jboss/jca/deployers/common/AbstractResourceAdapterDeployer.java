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

package org.jboss.jca.deployers.common;

import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.FailureHelper;
import org.jboss.jca.validator.Key;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.Validate;
import org.jboss.jca.validator.ValidateClass;
import org.jboss.jca.validator.ValidateObject;
import org.jboss.jca.validator.Validator;
import org.jboss.jca.validator.ValidatorException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.security.SubjectFactory;

/**
 * An abstract resource adapter deployer which contains common functionality
 * for all resource adapter archive based deployers.
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractResourceAdapterDeployer
{
   /** the logger **/
   protected final Logger log;

   /** trace boolean check */
   protected final boolean trace;

   /** boolean to set if validation is needed at class level or it should be considered already valid
    * (IOW  object put in repository at previous steps have been already validated at class level**/
   protected final boolean validateClasses;

   /** The configuration */
   private Configuration configuration = null;

   /**
    * Create a new AbstractResourceAdapterDeployer.
    *
    * @param validateClasses validateClasses validateClasses  boolean to express if this instance will
    * apply validation on classes structure
    * @param log the right log where put messages
    */
   public AbstractResourceAdapterDeployer(boolean validateClasses, Logger log)
   {
      super();
      this.log = log;
      trace = log.isTraceEnabled();
      this.validateClasses = validateClasses;
   }

   /**
    * Set the configuration
    * @param value value value The value
    */
   public void setConfiguration(Configuration value)
   {
      configuration = value;
   }

   /**
    * Get the configuration
    * @return The value
    */
   public Configuration getConfiguration()
   {
      return configuration;
   }

   /**
    * validate archive
    *
    * @param url url url of the archive
    * @param archiveValidation archiveValidation archiveValidation classes and/or to validate.
    * @param failures failures failures original list of failures
    * @return The list of failures gotten with all new failures added. Null in case of no failures
    * or if validation is not run according to {@link #getArchiveValidation()} Setting. It returns null also if
    * the concrete implementation of this class set validateClasses instance variable to flase and the list of
    * archiveValidation contains one or more instance of {@link ValidateClass} type
    */
   public Set<Failure> validateArchive(URL url, List<Validate> archiveValidation, Set<Failure> failures)
   {
      // Archive validation
      if (!getConfiguration().getArchiveValidation())
      {
         return null;
      }

      for (Validate validate : archiveValidation)
      {
         if (!(validate instanceof ValidateObject) && !this.validateClasses)
            return null;
      }

      Validator validator = new Validator();
      List<Failure> partialFailures = validator.validate(archiveValidation);

      if (partialFailures != null)
      {
         if (failures == null)
         {
            failures = new HashSet<Failure>();
         }
         failures.addAll(partialFailures);
      }

      return failures;
   }

   /**
    * print Failures into Log files.
    *
    * @param urlFileName urlFileName urlFileName filename Of deployed rar
    * @param validator validator validator validator instance used to run validation rules
    * @param failures failures failures the list of Failures to be printed
    * @param reportDirectory reportDirectory reportDirectory where to put various logs
    * @param fhInput fhInput fhInput optional parameter. Normally used only for test or in case of
    *   FailureHelper already present in context
    * @return the error Text
    *
    */
   public String printFailuresLog(String urlFileName, Validator validator, Collection<Failure> failures,
      File reportDirectory, FailureHelper... fhInput)
   {
      String errorText = "";
      FailureHelper fh = null;
      if (fhInput.length == 0)
         fh = new FailureHelper(failures);
      else
         fh = fhInput[0];

      if (failures != null && failures.size() > 0)
      {
         if (reportDirectory == null)
         {
            reportDirectory = getReportDirectory();
         }
         if (reportDirectory != null && reportDirectory.exists())
         {
            int lastSlashIndex = urlFileName.lastIndexOf("/");
            int lastSepaIndex = urlFileName.lastIndexOf(File.separator);

            int lastIndex = lastSlashIndex > lastSepaIndex ? lastSlashIndex : lastSepaIndex;
            if (lastIndex != -1)
               urlFileName = urlFileName.substring(lastIndex + 1);
            urlFileName += ".log";

            File report = new File(reportDirectory, urlFileName);
            FileWriter fw = null;
            BufferedWriter bw = null;
            try
            {
               fw = new FileWriter(report);
               bw = new BufferedWriter(fw, 8192);
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
               if (bw != null)
               {
                  try
                  {
                     bw.close();
                  }
                  catch (IOException ignore)
                  {
                     // Ignore
                  }
               }
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
      }
      return errorText;
   }

   /**
    * Cehck for failures at a certain level
    * @param failures failures failures The failures
    * @param severity severity severity The level
    * @return True if a failure is found with the specified severity; otherwise false
    */
   protected boolean hasFailuresLevel(Collection<Failure> failures, int severity)
   {
      if (failures != null)
      {
         for (Failure failure : failures)
         {
            if (failure.getSeverity() == severity)
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Start the resource adapter
    * @param resourceAdapter resourceAdapter resourceAdapter The resource adapter
    * @param bootstrapIdentifier bootstrapIdentifier bootstrapIdentifier The bootstrap context identifier;
    * may be <code>null</code>
    * @throws DeployException DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   protected void startContext(ResourceAdapter resourceAdapter, String bootstrapIdentifier) throws DeployException
   {
      try
      {
         Class clz = resourceAdapter.getClass();
         Method start = clz.getMethod("start", new Class[]{BootstrapContext.class});

         CloneableBootstrapContext cbc = null;

         if (bootstrapIdentifier != null && getConfiguration().getBootstrapContexts() != null)
         {
            CloneableBootstrapContext bc = getConfiguration().getBootstrapContexts().get(bootstrapIdentifier);

            if (bc != null)
               cbc = bc.clone();
         }

         if (cbc == null)
            cbc = getConfiguration().getDefaultBootstrapContext().clone();

         start.invoke(resourceAdapter, new Object[]{cbc});
      }
      catch (InvocationTargetException ite)
      {
         throw new DeployException("Unable to start " + resourceAdapter.getClass().getName(),
                                   ite.getTargetException());
      }
      catch (Throwable t)
      {
         throw new DeployException("Unable to start " + resourceAdapter.getClass().getName(), t);
      }
   }

   /**
    * Associate resource adapter with ojects if they implement ResourceAdapterAssociation
    * @param resourceAdapter resourceAdapter resourceAdapter The resource adapter
    * @param object object object The of possible association object
    * @throws DeployException DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   protected void associateResourceAdapter(ResourceAdapter resourceAdapter, Object object) throws DeployException
   {
      if (resourceAdapter != null && object != null)
      {
         if (object instanceof ResourceAdapterAssociation)
         {
            try
            {
               Class clz = object.getClass();

               Method setResourceAdapter = clz.getMethod("setResourceAdapter", new Class[]{ResourceAdapter.class});

               setResourceAdapter.invoke(object, new Object[]{resourceAdapter});
            }
            catch (Throwable t)
            {
               throw new DeployException("Unable to associate " + object.getClass().getName(), t);
            }
         }
      }
   }

   /**
    * Find the metadata for a managed connection factory
    * @param clz The fully quilified class name for the managed connection factory
    * @param defs The connection definitions
    * @return The metadata; <code>null</code> if none could be found
    */
   protected org.jboss.jca.common.api.metadata.common.CommonConnDef findConnectionDefinition(String clz,
      List<org.jboss.jca.common.api.metadata.common.CommonConnDef> defs)
   {
      if (defs != null)
      {
         // If there is only one we will return that
         if (defs.size() == 1)
         {
            org.jboss.jca.common.api.metadata.common.CommonConnDef cd = defs.get(0);

            if (cd.getClassName() != null && !clz.equals(cd.getClassName()))
            {
               log.warn("Only one connection definition found with a mismatch in class-name: " + cd);
               return null;
            }

            return cd;
         }

         // If there are multiple definitions the MCF class name is mandatory
         if (clz == null)
            throw new IllegalArgumentException("ManagedConnectionFactory must be defined in class-name");

         for (org.jboss.jca.common.api.metadata.common.CommonConnDef cd : defs)
         {
            if (clz.equals(cd.getClassName()))
               return cd;
         }
      }

      return null;
   }

   /**
    * Find the metadata for an admin object
    * @param clz The fully quilified class name for the admin object
    * @param defs The admin object definitions
    * @return The metadata; <code>null</code> if none could be found
    */
   protected org.jboss.jca.common.api.metadata.common.CommonAdminObject findAdminObject(String clz,
      List<org.jboss.jca.common.api.metadata.common.CommonAdminObject> defs)
   {
      if (defs != null)
      {
         // If there is only one we will return that
         if (defs.size() == 1)
         {
            org.jboss.jca.common.api.metadata.common.CommonAdminObject cao = defs.get(0);

            if (cao.getClassName() != null && !clz.equals(cao.getClassName()))
            {
               log.warn("Only one admin object found with a mismatch in class-name: " + cao);
               return null;
            }

            return cao;
         }

         // If there are multiple definitions the admin object class name is mandatory
         if (clz == null)
            throw new IllegalArgumentException("AdminObject must be defined in class-name");

         for (org.jboss.jca.common.api.metadata.common.CommonAdminObject cao : defs)
         {
            if (clz.equals(cao.getClassName()))
               return cao;
         }
      }

      return null;
   }

   /**
    * Create an instance of the pool configuration based on the input
    * @param pp pp pp The pool parameters
    * @param tp tp tp The timeout parameters
    * @param vp vp vp The validation parameters
    * @return The configuration
    */
   protected PoolConfiguration createPoolConfiguration(CommonPool pp, CommonTimeOut tp, CommonValidation vp)
   {
      PoolConfiguration pc = new PoolConfiguration();

      if (pp != null)
      {
         if (pp.getMinPoolSize() != null)
            pc.setMinSize(pp.getMinPoolSize().intValue());

         if (pp.getMaxPoolSize() != null)
            pc.setMaxSize(pp.getMaxPoolSize().intValue());

         if (pp.isPrefill() != null)
            pc.setPrefill(pp.isPrefill());

         if (pp.isUseStrictMin() != null)
            pc.setStrictMin(pp.isUseStrictMin());
      }

      if (tp != null)
      {
         if (tp.getBlockingTimeoutMillis() != null)
            pc.setBlockingTimeout(tp.getBlockingTimeoutMillis().longValue());

         if (tp.getIdleTimeoutMinutes() != null)
            pc.setIdleTimeout(tp.getIdleTimeoutMinutes().longValue());
      }

      if (vp != null)
      {
         if (vp.isBackgroundValidation() != null)
            pc.setBackgroundValidation(vp.isBackgroundValidation().booleanValue());

         if (vp.getBackgroundValidationMinutes() != null)
            pc.setBackgroundValidationMinutes(vp.getBackgroundValidationMinutes().intValue());

         if (vp.isUseFastFail() != null)
            pc.setUseFastFail(vp.isUseFastFail());
      }

      return pc;
   }

   /**
    * Start
    */
   public void start()
   {
      if (!checkConfigurationIsValid())
         throw new IllegalStateException("Configuration not valid or not defined");
   }

   /**
    * init the acrtivation spec
    *
    * @param  cl cl
    * @param  cmd cmd
    * @param  resourceAdapter resourceAdapter
    * @param  archiveValidationObjects archiveValidationObjects
    * @param  beanValidationObjects beanValidationObjects
    * @param failures falures to be updated during implemented operations
    * @param url url
    * @param activateDeployment activateDeployment
    * @return failures updated after implemented operations
    * @throws DeployException DeployException in case of error
    */
   protected Set<Failure> initActivationSpec(ClassLoader cl, Connector cmd, ResourceAdapter resourceAdapter,
      List<Validate> archiveValidationObjects, List<Object> beanValidationObjects, Set<Failure> failures, URL url,
      boolean activateDeployment) throws DeployException
   {
      // ActivationSpec
      if (cmd.getVersion() != Version.V_10)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) cmd.getResourceadapter();
         if (ra1516 != null && ra1516.getInboundResourceadapter() != null &&
             ra1516.getInboundResourceadapter().getMessageadapter() != null &&
             ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
         {
            List<MessageListener> mlMetas = ra1516.getInboundResourceadapter().getMessageadapter()
               .getMessagelisteners();

            if (mlMetas.size() > 0)
            {
               for (MessageListener mlMD : mlMetas)
               {
                  if (mlMD.getActivationspec() != null &&
                      mlMD.getActivationspec().getActivationspecClass().getValue() != null)
                  {
                     failures = validateArchive(
                        url,
                        Arrays.asList((Validate) new ValidateClass(Key.ACTIVATION_SPEC, mlMD.getActivationspec()
                           .getActivationspecClass().getValue(), cl, mlMD.getActivationspec().getConfigProperties())),
                        failures);
                     if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                        Severity.ERROR)))
                     {
                        if (activateDeployment)
                        {
                           List<? extends ConfigProperty> cpm = mlMD.getActivationspec().getConfigProperties();

                           Object o = initAndInject(mlMD.getActivationspec().getActivationspecClass().getValue(),
                              cpm, cl);

                           if (trace)
                           {
                              log.trace("ActivationSpec: " + o.getClass().getName());
                              log.trace("ActivationSpec defined in classloader: " + o.getClass().getClassLoader());
                           }

                           archiveValidationObjects.add(new ValidateObject(Key.ACTIVATION_SPEC, o, cpm));
                           beanValidationObjects.add(o);
                           associateResourceAdapter(resourceAdapter, o);
                        }
                     }
                  }
               }
            }
         }
      }
      return failures;
   }

   /**
    * init an Admin Object
    *
    * @param  cmd cmd
    * @param  cl cl
    * @param  archiveValidationObjects archiveValidationObjects
    * @param  beanValidationObjects beanValidationObjects
    * @param failures falures to be updated during implemented operations
    * @param url url
    * @param deploymentName The deployment name
    * @param activateDeployment activateDeployment
    * @param aosRaXml Admin object definitions from -ra.xml
    * @param aosIronJacamar Admin object definitions from ironjacamar.xml
    * @param aos The resulting array of admin objects
    * @param aoJndiNames The resulting array of JNDI names
    * @param mgtConnector The management view of the connector
    * @return failures updated after implemented operations
    * @throws DeployException DeployException in case of errors
    */
   protected Set<Failure> initAdminObject(Connector cmd, ClassLoader cl, List<Validate> archiveValidationObjects,
      List<Object> beanValidationObjects, Set<Failure> failures,
         URL url, String deploymentName, boolean activateDeployment,
         List<org.jboss.jca.common.api.metadata.common.CommonAdminObject> aosRaXml,
         List<org.jboss.jca.common.api.metadata.common.CommonAdminObject> aosIronJacamar,
         Object[] aos, String[] aoJndiNames,
         org.jboss.jca.core.api.management.Connector mgtConnector)
      throws DeployException
   {
      // AdminObject
      if (cmd.getVersion() != Version.V_10)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) cmd.getResourceadapter();
         if (ra1516 != null && ra1516.getAdminObjects() != null)
         {
            List<AdminObject> aoMetas = ((ResourceAdapter1516) cmd.getResourceadapter()).getAdminObjects();
            if (aoMetas.size() > 0)
            {
               aos = new Object[aoMetas.size()];
               aoJndiNames = new String[aoMetas.size()];

               for (int i = 0; i < aoMetas.size(); i++)
               {
                  AdminObject aoMeta = aoMetas.get(i);

                  if (aoMeta.getAdminobjectClass() != null && aoMeta.getAdminobjectClass().getValue() != null)
                  {
                     CommonAdminObject aoRaXml = findAdminObject(aoMeta.getAdminobjectClass().getValue(), aosRaXml);
                     CommonAdminObject ijAO = findAdminObject(aoMeta.getAdminobjectClass().getValue(), aosIronJacamar);

                     failures = validateArchive(url,
                        Arrays.asList((Validate) new ValidateClass(Key.ADMIN_OBJECT, aoMeta.getAdminobjectClass()
                           .getValue(), cl, aoMeta.getConfigProperties())), failures);
                     if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                        Severity.ERROR)))
                     {
                        if (activateDeployment)
                        {
                           Object ao = initAndInject(aoMeta.getAdminobjectClass().getValue(),
                                                     aoMeta.getConfigProperties(), cl);

                           if (trace)
                           {
                              log.trace("AdminObject: " + ao.getClass().getName());
                              log.trace("AdminObject defined in classloader: " + ao.getClass().getClassLoader());
                           }

                           archiveValidationObjects.add(new ValidateObject(Key.ADMIN_OBJECT, ao, aoMeta
                                                                           .getConfigProperties()));
                           beanValidationObjects.add(ao);

                           if (ao != null && ao instanceof Serializable && ao instanceof Referenceable)
                           {
                              try
                              {
                                 String jndiName = null;
                                 if (aoRaXml != null || ijAO != null)
                                 {
                                    if (aoRaXml != null)
                                    {
                                       jndiName = aoRaXml.getJndiName();

                                       if (aoRaXml.isUseJavaContext() != null &&
                                           aoRaXml.isUseJavaContext().booleanValue() &&
                                           !jndiName.startsWith("java:/"))
                                       {
                                          jndiName = "java:/" + jndiName;
                                       }
                                    }
                                    else
                                    {
                                       jndiName = ijAO.getJndiName();

                                       if (ijAO.isUseJavaContext() != null &&
                                           ijAO.isUseJavaContext().booleanValue() &&
                                           !jndiName.startsWith("java:/"))
                                       {
                                          jndiName = "java:/" + jndiName;
                                       }
                                    }

                                    bindAdminObject(url, deploymentName, ao, jndiName);
                                 }
                                 else
                                 {
                                    String[] names = bindAdminObject(url, deploymentName, ao);
                                    jndiName = names[0];
                                 }

                                 aos[i] = ao;
                                 aoJndiNames[i] = jndiName;

                                 org.jboss.jca.core.api.management.AdminObject mgtAo =
                                    new org.jboss.jca.core.api.management.AdminObject(ao);

                                 mgtAo.getConfigProperties().
                                    addAll(createManagementView(aoMeta.getConfigProperties()));

                                 mgtConnector.getAdminObjects().add(mgtAo);
                              }
                              catch (Throwable t)
                              {
                                 throw new DeployException("Failed to bind admin object", t);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      return failures;
   }

   /**
   *
   * create objects and inject value for this depployment. it is a general method returning a {@link CommonDeployment}
   * to be used to exchange objects needed to real injection in the container
   *
   * @param url url
   * @param deploymentName deploymentName
   * @param root root
   * @param cl cl
   * @param cmd connector md
   * @param ijmd ironjacamar md
   * @return return the exchange POJO with value useful for injection in the container (fungal or AS)
   * @throws DeployException DeployException
   * @throws ResourceException ResourceException
   * @throws ValidatorException ValidatorException
   * @throws AlreadyExistsException AlreadyExistsException
   * @throws ClassNotFoundException ClassNotFoundException
   * @throws Throwable Throwable
   */
   protected CommonDeployment createObjectsAndInjectValue(URL url, String deploymentName, File root, ClassLoader cl,
      Connector cmd, IronJacamar ijmd) throws DeployException, ResourceException, ValidatorException,
      AlreadyExistsException, ClassNotFoundException, Throwable
   {
      return createObjectsAndInjectValue(url, deploymentName, root, cl, cmd, ijmd, null);
   }

   /**
    *
    * create objects and inject value for this depployment. it is a general method returning a {@link CommonDeployment}
    * to be used to exchange objects needed to real injection in the container
    *
    * @param url url
    * @param deploymentName deploymentName
    * @param root root
    * @param cl cl
    * @param cmd connector md
    * @param ijmd ironjacamar md
    * @param raxml Resource Adapter from -ra.xml definition
    * @return return the exchange POJO with value useful for injection in the container (fungal or AS)
    * @throws DeployException DeployException
    * @throws ResourceException ResourceException
    * @throws ValidatorException ValidatorException
    * @throws AlreadyExistsException AlreadyExistsException
    * @throws ClassNotFoundException ClassNotFoundException
    * @throws Throwable Throwable
    */
   protected CommonDeployment createObjectsAndInjectValue(URL url, String deploymentName, File root, ClassLoader cl,
      Connector cmd, IronJacamar ijmd, org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter raxml)
      throws DeployException, ResourceException, ValidatorException, AlreadyExistsException, ClassNotFoundException,
      Throwable
   {
      Set<Failure> failures = null;
      try
      {
         // Notify regarding license terms
         if (cmd != null && cmd.getLicense() != null && cmd.getLicense().isLicenseRequired())
            log.info("Required license terms for " + url.toExternalForm());

         String mgtUniqueId = url.getFile();
         if (mgtUniqueId.indexOf("/") != -1)
            mgtUniqueId = mgtUniqueId.substring(mgtUniqueId.lastIndexOf("/") + 1);

         org.jboss.jca.core.api.management.Connector mgtConnector =
            new org.jboss.jca.core.api.management.Connector(mgtUniqueId);

         ResourceAdapter resourceAdapter = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Object> beanValidationObjects = new ArrayList<Object>();
         Object[] cfs = null;
         String[] cfJndiNames = null;
         Object[] aos = null;
         String[] aoJndiNames = null;

         // Check metadata for JNDI information and activate explicit
         boolean activateDeployment = checkActivation(cmd, ijmd);

         // Create objects and inject values
         if (cmd != null)
         {
            // ResourceAdapter
            if (cmd.getVersion() != Version.V_10)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516) cmd.getResourceadapter();
               if (ra1516 != null && ra1516.getResourceadapterClass() != null)
               {
                  failures = validateArchive(url,
                     Arrays.asList((Validate) new ValidateClass(Key.RESOURCE_ADAPTER, ra1516
                        .getResourceadapterClass(), cl, cmd.getResourceadapter().getConfigProperties())), failures);

                  if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                     Severity.ERROR)))
                  {
                     if (activateDeployment)
                     {
                        resourceAdapter = (ResourceAdapter) initAndInject(ra1516.getResourceadapterClass(),
                           ra1516.getConfigProperties(), cl);

                        if (trace)
                        {
                           log.trace("ResourceAdapter: " + resourceAdapter.getClass().getName());
                           log.trace("ResourceAdapter defined in classloader: " +
                                     resourceAdapter.getClass().getClassLoader());
                        }

                        archiveValidationObjects.add(new ValidateObject(Key.RESOURCE_ADAPTER, resourceAdapter, ra1516
                           .getConfigProperties()));
                        beanValidationObjects.add(resourceAdapter);

                        org.jboss.jca.core.api.management.ResourceAdapter mgtRa =
                           new org.jboss.jca.core.api.management.ResourceAdapter(resourceAdapter);

                        mgtRa.getConfigProperties().addAll(createManagementView(ra1516.getConfigProperties()));
                        mgtConnector.setResourceAdapter(mgtRa);
                     }
                  }
               }
            }

            // ManagedConnectionFactory
            if (cmd.getVersion() == Version.V_10)
            {
               ResourceAdapter10 ra10 = (ResourceAdapter10) cmd.getResourceadapter();

               if (activateDeployment)
               {
                  CommonConnDef ijCD = null;
                  CommonConnDef cdRaXml = null;
                  if (raxml != null)
                  {
                     cdRaXml = findConnectionDefinition(ra10.getManagedConnectionFactoryClass().getValue(),
                        raxml.getConnectionDefinitions());
                  }
                  if (ijmd != null)
                  {
                     ijCD = findConnectionDefinition(ra10.getManagedConnectionFactoryClass().getValue(),
                        ijmd.getConnectionDefinitions());
                  }
                  //
                  //                  if (ijmd == null || ijCD == null || ijCD.isEnabled())
                  if (ijCD == null || ijCD.isEnabled() || (cdRaXml != null && cdRaXml.isEnabled()))
                  {
                     ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(ra10
                        .getManagedConnectionFactoryClass().getValue(), ra10.getConfigProperties(), cl);

                     if (trace)
                     {
                        log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                        log.trace("ManagedConnectionFactory is defined in classloader: " +
                                  mcf.getClass().getClassLoader());
                     }

                     mcf.setLogWriter(getLogPrintWriter());

                     archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY, mcf, ra10
                        .getConfigProperties()));
                     beanValidationObjects.add(mcf);
                     associateResourceAdapter(resourceAdapter, mcf);
                     // Create the pool
                     PoolConfiguration pc = null;
                     if (cdRaXml != null)
                     {
                        pc = createPoolConfiguration(cdRaXml.getPool(), cdRaXml.getTimeOut(), cdRaXml.getValidation());
                     }
                     else if (ijCD != null)
                     {
                        pc = createPoolConfiguration(ijCD.getPool(), ijCD.getTimeOut(), ijCD.getValidation());
                     }
                     else
                     {
                        // Default default settings
                        pc = createPoolConfiguration(null, null, null);
                     }
                     PoolFactory pf = new PoolFactory();

                     Boolean noTxSeparatePool = Boolean.FALSE;
                     CommonSecurity security = null;
                     if (cdRaXml != null && cdRaXml.getPool() != null && cdRaXml.isXa())
                     {
                        CommonXaPool ijXaPool = (CommonXaPool) cdRaXml.getPool();
                        security = cdRaXml.getSecurity();
                        if (ijXaPool != null)
                           noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                     }
                     else if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                     {
                        CommonXaPool ijXaPool = (CommonXaPool) ijCD.getPool();
                        security = ijCD.getSecurity();
                        if (ijXaPool != null)
                           noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                     }

                     PoolStrategy strategy= PoolStrategy.ONE_POOL;

                     String securityDomain = null;
                     if (security != null) {
                        if (security.isApplication())
                        {
                           strategy = PoolStrategy.POOL_BY_CRI;
                        }
                        else if (security.getSecurityDomain() != null &&
                                 security.getSecurityDomain().trim().length() != 0)
                        {
                           strategy = PoolStrategy.POOL_BY_SUBJECT;
                           securityDomain = security.getSecurityDomain();
                        }
                        else if (security.getSecurityDomainAndApplication() != null &&
                                 security.getSecurityDomainAndApplication().trim().length() != 0)
                        {
                           strategy = PoolStrategy.POOL_BY_SUBJECT_AND_CRI;
                           securityDomain = security.getSecurityDomainAndApplication();
                        }

                     }

                     Pool pool = pf.create(strategy, mcf, pc, noTxSeparatePool.booleanValue());

                     // Add a connection manager
                     ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                     ConnectionManager cm = null;

                     TransactionSupportEnum tsmd = TransactionSupportEnum.NoTransaction;
                     if (raxml != null && raxml.getTransactionSupport() != null)
                     {
                        tsmd = raxml.getTransactionSupport();
                     }
                     else if (ijmd != null && ijmd.getTransactionSupport() != null)
                     {
                        tsmd = ijmd.getTransactionSupport();
                     }
                     else
                     {
                        tsmd = ((ResourceAdapter10) cmd.getResourceadapter()).getTransactionSupport();
                     }

                     TransactionSupportLevel tsl = TransactionSupportLevel.NoTransaction;

                     if (tsmd == TransactionSupportEnum.NoTransaction)
                     {
                        tsl = TransactionSupportLevel.NoTransaction;
                     }
                     else if (tsmd == TransactionSupportEnum.LocalTransaction)
                     {
                        tsl = TransactionSupportLevel.LocalTransaction;
                     }
                     else if (tsmd == TransactionSupportEnum.XATransaction)
                     {
                        tsl = TransactionSupportLevel.XATransaction;
                     }

                     // Section 7.13 -- Read from metadata -> overwrite with specified value if present
                     if (mcf instanceof TransactionSupport)
                        tsl = ((TransactionSupport) mcf).getTransactionSupport();

                     // Connection manager properties
                     Integer allocationRetry = null;
                     Long allocationRetryWaitMillis = null;
                     if (cdRaXml != null && cdRaXml.getTimeOut() != null)
                     {
                        allocationRetry = cdRaXml.getTimeOut().getAllocationRetry();
                        allocationRetryWaitMillis = cdRaXml.getTimeOut().getAllocationRetryWaitMillis();
                     }

                     if (ijCD != null && ijCD.getTimeOut() != null)
                     {
                        if (allocationRetry == null)
                           allocationRetry = ijCD.getTimeOut().getAllocationRetry();

                        if (allocationRetryWaitMillis == null)
                           allocationRetryWaitMillis = ijCD.getTimeOut().getAllocationRetryWaitMillis();
                     }

                     // Select the correct connection manager
                     if (tsl == TransactionSupportLevel.NoTransaction)
                     {
                        cm = cmf.createNonTransactional(tsl, pool, getSubjectFactory(securityDomain),
                           allocationRetry,
                           allocationRetryWaitMillis);
                     }
                     else
                     {
                        Boolean interleaving = null;
                        Integer xaResourceTimeout = null;
                        Boolean isSameRMOverride = null;
                        Boolean wrapXAResource = null;
                        Boolean padXid = null;
                        if (cdRaXml != null && cdRaXml.isXa())
                        {
                           CommonXaPool ijXaPool = (CommonXaPool) cdRaXml.getPool();

                           interleaving = ijXaPool.isInterleaving();
                           isSameRMOverride = ijXaPool.isSameRmOverride();
                           wrapXAResource = ijXaPool.isWrapXaDataSource();
                           padXid = ijXaPool.isPadXid();
                        }

                        if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                        {
                           CommonXaPool ijXaPool = (CommonXaPool) ijCD.getPool();

                           if (ijXaPool != null)
                           {
                              if (interleaving == null)
                                 interleaving = ijXaPool.isInterleaving();

                              if (isSameRMOverride == null)
                                 isSameRMOverride = ijXaPool.isSameRmOverride();

                              if (wrapXAResource == null)
                                 wrapXAResource = ijXaPool.isWrapXaDataSource();

                              if (padXid == null)
                                 padXid = ijXaPool.isPadXid();
                           }
                        }

                        cm = cmf.createTransactional(tsl, pool, getSubjectFactory(securityDomain), allocationRetry,
                           allocationRetryWaitMillis,
                           getTransactionManager(), interleaving, xaResourceTimeout, isSameRMOverride,
                           wrapXAResource, padXid);
                     }

                     // ConnectionFactory
                     Object cf = mcf.createConnectionFactory(cm);

                     if (cf == null)
                     {
                        log.error("ConnectionFactory is null");
                     }
                     else
                     {
                        if (trace)
                        {
                           log.trace("ConnectionFactory: " + cf.getClass().getName());
                           log.trace("ConnectionFactory defined in classloader: " + cf.getClass().getClassLoader());
                        }
                     }

                     archiveValidationObjects.add(new ValidateObject(Key.CONNECTION_FACTORY, cf));

                     if (cf != null && cf instanceof Serializable && cf instanceof Referenceable)
                     {
                        if (cdRaXml != null || ijCD != null)
                        {
                           String jndiName;
                           if (cdRaXml != null)
                           {
                              jndiName = cdRaXml.getJndiName();

                              if (cdRaXml.isUseJavaContext() != null &&
                                  cdRaXml.isUseJavaContext().booleanValue() &&
                                  !jndiName.startsWith("java:/"))
                              {
                                 jndiName = "java:/" + jndiName;
                              }
                           }
                           else
                           {
                              jndiName = ijCD.getJndiName();

                              if (ijCD.isUseJavaContext() != null &&
                                  ijCD.isUseJavaContext().booleanValue() &&
                                  !jndiName.startsWith("java:/"))
                              {
                                 jndiName = "java:/" + jndiName;
                              }
                           }

                           bindConnectionFactory(url, deploymentName, cf, jndiName);
                           cfs = new Object[]{cf};
                           cfJndiNames = new String[]{jndiName};

                           cm.setJndiName(jndiName);

                           String poolName = null;
                           if (cdRaXml != null)
                           {
                              poolName = cdRaXml.getPoolName();
                           }
                           else if (ijCD != null)
                           {
                              poolName = ijCD.getPoolName();
                           }

                           if (poolName == null)
                              poolName = jndiName;

                           pool.setName(poolName);
                        }
                        else
                        {
                           cfJndiNames = bindConnectionFactory(url, deploymentName, cf);
                           cfs = new Object[]{cf};

                           cm.setJndiName(cfJndiNames[0]);

                           String poolName = null;
                           if (cdRaXml != null)
                           {
                              poolName = cdRaXml.getPoolName();
                           }
                           else if (ijCD != null)
                           {
                              poolName = ijCD.getPoolName();
                           }

                           if (poolName == null)
                              poolName = cfJndiNames[0];

                           pool.setName(poolName);
                        }

                        org.jboss.jca.core.api.management.ManagedConnectionFactory mgtMcf =
                           new org.jboss.jca.core.api.management.ManagedConnectionFactory(mcf);

                        mgtMcf.getConfigProperties().addAll(createManagementView(ra10.getConfigProperties()));
                        mgtMcf.setPoolConfiguration(pc);
                        mgtMcf.setPool(pool);

                        mgtConnector.getManagedConnectionFactories().add(mgtMcf);
                     }
                  }
               }
            }
            else
            {
               ResourceAdapter1516 ra = (ResourceAdapter1516) cmd.getResourceadapter();
               if (ra != null && ra.getOutboundResourceadapter() != null &&
                   ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
               {
                  List<ConnectionDefinition> cdMetas = ra.getOutboundResourceadapter().getConnectionDefinitions();
                  if (cdMetas.size() > 0)
                  {
                     //                     if (cdMetas.size() == 1)
                     //                     {
                     //                        ConnectionDefinition cdMeta = cdMetas.get(0);
                     cfs = new Object[cdMetas.size()];
                     cfJndiNames = new String[cdMetas.size()];

                     for (int cdIndex = 0; cdIndex < cdMetas.size(); cdIndex++)
                     {
                        ConnectionDefinition cdMeta = cdMetas.get(cdIndex);

                        failures = validateArchive(url,
                           Arrays.asList((Validate) new ValidateClass(Key.MANAGED_CONNECTION_FACTORY, cdMeta
                              .getManagedConnectionFactoryClass().getValue(), cl, cdMeta.getConfigProperties())),
                           failures);

                        if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                           Severity.ERROR)))
                        {
                           if (activateDeployment)
                           {
                              org.jboss.jca.common.api.metadata.common.CommonConnDef ijCD = null;
                              org.jboss.jca.common.api.metadata.common.CommonConnDef cdRaXml = null;

                              if (raxml != null)
                              {
                                 cdRaXml = findConnectionDefinition(cdMeta.getManagedConnectionFactoryClass()
                                    .getValue(), raxml.getConnectionDefinitions());
                              }
                              if (ijmd != null)
                              {
                                 ijCD = findConnectionDefinition(
                                    cdMeta.getManagedConnectionFactoryClass().getValue(),
                                    ijmd.getConnectionDefinitions());
                              }

                              //                              if (ijmd == null || ijCD == null || ijCD.isEnabled())
                              //                              {
                              if (ijCD == null || ijCD.isEnabled() || (cdRaXml != null && cdRaXml.isEnabled()))
                              {
                                 ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(cdMeta
                                    .getManagedConnectionFactoryClass().getValue(), cdMeta.getConfigProperties(), cl);

                                 if (trace)
                                 {
                                    log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                                    log.trace("ManagedConnectionFactory defined in classloader: " +
                                              mcf.getClass().getClassLoader());
                                 }

                                 mcf.setLogWriter(getLogPrintWriter());

                                 archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY, mcf,
                                                                                 cdMeta.getConfigProperties()));
                                 beanValidationObjects.add(mcf);
                                 associateResourceAdapter(resourceAdapter, mcf);
                                 // Create the pool
                                 PoolConfiguration pc = null;
                                 if (cdRaXml != null && cdRaXml.getPool() != null)
                                 {
                                    pc = createPoolConfiguration(cdRaXml.getPool(), cdRaXml.getTimeOut(),
                                       cdRaXml.getValidation());
                                 }
                                 else if (ijCD != null)
                                 {
                                    pc = createPoolConfiguration(ijCD.getPool(), ijCD.getTimeOut(),
                                       ijCD.getValidation());
                                 }
                                 else
                                 {
                                    // Default default settings
                                    pc = createPoolConfiguration(null, null, null);
                                 }

                                 PoolFactory pf = new PoolFactory();

                                 Boolean noTxSeparatePool = Boolean.FALSE;
                                 CommonSecurity security = null;
                                 if (cdRaXml != null && cdRaXml.getPool() != null && cdRaXml.isXa())
                                 {
                                    CommonXaPool ijXaPool = (CommonXaPool) cdRaXml.getPool();
                                    security = cdRaXml.getSecurity();
                                    if (ijXaPool != null)
                                       noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                                 }
                                 else if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                                 {
                                    CommonXaPool ijXaPool = (CommonXaPool) ijCD.getPool();
                                    security = ijCD.getSecurity();
                                    if (ijXaPool != null)
                                       noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                                 }

                                 PoolStrategy strategy = PoolStrategy.ONE_POOL;

                                 String securityDomain = null;
                                 if (security != null) {
                                    if (security.isApplication())
                                    {
                                       strategy = PoolStrategy.POOL_BY_CRI;
                                    }
                                    else if (security.getSecurityDomain() != null &&
                                             security.getSecurityDomain().trim().length() != 0)
                                    {
                                       strategy = PoolStrategy.POOL_BY_SUBJECT;
                                       securityDomain = security.getSecurityDomain();
                                    }
                                    else if (security.getSecurityDomainAndApplication() != null &&
                                             security.getSecurityDomainAndApplication().trim().length() != 0)
                                    {
                                       strategy = PoolStrategy.POOL_BY_SUBJECT_AND_CRI;
                                       securityDomain = security.getSecurityDomainAndApplication();
                                    }

                                 }
                                 Pool pool = pf.create(strategy, mcf, pc, noTxSeparatePool.booleanValue());

                                 // Add a connection manager
                                 ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                                 ConnectionManager cm = null;
                                 TransactionSupportLevel tsl = TransactionSupportLevel.NoTransaction;
                                 TransactionSupportEnum tsmd = TransactionSupportEnum.NoTransaction;
                                 if (raxml != null && raxml.getTransactionSupport() != null)
                                 {
                                    tsmd = raxml.getTransactionSupport();
                                 }
                                 else if (ijmd != null && ijmd.getTransactionSupport() != null)
                                 {
                                    tsmd = ijmd.getTransactionSupport();
                                 }
                                 else
                                 {
                                    tsmd = ra.getOutboundResourceadapter().getTransactionSupport();
                                 }

                                 if (tsmd == TransactionSupportEnum.NoTransaction)
                                 {
                                    tsl = TransactionSupportLevel.NoTransaction;
                                 }
                                 else if (tsmd == TransactionSupportEnum.LocalTransaction)
                                 {
                                    tsl = TransactionSupportLevel.LocalTransaction;
                                 }
                                 else if (tsmd == TransactionSupportEnum.XATransaction)
                                 {
                                    tsl = TransactionSupportLevel.XATransaction;
                                 }

                                 // Section 7.13 -- Read from metadata -> overwrite with specified value if present
                                 if (mcf instanceof TransactionSupport)
                                    tsl = ((TransactionSupport) mcf).getTransactionSupport();

                                 // Connection manager properties
                                 Integer allocationRetry = null;
                                 Long allocationRetryWaitMillis = null;
                                 if (cdRaXml != null && cdRaXml.getTimeOut() != null)
                                 {
                                    allocationRetry = cdRaXml.getTimeOut().getAllocationRetry();
                                    allocationRetryWaitMillis = cdRaXml.getTimeOut().getAllocationRetryWaitMillis();
                                 }

                                 if (ijCD != null && ijCD.getTimeOut() != null)
                                 {
                                    if (allocationRetry == null)
                                       allocationRetry = ijCD.getTimeOut().getAllocationRetry();

                                    if (allocationRetryWaitMillis == null)
                                       allocationRetryWaitMillis = ijCD.getTimeOut().getAllocationRetryWaitMillis();
                                 }

                                 // Select the correct connection manager
                                 if (tsl == TransactionSupportLevel.NoTransaction)
                                 {
                                    cm = cmf.createNonTransactional(tsl, pool, getSubjectFactory(securityDomain),
                                       allocationRetry,
                                       allocationRetryWaitMillis);
                                 }
                                 else
                                 {
                                    Boolean interleaving = null;
                                    Integer xaResourceTimeout = null;
                                    Boolean isSameRMOverride = null;
                                    Boolean wrapXAResource = null;
                                    Boolean padXid = null;
                                    if (cdRaXml != null && cdRaXml.isXa())
                                    {
                                       CommonXaPool ijXaPool = (CommonXaPool) cdRaXml.getPool();

                                       interleaving = ijXaPool.isInterleaving();
                                       isSameRMOverride = ijXaPool.isSameRmOverride();
                                       wrapXAResource = ijXaPool.isWrapXaDataSource();
                                       padXid = ijXaPool.isPadXid();
                                    }

                                    if (ijCD != null && ijCD.isXa())
                                    {
                                       CommonXaPool ijXaPool = (CommonXaPool) ijCD.getPool();

                                       if (interleaving == null)
                                          interleaving = ijXaPool.isInterleaving();

                                       if (isSameRMOverride == null)
                                          isSameRMOverride = ijXaPool.isSameRmOverride();

                                       if (wrapXAResource == null)
                                          wrapXAResource = ijXaPool.isWrapXaDataSource();

                                       if (padXid == null)
                                          padXid = ijXaPool.isPadXid();
                                    }

                                    cm = cmf.createTransactional(tsl, pool, getSubjectFactory(securityDomain),
                                       allocationRetry,
                                       allocationRetryWaitMillis, getTransactionManager(), interleaving,
                                       xaResourceTimeout, isSameRMOverride, wrapXAResource, padXid);
                                 }

                                 // ConnectionFactory
                                 Object cf = mcf.createConnectionFactory(cm);

                                 if (cf == null)
                                 {
                                    log.error("ConnectionFactory is null");
                                 }
                                 else
                                 {
                                    if (trace)
                                    {
                                       log.trace("ConnectionFactory: " + cf.getClass().getName());
                                       log.trace("ConnectionFactory defined in classloader: " +
                                                 cf.getClass().getClassLoader());
                                    }
                                 }

                                 archiveValidationObjects.add(new ValidateObject(Key.CONNECTION_FACTORY, cf));

                                 if (cf != null && cf instanceof Serializable && cf instanceof Referenceable)
                                 {
                                    if (cdRaXml != null || ijCD != null)
                                    {
                                       String jndiName;
                                       if (cdRaXml != null)
                                       {
                                          jndiName = cdRaXml.getJndiName();

                                          if (cdRaXml.isUseJavaContext() != null &&
                                              cdRaXml.isUseJavaContext().booleanValue() &&
                                              !jndiName.startsWith("java:/"))
                                          {
                                             jndiName = "java:/" + jndiName;
                                          }
                                       }
                                       else
                                       {
                                          jndiName = ijCD.getJndiName();

                                          if (ijCD.isUseJavaContext() != null &&
                                              ijCD.isUseJavaContext().booleanValue() &&
                                              !jndiName.startsWith("java:/"))
                                          {
                                             jndiName = "java:/" + jndiName;
                                          }
                                       }

                                       bindConnectionFactory(url, deploymentName, cf, jndiName);
                                       cfs[cdIndex] = cf;
                                       cfJndiNames[cdIndex] = jndiName;

                                       cm.setJndiName(jndiName);

                                       String poolName = null;
                                       if (cdRaXml != null)
                                       {
                                          poolName = cdRaXml.getPoolName();
                                       }
                                       else if (ijCD != null)
                                       {
                                          poolName = ijCD.getPoolName();
                                       }

                                       if (poolName == null)
                                          poolName = jndiName;

                                       pool.setName(poolName);
                                    }
                                    else
                                    {
                                       cfJndiNames = bindConnectionFactory(url, deploymentName, cf);
                                       cfs = new Object[]{cf};

                                       cm.setJndiName(cfJndiNames[0]);

                                       String poolName = null;
                                       if (cdRaXml != null)
                                       {
                                          poolName = cdRaXml.getPoolName();
                                       }
                                       else if (ijCD != null)
                                       {
                                          poolName = ijCD.getPoolName();
                                       }

                                       if (poolName == null)
                                          poolName = cfJndiNames[0];

                                       pool.setName(poolName);
                                    }

                                    org.jboss.jca.core.api.management.ManagedConnectionFactory mgtMcf =
                                       new org.jboss.jca.core.api.management.ManagedConnectionFactory(mcf);

                                    mgtMcf.getConfigProperties().
                                       addAll(createManagementView(cdMeta.getConfigProperties()));
                                    mgtMcf.setPoolConfiguration(pc);
                                    mgtMcf.setPool(pool);

                                    mgtConnector.getManagedConnectionFactories().add(mgtMcf);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            failures = initActivationSpec(cl, cmd, resourceAdapter, archiveValidationObjects, beanValidationObjects,
                                          failures, url, activateDeployment);

            failures = initAdminObject(cmd, cl, archiveValidationObjects, beanValidationObjects, failures, url,
               deploymentName, activateDeployment, raxml != null ? raxml.getAdminObjects() : null, ijmd != null
                  ? ijmd.getAdminObjects() : null, aos, aoJndiNames, mgtConnector);
         }

         // Archive validation
         failures = validateArchive(url, archiveValidationObjects, failures);

         if ((getConfiguration().getArchiveValidationFailOnWarn() && hasFailuresLevel(failures, Severity.WARNING)) ||
             (getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures, null), failures);
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
         }

         if (cmd != null)
         {
            try
            {
               // Register with MDR
               registerResourceAdapterToMDR(url, root, cmd, ijmd);
            }
            catch (AlreadyExistsException e)
            {
               //ignore it, RA already registered
            }
         }

         if (activateDeployment)
         {
            // Bean validation
            if (getConfiguration().getBeanValidation())
            {
               List<Class> groupsClasses = null;

               if (ijmd != null && ijmd.getBeanValidationGroups() != null &&
                   ijmd.getBeanValidationGroups().size() > 0)
               {
                  groupsClasses = new ArrayList<Class>();
                  for (String group : ijmd.getBeanValidationGroups())
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
            {
               String bootstrapIdentifier = null;
               if (raxml != null && raxml.getBootstrapContext() != null &&
                   !raxml.getBootstrapContext().trim().equals(""))
               {
                  bootstrapIdentifier = raxml.getBootstrapContext();
               }

               if (bootstrapIdentifier == null && ijmd != null && ijmd.getBootstrapContext() != null &&
                   !ijmd.getBootstrapContext().trim().equals(""))
               {
                  bootstrapIdentifier = ijmd.getBootstrapContext();
               }

               startContext(resourceAdapter, bootstrapIdentifier);
            }
         }

         if (activateDeployment)
         {
            log.info("Deployed: " + url.toExternalForm());
         }
         else
         {
            log.debug("Activated: " + url.toExternalForm());
         }

         return new CommonDeployment(url, deploymentName, activateDeployment, resourceAdapter, cfs, cfJndiNames,
                                     aos, aoJndiNames, mgtConnector, cl, log);

      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         if ((getConfiguration().getArchiveValidationFailOnWarn() && hasFailuresLevel(failures, Severity.WARNING)) ||
             (getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new DeployException("Deployment " + url.toExternalForm() + " failed",
                                      new ValidatorException(printFailuresLog(url.getPath(), new Validator(),
                                         failures, null), failures));
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
            throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
         }
      }
   }

   protected abstract SubjectFactory getSubjectFactory(String securityDomain);

   /**
    * Get management views for config property's
    * @param cps The config property's
    * @return The management view of these
    */
   private List<org.jboss.jca.core.api.management.ConfigProperty> createManagementView(
      List<? extends ConfigProperty> cps)
   {
      List<org.jboss.jca.core.api.management.ConfigProperty> result =
         new ArrayList<org.jboss.jca.core.api.management.ConfigProperty>();

      if (cps != null)
      {
         for (ConfigProperty cp : cps)
         {
            org.jboss.jca.core.api.management.ConfigProperty mgtCp = null;

            if (cp instanceof org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16)
            {
               org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16 cp16 =
                  (org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16)cp;

               Boolean dynamic = cp16.getConfigPropertySupportsDynamicUpdates();
               if (dynamic == null)
                  dynamic = Boolean.FALSE;

               Boolean confidential = cp16.getConfigPropertyConfidential();
               if (confidential == null)
                  confidential = Boolean.FALSE;

               mgtCp =
                  new org.jboss.jca.core.api.management.ConfigProperty(
                     cp.getConfigPropertyName().getValue(),
                     dynamic.booleanValue(), confidential.booleanValue());
            }
            else
            {
               mgtCp =
                  new org.jboss.jca.core.api.management.ConfigProperty(
                     cp.getConfigPropertyName().getValue());
            }

            result.add(mgtCp);
         }
      }

      return result;
   }

   /**
    *
    * get The directory where write error reports
    *
    * @return the directory as {@link File}
    */
   protected abstract File getReportDirectory();

   /**
    *
    * Register the ResourceAdapter to the MDR. Implementer should provide the implementation to get MDR and do the
    *  registration
    *
    * @param url url
    * @param root root
    * @param cmd cmd
    * @param ijmd ijmd
    * @throws AlreadyExistsException AlreadyExistsException
    */
   protected abstract void registerResourceAdapterToMDR(URL url, File root, Connector cmd, IronJacamar ijmd)
      throws AlreadyExistsException;

   /**
    *
    * get The transaction Manager. Implementers have to provide right implementation to find and get it
    *
    * @return the transaction manager to be used
    */
   protected abstract TransactionManager getTransactionManager();

   /**
    *
    * get a PrintWriter where logger will put its output
    *
    * @return the printWriter for Logger
    */
   protected abstract PrintWriter getLogPrintWriter();

   /**
    * Bind connection factory into JNDI
    * @param url The deployment URL
    * @param deploymentName The deployment name
    * @param cf The connection factory
    * @return The JNDI names bound
    * @exception Throwable Thrown if an error occurs
    */
   protected abstract String[] bindConnectionFactory(URL url, String deploymentName, Object cf) throws Throwable;

   /**
    * Bind connection factory into JNDI
    * @param url The deployment URL
    * @param deploymentName The deployment name
    * @param cf The connection factory
    * @param jndiName The JNDI name
    * @return The JNDI names bound
    * @exception Throwable Thrown if an error occurs
    */
   protected abstract String[] bindConnectionFactory(URL url, String deploymentName, Object cf, String jndiName)
      throws Throwable;

   /**
    * Bind admin object into JNDI
    * @param url The deployment URL
    * @param deploymentName The deployment name
    * @param ao The admin object
    * @return The JNDI names bound
    * @exception Throwable Thrown if an error occurs
    */
   protected abstract String[] bindAdminObject(URL url, String deploymentName, Object ao) throws Throwable;

   /**
    * Bind admin object into JNDI
    * @param url The deployment URL
    * @param deploymentName The deployment name
    * @param ao The admin object
    * @param jndiName The JNDI name
    * @return The JNDI names bound
    * @exception Throwable Thrown if an error occurs
    */
   protected abstract String[] bindAdminObject(URL url, String deploymentName, Object ao, String jndiName)
      throws Throwable;

   /**
    * check if the configuration for this deployer has been set to a valid value
    *
    * @return false if configuration is not valid
    */
   protected abstract boolean checkConfigurationIsValid();

   /**
    * Check if the resource adapter should be activated based on the ironjacamar.xml input
    * @param cmd cmd cmd The connector metadata
    * @param ijmd ijmd ijmd The IronJacamar metadata
    * @return True if the deployment should be activated; otherwise false
    */
   protected abstract boolean checkActivation(Connector cmd, IronJacamar ijmd);

   /**
    * Initialize and inject configuration properties into container
    * @param value value
    * @param cpm confi properties
    * @param cl  The class loader
    * @return The object
    * @throws DeployException DeployException Thrown if the object cant be initialized
    */
   protected abstract Object initAndInject(String value, List<? extends ConfigProperty> cpm, ClassLoader cl)
      throws DeployException;

}
