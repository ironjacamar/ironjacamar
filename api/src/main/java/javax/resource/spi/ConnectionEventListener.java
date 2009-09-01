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

import java.util.EventListener;

/**  The <code>ConnectionEventListener</code> interface provides an event
 *   callback mechanism to enable an application server to receive 
 *   notifications from a <code>ManagedConnection</code> instance. 
 *
 *   <p>An application server uses these event notifications to manage 
 *   its connection pool, to clean up any invalid or terminated connections
 *   and to manage local transactions.
 *
 *   <p>An application server implements the 
 *   <code>ConnectionEventListener</code> interface. It registers a connection 
 *   listener with a <code>ManagedConnection</code> instance by using 
 *   <code>ManagedConnection.addConnectionEventListener</code> method.
 *  
 *   @version     0.5
 *   @author      Rahul Sharma
 *
 *   @see         javax.resource.spi.ConnectionEvent
 **/

public interface ConnectionEventListener extends EventListener 
{
   
   /** Notifies that an application component has closed the connection.
    *
    *  <p>A ManagedConnection instance notifies its registered set of 
    *  listeners by calling ConnectionEventListener.connectionClosed method
    *  when an application component closes a connection handle. The 
    *  application server uses this connection close event to put the
    *  ManagedConnection instance back in to the connection pool.
    *
    *  @param    event     event object describing the source of 
    *                      the event
    */
   public void connectionClosed(ConnectionEvent event);
   
   /** Notifies that a Resource Manager Local Transaction was started on
    *  the ManagedConnection instance.
    *
    *  @param    event     event object describing the source of 
    *                      the event
    */
   public void localTransactionStarted(ConnectionEvent event);
   
   /** Notifies that a Resource Manager Local Transaction was committed 
    *  on the ManagedConnection instance.
    *
    *  @param    event     event object describing the source of 
    *                      the event
    */
   public void localTransactionCommitted(ConnectionEvent event);
   
   /** Notifies that a Resource Manager Local Transaction was rolled back 
    *  on the ManagedConnection instance.
    *
    *  @param    event     event object describing the source of 
    *                      the event
    */
   public void localTransactionRolledback(ConnectionEvent event);
   
   /** Notifies a connection related error. 
    *  The ManagedConnection instance calls the method
    *  ConnectionEventListener.connectionErrorOccurred to notify 
    *  its registered listeners of the occurrence of a physical 
    *  connection-related error. The event notification happens 
    *  just before a resource adapter throws an exception to the 
    *  application component using the connection handle.
    *
    *  The connectionErrorOccurred method indicates that the 
    *  associated ManagedConnection instance is now invalid and 
    *  unusable. The application server handles the connection 
    *  error event notification by initiating application 
    *  server-specific cleanup (for example, removing ManagedConnection 
    *  instance from the connection pool) and then calling
    *  ManagedConnection.destroy method to destroy the physical 
    *  connection.
    *
    * @param     event     event object describing the source of 
    *                      the event
    */
   public void connectionErrorOccurred(ConnectionEvent event);
}
