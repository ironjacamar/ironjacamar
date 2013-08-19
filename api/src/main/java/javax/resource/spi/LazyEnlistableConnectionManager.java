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

/**
 * This is a mix-in interface that may be optionally implemented by a 
 * <code>ConnectionManager</code> implementation. An implementation of
 * this interface must support the lazy transaction enlistment optimization.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface LazyEnlistableConnectionManager 
{
   
   /**
    * This method is called by a resource adapter (that is capable of
    * lazy transaction enlistment optimization) in order to lazily enlist
    * a connection object with a XA transaction. 
    *
    * @param mc The <code>ManagedConnection</code> instance that needs to be
    * lazily associated.
    *
    * @throws  ResourceException Generic exception.
    *
    * @throws  ApplicationServerInternalException 
    *                            Application server specific exception.
    *
    * @throws  ResourceAllocationException
    *                            Failed to allocate system resources for
    *                            connection request.
    *
    * @throws  ResourceAdapterInternalException
    *                            Resource adapter related error condition.
    */
   void lazyEnlist(ManagedConnection mc) throws ResourceException;
}
