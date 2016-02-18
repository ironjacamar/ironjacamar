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

import org.ironjacamar.core.api.deploymentrepository.AdminObject;
import org.ironjacamar.core.api.deploymentrepository.ConfigProperty;
import org.ironjacamar.core.spi.naming.JndiStrategy;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.resource.spi.ResourceAdapterAssociation;

/**
 * An admin object implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AdminObjectImpl implements AdminObject
{
   /** Activated */
   private boolean activated;

   /** JNDI name */
   private String jndiName;

   /** The admin object */
   private Object adminObject;
   
   /** The JNDI object */
   private Object jndiObject;
   
   /** Config properties */
   private Collection<ConfigProperty> configProperties;

   /** Activation */
   private org.ironjacamar.common.api.metadata.resourceadapter.AdminObject activation;

   /** Statistics */
   private StatisticsPlugin statistics;
   
   /** The JNDI strategy */
   private JndiStrategy jndiStrategy;
   
   /**
    * Constructor
    * @param jndiName The JNDI name
    * @param ao The admin object
    * @param configProperties The configuration properties
    * @param activation The activation
    * @param statistics The statistics
    * @param jndiStrategy The JNDI strategy
    */
   public AdminObjectImpl(String jndiName,
                          Object ao,
                          Collection<ConfigProperty> configProperties,
                          org.ironjacamar.common.api.metadata.resourceadapter.AdminObject activation,
                          StatisticsPlugin statistics,
                          JndiStrategy jndiStrategy)
   {
      this.activated = false;
      this.jndiName = jndiName;
      this.adminObject = ao;
      this.jndiObject = null;
      this.configProperties = configProperties;
      this.activation = activation;
      this.statistics = statistics;
      this.jndiStrategy = jndiStrategy;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getJndiName()
   {
      return jndiName;
   }
   
   /**
    * {@inheritDoc}
    */
   public Object getAdminObject()
   {
      return adminObject;
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
   public org.ironjacamar.common.api.metadata.resourceadapter.AdminObject getActivation()
   {
      return activation;
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
   public boolean isActivated()
   {
      return activated;
   }

   /**
    * {@inheritDoc}
    */
   public boolean activate() throws Exception
   {
      if (!activated)
      {
         if (adminObject instanceof ResourceAdapterAssociation)
         {
            if (!(adminObject instanceof Serializable &&
                  adminObject instanceof javax.resource.Referenceable))
            {
               throw new Exception("TODO");
            }
         }
         else
         {
            if (!(adminObject instanceof javax.naming.Referenceable))
            {
               DelegatorInvocationHandler dih = new DelegatorInvocationHandler(adminObject);

               List<Class<?>> interfaces = new ArrayList<Class<?>>();
               Class<?> clz = adminObject.getClass();
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

               jndiObject = Proxy.newProxyInstance(SecurityActions.getClassLoader(adminObject.getClass()),
                                                   interfaces.toArray(new Class<?>[interfaces.size()]),
                                                   dih);
            }
         }

         if (jndiObject != null)
         {
            jndiStrategy.bind(jndiName, jndiObject);
         }
         else
         {
            jndiStrategy.bind(jndiName, adminObject);
         }

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
         if (jndiObject != null)
         {
            jndiStrategy.unbind(jndiName, jndiObject);
         }
         else
         {
            jndiStrategy.unbind(jndiName, adminObject);
         }

         activated = false;
         return true;
      }

      return false;
   }
}
