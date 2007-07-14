/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jca.plugins.cm.pool;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.jca.plugins.advice.AbstractJCAInterceptor;
import org.jboss.jca.spi.ResourceExceptionUtil;
import org.jboss.jca.spi.cm.ManagedConnectionContext;
import org.jboss.jca.spi.pool.ManagedConnectionContextPool;
import org.jboss.jca.spi.pool.ManagedConnectionContextPoolFactory;

/**
 * ConnectionManager pool advice.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class PoolAdvice extends AbstractJCAInterceptor
{
   protected ManagedConnectionContextPool pool;
   
   /**
    * Create a new PoolAdvice.
    * 
    * @param factory the pool factory
    */
   public PoolAdvice(ManagedConnectionContextPoolFactory factory)
   {
      if (factory == null)
         throw new IllegalArgumentException("Null factory");
      pool = factory.createPool();
   }
   
   public Object invoke(Invocation invocation) throws Throwable
   {
      MethodInvocation mi = (MethodInvocation) invocation; 
      ConnectionRequestInfo cri = (ConnectionRequestInfo) mi.getArguments()[1];
      Subject subject = null; // TODO Subject
      
      boolean trace = log.isTraceEnabled();

      if (trace)
         log.trace("Get ManagedConnectionContext from pool=" + pool + " subject=" + subject + " cri=" + cri);
      try
      {
         ManagedConnectionContext context = pool.getManagedConnectonContext(subject, cri);
         if (context != null)
         {
            context.setInUse(pool);
            if (trace)
               log.trace("Got ManagedConnectionContext result=" + context);
            return context;
         }
         if (trace)
            log.trace("No connection from pool=" + pool);
      }
      catch (Throwable t)
      {
         throw ResourceExceptionUtil.checkApplicationServerInternal(t);
      }
      
      // Create a new connection
      ManagedConnectionContext context = (ManagedConnectionContext) invocation.invokeNext();
      context.setPool(pool);
      if (trace)
         log.trace("Assigned to pool " + context);
      return context;
   }
}
