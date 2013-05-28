/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.bv;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Bean validation implementation backed by Hibernate Validator
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class BeanValidation
{
   /** Validator factory */
   private static final String VALIDATOR_FACTORY = "java:/ValidatorFactory";

   /** Validator */
   private static final String VALIDATOR = "java:/Validator";

   /** The validator factory */
   private ValidatorFactory validatorFactory;

   /** The validator */
   private Validator validator;

   /**
    * Constructor
    */
   public BeanValidation()
   {
      Configuration configuration = Validation.byDefaultProvider().configure();
      Configuration<?> conf = configuration.traversableResolver(new JCATraversableResolver());

      validatorFactory = conf.buildValidatorFactory();
      validator = validatorFactory.getValidator();
   }

   /**
    * Get the validator factory
    * @return The factory
    */
   public ValidatorFactory getValidatorFactory()
   {
      return validatorFactory;
   }

   /**
    * Get the validator
    * @return The validator
    */
   public Validator getValidator()
   {
      return validator;
   }

   /**
    * Start
    * @exception Throwable If an error occurs
    */
   public void start() throws Throwable
   {
      Context context = null;
      try
      {
         context = new InitialContext();

         context.rebind(VALIDATOR_FACTORY, new SerializableValidatorFactory(validatorFactory));
         context.rebind(VALIDATOR, new SerializableValidator(validator));
      }
      finally
      {
         try
         {
            if (context != null)
               context.close();
         }
         catch (NamingException ne)
         {
            // Ignore
         }
      }
   }

   /**
    * Stop
    * @exception Throwable If an error occurs
    */
   public void stop() throws Throwable
   {
      Context context = null;
      try
      {
         context = new InitialContext();

         context.unbind(VALIDATOR);
         context.unbind(VALIDATOR_FACTORY);
      }
      finally
      {
         try
         {
            if (context != null)
               context.close();
         }
         catch (NamingException ne)
         {
            // Ignore
         }
      }
   }
}
