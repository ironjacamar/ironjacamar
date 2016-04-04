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
package org.ironjacamar.core.connectionmanager.pool.dflt;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.spi.graceful.GracefulCallback;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.test.TestConnection;
import org.ironjacamar.rars.test.TestConnectionFactory;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Graceful shutdown test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class GracefulShutdownTestCase
{
   /** The test connection factory */
   @Resource(mappedName = "java:/eis/TestConnectionFactory")
   private TestConnectionFactory cf;

   /** The deployment repository */
   @Inject
   private DeploymentRepository dr;
   
   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createTestRar();
   }
   
   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTestDeployment(0, null, 0);
   }
   
   /**
    * Shutdown
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testShutdown() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      cm.shutdown();
      
      TestConnection c = null;
      try
      {
         c = cf.getConnection();
         fail("Got a connection");
      }
      catch (Exception e)
      {
         // Ok
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }

   /**
    * Prepare shutdown
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdown() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      TestConnection c = cf.getConnection();
      assertNotNull(c);
      
      cm.prepareShutdown();

      TestConnection c2 = null;
      try
      {
         c2 = cf.getConnection();
         fail("Got a connection");
      }
      catch (Exception e)
      {
         // Ok
      }
      finally
      {
         if (c2 != null)
            c2.close();
      }

      c.close();
   }

   /**
    * Prepare shutdown and cancel
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdownAndCancel() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      TestConnection c = cf.getConnection();
      assertNotNull(c);
      
      cm.prepareShutdown();

      TestConnection c2 = null;
      try
      {
         c2 = cf.getConnection();
         fail("Got a connection");
      }
      catch (Exception e)
      {
         // Ok
      }
      finally
      {
         if (c2 != null)
            c2.close();
      }

      assertTrue(cm.cancelShutdown());
      assertFalse(cm.cancelShutdown());

      c2 = cf.getConnection();
      c2.close();
      
      c.close();
   }

   /**
    * Prepare shutdown and cancel w/ callback
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdownAndCancelWithCallback() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      TestConnection c = cf.getConnection();
      assertNotNull(c);
      
      ShutdownCallback cb = new ShutdownCallback();

      cm.prepareShutdown(cb);

      TestConnection c2 = null;
      try
      {
         c2 = cf.getConnection();
         fail("Got a connection");
      }
      catch (Exception e)
      {
         // Ok
      }
      finally
      {
         if (c2 != null)
            c2.close();
      }

      assertTrue(cm.cancelShutdown());

      assertTrue(cb.wasCancelCalled());
      
      c.close();
   }

   /**
    * Prepare shutdown w/ seconds
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdownWithSeconds() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      cm.prepareShutdown(1);

      Thread.sleep(1500L);
      
      TestConnection c = null;
      try
      {
         c = cf.getConnection();
         fail("Got a connection");
      }
      catch (Exception e)
      {
         // Ok
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }

   /**
    * Prepare shutdown w/ seconds and cancel
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdownWithSecondsAndCancel() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      TestConnection c = cf.getConnection();
      assertNotNull(c);
      
      cm.prepareShutdown(1);

      TestConnection c2 = null;
      try
      {
         c2 = cf.getConnection();
         fail("Got a connection");
      }
      catch (Exception e)
      {
         // Ok
      }
      finally
      {
         if (c2 != null)
            c2.close();
      }

      assertTrue(cm.cancelShutdown());

      c2 = cf.getConnection();
      c2.close();
      
      c.close();
   }

   /**
    * Prepare shutdown and callback
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdownWithCallback() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      TestConnection c = cf.getConnection();
      assertNotNull(c);

      ShutdownCallback sc = new ShutdownCallback();
      
      cm.prepareShutdown(sc);

      c.close();

      cm.shutdown();

      assertTrue(sc.wasDoneCalled());
   }

   /**
    * Prepare shutdown w/ seconds and callback
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdownWithSecondsWithCallback() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      TestConnection c = cf.getConnection();
      assertNotNull(c);

      ShutdownCallback sc = new ShutdownCallback();
      
      cm.prepareShutdown(1, sc);

      Thread.sleep(1500L);
      
      c.close();

      assertTrue(sc.wasDoneCalled());
   }

   /**
    * Prepare shutdown w/ seconds and cancel w/ callback
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testPrepareShutdownWithSecondsAndCancelWithCallback() throws Throwable
   {
      assertNotNull(cf);
      assertNotNull(dr);

      assertEquals(1, dr.getDeployments().size());
      
      org.ironjacamar.core.api.deploymentrepository.Deployment d = dr.findByJndi("java:/eis/TestConnectionFactory");
      assertNotNull(d);

      org.ironjacamar.core.api.deploymentrepository.ConnectionFactory dcf =
         d.getConnectionFactories().iterator().next();
      assertNotNull(dcf);

      assertTrue(dcf.getConnectionManager() instanceof org.ironjacamar.core.connectionmanager.ConnectionManager);
      
      org.ironjacamar.core.connectionmanager.ConnectionManager cm =
         (org.ironjacamar.core.connectionmanager.ConnectionManager)dcf.getConnectionManager();
      assertNotNull(cm);

      TestConnection c = cf.getConnection();
      assertNotNull(c);

      ShutdownCallback sc = new ShutdownCallback();
      
      cm.prepareShutdown(1, sc);

      TestConnection c2 = null;
      try
      {
         c2 = cf.getConnection();
         fail("Got a connection");
      }
      catch (Exception e)
      {
         // Ok
      }
      finally
      {
         if (c2 != null)
            c2.close();
      }

      assertTrue(cm.cancelShutdown());

      c2 = cf.getConnection();
      c2.close();
      
      c.close();

      assertTrue(sc.wasCancelCalled());
   }


   /**
    * Callback
    */
   static class ShutdownCallback implements GracefulCallback
   {
      private boolean cancelCalled;
      private boolean doneCalled;

      /**
       * Constructor
       */
      ShutdownCallback()
      {
         this.cancelCalled = false;
         this.doneCalled = false;
      }

      /**
       * Was cancel called
       * @return The result
       */
      boolean wasCancelCalled()
      {
         return cancelCalled;
      }

      /**
       * Was done called
       * @return The result
       */
      boolean wasDoneCalled()
      {
         return doneCalled;
      }

      /**
       * {@inheritDoc}
       */
      public void cancel()
      {
         cancelCalled = true;
      }

      /**
       * {@inheritDoc}
       */
      public void done()
      {
         doneCalled = true;
      }
   }
}
