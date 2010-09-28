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

import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
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
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
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

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.api.util.Injection;
import com.github.fungal.api.util.JarFilter;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployment;

/**
 * An abstract resource adapter deployer which contains common functionality
 * for all resource adapter archive based deployers.
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractResourceAdapterDeployer
{
   private static Logger log = Logger.getLogger(AbstractResourceAdapterDeployer.class);

   private static boolean trace = log.isTraceEnabled();

   /** The configuration */
   private RAConfiguration raConfiguration = null;

   private final boolean validateClasses;

   /**
    * Create a new AbstractResourceAdapterDeployer.
    *
    * @param activateDeployment
    * @param validateClasses
    */
   public AbstractResourceAdapterDeployer(boolean validateClasses)
   {
      super();
      this.validateClasses = validateClasses;
   }

   /**
    * Set the configuration
    * @param value The value
    */
   public void setConfiguration(RAConfiguration value)
   {
      raConfiguration = value;
   }

   /**
    * Get the configuration
    * @return The value
    */
   public RAConfiguration getConfiguration()
   {
      return raConfiguration;
   }

   /**
    * validate archive
    *
    * @param url of the archive
    * @param archiveValidation classes and/or to validate.
    * @param failures original list of failures
    * @return The list of failures gotten with all new failures added. Null in case of no failures
    * or if validation is not run according to {@link #getArchiveValidation()} Setting. It returns null also if
    * the concrete implementation of this class set validateClasses instance variable to flase and the list of
    * archiveValidation contains one or more instance of {@link ValidateClass} type
    */
   //IT IS PACKAGE PROTECTED ONLY FOR TESTS ACCESSIBILITY
   Set<Failure> validateArchive(URL url, List<Validate> archiveValidation, Set<Failure> failures)
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
    * @param urlFileName filename Of deployed rar
    * @param validator validator instance used to run validation rules
    * @param failures the list of Failures to be printed
    * @param reportDirectory where to put various logs
    * @param fhInput optional parameter. Normally used only for test or in case of
    *   FailureHelper already present in context
    * @return the error Text
    *
    */
   //IT IS PACKAGE PROTECTED ONLY FOR TESTS ACCESSIBILITY
   String printFailuresLog(String urlFileName, Validator validator, Collection<Failure> failures, File reportDirectory,
      FailureHelper... fhInput)
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
            reportDirectory = new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/log/");
         }
         if (reportDirectory.exists())
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
    * @param failures The failures
    * @param severity The level
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
    * @param bootstrapIdentifier The bootstrap context identifier; may be <code>null</code>
    * @throws DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   protected void startContext(ResourceAdapter resourceAdapter, String bootstrapIdentifier) throws DeployException
   {
      try
      {
         Class clz = resourceAdapter.getClass();
         Method start = clz.getMethod("start", new Class[]
         { BootstrapContext.class });

         CloneableBootstrapContext cbc = null;

         if (bootstrapIdentifier != null && getConfiguration().getBootstrapContexts() != null)
         {
            CloneableBootstrapContext bc = getConfiguration().getBootstrapContexts().get(bootstrapIdentifier);

            if (bc != null)
               cbc = bc.clone();
         }

         if (cbc == null)
            cbc = getConfiguration().getDefaultBootstrapContext().clone();

         start.invoke(resourceAdapter, new Object[]
         { cbc });
      }
      catch (InvocationTargetException ite)
      {
         throw new DeployException("Unable to start " + resourceAdapter.getClass().getName(), ite.getTargetException());
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
   protected void associateResourceAdapter(ResourceAdapter resourceAdapter, Object object) throws DeployException
   {
      if (resourceAdapter != null && object != null)
      {
         if (object instanceof ResourceAdapterAssociation)
         {
            try
            {
               Class clz = object.getClass();

               Method setResourceAdapter = clz.getMethod("setResourceAdapter", new Class[]
               { ResourceAdapter.class });

               setResourceAdapter.invoke(object, new Object[]
               { resourceAdapter });
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
   protected Object initAndInject(String className, List<? extends ConfigProperty> configs, ClassLoader cl)
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

   /**
    * Bind connection factory into JNDI
    * @param url The deployment URL
    * @param deployment The deployment name
    * @param cf The connection factory
    * @return The JNDI names bound
    * @exception Throwable Thrown if an error occurs
    */
   protected String[] bindConnectionFactory(URL url, String deployment, Object cf) throws Throwable
   {
      JndiStrategy js = getConfiguration().getJndiStrategy().clone();

      String[] result = js.bindConnectionFactories(deployment, new Object[]
      { cf });

      getConfiguration().getMetadataRepository().registerJndiMapping(url, cf.getClass().getName(), result[0]);

      return result;
   }

   /**
    * Bind connection factory into JNDI
    * @param url The deployment URL
    * @param deployment The deployment name
    * @param cf The connection factory
    * @param jndi The JNDI name
    * @return The JNDI names bound
    * @exception Throwable Thrown if an error occurs
    */
   protected String[] bindConnectionFactory(URL url, String deployment, Object cf, String jndi) throws Throwable
   {
      JndiStrategy js = getConfiguration().getJndiStrategy().clone();

      String[] result = js.bindConnectionFactories(deployment, new Object[]
      { cf }, new String[]
      { jndi });

      getConfiguration().getMetadataRepository().registerJndiMapping(url, cf.getClass().getName(), jndi);

      return result;
   }

   /**
    * Find the connection factory for a managed connection factory
    * @param clz The fully quilified class name for the managed connection factory
    * @param defs The connection definitions
    * @return The connection definiton; <code>null</code> if none could be found
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
               log.warn("Only one connection definitopn found with a mis-match in class-name: " + cd);
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
    * Create an instance of the pool configuration based on the input
    * @param pp The pool parameters
    * @param tp The timeout parameters
    * @param vp The validation parameters
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
      if (raConfiguration == null)
         throw new IllegalStateException("Configuration not defined");
   }

   /**
    * init the acrtivation spec
    *
    * @param  cl cl
    * @param  cmd cmd
    * @param  resourceAdapter resourceAdapter
    * @param  archiveValidationObjects archiveValidationObjects
    * @param  beanValidationObjects beanValidationObjects
    * @throws DeployException in case of error
    */
   protected Set<Failure> initActivationSpec(KernelClassLoader cl, Connector cmd, ResourceAdapter resourceAdapter,
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
               for (MessageListener mlMeta : mlMetas)
               {
                  if (mlMeta.getActivationspec() != null &&
                      mlMeta.getActivationspec().getActivationspecClass().getValue() != null)
                  {
                     failures = validateArchive(
                        url,
                        Arrays.asList((Validate) new ValidateClass(Key.ACTIVATION_SPEC, mlMeta.getActivationspec()
                           .getActivationspecClass().getValue(), cl, mlMeta.getActivationspec().getConfigProperties())),
                        failures);
                     if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                        Severity.ERROR)))
                     {
                        if (activateDeployment)
                        {
                           List<? extends ConfigProperty> cpm = mlMeta.getActivationspec().getConfigProperties();

                           Object o = initAndInject(mlMeta.getActivationspec().getActivationspecClass().getValue(),
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
    * @throws DeployException in case of errors
    */
   protected Set<Failure> initAdminObject(Connector cmd, KernelClassLoader cl, List<Validate> archiveValidationObjects,
      List<Object> beanValidationObjects, Set<Failure> failures, URL url, boolean activateDeployment)
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
               for (AdminObject aoMeta : aoMetas)
               {
                  if (aoMeta.getAdminobjectClass() != null && aoMeta.getAdminobjectClass().getValue() != null)
                  {
                     failures = validateArchive(url,
                        Arrays.asList((Validate) new ValidateClass(Key.ADMIN_OBJECT, aoMeta.getAdminobjectClass()
                           .getValue(), cl, aoMeta.getConfigProperties())), failures);
                     if (!(getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures,
                        Severity.ERROR)))
                     {
                        if (activateDeployment)
                        {
                           Object o = initAndInject(aoMeta.getAdminobjectClass().getValue(),
                              aoMeta.getConfigProperties(), cl);

                           if (trace)
                           {
                              log.trace("AdminObject: " + o.getClass().getName());
                              log.trace("AdminObject defined in classloader: " + o.getClass().getClassLoader());
                           }

                           archiveValidationObjects.add(new ValidateObject(Key.ADMIN_OBJECT, o, aoMeta
                              .getConfigProperties()));
                           beanValidationObjects.add(o);
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
    * FIXME Comment this
    *
    * @param url
    * @param f
    * @param root
    * @param destination
    * @param cl
    * @param cmd
    * @param ijmd
    * @return
    * @throws DeployException
    * @throws ResourceException
    * @throws Throwable
    * @throws ValidatorException
    * @throws AlreadyExistsException
    * @throws ClassNotFoundException
    */
   protected Deployment createObjectsAndInjectValue(URL url, String deploymentName, File root, File destination,
      KernelClassLoader cl, Connector cmd, IronJacamar ijmd, URL deployment) throws DeployException, ResourceException,
      Throwable, ValidatorException, AlreadyExistsException, ClassNotFoundException
   {
      Set<Failure> failures = null;
      try
      {
         // Notify regarding license terms
         if (cmd != null && cmd.getLicense() != null && cmd.getLicense().isLicenseRequired())
            log.info("Required license terms for " + url.toExternalForm());

         ResourceAdapter resourceAdapter = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Object> beanValidationObjects = new ArrayList<Object>();
         Object[] cfs = null;
         String[] jndiNames = null;

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
                     Arrays.asList((Validate) new ValidateClass(Key.RESOURCE_ADAPTER, ra1516.getResourceadapterClass(),
                                                                cl, cmd.getResourceadapter().getConfigProperties())),
                     failures);

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

                  if (ijmd != null)
                  {
                  ijCD = findConnectionDefinition(ra10.getManagedConnectionFactoryClass().getValue(),
                     ijmd.getConnectionDefinitions());
                  }
                  //
                  //                  if (ijmd == null || ijCD == null || ijCD.isEnabled())
                  if (ijCD == null || ijCD.isEnabled())
                  {
                     ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(ra10
                        .getManagedConnectionFactoryClass().getValue(), ra10.getConfigProperties(), cl);

                     if (trace)
                     {
                        log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                        log.trace("ManagedConnectionFactory defined in classloader: " + mcf.getClass().getClassLoader());
                     }

                     mcf.setLogWriter(new PrintWriter(getConfiguration().getPrintStream()));

                     archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY, mcf, ra10
                        .getConfigProperties()));
                     beanValidationObjects.add(mcf);
                     associateResourceAdapter(resourceAdapter, mcf);

                     // Create the pool
                     PoolConfiguration pc = createPoolConfiguration(ijCD != null ? ijCD.getPool() : null, ijCD != null
                        ? ijCD.getTimeOut()
                        : null, ijCD != null ? ijCD.getValidation() : null);
                     PoolFactory pf = new PoolFactory();

                     Boolean noTxSeparatePool = Boolean.FALSE;

                     if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                     {
                        CommonXaPool ijXaPool = (CommonXaPool) ijCD.getPool();

                        if (ijXaPool != null)
                           noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                     }

                     Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, noTxSeparatePool.booleanValue());

                     // Add a connection manager
                     ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                     ConnectionManager cm = null;

                     TransactionSupportEnum tsmd = TransactionSupportEnum.NoTransaction;

                     if (ijmd != null && ijmd.getTransactionSupport() != null)
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

                     if (ijCD != null && ijCD.getTimeOut() != null)
                     {
                        allocationRetry = ijCD.getTimeOut().getAllocationRetry();
                        allocationRetryWaitMillis = ijCD.getTimeOut().getAllocationRetryWaitMillis();
                     }

                     // Select the correct connection manager
                     if (tsl == TransactionSupportLevel.NoTransaction)
                     {
                        cm = cmf.createNonTransactional(tsl, pool, allocationRetry, allocationRetryWaitMillis);
                     }
                     else
                     {
                        Boolean interleaving = null;
                        Integer xaResourceTimeout = null;
                        Boolean isSameRMOverride = null;
                        Boolean wrapXAResource = null;
                        Boolean padXid = null;

                        if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                        {
                           CommonXaPool ijXaPool = (CommonXaPool) ijCD.getPool();

                           if (ijXaPool != null)
                           {
                              interleaving = ijXaPool.isInterleaving();
                              isSameRMOverride = ijXaPool.isSameRmOverride();
                              wrapXAResource = ijXaPool.isWrapXaDataSource();
                              padXid = ijXaPool.isPadXid();
                           }
                        }

                        cm = cmf.createTransactional(tsl, pool, allocationRetry, allocationRetryWaitMillis,
                           getConfiguration().getTransactionManager(), interleaving, xaResourceTimeout,
                           isSameRMOverride, wrapXAResource, padXid);
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
                        if (ijCD != null)
                        {
                           String jndiName = ijCD.getJndiName();

                        bindConnectionFactory(url, deploymentName, cf, jndiName);
                        cfs = new Object[]
                        { cf };
                        jndiNames = new String[]
                        { jndiName };

                        cm.setJndiName(jndiName);
                        }
                        else
                        {
                           jndiNames = bindConnectionFactory(url, deploymentName, cf);
                           cfs = new Object[]
                           { cf };

                           cm.setJndiName(jndiNames[0]);
                        }
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
                     jndiNames = new String[cdMetas.size()];

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

                              if (ijmd != null)
                              {
                                 ijCD = findConnectionDefinition(cdMeta.getManagedConnectionFactoryClass().getValue(),
                                    ijmd.getConnectionDefinitions());
                              }

//                              if (ijmd == null || ijCD == null || ijCD.isEnabled())
//                              {
                              if (ijCD == null || ijCD.isEnabled())
                              {
                                 ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(cdMeta
                                    .getManagedConnectionFactoryClass().getValue(), cdMeta.getConfigProperties(), cl);

                                 if (trace)
                                 {
                                    log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                                    log.trace("ManagedConnectionFactory defined in classloader: " +
                                              mcf.getClass().getClassLoader());
                                 }

                                 mcf.setLogWriter(new PrintWriter(getConfiguration().getPrintStream()));

                                 archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY, mcf,
                                                                                 cdMeta.getConfigProperties()));
                                 beanValidationObjects.add(mcf);
                                 associateResourceAdapter(resourceAdapter, mcf);

                                 // Create the pool
                                 PoolConfiguration pc = createPoolConfiguration(ijCD != null ? ijCD.getPool() : null,
                                    ijCD != null ? ijCD.getTimeOut() : null, ijCD != null ? ijCD.getValidation() : null);
                                 PoolFactory pf = new PoolFactory();

                                 Boolean noTxSeparatePool = Boolean.FALSE;

                                 if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                                 {
                                    org.jboss.jca.common.api.metadata.common.CommonXaPool ijXaPool = (org.jboss.jca.common.api.metadata.common.CommonXaPool) ijCD
                                       .getPool();

                                    if (ijXaPool != null)
                                       noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                                 }

                                 Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, noTxSeparatePool.booleanValue());

                                 // Add a connection manager
                                 ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                                 ConnectionManager cm = null;
                                 TransactionSupportLevel tsl = TransactionSupportLevel.NoTransaction;
                                 TransactionSupportEnum tsmd = TransactionSupportEnum.NoTransaction;

                                 if (ijmd != null && ijmd.getTransactionSupport() != null)
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

                                 if (ijCD != null && ijCD.getTimeOut() != null)
                                 {
                                    allocationRetry = ijCD.getTimeOut().getAllocationRetry();
                                    allocationRetryWaitMillis = ijCD.getTimeOut().getAllocationRetryWaitMillis();
                                 }

                                 // Select the correct connection manager
                                 if (tsl == TransactionSupportLevel.NoTransaction)
                                 {
                                    cm = cmf.createNonTransactional(tsl, pool, allocationRetry,
                                       allocationRetryWaitMillis);
                                 }
                                 else
                                 {
                                    Boolean interleaving = null;
                                    Integer xaResourceTimeout = null;
                                    Boolean isSameRMOverride = null;
                                    Boolean wrapXAResource = null;
                                    Boolean padXid = null;

                                    if (ijCD != null && ijCD.isXa())
                                    {
                                       CommonXaPool ijXaPool = (CommonXaPool) ijCD.getPool();

                                       interleaving = ijXaPool.isInterleaving();
                                       isSameRMOverride = ijXaPool.isSameRmOverride();
                                       wrapXAResource = ijXaPool.isWrapXaDataSource();
                                       padXid = ijXaPool.isPadXid();
                                    }

                                    cm = cmf.createTransactional(tsl, pool, allocationRetry, allocationRetryWaitMillis,
                                       getConfiguration().getTransactionManager(), interleaving, xaResourceTimeout,
                                       isSameRMOverride, wrapXAResource, padXid);
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
                                    if (ijCD != null)
                                    {
                                    String jndiName = ijCD.getJndiName();

                                    bindConnectionFactory(url, deploymentName, cf, jndiName);
                                    cfs[cdIndex] = cf;
                                    jndiNames[cdIndex] = jndiName;

                                    cm.setJndiName(jndiName);
                                    }
                                    else
                                    {
                                       jndiNames = bindConnectionFactory(url, deploymentName, cf);
                                       cfs = new Object[]
                                       { cf };

                                       cm.setJndiName(jndiNames[0]);
                                    }

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
               activateDeployment);
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
               getConfiguration().getMetadataRepository().registerResourceAdapter(url, root, cmd, ijmd);
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

               if (ijmd != null && ijmd.getBeanValidationGroups() != null && ijmd.getBeanValidationGroups().size() > 0)
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

               if (ijmd != null)
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

         return createDeployment(url, deploymentName, activateDeployment, resourceAdapter, getConfiguration()
            .getJndiStrategy(), getConfiguration().getMetadataRepository(), cfs, destination, cl, log, jndiNames,
            deployment, activateDeployment);

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
                                      new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures,
                                         null), failures));
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
            throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
         }
      }
   }

   /**
    * Check if the resource adapter should be activated based on the ironjacamar.xml input
    * @param cmd The connector metadata
    * @param ijmd The IronJacamar metadata
    * @return True if the deployment should be activated; otherwise false
    */
   protected abstract boolean checkActivation(Connector cmd, IronJacamar ijmd);

   public abstract Deployment createDeployment(URL deploymentUrl, String deploymentName, boolean activator,
      ResourceAdapter resourceAdapter, JndiStrategy jndiStrategy, MetadataRepository metadataRepository, Object[] cfs,
      File destination, ClassLoader cl, Logger log, String[] jndis, URL deployment, boolean activateDeployemnt);
}
