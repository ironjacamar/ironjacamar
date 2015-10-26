/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.rar;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.bv.BeanValidationUtil;
import org.jboss.jca.core.spi.rar.Endpoint;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * An endpoint representation
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class EndpointImpl implements Endpoint
{
   /** The reference to the resource adapter instance */
   private WeakReference<ResourceAdapter> ra;

   /** Is the resource adapter a 1.6 archive */
   private boolean is16;

   /** Bean validation groups */
   private Set<String> beanValidationGroups;
   
   /** The product name */
   private String productName;

   /** The product version */
   private String productVersion;

   /** Transaction integration */
   private TransactionIntegration transactionIntegration;

   /** XA capable */
   private boolean xa;

   /** The active recovery modules */
   private Map<ActivationSpec, XAResourceRecovery> recovery;

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, Endpoint.class.getName());

   /** Is bean validation for inflow enabled */
   private static boolean bvEnabled;

   static
   {
      bvEnabled = Boolean.valueOf(SecurityActions.getSystemProperty("ironjacamar.bv.inflow", "true"));
   }

   /**
    * Constructor
    * @param ra The resource adapter reference
    * @param is16 Is the resource adapter a 1.6 archive
    * @param bvg The bean validation groups
    * @param productName The product name
    * @param productVersion The product version
    * @param ti The transaction integration
    * @param xa XA capable
    */
   EndpointImpl(WeakReference<ResourceAdapter> ra, boolean is16, Set<String> bvg,
                String productName, String productVersion, TransactionIntegration ti,
                boolean xa)
   {
      this.ra = ra;
      this.is16 = is16;
      this.beanValidationGroups = bvg;
      this.productName = productName;
      this.productVersion = productVersion;
      this.transactionIntegration = ti;
      this.xa = xa;
      this.recovery = new HashMap<ActivationSpec, XAResourceRecovery>();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void activate(MessageEndpointFactory endpointFactory,
                        ActivationSpec spec) throws ResourceException
   {
      if (endpointFactory == null)
         throw new IllegalArgumentException("MessageEndpointFactory is null");

      if (spec == null)
         throw new IllegalArgumentException("ActivationSpec is null");

      ResourceAdapter rar = ra.get();

      if (rar == null)
         throw new ResourceException(bundle.resourceAdapterInstanceNotActive());

      if (is16 && bvEnabled)
      {
         ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
         try
         {
            SecurityActions.setThreadContextClassLoader(SecurityActions.getClassLoader(rar.getClass()));
            
            List<Class<?>> groups = new ArrayList<Class<?>>(1);

            if (beanValidationGroups != null)
            {
               for (String group : beanValidationGroups)
               {
                  try
                  {
                     Class<?> clz = Class.forName(group, true, SecurityActions.getClassLoader(rar.getClass()));
                     groups.add(clz);
                  }
                  catch (Throwable t)
                  {
                     log.debugf(t, "Unable to load bean validation group: %s", group);
                  }
               }
            }

            if (groups.isEmpty())
               groups.add(Default.class);

            Validator validator = BeanValidationUtil.createValidator();
            Class[] vargs = groups.toArray(new Class[groups.size()]);

            Set errors = validator.validate(spec, vargs);

            if (errors != null && errors.size() > 0)
            {
               throw new ResourceException(bundle.validationException(), new ConstraintViolationException(errors));
            }
         }
         catch (RuntimeException re)
         {
            throw new ResourceException(bundle.validationException(), re);
         }
         finally
         {
            SecurityActions.setThreadContextClassLoader(oldTCCL);
         }
      }

      try
      {
         spec.validate();
      }
      catch (UnsupportedOperationException uoe)
      {
         // Ignore
      }

      rar.endpointActivation(endpointFactory, spec);

      if (transactionIntegration != null && transactionIntegration.getRecoveryRegistry() != null && xa)
      {
         XAResourceRecovery xrr = transactionIntegration.createXAResourceRecovery(rar, spec,
                                                                                  productName, productVersion);

         log.tracef("Adding %s for recovery", xrr);

         try
         {
            xrr.initialize();
            transactionIntegration.getRecoveryRegistry().addXAResourceRecovery(xrr);
            recovery.put(spec, xrr);
         }
         catch (Exception e)
         {
            throw new ResourceException(bundle.errorDuringRecoveryInitialization(), e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deactivate(MessageEndpointFactory endpointFactory,
                          ActivationSpec spec) throws ResourceException
   {
      if (endpointFactory == null)
         throw new IllegalArgumentException("MessageEndpointFactory is null");

      if (spec == null)
         throw new IllegalArgumentException("ActivationSpec is null");

      ResourceAdapter rar = ra.get();

      if (rar == null)
         throw new ResourceException(bundle.resourceAdapterInstanceNotActive());

      if (transactionIntegration != null && transactionIntegration.getRecoveryRegistry() != null && xa)
      {
         XAResourceRecovery xrr = recovery.remove(spec);
         if (xrr != null)
         {
            log.tracef("Removing %s for recovery", xrr);

            try
            {
               xrr.shutdown();
            }
            catch (Exception e)
            {
               throw new ResourceException(bundle.errorDuringRecoveryShutdown(), e);
            }
            finally
            {
               transactionIntegration.getRecoveryRegistry().removeXAResourceRecovery(xrr);
            }
         }
      }

      rar.endpointDeactivation(endpointFactory, spec);
   }
}
