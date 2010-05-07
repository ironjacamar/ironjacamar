/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.codegenerator;

import java.util.List;

/**
 * A Definition.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class Definition
{
   /** output directory  */
   private String outputDir;
   /** resource adapter package name */
   private String raPackage;
   /** resource adapter class name */
   private String raClass;
   /** resource adapter configuration properties */
   private List<ConfigPropType> raConfigProps;
   
   /** managed connection factory class name */
   private String mcfClass;
   /** resource adapter configuration properties */
   private List<ConfigPropType> mcfConfigProps;
   
   /** managed connection class name */
   private String mcClass;
   /** connection interface name */
   private String connInterfaceClass;
   /** connection impl class name */
   private String connImplClass;
   
   /**
    * Set the outputDir.
    * 
    * @param outputDir The outputDir to set.
    */
   public void setOutputDir(String outputDir)
   {
      this.outputDir = outputDir;
   }

   /**
    * Get the outputDir.
    * 
    * @return the outputDir.
    */
   public String getOutputDir()
   {
      return outputDir;
   }

   /**
    * Set the raPackage.
    * 
    * @param raPackage The raPackage to set.
    */
   public void setRaPackage(String raPackage)
   {
      this.raPackage = raPackage;
   }

   /**
    * Get the raPackage.
    * 
    * @return the raPackage.
    */
   public String getRaPackage()
   {
      return raPackage;
   }

   /**
    * Set the raClass.
    * 
    * @param raClass The raClass to set.
    */
   public void setRaClass(String raClass)
   {
      this.raClass = raClass;
   }

   /**
    * Get the raClass.
    * 
    * @return the raClass.
    */
   public String getRaClass()
   {
      return raClass;
   }

   /**
    * Set the raConfigProps.
    * 
    * @param raConfigProps The raConfigProps to set.
    */
   public void setRaConfigProps(List<ConfigPropType> raConfigProps)
   {
      this.raConfigProps = raConfigProps;
   }

   /**
    * Get the raConfigProps.
    * 
    * @return the raConfigProps.
    */
   public List<ConfigPropType> getRaConfigProps()
   {
      return raConfigProps;
   }

   /**
    * Set the mcfClass.
    * 
    * @param mcfClass The mcfClass to set.
    */
   public void setMcfClass(String mcfClass)
   {
      this.mcfClass = mcfClass;
   }

   /**
    * Get the mcfClass.
    * 
    * @return the mcfClass.
    */
   public String getMcfClass()
   {
      return mcfClass;
   }

   /**
    * Set the mcClass.
    * 
    * @param mcClass The mcClass to set.
    */
   public void setMcClass(String mcClass)
   {
      this.mcClass = mcClass;
   }

   /**
    * Get the mcClass.
    * 
    * @return the mcClass.
    */
   public String getMcClass()
   {
      return mcClass;
   }

   /**
    * Set the connInterfaceClass.
    * 
    * @param connInterfaceClass The connInterfaceClass to set.
    */
   public void setConnInterfaceClass(String connInterfaceClass)
   {
      this.connInterfaceClass = connInterfaceClass;
   }

   /**
    * Get the connInterfaceClass.
    * 
    * @return the connInterfaceClass.
    */
   public String getConnInterfaceClass()
   {
      return connInterfaceClass;
   }

   /**
    * Set the connImplClass.
    * 
    * @param connImplClass The connImplClass to set.
    */
   public void setConnImplClass(String connImplClass)
   {
      this.connImplClass = connImplClass;
   }

   /**
    * Get the connImplClass.
    * 
    * @return the connImplClass.
    */
   public String getConnImplClass()
   {
      return connImplClass;
   }

   /**
    * Set the mcfConfigProps.
    * 
    * @param mcfConfigProps The mcfConfigProps to set.
    */
   public void setMcfConfigProps(List<ConfigPropType> mcfConfigProps)
   {
      this.mcfConfigProps = mcfConfigProps;
   }

   /**
    * Get the mcfConfigProps.
    * 
    * @return the mcfConfigProps.
    */
   public List<ConfigPropType> getMcfConfigProps()
   {
      return mcfConfigProps;
   }

}
