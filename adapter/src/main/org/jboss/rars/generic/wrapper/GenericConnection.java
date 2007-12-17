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
package org.jboss.rars.generic.wrapper;

import java.lang.reflect.Method;

import javax.resource.ResourceException;
import javax.resource.spi.ResourceAdapterInternalException;

import org.jboss.logging.Logger;
import org.jboss.rars.generic.mcf.GenericManagedConnection;

/**
 * GenericConnection.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class GenericConnection extends GenericWrapper implements GenericHandle
{
   /** The log */
   private static final Logger log = Logger.getLogger(GenericConnection.class);
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /** The managed connection */
   private GenericManagedConnection managedConnection;
   
   /**
    * Create a new GenericConnection.
    * 
    * @param managedConnection the managed connection
    */
   public GenericConnection(GenericManagedConnection managedConnection)
   {
      super(managedConnection.getManagedConnectionFactory());
      setManagedConnection(managedConnection);
      if (trace)
         log.trace(this + " CREATED");
   }
   
   /**
    * Get the managed connection
    * 
    * @return the managed connection
    */
   protected GenericManagedConnection getManagedConnection()
   {
      return managedConnection;
   }

   /**
    * Set the managed connection
    * 
    * @param managedConnection the managed connection
    */
   public void setManagedConnection(GenericManagedConnection managedConnection)
   {
      this.managedConnection = managedConnection;
      if (managedConnection != null)
         setManagedConnectionFactory(managedConnection.getManagedConnectionFactory());
   }

   public Object getWrappedObject() throws ResourceException
   {
      GenericManagedConnection managedConnection = getManagedConnection();
      if (managedConnection == null)
         throw new ResourceAdapterInternalException("Connection is not associated with a managed connection");
      return managedConnection.getRealConnection();
   }

   protected void checkFatal(Throwable t)
   {
      // Looks like it is already broken and handled
      if (managedConnection == null)
         return;
      managedConnection.checkFatal(t);
   }

   protected void handleClose(Method method)
   {
      GenericManagedConnection managedConnection = getManagedConnection();
      if (managedConnection == null)
         return;
      managedConnection.close(this);
   }
   
   protected void toString(StringBuilder buffer)
   {
      GenericManagedConnection managedConnection = getManagedConnection();
      if (managedConnection == null)
         buffer.append("null");
      else
         buffer.append(managedConnection.getRealConnection());
   }
}
