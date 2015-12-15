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
 * Enlistment for XATransaction connection listener
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class EnlistmentXATransactionTestCase
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
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.XATransaction, "1");
   }
   
   /**
    * The activation for resource 2
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private static ResourceAdaptersDescriptor createActivationTwo() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.XATransaction, "2");
   }
   
   /**
    * Commit
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testCommitOneResource() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c1 = cf1.getConnection();
      assertNotNull(c1);

      String id = c1.getId();
      
      c1.close();

      assertFalse(c1.isInPool());
      
      ut.commit();

      assertTrue(c1.isInPool());

      c1 = cf1.getConnection();

      assertEquals("3B8", c1.getState(id));
      c1.clearState(id);
      
      c1.close();
   }

   /**
    * Rollback
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testRollbackOneResource() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c1 = cf1.getConnection();
      assertNotNull(c1);

      String id = c1.getId();
      
      c1.close();

      assertFalse(c1.isInPool());
      
      ut.rollback();

      assertTrue(c1.isInPool());

      c1 = cf1.getConnection();

      assertEquals("3C9", c1.getState(id));
      c1.clearState(id);
      
      c1.close();
   }


   /**
    * SetRollbackOnly - before connection is obtained
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testSetRollbackOnlyBeforeOneResource() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      ut.setRollbackOnly();

      try
      {
         TxLogConnection c1 = cf1.getConnection();
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
   public void testSetRollbackOnlyAfterOneResource() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c1 = cf1.getConnection();
      assertNotNull(c1);

      ut.setRollbackOnly();
      
      String idOrig = c1.getId();
      
      c1.close();

      assertFalse(c1.isInPool());

      c1 = cf1.getConnection();
      String idNext = c1.getId();

      assertEquals(idOrig, idNext);

      c1.close();
      
      ut.rollback();

      assertTrue(c1.isInPool());

      c1 = cf1.getConnection();

      assertEquals("3C9", c1.getState(idOrig));
      c1.clearState(idOrig);
      
      c1.close();
   }


   /**
    * Suspend/resume
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testSuspendResumeOneResource() throws Throwable
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

      // Narayana doesn't send TMSUSPEND/TMRESUME - e.g. 3D5B8
      assertEquals("3B8", c1.getState(id1));
      assertEquals("3B8", c1.getState(id2));
      assertNotEquals(id1, id2);
      
      c1.clearState(id1);
      c1.clearState(id2);
      
      c1.close();
   }

   /**
    * Commit
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testCommitTwoResources() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(cf2);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c1 = cf1.getConnection();
      assertNotNull(c1);

      TxLogConnection c2 = cf2.getConnection();
      assertNotNull(c2);

      String id1 = c1.getId();
      String id2 = c2.getId();
      
      c1.close();
      c2.close();

      assertFalse(c1.isInPool());
      assertFalse(c2.isInPool());
      
      ut.commit();

      assertTrue(c1.isInPool());
      assertTrue(c2.isInPool());

      c1 = cf1.getConnection();

      assertEquals("3B78", c1.getState(id1));
      c1.clearState(id1);

      assertEquals("3B78", c1.getState(id2));
      c1.clearState(id2);
      
      c1.close();
   }

   /**
    * Rollback
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testRollbackTwoResources() throws Throwable
   {
      assertNotNull(cf1);
      assertNotNull(cf2);
      assertNotNull(ut);

      ut.begin();
      
      TxLogConnection c1 = cf1.getConnection();
      assertNotNull(c1);

      TxLogConnection c2 = cf2.getConnection();
      assertNotNull(c2);

      String id1 = c1.getId();
      String id2 = c2.getId();
      
      c1.close();
      c2.close();

      assertFalse(c1.isInPool());
      assertFalse(c2.isInPool());

      ut.rollback();

      assertTrue(c1.isInPool());
      assertTrue(c2.isInPool());

      c1 = cf1.getConnection();

      assertEquals("3C9", c1.getState(id1));
      c1.clearState(id1);

      assertEquals("3C9", c1.getState(id2));
      c1.clearState(id2);
      
      c1.close();
   }
}
