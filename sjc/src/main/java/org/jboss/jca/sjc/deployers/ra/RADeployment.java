/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.sjc.deployers.ra;

import org.jboss.jca.sjc.deployers.Deployment;

/**
 * A resource adapter deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RADeployment implements Deployment
{
   /** The name */
   private String name;

   /** The classloader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param name The name of the deployment
    * @param cl The classloader for the deployment
    */
   public RADeployment(String name, ClassLoader cl)
   {
      this.name = name;
      this.cl = cl;
   }

   /**
    * Get the name
    * @return The name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the classloader
    * @return The classloader
    */
   public ClassLoader getClassLoader()
   {
      return cl;
   }
}
