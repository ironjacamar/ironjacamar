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
package org.jboss.jca.plugins.cm;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.jca.plugins.advice.AbstractJCAInterceptor;

/**
 * Trace advice.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class TraceAdvice extends AbstractJCAInterceptor
{
   public Object invoke(Invocation invocation) throws Throwable
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("BEFORE: " + invocation);
      try
      {
         Object result = invocation.invokeNext();
         if (trace)
            log.trace("AFTER: " + invocation + " RESULT=" + result);
         return result;
      }
      catch (Throwable t)
      {
         if (trace)
            log.trace("AFTER: " + invocation, t);
         throw t;
      }
   }
}
