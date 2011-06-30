/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.logging.MessageBundle;
import org.jboss.logging.Message;

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
}
