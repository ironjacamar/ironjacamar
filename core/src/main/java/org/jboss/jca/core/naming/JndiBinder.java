/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.jca.core.naming;

import org.jboss.jca.core.CoreLogger;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.jboss.logging.Logger;

/**
 * Helper class to create JNDI bindings
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JndiBinder implements ObjectFactory
{
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, JndiBinder.class.getName());

   private static ConcurrentMap<String, Object> objs = new ConcurrentHashMap<String, Object>();

   private String name;
   private Object obj;

   /**
    * Constructor
    */
   public JndiBinder()
   {
   }

   /**
    * Set the name
    * @param v The value
    */
   public void setName(String v)
   {
      this.name = v;
   }

   /**
    * Set the object
    * @param v The value
    */
   public void setObject(Object v)
   {
      this.obj = v;
   }

   /**
    * Obtain the connection factory
    * {@inheritDoc}
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws Exception
   {
      Reference ref = (Reference)obj;
      String objName = (String)ref.get("name").getContent();

      return objs.get(objName);
   }

   /**
    * Bind
    * @exception Throwable Thrown in case of an error
    */
   public void bind() throws Throwable
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");

      if (obj == null)
         throw new IllegalArgumentException("Obj is null");

      if (log.isTraceEnabled())
         log.tracef("Binding %s under %s", obj.getClass().getName(), name);

      Context context = new InitialContext();
      try
      {
         String className = obj.getClass().getName();
         Reference ref = new Reference(className,
                                       new StringRefAddr("class", className),
                                       JndiBinder.class.getName(),
                                       null);
         ref.add(new StringRefAddr("name", name));

         objs.put(name, obj);

         Util.bind(context, name, ref);

         if (log.isDebugEnabled())
            log.debug("Bound " + obj.getClass().getName() + " under " + name);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Unbind
    * @exception Throwable Thrown in case of an error
    */
   public void unbind() throws Throwable
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");

      log.tracef("Unbinding %s", name);

      Context context = null;
      try
      {
         context = new InitialContext();

         Util.unbind(context, name);

         objs.remove(name);

         if (log.isDebugEnabled())
            log.debug("Unbound " + name);
      }
      catch (Throwable t)
      {
         log.exceptionDuringUnbind(t);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }
   }
}
