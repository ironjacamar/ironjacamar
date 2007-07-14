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
package org.jboss.rars.generic.cf;

import java.lang.reflect.Method;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;

import org.jboss.rars.generic.mcf.GenericManagedConnectionFactory;
import org.jboss.rars.generic.ra.ResourceErrorHandler;

/**
 * GenericConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class GenericConnectionFactory implements ResourceErrorHandler
{
   /** The managed connection factory */
   private GenericManagedConnectionFactory mcf;
   
   /** The delegate connection manager */
   private ConnectionManager cm;
   
   /**
    * Create a new GenericConnectionFactory.
    * 
    * @param mcf the managed connection factory
    * @param cm the connection manager
    */
   public GenericConnectionFactory(GenericManagedConnectionFactory mcf, ConnectionManager cm)
   {
      this.mcf = mcf;
      this.cm = cm;
   }
   
   /**
    * Get the managed connection factory
    * 
    * @return the managed connection factory
    */
   public GenericManagedConnectionFactory getManagedConnectionFactory()
   {
      return mcf;
   }
   
   /**
    * Get the connection manager
    * 
    * @return the connection manager
    */
   public ConnectionManager getConnectionManager()
   {
      return cm;
   }

   /**
    * Get the connection request info
    * 
    * @param method the method
    * @param args the arguments
    * @return the connection request info
    * @throws ResourceException for any error
    */
   public ConnectionRequestInfo getConnectionRequestInfo(Method method, Object[] args) throws ResourceException
   {
      return mcf.getConnectionRequestInfo(this, method, args);
   }

   /**
    * Allocate a connection
    * 
    * @param cri the connection request info
    * @return the connection
    * @throws Exception for any error
    */
   public Object allocateConnection(ConnectionRequestInfo cri) throws ResourceException
   {
      ConnectionManager cm = getConnectionManager();
      GenericManagedConnectionFactory mcf = getManagedConnectionFactory();
      return cm.allocateConnection(mcf, cri);
   }

   /**
    * Report a specific error
    * 
    * @param context the context
    * @param t the throwable
    * @return never
    * @throws the correct error
    */
   public Throwable throwError(Object context, Throwable t) throws Throwable
   {
      throw mcf.error(context, t);
   }

   /**
    * Create a connection
    * 
    * @param cri the connection request info
    * @return the connection
    * @throws Throwable for any error
    */
   public abstract Object createConnection(ConnectionRequestInfo cri) throws Throwable;
}
