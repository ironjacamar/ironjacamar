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

/** <code>ConnectionFactory</code> provides an interface for getting
 *  connection to an EIS instance. An implementation of ConnectionFactory
 *  interface is provided by a resource adapter.
 *
 *  <p>Application code looks up a ConnectionFactory instance from JNDI
 *  namespace and uses it to get EIS connections. 
 *
 *  <p>An implementation class for ConnectionFactory is required to 
 *  implement <code>java.io.Serializable</code> and 
 *  <code>javax.resource.Referenceable</code>interfaces to support 
 *  JNDI registration.
 *  
 *  @author  Rahul Sharma
 *  @version 0.8
 *  @see     javax.resource.cci.Connection
 *  @see     javax.resource.Referenceable
 **/
public interface ConnectionFactory  extends java.io.Serializable, javax.resource.Referenceable 
{
   
   /** Gets a connection to an EIS instance. This getConnection variant
    *  should be used when a component wants the container to manage EIS 
    *  sign-on. This case is termed container-managed sign-on. The 
    *  component does not pass any security information.
    *
    *
    *  @return   Connection instance
    *  @throws   ResourceException   Failed to get a connection to
    *                                the EIS instance. Examples of
    *                                error cases are:
    *          <UL>
    *          <LI> Invalid configuration of ManagedConnectionFactory--
    *               example: invalid server name
    *          <LI> Application server-internal error--example:
    *               connection pool related error
    *          <LI> Communication error 
    *          <LI> EIS-specific error--example: EIS not active
    *          <LI> Resource adapter-internal error
    *          <LI> Security related error; example: invalid user
    *          <LI> Failure to allocate system resources
    *         </UL>                        
    **/
   public Connection getConnection() throws ResourceException;

   /** Gets a connection to an EIS instance. A component should use 
    *  the getConnection variant with javax.resource.cci.ConnectionSpec
    *  parameter, if it needs to pass any resource adapter specific 
    *  security information and connection parameters. In the component-
    *  managed sign-on case, an application component passes security 
    *  information (example: username, password) through the 
    *  ConnectionSpec instance.
    * 
    *  <p>It is important to note that the properties passed through 
    *  the getConnection method should be client-specific (example: 
    *  username, password, language) and not related to the 
    *  configuration of a target EIS instance (example: port number, 
    *  server name). The ManagedConnectionFactory instance is configured
    *  with complete set of properties required for the creation of a 
    *  connection to an EIS instance. 
    *
    *  @param  properties          Connection parameters and security
    *                              information specified as 
    *                              ConnectionSpec instance
    *  @return Connection instance
    *
    *  @throws ResourceException   Failed to get a connection to
    *                              the EIS instance. Examples of
    *                              error cases are:
    *          <UL>
    *          <LI> Invalid specification of input parameters
    *          <LI> Invalid configuration of ManagedConnectionFactory--
    *               example: invalid server name
    *          <LI> Application server-internal error--example:
    *               connection pool related error
    *          <LI> Communication error 
    *          <LI> EIS-specific error--example: EIS not active
    *          <LI> Resource adapter-internal error
    *          <LI> Security related error; example: invalid user
    *          <LI> Failure to allocate system resources
    *         </UL>                        
    *  @see     javax.resource.cci.ConnectionSpec
    **/
   public Connection getConnection(ConnectionSpec properties) 
      throws ResourceException;

   /** Gets a RecordFactory instance. The RecordFactory is used for
    *  the creation of generic Record instances.
    *
    *  @return RecordFactory         RecordFactory instance
    *
    *  @throws ResourceException     Failed to create a RecordFactory
    *  @throws javax.resource.NotSupportedException Operation not supported
    **/
   public RecordFactory getRecordFactory() throws ResourceException;

   /** Gets metadata for the Resource Adapter. Note that the metadata
    *  information is about the ResourceAdapter and not the EIS instance.
    *  An invocation of this method does not require that an active
    *  connection to an EIS instance should have been established.
    *
    *  @return  ResourceAdapterMetaData instance
    *  @throws   ResourceException Failed to get metadata information 
    *                              about the resource adapter
    **/
   public ResourceAdapterMetaData getMetaData() throws ResourceException;
}
