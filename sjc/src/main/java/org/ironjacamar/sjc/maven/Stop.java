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

package org.ironjacamar.sjc.maven;

import org.ironjacamar.sjc.ProcessController;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Stop an IronJacamar/Standalone instance
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Stop extends AbstractHomeMojo
{
   /**
    * Constructor
    */
   public Stop()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      if (getHome() == null)
         throw new MojoFailureException("Home isn't set");

      try 
      {
         ProcessController pc = ProcessController.getInstance();
         int exitCode = pc.stop(getHome());

         if (exitCode != 0)
            throw new MojoFailureException("IronJacamar instance exit code: " + exitCode);

         getLog().info("Stopped IronJacamar instance from: " + getHome());
      }
      catch (Throwable t) 
      {
         throw new MojoFailureException(t.getMessage(), t);
      }
   }
}
