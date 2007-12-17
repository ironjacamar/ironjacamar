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

import org.jboss.aop.joinpoint.Invocation;

/**
 * Trace advice.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class TraceAdvice extends AbstractRARInterceptor
{
   public Object invoke(Invocation invocation) throws Throwable
   {
      boolean trace = log.isTraceEnabled();
      long begin = 0;
      if (trace)
      {
         begin = System.currentTimeMillis();
         log.trace(format("BEFORE: ", invocation));
      }
      try
      {
         Object result = invocation.invokeNext();
         if (trace)
         {
            long now = System.currentTimeMillis();
            log.trace(format("AFTER: ", invocation, now-begin, result));
         }
         return result;
      }
      catch (Throwable t)
      {
         if (trace)
         {
            long now = System.currentTimeMillis();
            log.trace(format("AFTER: ", invocation, now-begin), t);
         }
         throw t;
      }
   }
   
   protected static void format(StringBuilder buffer, String context, Invocation invocation)
   {
      buffer.append(context);
      format(buffer, invocation);
   }
   
   protected static void format(StringBuilder buffer, String context, Invocation invocation, long time)
   {
      if (time > 10)
         buffer.append("-------> ");
      format(buffer, context, invocation);
      buffer.append(" time=").append(time).append("ms");
   }
   
   protected static StringBuilder format(String context, Invocation invocation)
   {
      StringBuilder buffer = new StringBuilder();
      format(buffer, context, invocation);
      return buffer;
   }
   
   protected static StringBuilder format(String context, Invocation invocation, long time)
   {
      StringBuilder buffer = new StringBuilder();
      format(buffer, context, invocation, time);
      return buffer;
   }
   
   protected static StringBuilder format(String context, Invocation invocation, long time, Object result)
   {
      StringBuilder buffer = new StringBuilder();
      format(buffer, context, invocation, time);
      buffer.append(" RESULT=").append(result);
      return buffer;
   }
}
