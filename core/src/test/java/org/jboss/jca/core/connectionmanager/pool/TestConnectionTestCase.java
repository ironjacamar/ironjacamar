/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2021, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.common.MockManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.security.DefaultSubjectFactory;
import org.jboss.jca.core.spi.security.SubjectFactory;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test connection test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public class TestConnectionTestCase
{
   /**
    * OnePool
    * @throws Exception for exception.
    */
   @Test
   public void testOnePool() throws Exception
   {
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ManagedConnectionFactory mcf = new MockManagedConnectionFactory();

      PoolConfiguration config = new PoolConfiguration();

      PoolFactory pf = new PoolFactory();
      Pool pool =
         pf.create(PoolStrategy.ONE_POOL, mcf, config, false, true,
                   org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory.DEFAULT_IMPLEMENTATION);

      NoTxConnectionManager noTxConnectionManager = 
         cmf.createNonTransactional(TransactionSupportLevel.NoTransaction, 
                                    pool, null, null, false, null, true, true, false, null,
                                    FlushStrategy.FAILING_CONNECTION_ONLY,
                                    null, null);

      pool.testConnection();
   }

   /**
    * PoolByCri
    * @throws Exception for exception.
    */
   @Test
   public void testPoolByCri() throws Exception
   {
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ManagedConnectionFactory mcf = new MockManagedConnectionFactory();

      PoolConfiguration config = new PoolConfiguration();

      PoolFactory pf = new PoolFactory();
      Pool pool =
         pf.create(PoolStrategy.POOL_BY_CRI, mcf, config, false, true,
                   org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory.DEFAULT_IMPLEMENTATION);

      boolean resourceException = false;
      try
      {
         pool.testConnection();
      }
      catch (ResourceException e) {
         resourceException = true;
      }
      assertTrue(resourceException);
   }

   /**
    * PoolBySubject
    * @throws Exception for exception.
    */
   @Test
   public void testPoolBySubject() throws Exception
   {
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ManagedConnectionFactory mcf = new MockManagedConnectionFactory();

      SubjectFactory subjectFactory = new DefaultSubjectFactory("domain", "user", "password");

      PoolConfiguration config = new PoolConfiguration();

      PoolFactory pf = new PoolFactory();
      Pool pool =
         pf.create(PoolStrategy.POOL_BY_SUBJECT, mcf, config, false, true,
                   org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory.DEFAULT_IMPLEMENTATION);

      NoTxConnectionManager noTxConnectionManager = 
         cmf.createNonTransactional(TransactionSupportLevel.NoTransaction, pool,
                                    subjectFactory, "domain", false, null, true, true, false, null,
                                    FlushStrategy.FAILING_CONNECTION_ONLY,
                                    null, null);

      pool.testConnection();
   }

   /**
    * PoolBySubjectAndCri
    * @throws Exception for exception.
    */
   @Test
   public void testPoolBySubjectAndCri() throws Exception
   {
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ManagedConnectionFactory mcf = new MockManagedConnectionFactory();

      PoolConfiguration config = new PoolConfiguration();

      PoolFactory pf = new PoolFactory();
      Pool pool =
         pf.create(PoolStrategy.POOL_BY_SUBJECT_AND_CRI, mcf, config, false, true,
                   org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory.DEFAULT_IMPLEMENTATION);

      boolean resourceException = false;
      try
      {
         pool.testConnection();
      }
      catch (ResourceException e) {
         resourceException = true;
      }
      assertTrue(resourceException);
   }

   /**
    * ReauthPool
    * @throws Exception for exception.
    */
   @Test
   public void testReauth() throws Exception
   {
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ManagedConnectionFactory mcf = new MockManagedConnectionFactory();

      PoolConfiguration config = new PoolConfiguration();

      PoolFactory pf = new PoolFactory();
      Pool pool =
         pf.create(PoolStrategy.REAUTH, mcf, config, false, true,
                   org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory.DEFAULT_IMPLEMENTATION);

      boolean resourceException = false;
      try
      {
         pool.testConnection();
      }
      catch (ResourceException e) {
         resourceException = true;
      }
      assertTrue(resourceException);
   }
}
