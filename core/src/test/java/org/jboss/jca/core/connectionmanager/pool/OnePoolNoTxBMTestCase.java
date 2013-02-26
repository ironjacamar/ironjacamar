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

import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;

import java.util.Locale;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * An OnePoolTestCase.
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
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
      return getDeployment();
   }

   /**
    * testConnection
    * @throws Exception in case of unexpected errors
    */
   @Test
   @BMRule(name = "Throw exception on getConnection",
           targetClass = "org.jboss.jca.core.connectionmanager.pool.AbstractPool",
           targetMethod = "getConnection",
           action = "throw new javax.resource.ResourceException()")
   public void testConnection() throws Exception
   {
      AbstractPool pool = getPool();
      assertFalse(((OnePool) pool).testConnection());
   }
}
