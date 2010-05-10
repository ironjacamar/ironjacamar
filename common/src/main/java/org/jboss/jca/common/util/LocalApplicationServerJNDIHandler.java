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

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import com.github.fungal.spi.deployers.DeployException;

/**
 * Local only support for connection factory
 * TODO:  Add undeploy support to unregister CF builder
 *
 * @author <a href="mailto:smarlow@redhat.com">Scott Marlow</a>
 */

public class LocalApplicationServerJNDIHandler implements ObjectFactory
{

   private static ConcurrentHashMap<String, ConnectionFactoryBuilder> connectionFactories =
      new ConcurrentHashMap<String, ConnectionFactoryBuilder>();

   /**
    * Obtain the connection factory
    * {@inheritDoc}
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws Exception
   {
      Reference ref = (Reference)obj;
      String className = (String)ref.get("class").getContent();
      String cfname = (String)ref.get("name").getContent();
      ConnectionFactoryBuilder cfb = connectionFactories.get(qualifiedName(cfname, className));
      return cfb.getConnectionFactory();
   }

   /**
    * Register a connection factory builder
    * @param name of the connection factory
    * @param className connection factory class name
    * @param connectionFactoryBuilder is the connection factory builder to be registered 
    * @throws DeployException if already registered and therefore deployed
    */
   public static void register(String name,
                               String className,
                               ConnectionFactoryBuilder connectionFactoryBuilder)
      throws DeployException
   {

      if (connectionFactories.putIfAbsent(qualifiedName(name, className), connectionFactoryBuilder) != null)
      {
         throw new DeployException("Deployment " + className + " failed, " + name + " is already deployed");
      }
   }

   private static String qualifiedName(String name, String className)
   {
      return className + "#" + name;
   }

}
