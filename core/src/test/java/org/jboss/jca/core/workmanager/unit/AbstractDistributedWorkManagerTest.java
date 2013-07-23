/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.workmanager.unit;

import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.workmanager.rars.dwm.WorkConnection;
import org.jboss.jca.core.workmanager.rars.dwm.WorkConnectionFactory;
import org.jboss.jca.core.workmanager.rars.dwm.WorkResourceAdapter;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * An abstract distributed workmanager test
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractDistributedWorkManagerTest
{
   private static Logger log = Logger.getLogger(AbstractDistributedWorkManagerTest.class);

   @Resource(mappedName = "java:/eis/WorkConnectionFactory")
   private WorkConnectionFactory wcf;

   /**
    * Get the distributed work manager
    * @return The value
    */
   protected abstract DistributedWorkManager getDistributedWorkManager();

   /**
    * Get the bootstrap context
    * @return The value
    */
   protected abstract BootstrapContext getBootstrapContext();

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test that the used distributed work managers are configured
    * @throws Throwable throwable exception
    */
   @Test
   public void testConfigured() throws Throwable
   {
      assertNotNull(getDistributedWorkManager());
      assertNotNull(getDistributedWorkManager().getPolicy());
      assertNotNull(getDistributedWorkManager().getSelector());
      assertNotNull(getDistributedWorkManager().getTransport());

      assertNotNull(getBootstrapContext());
   }

   /**
    * Test that a work instance can be executed
    * @throws Throwable throwable exception
    */
   @Test
   public void testExecuted() throws Throwable
   {
      log.infof("DWM: %s", getDistributedWorkManager());

      assertNotNull(wcf);

      WorkConnection wc = wcf.getConnection();
      try
      {
         assertNotNull(wc.getWorkManager());
         assertTrue(wc.getWorkManager() instanceof javax.resource.spi.work.DistributableWorkManager);

         DistributedWorkManager dwm = (DistributedWorkManager)wc.getWorkManager();

         dwm.getStatistics().clear();
         dwm.getDistributedStatistics().clear();

         wc.doWork(new MyWork());
         wc.doWork(new MyDistributableWork());

         assertNotNull(dwm.getStatistics());
         assertNotNull(dwm.getDistributedStatistics());

         log.infof("Statistics: %s", dwm.getStatistics());
         log.infof("DistributedStatistics: %s", dwm.getDistributedStatistics());

         assertEquals(1, dwm.getStatistics().getWorkSuccessful());
         assertEquals(2, dwm.getDistributedStatistics().getWorkSuccessful());

         dwm.getStatistics().clear();
         assertEquals(0, dwm.getStatistics().getWorkSuccessful());

         dwm.getDistributedStatistics().clear();
         assertEquals(0, dwm.getDistributedStatistics().getWorkSuccessful());
      }
      finally
      {
         wc.close();
      }
   }

   /**
    * Test that a work instance can report a failure
    * @throws Throwable throwable exception
    */
   @Test
   public void testFailure() throws Throwable
   {
      log.infof("DWM: %s", getDistributedWorkManager());

      assertNotNull(wcf);

      WorkConnection wc = wcf.getConnection();
      DistributedWorkManager dwm = null;
      try
      {
         assertNotNull(wc.getWorkManager());
         assertTrue(wc.getWorkManager() instanceof javax.resource.spi.work.DistributableWorkManager);

         dwm = (DistributedWorkManager)wc.getWorkManager();

         dwm.getStatistics().clear();
         dwm.getDistributedStatistics().clear();

         wc.doWork(new MyDistributableFailureWork());

         fail("Expected WorkException");
      }
      catch (WorkException we)
      {
         assertTrue(we instanceof WorkCompletedException);

         WorkCompletedException wce = (WorkCompletedException)we;

         assertNotNull(wce.getCause());

         assertTrue(wce.getCause() instanceof RuntimeException);
         
         RuntimeException re = (RuntimeException)wce.getCause();

         assertEquals("FAILURE", re.getMessage());
      }
      finally
      {
         assertNotNull(dwm);
         assertNotNull(dwm.getDistributedStatistics());
         assertEquals(1, dwm.getDistributedStatistics().getWorkFailed());

         wc.close();
      }
   }

   /**
    * Test that the used distributed work managers are an instance of the
    * <code>javax.resource.spi.work.DistributableWorkManager</code> interface
    * @throws Throwable throwable exception
    */
   @Test
   public void testInstanceOf() throws Throwable
   {
      log.infof("DWM: %s", getDistributedWorkManager());

      assertNotNull(getDistributedWorkManager());
      assertTrue(getDistributedWorkManager() instanceof javax.resource.spi.work.DistributableWorkManager);
   }

   // --------------------------------------------------------------------------------||
   // Helper classes -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   /**
    * Work
    */
   public static class MyWork implements Work
   {
      /**
       * {@inheritDoc}
       */
      public void run()
      {
         System.out.println("MyWork: run");
      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
         System.out.println("MyWork: release");
      }
   }

   /**
    * DistributableWork
    */
   public static class MyDistributableWork extends CustomClass implements DistributableWork,
                                                                          CustomInterface,
                                                                          ResourceAdapterAssociation
   {
      /** Serial version uid */
      private static final long serialVersionUID = 1L;

      /** Resource adapter */
      private ResourceAdapter ra;

      /** Custom parameter */
      private CustomParameter parameter;

      /**
       * Constructor
       */
      public MyDistributableWork()
      {
         this.parameter = new CustomParameter();
      }

      /**
       * {@inheritDoc}
       */
      public void method()
      {
         System.out.println("MyDistributableWork: method");
      }

      /**
       * {@inheritDoc}
       */
      public void run()
      {
         System.out.println("MyDistributableWork: run");
         method();
         System.out.println("MyDistributableWork: ra=" + ra);

         if (ra != null)
            System.out.println("MyDistributableWork: ra=" + (WorkResourceAdapter)ra);
      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
         System.out.println("MyDistributableWork: release");
      }

      /**
       * {@inheritDoc}
       */
      public ResourceAdapter getResourceAdapter()
      {
         return ra;
      }

      /**
       * {@inheritDoc}
       */
      public void setResourceAdapter(ResourceAdapter ra)
      {
         this.ra = ra;
      }
   }

   /**
    * Custom class
    */
   public static class CustomClass
   {
      /**
       * Constructor
       */
      public CustomClass()
      {
      }
   }

   /**
    * Custom interface
    */
   public static interface CustomInterface
   {
      /**
       * Method
       */
      public void method();
   }

   /**
    * Custom parameter
    */
   public static class CustomParameter implements Serializable
   {
      /** Serial version uid */
      private static final long serialVersionUID = 1L;

      /**
       * Constructor
       */
      public CustomParameter()
      {
      }
   }

   /**
    * DistributableWork: Failure
    */
   public static class MyDistributableFailureWork implements DistributableWork
   {
      /** Serial version uid */
      private static final long serialVersionUID = 1L;

      /**
       * Constructor
       */
      public MyDistributableFailureWork()
      {
      }

      /**
       * {@inheritDoc}
       */
      public void run()
      {
         System.out.println("MyDistributableFailureWork: run");
         throw new RuntimeException("FAILURE");
      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
         System.out.println("MyDistributableFailureWork: release");
      }
   }
}
