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
package org.jboss.rars.cm.simple;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;

import org.jboss.logging.Logger;

/**
 * SimpleConnectionEventListener.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class SimpleConnectionEventListener implements ConnectionEventListener
{
   /** The log */
   private static final Logger log = Logger.getLogger(SimpleConnectionEventListener.class);
   
   /** The managed connection */
   private ManagedConnection mc;
   
   /**
    * Create a new SimpleConnectionEventListener.
    * 
    * @param mc the managed connection
    */
   public SimpleConnectionEventListener(ManagedConnection mc)
   {
      this.mc = mc;
   }
   
   public void connectionClosed(ConnectionEvent event)
   {
      close();
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

   /**
    * Close the managed connection
    */
   protected void close()
   {
      ManagedConnection managedConnection = mc;
      mc = null;
      try
      {
         if (managedConnection != null)
            managedConnection.destroy();
      }
      catch (ResourceException ignored)
      {
         log.trace("Ignored", ignored);
      }
   }
}
