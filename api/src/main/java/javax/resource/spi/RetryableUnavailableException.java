/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package javax.resource.spi;

/**
 * A subclass of the <code>UnavailableException</code> that 
 * indicates that the rejection of the work submission is transient. 
 * It is thrown in situations where the previously failed work submission 
 * might be able to succeed if the resource adapter retries the operation
 * at a later point in time.
 * 
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */
public class RetryableUnavailableException extends UnavailableException implements RetryableException 
{
   /**
    * Determines if a deserialized instance of this class
    * is compatible with this class.
    */
   private static final long serialVersionUID = 3730185319227786830L;
   
   /**
    * Constructs a new instance with null as its detail message.
    */
   public RetryableUnavailableException() 
   {
      super();
   }
   
   /**
    * Constructs a new instance with the specified detail message.
    * @param message the detail message.
    */
   public RetryableUnavailableException(String message) 
   {
      super(message);
   }
   
   /**
    * Constructs a new throwable with the specified cause.
    * @param cause a chained exception of type <code>Throwable</code>.
    */
   public RetryableUnavailableException(Throwable cause) 
   {
      super(cause);
   }

   /**
    * Constructs a new throwable with the specified detail message and cause.
    * @param message the detail message.
    * @param cause a chained exception of type <code>Throwable</code>.
    */
   public RetryableUnavailableException(String message, Throwable cause) 
   {
      super(message, cause);
   }

   /**
    * Constructs a new throwable with the specified detail message and
    * an error code.
    * @param message a description of the exception.
    * @param errorCode a string specifying the vendor specific error code.
    */
   public RetryableUnavailableException(String message, String errorCode) 
   {
      super(message, errorCode);
   }
}
