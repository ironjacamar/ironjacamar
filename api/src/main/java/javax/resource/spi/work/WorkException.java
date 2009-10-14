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

package javax.resource.spi.work;

/**
 * A common base class for all <code>Work</code> processing related exceptions.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public class WorkException extends javax.resource.ResourceException 
{
   /** Serial version uid */
   private static final long serialVersionUID = 6360106395593829940L;

   /**
    * Indicates an internal error condition.
    */
   public static final String INTERNAL = "-1";
   
   /**
    * Undefined error code.
    */
   public static final String UNDEFINED = "0";
   
   /**
    * Indicates start timeout expiration.
    */
   public static final String START_TIMED_OUT = "1";
   
   /**
    * Indicates that concurrent work within a transaction is
    * disallowed. That is, there is already another <code>Work</code>
    * instance associated with the specified transaction context.
    */
   public static final String TX_CONCURRENT_WORK_DISALLOWED = "2";
   
   /**
    * Indicates a failure in recreating the specified transaction context.
    */
   public static final String TX_RECREATE_FAILED = "3";
   
   /**
    * Constructs a new instance with null as its detail message.
    */
   public WorkException() 
   {
      super();
   }
   
   /**
    * Constructs a new instance with the specified detail message.
    *
    * @param message the detail message.
    */
   public WorkException(String message) 
   {
      super(message);
   }

   /**
    * Constructs a new throwable with the specified cause.
    *
    * @param cause a chained exception of type
    * <code>Throwable</code>.
    */
   public WorkException(Throwable cause) 
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
   public WorkException(String message, Throwable cause) 
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
   public WorkException(String message, String errorCode)
   {
      super(message, errorCode);
   }
}
