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
import org.jboss.jca.core.spi.rar.Activation;
import org.jboss.jca.core.spi.rar.NotFoundException;
import org.jboss.jca.core.util.Injection;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * An activation implementation
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ActivationImpl implements Activation
{   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
      ActivationImpl.class.getName());

   /** Resource adapter */
   private WeakReference<ResourceAdapter> rar;

   /** ActivationSpec class */
   private WeakReference<Class<?>> activationSpecClass;

   /** Config properties */
   private Map<String, Class<?>> configProperties;

   /** Required config properties */
   private Set<String> requiredConfigProperties;

   /** Value properties */
   private Map<String, String> valueProperties;

   /**
    * Constructor
    * @param rar The resource adapter
    * @param activationSpecClass The activation spec class
    * @param configProperties The config properties
    * @param requiredConfigProperties The required config properties
    * @param valueProperties The value properties
    */
   ActivationImpl(ResourceAdapter rar,
                  Class<?> activationSpecClass,
                  Map<String, Class<?>> configProperties,
                  Set<String> requiredConfigProperties,
                  Map<String, String> valueProperties)
   {
      this.rar = new WeakReference<ResourceAdapter>(rar);
      this.activationSpecClass = new WeakReference<Class<?>>(activationSpecClass);
      this.configProperties = configProperties;
      this.requiredConfigProperties = requiredConfigProperties;
      this.valueProperties = valueProperties;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, Class<?>> getConfigProperties()
   {
      return configProperties;
   }

   /**
    * {@inheritDoc}
    */
   public Set<String> getRequiredConfigProperties()
   {
      return requiredConfigProperties;
   }

   /**
    * {@inheritDoc}
    */
   public ActivationSpec createInstance()
      throws NotFoundException, InstantiationException, IllegalAccessException, ResourceException
   {
      Class<?> clz = activationSpecClass.get();

      if (clz == null)
         throw new NotFoundException(bundle.activationSpecClassNotAvailable());

      ResourceAdapter ra = rar.get();

      if (ra == null)
         throw new NotFoundException(bundle.resourceAdapterNotAvailable());

      ActivationSpec instance = ActivationSpec.class.cast(clz.newInstance());
      instance.setResourceAdapter(ra);

      if (valueProperties != null && valueProperties.size() > 0)
      {
         Injection injector = new Injection();
         Iterator<Map.Entry<String, String>> it = valueProperties.entrySet().iterator();
         while (it.hasNext())
         {
            String propertyName = null;
            String propertyValue = null;
            try
            {
               Map.Entry<String, String> entry = it.next();

               propertyName = entry.getKey();
               propertyValue = entry.getValue();
               
               injector.inject(instance, propertyName, propertyValue);
            }
            catch (Throwable t)
            {
               log.debugf(t, "Ignoring: %s (%s)", propertyName, propertyValue);
            }
         }
      }

      return instance;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ActivationImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[rar=").append(rar != null ? rar.get() : "null");
      sb.append(" activationSpecClass=").append(activationSpecClass != null ? activationSpecClass.get() : "null");
      sb.append(" configProperties=").append(configProperties);
      sb.append(" requiredConfigProperties=").append(requiredConfigProperties);
      sb.append(" valueProperties=").append(valueProperties);
      sb.append("]");

      return sb.toString();
   }
}
