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
 * This interface specifies the methods to associate a 
 * <code>ResourceAdapter</code> object with other objects that 
 * implement this interface like 
 * <code>ManagedConnectionFactory</code> and <code>ActivationSpec</code>.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface ResourceAdapterAssociation 
{
   
   /**
    * Get the associated <code>ResourceAdapter</code> object.
    *
    * @return the associated <code>ResourceAdapter</code> object.
    */
   ResourceAdapter getResourceAdapter();
   
   /**
    * Associate this object with a <code>ResourceAdapter</code> object. 
    * Note, this method must be called exactly once. That is, the 
    * association must not change during the lifetime of this object.
    *
    * @param ra <code>ResourceAdapter</code> object to be associated with.
    *
    * @throws ResourceException generic exception.
    *
    * @throws ResourceAdapterInternalException
    *            resource adapter related error condition.
    *
    * @throws IllegalStateException indicates that this object is in an
    * illegal state for the method invocation. For example, this occurs when
    * this method is called more than once on the same object.
    */
   void setResourceAdapter(ResourceAdapter ra) throws ResourceException;
}
