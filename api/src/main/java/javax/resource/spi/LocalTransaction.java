/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package javax.resource.spi;

import javax.resource.ResourceException;

/** LocalTransaction interface provides support for transactions that
 *  are managed internal to an EIS resource manager, and do not require
 *  an external transaction manager.
 * 
 *  <p>A resource adapter implements the javax.resource.spi.LocalTransaction 
 *  interface to provide support for local transactions that are performed
 *  on the underlying resource manager.
 *
 *  <p>If a resource adapter supports the LocalTransaction interface, then 
 *  the application server can choose to perform local transaction 
 *  optimization (uses local transaction instead of a JTA transaction for
 *  a single resource manager case).
 *
 *  @version     0.5
 *  @author      Rahul Sharma
 *  @see         javax.resource.spi.ManagedConnection
 **/
public interface LocalTransaction 
{
   /** Begin a local transaction
    *  
    *  @throws  ResourceException   generic exception if operation fails
    *  @throws  LocalTransactionException  
    *                               error condition related 
    *                               to local transaction management
    *  @throws  ResourceAdapterInternalException
    *                               error condition internal to resource
    *                               adapter
    *  @throws  EISSystemException  EIS instance specific error condition        
    **/
   public void begin() throws ResourceException;

   /** Commit a local transaction 
    *
    *  @throws  ResourceException   generic exception if operation fails
    *  @throws  LocalTransactionException  
    *                               error condition related 
    *                               to local transaction management
    *  @throws  ResourceAdapterInternalException
    *                               error condition internal to resource
    *                               adapter
    *  @throws  EISSystemException  EIS instance specific error condition        
    **/
   public void commit() throws ResourceException;
  
   /** Rollback a local transaction
    *  @throws  ResourceException   generic exception if operation fails
    *  @throws  LocalTransactionException  
    *                               error condition related 
    *                               to local transaction management
    *  @throws  ResourceAdapterInternalException
    *                               error condition internal to resource
    *                               adapter
    *  @throws  EISSystemException  EIS instance specific error condition        
    **/
   public void rollback() throws ResourceException;
}
