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
package org.jboss.rars.generic.wrapper;

import java.lang.reflect.Method;

import javax.resource.ResourceException;

import org.jboss.logging.Logger;
import org.jboss.rars.generic.mcf.GenericManagedConnectionFactory;
import org.jboss.util.JBossStringBuilder;

/**
 * GenericChild.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class GenericChild extends GenericWrapper implements GenericHandle
{
   /** The log */
   private static final Logger log = Logger.getLogger(GenericChild.class);
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /** The target */
   protected Object target;
   
   /**
    * Create a new GenericChild.
    * 
    * @param mcf the managed connection factory
    * @param target the target
    */
   public GenericChild(GenericManagedConnectionFactory mcf, Object target)
   {
      super(mcf);
      if (target == null)
         throw new IllegalStateException("Null target");
      this.target = target;
      if (trace)
         log.trace(this + " CREATED");
   }

   public Object getWrappedObject() throws ResourceException
   {
      return target;
   }

   protected void handleClose(Method method)
   {
      getManagedConnectionFactory().invokeClose(this, method, false);
   }
   
   protected void toString(JBossStringBuilder buffer)
   {
      buffer.append(target);
   }
}
