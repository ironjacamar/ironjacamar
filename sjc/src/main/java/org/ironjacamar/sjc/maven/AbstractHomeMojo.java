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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * An abstract home mojo
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractHomeMojo extends AbstractMojo
{
   /** Home directory */
   private String home;

   /** Constructor */
   public AbstractHomeMojo()
   {
      home = null;
   }

   /**
    * Get the home directory
    * @return The value
    */
   public String getHome()
   {
      return home;
   }

   /**
    * Set the home directory
    * @param v The value
    */
   public void setHome(String v)
   {
      this.home = v;
   }

   /**
    * Execute
    *
    * @throws MojoExecutionException Thrown if the plugin cant be executed
    * @throws MojoFailureException   Thrown if there is an error
    */
   public abstract void execute() throws MojoExecutionException, MojoFailureException;
}
