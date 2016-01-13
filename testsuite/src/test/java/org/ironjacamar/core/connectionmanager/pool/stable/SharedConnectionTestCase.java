/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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
package org.ironjacamar.core.connectionmanager.pool.stable;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.BeansStablePool;
import org.ironjacamar.embedded.junit4.Initializer;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.txlog.TxLogConnection;
import org.ironjacamar.rars.txlog.TxLogConnectionFactory;
import org.ironjacamar.util.TestUtils;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROYED;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.UserTransaction;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Shared connection test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@Initializer(clazz = BeansStablePool.class)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class SharedConnectionTestCase
{
   /** The txlog connection factory */
   @Resource(mappedName = "java:/eis/TxLogConnectionFactory")
   private static TxLogConnectionFactory cf;

   /** The deployment repository */
   @Inject
   private static DeploymentRepository dr;
   
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
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.XATransaction);
   }
   
   /**
    * Enlist
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testEnlist() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);
      assertNotNull(ut);

      ut.begin();
      
      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TxLogConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      org.ironjacamar.core.api.deploymentrepository.Pool p = dcf.getPool();
      assertNotNull(p);

      assertTrue(p.getPool() instanceof StablePool);
      StablePool stablePool = (StablePool)p.getPool();

      ConcurrentHashMap<Credential, ManagedConnectionPool> mcps =
         (ConcurrentHashMap<Credential, ManagedConnectionPool>)TestUtils.extract(stablePool, "pools");
      assertNotNull(mcps);
      assertEquals(0, mcps.size());
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      assertEquals(1, mcps.size());

      ManagedConnectionPool mcp = mcps.values().iterator().next();
      assertNotNull(mcp);

      ConcurrentLinkedDeque<ConnectionListener> listeners =
         (ConcurrentLinkedDeque<ConnectionListener>)TestUtils.extract(mcp, "listeners");
      assertNotNull(listeners);
      assertEquals(1, listeners.size());

      ConnectionListener cl = listeners.getFirst();
      assertEquals(IN_USE, cl.getState());

      assertTrue(cl.isEnlisted());

      // We can enlist() in the same transaction
      cl.enlist();

      CountDownLatch cdl = new CountDownLatch(1);
      ConnectionListenerRunnable clr = new ConnectionListenerRunnable(cl, ut, cdl);
      new Thread(clr).start();

      cdl.await();

      // But not in another
      assertNotNull(clr.getException());
      
      assertEquals(DESTROY, cl.getState());

      c.close();
      ut.rollback();      

      assertEquals(DESTROYED, cl.getState());
      assertEquals(0, listeners.size());
      assertEquals(1, mcps.size());

      // We cheat and shutdown the pool to clear out mcps
      stablePool.shutdown();
   }

   /**
    * ConnectionListener runnable
    */
   static class ConnectionListenerRunnable implements Runnable
   {
      private ConnectionListener cl;
      private UserTransaction ut;
      private CountDownLatch cdl;
      private Exception exception;

      /**
       * Constructor
       * @param cl The ConnectionListener
       * @param ut The UserTransaction
       * @param cdl The CountDownLatch
       */
      ConnectionListenerRunnable(ConnectionListener cl, UserTransaction ut, CountDownLatch cdl)
      {
         this.cl = cl;
         this.ut = ut;
         this.cdl = cdl;
         this.exception = null;
      }

      /**
       * Get the exception
       * @return The value
       */
      Exception getException()
      {
         return exception;
      }

      /**
       * {@inheritDoc}
       */
      public void run()
      {
         try
         {
            ut.begin();
            cl.enlist();
         }
         catch (Exception e)
         {
            exception = e;
         }
         finally
         {
            try
            {
               ut.rollback();
            }
            catch (Exception ignore)
            {
               // Ignore
            }
            cdl.countDown();
         }
      }
   }
}
