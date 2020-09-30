/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.spi.connectionmanager;

import java.util.Set;

import javax.resource.ResourceException;

/**
 * ComponentStack.
 * 
 * @author <a href="abrock@redhat.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ComponentStack
{
   /**
    * Push a component context
    * 
    * @param rawKey the raw key, e.g. the servlet or ejb context
    * @param unsharableResources a set of real jndi names marked as unshareable 
    * @throws ResourceException for any error
    */
   @SuppressWarnings("unchecked")
   public void pushMetaAwareObject(final Object rawKey, Set unsharableResources) throws ResourceException;

   /**
    * Pop a component context
    * 
    * @param unsharableResources a set of real jndi names marked as unshareable 
    * @throws ResourceException for any error
    */
   @SuppressWarnings("unchecked")
   public void popMetaAwareObject(Set unsharableResources) throws ResourceException;
}
