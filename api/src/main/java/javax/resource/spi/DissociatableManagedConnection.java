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
