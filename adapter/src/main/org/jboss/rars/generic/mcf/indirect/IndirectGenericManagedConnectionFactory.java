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
import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ResourceAdapterInternalException;

import org.jboss.rars.generic.cf.GenericConnectionFactory;
import org.jboss.rars.generic.mcf.GenericManagedConnection;
import org.jboss.rars.generic.mcf.GenericManagedConnectionFactory;
import org.jboss.rars.generic.wrapper.GenericConnection;

/**
 * DirectGenericManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class IndirectGenericManagedConnectionFactory extends GenericManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2214678871251343168L;
   
   /** Our close method */
   private static final Method CLOSE_FROM_INDIRECT_CONNECTION;

   /** The indirect connection interfaces */
   private Class[] indirectConnectionInterfaces;
   
   static
   {
      try
      {
         CLOSE_FROM_INDIRECT_CONNECTION = RealConnection.class.getMethod("closeFromIndirectConnection", null);
      }
      catch (NoSuchMethodException e)
      {
         throw new ExceptionInInitializerError(e);
      }
   }
   
   protected GenericConnectionFactory createGenericConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      return new IndirectGenericConnectionFactory(this, cxManager);
   }
   
   /**
    * Create the indirect connection
    * 
    * @param cm the connecton manager
    * @param cri the connection request info
    * @return the indirect connection
    * @throws Throwable for any error
    */
   public Object createIndirectConnection(ConnectionManager cm, ConnectionRequestInfo cri) throws Throwable
   {
      IndirectGenericConnection impl = new IndirectGenericConnection(this, cm, cri);

      Class[] interfaces = getIndirectConnectionInterfaces(impl);
      Object connection = impl.createProxy(interfaces, impl);
      return connection;
   }

   /**
    * Get the indirect connection interfaces
    * 
    * @param impl the implemetation
    * @return the interfaces
    * @throws ResourceException for any error
    */
   private Class[] getIndirectConnectionInterfaces(IndirectGenericConnection impl) throws ResourceException
   {
      if (indirectConnectionInterfaces == null)
      {
         Set interfaces = new HashSet();
         addIndirectConnectionInterfaces(impl, interfaces);
         interfaces.add(IndirectConnection.class);
         indirectConnectionInterfaces = (Class[]) interfaces.toArray(new Class[interfaces.size()]);
      }
      return indirectConnectionInterfaces;
   }

   /**
    * Add the connection interfaces
    * 
    * @param connection the connection 
    * @param interfaces the interfaces
    */
   protected abstract void addIndirectConnectionInterfaces(IndirectGenericConnection connection, Set interfaces);
   
   protected void addConnectionInterfaces(GenericConnection connection, Set interfaces) throws ResourceException
   {
      interfaces.add(RealConnection.class);
      super.addConnectionInterfaces(connection, interfaces);
   }

   public boolean isCloseMethod(Method method)
   {
      if (super.isCloseMethod(method))
         return true;
      return CLOSE_FROM_INDIRECT_CONNECTION.equals(method);
   }

   protected void destroyRealConnection(GenericManagedConnection mc) throws ResourceException
   {
      RealConnection connection = (RealConnection) mc.getRealConnection();
      try
      {
         connection.closeFromIndirectConnection();
      }
      catch (Throwable t)
      {
         throw new ResourceAdapterInternalException("Error closing the connection", t);
      }
   }

   /**
    * Report the singleton error
    * 
    * @return the single error
    */
   protected String getSingletonError()
   {
      return "You are only allowed to have one child object open at once";
   }
}
