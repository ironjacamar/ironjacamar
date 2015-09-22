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

package org.ironjacamar.core.connectionmanager.pool.dflt;

import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROYED;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;

import java.util.concurrent.ConcurrentLinkedDeque;

import javax.resource.ResourceException;

/**
 * The default ManagedConnectionPool
 */
public class DefaultManagedConnectionPool implements ManagedConnectionPool
{
   /** The associated pool */
   private DefaultPool pool;

   /** The credential */
   private Credential credential;
   
   /** The connection listeners */
   private ConcurrentLinkedDeque<ConnectionListener> listeners;
   
   /**
    * Constructor
    * @param pool The pool
    * @param credential The credential
    */
   public DefaultManagedConnectionPool(DefaultPool pool, Credential credential)
   {
      this.pool = pool;
      this.credential = credential;
      this.listeners = new ConcurrentLinkedDeque<ConnectionListener>();
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getConnectionListener() throws ResourceException
   {
      long timestamp = System.currentTimeMillis();
      while (System.currentTimeMillis() - timestamp <= pool.getConfiguration().getBlockingTimeout())
      {
         for (ConnectionListener cl : listeners)
         {
            if (cl.changeState(FREE, IN_USE))
               return cl;
         }

         if (!pool.isFull())
         {
            try
            {
               listeners.addFirst(pool.createConnectionListener(credential));
            }
            catch (ResourceException re)
            {
            }
         }

         Thread.yield();
      }

      throw new ResourceException("No ConnectionListener");
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnectionListener(ConnectionListener cl, boolean kill) throws ResourceException
   {
      if (!kill)
      {
         cl.changeState(IN_USE, FREE);
      }
      else
      {
         if (cl.changeState(IN_USE, DESTROY))
         {
            try
            {
               pool.destroyConnectionListener(cl);
            }
            finally
            {
               cl.changeState(DESTROY, DESTROYED);
               listeners.remove(cl);
            }
         }
      }
   }
}
