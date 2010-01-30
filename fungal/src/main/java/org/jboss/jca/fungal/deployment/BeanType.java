/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.deployment;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bean
 */
public class BeanType
{
   private ConstructorType constructor;
   private List<PropertyType> property;
   private List<DependsType> depends;
   private List<InstallType> install;
   private List<UninstallType> uninstall;
   private List<IncallbackType> incallback;
   private List<UncallbackType> uncallback;
   private IgnoreCreateType ignoreCreate;
   private IgnoreStartType ignoreStart;
   private IgnoreStopType ignoreStop;
   private IgnoreDestroyType ignoreDestroy;
   private String name;
   private String interfaze;
   private String clazz;

   /**
    * Constructor
    */
   public BeanType()
   {
      constructor = null;
      property = null;
      depends = null;
      install = null;
      uninstall = null;
      incallback = null;
      uncallback = null;
      ignoreCreate = null;
      ignoreStart = null;
      ignoreStop = null;
      ignoreDestroy = null;
      name = null;
      interfaze = null;
      clazz = null;
   }

   /**
    * Get the constructor
    * @return The value
    */
   public ConstructorType getConstructor()
   {
      return constructor;
   }

   /**
    * Set the constructor
    * @param value The value
    */
   public void setConstructor(ConstructorType value)
   {
      constructor = value;
   }

   /**
    * Get the property values
    * @return The value
    */
   public List<PropertyType> getProperty()
   {
      if (property == null)
         property = new ArrayList<PropertyType>(1);

      return property;
   }

   /**
    * Get the depends values
    * @return The value
    */
   public List<DependsType> getDepends()
   {
      if (depends == null)
         depends = new ArrayList<DependsType>(1);

      return depends;
   }

   /**
    * Get the install values
    * @return The value
    */
   public List<InstallType> getInstall()
   {
      if (install == null)
         install = new ArrayList<InstallType>(1);

      return install;
   }

   /**
    * Get the uninstall values
    * @return The value
    */
   public List<UninstallType> getUninstall()
   {
      if (uninstall == null)
         uninstall = new ArrayList<UninstallType>(1);

      return uninstall;
   }

   /**
    * Get the incallback values
    * @return The value
    */
   public List<IncallbackType> getIncallback()
   {
      if (incallback == null)
         incallback = new ArrayList<IncallbackType>(1);

      return incallback;
   }

   /**
    * Get the uncallback values
    * @return The value
    */
   public List<UncallbackType> getUncallback()
   {
      if (uncallback == null)
         uncallback = new ArrayList<UncallbackType>(1);

      return uncallback;
   }

   /**
    * Get the ignore create value
    * @return The value
    */
   public IgnoreCreateType getIgnoreCreate()
   {
      return ignoreCreate;
   }

   /**
    * Set the ignore create value
    * @param value The value
    */
   public void setIgnoreCreate(IgnoreCreateType value)
   {
      ignoreCreate = value;
   }

   /**
    * Get the ignore start value
    * @return The value
    */
   public IgnoreStartType getIgnoreStart()
   {
      return ignoreStart;
   }

   /**
    * Set the ignore start value
    * @param value The value
    */
   public void setIgnoreStart(IgnoreStartType value)
   {
      ignoreStart = value;
   }

   /**
    * Get the ignore stop value
    * @return The value
    */
   public IgnoreStopType getIgnoreStop()
   {
      return ignoreStop;
   }

   /**
    * Set the ignore stop value
    * @param value The value
    */
   public void setIgnoreStop(IgnoreStopType value)
   {
      ignoreStop = value;
   }

   /**
    * Get the ignore destroy value
    * @return The value
    */
   public IgnoreDestroyType getIgnoreDestroy()
   {
      return ignoreDestroy;
   }

   /**
    * Set the ignore destroy value
    * @param value The value
    */
   public void setIgnoreDestroy(IgnoreDestroyType value)
   {
      ignoreDestroy = value;
   }

   /**
    * Get the name
    * @return The value
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set the name
    * @param value The value
    */
   public void setName(String value)
   {
      name = value;
   }

   /**
    * Get the interface
    * @return The value
    */
   public String getInterface()
   {
      return interfaze;
   }

   /**
    * Set the interface
    * @param value The value
    */
   public void setInterface(String value)
   {
      interfaze = value;
   }

   /**
    * Get the class
    * @return The value
    */
   public String getClazz()
   {
      return clazz;
   }

   /**
    * Set the class
    * @param value The value
    */
   public void setClazz(String value)
   {
      clazz = value;
   }
}
