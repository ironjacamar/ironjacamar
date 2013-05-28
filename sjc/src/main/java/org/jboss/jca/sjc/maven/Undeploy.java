/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.sjc.maven;

import java.io.File;
import java.io.Serializable;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * An undeploy task
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Undeploy extends AbstractHostPortMojo
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
    * {@inheritDoc}
    */
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      if (file == null)
         throw new MojoFailureException("File not defined");

      if (!file.exists())
         throw new MojoFailureException("File doesn't exists: " + file);

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
            getLog().info("Undeployed: " + file.getName());
         }
         else
         {
            getLog().info(file.getName() + " wasn't undeployed");
         }
      }
      catch (Throwable t)
      {
         throw new MojoFailureException("Unable to undeploy to " + getHost() + ":" + getPort() +
                                        " (" + t.getMessage() + ")", t);
      }
   }
}
