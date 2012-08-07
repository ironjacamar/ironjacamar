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
package org.jboss.jca.test.eis.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Abstract task
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractTask extends Task
{
   /** The host */
   private String host;
   
   /** The port */
   private int port;
   
   /**
    * Constructor
    */
   public AbstractTask()
   {
      host = "localhost";
      port = 1400;
   }
   
   /**
    * Get the host
    * @return The value
    */
   public String getHost()
   {
      return host;
   }

   /**
    * Set the host
    * @param v The value
    */
   public void setHost(String v)
   {
      this.host = v;
   }

   /**
    * Get the port
    * @return The value
    */
   public int getPort()
   {
      return port;
   }

   /**
    * Set the port
    * @param v The value
    */
   public void setPort(int v)
   {
      this.port = v;
   }

   /**
    * {@inheritDoc}
    */
   public abstract void execute() throws BuildException;
}
