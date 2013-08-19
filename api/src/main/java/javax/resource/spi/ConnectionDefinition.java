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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Defines a set of connection interfaces and classes pertaining to a particular
 * connection type. This annotation can be placed only on a JavaBean that
 * implements the {@link ManagedConnectionFactory ManagedConnectionFactory}
 * interface.
 * 
 * @since 1.6
 * 
 * @version Java EE Connector Architecture 1.6
 */

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface ConnectionDefinition 
{

   /**
    * Specifies the ConnectionFactory interface supported by the resource
    * adapter. Example: javax.resource.cci.ConnectionFactory or
    * com.wombat.ConnectionFactory
    */
   Class connectionFactory();
   
   /**
    * Specifies the Class provided by the resource adapter that implements the
    * resource adapter specific ConnectionFactory interface. Example:
    * com.wombat.ConnectionFactoryImpl
    */
   Class connectionFactoryImpl();
   
   /**
    * Specifies the Connection interface supported by the resource adapter.
    * Example: javax.resource.cci.Connection or com.wombat.Connection
    */
   Class connection();
   
   /**
    * Specifies the class provided by the resource adapter that implements the
    * resource adapter specific Connection interface. Example:
    * com.wombat.ConnectionImpl
    */
   Class connectionImpl();
}
