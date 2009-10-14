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

package javax.resource.cci;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A <code>ResourceWarning</code> provides information on warnings related to
 * execution of an interaction with an EIS. Warnings are silently 
 * chained to the object whose method caused it to be reported.
 *
 * @see Interaction#getWarnings
 */
public class ResourceWarning extends javax.resource.ResourceException 
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
         serialVersionUID = 4883365394555743885L;
      }
      else
      {
         serialVersionUID = 1233859030030360576L;
      }
   }

   /**
    * Constructs a new instance with null as its detail message.
    */
   public ResourceWarning() 
   { 
      super(); 
   }

   /**
    * Constructs a new instance with the specified detail message.
    *
    * @param message the detail message.
    */
   public ResourceWarning(String message) 
   {
      super(message);
   }

   /**
    * Constructs a new throwable with the specified cause.
    *
    * @param cause a chained exception of type 
    * <code>Throwable</code>.
    */
   public ResourceWarning(Throwable cause) 
   {
      super(cause);
   }

   /**
    * Constructs a new throwable with the specified detail message and cause.
    *
    * @param message the detail message.
    *
    * @param cause a chained exception of type 
    * <code>Throwable</code>.
    */
   public ResourceWarning(String message, Throwable cause) 
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
   public ResourceWarning(String message, String errorCode) 
   {
      super(message, errorCode);
   }
  
   /**
    * Retrieves the warning chained to this <code>ResourceWarning</code>
    * object.
    *
    * @return next <code>ResourceWarning</code> in the chain; null if none.
    *
    * @deprecated J2SE release 1.4 supports a chained exception facility 
    * that allows any throwable to know about another throwable, if any,
    * that caused it to get thrown. Refer to <code>getCause</code> and 
    * <code>initCause</code> methods of the 
    * <code>java.lang.Throwable</code> class.
    */
   public ResourceWarning getLinkedWarning() 
   {
      try 
      {
         return ((ResourceWarning)getLinkedException());
      } 
      catch (ClassCastException ex) 
      {
         return null;
      }
   }
  
   /**
    * Adds an <code>ResourceWarning</code> object to the end of the chain.
    *
    * @param warning <code>ResourceWarning</code> to be added to the chain.
    *
    * @deprecated J2SE release 1.4 supports a chained exception facility 
    * that allows any throwable to know about another throwable, if any,
    * that caused it to get thrown. Refer to <code>getCause</code> and 
    * <code>initCause</code> methods of the 
    * <code>java.lang.Throwable</code> class.
    */
   public void setLinkedWarning(ResourceWarning warning) 
   {
      setLinkedException(warning);
   }
}
