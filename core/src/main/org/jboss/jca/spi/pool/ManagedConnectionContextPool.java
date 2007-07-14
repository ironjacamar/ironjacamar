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
package org.jboss.jca.spi.pool;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.jboss.jca.spi.cm.ManagedConnectionContext;

/**
 * ManagedConnectionContextPool.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public interface ManagedConnectionContextPool
{
   /**
    * Get a managed connection context
    * 
    * @param subject the subject
    * @param info the connecton request info
    * @return the managed connection context
    * @throws ResourceException for any error
    */
   ManagedConnectionContext getManagedConnectonContext(Subject subject, ConnectionRequestInfo info) throws ResourceException; 

   /**
    * Return a managed connection context
    * 
    * @param context the managed connection context
    */
   void returnManagedConnectonContext(ManagedConnectionContext context); 

   /**
    * Remove a managed connection context
    * 
    * @param context the managed connection context
    */
   void removeManagedConnectonContext(ManagedConnectionContext context); 
}
