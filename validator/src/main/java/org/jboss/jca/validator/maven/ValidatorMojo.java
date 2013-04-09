/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2010, Red Hat Inc, and individual contributors
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
package org.jboss.jca.validator.maven;

import org.jboss.jca.validator.Validation;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A ValidatorMojo.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class ValidatorMojo extends AbstractMojo
{
   /** output directory */
   private String outputDir;
   
   /** file need to be validated */
   private File rarFile;
   
   /** classpath */
   private String[] classpath;

   
   /**
    * Constructor
    */
   public ValidatorMojo()
   {
      outputDir = ".";
   }
   
   /**
    * Execute
    * 
    * @exception MojoExecutionException Thrown if the plugin cant be executed
    * @exception MojoFailureException Thrown if there is an error
    */
   @Override
   public void execute()  throws MojoExecutionException, MojoFailureException
   {
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try 
      {
         Thread.currentThread().setContextClassLoader(ValidatorMojo.class.getClassLoader());

         Validation.validate(getRarFile().toURI().toURL(), 
            getOutputDir(), getClasspath());
      }
      catch (Throwable t) 
      {
         t.printStackTrace();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }
   
   /**
    * Get the output directory.
    * 
    * @return the outputDir.
    */
   public String getOutputDir()
   {
      return outputDir;
   }

   /**
    * Set the output directory.
    * 
    * @param outputDir The outputDir to set.
    */
   public void setOutputDir(String outputDir)
   {
      this.outputDir = outputDir;
   }

   /**
    * Get the rarFile.
    * 
    * @return the rarFile.
    */
   public File getRarFile()
   {
      return rarFile;
   }

   /**
    * Set the rarFile.
    * 
    * @param rarFile The rarFile to set.
    */
   public void setRarFile(File rarFile)
   {
      this.rarFile = rarFile;
   }

   /**
    * Set the classpath.
    * 
    * @param classpath The classpath to set.
    */
   public void setClasspath(String[] classpath)
   {
      this.classpath = classpath;
   }

   /**
    * Get the classpath.
    * 
    * @return the classpath.
    */
   public String[] getClasspath()
   {
      return classpath;
   }
}
