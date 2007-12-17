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

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;

import org.jboss.logging.Logger;

/**
 * PoolingConnectionEventListener.
 * 
 * @todo handle counting
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class PoolingConnectionEventListener implements ConnectionEventListener
{
   /** The log */
   private static final Logger log = Logger.getLogger(PoolingConnectionEventListener.class);
   
   /** The connection manager */
   private PoolingConnectionManager cm;
   
   /** The managed connection */
   private ManagedConnection mc;
   
   /**
    * Create a new PoolingConnectionEventListener.
    * 
    * @param cm the connection manager
    * @param mc the managed connection
    */
   public PoolingConnectionEventListener(PoolingConnectionManager cm, ManagedConnection mc)
   {
      this.cm = cm;
      this.mc = mc;
   }
   
   public void connectionClosed(ConnectionEvent event)
   {
      if (mc != null)
      {
         try
         {
            mc.cleanup();
            cm.returnManagedConnection(mc);
         }
         catch (Throwable t)
         {
            log.warn("Error closing connection", t);
            close();
         }
      }
   }
   
   public void connectionErrorOccurred(ConnectionEvent event)
   {
      log.warn("Connection Error occurred for " + mc, event.getException());
      close();
   }

   public void localTransactionStarted(ConnectionEvent event)
   {
   }

   public void localTransactionCommitted(ConnectionEvent event)
   {
   }

   public void localTransactionRolledback(ConnectionEvent event)
   {
   }

   private void close()
   {
      ManagedConnection managedConnection = mc;
      mc = null;
      try
      {
         if (managedConnection != null)
         {
            cm.removeManagedConnection(mc);
            managedConnection.destroy();
         }
      }
      catch (ResourceException ignored)
      {
         log.trace("Ignored", ignored);
      }
   }
}
