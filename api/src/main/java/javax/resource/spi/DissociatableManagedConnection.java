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
 * <code>ManagedConnection</code> implementation. An implementation of
 * this interface must support the lazy connection association optimization.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface DissociatableManagedConnection 
{
   
   /**
    * This method is called by an application server (that is capable of
    * lazy connection association optimization) in order to dissociate
    * a <code>ManagedConnection</code> instance from all of its connection
    * handles.
    *
    * @throws ResourceException generic exception if operation fails.
    *
    * @throws ResourceAdapterInternalException
    *            resource adapter internal error condition
    * @throws IllegalStateException
    *         Illegal state for dissociating a
    *         <code>ManagedConnection</code> from all of its connection
    *         handles.
    */
   void dissociateConnections() throws ResourceException;
}
