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

import javax.resource.spi.TransactionSupport;

/**
 * Annotation used to define a Connector Connection Factory resource to be registered in JNDI. 
 * Once defined, a resource may be referenced by a component using the lookup element of the Resource annotation.
 *
 * @since 1.7
 * @see javax.annotation.Resource
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectionFactoryDefinition
{
   /** JNDI name of the connection factory resource being defined. */
   String name();

   /** Description of the resource. */
   String description() default "";

   /**
    * The name of the resource adapter that the connection factory must be created from.
    * The resource adapter is required to be available at deployment time.
    */
   String resourceAdapter();

   /** The fully qualified domain name of the connection factory interface class. */
   String className();

   /**
    * The level of transaction support the connection factory resource needs to support.
    * If a transaction support specification is specified, it must be a level of transaction support whose
    * ordinal value in the TransactionSupport.TransactionSupportLevel enum is equal to or lesser than the
    * resource adapter's transaction support classification.
    */
   TransactionSupport.TransactionSupportLevel transactionSupport() default 
      TransactionSupport.TransactionSupportLevel.NoTransaction;

   /**
    * The maximum number of connections that should be allocated for a connection pool that backs this
    * connnection factory resource. The default for this attribute is vendor specific.
    */
   int maxPoolSize() default -1;

   /**
    * The minimum number of connections that should be allocated for a connection pool that backs this
    * connnection factory resource. The default for this attribute is vendor specific.
    */
   int minPoolSize() default -1;

   /**
    * Properties of the Connection Factory.
    *
    * These properties may be vendor-specific properties. Vendor-specific properties may be combined 
    * with or used to override the connection factory properties defined using this annotation.
    * Connection Factory properties that are specified and are not supported
    * in a given resource adapter or cannot be mapped to a vendor specific configuration property may be ignored.
    */
   String[] properties() default { };
}
