/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.rars.cm.pooling;

import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.util.collection.CollectionsFactory;

/**
 * PoolingConnectionManager.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class PoolingConnectionManager implements ConnectionManager
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -411325259312495917L;

   /** The log */
   private static final Logger log = Logger.getLogger(PoolingConnectionManager.class); 
   
   /** The pool */
   private Set pool = CollectionsFactory.createCopyOnWriteSet();  
   
   /** The checked out connectins */
   private Set checkedOut = CollectionsFactory.createCopyOnWriteSet();
   
   public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      Subject subject = null;
      ManagedConnection mc = getManagedConnection(mcf, subject, cxRequestInfo);
      checkedOut.add(mc);
      try
      {
         return mc.getConnection(subject, cxRequestInfo);
      }
      catch (Throwable t)
      {
         checkedOut.remove(mc);
         try
         {
            mc.destroy();
         }
         catch (Throwable ignored)
         {
         }
         if (t instanceof ResourceException)
            throw (ResourceException) t;
         else
            throw new ResourceAdapterInternalException("Unable to get connection handle", t);
      }
   }
   
   protected void finalize() throws Throwable
   {
      stop();
   }

   public synchronized void stop()
   {
      if (pool.size() != 0)
      {
         for (Iterator i = pool.iterator(); i.hasNext();)
         {
            ManagedConnection mc = (ManagedConnection) i.next();
            try
            {
               mc.destroy();
            }
            catch (Throwable ignored)
            {
               log.debug("Error stopping pool", ignored);
            }
         }
      }
   }
   
   /**
    * Get a managed connection
    * 
    * @param mcf the managed connection factory
    * @param subject the subject
    * @param cri the connection request info
    * @return the managed connection
    */
   protected synchronized ManagedConnection getManagedConnection(ManagedConnectionFactory mcf, Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      boolean trace = log.isTraceEnabled();
      
      // Try the pool
      if (pool.size() != 0)
      {
         ManagedConnection mc = mcf.matchManagedConnections(pool, subject, cri);
         if (mc != null)
         {
            pool.remove(mc);
            checkedOut.add(mc);
            if (trace)
               log.trace("Got managed connection from pool " + mc);
            return mc;
         }
      }
      
      ManagedConnection mc = mcf.createManagedConnection(null, cri);
      checkedOut.add(mc);
      PoolingConnectionEventListener listener = new PoolingConnectionEventListener(this, mc);
      mc.addConnectionEventListener(listener);
      if (trace)
         log.trace("Created new managed connection " + mc);
      return mc;
   }
   
   protected synchronized void returnManagedConnection(ManagedConnection mc) throws ResourceException
   {
      pool.add(mc);
      checkedOut.remove(mc);
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Return managed connection to the pool " + mc);
   }
   
   protected synchronized void removeManagedConnection(ManagedConnection mc)
   {
      boolean trace = log.isTraceEnabled();
      boolean removed = pool.remove(mc);
      if (trace && removed)
         log.trace("Removed destroyed managed connection from the pool " + mc);
      checkedOut.remove(mc);
      if (trace && removed)
         log.trace("Removed destroyed managed connection that was checked out " + mc);
   }
}
