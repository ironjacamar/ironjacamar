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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import javax.resource.spi.work.WorkContext;

/**
 * The <code>Connector</code> annotation is a component-defining annotation and
 * it can be used by the resource adapter developer to specify that the JavaBean
 * is a resource adapter JavaBean. The Connector annotation is applied to the
 * JavaBean class.
 * 
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Connector 
{

   /**
    * Describes the resource adapter module.
    */
   String[] description() default { };
   
   /**
    * An optional short name, providing information about the
    * resource adapter module,  that is intended to be displayed 
    * by tools.
    */
   String[] displayName() default { };
 
   /**
    * Specifies the file name for small GIF or JPEG icon images that are 
    * used to represent the resource adapter in a GUI tool. 
    *
    * Each smallIcon must be associated with a largeIcon element and the 
    * application server must use the ordinal value in their respective 
    * arrays to find the related pairs of icons.
    */
   String[] smallIcon() default { };
   
   /**
    * Specifies the file name for large GIF or JPEG icon images that are 
    * used to represent the resource adapter in a GUI tool. 
    * Each smallIcon must be associated with a largeIcon element and 
    * the application server must use the ordinal value in their 
    * respective arrays to find the related pairs of icons.
    */
   String[] largeIcon() default { };
   
   /**
    * Specifies the name of the resource adapter provider vendor.
    */
   String vendorName() default "";
   
   /**
    * Contains information about the type of EIS. For example, the type of an
    * EIS can be product name of the EIS independent of any version info.This
    * helps in identifying EIS instances that can be used with this resource
    * adapter.
    */
   String eisType() default "";
   
   /**
    * Specifies the version of the resource adapter implementation.
    */
   String version() default "";
   
   /**
    * Specifies licensing requirements for the resource adapter module and an
    * optional description of the licensing terms .
    */
   String[] licenseDescription() default { };
   
   /**
    * Specifies whether a license is required to deploy and use this resource
    * adapter
    */
   boolean licenseRequired() default false;
   
   /**
    * Specifies the authentication mechanisms supported by the resource
    * adapter.
    * 
    * @see AuthenticationMechanism
    */
   AuthenticationMechanism[] authMechanisms() default { };
   
   /**
    * Specifies whether a license is required to deploy and use this resource
    * adapter
    */
   boolean reauthenticationSupport() default false;
   
   /**
    * Specifies the extended security permissions required to be provided for
    * the operation of the resource adapter module
    * 
    * @see SecurityPermission
    */
   SecurityPermission[] securityPermissions() default { };
   
   /**
    * Specifies the level of transaction support provided by the resource
    * adapter.
    * 
    * @see TransactionSupport.TransactionSupportLevel
    */
   TransactionSupport.TransactionSupportLevel transactionSupport() 
      default TransactionSupport.TransactionSupportLevel.NoTransaction;
   
   /**
    * Specifies a list of fully qualified classes that implements the
    * {@link WorkContext WorkContext} interface that a resource adapter
    * requires the application server to support.
    */
   Class<? extends WorkContext>[] requiredWorkContexts() default { };
}
