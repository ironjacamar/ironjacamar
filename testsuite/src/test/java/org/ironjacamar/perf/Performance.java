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
package org.ironjacamar.perf;

import org.ironjacamar.embedded.Embedded;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.perf.PerfConnection;
import org.ironjacamar.rars.perf.PerfConnectionFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic performance tests
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class Performance
{
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

   /** Embedded */
   protected static Embedded embedded = null;

   /** Data */
   private static SortedMap<String, SortedMap<Integer, Integer>> data =
      new TreeMap<String, SortedMap<Integer, Integer>>();

   private ResourceAdapterArchive resourceAdapter;
   private ResourceAdaptersDescriptor resourceAdapterActivation;
   private static ExecutorService es;

   /**
    * Create .rar
    * @return The resource adapter archive
    */
   public ResourceAdapterArchive createRar()
   {
      return ResourceAdapterFactory.createPerfRar();
   }

   /**
    * Create deployment
    * @param tsl The transaction support
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   private ResourceAdaptersDescriptor createDeployment(TransactionSupportLevel tsl, int poolSize)
   {
      return ResourceAdapterFactory.createPerfDeployment(tsl, USE_CCM, TX_BEGIN_DURATION, TX_COMMIT_DURATION, poolSize);
   }

   /**
    * Create an XATransaction deployment
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createXATxDeployment(int poolSize)
   {
      return createDeployment(TransactionSupportLevel.XATransaction, poolSize);
   }

   /**
    * Create a LocalTransaction deployment
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createLocalTxDeployment(int poolSize)
   {
      return createDeployment(TransactionSupportLevel.LocalTransaction, poolSize);
   }

   /**
    * Create a NoTransaction deployment
    * @param poolSize The pool size
    * @return The resource adapter descriptor
    */
   public ResourceAdaptersDescriptor createNoTxDeployment(int poolSize)
   {
      return createDeployment(TransactionSupportLevel.NoTransaction, poolSize);
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
         t.printStackTrace(System.err);
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
      try
      {
         embedded.undeploy(resourceAdapter);
         embedded.undeploy(resourceAdapterActivation);
      }
      catch (Throwable t)
      {
         t.printStackTrace(System.err);
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
         t.printStackTrace(System.err);
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
            System.err.println("Thread: " + Thread.currentThread().getName() + ", " + t.getMessage());
            t.printStackTrace(System.err);

            if (pc != null)
            {
               pc.error();
               pc = null;
            }

            if (ut != null)
            {
               try
               {
                  int status = ut.getStatus();
                  if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK)
                  {
                     ut.rollback();
                  }
               }
               catch (Exception inner)
               {
                  System.err.println("Rollback: Thread: " + Thread.currentThread().getName());
                  inner.printStackTrace(System.err);
               }
            
            }
         }
         finally
         {
            done.countDown();
         }
         return Integer.valueOf(success);
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
            System.out.println("PERF-DATA: " + entry.getKey() + "," + result.getKey() + "," + result.getValue());
         }
      }
   }

   /**
    * Print settings
    */
   static void printSettings()
   {
      System.out.println("Clients: " + Arrays.toString(CLIENTS));
      System.out.println("Pool sizes: " + Arrays.toString(POOL_SIZES));
      System.out.println("Threads: " + CLIENTS[CLIENTS.length - 1]);
      System.out.println("RampUp: " + DO_RAMP_UP);
      System.out.println("RampUp iterations: " + RAMP_UP_ITERATIONS);
      System.out.println("Transactions: " + TRANSACTIONS_PER_CLIENT);
      System.out.println("Statistics: " + STATISTICS);
      System.out.println("Use TX for NoTransaction: " + USE_TRANSACTION_FOR_NOTRANSACTION);
      System.out.println("Transaction begin duration: " + TX_BEGIN_DURATION);
      System.out.println("Transaction commit duration: " + TX_COMMIT_DURATION);
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
