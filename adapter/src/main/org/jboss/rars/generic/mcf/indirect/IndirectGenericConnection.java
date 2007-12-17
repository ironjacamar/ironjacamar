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

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.IllegalStateException;

import org.jboss.logging.Logger;
import org.jboss.rars.generic.wrapper.GenericWrapper;

/**
 * IndirectGenericConnection.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class IndirectGenericConnection extends GenericWrapper
{
   /** The log */
   private static final Logger log = Logger.getLogger(IndirectGenericConnection.class);
   
   /** The connection request info */
   private ConnectionRequestInfo cri;
   
   /** The connection manager */
   private ConnectionManager cm;
   
   /** The real connection */
   private RealConnection realConnection;
   
   /**
    * Create a new IndirectGenericConnection.
    * 
    * @param mcf the managed connection factory
    * @param cm the connection manager
    * @param cri the connection request info
    */
   public IndirectGenericConnection(IndirectGenericManagedConnectionFactory mcf, ConnectionManager cm, ConnectionRequestInfo cri)
   {
      super(mcf);
      this.cm = cm;
      this.cri = cri;
   }

   /**
    * Get the connectionManager.
    * 
    * @return the connectionManager.
    */
   public ConnectionManager getConnectionManager()
   {
      return cm;
   }

   /**
    * Get the connectionRequestInfo.
    * 
    * @return the connectionRequestInfo.
    */
   public ConnectionRequestInfo getConnectionRequestInfo()
   {
      return cri;
   }

   public Object allocateConnection(ConnectionRequestInfo cri) throws ResourceException
   {
      IndirectGenericManagedConnectionFactory mcf = (IndirectGenericManagedConnectionFactory) getManagedConnectionFactory();

      if (realConnection != null)
         throw new IllegalStateException(mcf.getSingletonError());
      
      Object handle = cm.allocateConnection(mcf, cri);
      if (handle instanceof RealConnection == false)
         throw new IllegalStateException("Not a RealConnection " + handle + " interfaces=" + Arrays.asList(handle.getClass().getInterfaces()));
      realConnection = (RealConnection) handle;
      realConnection.setIndirectGenericConnection(this);
      return realConnection;
   }
   
   public Object getWrappedObject() throws ResourceException
   {
      return this;
   }

   /**
    * Notification that the real connection has been closed
    * 
    * @param realConnection the real connection
    */
   public void realConnectionClosed(Object realConnection)
   {
      if (this.realConnection == realConnection)
         this.realConnection = null;
   }
   
   protected void handleClose(Method method)
   {
      if (realConnection != null)
      {
         try
         {
            realConnection.closeFromIndirectConnection();
         }
         catch (Throwable ignored)
         {
            log.trace("Ignored error in close", ignored);
         }
      }
   }

   protected void toString(StringBuilder buffer)
   {
      buffer.append(cri);
   }
}
