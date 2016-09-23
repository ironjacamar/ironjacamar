/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.bv;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

/**
 * Serializable validator factory
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SerializableValidatorFactory implements ValidatorFactory, Serializable
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** The validator factory */
   private transient ValidatorFactory validatorFactory;

   /**
    * Constructor
    */
   public SerializableValidatorFactory()
   {
      this(BeanValidationImpl.createValidatorFactory());
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
    * {@inheritDoc}
    */
   public MessageInterpolator getMessageInterpolator()
   {
      return validatorFactory.getMessageInterpolator();
   }

   /**
    * {@inheritDoc}
    */
   public Validator getValidator()
   {
      return validatorFactory.getValidator();
   }

   /**
    * {@inheritDoc}
    */
   public ValidatorContext usingContext()
   {
      return validatorFactory.usingContext();
   }

   /**
    * {@inheritDoc}
    */
   public <T> T unwrap(Class<T> type)
   {
      return validatorFactory.unwrap(type);
   }

   /**
    * {@inheritDoc}
    */
   public ConstraintValidatorFactory getConstraintValidatorFactory()
   {
      return validatorFactory.getConstraintValidatorFactory();
   }

   /**
    * {@inheritDoc}
    */
   public TraversableResolver getTraversableResolver()
   {
      return validatorFactory.getTraversableResolver();
   }

   /**
    * {@inheritDoc}
    */
   public ParameterNameProvider getParameterNameProvider()
   {
      return validatorFactory.getParameterNameProvider();
   }

   /**
    * {@inheritDoc}
    */
   public void close()
   {
      validatorFactory.close();
   }

   /**
    * Write the object - Nothing is written as the validator factory is transient
    * @param out The output stream
    * @exception IOException Thrown if an error occurs
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
   }

   /**
    * Read the object - Nothing is read as the validator factory is transient.
    * A new instance is created
    * @param in The input stream
    * @exception IOException Thrown if an error occurs
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      validatorFactory = BeanValidationImpl.createValidatorFactory();
   }
}
