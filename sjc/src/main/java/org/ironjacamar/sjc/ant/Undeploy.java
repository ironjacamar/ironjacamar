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

package org.ironjacamar.sjc.ant;

import java.io.File;
import java.io.Serializable;

import org.apache.tools.ant.BuildException;

/**
 * An undeploy task
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Undeploy extends AbstractHostPortTask
{
   /** The file */
   private File file;

   /**
    * Constructor
    */
   public Undeploy()
   {
      this.file = null;
   }

   /**
    * Set the file
    * @param v The value
    */
   public void setFile(File v)
   {
      file = v;
   }

   /**
    * Execute
    * @exception BuildException If the build fails
    */
   public void execute() throws BuildException
   {
      if (file == null)
         throw new BuildException("File not defined");

      if (!file.exists())
         throw new BuildException("File doesn't exists: " + file);

      try
      {
         Boolean result = null;
         if (isLocal())
         {
            Object value = executeCommand("local-undeploy", new Serializable[] {file.toURI().toURL()});

            if (value instanceof Boolean)
            {
               result = (Boolean)value;
            }
            else
            {
               throw (Throwable)value;
            }
         }
         else
         {
            Object value = executeCommand("remote-undeploy", new Serializable[] {file.getName()});

            if (value instanceof Boolean)
            {
               result = (Boolean)value;
            }
            else
            {
               throw (Throwable)value;
            }
         }

         if (result.booleanValue())
         {
            log("Undeployed: " + file.getName());
         }
         else
         {
            log(file.getName() + " wasn't undeployed");
         }
      }
      catch (Throwable t)
      {
         throw new BuildException("Unable to undeploy to " + getHost() + ":" + getPort() + " (" + t.getMessage() + ")",
                                  t);
      }
   }
}
