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

import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.Activationspec;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.rar.Endpoint;
import org.jboss.jca.core.spi.rar.NotFoundException;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * A simple implementation of the resource adapter repository
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SimpleResourceAdapterRepository implements ResourceAdapterRepository
{   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
      SimpleResourceAdapterRepository.class.getName());

   /** The approved types */
   private static Set<Class<?>> approvedTypes;

   /** Resource adapters */
   private Map<String, WeakReference<javax.resource.spi.ResourceAdapter>> rars;

   /** Ids */
   private Map<String, AtomicInteger> ids;

   /** Recovery */
   private Map<String, Boolean> recovery;

   /** The metadata repository */
   private MetadataRepository mdr;

   /** The transaction integration */
   private TransactionIntegration transactionIntegration;

   // We include the primitive types because we can handle those
   static
   {
      approvedTypes = new HashSet<Class<?>>();
      approvedTypes.add(boolean.class);
      approvedTypes.add(Boolean.class);
      approvedTypes.add(byte.class);
      approvedTypes.add(Byte.class);
      approvedTypes.add(short.class);
      approvedTypes.add(Short.class);
      approvedTypes.add(int.class);
      approvedTypes.add(Integer.class);
      approvedTypes.add(long.class);
      approvedTypes.add(Long.class);
      approvedTypes.add(float.class);
      approvedTypes.add(Float.class);
      approvedTypes.add(double.class);
      approvedTypes.add(Double.class);
      approvedTypes.add(char.class);
      approvedTypes.add(Character.class);
      approvedTypes.add(String.class);
   }

   /**
    * Constructor
    */
   public SimpleResourceAdapterRepository()
   {
      this.rars = new HashMap<String, WeakReference<javax.resource.spi.ResourceAdapter>>();
      this.ids = new HashMap<String, AtomicInteger>();
      this.recovery = new HashMap<String, Boolean>();
      this.mdr = null;
      this.transactionIntegration = null;
   }

   /**
    * Set the metadata repository
    * @param v The value
    */
   public synchronized void setMetadataRepository(MetadataRepository v)
   {
      this.mdr = v;
   }

   /**
    * Set the transaction integration
    * @param v The value
    */
   public synchronized void setTransactionIntegration(TransactionIntegration v)
   {
      this.transactionIntegration = v;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized String registerResourceAdapter(javax.resource.spi.ResourceAdapter ra) 
   {
      if (ra == null)
         throw new IllegalArgumentException("ResourceAdapter is null");

      String clzName = ra.getClass().getName();

      AtomicInteger id = ids.get(clzName);
      if (id == null)
      {
         id = new AtomicInteger(0);
         ids.put(clzName, id);
      }

      String key = clzName + "#" + id.incrementAndGet();

      rars.put(key, new WeakReference<javax.resource.spi.ResourceAdapter>(ra));

      return key;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void unregisterResourceAdapter(String key) throws NotFoundException
   {
      if (key == null)
         throw new IllegalArgumentException("Key is null");

      if (!rars.keySet().contains(key))
         throw new NotFoundException(bundle.keyNotRegistered(key));

      rars.remove(key);
      recovery.remove(key);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized javax.resource.spi.ResourceAdapter getResourceAdapter(String uniqueId) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!rars.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      WeakReference<javax.resource.spi.ResourceAdapter> ra = rars.get(uniqueId);

      if (ra.get() == null)
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      return ra.get();
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Set<String> getResourceAdapters()
   {
      return Collections.unmodifiableSet(rars.keySet());
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Set<String> getResourceAdapters(Class<?> messageListenerType)
   {
      if (messageListenerType == null)
         throw new IllegalArgumentException("MessageListenerType is null");
      
      if (mdr == null)
         throw new IllegalStateException("MDR is null");
      
      Set<String> result = new HashSet<String>();

      Iterator<Map.Entry<String, WeakReference<javax.resource.spi.ResourceAdapter>>> it = rars.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<String, WeakReference<javax.resource.spi.ResourceAdapter>> entry = it.next();

         String raKey = entry.getKey();
         WeakReference<javax.resource.spi.ResourceAdapter> ra = entry.getValue();

         if (ra.get() != null)
         {
            javax.resource.spi.ResourceAdapter rar = ra.get();
            Connector md = null;

            Set<String> mdrKeys = mdr.getResourceAdapters();
            Iterator<String> mdrIt = mdrKeys.iterator();

            while (md == null && mdrIt.hasNext())
            {
               String mdrId = mdrIt.next();
               try
               {
                  Connector c = mdr.getResourceAdapter(mdrId);
            
                  if (c.getResourceadapter() != null)
                  {
                     String clz = c.getResourceadapter().getResourceadapterClass();

                     if (rar.getClass().getName().equals(clz))
                        md = c;
                  }
               }
               catch (Throwable t)
               {
                  // We will ignore
                  log.debugf("Resource adapter %s is ignored", rar.getClass().getName()); 
               }
            }

            if (md != null && md.getResourceadapter() != null)
            {
               org.jboss.jca.common.api.metadata.spec.ResourceAdapter raSpec = md.getResourceadapter();

               if (raSpec.getInboundResourceadapter() != null &&
                   raSpec.getInboundResourceadapter().getMessageadapter() != null &&
                   raSpec.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null &&
                   raSpec.getInboundResourceadapter().getMessageadapter().getMessagelisteners().size() > 0)
               {
                  List<org.jboss.jca.common.api.metadata.spec.MessageListener> listeners =
                     raSpec.getInboundResourceadapter().getMessageadapter().getMessagelisteners();

                  for (org.jboss.jca.common.api.metadata.spec.MessageListener ml : listeners)
                  {
                     try
                     {
                        ClassLoader cl = SecurityActions.getClassLoader(rar.getClass());
                        Class<?> mlType = Class.forName(ml.getMessagelistenerType().getValue(), true, cl);

                        if (mlType.isAssignableFrom(messageListenerType))
                           result.add(raKey);
                     }
                     catch (Throwable t)
                     {
                        // We will ignore
                     }
                  }
               }
            }
         }
      }

      return Collections.unmodifiableSet(result);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Endpoint getEndpoint(String uniqueId) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!rars.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      WeakReference<javax.resource.spi.ResourceAdapter> ra = rars.get(uniqueId);

      if (ra.get() == null)
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      String mdrIdentifier = getMDRIdentifier(ra.get());
      boolean is16 = is16(mdrIdentifier);
      Set<String> beanValidationGroups = getBeanValidationGroups(mdrIdentifier);
      String productName = getProductName(mdrIdentifier);
      String productVersion = getProductVersion(mdrIdentifier);
      Boolean isXA = recovery.get(uniqueId);

      if (isXA == null)
         isXA = Boolean.TRUE;

      return new EndpointImpl(ra, is16, beanValidationGroups,
                              productName, productVersion, transactionIntegration, isXA.booleanValue());
   }

   /**
    * {@inheritDoc}
    */
   public synchronized List<org.jboss.jca.core.spi.rar.MessageListener> getMessageListeners(String uniqueId)
      throws NotFoundException, InstantiationException, IllegalAccessException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!rars.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      WeakReference<javax.resource.spi.ResourceAdapter> ra = rars.get(uniqueId);

      if (ra.get() == null)
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      if (mdr == null)
         throw new IllegalStateException("MDR is null");
      
      javax.resource.spi.ResourceAdapter rar = ra.get();
      Connector md = null;

      Set<String> mdrKeys = mdr.getResourceAdapters();
      Iterator<String> mdrIt = mdrKeys.iterator();

      while (md == null && mdrIt.hasNext())
      {
         String mdrId = mdrIt.next();
         try
         {
            Connector c = mdr.getResourceAdapter(mdrId);
            
            if (c.getResourceadapter() != null)
            {
               String clz = c.getResourceadapter().getResourceadapterClass();

               if (rar.getClass().getName().equals(clz))
                  md = c;
            }
         }
         catch (Throwable t)
         {
            throw new NotFoundException(bundle.unableLookupResourceAdapterInMDR(uniqueId), t);
         }
      }

      if (md == null)
         throw new NotFoundException(bundle.unableLookupResourceAdapterInMDR(uniqueId));

      if (md.getResourceadapter() != null)
      {
         org.jboss.jca.common.api.metadata.spec.ResourceAdapter raSpec = md.getResourceadapter();

         if (raSpec.getInboundResourceadapter() != null &&
             raSpec.getInboundResourceadapter().getMessageadapter() != null &&
             raSpec.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null &&
             raSpec.getInboundResourceadapter().getMessageadapter().getMessagelisteners().size() > 0)
         {
            List<org.jboss.jca.common.api.metadata.spec.MessageListener> listeners =
               raSpec.getInboundResourceadapter().getMessageadapter().getMessagelisteners();

            List<org.jboss.jca.core.spi.rar.MessageListener> result =
               new ArrayList<org.jboss.jca.core.spi.rar.MessageListener>(listeners.size());

            for (org.jboss.jca.common.api.metadata.spec.MessageListener ml : listeners)
            {
               result.add(createMessageListener(rar, ml));
            }

            return result;
         }
      }

      return Collections.emptyList();
   }


   /**
    * {@inheritDoc}
    */
   public void setRecoveryForResourceAdapter(String uniqueId, boolean isXA) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!rars.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      recovery.put(uniqueId, isXA ? Boolean.TRUE : Boolean.FALSE);
   }

   /**
    * Create a message listener instance
    * @param rar The resource adapter
    * @param ml The metadata for the message listener
    * @return The instance
    * @exception InstantiationException Thrown if an object couldn't created
    * @exception IllegalAccessException Thrown if object access is inaccessible
    */
   private org.jboss.jca.core.spi.rar.MessageListener 
   createMessageListener(javax.resource.spi.ResourceAdapter rar,
                         org.jboss.jca.common.api.metadata.spec.MessageListener ml)
      throws InstantiationException, IllegalAccessException
   {
      try
      {
         ClassLoader cl = SecurityActions.getClassLoader(rar.getClass());

         Class<?> type = Class.forName(ml.getMessagelistenerType().getValue(), true, cl);

         Map<String, Class<?>> configProperties = new HashMap<String, Class<?>>();
         Set<String> requiredConfigProperties = new HashSet<String>();
         Map<String, String> valueProperties = new HashMap<String, String>();

         Activationspec as = ml.getActivationspec();
         Class<?> asClz = Class.forName(as.getActivationspecClass().getValue(), true, cl);

         if (as.getConfigProperties() != null && as.getConfigProperties().size() > 0)
         {
            for (ConfigProperty cp : as.getConfigProperties())
            {
               String name = cp.getConfigPropertyName().getValue();
               Class<?> ct = Class.forName(cp.getConfigPropertyType().getValue(), true, cl);

               configProperties.put(name, ct);

               if (cp.getConfigPropertyValue() != null && cp.getConfigPropertyValue().getValue() != null)
                  valueProperties.put(name, cp.getConfigPropertyValue().getValue());
            }
         }

         configProperties.putAll(introspectActivationSpec(asClz));

         List<? extends RequiredConfigProperty> rcps = as.getRequiredConfigProperties();
         if (rcps != null && rcps.size() > 0)
         {
            for (RequiredConfigProperty rcp : rcps)
            {
               String name = rcp.getConfigPropertyName().getValue();

               requiredConfigProperties.add(name);
            }
         }

         ActivationImpl a = new ActivationImpl(rar,
                                               asClz,
                                               Collections.unmodifiableMap(configProperties),
                                               Collections.unmodifiableSet(requiredConfigProperties),
                                               Collections.unmodifiableMap(valueProperties));

         return new MessageListenerImpl(type, a);
      }
      catch (ClassNotFoundException cnfe)
      {
         InstantiationException ie = new InstantiationException("Unable to create representation");
         ie.initCause(cnfe);
         throw ie;
      }
   }

   /**
    * Introspect an activation spec class for config-property's
    * @param clz The class
    * @return The introspected map
    */
   private Map<String, Class<?>> introspectActivationSpec(Class<?> clz)
   {
      Map<String, Class<?>> result = new HashMap<String, Class<?>>();

      if (clz != null)
      {
         Method[] methods = SecurityActions.getMethods(clz);
         if (methods.length > 0)
         {
            for (int i = 0; i < methods.length; i++)
            {
               Method m = methods[i];

               if (m.getName().startsWith("set") && m.getParameterTypes().length == 1)
               {
                  Class<?> parameterType = m.getParameterTypes()[0];
                  
                  if (approvedTypes.contains(parameterType))
                  {
                     String n = m.getName().substring(3);
                     String name = n.substring(0, 1).toLowerCase(Locale.US);

                     if (n.length() > 1)
                        name = name.concat(n.substring(1));

                     result.put(name, parameterType);
                  }
               }
            }
         }
      }

      return result;
   }

   /**
    * Get MDR identifier
    * @param ra The resource adapter
    * @return The identifier
    */
   private String getMDRIdentifier(javax.resource.spi.ResourceAdapter ra)
   {
      for (String id : mdr.getResourceAdapters())
      {
         try
         {
            Connector raXml = mdr.getResourceAdapter(id);
            if (raXml != null)
            {
               if (raXml.getResourceadapter() != null)
               {
                  org.jboss.jca.common.api.metadata.spec.ResourceAdapter raSpec = raXml.getResourceadapter();
                  if (raSpec.getResourceadapterClass() != null && !raSpec.getResourceadapterClass().equals(""))
                  {
                     if (ra.getClass().getName().equals(raSpec.getResourceadapterClass()))
                        return id;
                  }
               }
            }
         }
         catch (Throwable t)
         {
            log.debugf(t, "Exception while loading id: %s", id);
         }
      }

      return null;
   }

   /**
    * Is the resource adapter a 1.6 archive
    * @param id The MDR identifier
    * @return True if 1.6; otherwise false
    */
   private boolean is16(String id)
   {
      if (id == null || id.equals(""))
         return false;

      try
      {
         Connector raXml = mdr.getResourceAdapter(id);
         if (raXml != null)
         {
            return (raXml.getVersion() == Version.V_16 || raXml.getVersion() == Version.V_17);
         }
      }
      catch (Throwable t)
      {
         log.debugf(t, "Exception while loading ra.xml: %s", id);
      }

      return false;
   }

   /**
    * Get the bean validation groups
    * @param id The MDR identifier
    * @return The groups; <code>null</code> if none were found
    */
   private Set<String> getBeanValidationGroups(String id)
   {
      if (id == null || id.equals(""))
         return null;

      try
      {
         Activation a = mdr.getActivation(id);
         if (a != null && a.getBeanValidationGroups() != null && a.getBeanValidationGroups().size() > 0)
         {
            Set<String> groups = new HashSet<String>();
            for (String group : a.getBeanValidationGroups())
            {
               groups.add(group);
            }
            return groups;
         }
      }
      catch (Throwable t)
      {
         log.debugf(t, "Exception while loading ironjacamar.xml: %s", id);
      }

      return null;
   }

   /**
    * Get the product name for the resource adapter
    * @param id The MDR identifier
    * @return The value
    */
   private String getProductName(String id)
   {
      if (id == null || id.equals(""))
         return "";

      try
      {
         Connector raXml = mdr.getResourceAdapter(id);
         if (raXml != null && !XsdString.isNull(raXml.getEisType()))
         {
            return raXml.getEisType().getValue();
         }
      }
      catch (Throwable t)
      {
         log.debugf(t, "Exception while loading ra.xml: %s", id);
      }

      return "";
   }

   /**
    * Get the product version for the resource adapter
    * @param id The MDR identifier
    * @return The value
    */
   private String getProductVersion(String id)
   {
      if (id == null || id.equals(""))
         return "";

      try
      {
         Connector raXml = mdr.getResourceAdapter(id);
         if (raXml != null)
         {
            if (!XsdString.isNull(raXml.getResourceadapterVersion()))
               return raXml.getResourceadapterVersion().getValue();
         }
      }
      catch (Throwable t)
      {
         log.debugf(t, "Exception while loading ra.xml: %s", id);
      }

      return "";
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("SimpleResourceAdapterRepository@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[");
      sb.append(" rars=").append(rars);
      sb.append(" ids=").append(ids);
      sb.append(" mdr=").append(mdr);
      sb.append(" ti=").append(transactionIntegration);
      sb.append("]");

      return sb.toString();
   }
}
