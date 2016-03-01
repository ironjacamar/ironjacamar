/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

import java.util.Map;
import java.util.Set;

/**
 * An activation spec implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ActivationSpecImpl
{
   /** The class name */
   private String className;

   /** The config properties */
   private Map<String, Class<?>> configProperties;
   
   /** The required config properties */
   private Set<String> requiredConfigProperties;
   
   /**
    * Constructor
    * @param className The class name
    * @param configProperties The config properties
    * @param requiredConfigProperties The required config properties
    */
   public ActivationSpecImpl(String className,
                             Map<String, Class<?>> configProperties,
                             Set<String> requiredConfigProperties)
   {
      this.className = className;
      this.configProperties = configProperties;
      this.requiredConfigProperties = requiredConfigProperties;
   }
   
   /**
    * Get the class name
    * @return The value
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Get the config properties
    * @return The value
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
}
