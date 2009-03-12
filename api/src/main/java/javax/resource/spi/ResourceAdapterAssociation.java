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
