/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.api.deploymentrepository;

import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.core.spi.classloading.ClassLoaderPlugin;

import java.io.File;
import java.util.Collection;

/**
 * The deployment
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Deployment
{
   /**
    * Get the identifier
    * @return The value
    */
   public String getIdentifier();

   /**
    * Get the file name or module name
    * @return The value
    */
   public String getName();

   /**
    * Get either a file, a directory (module) or null (in-mem)
    * @return The value
    */
   public File getArchive();

   /**
    * Get the classloader
    * @return The value
    */
   public ClassLoader getClassLoader();

   /**
    * Get the specification metadata
    * @return The value
    */
   public Connector getMetadata();

   /**
    * Get the activation
    * @return The value
    */
   public Activation getActivation();

   /**
    * Get the resource adapter
    * @return The value
    */
   public ResourceAdapter getResourceAdapter();

   /**
    * Get the connection factories
    * @return The value
    */
   public Collection<ConnectionFactory> getConnectionFactories();

   /**
    * Get the admin objects
    * @return The value
    */
   public Collection<AdminObject> getAdminObjects();

   /**
    * Get the class loader plugin
    *
    * @return The value
    */
   public ClassLoaderPlugin getClassLoaderPlugin();

   /**
    * Is the deployment active ?
    * @return <code>true</code> if activated, <code>false</code> if not
    */
   public boolean isActivated();

   /**
    * Activate the deployment
    * @return <code>true</code> if activated, <code>false</code> if already activated
    * @exception Exception Thrown in case of an error
    */
   public boolean activate() throws Exception;

   /**
    * Deactivate the deployment
    * @return <code>true</code> if deactivated, <code>false</code> if already deactivated
    * @exception Exception Thrown in case of an error
    */
   public boolean deactivate() throws Exception;
}
