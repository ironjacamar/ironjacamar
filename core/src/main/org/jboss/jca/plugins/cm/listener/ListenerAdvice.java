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
package org.jboss.jca.plugins.cm.listener;

import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.jca.plugins.advice.AbstractJCAInterceptor;
import org.jboss.jca.spi.ResourceExceptionUtil;
import org.jboss.jca.spi.cm.ManagedConnectionContext;
import org.jboss.jca.spi.cm.ManagedConnectionContextManager;

/**
 * ConnectionManager listener advice.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class ListenerAdvice extends AbstractJCAInterceptor
{
   /** The ManagedConnectionContextManager */
   protected ManagedConnectionContextManager contextManager;

   /**
    * Get the contextManager.
    * 
    * @return the contextManager.
    */
   public ManagedConnectionContextManager getContextManager()
   {
      return contextManager;
   }

   /**
    * Set the contextManager.
    * 
    * @param contextManager the context manager.
    */
   public void setContextManager(ManagedConnectionContextManager contextManager)
   {
      this.contextManager = contextManager;
   }
   
   public Object invoke(Invocation invocation) throws Throwable
   {
      if (contextManager == null)
         throw new ApplicationServerInternalException("No context manager");
      
      MethodInvocation mi = (MethodInvocation) invocation; 
      ManagedConnectionFactory mcf = (ManagedConnectionFactory) mi.getArguments()[0];
      ConnectionRequestInfo cri = (ConnectionRequestInfo) mi.getArguments()[1];
      Subject subject = null; // TODO Subject
      
      boolean trace = log.isTraceEnabled();
      
      if (trace)
         log.trace("Create ManagedConnection mcf=" + mcf + " subject=" + subject + " cri=" + cri);
      ManagedConnection mc = null;
      try
      {
         mc = mcf.createManagedConnection(subject, cri);
         if (mc == null)
            throw new ResourceAdapterInternalException("Null ManagedConnection from " + mcf);
         if (trace)
            log.trace("Created ManagedConnection result=" + mc);
      }
      catch (Throwable t)
      {
         throw ResourceExceptionUtil.checkResourceAdapterInternal(t);
      }

      if (trace)
         log.trace("Create ManagedConnectionContext manager=" + contextManager + " mc=" + mc);
      try
      {
         ManagedConnectionContext context = contextManager.createManagedConnectionContext(mcf, mc);
         if (context == null)
            throw new ApplicationServerInternalException("Null ManagedConnectionContext from " + contextManager);
         if (trace)
            log.trace("Created ManagedConnectionContext result=" + context);
         return context;
      }
      catch (Throwable t)
      {
         ManagedConnectionContext.safeDestroy(mc);
         throw ResourceExceptionUtil.checkApplicationServerInternal(t);
      }
   }
}
