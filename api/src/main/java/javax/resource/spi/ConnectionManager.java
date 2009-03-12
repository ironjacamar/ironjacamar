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

package javax.resource.spi;


import javax.resource.ResourceException;

/** <p>ConnectionManager interface provides a hook for the resource adapter to
 *  pass a connection request to the application server. 
 *  
 *  <p>An application server provides implementation of the ConnectionManager
 *  interface. This implementation is not specific to any particular type of
 *  the resource adapter or connection factory interface.
 * 
 *  <p>The ConnectionManager implementation delegates to the application 
 *  server to enable latter to provide quality of services (QoS) - security,
 *  connection pool management, transaction management and error 
 *  logging/tracing. 
 * 
 *  <p>An application server implements these services in a generic manner, 
 *  independent of any resource adapter and EIS specific mechanisms. The 
 *  connector architecture does not specify how an application server 
 *  implements these services; the implementation is specific to an 
 *  application server.
 *  
 *  <p>After an application server hooks-in its services, the connection 
 *  request gets delegated to a ManagedConnectionFactory instance either 
 *  for the creation of a new physical connection or for the matching of 
 *  an already existing physical connection.
 *  
 *  <p>An implementation class for ConnectionManager interface is
 *  required to implement the <code>java.io.Serializable</code> interface.
 *  
 *  <p>In the non-managed application scenario, the ConnectionManager 
 *  implementation class can be provided either by a resource adapter (as
 *  a default ConnectionManager implementation) or by application 
 *  developers. In both cases, QOS can be provided as components by third
 *  party vendors.</p>
 *
 *  @since       0.6
 *  @author      Rahul Sharma
 *  @see         javax.resource.spi.ManagedConnectionFactory
**/

public interface ConnectionManager extends java.io.Serializable 
{
   
   /** <p>The method allocateConnection gets called by the resource adapter's
    *  connection factory instance. This lets connection factory instance 
    *  (provided by the resource adapter) pass a connection request to 
    *  the ConnectionManager instance.</p>
    *  
    *  <p>The connectionRequestInfo parameter represents information specific
    *  to the resource adapter for handling of the connection request.</p>
    *
    *  @param   mcf
    *                       used by application server to delegate
    *                       connection matching/creation
    *  @param   cxRequestInfo     
    *                       connection request Information
    *
    *  @return  connection handle with an EIS specific connection interface.
    * 
    *
    *  @throws  ResourceException     Generic exception
    *  @throws  ApplicationServerInternalException 
    *                                 Application server specific exception
    *  @throws  SecurityException     Security related error
    *  @throws  ResourceAllocationException
    *                                 Failed to allocate system resources for
    *                                 connection request
    *  @throws  ResourceAdapterInternalException
    *                                 Resource adapter related error condition
    **/
   public Object allocateConnection(ManagedConnectionFactory mcf,
                                    ConnectionRequestInfo cxRequestInfo)
      throws ResourceException;
}
