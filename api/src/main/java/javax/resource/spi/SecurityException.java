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

package javax.resource.spi;

import java.security.AccessController;
import java.security.PrivilegedAction;

/** 
 * A <code>SecurityException</code> indicates error conditions 
 * related to the security
 * contract between an application server and resource adapter. The common
 * error conditions represented by this exception are:
 * <UL>
 * <LI>Invalid security information (represented as a Subject instance) passed
 *     across the security contract - for example, credentials have expired or
 *     have invalid format.
 * <LI>Lack of support for a specific security mechanism in an EIS or resource 
 *     adapter.
 * <LI>Failure to create a connection to an EIS because of failed 
 *     authentication or authorization.
 * <LI>Failure to authenticate a resource principal to an EIS instance 
 *     or failure 
 *     to establish a secure association with an underlying EIS instance.
 * <LI>Access control exception to indicate that a requested access to an EIS 
 *     resource or a request to create a new connection is denied.
 *  </UL>
 *
 * @version 1.0
 * @author Rahul Sharma
 * @author Ram Jeyaraman
 */

public class SecurityException extends javax.resource.ResourceException 
{
   /** Serial version uid */
   private static final long serialVersionUID;

   static
   {
      Boolean legacy = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Boolean run()
         {
            try
            {
               if (System.getProperty("org.jboss.j2ee.LegacySerialization") != null)
                  return Boolean.TRUE;
            }
            catch (Throwable ignored)
            {
               // Ignore
            }
            return Boolean.FALSE;
         }
      });

      if (Boolean.TRUE.equals(legacy))
      {
         serialVersionUID = 5003890345420586914L;
      }
      else
      {
         serialVersionUID = -7609456980660658364L;
      }
   }
   
   /**
    * Constructs a new instance with null as its detail message.
    */
   public SecurityException() 
   { 
      super(); 
   }
   
   /**
    * Constructs a new instance with the specified detail message.
    *
    * @param message the detail message.
    */
   public SecurityException(String message) 
   {
      super(message);
   }
   
   /**
    * Constructs a new throwable with the specified cause.
    *
    * @param cause a chained exception of type <code>Throwable</code>.
    */
   public SecurityException(Throwable cause) 
   {
      super(cause);
   }

   /**
    * Constructs a new throwable with the specified detail message and cause.
    *
    * @param message the detail message.
    *
    * @param cause a chained exception of type <code>Throwable</code>.
    */
   public SecurityException(String message, Throwable cause) 
   {
      super(message, cause);
   }
   
   /**
    * Constructs a new throwable with the specified detail message and
    * an error code.
    *
    * @param message a description of the exception.
    * @param errorCode a string specifying the vendor specific error code.
    */
   public SecurityException(String message, String errorCode) 
   {
      super(message, errorCode);
   }
}
