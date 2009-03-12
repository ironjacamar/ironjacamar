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

/** 
 * This interface is implemented by a <code>ManagedConnectionFactory</code>
 * instance that supports the ability to validate 
 * <code>ManagedConnection</code> objects.
 *
 * <p>This may be used by the application server to prune invalid 
 * <code>ManagedConnection</code> objects from its connection pool.
 *
 * <p>The application server may use this functionality to test the
 * validity of a <code>ManagedConnection</code> by passing in a 
 * <code>Set</code> of size one( with the <code>ManagedConnection</code>
 * that has to be tested for validity as the only member of the 
 * <code>Set</code>.
 * 
 *
 *  @author  Ram Jeyaraman
 *  @version 1.0
 */    
public interface ValidatingManagedConnectionFactory 
{
   /**
    * This method returns a set of invalid <code>ManagedConnection</code> 
    * objects chosen from a specified set of <code>ManagedConnection</code>
    * objects.
    *
    * @param connectionSet a set of <code>ManagedConnection</code> objects
    * that need to be validated.
    *
    * @return a set of invalid <code>ManagedConnection</code> objects.
    *
    * @throws ResourceException generic exception.
    */
   Set getInvalidConnections(Set connectionSet) throws ResourceException;
}
