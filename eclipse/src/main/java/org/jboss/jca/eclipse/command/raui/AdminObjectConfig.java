/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.eclipse.command.raui;

import org.jboss.jca.codegenerator.ConfigPropType;

import java.util.List;

/**
 * AdminObjectConifg
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class AdminObjectConfig
{

   private String clssName;
   
   private String jndiName = null;
   
   private String poolName = null;
   
   private Boolean enabled = null;
   
   private Boolean useJavaCtx = null;
   
   private List<ConfigPropType> configProps;

   /**
    * The constructor
    */
   public AdminObjectConfig()
   {
      super();
   }
   
   /**
    * Get clssName
    * @return The clssName
    */
   public String getClssName()
   {
      return clssName;
   }

   /**
    * Set clssName
    * @param clssName The value to set
    */
   public void setClssName(String clssName)
   {
      this.clssName = clssName;
   }

   /**
    * Get jndiName
    * @return The jndiName
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Set jndiName
    * @param jndiName The value to set
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   /**
    * Get poolName
    * @return The poolName
    */
   public String getPoolName()
   {
      return poolName;
   }

   /**
    * Set poolName
    * @param poolName The value to set
    */
   public void setPoolName(String poolName)
   {
      this.poolName = poolName;
   }

   /**
    * Get enabled
    * @return The enabled
    */
   public Boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Set enabled
    * @param enabled The value to set
    */
   public void setEnabled(Boolean enabled)
   {
      this.enabled = enabled;
   }

   /**
    * Get useJavaCtx
    * @return The useJavaCtx
    */
   public Boolean isUseJavaCtx()
   {
      return useJavaCtx;
   }

   /**
    * Set useJavaCtx
    * @param useJavaCtx The value to set
    */
   public void setUseJavaCtx(Boolean useJavaCtx)
   {
      this.useJavaCtx = useJavaCtx;
   }

   /**
    * Get configProps
    * @return The configProps
    */
   public List<ConfigPropType> getConfigProps()
   {
      return configProps;
   }

   /**
    * Set configProps
    * @param configProps The value to set
    */
   public void setConfigProps(List<ConfigPropType> configProps)
   {
      this.configProps = configProps;
   }
   
}
