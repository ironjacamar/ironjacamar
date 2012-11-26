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

package org.jboss.jca.sjc.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * An abstract home task
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractHomeTask extends Task
{
   /** Home directory */
   private String home;
   
   /**
    * Constructor
    */
   public AbstractHomeTask()
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
      this.home = getProject().replaceProperties(v);
   }

   /**
    * Execute
    * @exception BuildException If the build fails
    */
   public abstract void execute() throws BuildException;
}
