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
package org.jboss.rars.advice;

import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;
import org.jboss.util.JBossStringBuilder;

/**
 * AbstractRARInterceptor.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class AbstractRARInterceptor implements Interceptor
{
   /** The log */
   protected final Logger log = Logger.getLogger(getClass());

   public String getName()
   {
      return getClass().getName();
   }

   protected static String format(Invocation invocation)
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      format(buffer, invocation);
      return buffer.toString();
   }
   
   protected static void format(JBossStringBuilder buffer, Invocation invocation)
   {
      if (invocation instanceof MethodInvocation)
      {
         MethodInvocation mi = (MethodInvocation) invocation;
         Method method = mi.getMethod();
         buffer.append("[method=").append(method.getDeclaringClass().getName());
         buffer.append('.').append(method.getName());
         buffer.append('(');
         Class[] parameters = method.getParameterTypes();
         for (int i = 0; i < parameters.length; ++i)
         {
            if (i > 0)
               buffer.append(", ");
            buffer.append(parameters[i].getName());
         }
         buffer.append(')');
         buffer.append(" target=").append(mi.getTargetObject());
         buffer.append(']');
      }
      else
         buffer.append(invocation);
   }
}
