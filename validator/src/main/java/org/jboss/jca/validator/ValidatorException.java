/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.validator;

import java.util.Collection;

/**
 * Represents a failure in the validator chain
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 */
public class ValidatorException extends Exception
{
   /** SerialVersionUID */
   private static final long serialVersionUID = 6426899185131726483L;

   /** The collection of failures */
   private final Collection<Failure> failures;

   /**
    * Constructs a new exception with the specified detail message.
    * @param message The message
    * @param failures collection of failures 
    */
   public ValidatorException(String message, Collection<Failure> failures)
   {
      super(message);
      this.failures = failures;
   }

   /**
    * Constructs a new exception with the specified detail message.
    * @param message The message
    * @param failures collection of failures
    * @param cause the cause to be chained
    */
   public ValidatorException(String message, Collection<Failure> failures, Throwable cause)
   {
      super(message, cause);
      this.failures = failures;
   }

   /**
    * Get the collection of failures
    * @return failures
    */
   public synchronized Collection<Failure> getFailures()
   {
      return failures;
   }
}
