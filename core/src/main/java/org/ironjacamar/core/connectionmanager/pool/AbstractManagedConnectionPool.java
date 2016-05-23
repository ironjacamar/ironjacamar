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

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.tracer.Tracer;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.VALIDATION;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.ZOMBIE;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;

/**
 * The base class for all ManagedConnectionPool implementations
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractManagedConnectionPool implements ManagedConnectionPool
{
   /** The logger */
   protected CoreLogger log;

   /** The pool */
   protected Pool pool;

   /** The credential */
   protected Credential credential;

   /** pool is fifo*/
   protected final boolean poolIsFifo;

   /** last idle check */
   protected long lastIdleCheck;

   /**
    * Constructor
    * @param pool The pool
    * @param credential The credential
    */
   public AbstractManagedConnectionPool(Pool pool, Credential credential)
   {
      this.log = pool.getLogger();
      this.pool = pool;
      this.credential = credential;
      this.poolIsFifo = pool.isFIFO() && credential.equals(pool.getPrefillCredential());
      this.lastIdleCheck = System.currentTimeMillis();
   }

   /**
    * {@inheritDoc}
    */
   public Pool getPool()
   {
      return pool;
   }
   
   /**
    * Validate a connection listener
    * @param listeners The listeners
    * @param cl The connection listener
    * @param newState The new state
    * @return The validated connection listener, or <code>null</code> if validation failed
    */
   protected ConnectionListener validateConnectionListener(Collection<ConnectionListener> listeners,
                                                           ConnectionListener cl,
                                                           int newState)
   {
      ManagedConnectionFactory mcf = pool.getConnectionManager().getManagedConnectionFactory();

      if (mcf instanceof ValidatingManagedConnectionFactory)
      {
         ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory)mcf;
         try
         {
            Set candidateSet = Collections.singleton(cl.getManagedConnection());
            candidateSet = vcf.getInvalidConnections(candidateSet);

            if (candidateSet != null && !candidateSet.isEmpty())
            {
               if (Tracer.isEnabled())
                  Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl, false, false, true, false,
                        false, false, false, Tracer.isRecordCallstacks() ? new Throwable("CALLSTACK") : null);

               destroyAndRemoveConnectionListener(cl, listeners);
            }
            else
            {
               cl.validated();
               if (cl.changeState(VALIDATION, newState))
               {
                  return cl;
               }
               else
               {
                  if (Tracer.isEnabled())
                     Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl, false, false, true,
                           false, false, false, false, Tracer.isRecordCallstacks() ? new Throwable("CALLSTACK") : null);

                  destroyAndRemoveConnectionListener(cl, listeners);
               }
            }
         }
         catch (ResourceException re)
         {
            if (Tracer.isEnabled())
               Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl, false, false, true, false,
                     false, false, false, Tracer.isRecordCallstacks() ? new Throwable("CALLSTACK") : null);

            destroyAndRemoveConnectionListener(cl, listeners);
         }
      }
      else
      {
         log.debug("mcf is not instance of ValidatingManagedConnectionFactory");
         if (cl.changeState(VALIDATION, newState))
         {
            return cl;
         }
         else
         {
            if (Tracer.isEnabled())
               Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl, false, false, true, false,
                     false, false, false, Tracer.isRecordCallstacks() ? new Throwable("CALLSTACK") : null);
            destroyAndRemoveConnectionListener(cl, listeners);
         }
      }

      return null;
   }

   /**
    * Destroy and remove a connection listener
    * @param cl The connection listener
    * @param listeners The listeners
    */
   protected void destroyAndRemoveConnectionListener(ConnectionListener cl, Collection<ConnectionListener> listeners)
   {
      try
      {
         pool.destroyConnectionListener(cl);
      }
      catch (ResourceException e)
      {
         // TODO:
         cl.setState(ZOMBIE);
      }
      finally
      {
         listeners.remove(cl);
      }
   }

   /**
    * Find a ConnectionListener instance
    * @param mc The associated ManagedConnection
    * @param c The connection (optional)
    * @param listeners The listeners
    * @return The ConnectionListener, or <code>null</code>
    */
   protected ConnectionListener findConnectionListener(ManagedConnection mc, Object c,
                                                       Collection<ConnectionListener> listeners)
   {
      for (ConnectionListener cl : listeners)
      {
         if (cl.getManagedConnection().equals(mc) && (c == null || cl.getConnections().contains(c)))
            return cl;
      }

      return null;
   }

   /**
    * Remove a free ConnectionListener instance
    * @param free True if FREE, false if IN_USE
    * @param listeners The listeners
    * @return The ConnectionListener, or <code>null</code>
    */
   protected ConnectionListener removeConnectionListener(boolean free, Collection<ConnectionListener> listeners)
   {
      if (free)
      {
         for (ConnectionListener cl : listeners)
         {
            if (cl.changeState(FREE, IN_USE))
               return cl;
         }
      }
      else
      {
         for (ConnectionListener cl : listeners)
         {
            if (cl.getState() == IN_USE)
               return cl;
         }
      }

      return null;
   }

   /**
    * Get count
    * @param type The type
    * @param listeners The listeners
    * @return The value
    */
   protected int getCount(int type, Collection<ConnectionListener> listeners)
   {
      int count = 0;

      for (ConnectionListener cl : listeners)
      {
         if (cl.getState() == type)
            count++;
      }

      return count;
   }
}
