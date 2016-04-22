/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *  
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *  
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *  
 *  You should have received a copy of the Eclipse Public License 
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.core.workmanager.dwm;

import org.ironjacamar.core.api.workmanager.DistributedWorkManager;
import org.ironjacamar.embedded.Embedded;
import org.ironjacamar.embedded.EmbeddedFactory;
import org.ironjacamar.rars.wm.WorkConnection;
import org.ironjacamar.rars.wm.WorkConnectionFactory;
import org.ironjacamar.rars.wm.WorkResourceAdapter;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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

   private static final int INSTANCES = 2;

   /** Embedded */
   protected static List<Embedded> embeddedList = new ArrayList<>(INSTANCES);

   /**
    * Lifecycle start, before the suite is executed
    *
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      System.setProperty("java.net.preferIPv4Stack", "true");
      System.setProperty("jgroups.bind_addr", "127.0.0.1");
      for (int i = 0; i < INSTANCES; i++)
      {

         if (i == 0)
         {
            //instance 0 uses default embedded config
            Embedded embedded = EmbeddedFactory.create(true);

            // Startup
            embedded.startup();
            embeddedList.add(embedded);

         }
         else
         {
            //other instances use local xml config files
            // Deploy
            Embedded embedded = EmbeddedFactory.create(false);

            // Startup
            embedded.startup();

            URL naming = AbstractDistributedWorkManagerTest.class.getClassLoader()
                  .getResource("dwm/instance-" + i + "/naming.xml");
            URL transaction = AbstractDistributedWorkManagerTest.class.getClassLoader()
                  .getResource("dwm/instance-" + i + "/transaction.xml");
            URL jca = AbstractDistributedWorkManagerTest.class.getClassLoader()
                  .getResource("dwm/instance-" + i + "/jca.xml");

            embedded.deploy(naming);
            embedded.deploy(transaction);
            embedded.deploy(jca);
            embeddedList.add(embedded);
         }


      }
      //beforeRun();
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      try
      {
         //afterRun();

         int i = 0;
         for (Embedded embedded : embeddedList)
         {
            if (i == 0)
            {
               embedded.shutdown();
            }
            else
            {
               // Undeploy
               URL naming = AbstractDistributedWorkManagerTest.class.getClassLoader()
                     .getResource("dwm/instance-" + i + "/naming.xml");
               URL transaction = AbstractDistributedWorkManagerTest.class.getClassLoader()
                     .getResource("dwm/instance-" + i + "/transaction.xml");
               URL jca = AbstractDistributedWorkManagerTest.class.getClassLoader()
                     .getResource("dwm/instance-" + i + "/jca.xml");

               embedded.undeploy(jca);
               embedded.undeploy(transaction);
               embedded.undeploy(naming);
               // Shutdown embedded
               embedded.shutdown();

            }

            i++;
         }
         embeddedList.clear();
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }


   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test that the used distributed work managers are configured
    * @param dwm the Distributed Work Manager
    * @throws Throwable throwable exception
    */
   public void verifyConfig(DistributedWorkManager dwm) throws Throwable
   {
      assertNotNull(dwm);
      assertNotNull(dwm.getPolicy());
      assertNotNull(dwm.getSelector());
      assertNotNull(dwm.getTransport());

   }

   /**
    * Test that a work instance can be executed
    * @throws Throwable throwable exception
    */
   @Test
   public void testExecuted() throws Throwable
   {
      Context context = new InitialContext();

      WorkConnectionFactory wcf = (WorkConnectionFactory) context.lookup("java:/eis/WorkConnectionFactory");

      assertNotNull(wcf);


      WorkConnection wc = wcf.getConnection();
      try
      {
         assertNotNull(wc.getWorkManager());
         assertTrue(wc.getWorkManager() instanceof javax.resource.spi.work.DistributableWorkManager);

         DistributedWorkManager dwm = (DistributedWorkManager)wc.getWorkManager();
         verifyConfig(dwm);
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
      Context context = new InitialContext();

      WorkConnectionFactory wcf = (WorkConnectionFactory) context.lookup("java:/eis/WorkConnectionFactory");

      assertNotNull(wcf);

      WorkConnection wc = wcf.getConnection();
      DistributedWorkManager dwm = null;
      try
      {
         assertNotNull(wc.getWorkManager());
         assertTrue(wc.getWorkManager() instanceof javax.resource.spi.work.DistributableWorkManager);

         dwm = (DistributedWorkManager)wc.getWorkManager();
         verifyConfig(dwm);
         dwm.getStatistics().clear();
         dwm.getDistributedStatistics().clear();

         wc.doWork(new MyDistributableFailureWork());

         fail("Expected WorkException");
      }
      catch (WorkException we)
      {
         assertTrue(we instanceof WorkCompletedException);

         WorkException wce = (WorkException)we;

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
