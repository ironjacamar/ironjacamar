/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.bv;

import java.lang.annotation.ElementType;

import javax.validation.Path;
import javax.validation.TraversableResolver;

/**
 * JCATraversableResolver
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class JCATraversableResolver implements TraversableResolver
{
   /**
    * Determine if Bean Validation is allowed to reach the property state
    * 
    * @param traversableObject
    *           object hosting <code>traversableProperty</code> or null if
    *           validateValue is called
    * @param traversableProperty
    *           the traversable property.
    * @param rootBeanType
    *           type of the root object passed to the Validator.
    * @param pathToTraversableObject
    *           path from the root object to <code>traversableObject</code>
    *           (using the path specification defined by Bean Validator).
    * @param elementType
    *           either <code>FIELD</code> or <code>METHOD</code>.
    * 
    * @return <code>true</code> if Bean Validation is allowed to reach the
    *         property state, <code>false</code> otherwise.
    */
   public boolean isReachable(Object traversableObject, Path.Node traversableProperty,
         Class<?> rootBeanType, Path pathToTraversableObject,
         ElementType elementType)
   {
      return true;
   }

   /**
    * Determine if Bean Validation is allowed to cascade validation on the bean
    * instance returned by the property value marked as <code>@Valid</code>.
    * Note that this method is called only if isReachable returns true for the
    * same set of arguments and if the property is marked as <code>@Valid</code>
    * 
    * @param traversableObject
    *           object hosting <code>traversableProperty</code> or null if
    *           validateValue is called
    * @param traversableProperty
    *           the traversable property.
    * @param rootBeanType
    *           type of the root object passed to the Validator.
    * @param pathToTraversableObject
    *           path from the root object to <code>traversableObject</code>
    *           (using the path specification defined by Bean Validator).
    * @param elementType
    *           either <code>FIELD</code> or <code>METHOD</code>.
    * 
    * @return <code>true</code> if Bean Validation is allowed to cascade
    *         validation, <code>false</code> otherwise.
    */
   public boolean isCascadable(Object traversableObject,
         Path.Node traversableProperty, Class<?> rootBeanType,
         Path pathToTraversableObject, ElementType elementType)
   {
      return true;
   }
}
