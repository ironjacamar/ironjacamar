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

import java.util.Set;

import javax.resource.ResourceException;
import javax.security.auth.Subject;

/** 
 * ManagedConnectionFactory instance is a factory of both ManagedConnection
 *  and EIS-specific connection factory instances. This interface supports 
 *  connection pooling by providing methods for matching and creation of
 *  ManagedConnection instance. A ManagedConnectionFactory 
 *  instance is required to be a JavaBean.
 *
 *  @version     0.6
 *  @author      Rahul Sharma
 *
 *  @see         javax.resource.spi.ManagedConnection
 */

public interface ManagedConnectionFactory extends java.io.Serializable 
{
   
   /**
    * Creates a Connection Factory instance. The Connection Factory
    *  instance gets initialized with the passed ConnectionManager. In
    *  the managed scenario, ConnectionManager is provided by the 
    *  application server.
    *
    *  @param    cxManager    ConnectionManager to be associated with
    *                         created EIS connection factory instance
    *  @return   EIS-specific Connection Factory instance or
    *            javax.resource.cci.ConnectionFactory instance
    *   
    *  @throws   ResourceException     Generic exception
    *  @throws   ResourceAdapterInternalException
    *                Resource adapter related error condition
    */
   public Object createConnectionFactory(ConnectionManager cxManager)
      throws ResourceException;
   
   /**
    * Creates a Connection Factory instance. The Connection Factory 
    *  instance gets initialized with a default ConnectionManager provided
    *  by the resource adapter.
    *
    *  @return   EIS-specific Connection Factory instance or
    *            javax.resource.cci.ConnectionFactory instance
    *
    *  @throws   ResourceException     Generic exception
    *  @throws   ResourceAdapterInternalException
    *                Resource adapter related error condition
    */
   public Object createConnectionFactory() throws ResourceException;
   
   /** 
    * Creates a new physical connection to the underlying EIS 
    *  resource manager.
    *
    *  <p>ManagedConnectionFactory uses the security information (passed as
    *  Subject) and additional ConnectionRequestInfo (which is specific to
    *  ResourceAdapter and opaque to application server) to create this new
    *  connection.
    *
    *  @param   subject        Caller's security information
    *  @param   cxRequestInfo  Additional resource adapter specific connection
    *                          request information
    *
    *  @throws  ResourceException     generic exception
    *  @throws  SecurityException     security related error
    *  @throws  ResourceAllocationException
    *                                 failed to allocate system resources for
    *                                 connection request
    *  @throws  ResourceAdapterInternalException
    *                                 resource adapter related error condition
    *  @throws  EISSystemException    internal error condition in EIS instance
    *
    *  @return  ManagedConnection instance
    */
   public ManagedConnection createManagedConnection(Subject subject, 
                                                    ConnectionRequestInfo cxRequestInfo) 
      throws ResourceException;
   
   /** 
    * Returns a matched connection from the candidate set of connections. 
    *  
    *  
    *  <p>ManagedConnectionFactory uses the security info (as in Subject)
    *  and information provided through ConnectionRequestInfo and additional
    *  Resource Adapter specific criteria to do matching. Note that criteria
    *  used for matching is specific to a resource adapter and is not
    *  prescribed by the Connector specification.</p>
    *
    *  <p>This method returns a ManagedConnection instance that is the best 
    *  match for handling the connection allocation request.</p>
    *
    *  @param   connectionSet   candidate connection set
    *  @param   subject         caller's security information
    *  @param   cxRequestInfo   additional resource adapter specific 
    *                           connection request information  
    *
    *  @throws  ResourceException     generic exception
    *  @throws  SecurityException     security related error
    *  @throws  ResourceAdapterInternalException
    *                                 resource adapter related error condition
    *  @throws  javax.resource.NotSupportedException if operation is not supported
    *
    *  @return  ManagedConnection     if resource adapter finds an
    *                                 acceptable match otherwise null
    **/
   public ManagedConnection matchManagedConnections(Set connectionSet,
                                                    Subject subject,
                                                    ConnectionRequestInfo cxRequestInfo) 
      throws ResourceException;

   /** 
    * Set the log writer for this ManagedConnectionFactory instance.</p>
    *
    *  <p>The log writer is a character output stream to which all logging and
    *  tracing messages for this ManagedConnectionfactory instance will be 
    *  printed.</p>
    *
    * <p>ApplicationServer manages the association of output stream with the
    *  ManagedConnectionFactory. When a ManagedConnectionFactory object is 
    *  created the log writer is initially null, in other words, logging is 
    *  disabled. Once a log writer is associated with a
    *  ManagedConnectionFactory, logging and tracing for 
    *  ManagedConnectionFactory instance is enabled.
    *
    *  <p>The ManagedConnection instances created by ManagedConnectionFactory
    *  "inherits" the log writer, which can be overridden by ApplicationServer
    *  using ManagedConnection.setLogWriter to set ManagedConnection specific
    *  logging and tracing.
    *
    *  @param   out                   PrintWriter - an out stream for
    *                                 error logging and tracing
    *  @throws  ResourceException     generic exception
    *  @throws  ResourceAdapterInternalException
    *                                 resource adapter related error condition
    */
   public void setLogWriter(java.io.PrintWriter out) throws ResourceException;
   
   /** 
    * Get the log writer for this ManagedConnectionFactory instance.
    *
    *  <p>The log writer is a character output stream to which all logging and
    *  tracing messages for this ManagedConnectionFactory instance will be 
    *  printed
    *
    *  <p>ApplicationServer manages the association of output stream with the
    *  ManagedConnectionFactory. When a ManagedConnectionFactory object is 
    *  created the log writer is initially null, in other words, logging is 
    *  disabled.
    *
    *  @return  PrintWriter
    *  @throws  ResourceException     generic exception
    */
   public java.io.PrintWriter getLogWriter() throws ResourceException;
   
   /** 
    * Returns the hash code for the ManagedConnectionFactory
    * 
    *  @return  hash code for the ManagedConnectionFactory
    */
   public int hashCode();
   
   /** 
    * Check if this ManagedConnectionFactory is equal to another
    * ManagedConnectionFactory.
    *  @param other The other object
    *  @return  true if two instances are equal
    */
   public boolean equals(Object other);
}
