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

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.api.metadata.ra.ra16.Activationspec16;
import org.jboss.jca.common.metadata.ra.common.ConfigPropertyImpl;
import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.pool.api.PrefillPool;
import org.jboss.jca.core.recovery.DefaultRecoveryPlugin;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.jboss.logging.Messages;
import org.jboss.security.SubjectFactory;

/**
 * An abstract resource adapter deployer which contains common functionality
 * for all resource adapter archive based deployers.
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractResourceAdapterDeployer
{
   /** The bundle */
   private static DeployersBundle bundle = Messages.getBundle(DeployersBundle.class);

   /** the logger **/
   protected final DeployersLogger log;

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
    */
   public AbstractResourceAdapterDeployer(boolean validateClasses)
   {
      super();
      this.log = getLogger();
      this.trace = log.isTraceEnabled();
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
         throw new DeployException(bundle.unableToStartResourceAdapter(resourceAdapter.getClass().getName()),
                                   ite.getTargetException());
      }
      catch (Throwable t)
      {
         throw new DeployException(bundle.unableToStartResourceAdapter(resourceAdapter.getClass().getName()), t);
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
   private Set<String> findManagedConnectionFactories(org.jboss.jca.common.api.metadata.ra.ResourceAdapter ra)
   {
      Set<String> result = new HashSet<String>(1);

      if (ra != null)
      {
         if (ra instanceof ResourceAdapter10)
         {
            result.add(((ResourceAdapter10)ra).getManagedConnectionFactoryClass().getValue());
         }
         else
         {
            ResourceAdapter1516 ra1516 = (ResourceAdapter1516)ra;
            if (ra1516.getOutboundResourceadapter() != null)
            {
               for (org.jboss.jca.common.api.metadata.ra.ConnectionDefinition cd : 
                       ra1516.getOutboundResourceadapter().getConnectionDefinitions())
               {
                  result.add(cd.getManagedConnectionFactoryClass().getValue());
               }
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
   private Set<String> resolveAdminObjects(org.jboss.jca.common.api.metadata.ra.ResourceAdapter ra)
   {
      Set<String> result = new HashSet<String>(1);

      if (ra != null)
      {
         if (ra instanceof ResourceAdapter1516)
         {
            ResourceAdapter1516 ra1516 = (ResourceAdapter1516)ra;
            if (ra1516.getAdminObjects() != null)
            {
               for (org.jboss.jca.common.api.metadata.ra.AdminObject ao : ra1516.getAdminObjects())
               {
                  result.add(ao.getAdminobjectClass().getValue());
               }
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
   protected Set<CommonConnDef> findConnectionDefinitions(String clz, Set<String> mcfs, List<CommonConnDef> defs,
                                                          ClassLoader cl)
      throws DeployException
   {
      Set<CommonConnDef> result = null;

      if (mcfs != null && defs != null)
      {
         // If there is only one we will return that
         if (mcfs.size() == 1 && defs.size() == 1)
         {
            CommonConnDef cd = defs.get(0);

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
               result = new HashSet<CommonConnDef>(1);
               result.add(cd);

               return result;
            }
         }

         // If there are multiple definitions the MCF class name is mandatory
         if (clz == null)
            throw new IllegalArgumentException(bundle.undefinedManagedConnectionFactory());

         for (CommonConnDef cd : defs)
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
                     result = new HashSet<CommonConnDef>();

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
    * Find the metadata for an admin object
    * @param clz The fully quilified class name for the admin object
    * @param aos The admin object classes
    * @param defs The admin object definitions
    * @return The metadata; <code>null</code> if none could be found
    * @exception DeployException Thrown in case of configuration error
    */
   protected Set<CommonAdminObject> findAdminObjects(String clz, Set<String> aos, List<CommonAdminObject> defs)
      throws DeployException
   {
      Set<CommonAdminObject> result = null;

      if (aos != null && defs != null)
      {
         // If there is only one we will return that
         if (aos.size() == 1 && defs.size() == 1)
         {
            org.jboss.jca.common.api.metadata.common.CommonAdminObject cao = defs.get(0);

            if (cao.getClassName() != null && !clz.equals(cao.getClassName()))
            {
               log.adminObjectMismatch(cao.getClassName());
               throw new DeployException(clz + " not a valid admin object");
            }

            result = new HashSet<CommonAdminObject>(1);
            result.add(cao);

            return result;
         }

         // If there are multiple definitions the admin object class name is mandatory
         if (clz == null)
            throw new IllegalArgumentException(bundle.undefinedAdminObject());

         for (org.jboss.jca.common.api.metadata.common.CommonAdminObject cao : defs)
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
                     result = new HashSet<CommonAdminObject>();

                  result.add(cao);
               }
            }
         }
      }

      return result;
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
            pc.setIdleTimeoutMinutes(tp.getIdleTimeoutMinutes().intValue());
      }

      if (vp != null)
      {
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
                     List<? extends ConfigProperty> asCps = null;
                     if (mlMD.getActivationspec() instanceof Activationspec16)
                     {
                        asCps = ((Activationspec16)mlMD.getActivationspec()).getConfigProperties();
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

                           if (trace)
                           {
                              log.trace("ActivationSpec: " + as.getClass().getName());
                              log.trace("ActivationSpec defined in classloader: " + as.getClass().getClassLoader());
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
      URL url, String deploymentName, boolean activateDeployment, ResourceAdapter resourceAdapter,
      List<org.jboss.jca.common.api.metadata.common.CommonAdminObject> aosRaXml,
      List<org.jboss.jca.common.api.metadata.common.CommonAdminObject> aosIronJacamar,
      List<Object> aos, List<String> aoJndiNames,
      org.jboss.jca.core.api.management.Connector mgtConnector)
      throws DeployException
   {
      // AdminObject
      if (cmd.getVersion() != Version.V_10)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) cmd.getResourceadapter();
         if (ra1516 != null && ra1516.getAdminObjects() != null)
         {
            List<AdminObject> aoMetas = ra1516.getAdminObjects();
            if (aoMetas.size() > 0)
            {
               Set<String> aosClz = resolveAdminObjects(ra1516);
               Set<String> processedAos = new HashSet<String>();

               for (int i = 0; i < aoMetas.size(); i++)
               {
                  AdminObject aoMeta = aoMetas.get(i);

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
                        Set<CommonAdminObject> adminObjects = null;

                        if (aosRaXml != null)
                           adminObjects = findAdminObjects(aoClz, aosClz, aosRaXml);

                        if (adminObjects == null && aosIronJacamar != null)
                           adminObjects = findAdminObjects(aoClz, aosClz, aosIronJacamar);

                        if (!requireExplicitJndiBindings() &&
                            aosRaXml == null && aosIronJacamar == null &&
                            aosClz.size() == 1)
                        {
                           adminObjects = new HashSet<CommonAdminObject>(1);
                           adminObjects.add(null);
                        }

                        if (activateDeployment && adminObjects != null)
                        {
                           Injection injector = new Injection();

                           for (CommonAdminObject adminObject : adminObjects)
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

                              if (trace)
                              {
                                 log.trace("AdminObject: " + ao.getClass().getName());
                                 log.trace("AdminObject defined in classloader: " + ao.getClass().getClassLoader());
                              }

                              archiveValidationObjects.add(new ValidateObject(Key.ADMIN_OBJECT, ao, aoMeta
                                                                              .getConfigProperties()));
                              beanValidationObjects.add(ao);

                              if (ao != null)
                              {
                                 boolean adminObjectBound = false;

                                 if ((ao instanceof ResourceAdapterAssociation &&
                                      ao instanceof Serializable &&
                                      ao instanceof javax.resource.Referenceable) ||
                                     (ao instanceof javax.naming.Referenceable))
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
                           log.debug("No activation: " + aoClz);
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
                  System.load(lib);
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
   * @throws org.jboss.jca.core.spi.mdr.AlreadyExistsException AlreadyExistsException
   * @throws ClassNotFoundException ClassNotFoundException
   * @throws Throwable Throwable
   */
   protected CommonDeployment createObjectsAndInjectValue(URL url, String deploymentName, File root, ClassLoader cl,
      Connector cmd, IronJacamar ijmd) throws DeployException, ResourceException, ValidatorException,
      org.jboss.jca.core.spi.mdr.AlreadyExistsException, ClassNotFoundException, Throwable
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
    * @throws org.jboss.jca.core.spi.mdr.AlreadyExistsException AlreadyExistsException
    * @throws ClassNotFoundException ClassNotFoundException
    * @throws Throwable Throwable
    */
   protected CommonDeployment createObjectsAndInjectValue(URL url, String deploymentName, File root, ClassLoader cl,
      Connector cmd, IronJacamar ijmd, org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter raxml)
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

         ResourceAdapter resourceAdapter = null;
         String resourceAdapterKey = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Object> beanValidationObjects = new ArrayList<Object>();
         List<Object> cfs = new ArrayList<Object>();
         List<String> cfJndiNames = new ArrayList<String>();
         List<ConnectionManager> cfCMs = new ArrayList<ConnectionManager>();
         List<Object> aos = new ArrayList<Object>();
         List<String> aoJndiNames = new ArrayList<String>();
         List<XAResourceRecovery> recoveryModules = new ArrayList<XAResourceRecovery>(1);

         // Check metadata for JNDI information and activate explicit
         boolean activateDeployment = checkActivation(cmd, ijmd);

         if (log.isTraceEnabled())
         {
            log.tracef("Connector=%s", cmd);
            log.tracef("IronJacamar=%s", ijmd);
            log.tracef("RaXML=%s", raxml);
            log.tracef("ActivateDeployment=%s", activateDeployment);
         }

         // Load native libraries
         if (activateDeployment)
            loadNativeLibraries(root);

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
                        String raClz = ra1516.getResourceadapterClass();
                        Object or = initAndInject(raClz, ra1516.getConfigProperties(), cl);

                        if (or == null || !(or instanceof ResourceAdapter))
                           throw new DeployException(bundle.invalidResourceAdapter(raClz));

                        resourceAdapter = (ResourceAdapter)or;

                        Map<String, String> raConfigProperties = null;

                        if (raxml != null)
                           raConfigProperties = raxml.getConfigProperties();

                        if (raConfigProperties == null && ijmd != null)
                           raConfigProperties = ijmd.getConfigProperties();

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

                        if (trace)
                        {
                           log.trace("ResourceAdapter: " + resourceAdapter.getClass().getName());
                           log.trace("ResourceAdapter defined in classloader: " +
                                     resourceAdapter.getClass().getClassLoader());
                        }

                        archiveValidationObjects.add(new ValidateObject(Key.RESOURCE_ADAPTER, resourceAdapter, ra1516
                           .getConfigProperties()));
                        beanValidationObjects.add(resourceAdapter);

                        if (activateDeployment)
                        {
                           org.jboss.jca.core.api.management.ResourceAdapter mgtRa =
                              new org.jboss.jca.core.api.management.ResourceAdapter(resourceAdapter);

                           mgtRa.getConfigProperties().addAll(createManagementView(ra1516.getConfigProperties()));
                           mgtConnector.setResourceAdapter(mgtRa);
                        }
                     }
                  }
               }
            }

            // ManagedConnectionFactory
            if (cmd.getVersion() == Version.V_10)
            {
               ResourceAdapter10 ra10 = (ResourceAdapter10) cmd.getResourceadapter();

               if (raxml != null && raxml.getConfigProperties() != null && raxml.getConfigProperties().size() > 0)
               {
                  for (String s : raxml.getConfigProperties().keySet())
                  {
                     log.invalidConfigProperty(s);
                  }
               }

               if (ijmd != null && ijmd.getConfigProperties() != null && ijmd.getConfigProperties().size() > 0)
               {
                  for (String s : ijmd.getConfigProperties().keySet())
                  {
                     log.invalidConfigProperty(s);
                  }
               }

               Set<String> mcfs = findManagedConnectionFactories(ra10);

               Set<CommonConnDef> connectionDefinitions = null;

               if (raxml != null)
               {
                  List<CommonConnDef> cdDefs = raxml.getConnectionDefinitions();

                  if (cdDefs != null)
                  {
                     connectionDefinitions = 
                        findConnectionDefinitions(ra10.getManagedConnectionFactoryClass().getValue(), mcfs, cdDefs, cl);
                  }
               }

               if (connectionDefinitions == null && ijmd != null)
               {
                  List<CommonConnDef> cdDefs = ijmd.getConnectionDefinitions();

                  if (cdDefs != null)
                  {
                     connectionDefinitions =
                        findConnectionDefinitions(ra10.getManagedConnectionFactoryClass().getValue(), mcfs, cdDefs, cl);
                  }
               }

               if (!requireExplicitJndiBindings() && raxml == null && ijmd == null && mcfs.size() == 1)
               {
                  connectionDefinitions = new HashSet<CommonConnDef>(1);
                  connectionDefinitions.add(null);
               }

               if (activateDeployment && connectionDefinitions != null)
               {
                  for (CommonConnDef connectionDefinition : connectionDefinitions)
                  {
                     log.debugf("Activating: %s", connectionDefinition);

                     if (connectionDefinition == null || connectionDefinition.isEnabled())
                     {
                        String mcfClz = ra10.getManagedConnectionFactoryClass().getValue();
                        Object om = initAndInject(mcfClz, ra10.getConfigProperties(), cl);

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
                        FlushStrategy flushStrategy = null;

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

                        if (flushStrategy == null)
                           flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;

                        PoolFactory pf = new PoolFactory();

                        Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;

                        if (connectionDefinition != null &&
                            connectionDefinition.getPool() != null &&
                            connectionDefinition.isXa())
                        {
                           CommonXaPool xaPool = (CommonXaPool)connectionDefinition.getPool();
                           if (xaPool != null)
                              noTxSeparatePool = xaPool.isNoTxSeparatePool();
                        }

                        PoolStrategy strategy = PoolStrategy.ONE_POOL;
                        String securityDomain = null;

                        CommonSecurity security = null;
                        if (connectionDefinition != null && connectionDefinition.getSecurity() != null)
                        {
                           security = connectionDefinition.getSecurity();
                        }

                        if (security != null)
                        {
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

                        if (ra10 != null && ra10.getReauthenticationSupport() != null &&
                            ra10.getReauthenticationSupport().booleanValue())
                        {
                           strategy = PoolStrategy.REAUTH;
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
                           tsmd = ra10.getTransactionSupport();
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

                        if (connectionDefinition != null && connectionDefinition.getTimeOut() != null)
                        {
                           allocationRetry = connectionDefinition.getTimeOut().getAllocationRetry();
                           allocationRetryWaitMillis = connectionDefinition.getTimeOut().getAllocationRetryWaitMillis();
                        }
                        
                        Boolean useCCM = Boolean.TRUE;
                        if (connectionDefinition != null)
                           useCCM = connectionDefinition.isUseCcm();

                        // Select the correct connection manager
                        if (tsl == TransactionSupportLevel.NoTransaction)
                        {
                           cm = cmf.createNonTransactional(tsl, pool,
                                                           getSubjectFactory(securityDomain), securityDomain,
                                                           useCCM, getCachedConnectionManager(),
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
                           if (connectionDefinition != null && connectionDefinition.isXa())
                           {
                              CommonXaPool xaPool = (CommonXaPool)connectionDefinition.getPool();

                              if (xaPool != null)
                              {
                                 interleaving = xaPool.isInterleaving();
                                 isSameRMOverride = xaPool.isSameRmOverride();
                                 wrapXAResource = xaPool.isWrapXaResource();
                                 padXid = xaPool.isPadXid();
                              }
                           }

                           cm = cmf.createTransactional(tsl, pool,
                                                        getSubjectFactory(securityDomain), securityDomain,
                                                        useCCM, getCachedConnectionManager(),
                                                        flushStrategy,
                                                        allocationRetry, allocationRetryWaitMillis,
                                                        getTransactionIntegration(), interleaving,
                                                        xaResourceTimeout, isSameRMOverride,
                                                        wrapXAResource, padXid);
                        }

                        // ConnectionFactory
                        Object cf = mcf.createConnectionFactory(cm);

                        if (cf == null)
                        {
                           log.nullConnectionFactory();
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
                                 poolName = connectionDefinition.getPoolName();
                              
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
                                 poolName = connectionDefinition.getPoolName();
                              
                              if (poolName == null)
                                 poolName = cfJndiNames.get(0);
                              
                              jndiName = poolName;
                              pool.setName(poolName);
                           }

                           if (activateDeployment)
                           {
                              org.jboss.jca.core.api.management.ConnectionFactory mgtCf =
                                 new org.jboss.jca.core.api.management.ConnectionFactory(cf, mcf);
                              
                              mgtCf.setPoolConfiguration(pc);
                              mgtCf.setPool(pool);
                              mgtCf.setJndiName(jndiName);
                              
                              mgtCf.getManagedConnectionFactory().getConfigProperties().
                                 addAll(createManagementView(ra10.getConfigProperties()));
                              
                              mgtConnector.getConnectionFactories().add(mgtCf);
                              
                              // Prefill
                              if (pool instanceof PrefillPool)
                              {
                                 PrefillPool pp = (PrefillPool)pool;
                                 SubjectFactory subjectFactory = getSubjectFactory(securityDomain);
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
                  log.debug("No activation: " + ra10.getManagedConnectionFactoryClass().getValue());
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
                     Set<String> mcfs = findManagedConnectionFactories(ra);
                     Set<String> processedMcfs = new HashSet<String>();

                     for (int cdIndex = 0; cdIndex < cdMetas.size(); cdIndex++)
                     {
                        ConnectionDefinition cdMeta = cdMetas.get(cdIndex);
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
                           Set<CommonConnDef> connectionDefinitions = null;

                           if (raxml != null)
                           {
                              List<CommonConnDef> cdDefs = raxml.getConnectionDefinitions();
                           
                              if (cdDefs != null)
                              {
                                 connectionDefinitions =
                                    findConnectionDefinitions(mcfClz, mcfs, cdDefs, cl);
                              }
                           }

                           if (connectionDefinitions == null && ijmd != null)
                           {
                              List<CommonConnDef> cdDefs = ijmd.getConnectionDefinitions();
                              
                              if (cdDefs != null)
                              {
                                 connectionDefinitions = 
                                    findConnectionDefinitions(mcfClz, mcfs, cdDefs, cl);
                              }
                           }

                           if (!requireExplicitJndiBindings() && raxml == null && ijmd == null && mcfs.size() == 1)
                           {
                              connectionDefinitions = new HashSet<CommonConnDef>(1);
                              connectionDefinitions.add(null);
                           }

                           if (activateDeployment && connectionDefinitions != null)
                           {
                              log.debugf("ConnectionDefinitions: %s", connectionDefinitions.size());

                              for (CommonConnDef connectionDefinition : connectionDefinitions)
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

                                    if (flushStrategy == null)
                                       flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;
                                       
                                    PoolFactory pf = new PoolFactory();

                                    Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;
                                    if (connectionDefinition != null &&
                                        connectionDefinition.getPool() != null &&
                                        connectionDefinition.isXa())
                                    {
                                       CommonXaPool xaPool = (CommonXaPool)connectionDefinition.getPool();
                                       if (xaPool != null)
                                          noTxSeparatePool = xaPool.isNoTxSeparatePool();
                                    }

                                    CommonSecurity security = null;
                                    if (connectionDefinition != null && connectionDefinition.getSecurity() != null)
                                    {
                                       security = connectionDefinition.getSecurity();
                                    }

                                    PoolStrategy strategy = PoolStrategy.ONE_POOL;
                                    String securityDomain = null;

                                    if (security != null)
                                    {
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

                                    if (ra.getOutboundResourceadapter().getReauthenticationSupport())
                                    {
                                       strategy = PoolStrategy.REAUTH;
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

                                    // XAResource recovery
                                    XAResourceRecovery recoveryImpl = null;

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
                                                                       getSubjectFactory(securityDomain),
                                                                       securityDomain,
                                                                       useCCM, getCachedConnectionManager(),
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
                                       if (connectionDefinition != null && connectionDefinition.isXa())
                                       {
                                          CommonXaPool xaPool = (CommonXaPool)connectionDefinition.getPool();

                                          if (xaPool != null)
                                          {
                                             interleaving = xaPool.isInterleaving();
                                             isSameRMOverride = xaPool.isSameRmOverride();
                                             wrapXAResource = xaPool.isWrapXaResource();
                                             padXid = xaPool.isPadXid();
                                             recoveryMD = connectionDefinition.getRecovery();
                                          }
                                       }

                                       cm = cmf.createTransactional(tsl, pool,
                                                                    getSubjectFactory(securityDomain), securityDomain,
                                                                    useCCM, getCachedConnectionManager(),
                                                                    flushStrategy,
                                                                    allocationRetry, allocationRetryWaitMillis,
                                                                    getTransactionIntegration(),
                                                                    interleaving,
                                                                    xaResourceTimeout, isSameRMOverride,
                                                                    wrapXAResource, padXid);
                                       if (tsl == TransactionSupportLevel.XATransaction)
                                       {
                                          String recoverSecurityDomain = securityDomain;
                                          String recoverUser = null;
                                          String recoverPassword = null;
                                          if (recoveryMD == null || !recoveryMD.getNoRecovery())
                                          {
                                             // If we have an XAResourceRecoveryRegistry and the deployment is XA
                                             // lets register it for XA Resource Recovery using the "recover"
                                             // definitions from the -ds.xml file. Fallback to the standard definitions
                                             // for user name, password. Keep a seperate reference to the
                                             // security-domain

                                             Credential credential =
                                                recoveryMD != null ? recoveryMD.getCredential() : null;

                                             if (credential != null)
                                             {
                                                recoverSecurityDomain = credential.getSecurityDomain();

                                                recoverUser = credential.getUserName();
                                                recoverPassword = credential.getPassword();
                                             }

                                             if (log.isDebugEnabled())
                                             {
                                                if (recoverUser != null)
                                                {
                                                   log.debug("RecoverUser=" + recoverUser);
                                                }
                                                else if (recoverSecurityDomain != null)
                                                {
                                                   log.debug("RecoverSecurityDomain=" + recoverSecurityDomain);
                                                }
                                             }

                                             RecoveryPlugin plugin = null;
                                             if (recoveryMD != null && recoveryMD.getRecoverPlugin() != null)
                                             {
                                                List<ConfigProperty> configProperties = null;
                                                if (recoveryMD
                                                    .getRecoverPlugin()
                                                    .getConfigPropertiesMap() != null)
                                                {
                                                   configProperties =
                                                      new ArrayList<ConfigProperty>(recoveryMD
                                                                                    .getRecoverPlugin()
                                                                                    .getConfigPropertiesMap().size());

                                                   for (Map.Entry<String, String> property :
                                                           recoveryMD.getRecoverPlugin().
                                                           getConfigPropertiesMap().entrySet())
                                                   {
                                                      ConfigProperty c =
                                                         new ConfigPropertyImpl(null,
                                                                                new XsdString(property.getKey(),
                                                                                              null),
                                                                                new XsdString("String",
                                                                                              null),
                                                                                new XsdString(property
                                                                                              .getValue(),
                                                                                              null), null);
                                                      configProperties.add(c);
                                                   }

                                                   plugin =
                                                      (RecoveryPlugin)initAndInject(recoveryMD
                                                                                    .getRecoverPlugin().getClassName(),
                                                                                    configProperties, cl);
                                                }
                                             }
                                             else
                                             {
                                                plugin = new DefaultRecoveryPlugin();
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
                                                                            getSubjectFactory(recoverSecurityDomain),
                                                                            plugin);
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
                                       if (trace)
                                       {
                                          log.trace("ConnectionFactory: " + cf.getClass().getName());
                                          log.trace("ConnectionFactory defined in classloader: " +
                                                    cf.getClass().getClassLoader());
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

                                       if (getTransactionIntegration().getRecoveryRegistry() != null &&
                                           recoveryImpl != null)
                                       {
                                          recoveryImpl.setJndiName(cm.getJndiName());
                                          getTransactionIntegration().
                                             getRecoveryRegistry().addXAResourceRecovery(recoveryImpl);

                                          recoveryModules.add(recoveryImpl);
                                       }

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
                                             SubjectFactory subjectFactory = getSubjectFactory(securityDomain);
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
                              log.debug("No activation: " + mcfClz);
                           }
                        }
                     }
                  }
               }
            }

            failures = initActivationSpec(cl, cmd, resourceAdapter, archiveValidationObjects, beanValidationObjects,
                                          failures, url, activateDeployment);

            failures = initAdminObject(cmd, cl, archiveValidationObjects, beanValidationObjects, failures, url,
                                       deploymentName, activateDeployment, resourceAdapter, 
                                       raxml != null ? raxml.getAdminObjects() : null, ijmd != null
                                       ? ijmd.getAdminObjects() : null, aos, aoJndiNames,
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
            printFailuresLog(url.getPath(), new Validator(), failures, null);
         }

         if (cmd != null)
         {
            try
            {
               // Register with MDR
               registerResourceAdapterToMDR(url, root, cmd, ijmd);
            }
            catch (org.jboss.jca.core.spi.mdr.AlreadyExistsException e)
            {
               //ignore it, RA already registered
            }
         }

         if (activateDeployment)
         {
            // Bean validation
            if (getConfiguration().getBeanValidation() && cmd.getVersion() == Version.V_16)
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

               // Register with ResourceAdapterRepository
               resourceAdapterKey = registerResourceAdapterToResourceAdapterRepository(resourceAdapter);
            }
         }

         if (activateDeployment)
         {
            log.deployed(url.toExternalForm());
         }
         else
         {
            log.debug("Activated: " + url.toExternalForm());
         }

         Object[] cfObjs = cfs.size() > 0 ? cfs.toArray(new Object[cfs.size()]) : null;
         String[] cfJndis = cfJndiNames.size() > 0 ? cfJndiNames.toArray(new String[cfJndiNames.size()]) : null;
         ConnectionManager[] cfCM = cfCMs.size() > 0 ? cfCMs.toArray(new ConnectionManager[cfCMs.size()]) : null;
         Object[] aoObjs = aos.size() > 0 ? aos.toArray(new Object[aos.size()]) : null;
         String[] aoJndis = aoJndiNames.size() > 0 ? aoJndiNames.toArray(new String[aoJndiNames.size()]) : null;
         
         return new CommonDeployment(url, deploymentName, activateDeployment,
                                     resourceAdapter, resourceAdapterKey,
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
    * @param securityDomain The security domain
    * @return The subject factory; must return <code>null</code> if security domain isn't defined
    * @exception DeployException Thrown if the security domain can't be resolved
    */
   protected abstract SubjectFactory getSubjectFactory(String securityDomain) throws DeployException;

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
               if (pcs != null && pcs.size() > 0)
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

            if (cp instanceof org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16)
            {
               org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16 cp16 =
                  (org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16) cp;

               Boolean dynamic = cp16.getConfigPropertySupportsDynamicUpdates();
               if (dynamic == null)
                  dynamic = Boolean.FALSE;

               Boolean confidential = cp16.getConfigPropertyConfidential();
               if (confidential == null)
                  confidential = Boolean.FALSE;

               mgtCp =
                  new org.jboss.jca.core.api.management.ConfigProperty(
                                                                       cp.getConfigPropertyName().getValue(),
                                                                       dynamic.booleanValue(),
                                                                       confidential.booleanValue());
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
    * @throws org.jboss.jca.core.spi.mdr.AlreadyExistsException AlreadyExistsException
    */
   protected abstract void registerResourceAdapterToMDR(URL url, File root, Connector cmd, IronJacamar ijmd)
      throws org.jboss.jca.core.spi.mdr.AlreadyExistsException;

   /**
    * Register the ResourceAdapter to the ResourceAdapterRepository. Implementer should provide the implementation
    * to get repository and do the registration
    * @param instance the instance
    * @return The key
    */
   protected abstract String registerResourceAdapterToResourceAdapterRepository(ResourceAdapter instance);

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

   /**
    * Get the logger
    * @return The value
    */
   protected abstract DeployersLogger getLogger();
}
