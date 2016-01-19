/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.core.connectionmanager.listener.dflt;

import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.txlog.TxLogConnection;
import org.ironjacamar.rars.txlog.TxLogConnectionFactory;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Enlistment for LocalTransaction connection listener
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class EnlistmentLocalTransactionTestCase
{
   /** The txlog connection factory 1 */
   @Resource(mappedName = "java:/eis/TxLogConnectionFactory1")
   private static TxLogConnectionFactory cf1;

   /** The txlog connection factory 2 */
   @Resource(mappedName = "java:/eis/TxLogConnectionFactory2")
   private static TxLogConnectionFactory cf2;

   /** The TransactionManager */
   @Inject
   private static TransactionManager tm;
   
   /** The UserTransaction */
   @Inject
   private static UserTransaction ut;
   
   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogRar();
   }
   
   /**
    * The activation for resource 1
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivationOne() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.LocalTransaction, "1");
   }
   
   /**
    * The activation for resource 2
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private static ResourceAdaptersDescriptor createActivationTwo() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.LocalTransaction, "2");
   }
   
   /**
    * Commit
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testCommit() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c = cf1.getConnection();
      assertNotNull(c);

      String id = c.getId();
      
      c.close();

      assertFalse(c.isInPool());

      ut.commit();

      assertTrue(c.isInPool());

      c = cf1.getConnection();

      assertEquals("01", c.getState(id));
      c.clearState(id);
      
      c.close();
   }

   /**
    * Rollback
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testRollback() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c = cf1.getConnection();
      assertNotNull(c);

      String id = c.getId();
      
      c.close();

      assertFalse(c.isInPool());

      ut.rollback();

      assertTrue(c.isInPool());

      c = cf1.getConnection();

      assertEquals("02", c.getState(id));
      c.clearState(id);
      
      c.close();
   }

   /**
    * SetRollbackOnly - before connection is obtained
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testSetRollbackOnlyBefore() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      ut.setRollbackOnly();

      try
      {
         TxLogConnection c = cf1.getConnection();
         fail("Connection was obtained");
      }
      catch (ResourceException re)
      {
         // Ok
      }

      ut.rollback();
   }

   /**
    * SetRollbackOnly - after connection is obtained
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testSetRollbackOnlyAfter() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c = cf1.getConnection();
      assertNotNull(c);

      ut.setRollbackOnly();
      
      String idOrig = c.getId();
      
      c.close();

      assertFalse(c.isInPool());

      c = cf1.getConnection();
      String idNext = c.getId();

      assertEquals(idOrig, idNext);

      c.close();
      
      ut.rollback();

      assertTrue(c.isInPool());

      c = cf1.getConnection();

      assertEquals("02", c.getState(idOrig));
      c.clearState(idOrig);
      
      c.close();
   }

   /**
    * Suspend/resume
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testSuspendResume() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(tm);

      tm.begin();
      
      TxLogConnection c1 = cf1.getConnection();
      assertNotNull(c1);

      String id1 = c1.getId();

      Transaction tx = tm.suspend();

      tm.begin();
      
      TxLogConnection c2 = cf1.getConnection();
      assertNotNull(c2);

      String id2 = c2.getId();

      c2.close();
      assertFalse(c2.isInPool());

      tm.commit();

      assertTrue(c2.isInPool());

      tm.resume(tx);
      
      c1.close();

      assertFalse(c1.isInPool());

      tm.commit();

      assertTrue(c1.isInPool());

      c1 = cf1.getConnection();

      assertEquals("01", c1.getState(id1));
      assertEquals("01", c1.getState(id2));
      assertNotEquals(id1, id2);
      
      c1.clearState(id1);
      c1.clearState(id2);
      
      c1.close();
   }

   /**
    * 2 LocalXAResource enlistment
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testTwoLocalXAResourceEnlistment() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(cf2);
      assertNotNull(tm);

      tm.begin();
      
      TxLogConnection c1 = cf1.getConnection();
      assertNotNull(c1);

      String id1 = c1.getId();

      try
      {
         TxLogConnection c2 = cf2.getConnection();
         fail("2 LocalXAResource enlisted");
      }
      catch (ResourceException e)
      {
         // Ok
      }
      
      c1.close();

      assertFalse(c1.isInPool());

      tm.rollback();

      assertTrue(c1.isInPool());

      c1 = cf1.getConnection();

      assertEquals("02", c1.getState(id1));
      
      c1.clearState(id1);
      c1.close();
   }
}
