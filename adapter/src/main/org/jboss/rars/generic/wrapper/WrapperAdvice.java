/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.rars.advice.AbstractRARInterceptor;

/**
 * Wrapper advice.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class WrapperAdvice extends AbstractRARInterceptor
{
   public Object invoke(Invocation invocation) throws Throwable
   {
      MethodInvocation mi = (MethodInvocation) invocation;
      GenericWrapper wrapper = (GenericWrapper) mi.getMetaData(GenericWrapper.METADATA_KEY, GenericWrapper.METADATA_KEY);

      Method method = mi.getMethod();
      if (wrapper.isCloseMethod(method))
      {
         wrapper.closeWrapper(method);
         return null;
      }
      
      // Is this a straight forward invocation?
      Object target = mi.getTargetObject();
      Class targetClass = target.getClass();
      Class declaringClass = method.getDeclaringClass();
      if (declaringClass.isAssignableFrom(targetClass))
         return invocation.invokeNext();
      
      // Try any wrapped target
      Object wrappedObject = wrapper.getWrappedTarget();
      mi.setTargetObject(wrappedObject);
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         mi.setTargetObject(target);
      }
   }
}
