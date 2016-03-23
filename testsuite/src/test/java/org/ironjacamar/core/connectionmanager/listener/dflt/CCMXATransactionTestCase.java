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

import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
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

import java.util.HashSet;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.UserTransaction;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * CCM interaction for XATransaction connection listener
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class CCMXATransactionTestCase
{
   /** The txlog connection factory */
   @Resource(mappedName = "java:/eis/TxLogConnectionFactory")
   private static TxLogConnectionFactory cf;

   /** The cached connection manager */
   @Inject
   private static CachedConnectionManager ccm;
   
   /** The UserTransaction */
   @Inject
   private static UserTransaction ut;
   
   /** Unsharable resources */
   private static HashSet unsharableResources = new HashSet();

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
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.XATransaction,
                                                          "", Boolean.FALSE);
   }
   
   /**
    * Lazy enlistment - 1 connection
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testLazyEnlistmentOneConnection() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(ccm);
      assertNotNull(ut);

      ccm.pushContext(new Object(), unsharableResources);
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      String id = c.getId();

      assertEquals("", c.getState(id));
      
      ut.begin();
      
      c.close();

      assertFalse(c.isInPool());

      ut.commit();

      assertTrue(c.isInPool());

      c = cf.getConnection();

      assertEquals("3B8", c.getState(id));
      c.clearState(id);
      
      c.close();

      ccm.popContext(unsharableResources);
   }

   /**
    * Lazy enlistment - 2 connections
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testLazyEnlistmentTwoConnections() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(ccm);
      assertNotNull(ut);

      ccm.pushContext(new Object(), unsharableResources);
      
      TxLogConnection c1 = cf.getConnection();
      assertNotNull(c1);

      String id1 = c1.getId();

      TxLogConnection c2 = cf.getConnection();
      assertNotNull(c2);

      String id2 = c2.getId();

      assertNotEquals(id1, id2);

      ut.begin();
      
      assertEquals(c1.getId(), c2.getId());
      
      c1.close();
      c2.close();

      assertFalse(c1.isInPool());
      assertFalse(c2.isInPool());

      ut.commit();

      assertTrue(c1.isInPool());
      assertTrue(c2.isInPool());

      c1 = cf.getConnection();

      assertEquals("3B8", c1.getState(id1));
      c1.clearState(id1);
      assertEquals("", c1.getState(id2));
      c1.clearState(id2);
      
      c1.close();
      
      ccm.popContext(unsharableResources);
   }

   /**
    * Auto close - without transaction
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testAutoCloseWithoutTransaction() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(ccm);

      ccm.setDebug(true);
      
      ccm.pushContext(new Object(), unsharableResources);
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      assertEquals(1, ccm.getNumberOfConnections());
      assertEquals(1, ccm.listConnections().size());
      
      assertFalse(c.isInPool());

      ccm.popContext(unsharableResources);

      assertEquals(0, ccm.getNumberOfConnections());
      assertEquals(0, ccm.listConnections().size());

      assertTrue(c.isInPool());
      
      ccm.setDebug(false);
   }

   /**
    * Auto close - with transaction (pop before commit)
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testAutoCloseWithTransactionPopBefore() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(ut);
      assertNotNull(ccm);

      ccm.setDebug(true);
      
      ccm.pushContext(new Object(), unsharableResources);
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      String id = c.getId();
      
      assertEquals(1, ccm.getNumberOfConnections());
      assertEquals(1, ccm.listConnections().size());

      ut.begin();
      
      assertFalse(c.isInPool());
      assertEquals(1, ccm.getNumberOfConnections());
      assertEquals(1, ccm.listConnections().size());

      ccm.popContext(unsharableResources);

      assertFalse(c.isInPool());
      assertEquals(1, ccm.getNumberOfConnections());
      assertEquals(1, ccm.listConnections().size());
      
      ut.commit();
      
      assertTrue(c.isInPool());
      assertEquals(0, ccm.getNumberOfConnections());
      assertEquals(0, ccm.listConnections().size());

      c = cf.getConnection();
      assertEquals("3B8", c.getState(id));
      c.clearState(id);
      c.close();

      ccm.setDebug(false);
   }


   /**
    * Auto close - with transaction (pop after commit)
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testAutoCloseWithTransactionPopAfter() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(ut);
      assertNotNull(ccm);

      ccm.setDebug(true);
      
      ccm.pushContext(new Object(), unsharableResources);
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      String id = c.getId();
      
      assertEquals(1, ccm.getNumberOfConnections());
      assertEquals(1, ccm.listConnections().size());

      ut.begin();
      
      assertFalse(c.isInPool());
      assertEquals(1, ccm.getNumberOfConnections());
      assertEquals(1, ccm.listConnections().size());

      ut.commit();
      
      assertFalse(c.isInPool());
      assertEquals(1, ccm.getNumberOfConnections());
      assertEquals(1, ccm.listConnections().size());
      
      ccm.popContext(unsharableResources);

      assertTrue(c.isInPool());
      assertEquals(0, ccm.getNumberOfConnections());
      assertEquals(0, ccm.listConnections().size());

      c = cf.getConnection();
      assertEquals("3B8", c.getState(id));
      c.clearState(id);
      c.close();

      ccm.setDebug(false);
   }
}
