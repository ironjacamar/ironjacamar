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
package org.ironjacamar.validator.ant;

import org.ironjacamar.validator.Validation;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

/**
 * A ValidatorTask.
 * 
 * @author Jeff Zhang
 */
public class ValidatorTask extends Task
{
   
   /** output directory */
   private String outputDir;
   
   /** file need to be validated */
   private String rarFile;
   
   /** CommandlineJava */
   private CommandlineJava cmdl = new CommandlineJava();
   
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
    * Set the classpath to be used when running the Java class.
    *
    * @param s an Ant Path object containing the classpath.
    */
   public void setClasspath(Path s)
   {
      createClasspath().append(s);
   }

   /**
    * Add a path to the classpath.
    *
    * @return created classpath.
    */
   public Path createClasspath()
   {
      return getCommandLine().createClasspath(getProject()).createPath();
   }

   /**
    * Accessor to the command line.
    *
    * @return the current command line.
    */
   public CommandlineJava getCommandLine()
   {
      return cmdl;
   }

   /**
    * Execute
    * @exception BuildException If the build fails
    */
   @Override
   public void execute() throws BuildException 
   {
      ClassLoader oldCL = SecurityActions.getThreadContextClassLoader();
      try 
      {
         SecurityActions.setThreadContextClassLoader(SecurityActions.getClassLoader(ValidatorTask.class));

         Validation.validate(new File(getRarFile()).toURI().toURL(), 
            getOutputDir(), getCommandLine().getClasspath().list());
      }
      catch (Throwable t) 
      {
         throw new BuildException(t.getMessage(), t);
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(oldCL);
      }
   }

}
