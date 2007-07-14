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
package org.jboss.jca.plugins.cm.pool.simple;

import java.util.ArrayList;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.jboss.jca.spi.cm.ManagedConnectionContext;
import org.jboss.jca.spi.pool.ManagedConnectionContextPool;
import org.jboss.logging.Logger;

/**
 * SimplePool.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class SimplePool implements ManagedConnectionContextPool
{
   /** The log */
   private static final Logger log = Logger.getLogger(SimplePool.class);
   
   /** The pool */
   private List pool = new ArrayList(); 
   
   public ManagedConnectionContext getManagedConnectonContext(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      ManagedConnectionContext context = null;
      while (true)
      {
         synchronized (pool)
         {
            int size = pool.size();
            if (size == 0)
               return null;
            context = (ManagedConnectionContext) pool.remove(size-1);
         }
         
         if (context.match(subject, cri))
            return context;
         else
         {
            log.warn("Destroying unmatched connection context=" + context + " subject=" + subject + " cri=" + cri, new Throwable("STACKTRACE"));
            context.destroy();
         }
      }
   }

   public void removeManagedConnectonContext(ManagedConnectionContext context)
   {
      synchronized (pool)
      {
         pool.remove(context);
         if (log.isTraceEnabled())
            log.trace("Removed context=" + context + " size=" + pool.size());
      }
   }

   public void returnManagedConnectonContext(ManagedConnectionContext context)
   {
      synchronized (pool)
      {
         pool.add(context);
         if (log.isTraceEnabled())
            log.trace("Add context=" + context + " size=" + pool.size());
      }
   }
}
