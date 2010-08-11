/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.api.metadata;

/**
 *
 * A CopyableMetaData.
 * This interface force implementors to override {@link #copy()} method.
 * On the presence of this method are based some assumption to use generics during clone deep into the metadatas
 * members.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface CopyableMetaData extends Cloneable
{

   /**
    * Creates and returns a copy of this object.  Ther copy is done in deep of all elements.
    * It isn't formally a clone of the instance since it does not respect the assumption for which returned
    * clone of cloneMethod are instance of Object
    *
    * @return     a copy of this instance.
    */

   public CopyableMetaData copy();

}
