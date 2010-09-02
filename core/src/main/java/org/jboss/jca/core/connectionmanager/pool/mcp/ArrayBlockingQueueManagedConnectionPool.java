/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import org.jboss.jca.common.JBossResourceException;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.listener.ConnectionState;
import org.jboss.jca.core.connectionmanager.pool.SubPoolContext;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.idle.IdleRemover;
import org.jboss.jca.core.connectionmanager.pool.validator.ConnectionValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableUnavailableException;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * A managed connection pool implementation using ArrayBlockingQueue
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ArrayBlockingQueueManagedConnectionPool implements ManagedConnectionPool
{
   /** The log */
   private Logger log;

   /** Whether trace is enabled */
   private boolean trace;
   
   /** The managed connection factory */
   private ManagedConnectionFactory mcf;

   /** The connection listener factory */
   private ConnectionListenerFactory clf;

   /** The default subject */
   private Subject defaultSubject;

   /** The default connection request information */
   private ConnectionRequestInfo defaultCri;

   /** The pool configuration */
   private PoolConfiguration poolConfiguration;

   /** The pool */
   private Pool pool;

   /** 
    * Copy of the maximum size from the pooling parameters.
    * Dynamic changes to this value are not compatible with
    * the semaphore which cannot change be dynamically changed.
    */
   private int maxSize;

   /** The available connection event listeners */
   private ArrayBlockingQueue<ConnectionListener> cls;

   /** The permits used to control who can checkout a connection */
   private ConcurrentMap<ConnectionListener, ConnectionListener> permits;

   /** The subpool */
   private SubPoolContext subPool;

   /** The checked out connections */
   private final ConcurrentSkipListSet<ConnectionListener> checkedOut = 
      new ConcurrentSkipListSet<ConnectionListener>();

   /** Whether the pool has been started */
   private AtomicBoolean started = new AtomicBoolean(false);

   /** Whether the pool has been shutdown */
   private AtomicBoolean shutdown = new AtomicBoolean(false);

   /** the max connections ever checked out **/
   private AtomicInteger maxUsedConnections = new AtomicInteger(0);

   /**
    * Constructor
    */
   public ArrayBlockingQueueManagedConnectionPool()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void initialize(ManagedConnectionFactory mcf, ConnectionListenerFactory clf, Subject subject,
                          ConnectionRequestInfo cri, PoolConfiguration pc, Pool p, SubPoolContext spc,
                          Logger log)
   {
      this.mcf = mcf;
      this.clf = clf;
      this.defaultSubject = subject;
      this.defaultCri = cri;
      this.poolConfiguration = pc;
      this.maxSize = pc.getMaxSize();
      this.pool = p;
      this.subPool = spc;
      this.log = log;
      this.trace = log.isTraceEnabled();
      this.cls = new ArrayBlockingQueue<ConnectionListener>(this.maxSize, true);
      this.permits = new ConcurrentHashMap<ConnectionListener, ConnectionListener>(this.maxSize);
  
      if (pc.isPrefill())
      {
         PoolFiller.fillPool(this);
      }

      reenable();
   }

   /**
    * {@inheritDoc}
    */
   public SubPoolContext getSubPool()
   {
      return subPool;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isRunning()
   {
      return !shutdown.get();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isEmpty()
   {
      return cls.size() == 0 && checkedOut.size() == 0;
   }

   /**
    * {@inheritDoc}
    */
   public void reenable()
   {
      if (poolConfiguration.getIdleTimeout() != 0L)
      {
         //Register removal support
         IdleRemover.registerPool(this, poolConfiguration.getIdleTimeout());
      }
      
      if (poolConfiguration.getBackgroundValidationInterval() > 0)
      {
         log.debug("Registering for background validation at interval " + 
                   poolConfiguration.getBackgroundValidationInterval());
         
         //Register validation
         ConnectionValidator.registerPool(this, poolConfiguration.getBackgroundValidationInterval());
      }

      shutdown.set(false);
   }

   private synchronized long getAvailableConnections()
   {
      int result = maxSize - permits.size();

      return (result >= 0) ? result : 0;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      subject = (subject == null) ? defaultSubject : subject;
      cri = (cri == null) ? defaultCri : cri;

      ConnectionListener cl = null;
      boolean verifyConnectionListener = true;

      long startWait = System.currentTimeMillis();
      if (getAvailableConnections() > 0)
      {
         if (shutdown.get())
            throw new RetryableUnavailableException("The pool has been shutdown");
         
         cl = cls.peek();
         if (cl != null)
         {
            try
            {
               cl = cls.poll(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ie)
            {
               long end = System.currentTimeMillis() - startWait;
               throw new ResourceException("Interrupted while requesting connection! Waited " + end + " ms");
            }
         }
         else
         {
            try
            {
               // No, the pool was empty, so we have to make a new one.
               cl = createConnectionEventListener(subject, cri);

               // Started is atomic, so pool filler won't be scheduled twice
               if (!started.getAndSet(true))
               {
                  if (poolConfiguration.getMinSize() > 0)
                     PoolFiller.fillPool(this);
               }
               
               if (trace)
                  log.trace("supplying new ManagedConnection: " + cl);
               
               verifyConnectionListener = false;
            }
            catch (Throwable t)
            {
               log.warn("Throwable while attempting to get a new connection: " + cl, t);

               JBossResourceException.rethrowAsResourceException("Unexpected throwable while trying " + 
                                                                 "to create a connection: " + cl, t);
            }
         }
      }
      else
      {
         try
         {
            cl = cls.poll(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS);

            if (shutdown.get())
               throw new RetryableUnavailableException("The pool has been shutdown");
         }
         catch (InterruptedException ie)
         {
            if (!poolConfiguration.isUseFastFail())
            {
               throw new ResourceException("No ManagedConnections available within configured blocking timeout ( "
                                           + poolConfiguration.getBlockingTimeout() + " [ms] )");
            }
            else
            {
               if (trace)
                  log.trace("Fast failing for connection attempt. No more attempts will be made to " +
                            "acquire connection from pool and a new connection will be created immeadiately");

               try
               {
                  cl = createConnectionEventListener(subject, cri);
               
                  // Started is atomic, so pool filler won't be scheduled twice
                  if (!started.getAndSet(true))
                  {
                     if (poolConfiguration.getMinSize() > 0)
                        PoolFiller.fillPool(this);
                  }
               
                  if (trace)
                     log.trace("supplying new ManagedConnection: " + cl);
               
                  verifyConnectionListener = false;
               }
               catch (Throwable t)
               {
                  log.warn("Throwable while attempting to get a new connection: " + cl, t);

                  JBossResourceException.rethrowAsResourceException("Unexpected throwable while trying to " +
                                                                    "create a connection: " + cl, t);
               }
            }
         }
      }

      // Register the connection listener
      checkedOut.add(cl);

      // Update max used connections
      int size = maxSize - permits.size();
      if (size > maxUsedConnections.get())
         maxUsedConnections.set(size);
      
      if (!verifyConnectionListener)
      {
         // Register the connection listener with permits
         permits.put(cl, cl);

         // Return connection listener
         return cl;
      }
      else
      {
         try
         {
            Object matchedMC =
               mcf.matchManagedConnections(Collections.singleton(cl.getManagedConnection()), subject, cri);

            if (matchedMC != null)
            {
               if (trace)
                  log.trace("supplying ManagedConnection from pool: " + cl);

               // Register the connection listener with permits
               permits.put(cl, cl);

               // Return connection listener
               return cl;
            }

            // Match did not succeed but no exception was thrown.
            // Either we have the matching strategy wrong or the
            // connection died while being checked.  We need to
            // distinguish these cases, but for now we always
            // destroy the connection.
            log.warn("Destroying connection that could not be successfully matched: " + cl + " for: " + mcf);
            
            checkedOut.remove(cl);
            
            doDestroy(cl);
            cl = null;
         }
         catch (Throwable t)
         {
            log.warn("Throwable while trying to match ManagedConnection, destroying connection: " + cl, t);
         
            checkedOut.remove(cl);
            
            doDestroy(cl);
            cl = null;

            JBossResourceException.rethrowAsResourceException("Unexpected throwable while trying " +
                                                              "to create a connection: " + cl, t);
         }
      }

      throw new JBossResourceException("This should never happen", new Throwable("STACKTRACE"));
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnection(ConnectionListener cl, boolean kill)
   {
      if (cl.getState() == ConnectionState.DESTROYED)
      {
         if (trace)
            log.trace("ManagedConnection is being returned after it was destroyed" + cl);

         if (permits.containsKey(cl))
         {
            // release connection listener
            permits.remove(cl);
         }

         return;
      }

      if (trace)
         log.trace("putting ManagedConnection back into pool kill=" + kill + " cl=" + cl);

      try
      {
         cl.getManagedConnection().cleanup();
      }
      catch (ResourceException re)
      {
         log.warn("ResourceException cleaning up ManagedConnection: " + cl, re);
         kill = true;
      }

      // We need to destroy this one
      if (cl.getState() == ConnectionState.DESTROY || cl.getState() == ConnectionState.DESTROYED)
         kill = true;

      checkedOut.remove(cl);

      // This is really an error
      if (!kill && cls.size() >= poolConfiguration.getMaxSize())
      {
         log.warn("Destroying returned connection, maximum pool size exceeded " + cl);
         kill = true;
      }

      // If we are destroying, check the connection is not in the pool
      if (kill)
      {
         // Adrian Brock: A resource adapter can asynchronously notify us that
         // a connection error occurred.
         // This could happen while the connection is not checked out.
         // e.g. JMS can do this via an ExceptionListener on the connection.
         // I have twice had to reinstate this line of code, PLEASE DO NOT REMOVE IT!
         cls.remove(cl);
      }
      // return to the pool
      else
      {
         cl.used();
         if (!cls.contains(cl))
         {
            try
            {
               cls.put(cl);
            }
            catch (InterruptedException ie)
            {
               cl.setState(ConnectionState.DESTROY);
               kill = true;
            }
         }
         else
         {
            log.warn("Attempt to return connection twice (ignored): " + cl, new Throwable("STACKTRACE"));
         }
      }

      if (permits.containsKey(cl))
      {
         // release connection listener
         permits.remove(cl);
      }

      if (kill)
      {
         if (trace)
            log.trace("Destroying returned connection " + cl);

         doDestroy(cl);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush()
   {
      ArrayList<ConnectionListener> destroy = null;

      if (trace)
         log.trace("Flushing pool checkedOut=" + checkedOut + " inPool=" + cls);

      // Mark checked out connections as requiring destruction
      for (Iterator<ConnectionListener> i = checkedOut.iterator(); i.hasNext();)
      {
         ConnectionListener cl = i.next();

         if (trace)
            log.trace("Flush marking checked out connection for destruction " + cl);

         cl.setState(ConnectionState.DESTROY);
      }

      // Destroy connections in the pool
      ConnectionListener cl = cls.poll();
      while (cl != null)
      {
         if (destroy == null)
            destroy = new ArrayList<ConnectionListener>();

         destroy.add(cl);
         cl = cls.poll();
      }

      // We need to destroy some connections
      if (destroy != null)
      {
         for (int i = 0; i < destroy.size(); ++i)
         {
            ConnectionListener l = destroy.get(i);
            if (trace)
               log.trace("Destroying flushed connection " + l);

            doDestroy(l);
         }

         // We destroyed something, check the minimum.
         if (!shutdown.get() && poolConfiguration.getMinSize() > 0)
            PoolFiller.fillPool(this);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void removeIdleConnections()
   {
      ArrayList<ConnectionListener> destroy = null;
      long timeout = System.currentTimeMillis() - poolConfiguration.getIdleTimeout();
      
      boolean cont = true;
      while (cont)
      {
         // Check the first in the list
         ConnectionListener cl = cls.peek();
         if (cl != null && cl.isTimedOut(timeout) && shouldRemove())
         {
            // We need to destroy this one
            if (destroy == null)
               destroy = new ArrayList<ConnectionListener>(1);

            cl = cls.poll();

            if (cl != null)
            {
               destroy.add(cl);
            }
            else
            {
               // The connection list were empty
               cont = false;
            }
         }
         else
         {
            // They were inserted chronologically, so if this one 
            // isn't timed out, following ones won't be either.
            cont = false;
         }
      }

      // We found some connections to destroy
      if (destroy != null)
      {
         for (int i = 0; i < destroy.size(); ++i)
         {
            ConnectionListener cl = destroy.get(i);

            if (trace)
               log.trace("Destroying timedout connection " + cl);

            doDestroy(cl);
         }

         // We destroyed something, check the minimum.
         if (!shutdown.get() && poolConfiguration.getMinSize() > 0)
            PoolFiller.fillPool(this);

         // Empty sub-pool
         if (pool != null)
            pool.emptySubPool(this);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public void shutdown()
   {
      shutdown.set(true);
      IdleRemover.unregisterPool(this);
      ConnectionValidator.unregisterPool(this);
      flush();
   }

   /**
    * {@inheritDoc}
    */
   public void fillToMin()
   {
      while (poolConfiguration.getMinSize() - (cls.size() + checkedOut.size()) > 0)
      {
         if (shutdown.get())
            return;

         // Create a connection to fill the pool
         ConnectionListener cl = null;
         boolean destroy = false;
         try
         {
            cl = createConnectionEventListener(defaultSubject, defaultCri);
               
            if ((checkedOut.size() + cls.size()) < poolConfiguration.getMinSize())
            {
               if (trace)
                  log.trace("Filling pool cl=" + cl);

               if (!cls.offer(cl))
               {
                  log.debug("Connection couldn't be inserted during fillToMin");
                  destroy = true;
               }
            }
            else
            {
               log.debug("MinSize reached during fillToMin");
               destroy = true;
            }
         }
         catch (ResourceException re)
         {
            log.warn("Unable to fill pool ", re);
            destroy = true;
         }
         finally
         {
            if (destroy)
            {
               if (cl != null)
               {
                  doDestroy(cl);
               }

               break;
            }
         }
      }
   }

   /**
    * Create a connection event listener
    *
    * @param subject the subject
    * @param cri the connection request information
    * @return the new listener
    * @throws ResourceException for any error
    */
   private ConnectionListener createConnectionEventListener(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      ManagedConnection mc = mcf.createManagedConnection(subject, cri);
      try
      {
         return clf.createConnectionListener(mc, this);
      }
      catch (ResourceException re)
      {
         mc.destroy();
         throw re;
      }
   }

   /**
    * Destroy a connection
    *
    * @param cl the connection to destroy
    */
   private void doDestroy(ConnectionListener cl)
   {
      if (cl.getState() == ConnectionState.DESTROYED)
      {
         log.trace("ManagedConnection is already destroyed " + cl);
         return;
      }

      cl.setState(ConnectionState.DESTROYED);
      try
      {
         cl.getManagedConnection().destroy();
      }
      catch (Throwable t)
      {
         log.debug("Exception destroying ManagedConnection " + cl, t);
      }

   }
   
   private boolean shouldRemove()
   {      
      boolean remove = true;
      
      if (poolConfiguration.isStrictMin())
      {
         remove = cls.size() > poolConfiguration.getMinSize();
         
         if (trace)
            log.trace("StrictMin is active. Current connection will be removed is " + remove);
      }
      
      return remove;
   }
   
   /**
    * {@inheritDoc}
    */
   public void validateConnections() throws Exception
   {
      if (trace)
         log.trace("Attempting to  validate connections for pool " + this);

      boolean anyDestroyed = false;

      try
      {
         while (true)
         {
            ConnectionListener cl = null;
            boolean destroyed = false;

            if (cls.size() == 0)
            {
               break;
            }

            cl = removeForFrequencyCheck();

            if (cl == null)
            {
               break;
            }

            try
            {
               Set candidateSet = Collections.singleton(cl.getManagedConnection());

               if (mcf instanceof ValidatingManagedConnectionFactory)
               {
                  ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory) mcf;
                  candidateSet = vcf.getInvalidConnections(candidateSet);

                  if (candidateSet != null && candidateSet.size() > 0)
                  {
                     if (cl.getState() != ConnectionState.DESTROY)
                     {
                        doDestroy(cl);
                        destroyed = true;
                        anyDestroyed = true;
                     }
                  }
               }
               else
               {
                  log.warn("Warning: Background validation was specified with a non compliant " +
                           "ManagedConnectionFactory interface.");
               }
            }
            finally
            {
               if (!destroyed)
               {
                  if (!returnForFrequencyCheck(cl))
                     anyDestroyed = true;
               }
            }
         }
      }
      finally
      {
         if (anyDestroyed && !shutdown.get() && poolConfiguration.getMinSize() > 0)
         {
            PoolFiller.fillPool(this);
         }
      }
   }

   /**
    * Remove a connection to the pool for a frequency check
    * @return A connection; <code>null</code> if no connections needs to be checked
    */
   private ConnectionListener removeForFrequencyCheck()
   {
      log.debug("Checking for connection within frequency");

      ConnectionListener result = null;
      Iterator<ConnectionListener> iter = cls.iterator();

      while (result == null && iter.hasNext())
      {
         ConnectionListener cl = iter.next();
         long lastCheck = cl.getLastValidatedTime();

         if ((System.currentTimeMillis() - lastCheck) >= poolConfiguration.getBackgroundValidationInterval())
         {
            result = cl;
            cls.remove(cl);
         }
      }

      return result;
   }

   /**
    * Return a connection to the pool
    * @param cl The connection
    * @return <code>True</code> if the connection was returned; otherwise <code>false</code>
    */
   private boolean returnForFrequencyCheck(ConnectionListener cl)
   {
      log.debug("Returning for connection within frequency: " + cl);

      if (cl == null)
         return true;

      cl.setLastValidatedTime(System.currentTimeMillis());

      if (!cls.offer(cl))
      {
         log.debug("Connection couldn't be returned");
         doDestroy(cl);
         return false;
      }

      return true;
   }
}
