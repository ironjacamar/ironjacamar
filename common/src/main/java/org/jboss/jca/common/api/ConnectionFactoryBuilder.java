/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.common.api;

import javax.naming.Reference;
import javax.resource.spi.ManagedConnectionFactory;

import com.github.fungal.deployers.DeployException;

/**
 * Used for building the ConnectionFactory.  This is starting as a "simple as possible"
 * interface based on current needs to build the ConnectionFactory (no remote case yet supported).
 *
 * @author <a href="mailto:smarlow@redhat.com">Scott Marlow</a>
 */
public interface ConnectionFactoryBuilder
{

   /**
    * Returns the Reference which has the ConnectionFactory.
    * @return Reference which represents the ConnectionFactory
    * @throws DeployException if the ConnectionFactory has already been previously built
    */
   Reference build() throws DeployException;

   /**
    * specify the unique name of the connection factory   
    * @param name of connection factory which should be unique
    * @return this for convenience
    */
   ConnectionFactoryBuilder setName(String name);

   /**
    * Specify the managed connection factory.  Some implementation may ignore this call
    * (e.g. because they will instead create the ManagedConnectionFactory on the fly).
    * @param mcf is the managed connection factory to be used by connection factory.
    * @return this for convenience
    */
   ConnectionFactoryBuilder setManagedConnectionFactory(ManagedConnectionFactory mcf);

   /**
    * @return the managed connection factory
    */
   Object getManagedConnectionFactory();

   /**
    * Specify the ConnectionFactory.  Some implementations may ignore this call
    * (e.g. because they will instead create the ManagedConnectionFactory on the fly).
    * @param cf is the connection factory that will be returned
    * @return this for convenience
    */
   ConnectionFactoryBuilder setConnectionFactory(Object cf);

   /**
    * @return the connection factory
    */
   Object getConnectionFactory();


}
