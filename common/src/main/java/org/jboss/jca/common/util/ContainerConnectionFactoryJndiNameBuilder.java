/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.common.util;

import org.jboss.jca.common.api.ConnectionFactoryJndiNameBuilder;

/**
 * Used for building JNDI bind names.
 *
 * @author <a href="mailto:smarlow@redhat.com">Scott Marlow</a>
 */
public class ContainerConnectionFactoryJndiNameBuilder implements ConnectionFactoryJndiNameBuilder
{
   String deploymentName;

   /** JNDI prefix */
   private static final String JNDI_PREFIX = "java:/eis/";

   /**
    * {@inheritDoc}
    */
   public String build()
   {
      return JNDI_PREFIX + deploymentName;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionFactoryJndiNameBuilder setDeploymentName(String deploymentName)
   {
      if (deploymentName == null || deploymentName.trim().equals(""))
         throw new IllegalArgumentException("deploymentFile is null");
      this.deploymentName =  deploymentName;
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionFactoryJndiNameBuilder setConnectionFactory(String className)
   {
      return this;
   }

}
