/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.unit;

import org.jboss.jca.adapters.ArquillianJCATestUtils;
import org.jboss.jca.adapters.ConnectionManagerUtil;
import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.sql.Connection;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.TransactionManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases for SQLException on commit and rollback
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
public class CommitRollbackTestCase
{

   //-------------------------------------------------------------------------------------||
   //---------------------- GIVEN --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 1)
   public static ResourceAdapterArchive createArchive() throws Exception
   {
      return ArquillianJCATestUtils.buildShrinkwrapJdbcLocal();
   }

   /**
    * Define the -ds.xml
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment(order = 2)
   public static Descriptor createDescriptor() throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("sqlexception-ds.xml",
                                                            cl.getResourceAsStream("sqlexception-ds.xml"));
      return isd;
   }

   //-------------------------------------------------------------------------------------||
   //---------------------- WHEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   //
   @Resource(mappedName = "java:/SQLExceptionDS")
   private static DataSource ds;

   @Inject(name = "RealTransactionManager")
   private TransactionManager tm;

   //-------------------------------------------------------------------------------------||
   //---------------------- THEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Commit
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testCommit() throws Throwable
   {
      assertNotNull(ds);
      assertNotNull(tm);

      try
      {
         tm.begin();

         Connection c = ds.getConnection();
         assertNotNull(c);
         c.close();

         tm.commit();
      }
      //FIXME was RollbackException double check
      catch (HeuristicMixedException re)
      {
         // Expected
      }
   }

   /**
    * Rollback
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testRollback() throws Throwable
   {
      assertNotNull(ds);
      assertNotNull(tm);

      tm.begin();

      Connection c = ds.getConnection();
      assertNotNull(c);
      c.close();

      tm.rollback();
   }

   /**
    * After class
    * @exception Throwable In case of an assertion
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      ConnectionManager cm = ConnectionManagerUtil.extract(ds);
      assertNotNull(cm);
      assertEquals(2, cm.getPool().getStatistics().getDestroyedCount());
   }
}
