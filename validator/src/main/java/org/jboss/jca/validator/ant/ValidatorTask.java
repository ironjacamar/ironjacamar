/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.validator.ant;

import org.jboss.jca.validator.Main;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * A ValidatorTask.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class ValidatorTask extends Task
{
   
   /** output directory */
   private String outputDir;
   
   /** file need to be validated */
   private String rarFile;
   
   /**
    * Constructor
    */
   public ValidatorTask()
   {
      outputDir = ".";
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
   public String getRarFile()
   {
      return rarFile;
   }

   /**
    * Set the rarFile.
    * 
    * @param rarFile The rarFile to set.
    */
   public void setRarFile(String rarFile)
   {
      this.rarFile = rarFile;
   }

   /**
    * Execute
    * @exception BuildException If the build fails
    */
   @Override
   public void execute() throws BuildException 
   {
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try 
      {
         Thread.currentThread().setContextClassLoader(ValidatorTask.class.getClassLoader());
         
         Main main = new Main();

         //main.setOutput(getOutput());

         main.validate(new File(getRarFile()).toURI().toURL(), getOutputDir());
      }
      catch (Throwable t) 
      {
         throw new BuildException(t.getMessage(), t);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

}
