/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.deployers;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * The deployers bundle.
 *
 * Message ids ranging from 020000 to 029999 inclusively.
 */
@MessageBundle(projectCode = "IJ")
public interface DeployersBundle
{
   /**
    * Unable to start resource adapter
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20051, value = "Unable to start %s")
   public String unableToStartResourceAdapter(String clz);

   /**
    * Unable to associate
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20052, value = "Unable to associate %s")
   public String unableToAssociate(String clz);

   /**
    * Undefined managed connection factory
    * @return The value
    */
   @Message(id = 20053, value = "ManagedConnectionFactory must be defined in class-name")
   public String undefinedManagedConnectionFactory();

   /**
    * Undefined admin object
    * @return The value
    */
   @Message(id = 20054, value = "AdminObject must be defined in class-name")
   public String undefinedAdminObject();

   /**
    * Failed to bind admin object
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20055, value = "Failed to bind admin object %s")
   public String failedToBindAdminObject(String clz);

   /**
    * Deployment failed
    * @param url The url
    * @return The value
    */
   @Message(id = 20056, value = "Deployment failed: %s")
   public String deploymentFailed(String url);

   /**
    * Invalid managed connection factory
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20057, value = "Invalid ManagedConnectionFactory class: %s")
   public String invalidManagedConnectionFactory(String clz);

   /**
    * Invalid activation spec
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20058, value = "Invalid ActivationSpec class: %s")
   public String invalidActivationSpec(String clz);

   /**
    * Invalid resource adapter
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20059, value = "Invalid ResourceAdapter class: %s")
   public String invalidResourceAdapter(String clz);

   /**
    * Unable to inject
    * @param clz The class name
    * @param name The name
    * @param value The value
    * @return The value
    */
   @Message(id = 20060, value = "Unable to inject: %s property: %s value: %s")
   public String unableToInject(String clz, String name, String value);

   /**
    * Invalid work context
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20061, value = "Invalid required work context: %s")
   public String invalidRequiredWorkContext(String clz);

   /**
    * Invalid connection factory interface
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20062, value = "Invalid connection factory interface: %s")
   public String invalidConnectionFactoryInterface(String clz);

   /**
    * Invalid connection factory implementation
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20063, value = "Invalid connection factory implementation: %s")
   public String invalidConnectionFactoryImplementation(String clz);

   /**
    * Invalid connection interface
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20064, value = "Invalid connection interface: %s")
   public String invalidConnectionInterface(String clz);

   /**
    * Invalid connection implementation
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20065, value = "Invalid connection implementation: %s")
   public String invalidConnectionImplementation(String clz);

   /**
    * Connection factory implementation doesn't implement connection factory interface
    * @param intClz The interface class name
    * @param implClz The implementation class name
    * @return The value
    */
   @Message(id = 20066, value = "Connection factory implementation (%s) "
         + "doesn't implement connection factory interface (%s)")
   public String invalidConnectionFactoryImplementationDueToInterface(String intClz, String implClz);

   /**
    * Connection implementation doesn't implement connection interface
    * @param intClz The interface class name
    * @param implClz The implementation class name
    * @return The value
    */
   @Message(id = 20067, value = "Connection implementation (%s) doesn't implement connection interface (%s)")
   public String invalidConnectionImplementationDueToInterface(String intClz, String implClz);
}
