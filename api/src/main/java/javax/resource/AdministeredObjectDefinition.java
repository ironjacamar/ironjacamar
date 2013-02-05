/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

package javax.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define a Connector administered object to be registered in JNDI. 
 * Once defined, a resource may be referenced by a component using the lookup element of the Resource annotation.
 *
 * @since 1.7
 * @see javax.annotation.Resource
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AdministeredObjectDefinition
{
   /** JNDI name of the administrered object resource being defined. */
   String name();

   /** Description of the resource. */
   String description() default "";

   /**
    * The name of the resource adapter that the administered object must be created from.
    * The resource adapter is required to be available at deployment time.
    */
   String resourceAdapter();

   /** The fully qualified domain name of the administered object class. */
   String className();

   /** The fully qualified domain name of the administered object interface class. */
   String interfaceName() default "";

   /**
    * Properties of the administered object.
    *
    * These properties may be vendor-specific properties. Vendor-specific properties may be combined 
    * with or used to override the administered object properties defined using this annotation.
    * Administered object properties that are specified and are not supported
    * in a given resource adapter or cannot be mapped to a vendor specific configuration property may be ignored.
    */
   String[] properties() default { };
}
