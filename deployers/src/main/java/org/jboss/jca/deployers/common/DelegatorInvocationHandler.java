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
package org.jboss.jca.deployers.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.naming.Reference;

/**
 * A delegator invocation handler
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DelegatorInvocationHandler implements InvocationHandler
{
   /** Delegate */
   private Object delegate;

   /** Reference */
   private Reference reference;

   /**
    * Constructor
    * @param delegate The delegate
    */
   public DelegatorInvocationHandler(Object delegate)
   {
      this.delegate = delegate;
      this.reference = null;
   }

   /**
    * {@inheritDoc}
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if ("getReference".equals(method.getName()))
      {
         return reference;
      }
      else if ("setReference".equals(method.getName()))
      {
         this.reference = (Reference)args[0];

         if (delegate instanceof javax.resource.Referenceable)
         {
            ((javax.resource.Referenceable)delegate).setReference(this.reference);
         }

         return null;
      }
      else
      {
         return method.invoke(delegate, args);
      }
   }
}
