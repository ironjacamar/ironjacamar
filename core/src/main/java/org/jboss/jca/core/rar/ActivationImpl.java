/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.spi.rar.Activation;
import org.jboss.jca.core.spi.rar.NotFoundException;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;

/**
 * An activation implementation
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ActivationImpl implements Activation
{
   /** Resource adapter */
   private WeakReference<ResourceAdapter> rar;

   /** ActivationSpec class */
   private WeakReference<Class<?>> activationSpecClass;

   /** Config properties */
   private Map<String, Class<?>> configProperties;

   /** Required config properties */
   private Set<String> requiredConfigProperties;

   /**
    * Constructor
    * @param rar The resource adapter
    * @param activationSpecClass The activation spec class
    * @param configProperties The config properties
    * @param requiredConfigProperties The required config properties
    */
   ActivationImpl(ResourceAdapter rar,
                  Class<?> activationSpecClass,
                  Map<String, Class<?>> configProperties,
                  Set<String> requiredConfigProperties)
   {
      this.rar = new WeakReference<ResourceAdapter>(rar);
      this.activationSpecClass = new WeakReference<Class<?>>(activationSpecClass);
      this.configProperties = configProperties;
      this.requiredConfigProperties = requiredConfigProperties;
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
         throw new NotFoundException("The activation spec class is no longer available");

      ResourceAdapter ra = rar.get();

      if (ra == null)
         throw new NotFoundException("The resource adapter is no longer available");

      ActivationSpec instance = ActivationSpec.class.cast(clz.newInstance());
      instance.setResourceAdapter(ra);

      return instance;
   }
}
