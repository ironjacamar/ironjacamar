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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.jboss.util.JBossStringBuilder;

/**
 * ReflectionUtil.
 *
 * @todo move somewhere else
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
class ReflectionUtil
{
   /**
    * Invoked on the target
    * 
    * @param target the target
    * @param method the method
    * @param args the arguments
    * @return the result
    * @throws Throwable for any error
    */
   public static Object invoke(Object target, Method method, Object[] args) throws Throwable
   {
      try
      {
         return method.invoke(target, args);
      }
      catch (Throwable t)
      {
         throw handleErrors(target, method, args, t);
      }
   }

   /**
    * Handle invocation errors
    * 
    * @param target the target
    * @param method the method
    * @param arguments the arguments
    * @param t the error
    * @return never
    * @throws Throwable the throwable
    */
   public static Throwable handleErrors(Object target, Method method, Object[] arguments, Throwable t) throws Throwable
   {
      if (t instanceof IllegalArgumentException)
      {
         if (target == null)
            throw new IllegalArgumentException("Null target for method " + method);
         Class methodClass = method.getClass();
         Class targetClass = target.getClass();
         if (methodClass.isAssignableFrom(targetClass) == false)
            throw new IllegalArgumentException("Wrong target. " + targetClass + " for " + method);
         ArrayList expected = new ArrayList();
         Class[] parameterTypes = method.getParameterTypes();
         for (int i = 0; i < parameterTypes.length; ++i)
            expected.add(parameterTypes[i].getName());
         ArrayList actual = new ArrayList();
         if (arguments != null)
         {
            for (int i = 0; i < arguments.length; ++i)
            {
               if (arguments[i] == null)
                  actual.add(null);
               else
                  actual.add(arguments[i].getClass().getName());
            }
         }
         throw new IllegalArgumentException("Wrong arguments. " + method.getName() + " expected=" + expected + " actual=" + actual);
      }
      else if (t instanceof InvocationTargetException)
      {
         throw ((InvocationTargetException) t).getTargetException();
      }
      throw t;
   }
   
   protected static void format(JBossStringBuilder buffer, String context, Method method, Object target)
   {
      buffer.append(context);
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
      buffer.append(" target=").append(target);
      buffer.append(']');
   }
   
   protected static JBossStringBuilder format(String context, Method method, Object target)
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      format(buffer, context, method, target);
      return buffer;
   }
   
   protected static JBossStringBuilder format(String context, Method method, Object target, Object result)
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      format(buffer, context, method, target);
      buffer.append(" RESULT=").append(result);
      return buffer;
   }
}
