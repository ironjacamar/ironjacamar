/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.deploymentrepository;

import org.ironjacamar.core.api.deploymentrepository.ConfigProperty;
import org.ironjacamar.core.api.deploymentrepository.InflowRecovery;
import org.ironjacamar.core.api.deploymentrepository.MessageListener;
import org.ironjacamar.core.api.deploymentrepository.ResourceAdapter;
import org.ironjacamar.core.bootstrapcontext.CloneableBootstrapContext;
import org.ironjacamar.core.spi.bv.BeanValidation;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * A resource adapter implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterImpl implements ResourceAdapter
{
   /** Activated */
   private boolean activated;

   /** The resource adapter */
   private javax.resource.spi.ResourceAdapter resourceAdapter;

   /** The BootstrapContext */
   private BootstrapContext bc;
   
   /** The config properties */
   private Collection<ConfigProperty> configProperties;

   /** The statistics */
   private StatisticsPlugin statistics;

   /** The product name */
   private String productName;
   
   /** The product version */
   private String productVersion;
   
   /** The message listeners */
   private Map<String, ActivationSpecImpl> messageListeners;

   /** Is a 1.6+ archive */
   private boolean is16;

   /** Bean validation */
   private BeanValidation beanValidation;

   /** Bean validation groups */
   private List<String> bvGroups;

   /** Transaction integration */
   private TransactionIntegration transactionIntegration;
   
   /** Active endpoints */
   private Map<Endpoint, InflowRecovery> activeEndpoints;

   /**
    * Constructor
    * @param resourceAdapter The resource adapter
    * @param bc The BootstrapContext
    * @param configProperties The configuration properties
    * @param statistics The statistics
    * @param productName The product name
    * @param productVersion The product version
    * @param messageListeners The message listeners
    * @param is16 Is a 1.6+ archive
    * @param beanValidation Bean validation
    * @param bvGroups The bean validation groups
    * @param ti The transaction integration
    */
   public ResourceAdapterImpl(javax.resource.spi.ResourceAdapter resourceAdapter,
                              BootstrapContext bc,
                              Collection<ConfigProperty> configProperties,
                              StatisticsPlugin statistics,
                              String productName, String productVersion,
                              Map<String, ActivationSpecImpl> messageListeners,
                              boolean is16, BeanValidation beanValidation, List<String> bvGroups,
                              TransactionIntegration ti)
   {
      this.activated = false;
      this.resourceAdapter = resourceAdapter;
      this.bc = bc;
      this.configProperties = configProperties;
      this.statistics = statistics;
      this.productName = productName;
      this.productVersion = productVersion;
      this.messageListeners = messageListeners;
      this.is16 = is16;
      this.beanValidation = beanValidation;
      this.bvGroups = bvGroups;
      this.activeEndpoints = new HashMap<>();
      this.transactionIntegration = ti;
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean isActivated()
   {
      return activated;
   }

   /**
    * {@inheritDoc}
    */
   public javax.resource.spi.ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * {@inheritDoc}
    */
   public BootstrapContext getBootstrapContext()
   {
      return bc;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }

   /**
    * {@inheritDoc}
    */
   public StatisticsPlugin getStatistics()
   {
      return statistics;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMessageListenerSupported(String ml)
   {
      if (messageListeners != null && ml != null)
         return messageListeners.containsKey(ml);

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public MessageListener createMessageListener(String ml) throws Exception
   {
      if (isMessageListenerSupported(ml))
      {
         Class<?> mlClz = Class.forName(ml, true, resourceAdapter.getClass().getClassLoader());
         return new MessageListenerImpl(mlClz, messageListeners.get(ml), resourceAdapter.getClass().getClassLoader());
      }

      throw new Exception(ml + " not supported by " + resourceAdapter.getClass().getName());
   }
   
   /**
    * {@inheritDoc}
    */
   public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws Exception
   {
      if (mef == null)
         throw new Exception("MessageEndpointFactory is null");

      if (as == null)
         throw new Exception("ActivationSpec is null");

      as.setResourceAdapter(resourceAdapter);
      
      try
      {
         as.validate();
      }
      catch (UnsupportedOperationException uoe)
      {
         // Ignore
      }

      if (is16)
      {
         verifyBeanValidation(as);
      }
      
      resourceAdapter.endpointActivation(mef, as);

      InflowRecovery ir = null;
      if (transactionIntegration != null && transactionIntegration.getRecoveryRegistry() != null)
      {
         XAResourceRecovery xar = transactionIntegration.createXAResourceRecovery(resourceAdapter,
                                                                                  as,
                                                                                  productName, productVersion);

         ir = new InflowRecoveryImpl(mef, as, xar, transactionIntegration.getRecoveryRegistry());
         ir.activate();
      }

      activeEndpoints.put(new Endpoint(mef, as), ir);
   }

   /**
    * {@inheritDoc}
    */
   public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) throws Exception
   {
      if (mef == null)
         throw new Exception("MessageEndpointFactory is null");

      if (as == null)
         throw new Exception("ActivationSpec is null");

      Endpoint e = new Endpoint(mef, as);
      InflowRecovery ir = activeEndpoints.get(e);

      if (ir != null)
         ir.deactivate();
      
      try
      {
         resourceAdapter.endpointDeactivation(mef, as);
      }
      finally
      {
         activeEndpoints.remove(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Collection<InflowRecovery> getRecovery()
   {
      return Collections.unmodifiableCollection(activeEndpoints.values());
   }

   /**
    * {@inheritDoc}
    */
   public boolean activate() throws Exception
   {
      if (!activated)
      {
         resourceAdapter.start(bc);

         activated = true;
         return true;
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean deactivate() throws Exception
   {
      if (activated)
      {
         if (!activeEndpoints.isEmpty())
            throw new Exception("Still active endpoints");

         resourceAdapter.stop();
         ((CloneableBootstrapContext) bc).shutdown();
         activated = false;
         return true;
      }

      return false;
   }

   /**
    * Verify activation spec against bean validation
    * @param as The activation spec
    * @exception Exception Thrown in case of a violation
    */
   @SuppressWarnings("unchecked")
   private void verifyBeanValidation(Object as) throws Exception
   {
      if (beanValidation != null)
      {
         ValidatorFactory vf = null;

         try
         {
            vf = beanValidation.getValidatorFactory();
            Validator v = vf.getValidator();

            Collection<String> l = bvGroups;
            if (l == null || l.isEmpty())
               l = Arrays.asList(javax.validation.groups.Default.class.getName());

            Collection<Class<?>> groups = new ArrayList<>();
            for (String clz : l)
            {
               groups.add(Class.forName(clz, true, resourceAdapter.getClass().getClassLoader()));
            }
         
            Set failures = v.validate(as, groups.toArray(new Class<?>[groups.size()]));
         
            if (!failures.isEmpty())
            {
               throw new ConstraintViolationException("Violation for " + as, failures);
            }
         }
         finally
         {
            if (vf != null)
               vf.close();
         }
      }
   }

   /**
    * Endpoint
    */
   static class Endpoint
   {
      private MessageEndpointFactory mef;
      private ActivationSpec as;

      /**
       * Constructor
       * @param mef The MessageEndpointFactory
       * @param as The ActivationSpec
       */
      Endpoint(MessageEndpointFactory mef, ActivationSpec as)
      {
         this.mef = mef;
         this.as = as;
      }

      /**
       * {@inheritDoc}
       */
      public int hashCode()
      {
         int hash = 7;
         hash += 7 * mef.hashCode();
         hash += 7 * as.hashCode();
         return hash;
      }

      /**
       * {@inheritDoc}
       */
      public boolean equals(Object o)
      {
         if (o == this)
            return true;

         if (o == null || !(o instanceof Endpoint))
            return false;

         Endpoint e = (Endpoint)o;

         if (mef.equals(e.mef) && as.equals(e.as))
            return true;

         return false;
      }
   }
}
