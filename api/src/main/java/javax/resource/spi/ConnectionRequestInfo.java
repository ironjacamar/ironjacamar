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

/** <p>The ConnectionRequestInfo interface enables a resource adapter to 
 *  pass its own request specific data structure across the connection
 *  request flow. A resource adapter extends the empty interface to
 *  supports its own data structures for connection request.
 *  
 *  <p>A typical use allows a resource adapter to handle 
 *  application component specified per-connection request properties
 *  (example - client ID, language). The application server passes these 
 *  properties back across to match/createManagedConnection calls on 
 *  the resource adapter. These properties remain opaque to the 
 *  application server during the connection request flow. 
 *
 *  <p>Once the ConnectionRequestInfo reaches match/createManagedConnection
 *  methods on the ManagedConnectionFactory instance, resource adapter
 *  uses this additional per-request information to do connection 
 *  creation and matching.
 *
 *  @version     0.8
 *  @author      Rahul Sharma
 *  @see         javax.resource.spi.ManagedConnectionFactory
 *  @see         javax.resource.spi.ManagedConnection
**/

public interface ConnectionRequestInfo 
{
   
   /** Checks whether this instance is equal to another. Since
    *  connectionRequestInfo is defined specific to a resource
    *  adapter, the resource adapter is required to implement
    *  this method. The conditions for equality are specific
    *  to the resource adapter.
    *  @param other The other object
    *  @return True if the two instances are equal.
    **/
   public boolean equals(Object other);
   
   /** Returns the hashCode of the ConnectionRequestInfo.
    *
    *  @return hash code os this instance
    **/
   public int hashCode();
}
