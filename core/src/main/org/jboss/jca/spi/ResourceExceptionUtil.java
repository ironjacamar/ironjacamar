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
package org.jboss.jca.spi;

import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ResourceAdapterInternalException;

/**
 * A ResourceExceptionUtil.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class ResourceExceptionUtil
{
   /**
    * Checks whether the throwable is a resource exception
    * 
    * @param t the throwable
    * @return any non-ResourceException
    * @throws ResourceException if it is a resource exception
    */
   public static Throwable checkResourceException(Throwable t) throws ResourceException
   {
      if (t instanceof ResourceException)
         throw (ResourceException) t;
      else if (t instanceof SecurityException)
         throw (SecurityException) t;
      else
         return t;
   }

   /**
    * Handles a non resource exception as an application server internal
    * 
    * @param t the throwable
    * @return an application server internal for any non-ResourceException
    * @throws ResourceException if it is a resource exception
    */
   public static ApplicationServerInternalException checkApplicationServerInternal(Throwable t) throws ResourceException
   {
      t = checkResourceException(t);
      ApplicationServerInternalException e = new ApplicationServerInternalException("Unexpected error");
      e.initCause(t);
      return e;
   }

   /**
    * Handles a non resource exception as a resource adapter internal
    * 
    * @param t the throwable
    * @return a resource adapter internal for any non-ResourceException
    * @throws ResourceException if it is a resource exception
    */
   public static ResourceAdapterInternalException checkResourceAdapterInternal(Throwable t) throws ResourceException
   {
      t = checkResourceException(t);
      ResourceAdapterInternalException e = new ResourceAdapterInternalException("Unexpected error in resource adapter");
      e.initCause(t);
      return e;
   }
}
