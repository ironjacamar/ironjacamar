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

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.rars.advice.AbstractRARInterceptor;
import org.jboss.rars.generic.ra.ResourceErrorHandler;

/**
 * Wrapper error advice.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class WrapperErrorAdvice extends AbstractRARInterceptor
{
   public Object invoke(Invocation invocation) throws Throwable
   {
      try
      {
         return invocation.invokeNext();
      }
      catch (Throwable t)
      {
         MethodInvocation mi = (MethodInvocation) invocation;
         Object target = mi.getTargetObject();
         GenericWrapper wrapper = (GenericWrapper) mi.getMetaData(GenericWrapper.METADATA_KEY, GenericWrapper.METADATA_KEY);

         // Check for a fatal error
         if (wrapper != null)
         {
            target = wrapper;
            wrapper.checkFatal(t);
         }
         
         // Rethrow as the correct type
         if (target != null && target instanceof ResourceErrorHandler)
         {
            ResourceErrorHandler handler = (ResourceErrorHandler) target;
            handler.throwError(format(mi), t);
         }
         throw t;
      }
   }
}
