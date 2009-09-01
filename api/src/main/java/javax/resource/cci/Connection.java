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

package javax.resource.cci;

import javax.resource.ResourceException;


/** A Connection represents an application-level handle that is used 
 *  by a client to access the underlying physical connection. The actual 
 *  physical connection associated with a Connection instance is 
 *  represented by a ManagedConnection instance.
 *
 *  <p>A client gets a Connection instance by using the 
 *  <code>getConnection</code> method on a <code>ConnectionFactory</code> 
 *  instance. A connection can be associated with zero or more Interaction
 *  instances.
 * 
 *  @author  Rahul Sharma
 *  @version 0.8
 *  @see     javax.resource.cci.ConnectionFactory
 *  @see     javax.resource.cci.Interaction
 **/

public interface Connection 
{
  
   /** Creates an Interaction associated with this Connection. An
    *  Interaction enables an application to execute EIS functions. 
    *
    *  @return  Interaction instance  
    *  @throws  ResourceException     Failed to create an Interaction
    **/
   public Interaction createInteraction() throws ResourceException;

   /** Returns an LocalTransaction instance that enables a component to
    *  demarcate resource manager local transactions on the Connection.
    *  If a resource adapter does not allow a component to demarcate 
    *  local transactions on an Connection using LocalTransaction 
    *  interface, then the method getLocalTransaction should throw a 
    *  NotSupportedException.
    *
    *  @return   LocalTransaction instance
    *           
    *  @throws   ResourceException   Failed to return a LocalTransaction
    *                                instance because of a resource
    *                                adapter error
    *  @throws   javax.resource.NotSupportedException Demarcation of Resource manager 
    *                                local transactions is not supported
    *                                on this Connection
    *  @see javax.resource.cci.LocalTransaction
    **/
   public LocalTransaction getLocalTransaction() throws ResourceException;
  
   /** Gets the information on the underlying EIS instance represented
    *  through an active connection.
    *
    *  @return   ConnectionMetaData instance representing information 
    *            about the EIS instance
    *  @throws   ResourceException  
    *                        Failed to get information about the 
    *                        connected EIS instance. Error can be
    *                        resource adapter-internal, EIS-specific
    *                        or communication related.
    **/
   public ConnectionMetaData getMetaData() throws ResourceException;

   /** Gets the information on the ResultSet functionality supported by
    *  a connected EIS instance.
    *
    *  @return   ResultSetInfo instance
    *  @throws   ResourceException     Failed to get ResultSet related 
    *                                  information
    *  @throws   javax.resource.NotSupportedException ResultSet functionality is not
    *                                  supported
    **/
   public ResultSetInfo getResultSetInfo() throws ResourceException;
  
   /** Initiates close of the connection handle at the application level.
    *  A client should not use a closed connection to interact with 
    *  an EIS.
    *  
    *  @throws  ResourceException  Exception thrown if close
    *                              on a connection handle fails.
    *           <p>Any invalid connection close invocation--example,
    *              calling close on a connection handle that is 
    *              already closed--should also throw this exception.
    *  
    **/
   public void close() throws ResourceException;
}
