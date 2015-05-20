/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.validator;

import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Represents a failure in the validator chain
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ValidatorException extends Exception
{
   /** SerialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The collection of failures */
   private Collection<Failure> failures;

   /** The resource bundle */
   private ResourceBundle rb;
   
   /**
    * Constructs a new exception with the specified detail message.
    * @param message The message
    * @param failures collection of failures
    * @param rb The resource bundle
    */
   public ValidatorException(String message, Collection<Failure> failures, ResourceBundle rb)
   {
      super(message);
      this.failures = failures;
      this.rb = rb;
   }

   /**
    * Constructs a new exception with the specified detail message.
    * @param message The message
    * @param failures collection of failures
    * @param rb The resource bundle
    * @param cause the cause to be chained
    */
   public ValidatorException(String message, Collection<Failure> failures, ResourceBundle rb, Throwable cause)
   {
      super(message, cause);
      this.failures = failures;
      this.rb = rb;
   }

   /**
    * Get the collection of failures
    * @return The failures
    */
   public Collection<Failure> getFailures()
   {
      return failures;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(8192);
      FailureHelper fh = new FailureHelper(failures);

      sb = sb.append(super.toString());
      sb = sb.append(fh.asText(rb));

      return sb.toString();
   }
}
