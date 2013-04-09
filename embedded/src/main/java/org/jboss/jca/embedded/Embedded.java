/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.embedded;

import java.net.URL;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * The embedded IronJacamar container
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface Embedded
{
   /**
    * Startup
    * @exception Throwable If an error occurs
    */
   public void startup() throws Throwable;

   /**
    * Shutdown
    * @exception Throwable If an error occurs
    */
   public void shutdown() throws Throwable;

   /**
    * Deploy
    * @param url The resource url
    * @exception Throwable If an error occurs
    */
   public void deploy(URL url) throws Throwable;

   /**
    * Deploy
    * @param descriptor The descriptor
    * @exception Throwable If an error occurs
    */
   public void deploy(Descriptor descriptor) throws Throwable;

   /**
    * Deploy
    * @param raa The resource adapter archive
    * @exception Throwable If an error occurs
    */
   public void deploy(ResourceAdapterArchive raa) throws Throwable;

   /**
    * Undeploy
    * @param url The resource url
    * @exception Throwable If an error occurs
    */
   public void undeploy(URL url) throws Throwable;

   /**
    * Undeploy
    * @param descriptor The descriptor
    * @exception Throwable If an error occurs
    */
   public void undeploy(Descriptor descriptor) throws Throwable;

   /**
    * Undeploy
    * @param raa The resource adapter archive
    * @exception Throwable If an error occurs
    */
   public void undeploy(ResourceAdapterArchive raa) throws Throwable;

   /**
    * Lookup a bean
    * @param <T> the generics type
    * @param name The bean name
    * @param expectedType The expected type for the bean
    * @return The bean instance
    * @exception Throwable If an error occurs
    */
   public <T> T lookup(String name, Class<T> expectedType) throws Throwable;
}
