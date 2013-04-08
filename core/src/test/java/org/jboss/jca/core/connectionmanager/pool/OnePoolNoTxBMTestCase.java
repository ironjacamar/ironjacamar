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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;

import javax.resource.ResourceException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Byteman test for OnePool
 * 
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class OnePoolNoTxBMTestCase extends PoolTestCaseAbstract
{

   /**
    * deployment
    * @return archive
    */
   @Deployment
   public static ResourceAdapterArchive deployment()
   {
      return createNoTxDeployment(getBasicIJXml(SimpleManagedConnectionFactory.class.getName()));
   }

   /**
    * getConnection
    * @throws Throwable in case of unexpected errors
    */
   @Test
   @BMRule(name = "Throw exception on getConnection", 
   targetClass = "org.jboss.jca.core.connectionmanager.pool.AbstractPool", 
   targetMethod = "getConnection", 
   action = " throw new javax.resource.ResourceException()")
   public  void testGetConnection() throws Throwable
   {
      AbstractPool pool = getPool();
      ConnectionListener cl = null;
      try
      {
         cl = pool.getConnection(null, null, null);
      }
      catch (ResourceException re)
      {
         log.info("////Got");
         re.printStackTrace();
         // Ok
      }
      catch (Throwable t)
      {
         fail(t.getMessage());
         throw t;
      }
   }

}
