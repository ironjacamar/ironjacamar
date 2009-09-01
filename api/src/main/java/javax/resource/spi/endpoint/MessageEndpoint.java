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

package javax.resource.spi.endpoint;

import javax.resource.ResourceException;

/**
 * This defines a contract for a message endpoint. This is implemented by an
 * application server.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface MessageEndpoint 
{
   /**
    * This is called by a resource adapter before a message is delivered.
    *
    * @param method description of a target method. This information about
    * the intended target method allows an application server to decide 
    * whether to start a transaction during this method call, depending 
    * on the transaction preferences of the target method.
    * The processing (by the application server) of the actual message 
    * delivery method call on the endpoint must be independent of the 
    * class loader associated with this descriptive method object. 
    *
    * @throws NoSuchMethodException indicates that the specified method
    * does not exist on the target endpoint.
    *
    * @throws ResourceException generic exception.
    *
    * @throws javax.resource.spi.ApplicationServerInternalException indicates an error 
    * condition in the application server.
    *
    * @throws javax.resource.spi.IllegalStateException indicates that the endpoint is in an
    * illegal state for the method invocation. For example, this occurs when
    * <code>beforeDelivery</code> and <code>afterDelivery</code> 
    * method calls are not paired.
    *
    * @throws javax.resource.spi.UnavailableException indicates that the endpoint is not 
    * available.
    */
   void beforeDelivery(java.lang.reflect.Method method)
      throws NoSuchMethodException, ResourceException;

   /**
    * This is called by a resource adapter after a message is delivered.
    *
    * @throws ResourceException generic exception.
    *
    * @throws javax.resource.spi.ApplicationServerInternalException indicates an error 
    * condition in the application server.
    *
    * @throws javax.resource.spi.IllegalStateException indicates that the endpoint is in an
    * illegal state for the method invocation. For example, this occurs when
    * beforeDelivery and afterDelivery method calls are not paired.
    *
    * @throws javax.resource.spi.UnavailableException indicates that the endpoint is not 
    * available.
    */
   void afterDelivery() throws ResourceException;
   
   /**
    * This method may be called by the resource adapter to indicate that it
    * no longer needs a proxy endpoint instance. This hint may be used by
    * the application server for endpoint pooling decisions.
    */
   void release();
}
