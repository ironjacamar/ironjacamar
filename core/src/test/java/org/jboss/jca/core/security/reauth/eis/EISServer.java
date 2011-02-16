/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.security.reauth.eis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;

/**
 * The reauthentication EIS server
 * @author <a href="mailto:jesper.pedersen@comcast.net">Jesper Pedersen</a>
 */
public class EISServer implements Runnable
{
   /** The logger */
   private Logger log = Logger.getLogger(EISServer.class);

   /** The bind address */
   private String bindAddress;

   /** The bind port */
   private int bindPort;

   /** Maximum number of connections */
   private int maxConnections;

   /** Is the server running ? */
   private AtomicBoolean running;

   /** Shutdown */
   private CountDownLatch shutdown;

   /** Current number of valid connections */
   private AtomicInteger validConnections;

   /** The server socket */
   private ServerSocket ss;

   /** The thread pool */
   private ThreadPoolExecutor tpe;

   /**
    * Constructor
    * @param bindAddress The bind address
    * @param bindPort The bind port
    * @param maxConnections The maximum number of connections
    * @exception IOException Thrown if a server socket can not be created
    */
   public EISServer(String bindAddress, int bindPort, int maxConnections) throws IOException
   {
      if (bindAddress == null)
         bindAddress = "localhost";

      this.bindAddress = bindAddress;
      this.bindPort = bindPort;
      this.maxConnections = maxConnections;
      this.running = new AtomicBoolean(false);
      this.shutdown = null;
      this.validConnections = new AtomicInteger(0);
      this.ss = null;
      this.tpe = null;
   }

   /**
    * Start
    * @exception Throwable Thrown if an error occurs
    */
   public void start() throws Throwable
   {
      if (!running.get())
      {
         shutdown = new CountDownLatch(1);

         InetSocketAddress address = new InetSocketAddress(bindAddress, bindPort); 

         ss = new ServerSocket();
         ss.bind(address, maxConnections);

         BlockingQueue<Runnable> threadPoolQueue = new SynchronousQueue<Runnable>(true);
         
         tpe = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE,
                                      60, TimeUnit.SECONDS,
                                      threadPoolQueue);
         
         tpe.allowCoreThreadTimeOut(true);
         tpe.prestartAllCoreThreads();

         running.set(true);

         log.infof("EISServer started");
      }
   }

   /**
    * Stop
    * @exception Throwable Thrown if an error occurs
    */
   public void stop() throws Throwable
   {
      running.set(false);

      if (tpe != null)
         tpe.shutdown();

      if (ss != null)
      {
         try
         {
            ss.close();
         }
         catch (IOException ioe)
         {
            log.debugf(ioe.getMessage(), ioe);
         }
      }

      shutdown.await();

      log.infof("EISServer stopped");
   }

   /**
    * Run
    */
   public void run()
   {
      while (running.get())
      {
         Socket socket = null;
         try
         {
            log.debugf("Waiting for connection");

            socket = ss.accept();
            socket.setSoTimeout(300000);

            log.debugf("Accepted connection: %s", socket);

            boolean granted = validConnections.incrementAndGet() <= maxConnections;

            log.debugf("Granted: %s", granted);

            Runnable r = new Interaction(socket, granted, validConnections, maxConnections);
            tpe.submit(r);
         }
         catch (IOException ioe)
         {
            log.debugf(ioe.getMessage(), ioe);
         }
         catch (RejectedExecutionException ree)
         {
            log.debugf("Rejecting connection: %s", socket);

            validConnections.decrementAndGet();

            if (socket != null)
            {
               try
               {
                  socket.shutdownInput();
                  socket.shutdownOutput();

                  socket.close();
               }
               catch (IOException ioe)
               {
                  log.debugf(ioe.getMessage(), ioe);
               }
            }
         }
      }
      
      shutdown.countDown();
   }
}
