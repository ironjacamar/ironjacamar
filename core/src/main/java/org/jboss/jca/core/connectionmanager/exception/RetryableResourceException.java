/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.exception;

import javax.resource.ResourceException;

/**
 * RetryableResourceException
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Revision: 85945 $
 */
public class RetryableResourceException extends ResourceException
{
   /**Serial Version Id*/
   static final long serialVersionUID = -7865510613151405764L;

   /**
    * Constructor
    */
   public RetryableResourceException()
   {
      super();
   }

   /**
    * New exception with message.
    * @param message message
    */
   public RetryableResourceException(String message)
   {
      super(message);
   }

   /**
    * New exception with error message and code.
    * @param message error message
    * @param errorCode error code
    */
   public RetryableResourceException(String message, String errorCode)
   {
      super(message, errorCode);
   }

   /**
    * New exception with error message and throwable cause.
    * @param message error message
    * @param t throwable cause
    */
   public RetryableResourceException(String message, Throwable t)
   {
      super(message, t);
   }

   /**
    * New exception with throwable cause.
    * @param t  throwable cause
    */
   public RetryableResourceException(Throwable t)
   {
      super(t);
   }
}
