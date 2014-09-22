/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.perf;

import org.jboss.jca.core.tx.rars.perf.PerfConnection;
import org.jboss.jca.core.tx.rars.perf.PerfConnectionFactory;
import org.jboss.jca.core.tx.rars.perf.PerfConnectionFactoryImpl;
import org.jboss.jca.core.tx.rars.perf.PerfConnectionImpl;
import org.jboss.jca.core.tx.rars.perf.PerfManagedConnectionFactory;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdapterType;
import org.jboss.jca.embedded.dsl.resourceadapters12.api.ResourceAdaptersDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType;
import org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic performance tests
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Performance
{
   private static Logger log = Logger.getLogger(Performance.class);

   /** Settings */
   private static final int[] CLIENTS = {1, 10, 25, 50, 100, 150, 200, 250, 300};
   private static final int[] POOL_SIZES = {1, 10, 25, 50, 100, 150, 200, 250, 300};

   private static final boolean DO_RAMP_UP = true;
   private static final int RAMP_UP_ITERATIONS = 1;
   private static final int TRANSACTIONS_PER_CLIENT = 200;
   private static final boolean STATISTICS = false;
   private static final boolean RECORD_ENLISTMENT_TRACES = false;
   private static final boolean USE_TRANSACTION_FOR_NOTRANSACTION = true;
   private static final boolean USE_CCM = false;

   private static final long TX_BEGIN_DURATION = 0L;
   private static final long TX_COMMIT_DURATION = 0L;

   private static final String MCP_IMPL =
      org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreArrayListManagedConnectionPool.class.getName();

   /** Embedded */
   protected static Embedded embedded = null;

   /** Data */
   private static SortedMap<String, SortedMap<Integer, Integer>> data =
      new TreeMap<String, SortedMap<Integer, Integer>>();

   private static final String OBJECT_NAME =
      "iron.jacamar:deployment=perf-ra.xml,jndi=java!/eis/PerfConnectionFactory," +
      "type=ConnectionFactory,class=PerfConnectionFactoryImpl,subcategory=PoolStatistics";

   private ResourceAdapterArchive resourceAdapter;
   private ResourceAdaptersDescriptor resourceAdapterActivation;
   private static ExecutorService es;

   static
   {
      if (System.getProperty("ironjacamar.mcp") == null)
         System.setProperty("ironjacamar.mcp", MCP_IMPL);
      System.setProperty("ironjacamar.embedded.management", "true");

      if (!RECORD_ENLISTMENT_TRACES)
         System.setProperty("ironjacamar.disable_enlistment_trace", "true");
   }

   /**
    * Create .rar
    * @return The resource adapter archive
    */
   public ResourceAdapterArchive createRar()
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml")
         .version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter();
      OutboundResourceadapterType ort = rt.getOrCreateOutboundResourceadapter()
         .transactionSupport("XATransaction").reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt =
         ort.createConnectionDefinition()
            .managedconnectionfactoryClass(PerfManagedConnectionFactory.class.getName())
            .connectionfactoryInterface(PerfConnectionFactory.class.getName())
            .connectionfactoryImplClass(PerfConnectionFactoryImpl.class.getName())
            .connectionInterface(PerfConnection.class.getName())
            .connectionImplClass(PerfConnectionImpl.class.getName());

      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "perf.rar");
      
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "perf.jar");
      ja.addPackage(PerfConnection.class.getPackage());
      
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");

      return raa;
   }

   /**
    * Create deployment
    * @param tx The transaction support
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   private ResourceAdaptersDescriptor createDeployment(String tx, int poolSize)
   {
      ResourceAdaptersDescriptor dashRaXml = Descriptors.create(ResourceAdaptersDescriptor.class, "perf-ra.xml");

      ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("perf.rar");
      dashRaXmlRt.transactionSupport(tx);

      ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();
      org.jboss.jca.embedded.dsl.resourceadapters12.api.ConnectionDefinitionType dashRaXmlCdt =
         dashRaXmlCdst.createConnectionDefinition()
            .className(PerfManagedConnectionFactory.class.getName())
            .jndiName("java:/eis/PerfConnectionFactory").poolName("Perf")
            .useCcm(USE_CCM);

      dashRaXmlCdt.createConfigProperty().name("TxBeginDuration").text(Long.toString(TX_BEGIN_DURATION));
      dashRaXmlCdt.createConfigProperty().name("TxCommitDuration").text(Long.toString(TX_COMMIT_DURATION));

      if ("XATransaction".equals(tx))
      {
         org.jboss.jca.embedded.dsl.resourceadapters12.api.XaPoolType dashRaXmlPt = dashRaXmlCdt.getOrCreateXaPool()
            .minPoolSize(poolSize).initialPoolSize(poolSize).maxPoolSize(poolSize).prefill(Boolean.TRUE);

         dashRaXmlCdt.getOrCreateRecovery().noRecovery(Boolean.TRUE);
      }
      else
      {
         org.jboss.jca.embedded.dsl.resourceadapters12.api.PoolType dashRaXmlPt = dashRaXmlCdt.getOrCreatePool()
            .minPoolSize(poolSize).initialPoolSize(poolSize).maxPoolSize(poolSize).prefill(Boolean.TRUE);
      }

      return dashRaXml;
   }

   /**
    * Create an XATransaction deployment
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createXATxDeployment(int poolSize)
   {
      return createDeployment("XATransaction", poolSize);
   }

   /**
    * Create a LocalTransaction deployment
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createLocalTxDeployment(int poolSize)
   {
      return createDeployment("LocalTransaction", poolSize);
   }

   /**
    * Create a NoTransaction deployment
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createNoTxDeployment(int poolSize)
   {
      return createDeployment("NoTransaction", poolSize);
   }

   /**
    * Ramp up
    *
    * @param dashRaXml The deployment metadata
    * @param poolSize The pool size
    */
   public void rampUp(ResourceAdaptersDescriptor dashRaXml, int poolSize)
   {
      this.resourceAdapter = createRar();
      this.resourceAdapterActivation = dashRaXml;

      Context context = null;
      try
      {
         embedded.deploy(resourceAdapter);
         embedded.deploy(resourceAdapterActivation);

         toggleStatistics();

         if (DO_RAMP_UP)
         {
            int cycles = RAMP_UP_ITERATIONS * poolSize;

            context = new InitialContext();

            UserTransaction ut = (UserTransaction)context.lookup("java:/UserTransaction");
            assertNotNull(ut);

            PerfConnectionFactory cf = (PerfConnectionFactory)context.lookup("java:/eis/PerfConnectionFactory");
            assertNotNull(cf);

            CountDownLatch done = new CountDownLatch(cycles);

            List<Client> clientList = new ArrayList<Client>(cycles);
            for (int i = 0; i < cycles; i++)
            {
               clientList.add(new Client(cf, ut, done));
            }
            
            es.invokeAll(clientList);

            done.await();
         }
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         afterRun();
         beforeRun();
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Ramp down
    * @param type The type
    * @param clients The number of clients
    */
   public void rampDown(String type, int clients)
   {
      dumpStatistics(type, clients);

      try
      {
         embedded.undeploy(resourceAdapter);
         embedded.undeploy(resourceAdapterActivation);
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
      }
      finally
      {
         resourceAdapter = null;
         resourceAdapterActivation = null;
      }
   }

   /**
    * Base
    *
    * @param clients The number of clients
    * @param useTx Use transactions
    * @return The result
    * @throws Throwable Thrown in case of an error
    */
   public int testBase(int clients, boolean useTx) throws Throwable
   {
      int result = 0;
      Context context = null;

      try
      {
         context = new InitialContext();

         UserTransaction ut = (UserTransaction)context.lookup("java:/UserTransaction");
         assertNotNull(ut);

         PerfConnectionFactory cf = (PerfConnectionFactory)context.lookup("java:/eis/PerfConnectionFactory");
         assertNotNull(cf);

         CountDownLatch done = new CountDownLatch(clients);

         List<Client> clientList = new ArrayList<Client>(clients);
         for (int i = 0; i < clients; i++)
         {
            clientList.add(new Client(cf, useTx ? ut : null, done));
         }

         clearStatistics();

         long start = System.nanoTime();

         List<Future<Integer>> futures = es.invokeAll(clientList);

         done.await();

         long end = System.nanoTime();

         double millis = (end - start) / 1000000.0;

         if (millis <= 0.0)
            millis = 1.0;

         double seconds = millis / 1000.0;

         int totalTxs = 0;
         for (Future<Integer> f : futures)
         {
            totalTxs += f.get(1, TimeUnit.SECONDS).intValue();
         }

         double txPerSec = totalTxs / seconds;

         result = (int)Math.ceil(txPerSec);
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         afterRun();
         beforeRun();
         fail(t.getMessage());
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }

      return result;
   }

   /**
    * Client
    */
   static class Client implements Callable<Integer>
   {
      private PerfConnectionFactory pcf;
      private UserTransaction ut;
      private CountDownLatch done;

      /**
       * Constructor
       * @param pcf The perf connection factory
       * @param ut The user transaction
       * @param done Done counter
       */
      Client(PerfConnectionFactory pcf, UserTransaction ut, CountDownLatch done)
      {
         this.pcf = pcf;
         this.ut = ut;
         this.done = done;
      }

      /**
       * {@inheritDoc}
       */
      public Integer call()
      {
         int success = 0;
         PerfConnection pc = null;

         try
         {
            for (int i = 0; i < TRANSACTIONS_PER_CLIENT; i++)
            {
               if (ut != null)
                  ut.begin();

               pc = pcf.getConnection();
               pc.close();
               pc = null;

               if (ut != null)
                  ut.commit();

               success++;
            }
         }
         catch (Throwable t)
         {
            log.fatal("Thread: " + Thread.currentThread().getName() + ", " + t.getMessage(), t);

            if (pc != null)
            {
               pc.error();
               pc = null;
            }

            if (ut != null)
            {
               int status = ut.getStatus();
               if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK)
               {
                  try
                  {
                     ut.rollback();
                  }
                  catch (Exception inner)
                  {
                     log.error("Rollback: Thread: " + Thread.currentThread().getName(), inner);
                  }
               }
            }
         }
         finally
         {
            done.countDown();
            return Integer.valueOf(success);
         }
      }
   }

   /**
    * Toggle statistics
    */
   private void toggleStatistics()
   {
      try
      {
         ObjectName on = new ObjectName(OBJECT_NAME);
         MBeanServer mbeanServer = null;
         ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

         boolean found = false;
         Iterator<MBeanServer> it = servers.iterator();
         while (!found && it.hasNext())
         {
            MBeanServer ms = it.next();
            if (ms.isRegistered(on))
            {
               mbeanServer = ms;
               found = true;
            }
         }

         if (mbeanServer != null)
         {
            Attribute attribute = new Attribute("Enabled", STATISTICS);
            mbeanServer.setAttribute(on, attribute);
         }
         else
         {
            log.fatal(on + " not found");
         }
      }
      catch (Exception e)
      {
         log.fatal("toggleStatistics", e);
      }
   }

   /**
    * Clear statistics
    */
   private void clearStatistics()
   {
      try
      {
         ObjectName on = new ObjectName(OBJECT_NAME);
         MBeanServer mbeanServer = null;
         ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

         boolean found = false;
         Iterator<MBeanServer> it = servers.iterator();
         while (!found && it.hasNext())
         {
            MBeanServer ms = it.next();
            if (ms.isRegistered(on))
            {
               mbeanServer = ms;
               found = true;
            }
         }

         if (mbeanServer != null)
         {
            mbeanServer.invoke(on, "clear", new Object[] {}, new String[] {});
         }
         else
         {
            log.fatal(on + " not found");
         }
      }
      catch (Exception e)
      {
         log.fatal("clearStatistics", e);
      }
   }

   /**
    * Dump statistics
    * @param type The type
    * @param clients The number of clients
    */
   private void dumpStatistics(String type, int clients)
   {
      if (!STATISTICS)
         return;

      try
      {
         ObjectName on = new ObjectName(OBJECT_NAME);
         MBeanServer mbeanServer = null;
         ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

         boolean found = false;
         Iterator<MBeanServer> it = servers.iterator();
         while (!found && it.hasNext())
         {
            MBeanServer ms = it.next();
            if (ms.isRegistered(on))
            {
               mbeanServer = ms;
               found = true;
            }
         }

         if (mbeanServer != null)
         {
            Set<String> names = (Set<String>)mbeanServer.getAttribute(on, "Names");
            if (names != null)
            {
               for (String name : names)
               {
                  Object value = mbeanServer.getAttribute(on, name);
                  log.error("PERF-STAT: " + type + "," + clients + "," + name + "," + value);
               }
            }
         }
         else
         {
            log.fatal(on + " not found");
         }
      }
      catch (Exception e)
      {
         log.fatal("dumpStatistics", e);
      }
   }

   /**
    * No
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test(timeout = 180000)
   public void testNo() throws Throwable
   {
      for (int i = 0; i < CLIENTS.length; i++)
      {
         rampUp(createNoTxDeployment(POOL_SIZES[i]), POOL_SIZES[i]);
         insertResult("NoTransaction", CLIENTS[i], testBase(CLIENTS[i], USE_TRANSACTION_FOR_NOTRANSACTION));
         rampDown("NoTransaction", CLIENTS[i]);
      }
   }

   /**
    * Local
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test(timeout = 180000)
   public void testLocal() throws Throwable
   {
      for (int i = 0; i < CLIENTS.length; i++)
      {
         rampUp(createLocalTxDeployment(POOL_SIZES[i]), POOL_SIZES[i]);
         insertResult("LocalTransaction", CLIENTS[i], testBase(CLIENTS[i], true));
         rampDown("LocalTransaction", CLIENTS[i]);
      }
   }

   /**
    * XA
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test(timeout = 180000)
   public void testXA() throws Throwable
   {
      for (int i = 0; i < CLIENTS.length; i++)
      {
         rampUp(createXATxDeployment(POOL_SIZES[i]), POOL_SIZES[i]);
         insertResult("XATransaction", CLIENTS[i], testBase(CLIENTS[i], true));
         rampDown("XATransaction", CLIENTS[i]);
      }
   }

   /**
    * Insert result
    * @param type The type
    * @param clients The number of clients
    * @param result The result
    */
   private static void insertResult(String type, int clients, int result)
   {
      SortedMap<Integer, Integer> section = data.get(type);

      if (section == null)
         section = new TreeMap<Integer, Integer>();

      section.put(Integer.valueOf(clients), Integer.valueOf(result));

      data.put(type, section);
   }

   /**
    * Dump data
    */
   static void dumpData()
   {
      Iterator<Map.Entry<String, SortedMap<Integer, Integer>>> it = data.entrySet().iterator(); 
      while (it.hasNext())
      {
         Map.Entry<String, SortedMap<Integer, Integer>> entry = it.next();

         Iterator<Map.Entry<Integer, Integer>> entryIt = entry.getValue().entrySet().iterator();
         while (entryIt.hasNext())
         {
            Map.Entry<Integer, Integer> result = entryIt.next();
            log.fatal("PERF-DATA: " + entry.getKey() + "," + result.getKey() + "," + result.getValue());
         }
      }
   }

   /**
    * Print settings
    */
   static void printSettings()
   {
      log.errorf("MCP: %s", System.getProperty("ironjacamar.mcp"));
      log.errorf("Clients: %s", Arrays.toString(CLIENTS));
      log.errorf("Pool sizes: %s", Arrays.toString(POOL_SIZES));
      log.errorf("Threads: %s", CLIENTS[CLIENTS.length - 1]);
      log.errorf("RampUp: %s", DO_RAMP_UP);
      log.errorf("RampUp iterations: %s", RAMP_UP_ITERATIONS);
      log.errorf("Transactions: %s", TRANSACTIONS_PER_CLIENT);
      log.errorf("Statistics: %s", STATISTICS);
      log.errorf("Record enlistment: %s", RECORD_ENLISTMENT_TRACES);
      log.errorf("Use TX for NoTransaction: %s", USE_TRANSACTION_FOR_NOTRANSACTION);
      log.errorf("Transaction begin duration: %s", TX_BEGIN_DURATION);
      log.errorf("Transaction commit duration: %s", TX_COMMIT_DURATION);
   }

   /**
    * beforeRun
    */
   static void beforeRun()
   {
      es = Executors.newFixedThreadPool(CLIENTS[CLIENTS.length - 1], new PerformanceThreadFactory());
   }

   /**
    * afterRun
    */
   static void afterRun()
   {
      es.shutdown();
      es = null;
   }

   /**
    * Performance thread factory
    */
   static class PerformanceThreadFactory implements ThreadFactory
   {
      private AtomicInteger counter;

      /**
       * Constructor
       */
      PerformanceThreadFactory()
      {
         counter = new AtomicInteger(0);
      }

      /**
       * {@inheritDoc}
       */
      public Thread newThread(Runnable r)
      {
         StringBuilder sb = new StringBuilder().append("Performance client ").append(counter.incrementAndGet());
         return new Thread(r, sb.toString());
      }
   }
}
