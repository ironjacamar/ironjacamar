/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

import org.jboss.logging.Logger;

/**
 * Serializable validator factory
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class SerializableValidatorFactory implements ValidatorFactory, Serializable
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger(SerializableValidatorFactory.class);

   /** The validator factory */
   private transient ValidatorFactory validatorFactory;

   /**
    * Constructor
    */
   public SerializableValidatorFactory()
   {
      this(BeanValidationUtil.createValidatorFactory());
   }

   /**
    * Constructor
    * @param vf The validator factory
    */
   public SerializableValidatorFactory(ValidatorFactory vf)
   {
      this.validatorFactory = vf;
   }

   /**
    * Get the message interpolator
    * @return The interpolator
    */
   public MessageInterpolator getMessageInterpolator()
   {
      return validatorFactory.getMessageInterpolator();
   }

   /**
    * Get the validator
    * @return The validator
    */
   public Validator getValidator()
   {
      return validatorFactory.getValidator();
   }

   /**
    * Get the validator context
    * @return The context
    */
   public ValidatorContext usingContext()
   {
      return validatorFactory.usingContext();
   }

   /**
    * Unwrap
    * @param type The type
    * @return The context
    */
   public <T> T unwrap(Class<T> type)
   {
      return validatorFactory.unwrap(type);
   }

   /**
    * Get the constraint validator factory
    * @return The factory
    */
   public ConstraintValidatorFactory getConstraintValidatorFactory()
   {
      return validatorFactory.getConstraintValidatorFactory();
   }

   /**
    * Get the traversable resolver
    * @return The resolver
    */
   public TraversableResolver getTraversableResolver()
   {
      return validatorFactory.getTraversableResolver();
   }

   /**
    * Write the object - Nothing is written as the validator factory is transient
    * @param out The output stream
    * @exception IOException Thrown if an error occurs
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
   }

   /**
    * Read the object - Nothing is read as the validator factory is transient.
    * A new instance is created
    * @param out The output stream
    * @exception IOException Thrown if an error occurs
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      validatorFactory = BeanValidationUtil.createValidatorFactory();
   }
}
