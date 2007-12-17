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
package org.jboss.rars.generic.mcf;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.aop.proxy.container.AOPProxyFactory;
import org.jboss.aop.proxy.container.AOPProxyFactoryParameters;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
import org.jboss.logging.Logger;
import org.jboss.rars.cm.simple.SimpleConnectionManager;
import org.jboss.rars.generic.cf.GenericConnectionFactory;
import org.jboss.rars.generic.cri.NoConnectionRequestInfo;
import org.jboss.rars.generic.cri.SubjectCRIRequestID;
import org.jboss.rars.generic.ra.GenericResourceAdapter;
import org.jboss.rars.generic.wrapper.GenericChild;
import org.jboss.rars.generic.wrapper.GenericConnection;
import org.jboss.rars.generic.wrapper.GenericHandle;
import org.jboss.rars.generic.wrapper.GenericWrapper;

/**
 * GenericManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.7 $
 */
public abstract class GenericManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8813394264813638739L;

   /** The log */
   private static final Logger log = Logger.getLogger(GenericManagedConnectionFactory.class); 

   /** The proxy factory */
   private static final AOPProxyFactory proxyFactory = new GeneratedAOPProxyFactory();

   /** No close method */
   private static final Object NO_CLOSE_METHOD = new Object();
   
   /** Cache of close methods Map<class name, method> */
   private Map<String, Object> closeMethodCache = new ConcurrentHashMap<String, Object>();
   
   /** The writer */
   private transient PrintWriter writer;

   /** The resource adapter */
   private transient GenericResourceAdapter ra;

   /** The connection factory interfaces cache */
   private transient Class<?>[] connectionFactoryInterfaces;

   /** The connection interfaces cache */
   private transient Class<?>[] connectionInterfaces;
   
   /** The wrapped interfaces cache */
   private transient Map<Class<?>, Class<?>[]> wrappedInterfaces = new ConcurrentHashMap<Class<?>, Class<?>[]>();
   
   public Object createConnectionFactory() throws ResourceException
   {
      ConnectionManager cxManager = new SimpleConnectionManager();
      return createConnectionFactory(cxManager);
   }
   
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      GenericConnectionFactory impl = createGenericConnectionFactory(cxManager);

      Class<?>[] interfaces = getConnectionFactoryInterfaces(impl);
      
      Object connectionFactory = createProxy(interfaces, impl, null);
      log.debug(this + " CREATED " + connectionFactory);
      return connectionFactory;
   }

   /**
    * Get the connection factory interfaces
    * 
    * @param impl the implemetation
    * @return the interfaces
    * @throws ResourceException for any error
    */
   private Class<?>[] getConnectionFactoryInterfaces(GenericConnectionFactory impl) throws ResourceException
   {
      if (connectionFactoryInterfaces == null)
      {
         Set<Class<?>> interfaces = new HashSet<Class<?>>();
         addConnectionFactoryInterfaces(impl, interfaces);
         addGenericConnectionFactoryInterfaces(impl, interfaces);
         connectionFactoryInterfaces = interfaces.toArray(new Class[interfaces.size()]);
      }
      return connectionFactoryInterfaces;
   }
   
   /**
    * Create a generic connection factory
    * 
    * @param cxManager the connection manager
    * @return the generic connection factory
    * @throws ResourceException for any error
    */
   protected abstract GenericConnectionFactory createGenericConnectionFactory(ConnectionManager cxManager) throws ResourceException;

   /**
    * Get the connection factory interface
    * 
    * @param impl the generic connection factory
    * @param interfaces add the interfaces to this set
    */
   protected abstract void addConnectionFactoryInterfaces(GenericConnectionFactory impl, Set<Class<?>> interfaces);

   /**
    * Add the generic connection factory interfaces
    * 
    * @param impl the implementation
    * @param interfaces add the interfaces to this set
    * @throws ResourceException for any error
    */
   protected void addGenericConnectionFactoryInterfaces(GenericConnectionFactory impl, Set<Class<?>> interfaces) throws ResourceException
   {
      Class<?> clazz = impl.getClass();
      addInterfaces(clazz, interfaces);
   }

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      Object requestID = getRequestID(subject, cxRequestInfo);
      GenericManagedConnection mc = createManagedConnection(subject, cxRequestInfo, requestID);
      Object connection = createRealConnection(mc, subject, cxRequestInfo, requestID);
      mc.setRealConnection(connection);
      return mc;
   }

   @SuppressWarnings("unchecked")
   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      if (connectionSet != null && connectionSet.size() >= 0)
      {
         Object requestID = getRequestID(subject, cxRequestInfo);
         for (Iterator<GenericManagedConnection> i = connectionSet.iterator(); i.hasNext();)
         {
            GenericManagedConnection managedConnection = i.next();
            if (managedConnection.matches(requestID))
               return managedConnection;
         }
      }
      return null;
   }
   
   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }

   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
      this.ra = (GenericResourceAdapter) ra;
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      return writer;
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      this.writer = out;
   }

   /**
    * Is the method a close method
    * 
    * @param method the method
    * @return true when it is a close method
    */
   public boolean isCloseMethod(Method method)
   {
      return "close".equals(method.getName());
   }

   /**
    * Find close method
    * 
    * @param clazz the clazz
    * @return the method or null when not found
    */
   protected Method findCloseMethod(Class<?> clazz)
   {
      if (clazz.isInterface())
      {
         try
         {
            return clazz.getMethod("close", null);
         }
         catch (NoSuchMethodException ignored)
         {
         }
      }
      Class<?>[] interfaces = clazz.getInterfaces();
      for (int i = 0; i < interfaces.length; ++i)
      {
         Method method = findCloseMethod(interfaces[i]);
         if (method != null)
            return method;
      }
      return null;
   }
   
   /**
    * Invoke close on an object
    * 
    * @param wrapper the wrapper
    * @param method @todo the method
    * @param onProxy true for invoke close on the proxy
    */
   public void invokeClose(GenericWrapper wrapper, Method method, boolean onProxy)
   {
      Object object;
      try
      {
         object = wrapper.getWrappedObject();
      }
      catch (Throwable t)
      {
         log.warn("Unable to get wrapped object to close " + wrapper, t);
         return;
      }
      
      if (object == null)
      {
         log.warn("Wrapped object was null during close " + wrapper);
         return;
      }

      Class<?> clazz = object.getClass();
      Object closeMethod = closeMethodCache.get(clazz.getName());
      if (closeMethod == null)
      {
         if (method != null)
         {
            closeMethod = method;
         }
         else
         {
            closeMethod = findCloseMethod(clazz);
            if (closeMethod == null)
            {
               log.trace("Unable to find close method " + wrapper);
               closeMethod = NO_CLOSE_METHOD;
            }
         }
         closeMethodCache.put(clazz.getName(), closeMethod);
      }
      
      if (method == null)
      {
         if (closeMethod == NO_CLOSE_METHOD)
         {
            if (onProxy)
               wrapper.closeWrapper(null);
            return;
         }

         method = (Method) closeMethod;
      }
      
      Object target = object;
      if (onProxy)
         target = wrapper.getProxy();
      try
      {
         ReflectionUtil.invoke(target, method, null);
      }
      catch (Throwable t)
      {
         log.trace("Ignored error during close " + target, t);
      }
   }
   
   /**
    * Whether this result is the parent
    * 
    * @param method the method
    * @param parentWrapper the parent wrapper
    * @param child the target child of the method
    * @return true when result is parent
    * @throws ResourceException for any error
    */
   public boolean isParent(Method method, GenericWrapper parentWrapper, GenericWrapper child) throws ResourceException
   {
      Class<?> returnType = method.getReturnType();
      Object parent = parentWrapper.getWrappedTarget();
      return returnType.isInstance(parent);
   }

   /**
    * Whether this result is a child
    * 
    * @param method the method
    * @param object the object
    * @return true when a child
    */
   public boolean isChild(Method method, Object object)
   {
      Class<?> returnType = method.getReturnType();
      if (ignoreChild(method, object, returnType))
         return false;
      return isChild(method, object, returnType);
   }
   
   /**
    * Whether we should ignore a child type
    * 
    * @param method the method
    * @param object the object
    * @param type the type
    * @return true when it should be ignored
    */
   protected boolean ignoreChild(Method method, Object object, Class<?> type)
   {
      if (Collection.class.isAssignableFrom(type))
         return true;
      if (Map.class.isAssignableFrom(type))
         return true;
      if (Iterator.class.isAssignableFrom(type))
         return true;
      if (Enumeration.class.isAssignableFrom(type))
         return true;
      return false;
   }
   
   /**
    * Whether we this is a child
    * 
    * @param method the method
    * @param object the object
    * @param type the type
    * @return true when it is a child
    */
   protected boolean isChild(Method method, Object object, Class<?> type)
   {
      return type.isInterface();
   }
   
   /**
    * Whether an error is fatal
    * 
    * @param t the error
    * @return true when the error is fatal
    */
   protected boolean isFatal(Throwable t)
   {
      if (t instanceof RuntimeException)
         return true;
      if (t instanceof Error)
         return true;
      return false;
   }
   
   /**
    * Map an error
    * 
    * @param context the context
    * @param t the error
    * @return the mapped error
    */
   public Throwable error(Object context, Throwable t)
   {
      return t;
   }

   /**
    * Create a real connection
    * 
    * @param mc the managed connection
    * @param subject the subject
    * @param cxRequestInfo the connection request info
    * @param requestID the request id
    * @return the connection
    * @throws ResourceException for any error
    */
   protected abstract Object createRealConnection(GenericManagedConnection mc, Subject subject, ConnectionRequestInfo cxRequestInfo, Object requestID) throws ResourceException;

   /**
    * Destroy a real connection
    * 
    * @param mc the managed connection
    * @throws ResourceException for any error
    */
   protected abstract void destroyRealConnection(GenericManagedConnection mc) throws ResourceException;

   /**
    * Create a managed connection
    * 
    * @param subject the subject
    * @param cxRequestInfo the connection request info
    * @param requestID the request id
    * @return the managed connection
    * @throws ResourceException for any error
    */
   protected GenericManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo, Object requestID) throws ResourceException
   {
      return new GenericManagedConnection(this, requestID);
   }

   /**
    * Get a connection request id
    * 
    * @param subject the subject
    * @param cxRequestInfo the request info
    * @return the request id
    * @throws ResourceException for any error
    */
   protected Object getRequestID(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return new SubjectCRIRequestID(subject, cxRequestInfo);
   }
   
   /**
    * Get the connection request info
    *
    * @param cf the connection factory
    * @param method the method
    * @param args the arguments
    * @return the connection request info
    * @throws ResourceException for any error
    */
   public ConnectionRequestInfo getConnectionRequestInfo(GenericConnectionFactory cf, Method method, Object[] args) throws ResourceException
   {
      return NoConnectionRequestInfo.NONE;
   }

   /**
    * Create the generic connection
    * 
    * @param mc the managed connection
    * @param subject the subject
    * @param cri the connection request info
    * @return the connection
    * @throws ResourceException for any error
    */
   protected GenericConnection createGenericConnection(GenericManagedConnection mc, Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      return new GenericConnection(mc);
   }

   /**
    * Get the connection interfaces
    * 
    * @param impl the implementation
    * @return the interfaces
    * @throws ResourceException for any error
    */
   protected Class<?>[] getConnectionInterfaces(GenericConnection impl) throws ResourceException
   {
      if (connectionInterfaces == null)
      {
         Set<Class<?>> interfaces = new HashSet<Class<?>>();
         addConnectionInterfaces(impl, interfaces);
         interfaces.add(GenericHandle.class);
         connectionInterfaces = interfaces.toArray(new Class[interfaces.size()]);
      }
      return connectionInterfaces;
   }

   /**
    * Create a generic child
    * 
    * @param impl the implementation
    * @return the child
    * @throws ResourceException for any error
    */
   public GenericWrapper createGenericChild(Object impl) throws ResourceException
   {
      return new GenericChild(this, impl);
   }

   /**
    * Add the connection object's interfaces
    * 
    * @param connection the connection
    * @param interfaces add the interfaces to this set
    * @throws ResourceException for any error
    */
   protected void addConnectionInterfaces(GenericConnection connection, Set<Class<?>> interfaces) throws ResourceException
   {
      Object object = connection.getWrappedTarget();
      Class<?> clazz = object.getClass();
      addInterfaces(clazz, interfaces);
   }

   /**
    * Get the wrapped object's interfaces
    * 
    * @param wrapper the wrapper
    * @return the interfaces
    * @throws ResourceException for any error
    */
   public Class<?>[] getWrappedInterfaces(GenericWrapper wrapper) throws ResourceException
   {
      Object object = wrapper.getWrappedTarget();
      Class<?> clazz = object.getClass();
      Class<?>[] result = wrappedInterfaces.get(clazz);
      if (result == null)
      {
         Set<Class<?>> interfaces = new HashSet<Class<?>>();
         addInterfaces(clazz, interfaces);
         interfaces.add(GenericHandle.class);
         result = interfaces.toArray(new Class[interfaces.size()]);
         wrappedInterfaces.put(clazz, result);
      }
      return result;
   }

   /**
    * Add the implementation interfaces
    * 
    * @param clazz the class
    * @param interfaces add the interfaces to this set
    * @throws ResourceException for any error
    */
   private void addInterfaces(Class<?> clazz, Set<Class<?>> interfaces) throws ResourceException
   {
      if (clazz == null)
         return;
      Class<?>[] intfs = clazz.getInterfaces();
      for (int i = 0; i < intfs.length; ++i)
         interfaces.add(intfs[i]);
      addInterfaces(clazz.getSuperclass(), interfaces);
   }
   
   /**
    * Create a proxy
    * 
    * @param interfaces the interfaces
    * @param impl the implementation
    * @param metaData the metadata
    * @return the proxy
    */
   public Object createProxy(Class<?>[] interfaces, Object impl, SimpleMetaData metaData)
   {
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setObjectAsSuperClass(true);
      params.setInterfaces(interfaces);
      params.setSimpleMetaData(metaData);
      params.setTarget(impl);
      
      return proxyFactory.createAdvisedProxy(params);
   }
}
