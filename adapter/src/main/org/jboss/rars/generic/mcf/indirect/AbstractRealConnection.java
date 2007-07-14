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
package org.jboss.rars.generic.mcf.indirect;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;

import org.jboss.rars.generic.mcf.GenericManagedConnection;

/**
 * AbstractRealConnection.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class AbstractRealConnection implements RealConnection
{
   /** The managed connection */
   private GenericManagedConnection mc;

   /** The indirect connection */
   private IndirectGenericConnection indirectConnection;
   
   /**
    * Create a new AbstractRealConnection.
    * 
    * @param mc the managed connection
    */
   public AbstractRealConnection(GenericManagedConnection mc)
   {
      this.mc = mc;
      RealConnectionEventListener listener = new RealConnectionEventListener();
      mc.addConnectionEventListener(listener);
   }

   /**
    * Get the managed connection
    * 
    * @return the managed connection
    */
   public GenericManagedConnection getManagedConnection()
   {
      return mc;
   }

   public void setIndirectGenericConnection(IndirectGenericConnection indirectConnection)
   {
      if (indirectConnection == null)
         throw new IllegalArgumentException("Null indirect connection");
      if (this.indirectConnection != null)
         throw new IllegalStateException("Already have an indirect connection");
      this.indirectConnection = indirectConnection;
   }

   private class RealConnectionEventListener implements ConnectionEventListener
   {
      public void connectionClosed(ConnectionEvent event)
      {
         indirectConnection.realConnectionClosed(event.getConnectionHandle());
         indirectConnection = null;
      }

      public void connectionErrorOccurred(ConnectionEvent event)
      {
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
   }
}
