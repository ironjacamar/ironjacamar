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
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.logging.Logger;
import org.jboss.rars.generic.mcf.GenericManagedConnectionFactory;
import org.jboss.rars.generic.ra.ResourceErrorHandler;
import org.jboss.util.collection.CollectionsFactory;
import org.jboss.util.JBossStringBuilder;
import org.jboss.util.Strings;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * GenericWrapper.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public abstract class GenericWrapper implements ResourceErrorHandler
{
   /** The log */
   private static final Logger log = Logger.getLogger(GenericWrapper.class);

   /** The metadata key */
   public static final String METADATA_KEY = GenericWrapper.class.getName();
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /** The managed connection factory */
   private GenericManagedConnectionFactory mcf;
   
   /** The proxy */
   private Object proxy;
   
   /** Whether we are closed */
   private SynchronizedBoolean closed = new SynchronizedBoolean(false);

   /** The parent */
   private GenericWrapper parent;
   
   /** The children */
   private Set children = CollectionsFactory.createCopyOnWriteSet();
   
   /**
    * Create a new GenericWrapper.
    * 
    * @param mcf the managed connection factory
    */
   protected GenericWrapper(GenericManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
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
    * Set the managed connection factory
    * 
    * @param mcf the managed connection factory
    */
   protected void setManagedConnectionFactory(GenericManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
   }
   
   /**
    * Get the proxy.
    * 
    * @return the proxy.
    */
   public Object getProxy()
   {
      return proxy;
   }

   /**
    * Set the proxy.
    * 
    * @param proxy the proxy.
    */
   protected void setProxy(Object proxy)
   {
      this.proxy = proxy;
   }

   public GenericWrapper getWrapper(GenericManagedConnectionFactory mcf)
   {
      if (getManagedConnectionFactory() != mcf)
         throw new SecurityException("Pass the correct managed connection factory");
      return this;
   }

   /**
    * Whether this is a close method
    * 
    * @param method the method
    * @return true when a close method
    */
   protected boolean isCloseMethod(Method method)
   {
      return getManagedConnectionFactory().isCloseMethod(method);
   }

   /**
    * Whether the handle is closed
    * 
    * @return true when already closed
    */
   protected boolean isClosed()
   {
      return closed.get();
   }
   
   /**
    * Close the handle
    * 
    * @param method the method
    */
   public void closeWrapper(Method method)
   {
      if (closed.set(true) == false)
      {
         if (trace)
            log.trace(this + " CLOSED");
         Set children = getChildren();
         if (children != null && children.size() > 0)
         {
            for (Iterator i = children.iterator(); i.hasNext();)
            {
               GenericWrapper child = (GenericWrapper) i.next();
               removeChild(child);
               child.setParent(null);
               child.invokeClose(null);
            }
         }
         if (parent != null)
         {
            parent.removeChild(this);
            setParent(null);
         }
         handleClose(method);
      }
   }

   /**
    * Invoke close
    * 
    * @param method the method
    */
   protected void invokeClose(Method method)
   {
      getManagedConnectionFactory().invokeClose(this, method, true);
   }
   
   /**
    * Handle the close operation
    * 
    * @param method the method
    */
   protected abstract void handleClose(Method method);
   
   /**
    * Get the wrapped object
    * 
    * @return the wrapped object
    * @throws ResourceException for any error
    */
   public abstract Object getWrappedObject() throws ResourceException;
   
   /**
    * Get the wrapped target
    * 
    * @return the wrapped target
    * @throws ResourceException for any error
    */
   public Object getWrappedTarget() throws ResourceException
   {
      Object result = getWrappedObject();
      if (result instanceof WrappedObject)
         result = ((WrappedObject) result).getWrappedObject();
      return result;
   }

   /**
    * Check whether an error is fatal
    * 
    * @param t the throwable
    */
   protected void checkFatal(Throwable t)
   {
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
      throw getManagedConnectionFactory().error(context, t);
   }

   /**
    * Get the parent
    * 
    * @return the parent
    */
   protected GenericWrapper getParent()
   {
      return parent;
   }

   /**
    * Set the parent
    * 
    * @param parent the parent
    */
   protected void setParent(GenericWrapper parent)
   {
      this.parent = parent;
   }
   
   /**
    * Add a child
    * 
    * @param child the child
    */
   protected void addChild(GenericWrapper child)
   {
      if (trace)
         log.trace(this + " ADDCHILD " + child);
      children.add(child);
   }
   
   /**
    * Remove a child
    * 
    * @param child the child
    */
   protected void removeChild(GenericWrapper child)
   {
      if (trace)
         log.trace(this + " REMOVECHILD " + child);
      children.remove(child);
   }
   
   /**
    * Get the children
    * 
    * @return the children
    */
   protected Set getChildren()
   {
      return children;
   }
   
   /**
    * Check for a parent
    * 
    * @param method the method
    * @return the parent result if it is the parent or null for not the parent
    * @throws Throwable for any error
    */
   protected Object checkParent(Method method) throws Throwable
   {
      if (parent != null && getManagedConnectionFactory().isParent(method, parent, this))
         return parent.getProxy();
      return null;
   }
   
   /**
    * Check for a child
    * 
    * @param method the method
    * @param result the result
    * @return the child result
    * @throws Throwable for any error
    */
   protected Object checkChild(Method method, Object result) throws Throwable
   {
      GenericManagedConnectionFactory mcf = getManagedConnectionFactory();
      if (mcf.isChild(method, result))
      {
         GenericWrapper child = mcf.createGenericChild(result);
         child.setParent(this);
         addChild(child);
         return child.createHandle();
      }
      return result;
   }

   /**
    * Create a handle
    * 
    * @param interfaces the interfaces
    * @return the handle
    * @throws ResourceException for any error
    */
   protected GenericHandle createHandle() throws ResourceException
   {
      Class[] interfaces = mcf.getWrappedInterfaces(this);
      return createHandle(interfaces);
   }

   /**
    * Create a handle
    * 
    * @param interfaces the interfaces
    * @return the handle
    */
   public GenericHandle createHandle(Class[] interfaces) throws ResourceException
   {
      return (GenericHandle) createProxy(interfaces, getWrappedObject());
   }

   /**
    * Create a proxy
    * 
    * @param interfaces the interfaces
    * @param impl the implementation
    * @return the proxy
    */
   public Object createProxy(Class[] interfaces, Object impl) throws ResourceException
   {
      SimpleMetaData metaData = new SimpleMetaData();
      metaData.addMetaData(GenericWrapper.METADATA_KEY, GenericWrapper.METADATA_KEY, this);
      
      Object proxy = mcf.createProxy(interfaces, impl, metaData);
      setProxy(proxy);
      return proxy;
   }

   public String toString()
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      Strings.defaultToString(buffer, this);
      buffer.append('[');
      toString(buffer);
      buffer.append(']');
      return buffer.toString();
   }

   /**
    * Fill in the state
    * 
    * @param buffer the buffer
    */
   protected abstract void toString(JBossStringBuilder buffer);
}
