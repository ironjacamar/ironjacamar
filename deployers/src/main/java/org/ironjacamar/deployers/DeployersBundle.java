/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.ironjacamar.deployers;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * The deployers bundle.
 *
 * Message ids ranging from 020000 to 029999 inclusively.
 */
@MessageBundle(projectCode = "IJ2")
public interface DeployersBundle
{
   /**
    * Unable to create resource adapter
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20051, value = "Unable to start %s")
   public String unableToCreateResourceAdapter(String clz);

   /**
    * Unable to associate
    * @param clz The class name
    * @return The value
    */
   @Message(id = 20052, value = "Unable to associate %s")
   public String unableToAssociate(String clz);

   /**
    * Unable to create connection definition
    * @param id the connection definition id
    * @param jndiName the connection definition jndi name
    * @return The value
    */
   @Message(id = 20053, value = "Unable to create connection definition (id=%s, jndiName=%s)")
   public String unableToCreateConnectionDefinition(String id, String jndiName);

   /**
    * Unable to admin object definition
    * @param id the admin object id
    * @param jndiName the admin object jndi name
    * @return The value
    */
   @Message(id = 20054, value = "Unable to create admin object (id=%s, jndiName=%s)")
   public String unableToCreateAdminObject(String id, String jndiName);

   /**
    * Deployment failed
    * @param archiveName The archive's name
    * @return The value
    */
   @Message(id = 20055, value = "Deployment failed: %s")
   public String deploymentFailed(String archiveName);

   /**
    * Unable to inject
    * @param clz The class name
    * @param name The name
    * @param value The value
    * @return The value
    */
   @Message(id = 20056, value = "Unable to inject: %s property: %s value: %s")
   public String unableToInject(String clz, String name, String value);

   /**
    * Unable to register the deployment into deployment repository
    * @param identifier The deployment identifier
    * @param name The deployment name
    * @return The value
    */
   @Message(id = 20057, value = "Unable to register deployment (identifier=%s, name=%s) into deployment repository")
   public String unableToRegister(String identifier, String name);

   /**
    * unable to load bean validation group
    * @param clzName the class name
    * @param deployment the deployment id
    * @return the value
    */
   @Message(id = 20058, value = "Unable to load beanValidationGroup %s for Deployment %s")
   public String unableToLoadBeanValidationGroup(String clzName, String deployment);

   /**
    * violation of a validation rule
    * @param deployment the deployment id
    * @return the value
    */
   @Message(id = 20059, value = "Violation for %s")
   public String violationOfValidationRule(String deployment);
}
