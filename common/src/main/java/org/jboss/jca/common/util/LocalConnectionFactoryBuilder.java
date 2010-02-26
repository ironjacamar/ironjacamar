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

import org.jboss.jca.common.api.ConnectionFactoryBuilder;
import org.jboss.jca.fungal.deployers.DeployException;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * Local only connection factory builder.
 *
 * @author <a href="mailto:smarlow@redhat.com">Scott Marlow</a>
 */
public class LocalConnectionFactoryBuilder implements ConnectionFactoryBuilder
{

   private ManagedConnectionFactory mcf;
   private Object cf;
   private String name;

   /**
    * {@inheritDoc}
    */
   public ConnectionFactoryBuilder setName(String name)
   {
      if (name == null || name.trim().equals(""))
         throw new IllegalArgumentException("Name is null");
      this.name = name;
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public Reference build() throws DeployException
   {
      String className = cf.getClass().getName();
      String name = this.name;
      Reference ref = new Reference(
         cf.getClass().getName(),
         new StringRefAddr("class", className),
         LocalApplicationServerJNDIHandler.class.getName(),
         null);
      ref.add(new StringRefAddr("name", name));
      LocalApplicationServerJNDIHandler.register(name, className, this);
      return ref;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionFactoryBuilder setManagedConnectionFactory(ManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionFactory getManagedConnectionFactory()
   {
      return mcf;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionFactoryBuilder setConnectionFactory(Object cf)
   {
      this.cf = cf;
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnectionFactory()
   {
      return cf;
   }
}
