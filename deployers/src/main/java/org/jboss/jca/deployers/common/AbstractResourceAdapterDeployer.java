/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2010, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.Security;
import org.jboss.jca.common.api.metadata.common.SecurityMetadata;
import org.jboss.jca.common.api.metadata.common.TimeOut;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.metadata.common.XaPool;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.resourceadapter.WorkManagerSecurity;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.MessageListener;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.spec.ConfigPropertyImpl;
import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.bootstrapcontext.BootstrapContextCoordinator;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.pool.api.PrefillPool;
import org.jboss.jca.core.connectionmanager.pool.capacity.CapacityFactory;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory;
import org.jboss.jca.core.recovery.DefaultRecoveryPlugin;
import org.jboss.jca.core.security.CallbackImpl;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;
import org.jboss.jca.core.spi.security.Callback;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.XAResourceStatistics;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;
import org.jboss.jca.core.util.Injection;
import org.jboss.jca.deployers.DeployersBundle;
import org.jboss.jca.deployers.DeployersLogger;
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
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.resource.spi.work.WorkContext;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.jboss.logging.Messages;

/**
 * An abstract resource adapter deployer which contains common functionality
 * for all resource adapter archive based deployers.
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractResourceAdapterDeployer
{
   /** The bundle */
   private static DeployersBundle bundle = Messages.getBundle(DeployersBundle.class);

   /** the logger **/
   protected final DeployersLogger log;

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
    */
   public AbstractResourceAdapterDeployer(boolean validateClasses)
   {
      super();
      this.log = getLogger();
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
    * or if validation is not run according to {@link Configuration#getArchiveValidation()} Setting. It returns null
    * also if the concrete implementation of this class set validateClasses instance variable to flase and the list of
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
               log.validationReportFailure(ioe.getMessage(), ioe);
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
    * @param resourceAdapter The resource adapter
    * @param bootstrapContextIdentifier The bootstrap context identifier
    * @param bootstrapContextName The bootstrap context name; may be <code>null</code>
    * @param cb The callback
    * @throws DeployException DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   protected void startContext(javax.resource.spi.ResourceAdapter resourceAdapter,
                               String bootstrapContextIdentifier, String bootstrapContextName,
                               Callback cb)
      throws DeployException
   {
      try
      {
         CloneableBootstrapContext cbc = 
            BootstrapContextCoordinator.getInstance().createBootstrapContext(bootstrapContextIdentifier,
                                                                             bootstrapContextName);

         cbc.setResourceAdapter(resourceAdapter);

         if (cb != null)
            setCallbackSecurity((org.jboss.jca.core.api.workmanager.WorkManager)cbc.getWorkManager(), cb);

         resourceAdapter.start(cbc);
      }
      catch (Throwable t)
      {
         throw new DeployException(bundle.unableToStartResourceAdapter(resourceAdapter.getClass().getName()), t);
      }
   }

   /**
    * Sets the call back security info in this rar work manager before starting the resource adapter.
    *
    * @param workManager the work manager that will be used by the resource adapter
    * @param cb          the security callback
    */
   protected void setCallbackSecurity(org.jboss.jca.core.api.workmanager.WorkManager workManager, Callback cb)
   {
      workManager.setCallbackSecurity(cb);
   }

   /**
    * Associate resource adapter with ojects if they implement ResourceAdapterAssociation
    * @param resourceAdapter resourceAdapter resourceAdapter The resource adapter
    * @param object object object The of possible association object
    * @throws DeployException DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   protected void associateResourceAdapter(javax.resource.spi.ResourceAdapter resourceAdapter, Object object)
      throws DeployException
   {
      if (resourceAdapter != null && object != null)
      {
         if (object instanceof ResourceAdapterAssociation)
         {
            try
            {
               ResourceAdapterAssociation raa = (ResourceAdapterAssociation)object;
               raa.setResourceAdapter(resourceAdapter);
            }
            catch (Throwable t)
            {
               throw new DeployException(bundle.unableToAssociate(object.getClass().getName()), t);
            }
         }
      }
   }

   /**
    * Return a list of ManagedConnectionFactory classes
    * @param ra The metadata
    * @return The classes
    */
   private Set<String> findManagedConnectionFactories(org.jboss.jca.common.api.metadata.spec.ResourceAdapter ra)
   {
      Set<String> result = new HashSet<String>(1);

      if (ra != null)
      {
         if (ra.getOutboundResourceadapter() != null)
         {
            for (org.jboss.jca.common.api.metadata.spec.ConnectionDefinition cd : 
                    ra.getOutboundResourceadapter().getConnectionDefinitions())
            {
               result.add(cd.getManagedConnectionFactoryClass().getValue());
            }
         }
      }

      return result;
   }

   /**
    * Return a list of AdminObject classes
    * @param ra The metadata
    * @return The classes
    */
   private Set<String> resolveAdminObjects(org.jboss.jca.common.api.metadata.spec.ResourceAdapter ra)
   {
      Set<String> result = new HashSet<String>(1);

      if (ra != null)
      {
         if (ra.getAdminObjects() != null)
         {
            for (org.jboss.jca.common.api.metadata.spec.AdminObject ao : ra.getAdminObjects())
            {
               result.add(ao.getAdminobjectClass().getValue());
            }
         }
      }

      return result;
   }

   /**
    * Find the metadata for a managed connection factory
    * @param clz The fully quilified class name for the managed connection factory
    * @param mcfs The managed connection facotries
    * @param defs The connection definitions
    * @param cl The class loader
    * @return The metadata; <code>null</code> if none could be found
    * @exception DeployException Thrown in case of configuration error
    */
   protected Set<ConnectionDefinition> findConnectionDefinitions(String clz, Set<String> mcfs,
                                                                 List<ConnectionDefinition> defs,
                                                                 ClassLoader cl)
      throws DeployException
   {
      Set<ConnectionDefinition> result = null;

      if (mcfs != null && defs != null)
      {
         // If there is only one we will return that
         if (mcfs.size() == 1 && defs.size() == 1)
         {
            ConnectionDefinition cd = defs.get(0);

            if (cd.getClassName() != null && !clz.equals(cd.getClassName()))
            {
               log.connectionDefinitionMismatch(cd.getClassName());
               throw new DeployException(clz + " not a valid connection definition");
            }

            boolean add = true;
            if (cd.getClassName() != null)
            {
               if (!verifyManagedConnectionFactory(cd.getClassName(), cl))
               {
                  log.connectionDefinitionInvalid(cd.getClassName());
                  add = false;
               }
            }

            if (add)
            {
               result = new HashSet<ConnectionDefinition>(1);
               result.add(cd);

               return result;
            }
         }

         // If there are multiple definitions the MCF class name is mandatory
         if (clz == null)
            throw new IllegalArgumentException(bundle.undefinedManagedConnectionFactory());

         for (ConnectionDefinition cd : defs)
         {
            if (cd.getClassName() == null)
            {
               log.connectionDefinitionNull();
            }
            else
            {
               if (clz.equals(cd.getClassName()))
               {
                  if (result == null)
                     result = new HashSet<ConnectionDefinition>();

                  result.add(cd);
               }
               else
               {
                  if (!verifyManagedConnectionFactory(cd.getClassName(), cl))
                     log.connectionDefinitionInvalid(cd.getClassName());
               }
            }
         }
      }

      return result;
   }

   /**
    * Verify the MCF definition
    * @param clz The class name
    * @param cl The class loader
    * @return True if MCF, otherwise false
    */
   private boolean verifyManagedConnectionFactory(String clz, ClassLoader cl)
   {
      if (clz != null)
      {
         try
         {
            Class<?> c = Class.forName(clz, true, cl);
            if (ManagedConnectionFactory.class.isAssignableFrom(c))
               return true;
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }

      return false;
   }

   /**
    * Verify a class definition
    * @param clz The class name
    * @param cl The class loader
    * @return True if found, otherwise false
    */
   private boolean verifyClass(String clz, ClassLoader cl)
   {
      if (clz != null)
      {
         try
         {
            Class<?> c = Class.forName(clz, true, cl);
            return true;
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }

      return false;
   }

   /**
    * Verify that a class implements a certain interface
    * @param interfaceClz The interface class name
    * @param implClz The implementation class name
    * @param cl The class loader
    * @return True if correct, otherwise false
    */
   private boolean verifyInstance(String interfaceClz, String implClz, ClassLoader cl)
   {
      if (interfaceClz != null && implClz != null)
      {
         try
         {
            Class<?> interfaceDef = Class.forName(interfaceClz, true, cl);
            Class<?> implDef = Class.forName(implClz, true, cl);

            return interfaceDef.isAssignableFrom(implDef);
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }

      return false;
   }

   /**
    * Find the metadata for an admin object
    * @param clz The fully quilified class name for the admin object
    * @param aos The admin object classes
    * @param defs The admin object definitions
    * @return The metadata; <code>null</code> if none could be found
    * @exception DeployException Thrown in case of configuration error
    */
   protected Set<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject> findAdminObjects(String clz,
      Set<String> aos,
      List<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject> defs)
      throws DeployException
   {
      Set<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject> result = null;

      if (aos != null && defs != null)
      {
         // If there is only one we will return that
         if (aos.size() == 1 && defs.size() == 1)
         {
            org.jboss.jca.common.api.metadata.resourceadapter.AdminObject cao = defs.get(0);

            if (cao.getClassName() != null && !clz.equals(cao.getClassName()))
            {
               log.adminObjectMismatch(cao.getClassName());
               throw new DeployException(clz + " not a valid admin object");
            }

            result = new HashSet<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject>(1);
            result.add(cao);

            return result;
         }

         // If there are multiple definitions the admin object class name is mandatory
         if (clz == null)
            throw new IllegalArgumentException(bundle.undefinedAdminObject());

         for (org.jboss.jca.common.api.metadata.resourceadapter.AdminObject cao : defs)
         {
            if (cao.getClassName() == null)
            {
               log.adminObjectNull();
            }
            else
            {
               if (clz.equals(cao.getClassName()))
               {
                  if (result == null)
                     result = new HashSet<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject>();

                  result.add(cao);
               }
            }
         }
      }

      return result;
   }

   /**
    * Create an instance of the pool configuration based on the input
    * @param pp The pool parameters
    * @param tp The timeout parameters
    * @param vp The validation parameters
    * @return The configuration
    */
   protected PoolConfiguration createPoolConfiguration(org.jboss.jca.common.api.metadata.common.Pool pp,
                                                       TimeOut tp, Validation vp)
   {
      PoolConfiguration pc = new PoolConfiguration();

      if (pp != null)
      {
         if (pp.getMinPoolSize() != null)
            pc.setMinSize(pp.getMinPoolSize().intValue());

         if (pp.getInitialPoolSize() != null)
            pc.setInitialSize(pp.getInitialPoolSize().intValue());

         if (pp.getMaxPoolSize() != null)
            pc.setMaxSize(pp.getMaxPoolSize().intValue());

         if (pp.isPrefill() != null)
            pc.setPrefill(pp.isPrefill());

         if (pp.isUseStrictMin() != null)
            pc.setStrictMin(pp.isUseStrictMin());

         if (pp.isFair() != null)
            pc.setFair(pp.isFair());
      }

      if (tp != null)
      {
         if (tp.getBlockingTimeoutMillis() != null)
            pc.setBlockingTimeout(tp.getBlockingTimeoutMillis().longValue());

         if (tp.getIdleTimeoutMinutes() != null)
            pc.setIdleTimeoutMinutes(tp.getIdleTimeoutMinutes().intValue());
      }

      if (vp != null)
      {
         if (vp.isValidateOnMatch() != null)
            pc.setValidateOnMatch(vp.isValidateOnMatch().booleanValue());

         if (vp.isBackgroundValidation() != null)
            pc.setBackgroundValidation(vp.isBackgroundValidation().booleanValue());

         if (vp.getBackgroundValidationMillis() != null)
            pc.setBackgroundValidationMillis(vp.getBackgroundValidationMillis().intValue());

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
   protected Set<Failure> initActivationSpec(ClassLoader cl, Connector cmd,
                                             javax.resource.spi.ResourceAdapter resourceAdapter,
                                             List<Validate> archiveValidationObjects,
                                             List<Object> beanValidationObjects,
                                             Set<Failure> failures, URL url,
                                             boolean activateDeployment)
      throws DeployException
   {
      // ActivationSpec
      if (cmd.getVersion() != Version.V_10)
      {
         org.jboss.jca.common.api.metadata.spec.ResourceAdapter raSpec = cmd.getResourceadapter();
         if (raSpec != null && raSpec.getInboundResourceadapter() != null &&
             raSpec.getInboundResourceadapter().getMessageadapter() != null &&
             raSpec.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
         {
            List<MessageListener> mlMetas = raSpec.getInboundResourceadapter().getMessageadapter()
               .getMessagelisteners();

            if (mlMetas.size() > 0)
            {
               for (MessageListener mlMD : mlMetas)
               {
                  if (mlMD.getActivationspec() != null &&
                      mlMD.getActivationspec().getActivationspecClass().getValue() != null)
                  {
                     List<ConfigProperty> asCps = null;
                     if (mlMD.getActivationspec().getConfigProperties() != null)
                     {
                        asCps = mlMD.getActivationspec().getConfigProperties();
                     }

                     failures = validateArchive(
                        url,
                        Arrays.asList((Validate) new ValidateClass(Key.ACTIVATION_SPEC, mlMD.getActivationspec()
                           .getActivationspecClass().getValue(), cl, asCps)), failures);
                     if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                        Severity.ERROR)))
                     {
                        if (activateDeployment)
                        {
                           String asClass = mlMD.getActivationspec().getActivationspecClass().getValue();

                           Object oa = initAndInject(asClass, asCps, cl);

                           if (oa == null || !(oa instanceof ActivationSpec))
                              throw new DeployException(bundle.invalidActivationSpec(asClass));

                           ActivationSpec as = (ActivationSpec)oa;

                           if (log.isTraceEnabled())
                           {
                              log.tracef("ActivationSpec: %s", as.getClass().getName());
                              log.tracef("ActivationSpec defined in classloader: %s",
                                        SecurityActions.getClassLoader(as.getClass()));
                           }

                           // Associate for validation
                           associateResourceAdapter(resourceAdapter, as);

                           archiveValidationObjects.add(new ValidateObject(Key.ACTIVATION_SPEC, as, asCps));
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
    * @param resourceAdapter The resource adapter instance
    * @param aosAct Admin object definitions from activation
    * @param aos The resulting array of admin objects
    * @param aoJndiNames The resulting array of JNDI names
    * @param mgtConnector The management view of the connector
    * @return failures updated after implemented operations
    * @throws DeployException DeployException in case of errors
    */
   protected Set<Failure> initAdminObject(Connector cmd, ClassLoader cl, List<Validate> archiveValidationObjects,
                                          List<Object> beanValidationObjects, Set<Failure> failures,
                                          URL url, String deploymentName, boolean activateDeployment,
                                          javax.resource.spi.ResourceAdapter resourceAdapter,
                                          List<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject> aosAct,
                                          List<Object> aos, List<String> aoJndiNames,
                                          org.jboss.jca.core.api.management.Connector mgtConnector)
      throws DeployException
   {
      // AdminObject
      if (cmd.getVersion() != Version.V_10)
      {
         org.jboss.jca.common.api.metadata.spec.ResourceAdapter raSpec = cmd.getResourceadapter();
         if (raSpec != null && raSpec.getAdminObjects() != null)
         {
            List<org.jboss.jca.common.api.metadata.spec.AdminObject> aoMetas = raSpec.getAdminObjects();
            if (aoMetas.size() > 0)
            {
               Set<String> aosClz = resolveAdminObjects(raSpec);
               Set<String> processedAos = new HashSet<String>();

               for (int i = 0; i < aoMetas.size(); i++)
               {
                  org.jboss.jca.common.api.metadata.spec.AdminObject aoMeta = aoMetas.get(i);

                  log.debugf("Activating: %s", aoMeta);

                  if (aoMeta.getAdminobjectClass() != null && aoMeta.getAdminobjectClass().getValue() != null)
                  {
                     String aoClz = aoMeta.getAdminobjectClass().getValue();

                     if (processedAos.contains(aoClz))
                        continue;

                     processedAos.add(aoClz);

                     failures = validateArchive(url,
                        Arrays.asList((Validate) new ValidateClass(Key.ADMIN_OBJECT, aoClz, cl,
                                                                   aoMeta.getConfigProperties())), failures);

                     if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                        Severity.ERROR)))
                     {
                        Set<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject> adminObjects = null;

                        if (aosAct != null)
                           adminObjects = findAdminObjects(aoClz, aosClz, aosAct);

                        if (!requireExplicitJndiBindings() && aosAct == null && aosClz.size() == 1)
                        {
                           adminObjects = new HashSet<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject>(1);
                           adminObjects.add(null);
                        }

                        if (activateDeployment && adminObjects != null)
                        {
                           Injection injector = new Injection();

                           for (org.jboss.jca.common.api.metadata.resourceadapter.AdminObject adminObject :
                                   adminObjects)
                           {
                              Object ao = initAndInject(aoClz, aoMeta.getConfigProperties(), cl);

                              if (adminObject != null &&
                                  adminObject.getConfigProperties() != null &&
                                  adminObject.getConfigProperties().size() > 0)
                              {
                                 Iterator<Map.Entry<String, String>> it =
                                    adminObject.getConfigProperties().entrySet().iterator();

                                 while (it.hasNext())
                                 {
                                    Map.Entry<String, String> entry = it.next();
                                    
                                    try
                                    {
                                       injector.inject(ao, entry.getKey(), entry.getValue());
                                    }
                                    catch (Throwable t)
                                    {
                                       throw new DeployException(bundle.unableToInject(ao.getClass().getName(),
                                                                                       entry.getKey(),
                                                                                       entry.getValue()));
                                    }
                                 }
                              }

                              associateResourceAdapter(resourceAdapter, ao);

                              if (log.isTraceEnabled())
                              {
                                 log.tracef("AdminObject: %s", ao.getClass().getName());
                                 log.tracef("AdminObject defined in classloader: %s",
                                           SecurityActions.getClassLoader(ao.getClass()));
                              }

                              archiveValidationObjects.add(new ValidateObject(Key.ADMIN_OBJECT, ao, aoMeta
                                                                              .getConfigProperties()));
                              beanValidationObjects.add(ao);

                              if (ao != null)
                              {
                                 boolean adminObjectBound = false;
                                 boolean adminObjectVerified = false;

                                 if (ao instanceof ResourceAdapterAssociation)
                                 {
                                    if (ao instanceof Serializable &&
                                        ao instanceof javax.resource.Referenceable)
                                    {
                                       adminObjectVerified = true;
                                    }
                                 }
                                 else
                                 {
                                    if (!(ao instanceof javax.naming.Referenceable))
                                    {
                                       DelegatorInvocationHandler dih = new DelegatorInvocationHandler(ao);

                                       List<Class<?>> interfaces = new ArrayList<Class<?>>();
                                       Class<?> clz = ao.getClass();
                                       while (!clz.equals(Object.class))
                                       {
                                          Class<?>[] is = clz.getInterfaces();
                                          if (is != null)
                                          {
                                             for (Class<?> interfaceClass : is)
                                             {
                                                if (!interfaceClass.equals(javax.resource.Referenceable.class) &&
                                                    !interfaceClass.equals(ResourceAdapterAssociation.class) &&
                                                    !interfaceClass.equals(java.io.Serializable.class) &&
                                                    !interfaceClass.equals(java.io.Externalizable.class))
                                                {
                                                   if (!interfaces.contains(interfaceClass))
                                                      interfaces.add(interfaceClass);
                                                }
                                             }
                                          }

                                          clz = clz.getSuperclass();
                                       }

                                       interfaces.add(java.io.Serializable.class);
                                       interfaces.add(javax.resource.Referenceable.class);
                                       
                                       ao = Proxy.newProxyInstance(SecurityActions.getClassLoader(ao.getClass()),
                                                                   interfaces.toArray(new Class<?>[interfaces.size()]),
                                                                   dih);
                                    }

                                    adminObjectVerified = true;
                                 }

                                 if (adminObjectVerified)
                                 {
                                    try
                                    {
                                       String jndiName = null;
                                       if (adminObject != null)
                                       {
                                          jndiName = buildJndiName(adminObject.getJndiName(),
                                                                   adminObject.isUseJavaContext());

                                          bindAdminObject(url, deploymentName, ao, jndiName);
                                       }
                                       else
                                       {
                                          String[] names = bindAdminObject(url, deploymentName, ao);
                                          jndiName = names[0];
                                       }
                                       
                                       aos.add(ao);
                                       aoJndiNames.add(jndiName);
                                       adminObjectBound = true;

                                       org.jboss.jca.core.api.management.AdminObject mgtAo =
                                          new org.jboss.jca.core.api.management.AdminObject(ao);

                                       mgtAo.getConfigProperties().
                                          addAll(createManagementView(aoMeta.getConfigProperties()));
                                       mgtAo.setJndiName(jndiName);
                                       
                                       mgtConnector.getAdminObjects().add(mgtAo);
                                    }
                                    catch (Throwable t)
                                    {
                                       throw new 
                                          DeployException(bundle.failedToBindAdminObject(ao.getClass().getName()), t);
                                    }
                                 }

                                 if (!adminObjectBound)
                                 {
                                    log.adminObjectNotBound(aoClz);
                                    log.adminObjectNotSpecCompliant(aoClz);
                                 }
                              }
                           }
                        }
                        else
                        {
                           log.debugf("No activation: %s", aoClz);
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
    * Load native libraries
    * @param root The deployment root
    */
   private void loadNativeLibraries(File root)
   {
      if (root != null && root.exists())
      {
         List<String> libs = null;

         if (root.isDirectory())
         {
            if (root.listFiles() != null)
            {
               for (File f : root.listFiles())
               {
                  String fileName = f.getName().toLowerCase(Locale.US);
                  if (fileName.endsWith(".a") || fileName.endsWith(".so") || fileName.endsWith(".dll"))
                  {
                     if (libs == null)
                        libs = new ArrayList<String>();

                     libs.add(f.getAbsolutePath());
                  }
               }
            }
            else
            {
               log.debugf("Root is a directory, but there were an I/O error: %s", root.getAbsolutePath());
            }
         }
         else
         {
            JarFile jarFile = null;
            try
            {
               jarFile = new JarFile(root);
               Enumeration<JarEntry> entries = jarFile.entries();

               while (entries.hasMoreElements())
               {
                  JarEntry jarEntry = entries.nextElement();
                  String entryName = jarEntry.getName().toLowerCase(Locale.US);
                  if (entryName.endsWith(".a") || entryName.endsWith(".so") || entryName.endsWith(".dll"))
                  {
                     if (libs == null)
                        libs = new ArrayList<String>();

                     libs.add(jarEntry.getName());
                  }
               }
            }
            catch (Throwable t)
            {
               log.debugf("Unable to load native libraries from: %s", root.getAbsolutePath());
            }
            finally
            {
               if (jarFile != null)
               {
                  try
                  {
                     jarFile.close();
                  }
                  catch (IOException ioe)
                  {
                     // Ignore
                  }
               }
            }
         }

         if (libs != null)
         {
            for (String lib : libs)
            {
               try
               {
                  SecurityActions.load(lib);
                  log.debugf("Loaded library: %s", lib);
               }
               catch (Throwable t)
               {
                  log.debugf("Unable to load library: %s", lib);
               }
            }
         }
      }
   }

   /**
    * create objects and inject value for this deployment. it is a general method returning a {@link CommonDeployment}
    * to be used to exchange objects needed to real injection in the container
    *
    * @param url url
    * @param deploymentName deploymentName
    * @param root root
    * @param cl cl
    * @param cmd connector md
    * @param activation activation md
    * @return return the exchange POJO with value useful for injection in the container (fungal or AS)
    * @throws DeployException DeployException
    * @throws ResourceException ResourceException
    * @throws ValidatorException ValidatorException
    * @throws org.jboss.jca.core.spi.mdr.AlreadyExistsException AlreadyExistsException
    * @throws ClassNotFoundException ClassNotFoundException
    * @throws Throwable Throwable
    */
   protected CommonDeployment createObjectsAndInjectValue(URL url, String deploymentName, File root, ClassLoader cl,
                                                          Connector cmd, Activation activation)
      throws DeployException, ResourceException, ValidatorException,
      org.jboss.jca.core.spi.mdr.AlreadyExistsException,
             ClassNotFoundException, Throwable
   {
      Set<Failure> failures = null;
      try
      {
         // Notify regarding license terms
         if (cmd != null && cmd.getLicense() != null && cmd.getLicense().isLicenseRequired())
            log.requiredLicenseTerms(url.toExternalForm());

         String mgtUniqueId = url.getFile();
         if (mgtUniqueId.indexOf("/") != -1)
            mgtUniqueId = mgtUniqueId.substring(mgtUniqueId.lastIndexOf("/") + 1);

         org.jboss.jca.core.api.management.Connector mgtConnector =
            new org.jboss.jca.core.api.management.Connector(mgtUniqueId);

         javax.resource.spi.ResourceAdapter resourceAdapter = null;
         Map<String, String> raConfigProperties = null;
         String resourceAdapterKey = null;
         String bootstrapContextIdentifier = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Object> beanValidationObjects = new ArrayList<Object>();
         List<Object> cfs = new ArrayList<Object>();
         List<String> cfJndiNames = new ArrayList<String>();
         List<ConnectionManager> cfCMs = new ArrayList<ConnectionManager>();
         List<Object> aos = new ArrayList<Object>();
         List<String> aoJndiNames = new ArrayList<String>();
         List<XAResourceRecovery> recoveryModules = new ArrayList<XAResourceRecovery>(1);
         Callback callback = null;
         boolean isXA = false;

         // Check metadata for JNDI information and activate explicit
         boolean activateDeployment = checkActivation(cmd, activation);

         if (log.isTraceEnabled())
         {
            log.tracef("Connector=%s", cmd);
            log.tracef("Activation=%s", stripPassword(activation != null ? activation.toString() : ""));
            log.tracef("ActivateDeployment=%s", activateDeployment);
         }

         // Load native libraries
         if (activateDeployment)
            loadNativeLibraries(root);

         // Setup WorkManager security
         if (activation != null && activation.getWorkManager() != null &&
             activation.getWorkManager().getSecurity() != null)
         {
            callback = createCallback(activation.getWorkManager().getSecurity());
         }

         // Create objects and inject values
         if (cmd != null)
         {
            org.jboss.jca.common.api.metadata.spec.ResourceAdapter ra = cmd.getResourceadapter();

            // ResourceAdapter
            if (cmd.getVersion() != Version.V_10)
            {
               if (cmd.getVersion() == Version.V_16 || cmd.getVersion() == Version.V_17)
               {
                  if (cmd.getRequiredWorkContexts() != null && cmd.getRequiredWorkContexts().size() > 0)
                  {
                     CloneableBootstrapContext bc =
                        BootstrapContextCoordinator.getInstance().getDefaultBootstrapContext();
                     for (String requiredWorkContext : cmd.getRequiredWorkContexts())
                     {
                        try
                        {
                           Class<? extends WorkContext> rwc = 
                              (Class<? extends WorkContext>)Class.forName(requiredWorkContext, true, cl);

                           if (!bc.isContextSupported(rwc))
                           {
                              throw new DeployException(bundle.invalidRequiredWorkContext(requiredWorkContext));
                           }
                        }
                        catch (Throwable t)
                        {
                           throw new DeployException(bundle.invalidRequiredWorkContext(requiredWorkContext), t);
                        }
                     }
                  }
               }

               if (ra != null && ra.getResourceadapterClass() != null)
               {
                  failures = validateArchive(url,
                     Arrays.asList((Validate) new ValidateClass(Key.RESOURCE_ADAPTER, ra
                        .getResourceadapterClass(), cl, cmd.getResourceadapter().getConfigProperties())), failures);

                  if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                     Severity.ERROR)))
                  {
                     if (activateDeployment)
                     {
                        String raClz = ra.getResourceadapterClass();
                        Object or = initAndInject(raClz, ra.getConfigProperties(), cl);

                        if (or == null || !(or instanceof javax.resource.spi.ResourceAdapter))
                           throw new DeployException(bundle.invalidResourceAdapter(raClz));

                        resourceAdapter = (javax.resource.spi.ResourceAdapter)or;

                        if (activation != null)
                           raConfigProperties = activation.getConfigProperties();

                        if (raConfigProperties != null)
                        {
                           Injection injector = new Injection();
                           Iterator<Map.Entry<String, String>> it = raConfigProperties.entrySet().iterator();

                           while (it.hasNext())
                           {
                              Map.Entry<String, String> entry = it.next();
                                    
                              try
                              {
                                 injector.inject(resourceAdapter, entry.getKey(), entry.getValue());
                              }
                              catch (Throwable t)
                              {
                                 throw new DeployException(bundle.unableToInject(resourceAdapter.getClass().getName(),
                                                                                 entry.getKey(),
                                                                                 entry.getValue()));
                              }
                           }
                        }

                        if (log.isTraceEnabled())
                        {
                           log.tracef("ResourceAdapter: %s", resourceAdapter.getClass().getName());
                           log.tracef("ResourceAdapter defined in classloader: %s",
                                     SecurityActions.getClassLoader(resourceAdapter.getClass()));
                        }

                        archiveValidationObjects.add(new ValidateObject(Key.RESOURCE_ADAPTER, resourceAdapter, ra
                           .getConfigProperties()));
                        beanValidationObjects.add(resourceAdapter);

                        if (activateDeployment)
                        {
                           org.jboss.jca.core.api.management.ResourceAdapter mgtRa =
                              new org.jboss.jca.core.api.management.ResourceAdapter(resourceAdapter);

                           mgtRa.getConfigProperties().addAll(createManagementView(ra.getConfigProperties()));
                           mgtConnector.setResourceAdapter(mgtRa);
                        }
                     }
                  }
               }
            }

            if (ra != null && ra.getOutboundResourceadapter() != null &&
                ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
            {
               List<org.jboss.jca.common.api.metadata.spec.ConnectionDefinition> cdMetas =
                  ra.getOutboundResourceadapter().getConnectionDefinitions();
               if (cdMetas.size() > 0)
               {
                  Set<String> mcfs = findManagedConnectionFactories(ra);
                  Set<String> processedMcfs = new HashSet<String>();

                  for (int cdIndex = 0; cdIndex < cdMetas.size(); cdIndex++)
                  {
                     org.jboss.jca.common.api.metadata.spec.ConnectionDefinition cdMeta = cdMetas.get(cdIndex);
                     String mcfClz = cdMeta.getManagedConnectionFactoryClass().getValue();
                        
                     if (processedMcfs.contains(mcfClz))
                        continue;

                     processedMcfs.add(mcfClz);

                     log.debugf("CdMeta: %s", cdMeta);

                     failures = validateArchive(url,
                        Arrays.asList((Validate) new ValidateClass(Key.MANAGED_CONNECTION_FACTORY, mcfClz,
                                                                   cl, cdMeta.getConfigProperties())),
                                                failures);

                     if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                                                                                                    Severity.ERROR)))
                     {
                        Set<ConnectionDefinition> connectionDefinitions = null;

                        if (activation != null)
                        {
                           List<ConnectionDefinition> cdDefs = activation.getConnectionDefinitions();
                           
                           if (cdDefs != null)
                           {
                              connectionDefinitions =
                                 findConnectionDefinitions(mcfClz, mcfs, cdDefs, cl);
                           }
                        }

                        if (!requireExplicitJndiBindings() && activation == null && mcfs.size() == 1)
                        {
                           connectionDefinitions = new HashSet<ConnectionDefinition>(1);
                           connectionDefinitions.add(null);
                        }

                        if (activateDeployment && connectionDefinitions != null)
                        {
                           log.debugf("ConnectionDefinitions: %s", connectionDefinitions.size());
                              
                           String cfIntClz = cdMeta.getConnectionFactoryInterface().getValue();
                           if (!verifyClass(cfIntClz, cl))
                              throw new DeployException(bundle.invalidConnectionFactoryInterface(cfIntClz));
                  
                           String cfImplClz = cdMeta.getConnectionFactoryImplClass().getValue();
                           if (!verifyClass(cfImplClz, cl))
                              throw new DeployException(bundle.invalidConnectionFactoryImplementation(cfImplClz));

                           if (!verifyInstance(cfIntClz, cfImplClz, cl))
                              throw new DeployException(bundle.
                                 invalidConnectionFactoryImplementationDueToInterface(cfImplClz, cfIntClz));
                              
                           String cIntClz = cdMeta.getConnectionInterface().getValue();
                           if (!verifyClass(cIntClz, cl))
                              throw new DeployException(bundle.invalidConnectionInterface(cIntClz));
                              
                           String cImplClz = cdMeta.getConnectionImplClass().getValue();
                           if (!verifyClass(cImplClz, cl))
                              throw new DeployException(bundle.invalidConnectionImplementation(cImplClz));
                              
                           if (!verifyInstance(cIntClz, cImplClz, cl))
                              throw new 
                                 DeployException(bundle.invalidConnectionImplementationDueToInterface(cImplClz,
                                                                                                         cIntClz));

                           for (ConnectionDefinition connectionDefinition : connectionDefinitions)
                           {
                              log.debugf("Activating: %s", connectionDefinition);

                              if (connectionDefinition == null || connectionDefinition.isEnabled())
                              {
                                 Object om = initAndInject(mcfClz, cdMeta.getConfigProperties(), cl);

                                 if (om == null || !(om instanceof ManagedConnectionFactory))
                                    throw new DeployException(bundle.invalidManagedConnectionFactory(mcfClz));

                                 ManagedConnectionFactory mcf = (ManagedConnectionFactory)om;

                                 if (connectionDefinition != null &&
                                     connectionDefinition.getConfigProperties() != null)
                                 {
                                    Injection injector = new Injection();
                                    Iterator<Map.Entry<String, String>> it =
                                       connectionDefinition.getConfigProperties().entrySet().iterator();

                                    while (it.hasNext())
                                    {
                                       Map.Entry<String, String> entry = it.next();
                                          
                                       try
                                       {
                                          injector.inject(mcf, entry.getKey(), entry.getValue());
                                       }
                                       catch (Throwable t)
                                       {
                                          throw new DeployException(bundle.unableToInject(mcf.getClass().getName(),
                                                                                          entry.getKey(),
                                                                                          entry.getValue()));
                                       }
                                    }
                                 }

                                 if (log.isTraceEnabled())
                                 {
                                    log.tracef("ManagedConnectionFactory: %s", mcf.getClass().getName());
                                    log.tracef("ManagedConnectionFactory defined in classloader: %s",
                                              SecurityActions.getClassLoader(mcf.getClass()));
                                 }

                                 mcf.setLogWriter(getLogPrintWriter());

                                 archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY, mcf,
                                                                                    cdMeta.getConfigProperties()));
                                 beanValidationObjects.add(mcf);
                                 associateResourceAdapter(resourceAdapter, mcf);

                                 // Create the pool
                                 PoolConfiguration pc = null;
                                 FlushStrategy flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;

                                 if (connectionDefinition != null)
                                 {
                                    pc = createPoolConfiguration(connectionDefinition.getPool(),
                                                                 connectionDefinition.getTimeOut(),
                                                                 connectionDefinition.getValidation());

                                    if (connectionDefinition.getPool() != null)
                                       flushStrategy = connectionDefinition.getPool().getFlushStrategy();
                                 }
                                 else
                                 {
                                    // Default default settings
                                    pc = createPoolConfiguration(null, null, null);
                                 }

                                 // Check validation
                                 if (connectionDefinition != null)
                                 {
                                    if (connectionDefinition.getValidation() == null ||
                                        (connectionDefinition.getValidation().isValidateOnMatch() == null &&
                                         connectionDefinition.getValidation().isBackgroundValidation() == null))
                                    {
                                       if (!pc.isValidateOnMatch() && !pc.isBackgroundValidation())
                                       {
                                          if (mcf instanceof ValidatingManagedConnectionFactory)
                                          {
                                             log.enablingValidateOnMatch(connectionDefinition.getJndiName());
                                             pc.setValidateOnMatch(true);
                                          }
                                       }
                                    }
                                 }
                                 
                                 if (flushStrategy == null)
                                    flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;
                                       
                                 PoolFactory pf = new PoolFactory();

                                 Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;
                                 if (connectionDefinition != null &&
                                     connectionDefinition.getPool() != null &&
                                     connectionDefinition.isXa())
                                 {
                                    XaPool xaPool = (XaPool)connectionDefinition.getPool();
                                    if (xaPool != null)
                                       noTxSeparatePool = xaPool.isNoTxSeparatePool();
                                 }

                                 Security security = null;
                                 if (connectionDefinition != null && connectionDefinition.getSecurity() != null)
                                 {
                                    security = connectionDefinition.getSecurity();
                                 }

                                 PoolStrategy strategy = PoolStrategy.ONE_POOL;
                                 String securityDomain = null;
                                 boolean isCRI = false;

                                 if (security != null)
                                 {
                                    if (security.isApplication())
                                    {
                                       strategy = PoolStrategy.POOL_BY_CRI;
                                       pc.setMinSize(0);
                                       isCRI = true;
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
                                       pc.setMinSize(0);
                                       isCRI = true;
                                    }
                                 }

                                 if (ra != null && ra.getOutboundResourceadapter() != null &&
                                     ra.getOutboundResourceadapter().getReauthenticationSupport())
                                 {
                                    strategy = PoolStrategy.REAUTH;
                                    isCRI = false;
                                 }

                                 Boolean sharable = Defaults.SHARABLE;
                                 Boolean enlistment = Defaults.ENLISTMENT;
                                 Boolean connectable = Defaults.CONNECTABLE;
                                 Boolean tracking = Defaults.TRACKING;

                                 if (connectionDefinition != null)
                                 {
                                    sharable = connectionDefinition.isSharable();
                                    enlistment = connectionDefinition.isEnlistment();
                                    connectable = connectionDefinition.isConnectable();
                                    tracking = connectionDefinition.isTracking();
                                 }

                                 String mcpClass = connectionDefinition != null ? connectionDefinition.getMcp() : null;
                                 if (mcpClass == null)
                                 {
                                    ManagedConnectionPoolFactory mcpf = new ManagedConnectionPoolFactory();
                                    if (mcpf.isOverride())
                                       mcpClass = mcpf.getDefaultImplementation();
                                 }
                                 if (mcpClass == null)
                                    mcpClass = ManagedConnectionPoolFactory.EXPERIMENTAL_IMPLEMENTATION;

                                 org.jboss.jca.core.connectionmanager.pool.api.Pool pool =
                                    pf.create(strategy, mcf, pc, noTxSeparatePool.booleanValue(),
                                              sharable.booleanValue(), mcpClass);

                                 // Capacity
                                 applyCapacity(connectionDefinition, pool, isCRI);
                                    
                                 // Add a connection manager
                                 ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                                 ConnectionManager cm = null;
                                 TransactionSupportLevel tsl = TransactionSupportLevel.NoTransaction;
                                 TransactionSupportEnum tsmd = TransactionSupportEnum.NoTransaction;
                                 if (activation != null && activation.getTransactionSupport() != null)
                                 {
                                    tsmd = activation.getTransactionSupport();
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
                                 {
                                    TransactionSupportLevel oldTSL = tsl;
                                    tsl = ((TransactionSupport) mcf).getTransactionSupport();

                                    if (tsl != oldTSL)
                                       log.changedTransactionSupport(connectionDefinition != null ?
                                                                     connectionDefinition.getJndiName() :
                                                                     mcf.getClass().getName());
                                 }

                                 // XAResource recovery
                                 XAResourceRecovery recoveryImpl = null;
                                 boolean enableRecovery = false;

                                 // Connection manager properties
                                 Integer allocationRetry = null;
                                 Long allocationRetryWaitMillis = null;
                                 if (connectionDefinition != null && connectionDefinition.getTimeOut() != null)
                                 {
                                    allocationRetry = connectionDefinition.getTimeOut().getAllocationRetry();
                                    allocationRetryWaitMillis =
                                       connectionDefinition.getTimeOut().getAllocationRetryWaitMillis();
                                 }

                                 Boolean useCCM = Boolean.TRUE;
                                 if (connectionDefinition != null)
                                    useCCM = connectionDefinition.isUseCcm();

                                 // Select the correct connection manager
                                 if (tsl == TransactionSupportLevel.NoTransaction)
                                 {
                                    cm = cmf.createNonTransactional(tsl, pool,
                                                                    getSubjectFactory(security),
                                                                    securityDomain,
                                                                    useCCM, getCachedConnectionManager(),
                                                                    sharable,
                                                                    enlistment,
                                                                    connectable,
                                                                    tracking,
                                                                    flushStrategy,
                                                                    allocationRetry, allocationRetryWaitMillis);
                                 }
                                 else
                                 {
                                    Boolean interleaving = Defaults.INTERLEAVING;
                                    Integer xaResourceTimeout = null;
                                    Boolean isSameRMOverride = Defaults.IS_SAME_RM_OVERRIDE;
                                    Boolean wrapXAResource = Defaults.WRAP_XA_RESOURCE;
                                    Boolean padXid = Defaults.PAD_XID;
                                    Recovery recoveryMD = null;
                                    Boolean enlistmentTrace = Defaults.ENLISTMENT_TRACE;
                                    
                                    if (connectionDefinition != null && connectionDefinition.isXa())
                                    {
                                       XaPool xaPool = (XaPool)connectionDefinition.getPool();

                                       if (xaPool != null)
                                       {
                                          interleaving = xaPool.isInterleaving();
                                          isSameRMOverride = xaPool.isSameRmOverride();
                                          wrapXAResource = xaPool.isWrapXaResource();
                                          padXid = xaPool.isPadXid();
                                       }

                                       TimeOut timeout = connectionDefinition.getTimeOut();
                                       if (timeout != null)
                                       {
                                          xaResourceTimeout = timeout.getXaResourceTimeout();
                                       }
                                       
                                       recoveryMD = connectionDefinition.getRecovery();
                                    }

                                    pool.setInterleaving(interleaving.booleanValue());

                                    if (connectionDefinition != null)
                                       enlistmentTrace = connectionDefinition.isEnlistmentTrace();

                                    org.jboss.jca.core.api.management.ConnectionManager mgtCM =
                                            new org.jboss.jca.core.api.management.ConnectionManager(
                                                  connectionDefinition.getJndiName());

                                    mgtCM.setEnlistmentTrace(enlistmentTrace);
                                    mgtConnector.getConnectionManagers().add(mgtCM);
                                    cm = cmf.createTransactional(tsl, pool,
                                                                 getSubjectFactory(security),
                                                                 securityDomain, useCCM, getCachedConnectionManager(),
                                                                 sharable,
                                                                 enlistment,
                                                                 connectable,
                                                                 tracking,
                                                                 mgtCM,
                                                                 flushStrategy,
                                                                 allocationRetry, allocationRetryWaitMillis,
                                                                 getTransactionIntegration(),
                                                                 interleaving,
                                                                 xaResourceTimeout, isSameRMOverride,
                                                                 wrapXAResource, padXid);
                                    if (tsl == TransactionSupportLevel.XATransaction)
                                    {
                                       isXA = true;
                                       SecurityMetadata recoverSecurityMetadata = security;
                                       String recoverUser = null;
                                       String recoverPassword = null;
                                       String recoverSecurityDomain = securityDomain;
                                       if (recoveryMD == null || recoveryMD.getNoRecovery() == null ||
                                           !recoveryMD.getNoRecovery())
                                       {
                                          // If we have an XAResourceRecoveryRegistry and the deployment is XA
                                          // lets register it for XA Resource Recovery using the "recovery"
                                          // definition. Fallback to the standard definitions
                                          // for user name, password. Keep a seperate reference to the
                                          // security-domain
                                          enableRecovery = true;

                                          Credential credential =
                                             recoveryMD != null ? recoveryMD.getCredential() : null;

                                          if (credential != null)
                                          {
                                             if (credential.getSecurityDomain() != null)
                                             {
                                                recoverSecurityMetadata = credential;
                                                recoverSecurityDomain = credential.getSecurityDomain();
                                             }
                                             
                                             recoverUser = credential.getUserName();
                                             recoverPassword = credential.getPassword();
                                          }

                                          if (log.isDebugEnabled())
                                          {
                                             log.debug("RecoverUser=" + recoverUser);
                                             log.debug("RecoverSecurityDomain=" + recoverSecurityDomain);
                                          }

                                          if ((recoverUser != null && !recoverUser.trim().equals("") &&
                                               recoverPassword != null && !recoverPassword.trim().equals("")) ||
                                              (recoverSecurityDomain != null &&
                                                    !recoverSecurityDomain.trim().equals("")))
                                          {
                                             RecoveryPlugin plugin = null;
                                             if (recoveryMD != null && recoveryMD.getRecoverPlugin() != null &&
                                                 recoveryMD.getRecoverPlugin().getClassName() != null)
                                             {
                                                List<ConfigProperty> configProperties =
                                                   new ArrayList<ConfigProperty>(recoveryMD
                                                                                 .getRecoverPlugin()
                                                                                 .getConfigPropertiesMap()
                                                                                 .size());
                                                
                                                for (Map.Entry<String, String> property :
                                                        recoveryMD.getRecoverPlugin().
                                                        getConfigPropertiesMap().entrySet())
                                                {
                                                   ConfigProperty c =
                                                      new ConfigPropertyImpl(null,
                                                                             new XsdString(property.getKey(), null),
                                                                             XsdString.NULL_XSDSTRING,
                                                                             new XsdString(property.getValue(), null),
                                                                             Boolean.FALSE, Boolean.FALSE,
                                                                             Boolean.FALSE,
                                                                             null, false,
                                                                             null, null, null, null);

                                                   configProperties.add(c);
                                                }

                                                plugin =
                                                   (RecoveryPlugin)initAndInject(recoveryMD
                                                                                 .getRecoverPlugin()
                                                                                 .getClassName(),
                                                                                 configProperties, cl);
                                             }
                                             else
                                             {
                                                plugin = new DefaultRecoveryPlugin();
                                             }

                                             XAResourceStatistics xastat = null;

                                             if (pool.getStatistics() != null &&
                                                 pool.getStatistics() instanceof XAResourceStatistics)
                                             {
                                                xastat = (XAResourceStatistics)pool.getStatistics();
                                             }

                                             recoveryImpl =
                                                getTransactionIntegration().
                                                   createXAResourceRecovery(mcf,
                                                                            padXid,
                                                                            isSameRMOverride,
                                                                            wrapXAResource,
                                                                            recoverUser,
                                                                            recoverPassword,
                                                                            recoverSecurityDomain,
                                                                            getSubjectFactory(recoverSecurityMetadata),
                                                                            plugin,
                                                                            xastat);
                                          }
                                       }
                                    }
                                 }

                                 // ConnectionFactory
                                 Object cf = mcf.createConnectionFactory(cm);
                                 
                                 if (cf == null)
                                 {
                                    log.nullConnectionFactory();
                                 }
                                 else
                                 {
                                    if (log.isTraceEnabled())
                                    {
                                       log.tracef("ConnectionFactory: %s", cf.getClass().getName());
                                       log.tracef("ConnectionFactory defined in classloader: %s",
                                                 SecurityActions.getClassLoader(cf.getClass()));
                                    }
                                 }

                                 archiveValidationObjects.add(new ValidateObject(Key.CONNECTION_FACTORY, cf));

                                 if (cf != null && cf instanceof Serializable &&
                                     cf instanceof javax.resource.Referenceable)
                                 {
                                    String jndiName;
                                    if (connectionDefinition != null)
                                    {
                                       jndiName = buildJndiName(connectionDefinition.getJndiName(),
                                                                connectionDefinition.isUseJavaContext());
                                       
                                       bindConnectionFactory(url, deploymentName, cf, jndiName);
                                       cfs.add(cf);
                                       cfJndiNames.add(jndiName);

                                       cm.setJndiName(jndiName);
                                       cfCMs.add(cm);

                                       String poolName = null;
                                       if (connectionDefinition != null)
                                       {
                                          poolName = connectionDefinition.getPoolName();
                                       }

                                       if (poolName == null)
                                          poolName = jndiName;
                                       
                                       pool.setName(poolName);
                                    }
                                    else
                                    {
                                       String[] bindCfJndiNames = bindConnectionFactory(url, deploymentName, cf);
                                       cfs.add(cf);
                                       cfJndiNames.addAll(Arrays.asList(bindCfJndiNames));

                                       cm.setJndiName(bindCfJndiNames[0]);
                                       cfCMs.add(cm);

                                       String poolName = null;
                                       if (connectionDefinition != null)
                                       {
                                          poolName = connectionDefinition.getPoolName();
                                       }

                                       if (poolName == null)
                                          poolName = cfJndiNames.get(0);
                                       
                                       jndiName = poolName;
                                       pool.setName(poolName);
                                    }

                                    // Verify recovery settings, but always add the module to align deployment data
                                    if (enableRecovery && getTransactionIntegration().getRecoveryRegistry() != null)
                                    {
                                       if (recoveryImpl != null)
                                       {
                                          recoveryImpl.setJndiName(cm.getJndiName());
                                          recoveryImpl.initialize();
                                          getTransactionIntegration().
                                             getRecoveryRegistry().addXAResourceRecovery(recoveryImpl);
                                       }
                                       else
                                       {
                                          log.missingRecovery(cm.getJndiName());
                                       }
                                    }
                                    recoveryModules.add(recoveryImpl);

                                    if (activateDeployment)
                                    {
                                       org.jboss.jca.core.api.management.ConnectionFactory mgtCf =
                                          new org.jboss.jca.core.api.management.ConnectionFactory(cf, mcf);
                                          
                                       mgtCf.setPoolConfiguration(pc);
                                       mgtCf.setPool(pool);
                                       mgtCf.setJndiName(jndiName);
                                          
                                       mgtCf.getManagedConnectionFactory().getConfigProperties().
                                          addAll(createManagementView(cdMeta.getConfigProperties()));
                                          
                                       mgtConnector.getConnectionFactories().add(mgtCf);

                                       // Prefill
                                       if (pool instanceof PrefillPool)
                                       {
                                          PrefillPool pp = (PrefillPool)pool;
                                          SubjectFactory subjectFactory = getSubjectFactory(security);
                                          Subject subject = null;
                                             
                                          if (subjectFactory != null)
                                             subject = createSubject(subjectFactory, securityDomain, mcf);

                                          pp.prefill(subject, null, noTxSeparatePool.booleanValue());
                                       }
                                    }
                                 }
                                 else
                                 {
                                    log.connectionFactoryNotBound(mcf.getClass().getName());

                                    if (cf != null)
                                    {
                                       log.connectionFactoryNotSpecCompliant(cf.getClass().getName());
                                    }
                                    else
                                    {
                                       log.connectionFactoryNotSpecCompliant(mcf.getClass().getName());
                                    }
                                 }
                              }
                           }
                        }
                        else
                        {
                           log.debugf("No activation: %s", mcfClz);
                        }
                     }
                  }
               }
            }

            failures = initActivationSpec(cl, cmd, resourceAdapter, archiveValidationObjects, beanValidationObjects,
                                          failures, url, activateDeployment);

            failures = initAdminObject(cmd, cl, archiveValidationObjects, beanValidationObjects, failures, url,
                                       deploymentName, activateDeployment, resourceAdapter, 
                                       activation != null ? activation.getAdminObjects() : null, aos, aoJndiNames,
                                       activateDeployment ? mgtConnector : null);
         }

         // Archive validation
         failures = validateArchive(url, archiveValidationObjects, failures);

         if ((getConfiguration().getArchiveValidationFailOnWarn() &&
              (hasFailuresLevel(failures, Severity.WARNING) || hasFailuresLevel(failures, Severity.ERROR))) ||
             (getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures, null), failures);
         }
         else
         {
            if (failures != null && failures.size() > 0)
            {
               log.validationInvalidArchive(url.toExternalForm());
            }

            printFailuresLog(url.getPath(), new Validator(), failures, null);
         }

         if (cmd != null)
         {
            try
            {
               // Register with MDR
               registerResourceAdapterToMDR(url, root, cmd, activation);
            }
            catch (org.jboss.jca.core.spi.mdr.AlreadyExistsException e)
            {
               //ignore it, RA already registered
            }
         }

         if (activateDeployment)
         {
            // Bean validation
            if (getConfiguration().getBeanValidation() && (cmd.getVersion() == Version.V_16 || 
                                                           cmd.getVersion() == Version.V_17))
            {
               List<Class> groupsClasses = null;

               if (activation != null && activation.getBeanValidationGroups() != null &&
                   activation.getBeanValidationGroups().size() > 0)
               {
                  groupsClasses = new ArrayList<Class>();
                  for (String group : activation.getBeanValidationGroups())
                  {
                     groupsClasses.add(Class.forName(group, true, cl));
                  }
               }

               if (beanValidationObjects.size() > 0)
               {
                  BeanValidation beanValidator = getBeanValidation();
                  for (Object o : beanValidationObjects)
                  {
                     beanValidator.validate(o, groupsClasses);
                  }
               }
            }

            // Activate deployment
            if (resourceAdapter != null)
            {
               String bootstrapContextName = null;
               if (activation != null && activation.getBootstrapContext() != null &&
                   !activation.getBootstrapContext().trim().equals(""))
               {
                  bootstrapContextName = activation.getBootstrapContext();
               }

               bootstrapContextIdentifier =
                  BootstrapContextCoordinator.getInstance().createIdentifier(resourceAdapter.getClass().getName(),
                                                                             raConfigProperties,
                                                                             bootstrapContextName);

               startContext(resourceAdapter, bootstrapContextIdentifier, bootstrapContextName, callback);

               // Register with ResourceAdapterRepository
               resourceAdapterKey = registerResourceAdapterToResourceAdapterRepository(resourceAdapter);
               setRecoveryForResourceAdapterInResourceAdapterRepository(resourceAdapterKey, isXA);
            }
         }

         if (activateDeployment)
         {
            log.deployed(url.toExternalForm());
         }
         else
         {
            if (log.isDebugEnabled())
            {
               log.debug("Activated: " + url.toExternalForm());
            }
         }

         Object[] cfObjs = cfs.size() > 0 ? cfs.toArray(new Object[cfs.size()]) : null;
         String[] cfJndis = cfJndiNames.size() > 0 ? cfJndiNames.toArray(new String[cfJndiNames.size()]) : null;
         ConnectionManager[] cfCM = cfCMs.size() > 0 ? cfCMs.toArray(new ConnectionManager[cfCMs.size()]) : null;
         Object[] aoObjs = aos.size() > 0 ? aos.toArray(new Object[aos.size()]) : null;
         String[] aoJndis = aoJndiNames.size() > 0 ? aoJndiNames.toArray(new String[aoJndiNames.size()]) : null;
         
         return new CommonDeployment(url, deploymentName, activateDeployment,
                                     resourceAdapter, resourceAdapterKey, bootstrapContextIdentifier,
                                     cfObjs, cfJndis, cfCM,
                                     aoObjs, aoJndis,
                                     recoveryModules.toArray(new XAResourceRecovery[recoveryModules.size()]),
                                     mgtConnector, null, cl, log);

      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         if ((getConfiguration().getArchiveValidationFailOnWarn() &&
              (hasFailuresLevel(failures, Severity.WARNING) || hasFailuresLevel(failures, Severity.ERROR))) ||
             (getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new DeployException(bundle.deploymentFailed(url.toExternalForm()),
                                      new ValidatorException(printFailuresLog(url.getPath(), new Validator(),
                                         failures, null), failures));
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
            throw new DeployException(bundle.deploymentFailed(url.toExternalForm()), t);
         }
      }
   }

   /**
    * Build the jndi name
    * @param jndiName The jndi name
    * @param javaContext The java context
    * @return The value
    */
   protected String buildJndiName(String jndiName, Boolean javaContext)
   {
      if (javaContext != null)
      {
         if (javaContext.booleanValue() && !jndiName.startsWith("java:"))
         {
            jndiName = "java:" + jndiName;
         }
         else if (!javaContext.booleanValue() && jndiName.startsWith("java:"))
         {
            jndiName = jndiName.substring(6);
         }
      }

      return jndiName;
   }

   /**
    * Require explicit JNDI bindings
    * @return True if explicit JNDI bindings are required; otherwise false
    */
   protected boolean requireExplicitJndiBindings()
   {
      return true;
   }

   /**
    * Get a subject factory
    * @param securityMetadata The security metadata: contains the security domain and any other necessary information
    *                         for returning the subject factory
    * @return The subject factory; must return <code>null</code> if security domain isn't defined
    * @exception DeployException Thrown if the security domain can't be resolved
    */
   protected abstract SubjectFactory getSubjectFactory(SecurityMetadata securityMetadata) throws DeployException;

   /**
    * Create a subject
    * @param subjectFactory The subject factory
    * @param securityDomain The security domain
    * @param mcf The managed connection factory
    * @return The subject; <code>null</code> in case of an error
    */
   protected Subject createSubject(final SubjectFactory subjectFactory, 
                                   final String securityDomain,
                                   final ManagedConnectionFactory mcf)
   {
      if (subjectFactory == null)
         throw new IllegalArgumentException("SubjectFactory is null");

      if (securityDomain == null)
         throw new IllegalArgumentException("SecurityDomain is null");

      return AccessController.doPrivileged(new PrivilegedAction<Subject>() 
      {
         public Subject run()
         {
            try
            {
               Subject subject = subjectFactory.createSubject(securityDomain);

               Set<PasswordCredential> pcs = subject.getPrivateCredentials(PasswordCredential.class);
               if (pcs.size() > 0)
               {
                  for (PasswordCredential pc : pcs)
                  {
                     pc.setManagedConnectionFactory(mcf);
                  }
               }

               if (log.isDebugEnabled())
                  log.debug("Subject=" + subject);
                     
               return subject;
            }
            catch (Throwable t)
            {
               log.subjectCreationError(t.getMessage(), t);
            }

            return null;
         }
      });
   }

   /**
    * Get the bean validation module
    * @return The module
    */
   protected BeanValidation getBeanValidation()
   {
      return new BeanValidation();
   }

   /**
    * Get the cached connection manager
    * @return The handle
    */
   protected abstract CachedConnectionManager getCachedConnectionManager();

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

            Boolean dynamic = cp.getConfigPropertySupportsDynamicUpdates();
            if (dynamic == null)
               dynamic = Boolean.FALSE;

            Boolean confidential = cp.getConfigPropertyConfidential();
            if (confidential == null)
               confidential = Boolean.FALSE;

            mgtCp =
               new org.jboss.jca.core.api.management.ConfigProperty(cp.getConfigPropertyName().getValue(),
                                                                    dynamic.booleanValue(),
                                                                    confidential.booleanValue());

            result.add(mgtCp);
         }
      }

      return result;
   }

   /**
    * Get a callback implementation
    * @param ws The WorkManager security settings
    * @return The value
    */
   protected Callback createCallback(WorkManagerSecurity ws)
   {
      if (ws != null)
      {
         boolean mr = ws.isMappingRequired();
         String d = ws.getDomain();
         String dp = ws.getDefaultPrincipal();
         String[] dgs = ws.getDefaultGroups() != null ? 
            ws.getDefaultGroups().toArray(new String[ws.getDefaultGroups().size()]) : null;
         Map<String, String> ps = ws.getUserMappings();
         Map<String, String> gs = ws.getGroupMappings();

         return new CallbackImpl(mr, d, dp, dgs, ps, gs);
      }

      return null;
   }

   /**
    * Strip password
    * @param str The string
    * @return The result
    */
   private String stripPassword(String str)
   {
      if (str.indexOf("<password>") == -1)
         return str;

      Pattern pattern = Pattern.compile("<password>[^<]*</password>");
      String[] strs = pattern.split(str);

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < strs.length; i++)
      {
         String s = strs[i];
         sb.append(s);
         if (i < strs.length - 1)
            sb.append("<password>****</password>");
      }

      return sb.toString();
   }

   /**
    * Apply capacity
    * @param connectionDefinition The connection definition
    * @param pool The pool
    * @param isCRI Is the pool going to be CRI based
    */
   protected void applyCapacity(ConnectionDefinition connectionDefinition,
                                org.jboss.jca.core.connectionmanager.pool.api.Pool pool,
                                boolean isCRI)
   {
      if (connectionDefinition != null)
      {
         if (connectionDefinition.getPool() != null)
         {
            org.jboss.jca.common.api.metadata.common.Pool cdp = connectionDefinition.getPool();
                                 
            if (cdp.getCapacity() != null)
               pool.setCapacity(CapacityFactory.create(cdp.getCapacity(), isCRI));
         }
      }
   }

   /**
    * Should the archive be scanned for annotations
    * @param cmd The metadata
    * @return True if scan is needed; otherwise false
    */
   protected boolean scanArchive(Connector cmd)
   {
      if (cmd == null)
         return true;

      if (cmd.getVersion() == Version.V_16 || cmd.getVersion() == Version.V_17)
      {
         if (!cmd.isMetadataComplete())
            return true;
      }

      return false;
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
    * @param activation activation
    * @throws org.jboss.jca.core.spi.mdr.AlreadyExistsException AlreadyExistsException
    */
   protected abstract void registerResourceAdapterToMDR(URL url, File root, Connector cmd, Activation activation)
      throws org.jboss.jca.core.spi.mdr.AlreadyExistsException;

   /**
    * Register the ResourceAdapter to the ResourceAdapterRepository. Implementer should provide the implementation
    * to get repository and do the registration
    * @param instance the instance
    * @return The key
    */
   protected abstract String
   registerResourceAdapterToResourceAdapterRepository(javax.resource.spi.ResourceAdapter instance);

   /**
    * Set recovery mode for a resource adapter in the ResourceAdapterRepository
    * @param key The key for the resource adapter
    * @param isXA Is the resource adapter XA capable
    */
   protected abstract void setRecoveryForResourceAdapterInResourceAdapterRepository(String key, boolean isXA);

   /**
    * Get the transaction Manager. Implementers have to provide right implementation to find and get it
    * @return The value
    */
   protected abstract TransactionManager getTransactionManager();

   /**
    * Get the transaction integration. Implementers have to provide right implementation to find and get it
    * @return The value
    */
   protected abstract TransactionIntegration getTransactionIntegration();

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
    * @param activation The activation metadata
    * @return True if the deployment should be activated; otherwise false
    */
   protected abstract boolean checkActivation(Connector cmd, Activation activation);

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

   /**
    * Get the logger
    * @return The value
    */
   protected abstract DeployersLogger getLogger();
}
