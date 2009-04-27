/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.standalone.hack;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * A JavaCompInitializer that can be stopped.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JavaCompInitializer extends org.jboss.naming.JavaCompInitializer
{
   private static Logger log = Logger.getLogger(JavaCompInitializer.class);
   
   /**
    * Stop
    */
   public void stop()
   {
      InitialContext ctx = getIniCtx();
      if (ctx == null)
         return;
      
      try
      {
         ctx.unbind("java:comp");
      }
      catch (NamingException e)
      {
         log.debug("Failed to unbind 'java:comp'", e);
      }
      
      try
      {
         ctx.close();
      }
      catch (NamingException e)
      {
         log.debug("Failed to close InitialContext " + ctx, e);
      }
      
      setIniCtx(null);
   }
}
