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
package org.jboss.jca.spi.cm;

import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
import org.jboss.util.collection.CollectionsFactory;

/**
 * A ManagedConnectionContextManager.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class ManagedConnectionContextManager
{
   /** The log */
   private static final Logger log = Logger.getLogger(ManagedConnectionContextManager.class);
   
   /** The contexts */
   private Set contexts = CollectionsFactory.createCopyOnWriteSet();
   
   /**
    * Create a new managed connection context
    * 
    * @param managedConnectionFactory the managed connection factory
    * @param managedConnection the managed connection
    * @return the managed connection context
    * @throws ResourceException for any error
    */
   public ManagedConnectionContext createManagedConnectionContext(ManagedConnectionFactory managedConnectionFactory, ManagedConnection managedConnection) throws ResourceException
   {
      boolean trace = log.isTraceEnabled();
      
      ManagedConnectionContext context = new ManagedConnectionContext(this, managedConnectionFactory, managedConnection);
      contexts.add(context);
      if (trace)
         log.trace("Created context=" + context + " size=" + contexts.size());
      return context;
   }
   
   /**
    * Remove a managed connection context
    * 
    * @param context the managed connection context
    */
   public void removeManagedConnectionContext(ManagedConnectionContext context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");
      
      boolean trace = log.isTraceEnabled();

      context.destroyContext();
      contexts.remove(context);
      if (trace)
         log.trace("Destroyed context=" + context + " size=" + contexts.size());
   }
}
